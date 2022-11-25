package com.msd.appwidget.ip

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.util.Log
import android.widget.RemoteViews
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [IpAppWidgetProviderConfigureActivity]
 */
class IpAppWidgetProvider : AppWidgetProvider() {


    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        val action = intent!!.action
        Log.i(TAG, "Action $action")

        if (WifiManager.WIFI_STATE_CHANGED_ACTION == action) {
            val extras = intent.extras
            if (extras != null) {
                val appWidgetIds = extras.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS)
                if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
                    onUpdate(context!!, AppWidgetManager.getInstance(context), appWidgetIds)
                }
            }
        } else if (INTENT_ACTION_AUTO_UPDATE == action) {
            context?.let {
                getWidgetsAndUpdate(it)
            }

        } else if (INTENT_ACTION_ON_CLICK == action) {
            context?.let {
                getWidgetsAndUpdate(it)
                // Launch Main Activity
                val mainIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                mainIntent?.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(mainIntent)
            }
        }
    }

    private fun getWidgetsAndUpdate(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context.packageName, javaClass.name)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        if (appWidgetIds.isNotEmpty()) {
            onUpdate(context, AppWidgetManager.getInstance(context), appWidgetIds)
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
        Log.i(TAG, "Widget Enabled")
        context?.let {
            val workRequest = PeriodicWorkRequestBuilder<UpdateWidgetWorker>(30, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork("ipAddressWidgetUpdateWork", ExistingPeriodicWorkPolicy.KEEP, workRequest)


        }

    }

    override fun onDisabled(context: Context?) {
        super.onDisabled(context)
        Log.i(TAG, "Widget Disabled")
        context?.let {
            WorkManager.getInstance(context).cancelUniqueWork("ipAddressWidgetUpdateWork")
        }

    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    Log.i(TAG, "updateAppWidget")
    val widgetText = getIpAddress(context)
    Log.i(TAG, "ipAddress is $widgetText")
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.ip_app_widget_provider)
    views.setTextViewText(R.id.appwidget_text, widgetText)
    views.setOnClickPendingIntent(
        R.id.appwidget_text,
        getPendingSelfIntent(context, INTENT_ACTION_ON_CLICK)
    )

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}

internal fun getIpAddress(context: Context) = with(context.getConnectivityManager()) {
    Log.i(TAG, "Getting ipAddress")
    getLinkProperties(activeNetwork)?.let {
        if (it.linkAddresses.size > 1) {
            Log.i(TAG, "LinkAddresses ${it.linkAddresses}")
            val ipAddresses = it.linkAddresses.map { linkAddress -> linkAddress.address.hostAddress }
                .filter { address ->
                    address?.contains(".", true) ?: false
                }

           return if (ipAddresses.isEmpty()) {
                DEFAULT_IP_ADDRESS
            } else {
                ipAddresses.joinToString(", \n")
           }
        } else {
            Log.i(TAG, "linkAddresses less than one ${it.linkAddresses}")
            it.linkAddresses[0].address.hostAddress ?: DEFAULT_IP_ADDRESS
        }
    } ?: DEFAULT_IP_ADDRESS
}



internal fun Context.getConnectivityManager() =
    getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

internal fun getPendingSelfIntent(context: Context, action: String): PendingIntent {
    val intent = Intent(context, IpAppWidgetProvider::class.java)
    intent.action = action
    return PendingIntent.getBroadcast(
        context,
        ALARM_ID,
        intent,
        PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

}

internal fun updateAppWidget(appContext: Context) {
    val intent = Intent(INTENT_ACTION_AUTO_UPDATE, null, appContext, IpAppWidgetProvider::class.java)
    appContext.sendBroadcast(intent)
}