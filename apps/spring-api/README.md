# API Spring

API REST Java 21 / Spring Boot avec Spring Security JWT, Hibernate/JPA et MySQL. Les contrôleurs se trouvent sous `src/main/java/com/gamesUP/gamesUP/controller` ; les contrats HTTP sont des DTO.

## Préparer et lancer

1. Installer Java 21 et MySQL.
2. Créer une base `gamesup` avec un compte local autorisé.
3. Copier `.env.example` en `.env`, puis définir les identifiants MySQL et un `JWT_SECRET` aléatoire (32 octets minimum). Ne jamais committer `.env`.
4. Depuis ce dossier, démarrer l'API :

```sh
./mvnw spring-boot:run
```

Par défaut l'API écoute sur `http://localhost:8080`. Les commandes de vérification et la configuration de la base de test sont dans [`../../docs/outils.md`](../../docs/outils.md).

## Routes principales

| Domaine | Routes | Accès |
| --- | --- | --- |
| Authentification | `POST /api/auth/register`, `POST /api/auth/login` | Public |
| Jeux | `GET /games`, `GET /games/search`, `GET /games/{id}` | Client ou administrateur |
| Écriture des jeux | `POST /games`, `PUT /games/{id}`, `DELETE /games/{id}` | Administrateur |
| Commandes | `POST /api/orders`, `GET /api/orders/mine`, `GET /api/orders/mine/{id}` | Client connecté, données propres au compte |
| Gestion des commandes | `GET /api/orders`, `GET /api/orders/{id}`, `PUT /api/orders/{id}`, `DELETE /api/orders/{id}` | Administrateur |
| Utilisateurs | `/api/users` et `/api/users/{id}` | Administrateur |
| Référentiels | `GET /authors`, `GET /publishers` | Client ou administrateur |
| Écriture des référentiels | `POST`, `PUT`, `DELETE` sur `/authors` et `/publishers` | Administrateur |
| Recommandations | `GET /api/recommendations` | Client ou administrateur connecté |

Les écritures et lectures protégées nécessitent `Authorization: Bearer <jeton>`. FastAPI se configure via `RECOMMENDATION_API_BASE_URL` ; le délai maximum de réponse est limité dans la configuration du client Spring.

## Données synthétiques

Le profil opt-in `recommendation-demo` crée un corpus de démonstration sur la base courante. Il faut le lancer uniquement contre une base locale dédiée. Définir d'abord `RECOMMENDATION_DEMO_PASSWORD` dans l'environnement, puis utiliser la commande indiquée dans [`../../docs/recommandation.md`](../../docs/recommandation.md). Le compte de démonstration est `knn.target@gamesup.local`; son mot de passe n'est pas stocké dans le dépôt.

## Vérification

```sh
./mvnw clean verify
```

Cette commande compile, exécute les tests Spring, génère JaCoCo et applique son seuil de couverture de 70 % des lignes. Le rapport HTML se trouve dans `target/site/jacoco/index.html`.
