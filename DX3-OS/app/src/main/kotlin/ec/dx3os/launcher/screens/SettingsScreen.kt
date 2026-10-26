package ec.dx3os.launcher.screens

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import ec.dx3os.core.diagnostics.CheckStatus
import ec.dx3os.core.diagnostics.HardwareChecklist
import ec.dx3os.core.safety.DrivingPolicy
import ec.dx3os.core.safety.Feature
import ec.dx3os.launcher.R
import ec.dx3os.launcher.data.DeviceProfileReader
import ec.dx3os.launcher.ui.Host
import ec.dx3os.launcher.ui.Screen
import ec.dx3os.launcher.ui.Ui

class SettingsScreen(host: Host) : Screen(host) {
    private lateinit var shortcutsBox: LinearLayout
    private lateinit var perfText: TextView

    override fun createView(): View {
        val c = ctx
        val s = services.settings
        val col = Ui.column(c).apply { setPadding(Ui.dp(c, 24), 0, Ui.dp(c, 24), Ui.dp(c, 24)) }

        col.addView(Ui.sectionTitle(c, "Pantalla"))
        col.addView(toggle("Reloj de 24 horas", null, s.use24Hour) { s.use24Hour = it })

        col.addView(Ui.sectionTitle(c, "Vehículo"))
        col.addView(toggle("Datos del vehículo simulados", "Solo para pruebas de la interfaz. Los valores se marcan como SIMULADO " +
            "y nunca proceden del automóvil.", s.vehicleSimulation) { s.vehicleSimulation = it })

        col.addView(Ui.sectionTitle(c, "Accesos directos de Inicio"))
        shortcutsBox = Ui.column(c)
        col.addView(shortcutsBox)
        renderShortcuts()

        col.addView(Ui.sectionTitle(c, "Sistema"))
        val sys = Ui.row(c)
        sys.addView(Ui.button(c, "Elegir pantalla de inicio") { openSystem(Settings.ACTION_HOME_SETTINGS) },
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { rightMargin = Ui.dp(c, 12) })
        sys.addView(Ui.button(c, "Ajustes de Android") { openSystem(Settings.ACTION_SETTINGS) },
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        col.addView(sys)

        col.addView(Ui.sectionTitle(c, "Diagnóstico de hardware (solo lectura)"))
        col.addView(diagnostics())

        col.addView(Ui.sectionTitle(c, "Rendimiento medido"))
        perfText = Ui.text(c, "", 15f, R.color.text_secondary, maxLines = 6)
        col.addView(perfText)
        col.addView(Ui.spacer(c, 8))
        col.addView(Ui.button(c, "Medir memoria ahora") { renderPerf(sample = true) })
        renderPerf(sample = false)

        col.addView(Ui.sectionTitle(c, "Seguridad"))
        col.addView(Ui.text(c,
            "• DX3 OS no se comunica con la ECU, el bus CAN ni sistemas de seguridad.\n" +
                "• La cámara de reversa y las funciones originales siguen a cargo de la radio de fábrica.\n" +
                "• No se reproduce vídeo. Las funciones distractoras se restringen en movimiento.\n" +
                "• DX3 OS no modifica el firmware: se desinstala como cualquier app.", 15f, R.color.text_secondary, maxLines = 10))

        col.addView(Ui.sectionTitle(c, "Acerca de"))
        val version = try { c.packageManager.getPackageInfo(c.packageName, 0).versionName } catch (_: Exception) { "?" }
        col.addView(Ui.text(c, "DX3 OS $version · prototipo (Etapa 1). Sin conexión a Internet ni telemetría.", 15f, R.color.text_secondary))
        return ScrollView(c).apply { addView(col) }
    }

    private fun toggle(label: String, detail: String?, initial: Boolean, onChange: (Boolean) -> Unit): View {
        val c = ctx
        val row = Ui.row(c).apply {
            minimumHeight = c.resources.getDimensionPixelSize(R.dimen.touch_min)
            setPadding(Ui.dp(c, 16), Ui.dp(c, 8), Ui.dp(c, 16), Ui.dp(c, 8))
            background = Ui.rounded(c, Ui.color(c, R.color.surface), 16)
        }
        val texts = Ui.column(c)
        texts.addView(Ui.text(c, label, 18f))
        if (detail != null) texts.addView(Ui.text(c, detail, 14f, R.color.text_secondary, maxLines = 3).apply { setPadding(0, Ui.dp(c, 4), 0, 0) })
        row.addView(texts, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        val sw = Switch(c).apply {
            isChecked = initial
            contentDescription = label
            setOnCheckedChangeListener { _, v -> onChange(v) }
        }
        row.addView(sw)
        row.setOnClickListener { sw.toggle() }
        return row
    }

    private fun renderShortcuts() {
        val c = ctx
        shortcutsBox.removeAllViews()
        val apps = services.apps
        apps.reload()
        val slots = services.settings.shortcuts.pruneUnavailable(apps::isAvailable)
        slots.slots.forEachIndexed { i, t ->
            val label = t?.let { apps.find(it)?.label } ?: "Vacío"
            shortcutsBox.addView(Ui.button(c, "Acceso ${i + 1}: $label") { pickShortcut(i) }.also {
                it.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    .apply { bottomMargin = Ui.dp(c, 8) }
            })
        }
    }

    private fun pickShortcut(index: Int) {
        val state = DrivingPolicy.drivingState(services.vehicle.value)
        if (!DrivingPolicy.isAllowed(Feature.SHORTCUT_EDITING, state)) {
            host.toast("Disponible con el vehículo detenido")
            return
        }
        val apps = services.apps.value
        val labels = (listOf("— Vaciar —") + apps.map { it.label }).toTypedArray()
        AlertDialog.Builder(ctx, R.style.Theme_Dx3_Dialog)
            .setTitle("Acceso directo ${index + 1}")
            .setItems(labels) { _, which ->
                val s = services.settings
                s.shortcuts = s.shortcuts.with(index, if (which == 0) null else apps[which - 1].target)
                renderShortcuts()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun diagnostics(): View {
        val c = ctx
        val items = HardwareChecklist.build(DeviceProfileReader.read(c))
        val box = Ui.card(c)
        items.forEach { item ->
            val r = Ui.row(c).apply { setPadding(0, Ui.dp(c, 6), 0, Ui.dp(c, 6)) }
            val (tag, color) = when (item.status) {
                CheckStatus.CONFIRMED -> "LEÍDO" to R.color.ok
                CheckStatus.UNKNOWN -> "DESCONOCIDO" to R.color.warning
                CheckStatus.NEEDS_PHYSICAL_TEST -> "PRUEBA FÍSICA" to R.color.simulated
            }
            r.addView(Ui.badge(c, tag, color), LinearLayout.LayoutParams(Ui.dp(c, 140), ViewGroup.LayoutParams.WRAP_CONTENT))
            r.addView(Ui.spacer(c, 12))
            r.addView(Ui.text(c, item.label, 15f, maxLines = 1), LinearLayout.LayoutParams(Ui.dp(c, 220), ViewGroup.LayoutParams.WRAP_CONTENT))
            r.addView(Ui.text(c, item.value ?: item.howToVerify, 14f, R.color.text_secondary, maxLines = 2),
                LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            box.addView(r)
        }
        box.addView(Ui.spacer(c, 8))
        box.addView(Ui.button(c, "Copiar informe al portapapeles") {
            val cm = c.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("DX3 OS hardware", HardwareChecklist.toPlainText(items)))
            host.toast("Informe copiado")
        })
        return box
    }

    private fun renderPerf(sample: Boolean) {
        val perf = services.perf
        val first = perf.firstFrameMs?.let { "$it ms (proceso → primer dibujado, arranque en frío o tibio)" } ?: "sin medir"
        val mem = if (sample) perf.sampleMemory().let {
            "PSS total: ${it.totalPssKb / 1024} MB · heap Java: ${it.javaHeapUsedKb / 1024} MB · heap nativo: ${it.nativeHeapKb / 1024} MB\n" +
                "Límite por app: ${it.memoryClassMb} MB · equipo de baja RAM: ${if (it.lowRamDevice) "sí" else "no"}"
        } else "Memoria: pulse «Medir memoria ahora»"
        perfText.text = "Primer cuadro: $first\n$mem"
    }

    private fun openSystem(action: String) {
        if (!services.apps.start(Intent(action))) host.toast("Este equipo no permite abrir ese ajuste")
    }
}
