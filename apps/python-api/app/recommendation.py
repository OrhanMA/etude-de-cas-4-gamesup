from __future__ import annotations

from collections import defaultdict
from math import log1p

import numpy as np
from scipy.sparse import csr_matrix
from sklearn.feature_extraction import DictVectorizer
from sklearn.neighbors import NearestNeighbors

from .models import GameRecommendation, RecommendationResponse, UserData

PURCHASE_WEIGHT = 0.8
CONTENT_WEIGHT = 0.2
MAX_USER_NEIGHBORS = 10


def _features(game: dict[str, object], max_price: float, max_edition: int) -> dict[str, object]:
    """Encode les métadonnées ; auteur et éditeur sont les principaux signaux de contenu."""
    features: dict[str, object] = {
        "genre": str(game["genre"]).strip().casefold(),
        "price": float(game.get("price") or 0.0) / max_price if max_price else 0.0,
        "edition": int(game["edition_number"]) / max_edition if max_edition else 0.0,
    }
    for field in ("author", "publisher"):
        value = game.get(field)
        if value and str(value).strip():
            features[field] = str(value).strip().casefold()
    return features


def _popularity_scores(interactions: list[dict[str, int]]) -> dict[int, float]:
    popularity: dict[int, int] = defaultdict(int)
    for interaction in interactions:
        popularity[interaction["game_id"]] += interaction["interaction_count"]
    if not popularity:
        return {}
    maximum = max(popularity.values())
    return {game_id: count / maximum for game_id, count in popularity.items()}


def generate_recommendations(user_data: UserData) -> RecommendationResponse:
    """Classe les jeux en privilégiant les achats des voisins, puis les métadonnées.

    Spring transmet les achats des clients dont la commande est finalisée. Le premier KNN
    rapproche les clients selon leurs vecteurs d'achats (distance cosinus). Le second mesure
    la proximité des jeux selon l'auteur, l'éditeur, le genre, le prix et l'édition. Les
    tendances d'achat comptent pour 80 % du score final et les caractéristiques du catalogue
    pour 20 %.
    """
    games_by_id = {
        game.game_id: game.model_dump()
        for game in user_data.catalog
    }
    if len(games_by_id) < 2:
        return RecommendationResponse(
            user_id=user_data.user_id,
            strategy="no-candidates",
            recommendations=[],
        )

    interactions_by_pair: dict[tuple[int, int], int] = defaultdict(int)
    for interaction in user_data.interactions:
        if interaction.game_id in games_by_id:
            interactions_by_pair[(interaction.user_id, interaction.game_id)] += (
                interaction.interaction_count
            )
    valid_interactions = [
        {"user_id": user_id, "game_id": game_id, "interaction_count": count}
        for (user_id, game_id), count in interactions_by_pair.items()
    ]
    popularity = _popularity_scores(valid_interactions)
    owned_game_ids = {
        interaction["game_id"]
        for interaction in valid_interactions
        if interaction["user_id"] == user_data.user_id
    }
    if not valid_interactions:
        return RecommendationResponse(
            user_id=user_data.user_id,
            strategy="cold-start",
            recommendations=[],
        )

    user_ids = sorted({interaction["user_id"] for interaction in valid_interactions})
    game_ids = list(games_by_id)
    user_index = {user_id: index for index, user_id in enumerate(user_ids)}
    game_index = {game_id: index for index, game_id in enumerate(game_ids)}
    matrix_rows: list[int] = []
    matrix_columns: list[int] = []
    matrix_values: list[float] = []
    for interaction in valid_interactions:
        matrix_rows.append(user_index[interaction["user_id"]])
        matrix_columns.append(game_index[interaction["game_id"]])
        matrix_values.append(log1p(interaction["interaction_count"]))
    user_game_matrix = csr_matrix(
        (matrix_values, (matrix_rows, matrix_columns)),
        shape=(len(user_ids), len(game_ids)),
        dtype=float,
    )

    target_index = user_index.get(user_data.user_id)
    target_interactions = {
        game_id: count
        for (user_id, game_id), count in interactions_by_pair.items()
        if user_id == user_data.user_id
    }
    if not target_interactions or target_index is None:
        return _recommend_by_popularity(user_data, games_by_id, popularity, owned_game_ids)

    excluded_game_ids = set(target_interactions)
    neighbor_scores: dict[int, float] = defaultdict(float)
    if len(user_ids) > 1:
        neighbor_count = min(MAX_USER_NEIGHBORS + 1, len(user_ids))
        user_knn = NearestNeighbors(n_neighbors=neighbor_count, metric="cosine", algorithm="brute")
        user_knn.fit(user_game_matrix)
        distances, indices = user_knn.kneighbors(user_game_matrix[target_index])
        similarity_sum = 0.0
        neighbors: list[tuple[int, float]] = []
        for distance, index in zip(distances[0], indices[0], strict=True):
            neighbor_id = user_ids[int(index)]
            if neighbor_id == user_data.user_id:
                continue
            similarity = max(0.0, 1.0 - float(distance))
            if similarity > 0:
                neighbors.append((int(index), similarity))
                similarity_sum += similarity

        for index, similarity in neighbors:
            row = user_game_matrix[index].toarray()[0]
            for column, value in enumerate(row):
                game_id = game_ids[column]
                if value > 0 and game_id not in excluded_game_ids:
                    neighbor_scores[game_id] += similarity * float(value)
        if similarity_sum:
            neighbor_scores = defaultdict(
                float,
                {game_id: score / similarity_sum for game_id, score in neighbor_scores.items()},
            )

    content_scores = _content_similarity_scores(
        games_by_id,
        target_interactions,
    )
    max_neighbor_score = max(neighbor_scores.values(), default=0.0)
    combined_scores: dict[int, float] = {}
    for game_id in games_by_id:
        if game_id in excluded_game_ids:
            continue
        purchase_score = (
            neighbor_scores.get(game_id, 0.0) / max_neighbor_score
            if max_neighbor_score
            else 0.0
        )
        content_score = content_scores.get(game_id, 0.0)
        score = PURCHASE_WEIGHT * purchase_score + CONTENT_WEIGHT * content_score
        if score > 0:
            combined_scores[game_id] = score

    if not combined_scores:
        return _recommend_by_popularity(user_data, games_by_id, popularity, owned_game_ids)

    return _response(user_data, games_by_id, combined_scores, "knn-hybrid")


