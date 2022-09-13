package com.example.glancelocationappwidget.widget.stateDefinition

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import androidx.datastore.dataStoreFile
import androidx.glance.state.GlanceStateDefinition
import com.example.glancelocationappwidget.widget.model.LocationInfo
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream
import java.io.OutputStream

object LocationInfoStateDefinition : GlanceStateDefinition<LocationInfo> {

    private const val DATA_STORE_FILENAME = "locationInfo"


    private val Context.datastore by dataStore(DATA_STORE_FILENAME, WeatherInfoSerializer)

    override suspend fun getDataStore(context: Context, fileKey: String): DataStore<LocationInfo> {
        return context.datastore
    }

    override fun getLocation(context: Context, fileKey: String): File {
        return context.dataStoreFile(DATA_STORE_FILENAME)
    }

    object WeatherInfoSerializer : Serializer<LocationInfo> {
        override val defaultValue = LocationInfo.Unavailable("no location found")

        override suspend fun readFrom(input: InputStream): LocationInfo = try {
            Json.decodeFromString(
                LocationInfo.serializer(),
                input.readBytes().decodeToString()
            )
        } catch (exception: SerializationException) {
            throw CorruptionException("Could not read location data: ${exception.message}")
        }

        override suspend fun writeTo(t: LocationInfo, output: OutputStream) {
            output.use {
                it.write(
                    Json.encodeToString(LocationInfo.serializer(), t).encodeToByteArray()
                )
            }
        }
    }
}