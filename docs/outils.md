# Installation, initialisation et commandes

## Prérequis

- Git, Java 21, MySQL et son client en ligne de commande pour Spring. Le wrapper télécharge Maven 3.9.9 au premier lancement : Maven n'a pas besoin d'être installé séparément.
- Python 3.11 ou ultérieur avec `venv` et `pip` pour FastAPI. Python 3.11 a été utilisé pour la vérification locale.
- Node.js `^20.19.0`, `^22.12.0` ou `>=24.0.0` et npm ; le projet indique npm 11.19.0. Node 26.7.0 et npm 11.19.0 ont été utilisés pour la vérification locale.
- Une connexion Internet au premier téléchargement des dépendances Maven, Python et npm.
- Trois terminaux pour lancer simultanément les applications.

Les versions des dépendances applicatives sont définies par Maven, `requirements.txt` et `package-lock.json`. Les fichiers de secrets ne doivent jamais être commités.

## 1. Base de données et API Spring

Démarrer le serveur MySQL local. Avec un compte MySQL autorisé à créer des bases et des utilisateurs, ouvrir le client :

```sh
mysql --host=127.0.0.1 --user=root --password
```

Dans le client MySQL, créer les deux bases et un compte local dédié. Remplacer le mot de passe indicatif par une valeur choisie localement ; ne pas utiliser cette valeur d'exemple :

```sql
CREATE DATABASE IF NOT EXISTS gamesup CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS gamesup_test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'gamesup_local'@'localhost' IDENTIFIED BY 'remplacer-par-un-mot-de-passe-local';
GRANT ALL PRIVILEGES ON gamesup.* TO 'gamesup_local'@'localhost';
GRANT ALL PRIVILEGES ON gamesup_test.* TO 'gamesup_local'@'localhost';
EXIT;
```

Si le compte existe déjà, utiliser un autre nom ou adapter ses droits au lieu de relancer `CREATE USER`. Les modalités de connexion de l'administrateur MySQL dépendent de son installation. Les deux bases doivent être locales et dédiées au projet.

Depuis la racine du dépôt, copier le modèle sans écraser un éventuel fichier existant :

```sh
test -e apps/spring-api/.env || cp apps/spring-api/.env.example apps/spring-api/.env
```

Configurer ensuite `apps/spring-api/.env` avec le compte créé :

```dotenv
SERVER_PORT=8080
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_DATABASE=gamesup
MYSQL_USERNAME=gamesup_local
MYSQL_PASSWORD=<mot-de-passe-local>
JWT_SECRET=<secret-aleatoire-base64-de-32-octets-ou-plus>
RECOMMENDATION_API_BASE_URL=http://localhost:8000
```

Le fichier `.env` est ignoré par Git. Produire un secret local avec `openssl rand -base64 32` et recopier le résultat dans `JWT_SECRET`. Utiliser le mot de passe choisi pour le compte MySQL. Le fichier est chargé relativement au répertoire courant : lancer Spring depuis `apps/spring-api`.

Le lancement normal est :

```sh
cd apps/spring-api
./mvnw spring-boot:run
```

Hibernate utilise actuellement `ddl-auto=update` pour créer ou mettre à jour les tables dans la base applicative. Le profil de test utilise la base dédiée `gamesup_test` configurée dans `src/test/resources/application-test.properties` ; ne pas pointer les tests vers des données utiles. Il utilise par défaut `root` avec un mot de passe vide. Pour utiliser le compte créé ci-dessus, définir dans le terminal des tests :

```sh
export GAMESUP_TEST_DB_USERNAME=gamesup_local
read -rs GAMESUP_TEST_DB_PASSWORD
export GAMESUP_TEST_DB_PASSWORD
```

La commande `read` attend le mot de passe sans l'afficher. Ces variables de test sont distinctes des variables `MYSQL_*` de `.env`. Les tests JPA emploient aussi H2 en mémoire ; les tests de contexte nécessitent MySQL disponible.

### Premier administrateur local

