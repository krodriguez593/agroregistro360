package ec.dx3os.launcher.ui

import android.app.Activity
import android.view.View
import ec.dx3os.launcher.data.AppServices

/** Contrato entre las pantallas y la actividad anfitriona. */
interface Host {
    val activity: Activity
    val services: AppServices
    fun navigate(tab: Tab)
    fun requestBluetoothPermission()
    fun toast(message: String)
}

enum class Tab(val label: String, val iconRes: Int) {
    HOME("Inicio", ec.dx3os.launcher.R.drawable.ic_home),
    NAVIGATION("Navegación", ec.dx3os.launcher.R.drawable.ic_navigation),
    MUSIC("Música", ec.dx3os.launcher.R.drawable.ic_music),
    PHONE("Teléfono", ec.dx3os.launcher.R.drawable.ic_phone),
    APPS("Aplicaciones", ec.dx3os.launcher.R.drawable.ic_apps),
    SETTINGS("Ajustes", ec.dx3os.launcher.R.drawable.ic_settings),
}

/**
 * Pantalla ligera (sin Fragments). Solo existe la pantalla visible: al cambiar de pestaña
 * la anterior se destruye y libera sus observadores en [onHide].
 */
abstract class Screen(protected val host: Host) {
    protected val ctx get() = host.activity
    protected val services get() = host.services

    abstract fun createView(): View

    /** Pantalla visible y actividad en primer plano: registrar observadores aquí. */
    open fun onShow() {}

    /** Pantalla oculta o actividad en segundo plano: liberar observadores aquí. */
    open fun onHide() {}
}
