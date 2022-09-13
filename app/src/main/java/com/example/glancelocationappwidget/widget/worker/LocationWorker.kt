package com.example.glancelocationappwidget.widget.worker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.work.*
import com.example.glancelocationappwidget.widget.repo.LocationRepo
import com.example.glancelocationappwidget.widget.appWidget.LocationAppWidget
import com.example.glancelocationappwidget.widget.model.LocationInfo
import com.example.glancelocationappwidget.widget.stateDefinition.LocationInfoStateDefinition
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class LocationWorker(
    private val context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

    companion object {

        private val uniqueWorkName = LocationWorker::class.java.simpleName

        fun enqueue(context: Context, force: Boolean = false) {
            val manager = WorkManager.getInstance(context)
            val requestBuilder = OneTimeWorkRequestBuilder<LocationWorker>()
                .setInitialDelay(1, TimeUnit.MINUTES)
            var workPolicy = ExistingWorkPolicy.KEEP

            // Replace any enqueued work and expedite the request
            if (force) {
                workPolicy = ExistingWorkPolicy.REPLACE
            }

            manager.enqueueUniqueWork(
                uniqueWorkName,
                workPolicy,
                requestBuilder.build()
            )
        }


        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(uniqueWorkName)
        }
    }

    override suspend fun doWork(): Result {
        val manager = GlanceAppWidgetManager(context)
        val glanceIds = manager.getGlanceIds(LocationAppWidget::class.java)
        return try {

            setWidgetState(glanceIds, LocationInfo.Loading)
            val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return Result.failure()
            }
            fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                CoroutineScope(Dispatchers.IO).launch {
                    setWidgetState(glanceIds, LocationRepo.getLocationInfo(location = it))
                }
            }
            enqueue(context)

            Result.success()
        } catch (e: Exception) {
            setWidgetState(glanceIds, LocationInfo.Unavailable(e.message.orEmpty()))
            Result.failure()
        }
    }


    private fun setWidgetState(glanceIds: List<GlanceId>, newState: LocationInfo) {
        MainScope().launch {
            glanceIds.forEach { glanceId ->
                updateAppWidgetState(
                    context = context,
                    definition = LocationInfoStateDefinition,
                    glanceId = glanceId,
                    updateState = { newState }
                )
            }
            LocationAppWidget().updateAll(context)
        }
    }
}