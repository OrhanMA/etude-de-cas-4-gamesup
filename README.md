# GamesUP

Plateforme de vente de jeux de société réalisée pour l'étude de cas GamesUP. Le dépôt contient trois applications :

**Dépôt GitHub :** [github.com/OrhanMA/etude-de-cas-4-gamesup](https://github.com/OrhanMA/etude-de-cas-4-gamesup)

- `apps/spring-api` : API REST Java 21 / Spring Boot, Hibernate/JPA, MySQL et authentification JWT avec rôles client et administrateur.
- `apps/python-api` : API FastAPI qui calcule les recommandations avec un KNN hybride basé sur les achats et les caractéristiques des jeux.
- `apps/angular-frontend` : application Angular pour le catalogue, les commandes et l'administration.

## Démarrage rapide

Consulter [la documentation d'installation et d'exécution](docs/outils.md) pour préparer MySQL, configurer les variables locales, démarrer les trois applications et lancer les vérifications. Les variables sensibles sont locales et ne doivent pas être ajoutées au dépôt. Un exemple sans valeur secrète est fourni dans `apps/spring-api/.env.example`.

## Documentation

- [Architecture et diagrammes](docs/architecture.md)
- [Conception, sécurité et principes SOLID](docs/conception.md)
- [Tests et couverture](docs/tests.md)
- [Installation, lancement et commandes](docs/outils.md)
- [Algorithme de recommandation](docs/recommandation.md)
- [Bilan des choix et limites](docs/bilan.md)

Les tests de l'API Python ne font pas partie du périmètre de tests défini par l'énoncé. Les tests unitaires et d'intégration Spring constituent le critère de l'étude de cas ; Angular conserve sa propre suite de vérification fonctionnelle.
