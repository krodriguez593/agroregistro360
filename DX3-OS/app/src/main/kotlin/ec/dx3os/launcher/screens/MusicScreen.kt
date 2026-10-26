package ec.dx3os.launcher.screens

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import ec.dx3os.core.media.MediaFormat
import ec.dx3os.core.media.MediaUiState
import ec.dx3os.launcher.R
import ec.dx3os.launcher.ui.Host
import ec.dx3os.launcher.ui.Screen
import ec.dx3os.launcher.ui.Ui

/** Control del reproductor activo del sistema mediante MediaSession (API pública). */
class MusicScreen(host: Host) : Screen(host) {
    private lateinit var container: FrameLayout
    private var title: TextView? = null
    private var subtitle: TextView? = null
    private var progress: ProgressBar? = null
    private var elapsed: TextView? = null
    private var total: TextView? = null
    private var playPause: ImageView? = null
    private var shownKind: Class<*>? = null
    private var last: MediaUiState? = null

    private val ticker = object : Runnable {
        override fun run() {
            val s = last as? MediaUiState.Active ?: return
            updateProgress(s)
            if (s.nowPlaying.isPlaying) container.postDelayed(this, 1000)
        }
    }

    private val obs: (MediaUiState) -> Unit = { render(it) }

    override fun createView(): View {
        container = FrameLayout(ctx)
        return container
    }

    private fun render(s: MediaUiState) {
        last = s
        if (shownKind != s.javaClass) {
            shownKind = s.javaClass
            container.removeAllViews()
            container.addView(build(s))
        }
        if (s is MediaUiState.Active) {
            val np = s.nowPlaying
            title?.text = MediaFormat.titleOrFallback(np)
            subtitle?.text = MediaFormat.subtitle(np)
            playPause?.setImageResource(if (np.isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
            total?.text = MediaFormat.duration(np.durationMs)
            updateProgress(s)
            container.removeCallbacks(ticker)
            if (np.isPlaying) container.postDelayed(ticker, 1000)
        } else {
            container.removeCallbacks(ticker)
        }
    }

    private fun updateProgress(s: MediaUiState.Active) {
        val pos = services.media.estimatedPositionMs() ?: s.nowPlaying.positionMs
        val p = MediaFormat.progressPermille(s.nowPlaying.copy(positionMs = pos))
        progress?.visibility = if (p == null) View.INVISIBLE else View.VISIBLE
        if (p != null) progress?.progress = p
        elapsed?.text = MediaFormat.duration(pos)
    }

    private fun build(s: MediaUiState): View {
        val c = ctx
        val media = services.media
        return when (s) {
            MediaUiState.PermissionRequired -> Widgets.message(c, R.drawable.ic_music, "Permita el control de reproducción",
                "Android exige el acceso a notificaciones para que una app vea qué se está reproduciendo. " +
                    "DX3 OS no lee ni guarda el contenido de sus notificaciones.",
                "Abrir ajustes de acceso") {
                if (!services.apps.start(media.accessSettingsIntent())) host.toast("Este equipo no permite abrir ese ajuste")
            }
            is MediaUiState.Error -> Widgets.message(c, R.drawable.ic_warning, "Error multimedia", s.message)
            MediaUiState.NoActiveSession -> mediaAppList("Nada en reproducción", "Abra una app de música para controlarla desde aquí.")
            is MediaUiState.Active -> nowPlaying()
        }
    }

    private fun nowPlaying(): View {
        val c = ctx
        val media = services.media
        val col = Ui.column(c).apply {
            gravity = Gravity.CENTER
            setPadding(Ui.dp(c, 48), Ui.dp(c, 8), Ui.dp(c, 48), Ui.dp(c, 16))
        }
        title = Ui.text(c, "", 32f, bold = true, maxLines = 1).apply { gravity = Gravity.CENTER }
        subtitle = Ui.text(c, "", 18f, R.color.text_secondary, maxLines = 1).apply { gravity = Gravity.CENTER; setPadding(0, Ui.dp(c, 8), 0, Ui.dp(c, 20)) }
        col.addView(title, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        col.addView(subtitle, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        progress = Widgets.progressBar(c)
        col.addView(progress, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 6)))
        val times = Ui.row(c).apply { setPadding(0, Ui.dp(c, 6), 0, Ui.dp(c, 16)) }
        elapsed = Ui.text(c, "", 14f, R.color.text_secondary, maxLines = 1)
        total = Ui.text(c, "", 14f, R.color.text_secondary, maxLines = 1)
        times.addView(elapsed, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        times.addView(total)
        col.addView(times, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        val controls = Ui.row(c, Gravity.CENTER)
        controls.addView(Ui.roundIconButton(c, R.drawable.ic_prev, "Anterior", 84) { media.previous() })
        controls.addView(Ui.spacer(c, 28))
        playPause = Ui.roundIconButton(c, R.drawable.ic_play, "Reproducir o pausar", 104, primary = true) { media.playPause() }
        controls.addView(playPause)
        controls.addView(Ui.spacer(c, 28))
        controls.addView(Ui.roundIconButton(c, R.drawable.ic_next, "Siguiente", 84) { media.next() })
        col.addView(controls)
        col.addView(Ui.spacer(c, 16))
        col.addView(Ui.button(c, "Abrir app de origen") { if (!media.openSourceApp()) host.toast("No se pudo abrir la app de origen") })
        return col
    }

    private fun mediaAppList(titleText: String, detail: String): View {
        val c = ctx
        val apps = services.apps
        val mediaApps = apps.mediaApps()
        if (mediaApps.isEmpty()) return Widgets.message(c, R.drawable.ic_music, titleText, "$detail\nNo se encontraron apps de música compatibles.")
        val list = Ui.column(c).apply { setPadding(Ui.dp(c, 24), Ui.dp(c, 8), Ui.dp(c, 24), Ui.dp(c, 16)) }
        list.addView(Ui.text(c, titleText, 22f, bold = true))
        list.addView(Ui.text(c, detail, 16f, R.color.text_secondary).apply { setPadding(0, Ui.dp(c, 6), 0, Ui.dp(c, 16)) })
        mediaApps.forEach { app -> list.addView(Widgets.appRow(c, apps.icon(app.component), app.label) { apps.launch(app.component) }) }
        return ScrollView(c).apply { addView(list) }
    }

    override fun onShow() = services.media.observe(obs)

    override fun onHide() {
        container.removeCallbacks(ticker)
        services.media.removeObserver(obs)
    }
}
