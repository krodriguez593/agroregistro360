package ec.dx3os.launcher.data

import android.content.ComponentCallbacks2
import android.content.Context

/** Contenedor de dependencias sin framework de inyección (ligero, explícito). */
class AppServices(context: Context) {
    private val app = context.applicationContext

    val settings by lazy { Settings(app) }
    val clock by lazy { ClockTicker(app, settings) }
    val bluetooth by lazy { BluetoothMonitor(app) }
    val network by lazy { NetworkMonitor(app) }
    val media by lazy { MediaMonitor(app) }
    val apps by lazy { AppCatalog(app) }
    val vehicle by lazy { VehicleRepository(settings) }
    val perf by lazy { PerfStats(app) }

    fun onTrimMemory(level: Int) {
        // Liberar iconos en caché cuando el sistema lo pide. Nunca retener más de lo necesario.
        if (level >= ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) apps.trimIcons()
    }
}
