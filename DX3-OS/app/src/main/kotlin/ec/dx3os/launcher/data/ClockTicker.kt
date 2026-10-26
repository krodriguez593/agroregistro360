package ec.dx3os.launcher.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import ec.dx3os.core.clock.ClockFormatter
import ec.dx3os.core.clock.ClockText
import ec.dx3os.core.clock.LocalDateTimeParts
import java.util.Calendar

/** Reloj que se actualiza una vez por minuto (no por segundo) y ante cambios de hora/zona. */
class ClockTicker(private val context: Context, private val settings: Settings) :
    Observable<ClockText>(ClockText("--:--", null, "")) {

    private val tick = object : Runnable {
        override fun run() {
            refresh()
            mainHandler.postDelayed(this, ClockFormatter.millisUntilNextMinute(System.currentTimeMillis()))
        }
    }
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context, i: Intent) = refresh()
    }
    private val settingsListener: () -> Unit = { refresh() }

    fun refresh() {
        val c = Calendar.getInstance()
        val dow = c.get(Calendar.DAY_OF_WEEK) // 1 = domingo en Calendar
        val parts = LocalDateTimeParts(
            year = c.get(Calendar.YEAR),
            month = c.get(Calendar.MONTH) + 1,
            dayOfMonth = c.get(Calendar.DAY_OF_MONTH),
            dayOfWeek = if (dow == Calendar.SUNDAY) 7 else dow - 1,
            hour = c.get(Calendar.HOUR_OF_DAY),
            minute = c.get(Calendar.MINUTE),
        )
        publish(ClockFormatter.format(parts, settings.use24Hour))
    }

    override fun onActive() {
        context.registerReceiver(receiver, IntentFilter().apply {
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
            addAction(Intent.ACTION_DATE_CHANGED)
        })
        settings.addListener(settingsListener)
        tick.run()
    }

    override fun onInactive() {
        mainHandler.removeCallbacks(tick)
        settings.removeListener(settingsListener)
        context.unregisterReceiver(receiver)
    }
}
