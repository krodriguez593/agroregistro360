package ec.dx3os.launcher.screens

import android.app.AlertDialog
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import ec.dx3os.core.media.MediaFormat
import ec.dx3os.core.media.MediaUiState
import ec.dx3os.core.projection.AndroidAutoEvaluator
import ec.dx3os.core.projection.AndroidAutoInputs
import ec.dx3os.launcher.R
import ec.dx3os.launcher.ui.Ui

/** Vistas compartidas por varias pantallas. */
internal object Widgets {

    /** Estado vacío / error / permiso, con acción opcional. */
    fun message(c: Context, iconRes: Int, title: String, detail: String, actionLabel: String? = null, action: (() -> Unit)? = null): View =
        Ui.column(c).apply {
            gravity = Gravity.CENTER
            setPadding(Ui.dp(c, 32), Ui.dp(c, 24), Ui.dp(c, 32), Ui.dp(c, 24))
            addView(Ui.icon(c, iconRes, R.color.text_secondary, 40))
            addView(Ui.text(c, title, 22f, R.color.text, bold = true).apply { gravity = Gravity.CENTER; setPadding(0, Ui.dp(c, 12), 0, Ui.dp(c, 6)) })
            addView(Ui.text(c, detail, 16f, R.color.text_secondary, maxLines = 4).apply { gravity = Gravity.CENTER })
            if (actionLabel != null && action != null) {
                addView(Ui.spacer(c, 16))
                addView(Ui.button(c, actionLabel, primary = true, onClick = action))
            }
        }

    fun progressBar(c: Context) = ProgressBar(c, null, android.R.attr.progressBarStyleHorizontal).apply {
        max = 1000
        progressTintList = android.content.res.ColorStateList.valueOf(Ui.color(c, R.color.accent))
        progressBackgroundTintList = android.content.res.ColorStateList.valueOf(Ui.color(c, R.color.surface_high))
    }

    /** Texto del estado multimedia para tarjetas compactas. */
    fun mediaSummary(state: MediaUiState): Pair<String, String> = when (state) {
        MediaUiState.PermissionRequired -> "Acceso requerido" to "Permita el control de reproducción en Ajustes"
        MediaUiState.NoActiveSession -> "Nada en reproducción" to "Abra una app de música"
        is MediaUiState.Error -> "Error multimedia" to state.message
        is MediaUiState.Active -> MediaFormat.titleOrFallback(state.nowPlaying) to MediaFormat.subtitle(state.nowPlaying)
    }

    fun showAndroidAutoInfo(c: Context) {
        val p = AndroidAutoEvaluator.present(AndroidAutoInputs())
        AlertDialog.Builder(c, R.style.Theme_Dx3_Dialog)
            .setTitle(p.title)
            .setMessage(
                p.detail + "\n\n" +
                    "• Android Auto proyectado solo funciona si la radio es un receptor certificado por Google.\n" +
                    "• DX3 OS no implementa ni imita Android Auto.\n" +
                    "• Alternativas: radio de reemplazo certificada o adaptador oficial compatible.",
            )
            .setPositiveButton("Entendido", null)
            .show()
    }

    /** Fila de lista grande (≥76dp) con icono de app. */
    fun appRow(c: Context, icon: android.graphics.drawable.Drawable?, label: String, onClick: () -> Unit): View =
        Ui.row(c).apply {
            minimumHeight = c.resources.getDimensionPixelSize(R.dimen.touch_min)
            setPadding(Ui.dp(c, 16), 0, Ui.dp(c, 16), 0)
            background = Ui.clickable(c, Ui.rounded(c, Ui.color(c, R.color.surface), 16), 16)
            isClickable = true
            contentDescription = label
            setOnClickListener { onClick() }
            addView(android.widget.ImageView(c).apply {
                setImageDrawable(icon)
                layoutParams = LinearLayout.LayoutParams(Ui.dp(c, 44), Ui.dp(c, 44))
            })
            addView(Ui.spacer(c, 16))
            addView(Ui.text(c, label, 20f, maxLines = 1), LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                .apply { bottomMargin = Ui.dp(c, 10) }
        }

    fun setTextIfChanged(tv: TextView, value: CharSequence) { if (tv.text.toString() != value.toString()) tv.text = value }
}