Après le premier démarrage de Spring et d'Angular, inscrire normalement le compte souhaité sur `/register`. L'inscription crée toujours un utilisateur standard. Sur la base locale dédiée, un administrateur MySQL peut ensuite attribuer le rôle à ce compte existant :

```sql
USE gamesup;
SELECT id, email, role FROM users WHERE email = 'adresse-du-compte-local';
UPDATE users SET role = 'ROLE_ADMIN' WHERE email = 'adresse-du-compte-local';
SELECT id, email, role FROM users WHERE email = 'adresse-du-compte-local';
```

Remplacer l'adresse par celle du compte inscrit et vérifier que la première requête trouve bien le compte attendu. Se déconnecter puis se reconnecter pour obtenir un nouveau jeton contenant le rôle administrateur. Cette opération est réservée à l'initialisation locale ; le profil de démonstration de recommandations crée des comptes clients, pas un administrateur.

## 2. API de recommandation FastAPI

Dans un autre terminal :

```sh
cd apps/python-api
python3 -m venv .venv
.venv/bin/python -m pip install -r requirements.txt
.venv/bin/uvicorn app.main:app --host 127.0.0.1 --port 8000
```

La documentation interactive est sur `http://127.0.0.1:8000/docs`. Spring utilise `POST /recommendations/`; ne pas exposer cet endpoint directement au navigateur. Les paramètres KNN, le contrat JSON, les données synthétiques et les limites sont décrits dans [Recommandations](recommandation.md).

L'énoncé demande de ne pas tester l'API Python. Sa vérification manuelle de contrat se fait via `/docs` ou une requête d'exemple documentée ; la qualité évaluée par la suite automatisée porte sur Spring.

## 3. Application Angular

Dans un troisième terminal :

```sh
cd apps/angular-frontend
npm ci
npm start
```

Ouvrir `http://localhost:4200`. Le proxy de développement (`proxy.conf.json`) transmet `/api`, `/games`, `/authors` et `/publishers` vers `http://localhost:8080`. Pour l'essai complet de recommandation, Spring et FastAPI doivent tous deux être disponibles. L'interface admin demande un compte dont le rôle a été accordé côté base/API ; l'inscription publique crée un compte client.

## Tests et rapports

Depuis `apps/spring-api` :

```sh
./mvnw test                 # exécute les tests Spring
./mvnw clean verify         # suite complète et seuil JaCoCo
open target/site/jacoco/index.html  # macOS
```

Sous Windows PowerShell, lancer `.\mvnw.cmd clean verify`. Le rapport doit être régénéré après les modifications du code. JaCoCo exige actuellement 70 % de couverture des lignes au niveau du module.

Depuis `apps/angular-frontend` :

```sh
npm test -- --watch=false
npm run build
```

Vitest exécute les tests Angular ; aucun seuil de couverture n'est actuellement configuré. Aucun test automatisé Python n'est inclus car l'énoncé les exclut.

Pour une vérification manuelle de bout en bout, démarrer les trois applications, inscrire puis connecter un utilisateur, consulter le catalogue et appeler la page de recommandations. Un catalogue et des achats finalisés sont nécessaires pour obtenir des recommandations non vides ; un jeu de données local facultatif est décrit dans [Recommandations](recommandation.md). Les suites automatisées ne remplacent pas cet essai avec les trois serveurs et une base réelle.

## Vérification des secrets avant partage

Les secrets doivent être fournis par `.env` ignoré ou par l'environnement du processus. `.env.example` contient uniquement des valeurs fictives/instructions. Avant publication, vérifier les fichiers suivis et l'index Git, puis inspecter le diff sans afficher les valeurs sensibles :

```sh
git status --short
git ls-files '.env' '**/.env' '*secret*' '*credential*'
git diff --check
```

Ne jamais coller une valeur de secret dans un ticket, une documentation, un test ou un commit. Si un secret a déjà été commité, le révoquer et le remplacer ; supprimer sa valeur du dernier état ne suffit pas à l'effacer de l'historique.
