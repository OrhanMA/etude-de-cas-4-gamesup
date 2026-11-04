# Conception, sécurité et qualité du code

## Architecture REST

Les contrôleurs Spring traduisent les requêtes HTTP en appels aux interfaces de service. Les services appliquent les règles et transforment les entités en DTO. Les repositories Spring Data JPA isolent la persistance. Les entités restent dans le modèle interne ; les objets de requête/réponse définissent les contrats externes.

Les opérations couvrent les jeux (lecture, recherche, CRUD administrateur), commandes (création client, lecture de ses commandes, gestion administrateur), utilisateurs (consultation/gestion administrateur), auteurs et éditeurs, ainsi que l'authentification et les recommandations. La liste exacte et les chemins sont visibles dans les contrôleurs `apps/spring-api/src/main/java/com/gamesUP/gamesUP/controller`.

## Modèles et Hibernate

Hibernate, via Spring Data JPA, mappe les entités relationnelles. `OrderGame` est une entité de jointure car une ligne de commande contient ses propres données (quantité, prix unitaire). Les relations auteur/éditeur d'un jeu et utilisateur/commande sont persistées par JPA. La configuration locale utilise `ddl-auto=update` pour faciliter le démarrage de l'étude ; pour un déploiement durable, des migrations versionnées (Flyway ou Liquibase) sont préférables.

## DTO, validation et erreurs

Les entrées de l'API sont des DTO annotés avec Jakarta Validation. Les services vérifient l'existence et la cohérence des références métier avant d'écrire. Les résultats utilisent aussi des DTO afin de ne pas exposer directement les entités ni leurs relations paresseuses. L'intégration FastAPI possède des DTO distincts avec noms JSON explicites.

Les erreurs connues sont transformées en statuts HTTP adaptés : validation, ressource absente, accès interdit et dépendance de recommandation indisponible. Spring renvoie 503 si FastAPI ne répond pas et 502 si sa réponse ne respecte pas le contrat attendu.

## Sécurité et rôles

Spring Security est configuré comme serveur de ressources JWT stateless. L'inscription et la connexion sont publiques ; les autres routes exigent un jeton valide. Les rôles sont portés dans la revendication `roles`, convertis en autorités, puis contrôlés par les règles `@PreAuthorize` sur les opérations administratives. Les clients accèdent à leurs commandes via les routes `mine`; les listes et mutations administratives sont réservées à `ROLE_ADMIN`.

Le secret JWT est une variable locale `JWT_SECRET`. Les identifiants MySQL sont également injectés par environnement ou fichier `.env` ignoré. L'interface Angular garde le jeton en mémoire, mais le contrôle d'accès réel demeure côté Spring. En production, il faut aussi employer HTTPS, un secret aléatoire robuste, une gestion de rotation et un stockage de secrets dédié.

## Évaluation des principes SOLID

| Principe | Application dans le projet | Observation |
| --- | --- | --- |
| Responsabilité unique (S) | Contrôleurs HTTP, services métier, repositories de persistance, `RecommendationClient` pour l'appel HTTP | Les couches sont séparées. Le service de recommandation orchestre encore extraction, mapping et appel ; si ces transformations grossissent, les extraire dans des mappeurs dédiés. |
| Ouvert/fermé (O) | Les contrats `AuthService`, `GameService`, `RecommendationService` permettent de faire évoluer les implémentations sans changer les contrôleurs | Les règles de sécurité et le poids KNN restent configurés plutôt qu'éparpillés dans l'interface. |
| Substitution de Liskov (L) | Les implémentations de services respectent les interfaces utilisées par Spring et les tests | Pas de hiérarchie métier complexe ; le principe est couvert principalement par l'usage d'interfaces. |
| Ségrégation des interfaces (I) | Interfaces séparées par domaine (jeux, commandes, utilisateurs, recommandations) | Les contrôleurs ne dépendent pas d'une interface « tout faire ». |
| Inversion des dépendances (D) | Les contrôleurs injectent les interfaces de services ; les services dépendent des abstractions repository et du client de recommandation | L'injection par constructeur rend les dépendances explicites et facilite les tests avec des doublures. |

Le code nouveau de recommandation a été gardé en couches. Une validation du corps renvoyé par Python a été ajoutée afin qu'une réponse nulle, sans stratégie ou sans liste soit traitée comme une erreur de passerelle plutôt que de provoquer une erreur inattendue.

## Bonnes pratiques retenues et points à surveiller

- Contrats REST et DTO séparés des entités de persistance.
- Validation des entrées et mots de passe hachés avec l'encodeur Spring.
- Limites de temps de connexion et de lecture sur l'appel à FastAPI.
- Tests ciblés sur la sécurité, les services, les contrôleurs, JPA et le contrat HTTP sortant.
- Mots de passe injectés par variable d'environnement.
- Vérifier les réponses externes et ne pas transmettre les noms, emails ou mots de passe à Python.
