package com.example.glancelocationappwidget.widget.repo

import android.location.Location
import com.example.glancelocationappwidget.widget.model.LocationData
import com.example.glancelocationappwidget.widget.model.LocationInfo
import kotlinx.coroutines.delay
import kotlin.random.Random

object LocationRepo {

    suspend fun getLocationInfo(location: Location?, delay: Long = Random.nextInt(1, 3) * 1000L): LocationInfo {
        if (delay > 0) {
            delay(delay)
        }
        return LocationInfo.Available(
            placeName = "",
            currentData = LocationData(
                latitude = location?.latitude.toString(),
                longitude = location?.longitude.toString(),
                accuracy = location?.accuracy.toString(),
                altitude = location?.altitude.toString()
            )
        )
    }

}