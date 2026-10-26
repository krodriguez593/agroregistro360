package ec.dx3os.launcher.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import ec.dx3os.core.connectivity.NetworkStatus

/** Tipo de red activa. API 24+: callback; API 21-23: broadcast CONNECTIVITY_ACTION. */
class NetworkMonitor(private val context: Context) : Observable<NetworkStatus>(NetworkStatus.UNKNOWN) {

    private val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) = refresh()
        override fun onLost(network: Network) = refresh()
        override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) = refresh()
    }

    private val legacyReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context, i: Intent) = refresh()
    }

    @Suppress("DEPRECATION")
    fun refresh() {
        val m = cm ?: return publish(NetworkStatus.UNKNOWN)
        val status = try {
            if (Build.VERSION.SDK_INT >= 23) {
                val caps = m.activeNetwork?.let { m.getNetworkCapabilities(it) }
                when {
                    caps == null -> NetworkStatus.OFFLINE
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkStatus.WIFI
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkStatus.CELLULAR
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkStatus.ETHERNET
                    else -> NetworkStatus.OTHER
                }
            } else {
                val info = m.activeNetworkInfo
                when {
                    info == null || !info.isConnected -> NetworkStatus.OFFLINE
                    info.type == ConnectivityManager.TYPE_WIFI -> NetworkStatus.WIFI
                    info.type == ConnectivityManager.TYPE_MOBILE -> NetworkStatus.CELLULAR
                    info.type == ConnectivityManager.TYPE_ETHERNET -> NetworkStatus.ETHERNET
                    else -> NetworkStatus.OTHER
                }
            }
        } catch (_: SecurityException) {
            NetworkStatus.UNKNOWN
        }
        publish(status)
    }

    @Suppress("DEPRECATION")
    override fun onActive() {
        val m = cm
        if (m != null && Build.VERSION.SDK_INT >= 24) m.registerDefaultNetworkCallback(callback)
        else context.registerReceiver(legacyReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        refresh()
    }

    override fun onInactive() {
        val m = cm
        if (m != null && Build.VERSION.SDK_INT >= 24) m.unregisterNetworkCallback(callback)
        else context.unregisterReceiver(legacyReceiver)
    }
}
