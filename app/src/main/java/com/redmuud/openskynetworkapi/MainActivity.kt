package com.redmuud.openskynetworkapi

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.redmuud.openskynetworkapi.ui.components.FlightListSheet
import com.redmuud.openskynetworkapi.ui.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        val planes by viewModel.planes.collectAsState()
                        val selectedPlane by viewModel.selectedPlane.collectAsState()
                        val isLoading by viewModel.isLoading.collectAsState()
                        val error by viewModel.error.collectAsState()
                        var showFlightList by remember { mutableStateOf(false) }
                        val context = LocalContext.current
                        val scope = rememberCoroutineScope()
                        
                        // Track if camera is being moved by user
                        var isMapMoving by remember { mutableStateOf(false) }
                        var lastUpdateTime by remember { mutableStateOf(0L) }
                        
                        // Show error toast if there's an error
                        LaunchedEffect(error) {
                            error?.let {
                                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                            }
                        }
                        
                        // Initial camera position (Istanbul)
                        val initialPosition = LatLng(41.0082, 28.9784) // Istanbul
                        val cameraPositionState = rememberCameraPositionState {
                            position = CameraPosition.fromLatLngZoom(initialPosition, 9f)
                        }
                        
                        // Function to refresh data for current bounds
                        val refreshData = {
                            cameraPositionState.projection?.visibleRegion?.latLngBounds?.let { bounds ->
                                viewModel.loadPlanesInBoundingBox(
                                    minLatitude = bounds.southwest.latitude,
                                    minLongitude = bounds.southwest.longitude,
                                    maxLatitude = bounds.northeast.latitude,
                                    maxLongitude = bounds.northeast.longitude
                                )
                            }
                        }
                        
                        // Update data when camera movement ends
                        LaunchedEffect(cameraPositionState.position) {
                            if (!isMapMoving) {
                                val currentTime = System.currentTimeMillis()
                                // Only update if more than 2 seconds have passed since last update
                                if (currentTime - lastUpdateTime > 2000) {
                                    lastUpdateTime = currentTime
                                    refreshData()
                                }
                            }
                        }
                        
                        // Periodic refresh every 10 seconds when map is not moving
                        LaunchedEffect(Unit) {
                            while (isActive) {
                                delay(10000) // Wait for 10 seconds
                                if (!isMapMoving && !isLoading) {
                                    refreshData()
                                }
                            }
                        }
                        
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState,
                            onMapLoaded = {
                                // Initial data load
                                scope.launch {
                                    refreshData()
                                }
                            },
                            onMapClick = {
                                viewModel.selectPlane(null)
                            },
                            properties = MapProperties(
                                isMyLocationEnabled = false
                            ),
                            uiSettings = MapUiSettings(
                                compassEnabled = true,
                                zoomControlsEnabled = true,
                                myLocationButtonEnabled = false
                            )
                        ) {
                            planes.forEach { plane ->
                                if (plane.latitude != null && plane.longitude != null) {
                                    Marker(
                                        state = MarkerState(
                                            position = LatLng(
                                                plane.latitude,
                                                plane.longitude
                                            )
                                        ),
                                        title = plane.callsign ?: plane.icao24,
                                        snippet = "Alt: ${plane.altitude?.toInt()}m, Speed: ${plane.velocity?.toInt()}m/s",
                                        rotation = plane.heading?.toFloat() ?: 0f,
                                        onClick = {
                                            viewModel.selectPlane(plane)
                                            true
                                        }
                                    )
                                }
                            }
                        }
                        
                        Column(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(16.dp)
                        ) {
                            if (isLoading) {
                                LinearProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                )
                            }
                            
                            Button(
                                onClick = { showFlightList = true },
                                enabled = !isLoading && planes.isNotEmpty()
                            ) {
                                Text(if (isLoading) "Loading..." else "Show Flight List (${planes.size})")
                            }
                        }
                        
                        // Flight List Bottom Sheet
                        if (showFlightList) {
                            FlightListSheet(
                                planes = planes,
                                onPlaneSelected = { plane ->
                                    viewModel.selectPlane(plane)
                                    showFlightList = false
                                },
                                onDismiss = { showFlightList = false }
                            )
                        }
                    }
                }
            }
        }
    }
}