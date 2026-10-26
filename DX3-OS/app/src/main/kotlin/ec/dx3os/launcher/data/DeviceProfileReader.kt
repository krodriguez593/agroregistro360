package ec.dx3os.launcher.data

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import ec.dx3os.core.diagnostics.DeviceProfile
import ec.dx3os.core.diagnostics.TouchType

/**
 * Lee el perfil del equipo con APIs públicas de solo lectura. No lee número de serie,
 * IMEI, MAC ni cuentas: no son necesarios para identificar el hardware.
 */
object DeviceProfileReader {

    fun read(context: Context): DeviceProfile {
        val pm = context.packageManager
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mem = ActivityManager.MemoryInfo().also { am.getMemoryInfo(it) }
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getRealMetrics(metrics)

        fun has(f: String) = pm.hasSystemFeature(f)
        val touch = when {
            has(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH_JAZZHAND) -> TouchType.MULTITOUCH_JAZZHAND
            has(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH_DISTINCT) -> TouchType.MULTITOUCH_DISTINCT
            has(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH) -> TouchType.MULTITOUCH
            has(PackageManager.FEATURE_TOUCHSCREEN) -> TouchType.BASIC
            else -> TouchType.NONE
        }

        return DeviceProfile(
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            device = Build.DEVICE,
            board = Build.BOARD,
            hardware = Build.HARDWARE,
            socManufacturer = if (Build.VERSION.SDK_INT >= 31) Build.SOC_MANUFACTURER else null,
            socModel = if (Build.VERSION.SDK_INT >= 31) Build.SOC_MODEL else null,
            supportedAbis = Build.SUPPORTED_ABIS.toList(),
            androidRelease = Build.VERSION.RELEASE,
            sdkInt = Build.VERSION.SDK_INT,
            securityPatch = if (Build.VERSION.SDK_INT >= 23) Build.VERSION.SECURITY_PATCH else null,
            buildFingerprint = Build.FINGERPRINT,
            totalRamBytes = mem.totalMem.takeIf { it > 0 },
            screenWidthPx = metrics.widthPixels,
            screenHeightPx = metrics.heightPixels,
            densityDpi = metrics.densityDpi,
            touchscreen = touch,
            hasBluetooth = has(PackageManager.FEATURE_BLUETOOTH),
            hasBluetoothLe = has(PackageManager.FEATURE_BLUETOOTH_LE),
            hasWifi = has(PackageManager.FEATURE_WIFI),
            hasUsbHost = has(PackageManager.FEATURE_USB_HOST),
            hasCamera = has(PackageManager.FEATURE_CAMERA_ANY),
            isAutomotiveOs = if (Build.VERSION.SDK_INT >= 23) has(PackageManager.FEATURE_AUTOMOTIVE) else false,
        )
    }
}
