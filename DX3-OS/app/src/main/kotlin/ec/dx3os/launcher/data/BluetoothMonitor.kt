package ec.dx3os.launcher.data

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import ec.dx3os.core.connectivity.BluetoothStatus

/**
 * Estado Bluetooth mediante la API pública. Solo LEE estado: no empareja, no activa ni
 * desactiva la radio (en Android 13+ las apps ya no pueden hacerlo, y en la radio original el
 * Bluetooth puede estar gestionado por un módulo propio del fabricante; ver HARDWARE_REPORT.md).
 */
class BluetoothMonitor(private val context: Context) : Observable<BluetoothStatus>(BluetoothStatus.Off) {

    private val adapter: BluetoothAdapter? =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context, i: Intent) = refresh()
    }

    fun hasPermission(): Boolean = Build.VERSION.SDK_INT < 31 ||
        context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED

    fun refresh() {
        val a = adapter
        val status = when {
            a == null -> BluetoothStatus.Unsupported
            !hasPermission() -> BluetoothStatus.PermissionRequired
            else -> try {
                when (a.state) {
                    BluetoothAdapter.STATE_ON -> BluetoothStatus.On(
                        pairedDeviceCount = a.bondedDevices?.size,
                        audioProfileConnected = audioConnected(a),
                    )
                    BluetoothAdapter.STATE_TURNING_ON -> BluetoothStatus.TurningOn
                    BluetoothAdapter.STATE_TURNING_OFF -> BluetoothStatus.TurningOff
                    else -> BluetoothStatus.Off
                }
            } catch (_: SecurityException) {
                BluetoothStatus.PermissionRequired
            }
        }
        publish(status)
    }

    @Suppress("DEPRECATION") // HEADSET/A2DP siguen siendo la vía pública para consultar el estado.
    private fun audioConnected(a: BluetoothAdapter): Boolean? = try {
        a.getProfileConnectionState(BluetoothProfile.A2DP) == BluetoothProfile.STATE_CONNECTED ||
            a.getProfileConnectionState(BluetoothProfile.HEADSET) == BluetoothProfile.STATE_CONNECTED
    } catch (_: SecurityException) {
        null
    }

    override fun onActive() {
        if (adapter != null) {
            context.registerReceiver(receiver, IntentFilter().apply {
                addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
                addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
            })
        }
        refresh()
    }

    override fun onInactive() {
        if (adapter != null) context.unregisterReceiver(receiver)
    }
}
