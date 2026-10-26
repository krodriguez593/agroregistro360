package ec.dx3os.launcher.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.SystemClock
import android.provider.Settings as AndroidSettings
import ec.dx3os.core.media.MediaUiState
import ec.dx3os.core.media.NowPlaying

/**
 * Sesión multimedia activa del sistema (cualquier reproductor que publique MediaSession).
 * Requiere que el usuario habilite el acceso a notificaciones para DX3 OS.
 */
class MediaMonitor(private val context: Context) : Observable<MediaUiState>(MediaUiState.NoActiveSession) {

    private val msm = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager?
    private val listenerComponent = ComponentName(context, MediaAccessListenerService::class.java)
    private var controller: MediaController? = null

    private val sessionsListener = MediaSessionManager.OnActiveSessionsChangedListener { list ->
        bind(list?.firstOrNull())
    }

    private val controllerCallback = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) = emit()
        override fun onMetadataChanged(metadata: MediaMetadata?) = emit()
        override fun onSessionDestroyed() = bind(null)
    }

    fun hasAccess(): Boolean {
        val enabled = AndroidSettings.Secure.getString(context.contentResolver, "enabled_notification_listeners") ?: return false
        return enabled.split(':').any { ComponentName.unflattenFromString(it) == listenerComponent }
    }

    fun accessSettingsIntent(): Intent =
        Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    fun playPause() {
        val c = controller ?: return
        if (c.playbackState?.state == PlaybackState.STATE_PLAYING) c.transportControls.pause() else c.transportControls.play()
    }

    fun next() = controller?.transportControls?.skipToNext()
    fun previous() = controller?.transportControls?.skipToPrevious()

    /** Posición estimada en este instante (para la barra de progreso mientras la pantalla es visible). */
    fun estimatedPositionMs(): Long? {
        val s = controller?.playbackState ?: return null
        if (s.position < 0) return null
        if (s.state != PlaybackState.STATE_PLAYING) return s.position
        val delta = SystemClock.elapsedRealtime() - s.lastPositionUpdateTime
        return s.position + (delta * s.playbackSpeed).toLong()
    }

    fun openSourceApp(): Boolean {
        val pkg = controller?.packageName ?: return false
        val launch = context.packageManager.getLaunchIntentForPackage(pkg) ?: return false
        context.startActivity(launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        return true
    }

    private fun bind(c: MediaController?) {
        controller?.unregisterCallback(controllerCallback)
        controller = c
        c?.registerCallback(controllerCallback, mainHandler)
        emit()
    }

    private fun emit() {
        val c = controller ?: return publish(MediaUiState.NoActiveSession)
        val md = c.metadata
        val st = c.playbackState
        val actions = st?.actions ?: 0L
        val duration = md?.getLong(MediaMetadata.METADATA_KEY_DURATION)?.takeIf { it > 0 }
        publish(
            MediaUiState.Active(
                NowPlaying(
                    title = md?.getString(MediaMetadata.METADATA_KEY_TITLE),
                    artist = md?.getString(MediaMetadata.METADATA_KEY_ARTIST)
                        ?: md?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST),
                    sourceAppLabel = appLabel(c.packageName),
                    isPlaying = st?.state == PlaybackState.STATE_PLAYING,
                    durationMs = duration,
                    positionMs = st?.position?.takeIf { it >= 0 },
                    canSkipNext = actions and PlaybackState.ACTION_SKIP_TO_NEXT != 0L,
                    canSkipPrevious = actions and PlaybackState.ACTION_SKIP_TO_PREVIOUS != 0L,
                ),
            ),
        )
    }

    private fun appLabel(pkg: String): String? = try {
        val pm = context.packageManager
        pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
    } catch (_: PackageManager.NameNotFoundException) {
        null
    }

    override fun onActive() {
        val m = msm ?: return publish(MediaUiState.Error("Servicio multimedia no disponible"))
        if (!hasAccess()) return publish(MediaUiState.PermissionRequired)
        try {
            m.addOnActiveSessionsChangedListener(sessionsListener, listenerComponent, mainHandler)
            bind(m.getActiveSessions(listenerComponent).firstOrNull())
        } catch (_: SecurityException) {
            publish(MediaUiState.PermissionRequired)
        }
    }

    override fun onInactive() {
        try { msm?.removeOnActiveSessionsChangedListener(sessionsListener) } catch (_: RuntimeException) { }
        controller?.unregisterCallback(controllerCallback)
        controller = null
    }

    /** Re-evaluar acceso al volver de Ajustes del sistema. */
    fun recheck() {
        if (!hasObservers) return
        onInactive()
        onActive()
    }
}
