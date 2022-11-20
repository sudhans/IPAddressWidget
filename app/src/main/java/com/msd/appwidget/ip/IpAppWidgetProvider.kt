package com.msd.appwidget.ip

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.widget.RemoteViews

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [IpAppWidgetProviderConfigureActivity]
 */
class IpAppWidgetProvider : AppWidgetProvider() {


    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        val action = intent!!.action
        println("MSD:: Action $action")
        if (WifiManager.WIFI_STATE_CHANGED_ACTION == action) {
            val extras = intent.extras
            if (extras != null) {
                val appWidgetIds = extras.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS)
                if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
                    onUpdate(context!!, AppWidgetManager.getInstance(context), appWidgetIds)
                }
            }
        } else if (INTENT_ACTION_AUTO_UPDATE == action) {
            val appWidgetManager = AppWidgetManager.getInstance(context!!)
            val componentName = ComponentName(context.packageName, javaClass.name)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (appWidgetIds.isNotEmpty()) {
                onUpdate(context!!, AppWidgetManager.getInstance(context), appWidgetIds)
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context?) {
        super.onEnabled(context)
        println("MSD:: Widget Enabled")
        context?.let {
            val appWidgetAlarm = AppWidgetAlarm(it.applicationContext)
            appWidgetAlarm.startAlarm()
        }

    }

    override fun onDisabled(context: Context?) {
        super.onDisabled(context)
        println("MSD:: Widget Disabled")
        context?.let {
            val appWidgetManager = AppWidgetManager.getInstance(it)
            val componentName = ComponentName(it.packageName, javaClass.name)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (appWidgetIds.isEmpty()) {
                val appWidgetAlarm = AppWidgetAlarm(it.applicationContext)
                appWidgetAlarm.stopAlarm()
            }
        }

    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    println("MSD:: updateAppWidget")
    val widgetText = getIpAddress(context)
    println("MSD:: ipAddress is $widgetText")
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.ip_app_widget_provider)
    views.setTextViewText(R.id.appwidget_text, widgetText)
    views.setOnClickPendingIntent(R.id.appwidget_text, getPendingSelfIntent(context, INTENT_ACTION_AUTO_UPDATE))

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}

internal fun getIpAddress(context: Context) = with(context.getConnectivityManager()) {
    println("MSD:: Getting ipAddress")
    getLinkProperties(activeNetwork)?.let {
        if (it.linkAddresses.size > 1) {
            println("MSD:: Host:: ${it.linkAddresses[1].address.hostAddress}")
            it.linkAddresses[1].address.hostAddress ?: DEFAULT_IP_ADDRESS
        } else {
            println("MSD:: linkAddresses less than one ${it.linkAddresses}")
            it.linkAddresses[0].address.hostAddress ?: DEFAULT_IP_ADDRESS
        }
    } ?: DEFAULT_IP_ADDRESS
}

internal fun Context.getConnectivityManager() =
    getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

internal fun getPendingSelfIntent(context: Context, action: String): PendingIntent {
    val intent = Intent(context, IpAppWidgetProvider::class.java)
    intent.action = action
    return PendingIntent.getBroadcast(context, ALARM_ID, intent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)

}