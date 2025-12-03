import os
import re
from typing import List
import motor.motor_asyncio
from fastapi import FastAPI, Query, HTTPException
from pydantic import BaseModel
import certifi

app = FastAPI()

# MongoDB setup
MONGO_DETAILS = os.getenv("MONGO_URI")
# Optimization: certifi is correctly used here
client = motor.motor_asyncio.AsyncIOMotorClient(MONGO_DETAILS, tlsCAFile=certifi.where())
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


# Function to validate query input
def validate_query(query: str):
    if not re.match(r'^[a-zA-Z0-9 ]*$', query):
        raise HTTPException(status_code=400, detail="Invalid search term.")


# Optimization: Enforcing response_model for faster serialization and validation
@app.get("/search", response_model=List[AcupunctureData])
async def search_data(query: str = Query(..., min_length=2)):
    validate_query(query)
    try:
        # 1. Exact Organ Match
        # Optimization: Use projection {"_id": 0} to exclude ID at the DB level
        organ_query = {"organ": {"$regex": f"^{query}$", "$options": "i"}}
        organ_result = await collection.find_one(organ_query, {"_id": 0})

        if organ_result:
            # Optimization: Return as a list to match response_model consistency
            return [organ_result]

        # 2. Pattern/Symptom Match (Aggregation Pipeline)
        # Optimization: Offload filtering to MongoDB instead of Python loops
        pipeline = [
            {
                "$match": {
                    "$or": [
                        {"patterns.pattern": {"$regex": query, "$options": "i"}},
                        {"patterns.symptoms": {"$regex": query, "$options": "i"}}
                    ]
                }
            },
            {"$unwind": "$patterns"},
            {
                "$match": {
                    "$or": [
                        {"patterns.pattern": {"$regex": query, "$options": "i"}},
                        {"patterns.symptoms": {"$regex": query, "$options": "i"}}
                    ]
                }
            },
            {
                "$group": {
                    "_id": "$_id",
                    "organ": {"$first": "$organ"},
                    "patterns": {"$push": "$patterns"}
                }
            },
            {"$project": {"_id": 0}},  # Exclude _id from final output
            {"$limit": 100}  # Safety limit
        ]

        results = await collection.aggregate(pipeline).to_list(100)

        if not results:
            raise HTTPException(status_code=404, detail="No matching data found.")

        return results

    except Exception as e:
        # Catch errors but re-raise HTTPExceptions (like 404) correctly
        if isinstance(e, HTTPException):
            raise e
        raise HTTPException(status_code=500, detail=f"Internal server error: {str(e)}")


@app.head("/search")
async def search_head(query: str = Query(..., min_length=2)):
    validate_query(query)
    try:
        organ_query = {"organ": {"$regex": f"^{query}$", "$options": "i"}}
        # Optimization: Use count_documents or limit 1 with projection for speed
        if await collection.count_documents(organ_query, limit=1) > 0:
            return {"detail": "Resource available"}

        search_query = {
            "$or": [
                {"patterns.pattern": {"$regex": query, "$options": "i"}},
                {"patterns.symptoms": {"$regex": query, "$options": "i"}}
            ]
        }
        if await collection.count_documents(search_query, limit=1) > 0:
            return {"detail": "Resource available"}

        raise HTTPException(status_code=404, detail="No matching data found.")
    except Exception as e:
        if isinstance(e, HTTPException):
            raise e
        raise HTTPException(status_code=500, detail=f"Internal server error: {str(e)}")
