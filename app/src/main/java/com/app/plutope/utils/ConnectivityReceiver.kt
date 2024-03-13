package com.app.plutope.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL


class ConnectivityReceiver : BroadcastReceiver() {

    interface ConnectivityReceiverListener {
        fun onNetworkConnectionChanged(isConnected: Boolean)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            if (connectivityReceiverListener != null) {
                GlobalScope.launch(Dispatchers.IO) {
                    connectivityReceiverListener?.onNetworkConnectionChanged(isConnected(context))
                }
            }
        }
    }

    private fun isConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            capabilities != null &&
                    (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) &&
                    isInternetAvailable()
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting && isInternetAvailable()
        }
    }

    private fun isInternetAvailable(): Boolean {
        return try {
            // Try to connect to a known server (e.g., Google's DNS)
            val url = URL("http://www.google.com")
            val urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.connectTimeout = 3000 // Timeout in milliseconds
            urlConnection.connect()
            urlConnection.responseCode == HttpURLConnection.HTTP_OK


        } catch (e: IOException) {
            loge("ConnectivityReceiver", "Error checking internet connection :: ${e.message}")
            false
        }
    }

    private var connectivityReceiverListener: ConnectivityReceiverListener? = null

    fun setConnectivityReceiverListener(listener: ConnectivityReceiverListener) {
        connectivityReceiverListener = listener
    }
}