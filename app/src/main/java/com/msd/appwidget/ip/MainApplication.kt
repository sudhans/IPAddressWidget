package com.msd.appwidget.ip

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.util.Log

class MainApplication : Application() {
    private val appContext by lazy {
        this
    }

    private val networkRequest = NetworkRequest.Builder()
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
        .addTransportType(NetworkCapabilities.TRANSPORT_BLUETOOTH)
        .addTransportType(NetworkCapabilities.TRANSPORT_VPN)
        .build()
    private val networkCallback = object: ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            Log.i(TAG, "Network Available")
            updateAppWidget(appContext)
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            Log.i(TAG, "Network Lost")
            updateAppWidget(appContext)
        }

        override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
            super.onLinkPropertiesChanged(network, linkProperties)
            Log.i(TAG, "Network Link Properties Changed")
            updateAppWidget(appContext)
        }
    }

    private val wifiStateChangedBroadcastReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.i(TAG, "Wifi State Changed")
            updateAppWidget(appContext)
        }

    }

    override fun onCreate() {
        super.onCreate()
        getConnectivityManager().registerNetworkCallback(networkRequest, networkCallback)

        val wifiStateChangedIntentFilter = IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION)
        registerReceiver(wifiStateChangedBroadcastReceiver, wifiStateChangedIntentFilter)
    }
}