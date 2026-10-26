package ec.dx3os.launcher

import android.app.Application
import android.os.SystemClock
import ec.dx3os.launcher.data.AppServices

class Dx3Application : Application() {
    /** Momento de creación del proceso de la app (para medir el arranque real, ver PerfStats). */
    val processStartUptimeMs: Long = SystemClock.uptimeMillis()

    lateinit var services: AppServices
        private set

    override fun onCreate() {
        super.onCreate()
        services = AppServices(this)
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        services.onTrimMemory(level)
    }
}
