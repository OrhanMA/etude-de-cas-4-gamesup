# Bilan de la démarche

## Travail réalisé

Le projet reprend l'API existante et l'organise en domaines REST : catalogue et recherche de jeux, utilisateurs et rôles, commandes, auteurs, éditeurs, sécurité JWT, puis appel à une API de recommandation FastAPI. Le frontend Angular permet d'utiliser les parcours client et administrateur. Hibernate gère les relations vers MySQL.

L'étude de cas demande une recommandation KNN plus pertinente que le tirage aléatoire et une intégration Spring → Python. Le service combine les historiques d'achats des voisins et les attributs auteur/éditeur des jeux. Le jeu de démonstration synthétique fournit des exemples reproductibles sans confondre ces données avec des ventes réelles. La démarche, les variables à collecter et les limites sont documentées dans [Recommandation](recommandation.md).

## Pratiques positives

- Conservation de la base existante et amélioration incrémentale de l'architecture, conformément au scénario.
- Contrôleurs, services, repositories, DTO et entités distincts pour réduire le couplage.
- JWT stateless et contrôle des rôles côté API ; séparation des données de commande par utilisateur.
- Tests Spring unitaires et d'intégration, contrôle JaCoCo de 70 %, tests Vitest Angular.
- Secrets injectés depuis l'environnement et données de démo limitées à un profil explicite.
- Diagrammes d’architecture, d’entités et de séquence actualisés à partir du code, avec une source modifiable.

## Difficultés et pratiques à améliorer

- Mettre à jour régulièrement les diagrammes de conception pour matcher avec la réalité du code.
- Utiliser les bonnes pratiques de découplage directement au lieu d'implémenter directement dans le contrôleur et ensuite découpler.
- Les tests d'intégration Spring dépendent localement d'un service MySQL de test, tandis que H2 pourrait couvrir davantage de scénarios pour simplifier une exécution isolée.
- `ddl-auto=update` convient au prototypage mais rend les changements de schéma moins reproductibles qu'un outil de migration.
- Les données synthétiques démontrent le mécanisme mais ne représentent pas une population ni des habitudes d'achat réelles.

## Améliorations envisageables

1. Maintenir les sources éditables des diagrammes et les synchroniser avec les routes et composants.
2. Ajouter Flyway/Liquibase et exécuter les tests d'intégration sur une base éphémère reproductible.
3. Constituer un jeu de données anonymisé réel pour l'api de recommandation.
4. Calculer les voisinages de façon asynchrone ou mettre en cache l'index si le volume l'exige.
