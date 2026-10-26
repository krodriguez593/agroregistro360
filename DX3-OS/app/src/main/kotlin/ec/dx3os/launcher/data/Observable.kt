package ec.dx3os.launcher.data

import android.os.Handler
import android.os.Looper

/**
 * Valor observable en el hilo principal que solo trabaja mientras tiene observadores:
 * [onActive] se llama con el primer observador y [onInactive] al quitarse el último.
 * Así ningún receptor o callback queda registrado cuando la pantalla no lo necesita.
 */
abstract class Observable<T>(initial: T) {
    protected val mainHandler = Handler(Looper.getMainLooper())
    private val observers = LinkedHashSet<(T) -> Unit>()

    var value: T = initial
        private set

    protected val hasObservers: Boolean get() = observers.isNotEmpty()

    fun observe(observer: (T) -> Unit) {
        val first = observers.isEmpty()
        observers += observer
        if (first) onActive()
        observer(value)
    }

    fun removeObserver(observer: (T) -> Unit) {
        if (observers.remove(observer) && observers.isEmpty()) onInactive()
    }

    protected fun publish(newValue: T) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post { publish(newValue) }
            return
        }
        if (newValue == value) return
        value = newValue
        observers.toList().forEach { it(newValue) }
    }

    protected abstract fun onActive()
    protected abstract fun onInactive()
}
