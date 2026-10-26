package ec.dx3os.core.clock

/**
 * Instante local ya descompuesto. La capa Android lo construye a partir de
 * `java.util.Calendar` (minSdk 21 no dispone de java.time sin desugaring).
 *
 * @property month 1..12
 * @property dayOfWeek 1 = lunes .. 7 = domingo (ISO-8601)
 */
data class LocalDateTimeParts(
    val year: Int,
    val month: Int,
    val dayOfMonth: Int,
    val dayOfWeek: Int,
    val hour: Int,
    val minute: Int,
) {
    init {
        require(month in 1..12) { "month fuera de rango: $month" }
        require(dayOfMonth in 1..31) { "dayOfMonth fuera de rango: $dayOfMonth" }
        require(dayOfWeek in 1..7) { "dayOfWeek fuera de rango: $dayOfWeek" }
        require(hour in 0..23) { "hour fuera de rango: $hour" }
        require(minute in 0..59) { "minute fuera de rango: $minute" }
    }
}

data class ClockText(val time: String, val period: String?, val date: String)

/** Formato de reloj y fecha en español, determinista y sin dependencias de Locale del sistema. */
object ClockFormatter {
    private val DAYS = listOf("lunes", "martes", "miércoles", "jueves", "viernes", "sábado", "domingo")
    private val MONTHS = listOf(
        "enero", "febrero", "marzo", "abril", "mayo", "junio",
        "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre",
    )

    fun format(parts: LocalDateTimeParts, use24Hour: Boolean): ClockText {
        val minute = parts.minute.toString().padStart(2, '0')
        val time: String
        val period: String?
        if (use24Hour) {
            time = "${parts.hour.toString().padStart(2, '0')}:$minute"
            period = null
        } else {
            val h12 = when (val h = parts.hour % 12) { 0 -> 12; else -> h }
            time = "$h12:$minute"
            period = if (parts.hour < 12) "a. m." else "p. m."
        }
        val day = DAYS[parts.dayOfWeek - 1].replaceFirstChar { it.uppercaseChar() }
        val date = "$day, ${parts.dayOfMonth} de ${MONTHS[parts.month - 1]}"
        return ClockText(time, period, date)
    }

    /** Milisegundos hasta el siguiente cambio de minuto, para programar un único tick (sin bucles por segundo). */
    fun millisUntilNextMinute(nowMillis: Long): Long {
        val remainder = nowMillis % 60_000L
        return 60_000L - remainder
    }
}
