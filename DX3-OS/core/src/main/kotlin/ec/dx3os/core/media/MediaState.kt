package ec.dx3os.core.media

data class NowPlaying(
    val title: String?,
    val artist: String?,
    val sourceAppLabel: String?,
    val isPlaying: Boolean,
    val durationMs: Long?,
    val positionMs: Long?,
    val canSkipNext: Boolean,
    val canSkipPrevious: Boolean,
)

/** Estado de la tarjeta de música. Se alimenta de MediaSessionManager (API pública). */
sealed class MediaUiState {
    /** El usuario aún no concedió acceso a notificaciones (requisito de getActiveSessions). */
    object PermissionRequired : MediaUiState()
    object NoActiveSession : MediaUiState()
    data class Active(val nowPlaying: NowPlaying) : MediaUiState()
    data class Error(val message: String) : MediaUiState()
}

object MediaFormat {
    /** 65_000 -> "1:05"; 3_725_000 -> "1:02:05". Valores nulos o negativos -> "--:--". */
    fun duration(ms: Long?): String {
        if (ms == null || ms < 0) return "--:--"
        val totalSec = ms / 1000
        val h = totalSec / 3600
        val m = (totalSec % 3600) / 60
        val s = totalSec % 60
        val ss = s.toString().padStart(2, '0')
        return if (h > 0) "$h:${m.toString().padStart(2, '0')}:$ss" else "$m:$ss"
    }

    fun titleOrFallback(np: NowPlaying): String = np.title?.takeIf { it.isNotBlank() } ?: "Sin título"

    fun subtitle(np: NowPlaying): String = listOfNotNull(
        np.artist?.takeIf { it.isNotBlank() },
        np.sourceAppLabel?.takeIf { it.isNotBlank() },
    ).joinToString(" · ").ifEmpty { "Fuente desconocida" }

    /** Progreso 0..1000 para una barra; null si la fuente no informa duración. */
    fun progressPermille(np: NowPlaying): Int? {
        val d = np.durationMs ?: return null
        val p = np.positionMs ?: return null
        if (d <= 0) return null
        return ((p.coerceIn(0, d) * 1000) / d).toInt()
    }
}
