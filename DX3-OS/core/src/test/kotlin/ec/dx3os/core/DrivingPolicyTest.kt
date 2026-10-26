package ec.dx3os.core

import ec.dx3os.core.safety.DrivingPolicy
import ec.dx3os.core.safety.DrivingState
import ec.dx3os.core.safety.Feature
import ec.dx3os.core.vehicle.DataProvenance
import ec.dx3os.core.vehicle.Gear
import ec.dx3os.core.vehicle.UnavailableVehicleDataSource
import ec.dx3os.core.vehicle.VehicleSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DrivingPolicyTest {
    private fun snap(speed: Int?, gear: Gear?, brake: Boolean? = null) =
        VehicleSnapshot(DataProvenance.SIMULATED, speedKmh = speed, gear = gear, parkingBrake = brake)

    @Test fun unavailableSourceIsUnknown() {
        assertEquals(DrivingState.UNKNOWN, DrivingPolicy.drivingState(UnavailableVehicleDataSource.snapshot()))
    }

    @Test fun derivesStates() {
        assertEquals(DrivingState.PARKED, DrivingPolicy.drivingState(snap(0, Gear.PARK)))
        assertEquals(DrivingState.PARKED, DrivingPolicy.drivingState(snap(null, Gear.PARK)))
        assertEquals(DrivingState.PARKED, DrivingPolicy.drivingState(snap(0, Gear.NEUTRAL, brake = true)))
        assertEquals(DrivingState.MOVING, DrivingPolicy.drivingState(snap(40, Gear.DRIVE)))
        // Detenido en un semáforo en D sigue siendo conducción.
        assertEquals(DrivingState.MOVING, DrivingPolicy.drivingState(snap(0, Gear.DRIVE)))
        assertEquals(DrivingState.MOVING, DrivingPolicy.drivingState(snap(50, null)))
        assertEquals(DrivingState.UNKNOWN, DrivingPolicy.drivingState(snap(null, null)))
    }

    @Test fun unknownBlocksDistractingFeatures() {
        assertFalse(DrivingPolicy.isAllowed(Feature.VIDEO_PLAYBACK, DrivingState.UNKNOWN))
        assertFalse(DrivingPolicy.isAllowed(Feature.TEXT_ENTRY, DrivingState.UNKNOWN))
        assertTrue(DrivingPolicy.isAllowed(Feature.SHORTCUT_EDITING, DrivingState.UNKNOWN))
    }

    @Test fun movingKeepsEssentialsOnly() {
        assertFalse(DrivingPolicy.isAllowed(Feature.VIDEO_PLAYBACK, DrivingState.MOVING))
        assertFalse(DrivingPolicy.isAllowed(Feature.SHORTCUT_EDITING, DrivingState.MOVING))
        assertFalse(DrivingPolicy.isAllowed(Feature.APP_GRID, DrivingState.MOVING))
        assertTrue(DrivingPolicy.isAllowed(Feature.MEDIA_CONTROLS, DrivingState.MOVING))
        assertTrue(DrivingPolicy.isAllowed(Feature.NAVIGATION, DrivingState.MOVING))
        assertTrue(DrivingPolicy.isAllowed(Feature.PHONE, DrivingState.MOVING))
    }

    @Test fun parkedAllowsEverything() {
        Feature.values().forEach { assertTrue(it.name, DrivingPolicy.isAllowed(it, DrivingState.PARKED)) }
    }
}
