package ec.dx3os.core.safety

import ec.dx3os.core.vehicle.DataProvenance
import ec.dx3os.core.vehicle.Gear
import ec.dx3os.core.vehicle.VehicleSnapshot

enum class DrivingState { PARKED, MOVING, UNKNOWN }

/** Funciones que pueden restringirse durante la conducción. */
enum class Feature { VIDEO_PLAYBACK, TEXT_ENTRY, SHORTCUT_EDITING, APP_GRID, MEDIA_CONTROLS, NAVIGATION, PHONE }

/**
 * Política de distracción. Principio: ante la duda, se restringe.
 * - Un estado UNKNOWN se trata como MOVING para funciones distractoras (vídeo, texto).
 * - Las funciones básicas de conducción (música, navegación, teléfono manos libres) nunca se bloquean.
 */
object DrivingPolicy {
    private const val PARKED_MAX_SPEED_KMH = 3

    fun drivingState(snapshot: VehicleSnapshot): DrivingState {
        if (snapshot.provenance == DataProvenance.UNAVAILABLE) return DrivingState.UNKNOWN
        val speed = snapshot.speedKmh
        return when {
            snapshot.gear == Gear.PARK && (speed == null || speed <= PARKED_MAX_SPEED_KMH) -> DrivingState.PARKED
            snapshot.parkingBrake == true && speed != null && speed == 0 -> DrivingState.PARKED
            speed != null && speed > PARKED_MAX_SPEED_KMH -> DrivingState.MOVING
            speed != null && snapshot.gear != null -> DrivingState.MOVING
            else -> DrivingState.UNKNOWN
        }
    }

    fun isAllowed(feature: Feature, state: DrivingState): Boolean = when (feature) {
        Feature.MEDIA_CONTROLS, Feature.NAVIGATION, Feature.PHONE -> true
        // Sin datos de velocidad verificados no podemos saber si el vehículo está detenido;
        // el vídeo y la escritura quedan bloqueados salvo estacionamiento confirmado.
        Feature.VIDEO_PLAYBACK, Feature.TEXT_ENTRY -> state == DrivingState.PARKED
        // Editar accesos o recorrer la lista de apps: solo se bloquea si consta movimiento,
        // para que la app siga siendo configurable en hardware sin datos del vehículo.
        Feature.SHORTCUT_EDITING, Feature.APP_GRID -> state != DrivingState.MOVING
    }
}
