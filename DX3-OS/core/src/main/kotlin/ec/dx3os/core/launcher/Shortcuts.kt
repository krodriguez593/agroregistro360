package ec.dx3os.core.launcher

/** Acceso directo configurable: referencia a una actividad lanzable (paquete + clase). */
data class ShortcutTarget(val packageName: String, val activityName: String) {
    init {
        require(packageName.isNotBlank() && activityName.isNotBlank())
        require(SEPARATORS.none { it in packageName || it in activityName }) { "Nombre con separador reservado" }
    }

    companion object {
        internal val SEPARATORS = listOf('|', '/')
    }
}

/**
 * Lista fija de [slotCount] ranuras. Se serializa como "pkg/clase|pkg/clase||" (vacío = ranura libre)
 * para guardarse en SharedPreferences sin dependencias de JSON.
 */
data class ShortcutSlots(val slots: List<ShortcutTarget?>) {

    fun with(index: Int, target: ShortcutTarget?): ShortcutSlots {
        require(index in slots.indices) { "Ranura inexistente: $index" }
        return ShortcutSlots(slots.toMutableList().also { it[index] = target })
    }

    fun encode(): String = slots.joinToString("|") { it?.let { t -> "${t.packageName}/${t.activityName}" } ?: "" }

    /** Elimina accesos a apps desinstaladas, conservando la posición de las demás. */
    fun pruneUnavailable(isAvailable: (ShortcutTarget) -> Boolean): ShortcutSlots =
        ShortcutSlots(slots.map { t -> t?.takeIf(isAvailable) })

    companion object {
        const val DEFAULT_SLOT_COUNT = 4

        fun empty(slotCount: Int = DEFAULT_SLOT_COUNT) = ShortcutSlots(List(slotCount) { null })

        /** Decodifica de forma tolerante: entradas corruptas se convierten en ranuras vacías. */
        fun decode(raw: String?, slotCount: Int = DEFAULT_SLOT_COUNT): ShortcutSlots {
            if (raw.isNullOrEmpty()) return empty(slotCount)
            val parsed = raw.split('|').map { entry ->
                val parts = entry.split('/')
                if (parts.size == 2) runCatching { ShortcutTarget(parts[0], parts[1]) }.getOrNull() else null
            }
            return ShortcutSlots(List(slotCount) { i -> parsed.getOrNull(i) })
        }
    }
}
