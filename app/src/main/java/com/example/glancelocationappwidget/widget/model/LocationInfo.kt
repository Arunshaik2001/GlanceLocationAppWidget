package com.example.glancelocationappwidget.widget.model

import kotlinx.serialization.Serializable

@Serializable
sealed interface LocationInfo {
    @Serializable
    object Loading : LocationInfo

    @Serializable
    data class Available(
        val placeName: String,
        val currentData: LocationData,
    ) : LocationInfo

    @Serializable
    data class Unavailable(val message: String) : LocationInfo
}

@Serializable
data class LocationData(
    val latitude: String,
    val longitude: String,
    val altitude: String,
    val accuracy: String
)