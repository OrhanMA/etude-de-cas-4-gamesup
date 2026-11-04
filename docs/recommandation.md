# Recommandations GamesUP

## Données retenues

Spring construit un jeu de données à partir du catalogue et des commandes payées ou livrées. Une commande en brouillon n'est pas considérée comme un achat. Les interactions transmises à FastAPI sont agrégées par couple utilisateur/jeu :

- `user_id` : identifiant technique du client ;
- `game_id` : identifiant du jeu ;
- `interaction_count` : nombre d'unités achetées dans les commandes finalisées.

Chaque jeu du catalogue transmet son identifiant, son nom, son genre, son numéro d'édition, son prix, son auteur et son éditeur. Les auteurs et éditeurs alimentent le calcul de similarité de contenu ; le genre, le prix et l'édition apportent des signaux complémentaires.

Les utilisateurs ne sont envoyés qu'avec leurs identifiants techniques. Aucun nom, courriel ou mot de passe ne part vers FastAPI.

Pour entraîner et évaluer un futur modèle plus fin, les événements à conserver sont : utilisateur pseudonymisé, jeu, date d'achat, quantité, annulation/retour, note explicite si elle existe, vues, recherches et souhaits. La date et les événements de consultation ne sont pas encore inclus dans le calcul actuel. Les commandes impayées sont exclues.

## Algorithme KNN hybride

FastAPI reçoit les interactions finalisées de tous les clients et le catalogue disponible. Il construit deux espaces de distance :

1. **Contenu des jeux** : KNN avec des caractéristiques auteur, éditeur, genre, prix et édition. Les auteurs et éditeurs portent le poids principal dans cette distance.
2. **Tendances d'achat** : chaque client est représenté par un vecteur creux jeux × nombre d'interactions. `NearestNeighbors` avec distance cosinus repère les clients dont les historiques se recouvrent.

Le score final est `0,8 × score tendance d'achat + 0,2 × score contenu`. Le comportement d'achat pèse donc quatre fois plus lourd que les ressemblances de catalogue. Un jeu déjà acheté par le client est retiré des résultats. Les scores sont normalisés entre 0 et 1.

Si deux clients ont acheté les mêmes dix jeux, leurs vecteurs d'achat ont une similarité cosinus de 1. Si l'un a acheté un jeu supplémentaire, la similarité reste environ `10 / √(10 × 11) = 0,953` : ils restent proches, et le onzième jeu constitue un candidat pertinent pour l'autre client.

Le KNN n'a pas besoin d'un jeu d'entraînement étiqueté : il est ajusté aux interactions et au catalogue présents à chaque appel. C'est un modèle de voisinage non supervisé, pas un modèle entraîné à prédire une note. Les interactions d'achat servent de retours implicites, pas de notes de satisfaction. Une note, un retour ou un clic pourraient ensuite enrichir les poids et permettre une évaluation hors ligne. Pour un nouveau client sans historique, l'API utilise les jeux les plus achetés par les autres clients ; si aucune interaction n'existe, la liste est vide.

## Contrat Spring → FastAPI

Spring expose `GET /api/recommendations`, réservé aux utilisateurs connectés. Il extrait l'identifiant du compte depuis le JWT, charge les achats finalisés et le catalogue, puis appelle `POST /recommendations/` sur FastAPI. L'URL se configure avec `RECOMMENDATION_API_BASE_URL` (par défaut `http://localhost:8000`). L'appel a un délai de connexion de 2 secondes et un délai de réponse de 5 secondes ; une indisponibilité FastAPI donne un HTTP 503 côté Spring.

Exemple de requête interne vers FastAPI :

```json
{
  "user_id": 12,
  "interactions": [
    {"user_id": 12, "game_id": 4, "interaction_count": 1},
    {"user_id": 29, "game_id": 4, "interaction_count": 2}
  ],
  "catalog": [
    {
      "game_id": 4,
      "name": "Jeu de stratégie",
      "genre": "Stratégie",
      "edition_number": 1,
      "price": 20.0,
      "author": "Auteur A",
      "publisher": "Éditeur A"
    }
  ],
  "limit": 5
}
```

FastAPI renvoie `user_id`, la stratégie (`knn-hybrid`, `cold-start` ou `no-candidates`) et des recommandations avec `game_id`, `game_name`, `genre` et `score`. Spring vérifie le compte rendu, ignore les identifiants de jeu absents de son catalogue et retourne les détails depuis sa propre base.

## Jeu de démonstration local

Un amorçage explicite, protégé par le profil Spring `recommendation-demo`, crée 80 comptes synthétiques, 96 jeux, 12 auteurs, 8 éditeurs et 872 lignes d'achat payées en huit groupes. Dans chaque groupe, le client cible possède exactement dix jeux ; les voisins partagent ces dix jeux et achètent chacun un titre additionnel. Le jeu de tendance est ainsi recommandable pour le compte cible. Toutes les données sont préfixées `KNN Demo` ou utilisent `@gamesup.local`. L'amorçage s'arrête si le compte cible existe déjà, afin de ne pas créer de doublons.

Depuis `apps/spring-api`, définir un mot de passe local choisi par l'opérateur et lancer l'amorçage sur une base de démonstration dédiée. Le mot de passe n'est ni généré par le dépôt ni enregistré dans ses fichiers :

Remplacer le texte indicatif entre chevrons avant d'exécuter la commande.

```sh
export RECOMMENDATION_DEMO_PASSWORD='<mot-de-passe-local-choisi>'
./mvnw spring-boot:run -Dspring-boot.run.profiles=recommendation-demo \
  -Dspring-boot.run.arguments=--server.port=0
```

Le profil est désactivé en usage normal ; le code de création ne s'exécute donc pas au démarrage standard. Il ne faut activer ce profil que sur une base locale de démonstration.

Compte cible : `knn.target@gamesup.local`. Tous les comptes synthétiques du lot utilisent le mot de passe défini localement par `RECOMMENDATION_DEMO_PASSWORD`. Ne pas committer sa valeur. Après l'amorçage, le compte cible peut se connecter normalement à l'application en utilisant ce secret local.

## Limites

- Les données de démonstration sont artificielles et ne permettent pas de conclure à la qualité commerciale des résultats.
- Le modèle s'ajuste en mémoire à chaque requête ; pour un catalogue ou un volume d'interactions très important, il faudra entraîner/rafraîchir un index hors requête et ne transmettre que le profil du client.
- Le score mesure une proximité, pas une probabilité de satisfaction.
- Il n'y a pas encore d'évaluation offline (précision@k, rappel@k, diversité ou couverture) ni de protection contre les interactions frauduleuses.
