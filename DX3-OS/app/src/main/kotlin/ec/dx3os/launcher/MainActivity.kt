package ec.dx3os.launcher

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import ec.dx3os.core.clock.ClockText
import ec.dx3os.core.connectivity.BluetoothStatus
import ec.dx3os.core.connectivity.NetworkStatus
import ec.dx3os.core.safety.DrivingPolicy
import ec.dx3os.core.safety.DrivingState
import ec.dx3os.core.vehicle.VehicleSnapshot
import ec.dx3os.launcher.data.AppServices
import ec.dx3os.launcher.screens.AppsScreen
import ec.dx3os.launcher.screens.HomeScreen
import ec.dx3os.launcher.screens.MusicScreen
import ec.dx3os.launcher.screens.NavigationScreen
import ec.dx3os.launcher.screens.PhoneScreen
import ec.dx3os.launcher.screens.SettingsScreen
import ec.dx3os.launcher.ui.Host
import ec.dx3os.launcher.ui.Screen
import ec.dx3os.launcher.ui.Tab
import ec.dx3os.launcher.ui.Ui

class MainActivity : Activity(), Host {
    override val activity: Activity get() = this
    override val services: AppServices get() = (application as Dx3Application).services

    private lateinit var content: FrameLayout
    private lateinit var topTitle: TextView
    private lateinit var topClock: TextView
    private lateinit var topBt: View
    private lateinit var topNet: android.widget.ImageView
    private lateinit var topDriving: TextView
    private val tabViews = HashMap<Tab, LinearLayout>()

    private var current: Tab = Tab.HOME
    private var screen: Screen? = null
    private var started = false

    private val clockObserver: (ClockText) -> Unit = { topClock.text = listOfNotNull(it.time, it.period).joinToString(" ") }
    private val btObserver: (BluetoothStatus) -> Unit = { topBt.alpha = if (it is BluetoothStatus.On) 1f else 0.3f }
    private val netObserver: (NetworkStatus) -> Unit = {
        val online = it != NetworkStatus.OFFLINE && it != NetworkStatus.UNKNOWN
        topNet.setImageResource(if (online) R.drawable.ic_wifi else R.drawable.ic_offline)
        topNet.alpha = if (online) 1f else 0.5f
    }
    private val vehicleObserver: (VehicleSnapshot) -> Unit = {
        topDriving.visibility = if (DrivingPolicy.drivingState(it) == DrivingState.MOVING) View.VISIBLE else View.GONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        current = savedInstanceState?.getString(KEY_TAB)?.let { runCatching { Tab.valueOf(it) }.getOrNull() } ?: Tab.HOME
        val root = buildRoot()
        setContentView(root)
        measureFirstFrame(root)
        show(current, animate = false)
    }

    private fun buildRoot(): View {
        val c = this
        val root = Ui.column(c).apply { setBackgroundColor(Ui.color(c, R.color.bg)) }

        // Barra superior: título de la pantalla + indicadores de estado.
        val top = Ui.row(c).apply {
            setPadding(Ui.dp(c, 24), Ui.dp(c, 8), Ui.dp(c, 24), Ui.dp(c, 4))
            minimumHeight = Ui.dp(c, 48)
        }
        topTitle = Ui.text(c, "", 20f, R.color.text, bold = true, maxLines = 1)
        top.addView(topTitle, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        topDriving = Ui.badge(c, "EN MOVIMIENTO · FUNCIONES RESTRINGIDAS", R.color.warning).apply { visibility = View.GONE }
        top.addView(topDriving)
        top.addView(Ui.spacer(c, 16))
        topBt = Ui.icon(c, R.drawable.ic_bluetooth, R.color.accent, 22)
        top.addView(topBt)
        top.addView(Ui.spacer(c, 12))
        topNet = Ui.icon(c, R.drawable.ic_offline, R.color.text, 22)
        top.addView(topNet)
        top.addView(Ui.spacer(c, 16))
        topClock = Ui.text(c, "", 20f, R.color.text, bold = true, maxLines = 1)
        top.addView(topClock)
        root.addView(top, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        content = FrameLayout(c)
        root.addView(content, Ui.weighted(1f, vertical = true))

        // Barra inferior fija de 6 destinos.
        val bar = Ui.row(c).apply {
            setBackgroundColor(Ui.color(c, R.color.surface))
            setPadding(Ui.dp(c, 8), 0, Ui.dp(c, 8), 0)
        }
        Tab.values().forEach { tab ->
            val item = Ui.column(c).apply {
                gravity = Gravity.CENTER
                isClickable = true
                isFocusable = true
                contentDescription = tab.label
                background = Ui.clickable(c, Ui.rounded(c, 0, 16, null), 16)
                setOnClickListener { navigate(tab) }
                addView(Ui.icon(c, tab.iconRes, R.color.text_secondary, 28))
                addView(Ui.text(c, tab.label, 13f, R.color.text_secondary, maxLines = 1).apply { setPadding(0, Ui.dp(c, 6), 0, 0) })
            }
            tabViews[tab] = item
            bar.addView(item, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f).apply {
                setMargins(Ui.dp(c, 4), Ui.dp(c, 6), Ui.dp(c, 4), Ui.dp(c, 6))
            })
        }
        root.addView(bar, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, resources.getDimensionPixelSize(R.dimen.bottom_bar_height)))
        return root
    }

