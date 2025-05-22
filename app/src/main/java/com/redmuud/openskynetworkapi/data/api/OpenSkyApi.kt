package com.redmuud.openskynetworkapi.data.api

import com.redmuud.openskynetworkapi.model.StateResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenSkyApi {
    @GET("states/all")
    suspend fun getAllStates(
        @Query("lamin") minLatitude: Double,
        @Query("lomin") minLongitude: Double,
        @Query("lamax") maxLatitude: Double,
        @Query("lomax") maxLongitude: Double,
        @Query("extended") extended: Int = 1
    ): StateResponse
}
