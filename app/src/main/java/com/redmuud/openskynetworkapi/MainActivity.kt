package com.redmuud.openskynetworkapi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.redmuud.openskynetworkapi.ui.components.FlightListSheet
import com.redmuud.openskynetworkapi.ui.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

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
                        var showFlightList by remember { mutableStateOf(false) }
                        
                        // Initial camera position (Europe)
                        val initialPosition = LatLng(48.8566, 2.3522) // Paris
                        val cameraPositionState = rememberCameraPositionState {
                            position = CameraPosition.fromLatLngZoom(initialPosition, 5f)
                        }
                        
                        // Load planes periodically
                        LaunchedEffect(Unit) {
                            while (true) {
                                viewModel.loadPlanesInBoundingBox(
                                    minLatitude = 35.0,
                                    minLongitude = -10.0,
                                    maxLatitude = 60.0,
                                    maxLongitude = 25.0
                                )
                                delay(10000) // Update every 10 seconds
                            }
                        }
                        
                        // Update camera position when a plane is selected
                        LaunchedEffect(selectedPlane) {
                            selectedPlane?.let { plane ->
                                if (plane.latitude != null && plane.longitude != null) {
                                    cameraPositionState.position = CameraPosition.fromLatLngZoom(
                                        LatLng(plane.latitude, plane.longitude),
                                        12f
                                    )
                                }
                            }
                        }
                        
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState
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
                        
                        // Show Flight List Button
                        Button(
                            onClick = { showFlightList = true },
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(16.dp)
                        ) {
                            Text("Show Flight List")
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