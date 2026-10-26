package ec.dx3os.launcher.screens

import android.view.View
import android.widget.ScrollView
import ec.dx3os.launcher.R
import ec.dx3os.launcher.ui.Host
import ec.dx3os.launcher.ui.Screen
import ec.dx3os.launcher.ui.Ui

/**
 * DX3 OS no incluye un motor de navegación propio: delega en las apps instaladas que
 * aceptan destinos "geo:". Si no hay ninguna, se informa al usuario.
 */
class NavigationScreen(host: Host) : Screen(host) {
    override fun createView(): View {
        val c = ctx
        val apps = services.apps
        val nav = apps.navigationApps()
        if (nav.isEmpty()) {
            return Widgets.message(c, R.drawable.ic_navigation, "No hay apps de navegación",
                "Instale una app de mapas compatible con enlaces geo: para usarla desde aquí.")
        }
        val list = Ui.column(c).apply { setPadding(Ui.dp(c, 24), Ui.dp(c, 8), Ui.dp(c, 24), Ui.dp(c, 16)) }
        list.addView(Ui.text(c, "Elija la app de navegación", 16f, R.color.text_secondary).apply { setPadding(0, 0, 0, Ui.dp(c, 12)) })
        nav.forEach { app ->
            list.addView(Widgets.appRow(c, apps.icon(app.component), app.label) {
                if (!apps.launch(app.component)) host.toast("No se pudo abrir ${app.label}")
            })
        }
        return ScrollView(c).apply { addView(list) }
    }
}
