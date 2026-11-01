from typing import Literal

from pydantic import BaseModel, Field


class UserInteraction(BaseModel):
    """Achats payés d'un utilisateur, regroupés par jeu."""

    user_id: int = Field(gt=0)
    game_id: int = Field(gt=0)
    interaction_count: int = Field(default=1, ge=1)


class CatalogGame(BaseModel):
    """Champs du catalogue utilisés pour construire les vecteurs KNN."""

    game_id: int = Field(gt=0)
    name: str = Field(min_length=1)
    genre: str = Field(min_length=1)
    edition_number: int = Field(ge=1)
    price: float | None = Field(default=None, ge=0)
    author: str | None = None
    publisher: str | None = None


class UserData(BaseModel):
    user_id: int = Field(gt=0)
    interactions: list[UserInteraction] = Field(default_factory=list)
    catalog: list[CatalogGame] = Field(default_factory=list)
    limit: int = Field(default=5, ge=1, le=20)


class GameRecommendation(BaseModel):
    game_id: int
    game_name: str
    genre: str
    score: float = Field(ge=0, le=1)


class RecommendationResponse(BaseModel):
    user_id: int
    strategy: Literal["knn-hybrid", "cold-start", "no-candidates"]
    recommendations: list[GameRecommendation]
