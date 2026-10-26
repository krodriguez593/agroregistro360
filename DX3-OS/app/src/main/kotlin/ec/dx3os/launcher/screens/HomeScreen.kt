package ec.dx3os.launcher.screens

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import ec.dx3os.core.clock.ClockText
import ec.dx3os.core.connectivity.BluetoothStatus
import ec.dx3os.core.connectivity.ConnectivityPresenter
import ec.dx3os.core.connectivity.NetworkStatus
import ec.dx3os.core.launcher.ShortcutSlots
import ec.dx3os.core.media.MediaUiState
import ec.dx3os.core.projection.AndroidAutoEvaluator
import ec.dx3os.core.projection.AndroidAutoInputs
import ec.dx3os.core.vehicle.DataProvenance
import ec.dx3os.core.vehicle.Gear
import ec.dx3os.core.vehicle.VehicleSnapshot
import ec.dx3os.launcher.R
import ec.dx3os.launcher.ui.Host
import ec.dx3os.launcher.ui.Screen
import ec.dx3os.launcher.ui.Tab
import ec.dx3os.launcher.ui.Ui

class HomeScreen(host: Host) : Screen(host) {
    private lateinit var time: TextView
    private lateinit var period: TextView
    private lateinit var date: TextView
    private lateinit var btLabel: TextView
    private lateinit var netLabel: TextView
    private lateinit var musicTitle: TextView
    private lateinit var musicSubtitle: TextView
    private lateinit var playPause: ImageView
    private lateinit var musicControls: View
    private lateinit var vehicleBody: TextView
    private lateinit var vehicleBadge: TextView
    private lateinit var phoneStatus: TextView
    private lateinit var shortcutsRow: LinearLayout

    private val clockObs: (ClockText) -> Unit = {
        Widgets.setTextIfChanged(time, it.time)
        period.text = it.period ?: ""
        period.visibility = if (it.period == null) View.GONE else View.VISIBLE
        Widgets.setTextIfChanged(date, it.date)
    }
    private val btObs: (BluetoothStatus) -> Unit = {
        val p = ConnectivityPresenter.bluetooth(it)
        btLabel.text = p.label
        btLabel.setTextColor(Ui.severityColor(ctx, p.severity))
        phoneStatus.text = p.label
    }
    private val netObs: (NetworkStatus) -> Unit = {
        val p = ConnectivityPresenter.network(it)
        netLabel.text = p.label
        netLabel.setTextColor(Ui.severityColor(ctx, p.severity))
    }
    private val mediaObs: (MediaUiState) -> Unit = { s ->
        val (t, sub) = Widgets.mediaSummary(s)
        musicTitle.text = t
        musicSubtitle.text = sub
        musicControls.visibility = if (s is MediaUiState.Active) View.VISIBLE else View.GONE
        if (s is MediaUiState.Active) playPause.setImageResource(if (s.nowPlaying.isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
    }
    private val vehicleObs: (VehicleSnapshot) -> Unit = { renderVehicle(it) }
    private val appsObs: (List<ec.dx3os.launcher.data.LaunchableApp>) -> Unit = { renderShortcuts() }
    private val settingsObs: () -> Unit = { renderShortcuts() }

    override fun createView(): View {
        val c = ctx
        val gap = Ui.dp(c, 12)
        val root = Ui.row(c, Gravity.TOP).apply { setPadding(Ui.dp(c, 24), Ui.dp(c, 4), Ui.dp(c, 24), gap) }

        // Columna izquierda: reloj, fecha y estado del sistema.
        val left = Ui.column(c).apply { setPadding(0, 0, Ui.dp(c, 20), 0) }
        val clockRow = Ui.row(c, Gravity.BOTTOM)
        time = Ui.text(c, "--:--", 72f, R.color.text, bold = false, maxLines = 1).apply { typeface = android.graphics.Typeface.create("sans-serif-light", 0) }
        period = Ui.text(c, "", 20f, R.color.text_secondary, maxLines = 1).apply { setPadding(Ui.dp(c, 8), 0, 0, Ui.dp(c, 12)) }
        clockRow.addView(time); clockRow.addView(period)
        left.addView(clockRow)
        date = Ui.text(c, "", 20f, R.color.accent, maxLines = 1)
        left.addView(date)
        left.addView(Ui.spacer(c, 20))
        btLabel = statusLine(left, R.drawable.ic_bluetooth)
        netLabel = statusLine(left, R.drawable.ic_wifi)
        root.addView(left, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 0.34f))

        // Rejilla de tarjetas.
        val grid = Ui.column(c)
        val row1 = Ui.row(c, Gravity.FILL)
        val row2 = Ui.row(c, Gravity.FILL)
        grid.addView(row1, Ui.weighted(1f, true))
        grid.addView(Ui.spacer(c, 12))
        grid.addView(row2, Ui.weighted(1f, true))
        grid.addView(Ui.spacer(c, 12))
        shortcutsRow = Ui.row(c)
        grid.addView(shortcutsRow, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, c.resources.getDimensionPixelSize(R.dimen.touch_min)))
        root.addView(grid, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 0.66f))

