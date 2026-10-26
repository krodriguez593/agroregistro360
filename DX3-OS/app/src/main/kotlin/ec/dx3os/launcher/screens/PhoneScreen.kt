package ec.dx3os.launcher.screens

import android.content.Intent
import android.provider.Settings
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import ec.dx3os.core.connectivity.BluetoothStatus
import ec.dx3os.core.connectivity.ConnectivityPresenter
import ec.dx3os.launcher.R
import ec.dx3os.launcher.ui.Host
import ec.dx3os.launcher.ui.Screen
import ec.dx3os.launcher.ui.Ui

/**
 * Teléfono: estado Bluetooth + accesos al marcador y a los ajustes Bluetooth del sistema.
 * DX3 OS no lee contactos ni registro de llamadas. En radios con módulo Bluetooth propio del
 * fabricante, las llamadas manos libres las sigue gestionando la app original.
 */
class PhoneScreen(host: Host) : Screen(host) {
    private lateinit var status: TextView
    private lateinit var permissionButton: View

    private val obs: (BluetoothStatus) -> Unit = {
        val p = ConnectivityPresenter.bluetooth(it)
        status.text = p.label
        status.setTextColor(Ui.severityColor(ctx, p.severity))
        permissionButton.visibility = if (it == BluetoothStatus.PermissionRequired) View.VISIBLE else View.GONE
    }

    override fun createView(): View {
        val c = ctx
        val col = Ui.column(c).apply { setPadding(Ui.dp(c, 24), Ui.dp(c, 8), Ui.dp(c, 24), Ui.dp(c, 16)) }
        val card = Ui.card(c)
        val head = Ui.row(c)
        head.addView(Ui.icon(c, R.drawable.ic_bluetooth, R.color.accent, 32))
        head.addView(Ui.spacer(c, 12))
        status = Ui.text(c, "…", 22f, bold = true)
        head.addView(status)
        card.addView(head)
        card.addView(Ui.text(c,
            "Las llamadas manos libres dependen del Bluetooth de la radio. Si su radio usa una app propia del fabricante " +
                "para el teléfono, ábrala desde Aplicaciones.", 15f, R.color.text_secondary, maxLines = 4).apply { setPadding(0, Ui.dp(c, 10), 0, 0) })
        col.addView(card)
        col.addView(Ui.spacer(c, 16))

        val actions = Ui.row(c)
        actions.addView(Ui.button(c, "Abrir marcador", R.drawable.ic_phone, primary = true) {
            if (!services.apps.openDialer()) host.toast("No hay app de teléfono en este equipo")
        }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { rightMargin = Ui.dp(c, 12) })
        actions.addView(Ui.button(c, "Ajustes Bluetooth", R.drawable.ic_bluetooth) {
            if (!services.apps.start(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))) host.toast("Este equipo no permite abrir ese ajuste")
        }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        col.addView(actions)
        col.addView(Ui.spacer(c, 12))
        permissionButton = Ui.button(c, "Conceder permiso de Bluetooth") { host.requestBluetoothPermission() }.apply { visibility = View.GONE }
        col.addView(permissionButton)
        return ScrollView(c).apply { addView(col) }
    }

    override fun onShow() = services.bluetooth.observe(obs)
    override fun onHide() = services.bluetooth.removeObserver(obs)
}
