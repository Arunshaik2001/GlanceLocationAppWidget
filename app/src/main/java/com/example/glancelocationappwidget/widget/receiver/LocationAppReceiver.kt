package com.example.glancelocationappwidget.widget.receiver

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.example.glancelocationappwidget.widget.appWidget.LocationAppWidget
import com.example.glancelocationappwidget.widget.worker.LocationWorker

class LocationAppReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = LocationAppWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        LocationWorker.enqueue(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        LocationWorker.cancel(context)
    }
}