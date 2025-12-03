package com.deploy.accu_project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AcupunctureViewModel : ViewModel() {

    // StateFlow is the modern replacement for LiveData in Compose
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _searchResults = MutableStateFlow<List<AcupunctureResponse>>(emptyList())
    val searchResults: StateFlow<List<AcupunctureResponse>> = _searchResults.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _showErrorSnackBar = MutableStateFlow(false)
    val showErrorSnackBar: StateFlow<Boolean> = _showErrorSnackBar.asStateFlow()

    // Simple in-memory cache
    private val searchCache = mutableMapOf<String, List<AcupunctureResponse>>()

    fun search(query: String) {
        // 1. Check Cache first
        if (searchCache.containsKey(query)) {
            _searchResults.value = searchCache[query]!!
            return
        }

        // 2. Launch Coroutine for Network Request
        viewModelScope.launch {
            _loading.value = true
            _error.value = null // Clear previous errors

            try {
                // Retrofit now handles the JSON parsing automatically
                val results = RetrofitInstance.api.searchAcupuncture(query)

                _searchResults.value = results

                // Cache Optimization
                if (searchCache.size > 50) {
                    searchCache.clear()
                }
                searchCache[query] = results

            } catch (e: Exception) {
                // Handle Network or Parsing errors
                _error.value = "Error: ${e.localizedMessage ?: "Unknown error occurred"}"
                _showErrorSnackBar.value = true
            } finally {
                _loading.value = false
            }
        }
    }

    // Call this when the Snackbar is dismissed to reset the state
    fun dismissSnackBar() {
        _showErrorSnackBar.value = false
    }
}
