package com.redmuud.openskynetworkapi.data.repository

import android.util.Log
import com.redmuud.openskynetworkapi.data.api.OpenSkyApi
import com.redmuud.openskynetworkapi.model.StateVector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class OpenSkyRepository @Inject constructor(
    private val api: OpenSkyApi
) {
    suspend fun getStatesInBoundingBox(
        minLatitude: Double,
        minLongitude: Double,
        maxLatitude: Double,
        maxLongitude: Double
    ): Result<List<StateVector>> = withContext(Dispatchers.IO) {
        try {
            Log.d("OpenSkyRepository", "Fetching states for bounds: ($minLatitude, $minLongitude) to ($maxLatitude, $maxLongitude)")
            
            val response = api.getAllStates(
                minLatitude = minLatitude,
                minLongitude = minLongitude,
                maxLatitude = maxLatitude,
                maxLongitude = maxLongitude
            )
            
            val states = response.toStateVectors()
            Log.d("OpenSkyRepository", "Successfully received ${states.size} states")
            Result.success(states)
            
        } catch (e: HttpException) {
            Log.e("OpenSkyRepository", "HTTP Error: ${e.code()} - ${e.message()}")
            when (e.code()) {
                401 -> Result.failure(Exception("Authentication failed. Please check your credentials."))
                403 -> Result.failure(Exception("Access forbidden. Please check your account status."))
                429 -> Result.failure(Exception("Too many requests. Please wait before trying again."))
                else -> Result.failure(Exception("Network error: ${e.message()}"))
            }
        } catch (e: IOException) {
            Log.e("OpenSkyRepository", "Network Error", e)
            Result.failure(Exception("Network error. Please check your internet connection."))
        } catch (e: Exception) {
            Log.e("OpenSkyRepository", "Unexpected Error", e)
            Result.failure(e)
        }
    }
}
