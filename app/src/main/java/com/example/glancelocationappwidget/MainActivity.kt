package com.example.glancelocationappwidget

import android.Manifest
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import com.example.glancelocationappwidget.widget.repo.LocationRepo
import com.example.glancelocationappwidget.ui.theme.GlanceLocationAppWidgetTheme
import com.example.glancelocationappwidget.widget.appWidget.LocationAppWidget
import com.example.glancelocationappwidget.widget.stateDefinition.LocationInfoStateDefinition
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val TAG = "MainActivity"
    var LOCATION_REQUEST_CODE = 10001

    var fusedLocationProviderClient: FusedLocationProviderClient? = null
    var locationRequest: LocationRequest? = null

    var locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                MainScope().launch {
                    val manager = GlanceAppWidgetManager(context = applicationContext)
                    val glanceIds = manager.getGlanceIds(LocationAppWidget::class.java)
                    glanceIds.forEach { glanceId ->
                        updateAppWidgetState(
                            context = applicationContext,
                            definition = LocationInfoStateDefinition,
                            glanceId = glanceId,
                            updateState = { LocationRepo.getLocationInfo(location) }
                        )
                    }
                    LocationAppWidget().updateAll(applicationContext)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLastLocation()
            checkSettingsAndStartLocationUpdates();
        } else {
            askLocationPermission();
        }
    }

    override fun onStop() {
        super.onStop()
        stopLocationUpdates()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = LocationRequest.create().apply {
            interval = 4000
            fastestInterval = 2000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        };
        val widgetManager = AppWidgetManager.getInstance(this)


        val widgetProviders = widgetManager.getInstalledProvidersForPackage(packageName, null)

        setContent {
            val colors = if (isSystemInDarkTheme()) {
                darkColors()
            } else {
                lightColors()
            }
            MaterialTheme(colors) {
                Scaffold {
                    LazyColumn(contentPadding = it) {
                        items(widgetProviders) { providerInfo ->
                            WidgetInfoCard(providerInfo)
                        }
                    }
                }
            }
        }
    }


    private fun getLastLocation() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val locationTask: Task<Location> = fusedLocationProviderClient!!.lastLocation
        locationTask.addOnSuccessListener { location ->
            if (location != null) {
                MainScope().launch {
                    val manager = GlanceAppWidgetManager(context = applicationContext)
                    val glanceIds = manager.getGlanceIds(LocationAppWidget::class.java)
                    glanceIds.forEach { glanceId ->
                        updateAppWidgetState(
                            context = applicationContext,
                            definition = LocationInfoStateDefinition,
                            glanceId = glanceId,
                            updateState = { LocationRepo.getLocationInfo(location) }
                        )
                    }
                    LocationAppWidget().updateAll(applicationContext)
                }
            } else {
                Log.d(TAG, "onSuccess: Location was null...")
            }
        }
        locationTask.addOnFailureListener { e ->
            Log.e(
                TAG,
                "onFailure: " + e.localizedMessage
            )
        }
    }

    private fun askLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_REQUEST_CODE
                )
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_REQUEST_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkSettingsAndStartLocationUpdates()
            } else {
                //Permission not granted
            }
        }
    }

    private fun checkSettingsAndStartLocationUpdates() {
        val request = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest!!).build()
        val client = LocationServices.getSettingsClient(this)
        val locationSettingsResponseTask: Task<LocationSettingsResponse> =
            client.checkLocationSettings(request)
        locationSettingsResponseTask.addOnSuccessListener {
            startLocationUpdates()
        }
        locationSettingsResponseTask.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                try {
                    e.startResolutionForResult(this@MainActivity, 1001)
                } catch (ex: IntentSender.SendIntentException) {
                    ex.printStackTrace()
                }
            }
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationProviderClient!!.requestLocationUpdates(
            locationRequest!!,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun stopLocationUpdates() {
        fusedLocationProviderClient!!.removeLocationUpdates(locationCallback)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun WidgetInfoCard(providerInfo: AppWidgetProviderInfo) {
    val context = LocalContext.current
    val label = providerInfo.loadLabel(context.packageManager)
    val description = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        providerInfo.loadDescription(context).toString()
    } else {
        "Description not available"
    }
    val preview = painterResource(id = providerInfo.previewImage)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        onClick = {
            providerInfo.pin(context)
        }
    ) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.h6
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.body1
                )
            }
            Image(painter = preview, contentDescription = description)
        }
    }
}

private fun AppWidgetProviderInfo.pin(context: Context) {
    val successCallback = PendingIntent.getBroadcast(
        context,
        0,
        Intent(context, AppWidgetPinnedReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    AppWidgetManager.getInstance(context).requestPinAppWidget(provider, null, successCallback)
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    GlanceLocationAppWidgetTheme {
        Greeting("Android")
    }
}