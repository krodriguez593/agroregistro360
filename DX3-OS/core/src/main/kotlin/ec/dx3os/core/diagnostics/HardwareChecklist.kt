package ec.dx3os.core.diagnostics

/**
 * Datos de identificación que la app puede leer con APIs públicas (sin root ni permisos especiales).
 * null = la API no lo expone en esta versión de Android o no se pudo leer.
 */
data class DeviceProfile(
    val manufacturer: String?,
    val model: String?,
    val device: String?,
    val board: String?,
    val hardware: String?,
    val socManufacturer: String?,
    val socModel: String?,
    val supportedAbis: List<String>,
    val androidRelease: String?,
    val sdkInt: Int?,
    val securityPatch: String?,
    val buildFingerprint: String?,
    val totalRamBytes: Long?,
    val screenWidthPx: Int?,
    val screenHeightPx: Int?,
    val densityDpi: Int?,
    val touchscreen: TouchType?,
    val hasBluetooth: Boolean?,
    val hasBluetoothLe: Boolean?,
    val hasWifi: Boolean?,
    val hasUsbHost: Boolean?,
    val hasCamera: Boolean?,
    val isAutomotiveOs: Boolean?,
)

enum class TouchType { NONE, BASIC, MULTITOUCH, MULTITOUCH_DISTINCT, MULTITOUCH_JAZZHAND }

enum class CheckStatus { CONFIRMED, UNKNOWN, NEEDS_PHYSICAL_TEST }

data class ChecklistItem(val key: String, val label: String, val value: String?, val status: CheckStatus, val howToVerify: String)

/**
 * Convierte un [DeviceProfile] en la lista del informe de hardware.
 * Algunos puntos (cámara de reversa, mandos del volante, bootloader) NUNCA se pueden
 * confirmar desde una app: quedan como NEEDS_PHYSICAL_TEST.
 */
object HardwareChecklist {

    fun build(p: DeviceProfile): List<ChecklistItem> {
        fun item(key: String, label: String, value: String?, how: String) =
            ChecklistItem(key, label, value, if (value.isNullOrBlank()) CheckStatus.UNKNOWN else CheckStatus.CONFIRMED, how)

        val ram = p.totalRamBytes?.let { "%.2f GB".format(java.util.Locale.ROOT, it / 1_073_741_824.0) }
        val screen = if (p.screenWidthPx != null && p.screenHeightPx != null) {
            val w = maxOf(p.screenWidthPx, p.screenHeightPx)
            val h = minOf(p.screenWidthPx, p.screenHeightPx)
            "${w}x$h px" + (p.densityDpi?.let { " @ $it dpi" } ?: "")
        } else null

        return listOf(
            item("manufacturer", "Fabricante (Build)", joinNonBlank(p.manufacturer, p.model), "adb shell getprop ro.product.manufacturer / ro.product.model"),
            item("device", "Dispositivo / placa", joinNonBlank(p.device, p.board, p.hardware), "adb shell getprop ro.product.board / ro.hardware"),
            item("soc", "Procesador (SoC)", joinNonBlank(p.socManufacturer, p.socModel), "adb shell cat /proc/cpuinfo; getprop ro.soc.model"),
            item("abi", "Arquitectura", p.supportedAbis.takeIf { it.isNotEmpty() }?.joinToString(), "adb shell getprop ro.product.cpu.abilist"),
            item("android", "Versión de Android", p.androidRelease?.let { "Android $it (API ${p.sdkInt ?: "?"})" }, "adb shell getprop ro.build.version.release"),
            item("patch", "Parche de seguridad", p.securityPatch, "adb shell getprop ro.build.version.security_patch"),
            item("fingerprint", "Build fingerprint", p.buildFingerprint, "adb shell getprop ro.build.fingerprint"),
            item("ram", "Memoria RAM total", ram, "adb shell cat /proc/meminfo"),
            item("screen", "Resolución de pantalla", screen, "adb shell wm size; wm density"),
            item("touch", "Pantalla táctil", p.touchscreen?.name, "adb shell getevent -p (solo listado)"),
            item("bt", "Bluetooth", p.hasBluetooth?.let { yesNo(it) + (p.hasBluetoothLe?.let { le -> " · BLE ${yesNo(le)}" } ?: "") }, "pm list features | grep bluetooth"),
            item("wifi", "Wi-Fi", p.hasWifi?.let(::yesNo), "pm list features | grep wifi"),
            item("usbhost", "USB host", p.hasUsbHost?.let(::yesNo), "pm list features | grep usb.host"),
            item("aaos", "Android Automotive OS", p.isAutomotiveOs?.let(::yesNo), "pm list features | grep automotive"),
            ChecklistItem("storage", "Almacenamiento", null, CheckStatus.UNKNOWN, "adb shell df -h /data"),
            ChecklistItem("bootloader", "Bootloader y método de actualización", null, CheckStatus.NEEDS_PHYSICAL_TEST,
                "getprop ro.boot.* / documentación del fabricante. NO desbloquear."),
            ChecklistItem("reverse_cam", "Cámara de reversa", null, CheckStatus.NEEDS_PHYSICAL_TEST,
                "Engranar reversa con el vehículo detenido y freno de mano; observar si la radio original conmuta sola."),
            ChecklistItem("swc", "Mandos del volante", null, CheckStatus.NEEDS_PHYSICAL_TEST,
                "Probar cada botón y registrar eventos con 'adb shell getevent -l' durante la prueba (solo lectura)."),
            ChecklistItem("aa", "Android Auto / CarPlay de fábrica", null, CheckStatus.NEEDS_PHYSICAL_TEST,
                "Conectar un teléfono por USB con cable de datos y revisar menú de la radio y manual del vehículo."),
        )
    }

    fun summary(items: List<ChecklistItem>): Map<CheckStatus, Int> =
        CheckStatus.values().associateWith { s -> items.count { it.status == s } }

    /** Texto plano para copiar al informe. No incluye número de serie ni identificadores personales. */
    fun toPlainText(items: List<ChecklistItem>): String = buildString {
        appendLine("DX3 OS — perfil de hardware leído desde la app")
        items.forEach { appendLine("[${it.status}] ${it.label}: ${it.value ?: "—"}") }
    }

    private fun yesNo(b: Boolean) = if (b) "sí" else "no"
    private fun joinNonBlank(vararg v: String?) = v.filterNot { it.isNullOrBlank() }.joinToString(" · ").ifEmpty { null }
}
