import os
import re
from typing import List
import motor.motor_asyncio
from fastapi import FastAPI, Query, HTTPException
from pydantic import BaseModel

app = FastAPI()
# MongoDB setup
MONGO_DETAILS = os.getenv("MONGO_URI")
client = motor.motor_asyncio.AsyncIOMotorClient(MONGO_DETAILS)
database = client.accupuncture_db
collection = database.get_collection("accupuncture_data")


# Pydantic models
class PatternData(BaseModel):
    pattern: str
    symptoms: List[str]
    treatment_points: List[str]


class AcupunctureData(BaseModel):
    organ: str
    patterns: List[PatternData]


# Function to validate query input (prevent invalid input and injection attacks)
def validate_query(query: str):
    if not re.match(r'^[a-zA-Z0-9 ]*$', query):
        raise HTTPException(status_code=400, detail="Invalid search term.")


def remove_mongo_id(document):
    if "_id" in document:
        del document["_id"]  # Remove the _id field from the document
    return document


# Enhanced search endpoint with tailored responses for organ, symptom, and pattern
@app.get("/search")
async def search_data(query: str = Query(..., min_length=2)):
    validate_query(query)  # Validate input
    try:
        # First check if the query matches an organ (return the entire organ document)
        organ_query = {"organ": {"$regex": f"^{query}$", "$options": "i"}}  # Exact match with case insensitivity
        organ_result = await collection.find_one(organ_query)

        if organ_result:
            return remove_mongo_id(organ_result)
            # Return the full document if it's an organ match
        # If no organ match, search within patterns and symptoms
        search_query = {
            "$or": [
                {"patterns.pattern": {"$regex": query, "$options": "i"}},
                {"patterns.symptoms": {"$regex": query, "$options": "i"}}
            ]
        }
        documents = await collection.find(search_query).to_list(100)
        if not documents:
            raise HTTPException(status_code=404, detail="No matching data found.")
        # If it's a symptom or pattern search, return the relevant parts of the document
        results = []
        for document in documents:
            matched_data = {"organ": document["organ"], "patterns": []}

            for pattern_data in document["patterns"]:
                # Check if the query matches the pattern or symptoms
                if re.search(query, pattern_data["pattern"], re.IGNORECASE) or any(
                        re.search(query, symptom, re.IGNORECASE) for symptom in pattern_data["symptoms"]):
                    matched_data["patterns"].append(pattern_data)

            if matched_data["patterns"]:
                results.append(remove_mongo_id(matched_data))
        return results
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Internal server error: {str(e)}")

@app.get("/health")
# Endpoint for uptime robot to ping the api every 5 min to keep it active and warm
async def health_check():
    return {"status": "ok"}
