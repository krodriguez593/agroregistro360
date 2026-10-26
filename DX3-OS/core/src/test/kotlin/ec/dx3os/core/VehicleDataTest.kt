package ec.dx3os.core

import ec.dx3os.core.vehicle.DataProvenance
import ec.dx3os.core.vehicle.Gear
import ec.dx3os.core.vehicle.SimulatedVehicleDataSource
import ec.dx3os.core.vehicle.UnavailableVehicleDataSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VehicleDataTest {
    @Test fun unavailableHasNoInventedValues() {
        val s = UnavailableVehicleDataSource.snapshot()
        assertEquals(DataProvenance.UNAVAILABLE, s.provenance)
        assertNull(s.speedKmh); assertNull(s.gear); assertNull(s.fuelLevelPercent)
        assertNull(s.outsideTempC); assertNull(s.reverseEngaged)
    }

    @Test fun simulatorIsLabelledAndDeterministic() {
        var tick = 0L
        val sim = SimulatedVehicleDataSource { tick }
        val a = sim.snapshot()
        assertEquals(DataProvenance.SIMULATED, a.provenance)
        assertEquals(a, sim.snapshot())
        assertEquals(Gear.PARK, a.gear)
        tick = 35
        val r = sim.snapshot()
        assertEquals(Gear.REVERSE, r.gear)
        assertTrue(r.reverseEngaged == true)
    }
}
