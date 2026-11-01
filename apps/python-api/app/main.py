from fastapi import FastAPI
from .recommendation import generate_recommendations
from .models import RecommendationResponse, UserData

app = FastAPI()

# Endpoint de base pour tester que l'API est en ligne
@app.get("/")
async def root():
    return {"message": "API de recommandation en ligne"}

# Endpoint pour envoyer les données d'utilisateur et récupérer des recommandations
@app.post("/recommendations/", response_model=RecommendationResponse)
async def get_recommendations(data: UserData):
    return generate_recommendations(data)
