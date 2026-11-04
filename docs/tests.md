# Tests et couverture

## Périmètre

L'énoncé exige les tests unitaires et d'intégration de l'API Spring et précise que l'API Python ne doit pas être testée. La suite Angular est conservée pour vérifier les comportements du frontend développé dans le dépôt ; aucun test automatique Python n'est fourni.

## API Spring

Les tests couvrent les parcours principaux :

- `AuthFlowIntegrationTest` utilise Spring Boot, MockMvc, H2 et des jetons JWT pour vérifier inscription, connexion, authentification, accès client, commandes personnelles, opérations administrateur et contrôle de rôles. Le service de recommandation externe est simulé pour éviter de dépendre du processus FastAPI dans cette suite.
- `GameControllerTest` vérifie les réponses REST et la validation des entrées du contrôleur.
- `GameServiceImplTest` couvre la lecture, création et validation des références de jeu.
- `RecommendationServiceImplTest` contrôle la préparation des interactions/catalogue et la traduction des indisponibilités/réponses invalides de FastAPI.
- `RecommendationClientTest` vérifie le contrat JSON HTTP entre Spring et FastAPI.
- `RelationsJpaTest` vérifie les associations des entités sur H2 en mémoire.
- `GamesUpApplicationTests` charge le contexte et contrôle la connexion au profil MySQL de test.

Commande de validation complète depuis `apps/spring-api` :

```sh
./mvnw clean verify
```

Dernier résultat vérifié le 1er novembre 2026 : **40 tests passés, 0 échec, 0 erreur, 0 ignoré**. Le build Maven et le contrôle JaCoCo ont réussi. JaCoCo mesure **437 lignes couvertes sur 551, soit 79,3 %** ; seuil configuré : 70 %. Le rapport HTML est généré à `apps/spring-api/target/site/jacoco/index.html` et n'est pas commité. Ces valeurs devront être actualisées si le code évolue avant le rendu final.

Les tests avec `@SpringBootTest` utilisent H2 pour les parcours d'authentification ; le test de contexte utilise la base MySQL `gamesup_test`. Les tests de services isolés s'appuient sur Mockito. La suite ne démontre pas la performance ou la sécurité d'un environnement de production.

## Application Angular

Vitest vérifie les guards client/admin, l'intercepteur JWT, les services HTTP du catalogue/administration/commandes/recommandations, le parcours catalogue et l'affichage/erreur de la page de recommandations. Dernier résultat vérifié le 1er novembre 2026 : **11 fichiers de test et 22 tests passés**. `npm run build` a compilé la version de production. Aucun seuil de couverture Angular n'est configuré.

Depuis `apps/angular-frontend` :

```sh
npm test -- --watch=false
npm run build
```

## API FastAPI

Conformément à l'énoncé, aucun test Python automatisé n'est inclus. L'endpoint peut être inspecté manuellement avec la documentation OpenAPI `/docs`. La bonne intégration Spring → FastAPI est vérifiée côté Spring au niveau du contrat client HTTP ; le parcours réel nécessite les deux serveurs disponibles.
