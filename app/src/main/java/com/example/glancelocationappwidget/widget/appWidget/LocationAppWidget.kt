package com.example.glancelocationappwidget.widget.appWidget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.android.appwidget.R
import com.example.glancelocationappwidget.appWidgetBackgroundModifier
import com.example.glancelocationappwidget.widget.model.LocationInfo
import com.example.glancelocationappwidget.widget.stateDefinition.LocationInfoStateDefinition
import com.example.glancelocationappwidget.widget.worker.LocationWorker

class LocationAppWidget: GlanceAppWidget() {

    companion object {
        private val thinMode = DpSize(120.dp, 120.dp)
        private val smallMode = DpSize(184.dp, 184.dp)
        private val mediumMode = DpSize(260.dp, 200.dp)
        private val largeMode = DpSize(260.dp, 280.dp)
    }


    override val stateDefinition = LocationInfoStateDefinition


    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(thinMode, smallMode, mediumMode, largeMode)
    )

    @Composable
    override fun Content() {

        val locationInfo = currentState<LocationInfo>()
        val size = LocalSize.current
        CompositionLocalProvider {
            when (locationInfo) {
                LocationInfo.Loading -> {
                    Box(
                        modifier = appWidgetBackgroundModifier().then(GlanceModifier),
                        contentAlignment = Alignment.Center,
                        content = { CircularProgressIndicator() }
                    )
                }
                is LocationInfo.Available -> {
                    when (size) {
                        thinMode -> WeatherLarge(locationInfo)
                        smallMode -> WeatherLarge(locationInfo)
                        mediumMode -> WeatherLarge(locationInfo)
                        largeMode -> WeatherLarge(locationInfo)
                    }
                }
                is LocationInfo.Unavailable -> {
                    Column(
                        modifier = appWidgetBackgroundModifier().then(GlanceModifier),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        content = {
                            Text("Data not available")
                            Button("Refresh", actionRunCallback<UpdateLocationAction>())
                        },
                    )
                }
            }
        }
    }

    @Composable
    fun CurrentLocation(
        locationInfo: LocationInfo.Available,
        modifier: GlanceModifier = GlanceModifier,
        horizontal: Alignment.Horizontal = Alignment.Start
    ) {
        Column(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = horizontal
        ) {
            val defaultWeight = GlanceModifier.wrapContentSize()
            Text(
                text = "${locationInfo.currentData.latitude} latitude",
                style = TextStyle(
                    color = ColorProvider(R.color.purple_200),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = defaultWeight
            )
            Spacer(GlanceModifier.size(8.dp))
            Text(
                text = "${locationInfo.currentData.longitude} longitude",
                style = TextStyle(
                    color = ColorProvider(R.color.purple_200),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(GlanceModifier.size(8.dp))
            Text(
                text = "${locationInfo.currentData.altitude} altitude",
                style = TextStyle(
                    color = ColorProvider(R.color.purple_200),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }

    @Composable
    fun LocationIcon(modifier: GlanceModifier = GlanceModifier) {
        Box(modifier = modifier, contentAlignment = Alignment.TopStart) {
            Image(
                provider = ImageProvider(R.drawable.ic_location),
                contentDescription = stringResource(id = R.string.location),
                modifier = GlanceModifier.size(48.dp),
            )
        }
    }

    @Composable
    fun WeatherLarge(locationInfo: LocationInfo.Available) {
        Column(
            modifier = appWidgetBackgroundModifier().then(GlanceModifier.clickable(actionRunCallback<UpdateLocationAction>())),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.Start,
            content = {
                Row(
                    modifier = GlanceModifier.wrapContentHeight().fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    LocationIcon(modifier = GlanceModifier.fillMaxWidth().defaultWeight())
                    Button(stringResource(id = R.string.refresh), actionRunCallback<UpdateLocationAction>())
                }
                Row(
                    modifier = GlanceModifier.wrapContentHeight().fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    CurrentLocation(
                        locationInfo,
                        modifier = GlanceModifier.wrapContentHeight(),
                        Alignment.Start
                    )
                }
            },
        )
    }
}


class UpdateLocationAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        LocationWorker.enqueue(context = context, force = true)
    }
}