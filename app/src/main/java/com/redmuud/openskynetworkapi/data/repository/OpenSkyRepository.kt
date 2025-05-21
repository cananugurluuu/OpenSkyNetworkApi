package com.redmuud.openskynetworkapi.data.repository

import android.util.Log
import com.redmuud.openskynetworkapi.data.api.OpenSkyApi
import com.redmuud.openskynetworkapi.model.StateVector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class OpenSkyRepository @Inject constructor(
    private val api: OpenSkyApi
) {
    suspend fun getStatesInBoundingBox(
        minLatitude: Double,
        minLongitude: Double,
        maxLatitude: Double,
        maxLongitude: Double
    ): List<StateVector> = withContext(Dispatchers.IO) {
        try {
            Log.d("OpenSkyRepository", "Fetching states for bounds: ($minLatitude, $minLongitude) to ($maxLatitude, $maxLongitude)")
            val response = api.getAllStates(
                minLatitude = minLatitude,
                minLongitude = minLongitude,
                maxLatitude = maxLatitude,
                maxLongitude = maxLongitude,
                time = System.currentTimeMillis() / 1000
            )
            Log.d("OpenSkyRepository", "Received response with ${response.states.size} states")
            response.toStateVectors()
        } catch (e: Exception) {
            Log.e("OpenSkyRepository", "Error fetching states", e)
            throw e
        }
    }
}
