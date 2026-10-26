package ec.dx3os.core.vehicle

/**
 * De dónde proviene un dato del vehículo. La interfaz SIEMPRE muestra la procedencia:
 * nunca se presenta un dato simulado como real.
 */
enum class DataProvenance {
    /** Leído a través de una interfaz verificada en el hardware real (aún no existe ninguna). */
    VERIFIED,

    /** Generado por [SimulatedVehicleDataSource]; sólo para desarrollo y demostraciones. */
    SIMULATED,

    /** No hay interfaz compatible verificada. */
    UNAVAILABLE,
}

/** Todos los campos son opcionales: null significa "desconocido", nunca "cero". */
data class VehicleSnapshot(
    val provenance: DataProvenance,
    val speedKmh: Int? = null,
    val gear: Gear? = null,
    val reverseEngaged: Boolean? = null,
    val outsideTempC: Int? = null,
    val fuelLevelPercent: Int? = null,
    val parkingBrake: Boolean? = null,
)

enum class Gear { PARK, REVERSE, NEUTRAL, DRIVE, MANUAL }

/**
 * Fuente de datos del vehículo. Solo lectura por diseño: no existe ninguna operación
 * de escritura hacia el vehículo (CAN, ECU, etc.). Ver SECURITY.md.
 */
interface VehicleDataSource {
    val id: String
    fun snapshot(): VehicleSnapshot
}

/** Implementación por defecto: no hay ninguna interfaz del vehículo verificada. */
object UnavailableVehicleDataSource : VehicleDataSource {
    override val id = "unavailable"
    override fun snapshot() = VehicleSnapshot(provenance = DataProvenance.UNAVAILABLE)
}

/**
 * SIMULADOR — datos ficticios, deterministas a partir de [tick], para probar la interfaz.
 * La UI etiqueta estos valores como "SIMULADO".
 */
class SimulatedVehicleDataSource(private val tickProvider: () -> Long) : VehicleDataSource {
    override val id = "simulated"

    override fun snapshot(): VehicleSnapshot {
        val t = tickProvider()
        val phase = (t / 10) % 4 // cambia cada 10 ticks: P -> D -> D -> R
        val gear = when (phase) { 0L -> Gear.PARK; 3L -> Gear.REVERSE; else -> Gear.DRIVE }
        val speed = when (gear) {
            Gear.PARK -> 0
            Gear.REVERSE -> 4
            else -> (20 + (t % 10) * 4).toInt()
        }
        return VehicleSnapshot(
            provenance = DataProvenance.SIMULATED,
            speedKmh = speed,
            gear = gear,
            reverseEngaged = gear == Gear.REVERSE,
            outsideTempC = 22,
            fuelLevelPercent = (80 - (t / 20) % 60).toInt(),
            parkingBrake = gear == Gear.PARK,
        )
    }
}
