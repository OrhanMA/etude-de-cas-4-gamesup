# Application Angular GamesUP

Interface web pour l'inscription/connexion, la recherche et la consultation du catalogue, la création de commandes, l'historique client et les écrans d'administration. Les composants sont standalone et les fonctionnalités sont chargées par routes. Angular Material fournit les contrôles principaux.

## Installation et lancement

```sh
npm ci
npm start
```

Avant cela, démarrer l'API Spring sur `http://localhost:8080`. Le proxy déclaré dans `proxy.conf.json` transmet les routes API Spring au backend. Le navigateur ne contacte pas FastAPI directement. Ouvrir ensuite `http://localhost:4200`.

Parcours disponibles : `/register`, `/login`, `/catalog`, `/catalog/game/:id`, `/orders`, `/recommendations` et `/admin` (jeux, référentiels, clients et commandes). Les gardes Angular contrôlent la navigation et Spring applique les autorisations définitives. Le jeton JWT est gardé uniquement en mémoire et disparaît au rechargement ou à la déconnexion. La page de recommandations appelle uniquement la route Spring ; Spring reste le seul composant qui appelle FastAPI.

## Tests et compilation

```sh
npm test -- --watch=false
npm run build
```

Vitest exécute les tests des guards, services HTTP et parcours du catalogue. Aucun seuil de couverture Angular n'est configuré. La vérification fonctionnelle complète nécessite MySQL et Spring ; la recommandation demande aussi FastAPI disponible.

## Configuration de production

Le serveur qui héberge Angular doit transmettre `/api`, `/games`, `/authors` et `/publishers` à Spring (ou adapter le proxy selon l'environnement). Les variables et identifiants de backend ne doivent pas être intégrés au bundle Angular. Le contrôle d'accès se fait côté Spring ; les guards frontend ne constituent pas une frontière de sécurité.
