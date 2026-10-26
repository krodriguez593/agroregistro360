package ec.dx3os.launcher.screens

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.FrameLayout
import android.widget.GridView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import ec.dx3os.core.safety.DrivingPolicy
import ec.dx3os.core.safety.Feature
import ec.dx3os.core.vehicle.VehicleSnapshot
import ec.dx3os.launcher.R
import ec.dx3os.launcher.data.LaunchableApp
import ec.dx3os.launcher.ui.Host
import ec.dx3os.launcher.ui.Screen
import ec.dx3os.launcher.ui.Ui

/** Rejilla de apps instaladas con reciclado de vistas (GridView) para bajo uso de memoria. */
class AppsScreen(host: Host) : Screen(host) {
    private lateinit var container: FrameLayout
    private val adapter = Adapter()
    private var blocked: Boolean? = null

    private val appsObs: (List<LaunchableApp>) -> Unit = { adapter.items = it; adapter.notifyDataSetChanged(); updateEmpty() }
    private val vehicleObs: (VehicleSnapshot) -> Unit = {
        val b = !DrivingPolicy.isAllowed(Feature.APP_GRID, DrivingPolicy.drivingState(it))
        if (b != blocked) { blocked = b; rebuild() }
    }
    private var emptyView: View? = null

    override fun createView(): View {
        container = FrameLayout(ctx)
        return container
    }

    private fun rebuild() {
        val c = ctx
        container.removeAllViews()
        if (blocked == true) {
            container.addView(Widgets.message(c, R.drawable.ic_warning, "No disponible en movimiento",
                "La lista de aplicaciones se habilita con el vehículo detenido. Use Inicio, Música o Navegación."))
            return
        }
        val grid = GridView(c).apply {
            numColumns = GridView.AUTO_FIT
            columnWidth = Ui.dp(c, 140)
            stretchMode = GridView.STRETCH_COLUMN_WIDTH
            verticalSpacing = Ui.dp(c, 12)
            horizontalSpacing = Ui.dp(c, 12)
            setPadding(Ui.dp(c, 24), Ui.dp(c, 8), Ui.dp(c, 24), Ui.dp(c, 16))
            clipToPadding = false
            selector = android.graphics.drawable.ColorDrawable(0)
            adapter = this@AppsScreen.adapter
            setOnItemClickListener { _, _, pos, _ ->
                val app = this@AppsScreen.adapter.items[pos]
                if (!services.apps.launch(app.component)) host.toast("No se pudo abrir ${app.label}")
            }
        }
        container.addView(grid)
        emptyView = Widgets.message(c, R.drawable.ic_apps, "Sin aplicaciones", "No se encontraron aplicaciones instaladas.")
        container.addView(emptyView)
        updateEmpty()
    }

    private fun updateEmpty() { emptyView?.visibility = if (adapter.items.isEmpty()) View.VISIBLE else View.GONE }

    override fun onShow() {
        services.vehicle.observe(vehicleObs)
        services.apps.observe(appsObs)
    }

    override fun onHide() {
        services.vehicle.removeObserver(vehicleObs)
        services.apps.removeObserver(appsObs)
    }

    private inner class Adapter : BaseAdapter() {
        var items: List<LaunchableApp> = emptyList()
        override fun getCount() = items.size
        override fun getItem(position: Int) = items[position]
        override fun getItemId(position: Int) = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val c = ctx
            val v = (convertView as? LinearLayout) ?: Ui.column(c).apply {
                gravity = Gravity.CENTER
                minimumHeight = Ui.dp(c, 120)
                setPadding(Ui.dp(c, 8), Ui.dp(c, 12), Ui.dp(c, 8), Ui.dp(c, 12))
                background = Ui.clickable(c, Ui.rounded(c, Ui.color(c, R.color.surface), 16), 16)
                addView(ImageView(c).apply { layoutParams = LinearLayout.LayoutParams(Ui.dp(c, 56), Ui.dp(c, 56)) })
                addView(Ui.text(c, "", 15f, maxLines = 2).apply { gravity = Gravity.CENTER; setPadding(0, Ui.dp(c, 8), 0, 0) })
            }
            val app = items[position]
            (v.getChildAt(0) as ImageView).setImageDrawable(services.apps.icon(app.component))
            (v.getChildAt(1) as TextView).text = app.label
            v.contentDescription = app.label
            return v
        }
    }
}
