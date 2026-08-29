package com.minimalphone.launcher.core.system

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class RealNetworkStatus(
    val isWifiConnected: Boolean = false,
    val isCellularConnected: Boolean = false
)

class NetworkStatusMonitor(context: Context) {
    private val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val _status = MutableStateFlow(getCurrentStatus())
    val status: StateFlow<RealNetworkStatus> = _status

    private fun getCurrentStatus(): RealNetworkStatus {
        val network = cm.activeNetwork ?: return RealNetworkStatus()
        val caps = cm.getNetworkCapabilities(network) ?: return RealNetworkStatus()
        return RealNetworkStatus(
            isWifiConnected = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI),
            isCellularConnected = caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        )
    }

    init {
        try {
            cm.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    _status.value = getCurrentStatus()
                }

                override fun onLost(network: Network) {
                    _status.value = getCurrentStatus()
                }

                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    _status.value = getCurrentStatus()
                }
            })
        } catch (e: Exception) {
            _status.value = getCurrentStatus()
        }
    }
}
