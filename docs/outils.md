# Outils et commandes de test

L'énoncé demande des tests unitaires et d'intégration pour l'API Spring ainsi qu'un rapport de couverture. Il précise de ne pas tester l'API Python. Le frontend possède également ses propres outils, présentés à la fin de cette page.

## Outils présents

| Outil | Utilisation | Configuration actuelle |
| --- | --- | --- |
| Maven et Surefire | Compiler le projet et exécuter les tests Java. | Présents via le projet Maven et son parent Spring Boot. |
| JUnit 5 | Déclarer et exécuter les tests. | Inclus dans `spring-boot-starter-test`. |
| Mockito | Simuler une dépendance dans un test, par exemple un repository. | Inclus dans `spring-boot-starter-test`. |
| AssertJ | Vérifier les résultats avec des assertions lisibles. | Inclus dans `spring-boot-starter-test`. |
| Spring Boot Test | Tester un contexte Spring ou une partie de l'application. | Inclus dans `spring-boot-starter-test`. |
| MockMvc | Tester les contrôleurs avec des requêtes simulées, sans serveur HTTP. | Inclus dans Spring Test. |
| TestRestTemplate | Envoyer des requêtes HTTP, notamment vers un serveur Spring lancé pour un test. | Inclus dans Spring Boot Test. |
| Spring Security Test | Préparer des utilisateurs ou des requêtes pour tester la sécurité. | Dépendance de test présente dans le `pom.xml`. Le starter Spring Security est aussi activé ; les règles métier et les rôles restent à implémenter et à tester. |
| H2 | Vérifier les relations JPA sur une base isolée. | Dépendance de test uniquement ; `RelationsJpaTest` crée un schéma en mémoire. |
| JaCoCo 0.8.12 | Produire des rapports de couverture et vérifier le seuil. | Plugin configuré dans le `pom.xml` : minimum de 70 % des lignes pour l'ensemble du module lors de `verify`. |
| Vitest | Exécuter les tests du frontend Angular. | Dépendance de développement présente dans le frontend. |

JUnit, Mockito, AssertJ, MockMvc et Spring Security Test n'ont pas chacun une commande à lancer : Maven exécute les classes de test qui les utilisent.

## Prérequis pour l'API Spring

Les commandes Maven ci-dessous sont à exécuter depuis `apps/spring-api`.

### Utiliser Java 21

Le projet cible Java 21. Le Maven Wrapper utilise la version de Maven définie par le projet. Vérifier également la version de Java qu'il utilise :

```bash
cd apps/spring-api
java -version
./mvnw -version
```

### Vérifier l'encodage de la configuration

Le fichier `src/main/resources/application.properties` contient actuellement des commentaires encodés en Windows-1252. Le traitement Maven attend de l'UTF-8 et échoue tant que le fichier n'est pas converti. Les résultats ci-dessous ont été obtenus après conversion en UTF-8 dans une copie temporaire, sans modifier les valeurs de configuration. Pour vérifier le traitement des ressources après correction :

```bash
./mvnw process-resources
```

Cette commande ne lance pas les tests et ne valide pas la connexion à MySQL.

### Isoler la base de données

Le profil `test`, configuré dans `src/test/resources/application-test.properties`, pointe vers la base locale MySQL `127.0.0.1:3306/gamesup_test` et désactive les modifications automatiques de schéma Hibernate avec `spring.jpa.hibernate.ddl-auto=none`. Les tests de contexte utilisent cette base. Le test JPA remplace la connexion par H2 en mémoire et y crée un schéma éphémère ; les tests MockMvc utilisent un repository simulé. Le contrôleur des jeux utilise `GameRepository`.

Le profil est activé par défaut pour les tests lancés avec Maven Surefire. Le test de contexte existant déclare également `@ActiveProfiles("test")` pour son exécution dans un IDE. Ajouter cette annotation aux futurs tests Spring lancés dans l'IDE. Vérifier que toute configuration de connexion supplémentaire pointe bien vers une base dédiée aux tests.

La base `gamesup_test` a été créée sur le MySQL local utilisé pour cette vérification. Sur une autre installation, si elle n'existe pas encore, la créer une fois avec un compte MySQL autorisé :

