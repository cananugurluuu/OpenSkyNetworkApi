package com.redmuud.openskynetworkapi.data.repository

import com.redmuud.openskynetworkapi.data.api.OpenSkyApi
import com.redmuud.openskynetworkapi.model.StateVector
import javax.inject.Inject

class OpenSkyRepository @Inject constructor(
    private val api: OpenSkyApi
) {
    suspend fun getStatesInBoundingBox(
        minLatitude: Double,
        minLongitude: Double,
        maxLatitude: Double,
        maxLongitude: Double
    ): List<StateVector> {
        return api.getAllStates(minLatitude, minLongitude, maxLatitude, maxLongitude)
            .toStateVectors()
    }
}
