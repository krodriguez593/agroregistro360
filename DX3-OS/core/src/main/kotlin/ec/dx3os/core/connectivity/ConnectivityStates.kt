package ec.dx3os.core.connectivity

/** Estado Bluetooth tal como lo expone la API pública de Android (BluetoothAdapter). */
sealed class BluetoothStatus {
    /** El dispositivo no tiene adaptador Bluetooth accesible para apps. */
    object Unsupported : BluetoothStatus()

    /** Android 12+ requiere BLUETOOTH_CONNECT en tiempo de ejecución. */
    object PermissionRequired : BluetoothStatus()
    object Off : BluetoothStatus()
    object TurningOn : BluetoothStatus()
    object TurningOff : BluetoothStatus()

    /**
     * Encendido. [audioProfileConnected] indica si algún perfil de audio (A2DP / manos libres)
     * informa conexión; la API pública no da un recuento exacto. null = no se pudo consultar.
     * Nunca se rellena con valores supuestos.
     */
    data class On(val pairedDeviceCount: Int?, val audioProfileConnected: Boolean?) : BluetoothStatus()
}

enum class NetworkStatus { UNKNOWN, OFFLINE, WIFI, CELLULAR, ETHERNET, OTHER }

data class StatusPresentation(val label: String, val severity: Severity) {
    enum class Severity { OK, INFO, WARNING, ERROR }
}

object ConnectivityPresenter {
    fun bluetooth(status: BluetoothStatus): StatusPresentation = when (status) {
        BluetoothStatus.Unsupported -> StatusPresentation("Bluetooth no disponible en este equipo", StatusPresentation.Severity.ERROR)
        BluetoothStatus.PermissionRequired -> StatusPresentation("Permiso de Bluetooth requerido", StatusPresentation.Severity.WARNING)
        BluetoothStatus.Off -> StatusPresentation("Bluetooth apagado", StatusPresentation.Severity.INFO)
        BluetoothStatus.TurningOn -> StatusPresentation("Encendiendo Bluetooth…", StatusPresentation.Severity.INFO)
        BluetoothStatus.TurningOff -> StatusPresentation("Apagando Bluetooth…", StatusPresentation.Severity.INFO)
        is BluetoothStatus.On -> when (status.audioProfileConnected) {
            null -> StatusPresentation("Bluetooth encendido", StatusPresentation.Severity.OK)
            false -> StatusPresentation("Bluetooth encendido · sin audio conectado", StatusPresentation.Severity.OK)
            true -> StatusPresentation("Dispositivo de audio conectado", StatusPresentation.Severity.OK)
        }
    }

    fun network(status: NetworkStatus): StatusPresentation = when (status) {
        NetworkStatus.UNKNOWN -> StatusPresentation("Red: estado desconocido", StatusPresentation.Severity.INFO)
        NetworkStatus.OFFLINE -> StatusPresentation("Sin conexión", StatusPresentation.Severity.WARNING)
        NetworkStatus.WIFI -> StatusPresentation("Wi-Fi", StatusPresentation.Severity.OK)
        NetworkStatus.CELLULAR -> StatusPresentation("Datos móviles", StatusPresentation.Severity.OK)
        NetworkStatus.ETHERNET -> StatusPresentation("Ethernet", StatusPresentation.Severity.OK)
        NetworkStatus.OTHER -> StatusPresentation("Conectado", StatusPresentation.Severity.OK)
    }
}