        row1.addView(simpleCard(R.drawable.ic_navigation, "Navegación", "Abrir app de mapas") { host.navigate(Tab.NAVIGATION) }, cardLp(1f, gap))
        row1.addView(musicCard(gap), cardLp(2f, 0))

        val phone = Ui.card(c) { host.navigate(Tab.PHONE) }
        phone.addView(cardHeader(R.drawable.ic_phone, "Teléfono"))
        phoneStatus = Ui.text(c, "", 15f, R.color.text_secondary, maxLines = 2).apply { setPadding(0, Ui.dp(c, 8), 0, 0) }
        phone.addView(phoneStatus)
        row2.addView(phone, cardLp(1f, gap))

        val vehicle = Ui.card(c)
        val vh = cardHeader(R.drawable.ic_car, "Vehículo")
        vehicleBadge = Ui.badge(c, "SIMULADO", R.color.simulated).apply { visibility = View.GONE }
        vh.addView(vehicleBadge)
        vehicle.addView(vh)
        vehicleBody = Ui.text(c, "", 15f, R.color.text_secondary, maxLines = 3).apply { setPadding(0, Ui.dp(c, 8), 0, 0) }
        vehicle.addView(vehicleBody)
        row2.addView(vehicle, cardLp(1f, gap))

        val aa = AndroidAutoEvaluator.present(AndroidAutoInputs())
        row2.addView(simpleCard(R.drawable.ic_projection, "Android Auto", aa.title.removePrefix("Android Auto: ").replaceFirstChar { it.uppercaseChar() }) {
            Widgets.showAndroidAutoInfo(c)
        }, cardLp(1f, 0))
        return root
    }

    private fun statusLine(parent: LinearLayout, iconRes: Int): TextView {
        val r = Ui.row(ctx).apply { setPadding(0, Ui.dp(ctx, 6), 0, Ui.dp(ctx, 6)) }
        r.addView(Ui.icon(ctx, iconRes, R.color.text_secondary, 20))
        r.addView(Ui.spacer(ctx, 10))
        val t = Ui.text(ctx, "…", 15f, R.color.text_secondary, maxLines = 2)
        r.addView(t, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        parent.addView(r)
        return t
    }

    private fun cardLp(weight: Float, rightGap: Int) =
        LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, weight).apply { rightMargin = rightGap }

    private fun cardHeader(iconRes: Int, title: String) = Ui.row(ctx).apply {
        addView(Ui.icon(ctx, iconRes, R.color.accent, 26))
        addView(Ui.spacer(ctx, 10))
        addView(Ui.text(ctx, title, 18f, bold = true, maxLines = 1), LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
    }

    private fun simpleCard(iconRes: Int, title: String, subtitle: String, onClick: () -> Unit) = Ui.card(ctx, onClick).apply {
        addView(cardHeader(iconRes, title))
        addView(Ui.text(ctx, subtitle, 15f, R.color.text_secondary).apply { setPadding(0, Ui.dp(ctx, 8), 0, 0) })
    }

    private fun musicCard(gap: Int): View {
        val c = ctx
        val card = Ui.card(c) { host.navigate(Tab.MUSIC) }
        card.addView(cardHeader(R.drawable.ic_music, "Música"))
        val body = Ui.row(c).apply { setPadding(0, Ui.dp(c, 8), 0, 0) }
        val texts = Ui.column(c)
        musicTitle = Ui.text(c, "", 20f, bold = true, maxLines = 1)
        musicSubtitle = Ui.text(c, "", 15f, R.color.text_secondary, maxLines = 1).apply { setPadding(0, Ui.dp(c, 4), 0, 0) }
        texts.addView(musicTitle); texts.addView(musicSubtitle)
        body.addView(texts, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        val controls = Ui.row(c)
        val m = services.media
        controls.addView(Ui.roundIconButton(c, R.drawable.ic_prev, "Anterior", 60) { m.previous() })
        controls.addView(Ui.spacer(c, 8))
        playPause = Ui.roundIconButton(c, R.drawable.ic_play, "Reproducir o pausar", 64, primary = true) { m.playPause() }
        controls.addView(playPause)
        controls.addView(Ui.spacer(c, 8))
        controls.addView(Ui.roundIconButton(c, R.drawable.ic_next, "Siguiente", 60) { m.next() })
        musicControls = controls
        body.addView(controls)
        card.addView(body)
        return card
    }

    private fun renderVehicle(s: VehicleSnapshot) {
        vehicleBadge.visibility = if (s.provenance == DataProvenance.SIMULATED) View.VISIBLE else View.GONE
        vehicleBody.text = when (s.provenance) {
            DataProvenance.UNAVAILABLE -> "Datos no disponibles.\nSin interfaz del vehículo verificada."
            else -> buildString {
                append(s.speedKmh?.let { "$it km/h" } ?: "— km/h")
                append(" · ")
                append(gearLabel(s.gear))
                append("\nCombustible: ")
                append(s.fuelLevelPercent?.let { "$it %" } ?: "—")
            }
        }
    }

    private fun gearLabel(g: Gear?) = when (g) {
        Gear.PARK -> "P"; Gear.REVERSE -> "R"; Gear.NEUTRAL -> "N"; Gear.DRIVE -> "D"; Gear.MANUAL -> "M"; null -> "—"
    }

    private fun renderShortcuts() {
        val c = ctx
        val apps = services.apps
        val slots: ShortcutSlots = services.settings.shortcuts
        shortcutsRow.removeAllViews()
        slots.slots.forEachIndexed { i, target ->
            val app = target?.let { apps.find(it) }
            val tile = Ui.row(c, Gravity.CENTER).apply {
                background = Ui.clickable(c, Ui.rounded(c, Ui.color(c, R.color.surface), 16), 16)
                isClickable = true
                setPadding(Ui.dp(c, 12), 0, Ui.dp(c, 12), 0)
            }
            if (app != null) {
                tile.addView(ImageView(c).apply {
                    setImageDrawable(apps.icon(app.component))
                    layoutParams = LinearLayout.LayoutParams(Ui.dp(c, 36), Ui.dp(c, 36))
                })
                tile.addView(Ui.spacer(c, 10))
                tile.addView(Ui.text(c, app.label, 15f, maxLines = 1), LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
                tile.contentDescription = app.label
                tile.setOnClickListener { if (!apps.launch(app.component)) host.toast("No se pudo abrir ${app.label}") }
            } else {
                tile.addView(Ui.icon(c, R.drawable.ic_plus, R.color.text_secondary, 22))
                tile.addView(Ui.spacer(c, 8))
                tile.addView(Ui.text(c, "Acceso directo", 14f, R.color.text_secondary, maxLines = 1))
                tile.contentDescription = "Configurar acceso directo ${i + 1}"
                tile.setOnClickListener { host.navigate(Tab.SETTINGS) }
            }
            shortcutsRow.addView(tile, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f).apply {
                if (i < slots.slots.lastIndex) rightMargin = Ui.dp(c, 12)
            })
        }
    }

    override fun onShow() {
        services.clock.observe(clockObs)
        services.bluetooth.observe(btObs)
        services.network.observe(netObs)
        services.media.observe(mediaObs)
        services.vehicle.observe(vehicleObs)
        services.apps.observe(appsObs)
        services.settings.addListener(settingsObs)
    }

    override fun onHide() {
        services.clock.removeObserver(clockObs)
        services.bluetooth.removeObserver(btObs)
        services.network.removeObserver(netObs)
        services.media.removeObserver(mediaObs)
        services.vehicle.removeObserver(vehicleObs)
        services.apps.removeObserver(appsObs)
        services.settings.removeListener(settingsObs)
    }
}
