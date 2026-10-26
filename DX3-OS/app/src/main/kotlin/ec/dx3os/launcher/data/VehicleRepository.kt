package ec.dx3os.launcher.data

import android.os.SystemClock
import ec.dx3os.core.vehicle.SimulatedVehicleDataSource
import ec.dx3os.core.vehicle.UnavailableVehicleDataSource
import ec.dx3os.core.vehicle.VehicleDataSource
import ec.dx3os.core.vehicle.VehicleSnapshot

/**
 * Datos del vehículo. Hoy NO existe ninguna fuente real verificada para el Soueast DX3:
 * por defecto se usa [UnavailableVehicleDataSource]. El simulador solo se activa a mano
 * en Ajustes y la UI lo marca como "SIMULADO".
 */
class VehicleRepository(private val settings: Settings) :
    Observable<VehicleSnapshot>(UnavailableVehicleDataSource.snapshot()) {

    private val simulator = SimulatedVehicleDataSource { SystemClock.elapsedRealtime() / 1000 }
    private val settingsListener: () -> Unit = { restart() }

    private val poll = object : Runnable {
        override fun run() {
            publish(source().snapshot())
            // Solo el simulador necesita sondeo; la fuente no disponible no consume CPU.
            if (settings.vehicleSimulation) mainHandler.postDelayed(this, 1000)
        }
    }

    private fun source(): VehicleDataSource =
        if (settings.vehicleSimulation) simulator else UnavailableVehicleDataSource

    private fun restart() {
        mainHandler.removeCallbacks(poll)
        poll.run()
    }

    override fun onActive() {
        settings.addListener(settingsListener)
        poll.run()
    }

    override fun onInactive() {
        settings.removeListener(settingsListener)
        mainHandler.removeCallbacks(poll)
    }
}