def _content_similarity_scores(
    games_by_id: dict[int, dict[str, object]],
    target_interactions: dict[int, int],
) -> dict[int, float]:
    game_ids = list(games_by_id)
    games = [games_by_id[game_id] for game_id in game_ids]
    max_price = max(float(game.get("price") or 0.0) for game in games)
    max_edition = max(int(game["edition_number"]) for game in games)
    vectorizer = DictVectorizer(sparse=True)
    feature_matrix = vectorizer.fit_transform(
        [_features(game, max_price, max_edition) for game in games]
    )
    feature_weights = np.asarray([
        1.0 if name.startswith(("author=", "publisher="))
        else 0.7 if name.startswith("genre=")
        else 0.15 if name == "price"
        else 0.1
        for name in vectorizer.feature_names_
    ])
    feature_matrix = feature_matrix.multiply(feature_weights).tocsr()

    index_by_game = {game_id: index for index, game_id in enumerate(game_ids)}
    purchased_ids = [game_id for game_id in target_interactions if game_id in index_by_game]
    weights = np.asarray([log1p(target_interactions[game_id]) for game_id in purchased_ids])
    purchased_matrix = feature_matrix[[index_by_game[game_id] for game_id in purchased_ids]]
    profile_array = np.asarray(purchased_matrix.multiply(weights[:, None]).sum(axis=0) / weights.sum())
    profile = csr_matrix(profile_array)

    item_knn = NearestNeighbors(
        n_neighbors=len(game_ids),
        algorithm="brute",
        metric="cosine",
    ).fit(feature_matrix)
    distances, indices = item_knn.kneighbors(profile)
    return {
        game_ids[int(index)]: max(0.0, min(1.0, 1.0 - float(distance)))
        for distance, index in zip(distances[0], indices[0], strict=True)
    }


def _recommend_by_popularity(
    user_data: UserData,
    games_by_id: dict[int, dict[str, object]],
    popularity: dict[int, float],
    owned_game_ids: set[int],
) -> RecommendationResponse:
    scores = {
        game_id: score
        for game_id, score in popularity.items()
        if game_id in games_by_id and game_id not in owned_game_ids
    }
    return _response(user_data, games_by_id, scores, "cold-start")


def _response(
    user_data: UserData,
    games_by_id: dict[int, dict[str, object]],
    scores: dict[int, float],
    strategy: str,
) -> RecommendationResponse:
    recommendations = [
        GameRecommendation(
            game_id=game_id,
            game_name=str(games_by_id[game_id]["name"]),
            genre=str(games_by_id[game_id]["genre"]),
            score=round(max(0.0, min(1.0, score)), 4),
        )
        for game_id, score in sorted(scores.items(), key=lambda item: (-item[1], item[0]))
        if game_id in games_by_id
    ][:user_data.limit]
    return RecommendationResponse(
        user_id=user_data.user_id,
        strategy=strategy,  # type: ignore[arg-type]
        recommendations=recommendations,
    )
