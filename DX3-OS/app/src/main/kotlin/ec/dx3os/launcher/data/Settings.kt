package ec.dx3os.launcher.data

import android.content.Context
import android.text.format.DateFormat
import ec.dx3os.core.launcher.ShortcutSlots

/** Preferencias locales. No se guardan datos personales (ni contactos, ni historial). */
class Settings(private val context: Context) {
    private val prefs = context.getSharedPreferences("dx3_settings", Context.MODE_PRIVATE)
    private val listeners = LinkedHashSet<() -> Unit>()

    var use24Hour: Boolean
        get() = if (prefs.contains(KEY_24H)) prefs.getBoolean(KEY_24H, true) else DateFormat.is24HourFormat(context)
        set(v) = edit { putBoolean(KEY_24H, v) }

    /** Modo simulación de datos del vehículo. Desactivado por defecto. */
    var vehicleSimulation: Boolean
        get() = prefs.getBoolean(KEY_SIM, false)
        set(v) = edit { putBoolean(KEY_SIM, v) }

    var shortcuts: ShortcutSlots
        get() = ShortcutSlots.decode(prefs.getString(KEY_SHORTCUTS, null))
        set(v) = edit { putString(KEY_SHORTCUTS, v.encode()) }

    fun addListener(l: () -> Unit) { listeners += l }
    fun removeListener(l: () -> Unit) { listeners -= l }

    private inline fun edit(block: android.content.SharedPreferences.Editor.() -> Unit) {
        prefs.edit().apply(block).apply()
        listeners.toList().forEach { it() }
    }

    private companion object {
        const val KEY_24H = "use_24h"
        const val KEY_SIM = "vehicle_simulation"
        const val KEY_SHORTCUTS = "shortcuts"
    }
}
