package com.redmuud.openskynetworkapi.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redmuud.openskynetworkapi.data.repository.OpenSkyRepository
import com.redmuud.openskynetworkapi.model.StateVector
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: OpenSkyRepository
) : ViewModel() {
    
    private val _planes = MutableStateFlow<List<StateVector>>(emptyList())
    val planes = _planes.asStateFlow()
    
    private val _selectedPlane = MutableStateFlow<StateVector?>(null)
    val selectedPlane = _selectedPlane.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    // Istanbul bounding box coordinates
    private val istanbulBounds = BoundingBox(
        minLatitude = 40.226013967,
        minLongitude = 27.3445316488,
        maxLatitude = 41.6004635693,
        maxLongitude = 30.7411966586
    )
    
    init {
        loadPlanesInIstanbul()
    }
    
    private fun loadPlanesInIstanbul() {
        loadPlanesInBoundingBox(
            minLatitude = istanbulBounds.minLatitude,
            minLongitude = istanbulBounds.minLongitude,
            maxLatitude = istanbulBounds.maxLatitude,
            maxLongitude = istanbulBounds.maxLongitude
        )
    }
    
    fun loadPlanesInBoundingBox(
        minLatitude: Double,
        minLongitude: Double,
        maxLatitude: Double,
        maxLongitude: Double
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                Log.d("MainViewModel", "Fetching planes data for bounds: ($minLatitude, $minLongitude) to ($maxLatitude, $maxLongitude)")
                
                repository.getStatesInBoundingBox(
                    minLatitude,
                    minLongitude,
                    maxLatitude,
                    maxLongitude
                ).fold(
                    onSuccess = { states ->
                        Log.d("MainViewModel", "Successfully received ${states.size} planes")
                        _planes.value = states
                        _error.value = null
                    },
                    onFailure = { exception ->
                        Log.e("MainViewModel", "Error loading planes", exception)
                        _error.value = exception.message
                        _planes.value = emptyList()
                    }
                )
            } catch (e: Exception) {
                Log.e("MainViewModel", "Unexpected error", e)
                _error.value = "An unexpected error occurred: ${e.message}"
                _planes.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun selectPlane(plane: StateVector?) {
        _selectedPlane.value = plane
    }
}

private data class BoundingBox(
    val minLatitude: Double,
    val minLongitude: Double,
    val maxLatitude: Double,
    val maxLongitude: Double
)
