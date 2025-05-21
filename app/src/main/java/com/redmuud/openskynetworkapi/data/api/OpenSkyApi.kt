package com.redmuud.openskynetworkapi.data.api

import com.redmuud.openskynetworkapi.model.StateResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenSkyApi {
    @GET("states/all")
    suspend fun getAllStates(
        @Query("lamin") minLatitude: Double? = null,
        @Query("lomin") minLongitude: Double? = null,
        @Query("lamax") maxLatitude: Double? = null,
        @Query("lomax") maxLongitude: Double? = null,
        @Query("time") time: Long? = null
    ): StateResponse
}