```bash
mysql --host=127.0.0.1 --user=root --password --execute="CREATE DATABASE gamesup_test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

Par défaut, le profil utilise le compte local `root` avec un mot de passe vide. Si l'installation nécessite d'autres identifiants, définir les variables d'environnement `GAMESUP_TEST_DB_USERNAME` et `GAMESUP_TEST_DB_PASSWORD`. Les futures tables devront être fournies par les migrations ou les données de test : ce profil ne les crée pas automatiquement.

## Lancer les tests Java

Exécuter les tests détectés par Maven :

```bash
./mvnw test
```

Repartir de résultats générés propres et exécuter ces tests :

```bash
./mvnw clean test
```

Avec la configuration actuelle, `test` lance les classes reconnues par Surefire, qu'elles soient unitaires ou d'intégration, par exemple celles dont le nom se termine par `Test` ou `Tests`. Le dossier dans lequel elles se trouvent ne suffit pas à distinguer leur type.

Compiler, tester, produire le paquet et vérifier le seuil de couverture :

```bash
./mvnw clean verify
```

La phase `verify` inclut les phases précédentes. Elle déclenche le contrôle JaCoCo à 70 %. Le plugin Failsafe n'est pas activé dans ce projet : les classes nommées `*IT` ne sont donc pas automatiquement exécutées comme une suite d'intégration séparée.

### Sélectionner des tests

Lancer les tests de contexte :

```bash
./mvnw test '-Dtest=GamesUpApplicationTests'
```

Le chargement de ce contexte utilise la configuration de base décrite ci-dessus. Ce test vérifie le démarrage du contexte ; il ne couvre pas encore les fonctionnalités métier.

Les commandes suivantes sont des **exemples**. Le test `GameControllerTest` existe déjà ; les autres noms illustrent des tests à créer. Chaque commande échoue si aucune classe ou méthode ne correspond au nom choisi :

```bash
# Tests de services utilisant notamment JUnit, Mockito et AssertJ
./mvnw test '-Dtest=*ServiceTest'

# Tests de contrôleurs utilisant notamment MockMvc
./mvnw test '-Dtest=*ControllerTest'

# Tests de sécurité utilisant Spring Security Test
./mvnw test '-Dtest=*SecurityTest'

# Tests d'intégration dont le nom suit cette convention
./mvnw test '-Dtest=*IntegrationTest'

# Une classe, puis une méthode précise de cette classe
./mvnw test '-Dtest=GameServiceTest'
./mvnw test '-Dtest=GameServiceTest#nomDuTest'
```

Pour un parcours avec de vraies requêtes HTTP vers Spring, un test pourra démarrer l'application avec `@SpringBootTest(webEnvironment = RANDOM_PORT)` et utiliser `TestRestTemplate`. La commande reste celle de la classe de test correspondante.

### Vérifier les dépendances

Afficher l'arbre des dépendances du projet, y compris celles apportées par le starter de test :

```bash
./mvnw dependency:tree
```

Limiter l'affichage à Mockito, par exemple :

```bash
./mvnw dependency:tree '-Dincludes=org.mockito:mockito-core'
```

## Couverture JaCoCo

Le rapport est généré à la phase `test`, après l'exécution réussie des tests. Pour produire un rapport à partir d'une exécution propre :

```bash
./mvnw clean test
```

Ouvrir ensuite `target/site/jacoco/index.html`. Sur macOS :

```bash
open target/site/jacoco/index.html
```

Sous Windows PowerShell :

```powershell
Start-Process .\target\site\jacoco\index.html
```

Le dossier contient aussi des rapports XML et CSV. Pour régénérer les rapports à partir des données déjà présentes dans `target/jacoco.exec` :

```bash
./mvnw jacoco:report
```

Pour exécuter les tests et appliquer le seuil de 70 % sur les lignes :

```bash
./mvnw clean verify
```

Pour relancer uniquement le contrôle sur les données existantes :

```bash
./mvnw 'jacoco:check@jacoco-check'
```

Cette dernière commande ne relance pas les tests. Les données peuvent donc être anciennes ou ne concerner qu'une sélection de tests. Pour le rapport à remettre, préférer une exécution complète avec `clean`, sans ignorer les tests. Si un test échoue avant la génération du rapport, corriger d'abord son échec.

## Tests du frontend Angular

Ces tests sont distincts du périmètre de tests Spring demandé dans l'énoncé. Depuis la racine du dépôt :

```bash
cd apps/angular-frontend
npm test -- --watch=false
```

Lancer seulement le fichier de test actuel :

```bash
npm test -- --watch=false --include=src/app/app.spec.ts
```

## État de la vérification

Le 29 octobre 2026 sur macOS, une première exécution de `./mvnw clean test` a réussi avec Java 21 et MySQL 9.5 local : deux tests sont passés, dont un qui vérifie le nom de la base connectée. Le Wrapper a démarré après correction du choix de l'outil de calcul SHA-256, sans désactiver la vérification.

Avant l'ajout du contrôleur, deux vérifications temporaires avec MockMvc ont aussi confirmé que la sécurité Spring par défaut refuse une requête anonyme et laisse passer un utilisateur simulé jusqu'à une route inexistante. Elles ne valident pas une connexion réelle ni les rôles métier.

Après l'ajout du contrôleur et des relations, `./mvnw clean verify` a réussi dans une copie temporaire du module dont la configuration avait été convertie en UTF-8 : huit tests sont passés et le rapport JaCoCo compte 21 lignes couvertes sur 29, soit **72,4 %**. Les cinq tests du contrôleur utilisent MockMvc et un repository simulé ; le test des relations utilise H2 en mémoire. La connexion MySQL seule ne valide pas le schéma ni les parcours métier, et les règles de sécurité propres aux rôles restent à tester. Pour Angular, deux tests ont réussi lors de la vérification précédente du même jour.
