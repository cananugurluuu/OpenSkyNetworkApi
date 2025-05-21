package com.redmuud.openskynetworkapi.model

data class StateVector(
    val icao24: String,          // Unique ICAO 24-bit address of the transponder
    val callsign: String?,       // Callsign of the vehicle
    val originCountry: String,   // Country of origin
    val longitude: Double?,      // WGS-84 longitude in decimal degrees
    val latitude: Double?,       // WGS-84 latitude in decimal degrees
    val altitude: Double?,       // Altitude in meters
    val velocity: Double?,       // Velocity over ground in m/s
    val heading: Double?,        // True track in decimal degrees clockwise from north
    val onGround: Boolean       // Boolean value indicating if the position was retrieved from a surface position report
)

data class StateResponse(
    val time: Long,
    val states: List<List<Any?>>
) {
    fun toStateVectors(): List<StateVector> {
        return states.mapNotNull { state ->
            if (state.size >= 8) {
                StateVector(
                    icao24 = (state[0] as? String) ?: return@mapNotNull null,
                    callsign = state[1] as? String,
                    originCountry = (state[2] as? String) ?: return@mapNotNull null,
                    longitude = (state[5] as? Double),
                    latitude = (state[6] as? Double),
                    altitude = (state[7] as? Double),
                    velocity = (state[9] as? Double),
                    heading = (state[10] as? Double),
                    onGround = (state[8] as? Boolean) ?: false
                )
            } else null
        }
    }
}
