package ec.dx3os.core

import ec.dx3os.core.connectivity.BluetoothStatus
import ec.dx3os.core.connectivity.ConnectivityPresenter
import ec.dx3os.core.connectivity.NetworkStatus
import ec.dx3os.core.connectivity.StatusPresentation.Severity
import org.junit.Assert.assertEquals
import org.junit.Test

class ConnectivityPresenterTest {
    @Test fun bluetoothStates() {
        assertEquals(Severity.ERROR, ConnectivityPresenter.bluetooth(BluetoothStatus.Unsupported).severity)
        assertEquals(Severity.WARNING, ConnectivityPresenter.bluetooth(BluetoothStatus.PermissionRequired).severity)
        assertEquals("Bluetooth apagado", ConnectivityPresenter.bluetooth(BluetoothStatus.Off).label)
        assertEquals("Bluetooth encendido", ConnectivityPresenter.bluetooth(BluetoothStatus.On(2, null)).label)
        assertEquals("Dispositivo de audio conectado", ConnectivityPresenter.bluetooth(BluetoothStatus.On(2, true)).label)
        assertEquals("Bluetooth encendido · sin audio conectado", ConnectivityPresenter.bluetooth(BluetoothStatus.On(0, false)).label)
    }

    @Test fun networkStates() {
        assertEquals(Severity.WARNING, ConnectivityPresenter.network(NetworkStatus.OFFLINE).severity)
        assertEquals("Wi-Fi", ConnectivityPresenter.network(NetworkStatus.WIFI).label)
        assertEquals(Severity.INFO, ConnectivityPresenter.network(NetworkStatus.UNKNOWN).severity)
    }
}
