package ec.dx3os.core.projection

/**
 * Estado honesto de Android Auto proyectado.
 *
 * Android Auto proyectado requiere que la unidad principal implemente el receptor
 * certificado por Google (programa de socios). Una app de terceros no puede convertirse
 * en receptor de Android Auto. DX3 OS solo detecta y documenta; no implementa ni imita el protocolo.
 */
enum class AndroidAutoAvailability {
    /** No se ha identificado la unidad: no se puede afirmar compatibilidad. */
    UNVERIFIED,

    /** Se verificó (documentación del fabricante / prueba física) que la unidad es receptor certificado. */
    CERTIFIED_RECEIVER_PRESENT,

    /** Se verificó que la unidad no es compatible. */
    NOT_SUPPORTED,
}

data class AndroidAutoInputs(
    /** Resultado de la verificación de hardware (HARDWARE_REPORT.md). Por defecto: sin verificar. */
    val headUnitVerification: AndroidAutoAvailability = AndroidAutoAvailability.UNVERIFIED,
    /** Paquete de un receptor instalado por el fabricante, si se identificó en la verificación. */
    val receiverPackageInstalled: Boolean = false,
)

data class AndroidAutoPresentation(val title: String, val detail: String, val canLaunchReceiver: Boolean)

object AndroidAutoEvaluator {
    fun present(inputs: AndroidAutoInputs): AndroidAutoPresentation = when (inputs.headUnitVerification) {
        AndroidAutoAvailability.UNVERIFIED -> AndroidAutoPresentation(
            title = "Android Auto: sin verificar",
            detail = "Aún no se confirmó si esta radio es un receptor certificado. Ver HARDWARE_REPORT.md.",
            canLaunchReceiver = false,
        )
        AndroidAutoAvailability.NOT_SUPPORTED -> AndroidAutoPresentation(
            title = "Android Auto no compatible",
            detail = "Esta unidad no es un receptor certificado. Alternativas en docs/ANDROID_AUTO.md.",
            canLaunchReceiver = false,
        )
        AndroidAutoAvailability.CERTIFIED_RECEIVER_PRESENT -> if (inputs.receiverPackageInstalled) {
            AndroidAutoPresentation(
                title = "Android Auto",
                detail = "Conecte el teléfono para abrir el receptor del fabricante.",
                canLaunchReceiver = true,
            )
        } else {
            AndroidAutoPresentation(
                title = "Android Auto: receptor no encontrado",
                detail = "La unidad es compatible pero no se encontró la app receptora del fabricante.",
                canLaunchReceiver = false,
            )
        }
    }
}
