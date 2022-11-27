package com.msd.appwidget.ip

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                    ShowAllIpAddressList(
                        ipAddressList = getAllIpAddress(this),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }


}

internal fun shareTextData(context: Context, textToShare: String) {
    context.startActivity(Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, textToShare)
    })
}

internal fun copyIpAddressToClipBoard(context: Context, textToCopy: String) {
    val clipboardManager =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clipData = ClipData.newPlainText("ipAddress", textToCopy)
    clipboardManager.setPrimaryClip(clipData)
    Toast.makeText(context, "Ip Address copied to clipboard", Toast.LENGTH_SHORT).show()
}

internal fun getAllIpAddress(context: Context) = with(context.getConnectivityManager()) {
    Log.i(TAG, "Getting ipAddress")
    getLinkProperties(activeNetwork)?.let { linkProperties ->
        return linkProperties.linkAddresses
            .map { linkAddress ->

                linkAddress.address.hostAddress ?: DEFAULT_IP_ADDRESS

            }
            .sortedBy { it.length }
            .map { IpItemViewState(it) }
    } ?: listOf(IpItemViewState(DEFAULT_IP_ADDRESS))
}

@Composable
fun ShowAllIpAddressList(
    modifier: Modifier = Modifier,
    ipAddressList: List<IpItemViewState>,
    context: Context = LocalContext.current
) {
    LazyColumn(modifier = modifier) {
        items(items = ipAddressList) { data ->
            ShowIpListItem(ipItemViewState = data, onShare = {
                shareTextData(context, data.text)
            }, onClick = {
                copyIpAddressToClipBoard(context, data.text)
            })
        }
    }
}

@Composable
fun ShowIpListItem(
    ipItemViewState: IpItemViewState,
    onClick: () -> Unit = {},
    onShare: () -> Unit = {}
) {
    Row(verticalAlignment = Alignment.CenterVertically) {

        Icon(
            Icons.TwoTone.Share,
            modifier = Modifier
                .padding(16.dp)
                .clickable {
                    onShare()
                },
            contentDescription = stringResource(id = R.string.share_button_description)
        )

        Icon(
            painter = painterResource(id = R.drawable.ic_content_copy_24),
            modifier = Modifier
                .padding(16.dp)
                .clickable {
                    onClick()
                },
            contentDescription = stringResource(id = android.R.string.copy)
        )

        Text(
            text = ipItemViewState.text,
            fontSize = 24.sp,
            textAlign = TextAlign.Left,
            style = LocalTextStyle.current.merge(
                TextStyle(
                    lineHeightStyle = LineHeightStyle(
                        alignment = LineHeightStyle.Alignment.Center,
                        trim = LineHeightStyle.Trim.None
                    )
                )
            ),
            modifier = Modifier
                .padding(16.dp)
        )

    }


}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    IpAppWidgetTheme {
        ShowAllIpAddressList(ipAddressList = listOf(IpItemViewState("127.0.0.1")))

    }
}