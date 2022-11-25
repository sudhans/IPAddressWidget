package com.msd.appwidget.ip

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.msd.appwidget.ip.ui.theme.IpAppWidgetTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IpAppWidgetTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ShowIpAddress() {
                        copyIpAddressToClipBoard(
                            this
                        )
                    }
                }
            }
        }
    }

    private fun copyIpAddressToClipBoard(context: Context) {
        val clipboardManager =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("ipAddress", getIpAddress(context))
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(context, "Ip Address copied to clipboard", Toast.LENGTH_SHORT).show()
    }
}

internal fun getAllIpAddress(context: Context) = with(context.getConnectivityManager()) {
    Log.i(TAG, "Getting ipAddress")
    getLinkProperties(activeNetwork)?.let {
        if (it.linkAddresses.size > 1) {
            Log.i(TAG, "LinkAddresses ${it.linkAddresses}")
            val ipAddresses = it.linkAddresses.map { linkAddress -> linkAddress.address.hostAddress }
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

@Composable
fun ShowIpAddress( onClick: () -> Unit = {}) {
    Text(
        text = "IP Address(es) \n ${getAllIpAddress(LocalContext.current)}",
        Modifier.clickable {
            onClick()
        }
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    IpAppWidgetTheme {
        ShowIpAddress()
    }
}