# Tests et couverture

À compléter après implémentation et exécution des tests.

## Stratégie

Prévoir des tests unitaires et d’intégration Java, notamment pour les services, contrôleurs, rôles et échanges entre API. La feuille de travail ne demande pas de tests pour l’API Python.

## Exécution et résultats

Les prérequis et les commandes figurent dans [Outils et commandes de test](outils.md).

Le 29 octobre 2026 sur macOS, `./mvnw clean verify` a réussi dans une copie temporaire du module, après conversion de `application.properties` en UTF-8 et avec Java 21 : huit tests sont passés. Dans l'arbre de travail actuel, l'encodage de ce fichier bloque encore Maven. Deux tests chargent le contexte avec la base MySQL locale `gamesup_test`, cinq exercent le contrôleur avec MockMvc et un vérifie les relations JPA avec une base H2 en mémoire.

Les tests MockMvc utilisent un repository simulé. Ils ne prouvent pas que les routes fonctionnent avec un schéma MySQL réel. La sécurité Spring par défaut est vérifiée pour les requêtes anonymes et le jeton CSRF ; les règles et rôles métier restent à implémenter et à tester.

## Couverture

Objectif de l’évaluation : au moins 70 %. L’exécution propre dans la copie temporaire a mesuré **21 lignes couvertes sur 29, soit 72,4 %**, et le contrôle JaCoCo a réussi. Après correction de l'encodage, le rapport local peut être généré dans `apps/spring-api/target/site/jacoco/index.html` avec `./mvnw clean test`. Ce taux concerne le code présent à ce stade et ne valide pas les parcours métier complets.
