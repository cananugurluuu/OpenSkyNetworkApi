package com.redmuud.openskynetworkapi.ui.viewmodel

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
    
    fun loadPlanesInBoundingBox(
        minLatitude: Double,
        minLongitude: Double,
        maxLatitude: Double,
        maxLongitude: Double
    ) {
        viewModelScope.launch {
            try {
                val states = repository.getStatesInBoundingBox(
                    minLatitude,
                    minLongitude,
                    maxLatitude,
                    maxLongitude
                )
                _planes.value = states
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun selectPlane(plane: StateVector) {
        _selectedPlane.value = plane
    }
}
