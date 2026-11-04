# API de recommandation FastAPI

Cette API reçoit de Spring les achats finalisés et les caractéristiques du catalogue pour classer des jeux. Elle ne stocke pas les comptes ni les historiques. L'API Spring reste responsable de l'authentification, de l'autorisation et de l'accès aux données.

## Lancement local

```sh
python3 -m venv .venv
.venv/bin/python -m pip install -r requirements.txt
.venv/bin/uvicorn app.main:app --host 127.0.0.1 --port 8000
```

La page interactive FastAPI est disponible sur `http://127.0.0.1:8000/docs`. L'API Spring appelle `POST /recommendations/`. L'URL côté Spring se définit avec `RECOMMENDATION_API_BASE_URL` (par défaut `http://localhost:8000`).

## Contrat

La requête contient l'identifiant numérique du client courant, les interactions `user_id`, `game_id`, `interaction_count`, le catalogue (titre, genre, édition, prix, auteur, éditeur) et la limite de résultats. Aucun nom, courriel ou mot de passe n'est transmis. La réponse comprend l'identifiant cible, une stratégie et des recommandations ordonnées avec score entre 0 et 1.

Les modèles Pydantic validant ce contrat se trouvent dans `app/models.py`; le calcul est dans `app/recommendation.py`. Le détail de l'algorithme, de son amorçage synthétique et de ses limites figure dans [`../../docs/recommandation.md`](../../docs/recommandation.md).

## Vérification

L'étude de cas précise que l'API Python ne doit pas être testée. Aucun test automatisé Python n'est fourni. Pour vérifier manuellement le contrat pendant le développement, démarrer l'API et utiliser `/docs`; le parcours complet de recommandation est couvert côté Spring par les tests du client HTTP et du service, puis peut être essayé avec les deux API démarrées.
