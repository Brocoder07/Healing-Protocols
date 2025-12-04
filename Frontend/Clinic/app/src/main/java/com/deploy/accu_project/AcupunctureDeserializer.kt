package com.deploy.accu_project

import android.util.Log
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class AcupunctureListDeserializer : JsonDeserializer<List<AcupunctureResponse>> {

    init {
        Log.d("AcupunctureDeserializer", "Initializing Custom Deserializer...")
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): List<AcupunctureResponse> {

        Log.d("AcupunctureDeserializer", "Parsing JSON: $json")

        if (json.isJsonArray) {
            val jsonArray = json.asJsonArray
            val list = mutableListOf<AcupunctureResponse>()
            jsonArray.forEach { element ->
                list.add(context.deserialize(element, AcupunctureResponse::class.java))
            }
            return list
        } else if (json.isJsonObject) {
            Log.d("AcupunctureDeserializer", "Found Object, wrapping in List")
            val singleObject: AcupunctureResponse = context.deserialize(json, AcupunctureResponse::class.java)
            return listOf(singleObject)
        }

        throw JsonParseException("Unexpected JSON type: " + json.javaClass.simpleName)
    }
}
