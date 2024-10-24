package com.deploy.accu_project

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody

class AcupunctureViewModel : ViewModel() {
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading
    private val _searchResults = MutableLiveData<List<AcupunctureResponse>>()
    private val searchCache = mutableMapOf<String, List<AcupunctureResponse>>()
    val searchResults: LiveData<List<AcupunctureResponse>> get() = _searchResults
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error
    private val _showErrorSnackBar = MutableLiveData<Boolean>()
    val showErrorSnackBar: LiveData<Boolean> get() = _showErrorSnackBar

    fun search(query: String) {
        if (searchCache.containsKey(query)) {
            _searchResults.value = searchCache[query] //Use cached result
        } else {
            _loading.value = true //Set loading state
            RetrofitInstance.api.searchAcupuncture(query).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    _loading.value = false //Loading done
                    if (response.isSuccessful) {
                        response.body()?.let { responseBody ->
                            try {
                                val json = responseBody.string()
                                val jsonElement = JsonParser.parseString(json)
                                //Checking if the response is an array or a single object
                                if (jsonElement.isJsonArray) {
                                    //Handle array
                                    val listType = object : TypeToken<List<AcupunctureResponse>>() {}.type
                                    val results: List<AcupunctureResponse> = Gson().fromJson(jsonElement, listType)
                                    _searchResults.value = results
                                    searchCache[query] = results //Caching the result
                                } else if (jsonElement.isJsonObject) {
                                    //Handle single object
                                    val singleResult: AcupunctureResponse = Gson().fromJson(
                                        jsonElement,
                                        AcupunctureResponse::class.java
                                    )
                                    _searchResults.value = listOf(singleResult) //Converting single object to a list
                                    searchCache[query] = listOf(singleResult) //Caching the result
                                }
                            } catch (e: Exception) {
                                _error.value = "Parsing error: ${e.message}"
                                _showErrorSnackBar.value = true
                            }
                        }
                    } else {
                        _error.value = "Error: ${response.code()}, Invalid search"
                        _showErrorSnackBar.value = true
                    }
                }
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    _loading.value = false //Loading done even on failure
                    Log.e("AcupunctureViewModel", "Failed to fetch data: ${t.message}", t)
                    _error.value = "Response takes longer to load sometimes. Please try again later."
                    _showErrorSnackBar.value = true
                }
            })
        }
    }
}