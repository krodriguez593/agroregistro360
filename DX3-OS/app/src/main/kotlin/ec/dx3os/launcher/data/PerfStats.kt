package ec.dx3os.launcher.data

import android.app.ActivityManager
import android.content.Context
import android.os.Debug
import android.util.Log

/**
 * Mediciones reales en el dispositivo. No se inventan cifras: si una medida no se tomó,
 * se muestra como "sin medir". Los valores se escriben en logcat (etiqueta DX3Perf)
 * sin datos personales, para copiarlos a docs/PERFORMANCE.md.
 */
class PerfStats(private val context: Context) {

    /** Tiempo desde la creación del proceso hasta el primer dibujado de la pantalla de inicio. */
    var firstFrameMs: Long? = null
        private set

    fun recordFirstFrame(ms: Long) {
        if (firstFrameMs != null) return
        firstFrameMs = ms
        Log.i(TAG, "first_frame_ms=$ms")
    }

    data class MemorySample(val totalPssKb: Int, val javaHeapUsedKb: Long, val nativeHeapKb: Long, val memoryClassMb: Int, val lowRamDevice: Boolean)

    fun sampleMemory(): MemorySample {
        val info = Debug.MemoryInfo().also { Debug.getMemoryInfo(it) }
        val rt = Runtime.getRuntime()
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val sample = MemorySample(
            totalPssKb = info.totalPss,
            javaHeapUsedKb = (rt.totalMemory() - rt.freeMemory()) / 1024,
            nativeHeapKb = Debug.getNativeHeapAllocatedSize() / 1024,
            memoryClassMb = am.memoryClass,
            lowRamDevice = am.isLowRamDevice,
        )
        Log.i(TAG, "pss_kb=${sample.totalPssKb} java_heap_kb=${sample.javaHeapUsedKb} native_heap_kb=${sample.nativeHeapKb}")
        return sample
    }

    private companion object { const val TAG = "DX3Perf" }
}
