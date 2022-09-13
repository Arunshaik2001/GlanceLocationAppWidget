package com.example.glancelocationappwidget


import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import com.example.android.appwidget.R


@Composable
fun appWidgetBackgroundModifier() = GlanceModifier
    .fillMaxSize()
    .padding(16.dp)
    .appWidgetBackground()
    .background(R.color.background)
    .appWidgetCornerRadius()

fun GlanceModifier.appWidgetCornerRadius(): GlanceModifier {
    if (Build.VERSION.SDK_INT >= 31) {
        cornerRadius(android.R.dimen.system_app_widget_background_radius)
    } else {
        cornerRadius(16.dp)
    }
    return this
}



