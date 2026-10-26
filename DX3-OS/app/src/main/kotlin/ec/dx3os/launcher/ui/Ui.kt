package ec.dx3os.launcher.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import ec.dx3os.core.connectivity.StatusPresentation
import ec.dx3os.launcher.R

/** Sistema visual DX3 OS construido con vistas del framework (sin AndroidX). */
object Ui {
    fun dp(c: Context, v: Number): Int = (v.toFloat() * c.resources.displayMetrics.density + 0.5f).toInt()
    fun color(c: Context, id: Int): Int = c.resources.getColor(id, c.theme)

    fun severityColor(c: Context, s: StatusPresentation.Severity) = color(
        c,
        when (s) {
            StatusPresentation.Severity.OK -> R.color.ok
            StatusPresentation.Severity.INFO -> R.color.text_secondary
            StatusPresentation.Severity.WARNING -> R.color.warning
            StatusPresentation.Severity.ERROR -> R.color.error
        },
    )

    fun rounded(c: Context, fill: Int, radiusDp: Int = 20, strokeColor: Int? = color(c, R.color.stroke)) = GradientDrawable().apply {
        setColor(fill)
        cornerRadius = dp(c, radiusDp).toFloat()
        if (strokeColor != null) setStroke(dp(c, 1), strokeColor)
    }

    fun clickable(c: Context, base: Drawable, radiusDp: Int = 20): Drawable =
        RippleDrawable(ColorStateList.valueOf(color(c, R.color.ripple)), base, rounded(c, 0xFFFFFFFF.toInt(), radiusDp, null))

    fun text(c: Context, value: CharSequence = "", sp: Float = 18f, colorRes: Int = R.color.text, bold: Boolean = false, maxLines: Int = 2) =
        TextView(c).apply {
            text = value
            setTextSize(TypedValue.COMPLEX_UNIT_SP, sp)
            setTextColor(color(c, colorRes))
            typeface = Typeface.create("sans-serif" + if (bold) "-medium" else "", Typeface.NORMAL)
            this.maxLines = maxLines
            ellipsize = android.text.TextUtils.TruncateAt.END
            includeFontPadding = false
        }

    fun icon(c: Context, res: Int, tintRes: Int = R.color.text, sizeDp: Int = 28) = ImageView(c).apply {
        setImageResource(res)
        imageTintList = ColorStateList.valueOf(color(c, tintRes))
        layoutParams = LinearLayout.LayoutParams(dp(c, sizeDp), dp(c, sizeDp))
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
    }

    /** Tarjeta: contenedor vertical con fondo de superficie. */
    fun card(c: Context, onClick: (() -> Unit)? = null) = LinearLayout(c).apply {
        orientation = LinearLayout.VERTICAL
        val p = dp(c, 16)
        setPadding(p, p, p, p)
        val bg = rounded(c, color(c, R.color.surface))
        background = if (onClick != null) clickable(c, bg) else bg
        if (onClick != null) {
            isClickable = true
            isFocusable = true
            setOnClickListener { onClick() }
        }
    }

    /** Botón grande (≥76dp) con icono opcional. */
    fun button(c: Context, label: String, iconRes: Int? = null, primary: Boolean = false, onClick: () -> Unit) = LinearLayout(c).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER
        minimumHeight = c.resources.getDimensionPixelSize(R.dimen.touch_min)
        val h = dp(c, 20)
        setPadding(h, 0, h, 0)
        val fill = if (primary) color(c, R.color.accent) else color(c, R.color.surface_high)
        background = clickable(c, rounded(c, fill, 16, if (primary) null else color(c, R.color.stroke)), 16)
        isClickable = true
        isFocusable = true
        contentDescription = label
        setOnClickListener { onClick() }
        val fg = if (primary) R.color.bg else R.color.text
        if (iconRes != null) addView(icon(c, iconRes, fg, 26).also { (it.layoutParams as LinearLayout.LayoutParams).rightMargin = dp(c, 10) })
        addView(text(c, label, 18f, fg, bold = true, maxLines = 1))
    }

    /** Botón circular solo icono, para controles multimedia. */
    fun roundIconButton(c: Context, iconRes: Int, label: String, sizeDp: Int, primary: Boolean = false, onClick: () -> Unit) = ImageView(c).apply {
        setImageResource(iconRes)
        val pad = sizeDp / 4
        setPadding(dp(c, pad), dp(c, pad), dp(c, pad), dp(c, pad))
        imageTintList = ColorStateList.valueOf(color(c, if (primary) R.color.bg else R.color.text))
        val fill = color(c, if (primary) R.color.accent else R.color.surface_high)
        background = clickable(c, rounded(c, fill, sizeDp / 2, null), sizeDp / 2)
        layoutParams = LinearLayout.LayoutParams(dp(c, sizeDp), dp(c, sizeDp))
        contentDescription = label
        isClickable = true
        isFocusable = true
        setOnClickListener { onClick() }
    }

    fun badge(c: Context, label: String, colorRes: Int) = text(c, label, 12f, colorRes, bold = true, maxLines = 1).apply {
        letterSpacing = 0.08f
        val p = dp(c, 8)
        setPadding(p, dp(c, 4), p, dp(c, 4))
        background = rounded(c, 0x00000000, 8, color(c, colorRes))
    }

    fun row(c: Context, gravity: Int = Gravity.CENTER_VERTICAL) = LinearLayout(c).apply {
        orientation = LinearLayout.HORIZONTAL
        this.gravity = gravity
    }

    fun column(c: Context) = LinearLayout(c).apply { orientation = LinearLayout.VERTICAL }

    fun weighted(weight: Float, vertical: Boolean) = if (vertical) {
        LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, weight)
    } else {
        LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, weight)
    }

    fun spacer(c: Context, dpSize: Int) = View(c).apply { layoutParams = LinearLayout.LayoutParams(dp(c, dpSize), dp(c, dpSize)) }

    fun margins(v: View, c: Context, all: Int): View {
        (v.layoutParams as? ViewGroup.MarginLayoutParams)?.setMargins(dp(c, all), dp(c, all), dp(c, all), dp(c, all))
        return v
    }

    fun sectionTitle(c: Context, label: String) = text(c, label.uppercase(), 13f, R.color.accent, bold = true, maxLines = 1).apply {
        letterSpacing = 0.12f
        setPadding(0, dp(c, 20), 0, dp(c, 8))
    }
}