    private fun measureFirstFrame(root: View) {
        val app = application as Dx3Application
        root.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                root.viewTreeObserver.removeOnPreDrawListener(this)
                services.perf.recordFirstFrame(SystemClock.uptimeMillis() - app.processStartUptimeMs)
                return true
            }
        })
    }

    override fun navigate(tab: Tab) {
        if (tab == current && screen != null) return
        show(tab, animate = true)
    }

    private fun show(tab: Tab, animate: Boolean) {
        if (started) screen?.onHide()
        content.removeAllViews()
        current = tab
        val next = when (tab) {
            Tab.HOME -> HomeScreen(this)
            Tab.NAVIGATION -> NavigationScreen(this)
            Tab.MUSIC -> MusicScreen(this)
            Tab.PHONE -> PhoneScreen(this)
            Tab.APPS -> AppsScreen(this)
            Tab.SETTINGS -> SettingsScreen(this)
        }
        screen = next
        val v = next.createView()
        content.addView(v, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        if (animate) {
            v.alpha = 0f
            v.animate().alpha(1f).setDuration(120).start()
        }
        topTitle.text = if (tab == Tab.HOME) "DX3 OS" else tab.label
        topClock.visibility = if (tab == Tab.HOME) View.INVISIBLE else View.VISIBLE
        tabViews.forEach { (t, item) ->
            val selected = t == tab
            item.isSelected = selected
            item.background = Ui.clickable(this, Ui.rounded(this, if (selected) Ui.color(this, R.color.accent_dim) else 0, 16, null), 16)
            ((item.getChildAt(0)) as android.widget.ImageView).imageTintList =
                android.content.res.ColorStateList.valueOf(Ui.color(this, if (selected) R.color.accent else R.color.text_secondary))
            (item.getChildAt(1) as TextView).setTextColor(Ui.color(this, if (selected) R.color.accent else R.color.text_secondary))
        }
        if (started) next.onShow()
    }

    override fun onStart() {
        super.onStart()
        started = true
        services.clock.observe(clockObserver)
        services.bluetooth.observe(btObserver)
        services.network.observe(netObserver)
        services.vehicle.observe(vehicleObserver)
        screen?.onShow()
    }

    override fun onResume() {
        super.onResume()
        // Al volver de Ajustes del sistema, los permisos pueden haber cambiado.
        services.media.recheck()
        services.bluetooth.refresh()
    }

    override fun onStop() {
        screen?.onHide()
        services.clock.removeObserver(clockObserver)
        services.bluetooth.removeObserver(btObserver)
        services.network.removeObserver(netObserver)
        services.vehicle.removeObserver(vehicleObserver)
        started = false
        super.onStop()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Pulsar "Inicio" con DX3 OS como launcher vuelve a la pantalla principal.
        if (intent.hasCategory(Intent.CATEGORY_HOME)) navigate(Tab.HOME)
    }

    @Deprecated("API del framework; se mantiene por compatibilidad con minSdk 21")
    override fun onBackPressed() {
        if (current != Tab.HOME) navigate(Tab.HOME)
        // En Inicio no se sale: es el launcher.
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_TAB, current.name)
    }

    override fun requestBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= 31) requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_CONNECT), REQ_BT)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_BT) services.bluetooth.refresh()
    }

    override fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    private companion object {
        const val KEY_TAB = "tab"
        const val REQ_BT = 10
    }
}
