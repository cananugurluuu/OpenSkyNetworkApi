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
                Log.d("MainViewModel", "Fetching planes data...")
                
                val states = repository.getStatesInBoundingBox(
                    minLatitude,
                    minLongitude,
                    maxLatitude,
                    maxLongitude
                )
                Log.d("MainViewModel", "Received ${states.size} planes")
                _planes.value = states
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error fetching planes", e)
                _error.value = "Failed to load planes: ${e.message}"
                _planes.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun selectPlane(plane: StateVector) {
        _selectedPlane.value = plane
    }
}
