package ec.dx3os.core

import ec.dx3os.core.diagnostics.CheckStatus
import ec.dx3os.core.diagnostics.DeviceProfile
import ec.dx3os.core.diagnostics.HardwareChecklist
import ec.dx3os.core.diagnostics.TouchType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HardwareChecklistTest {
    private val empty = DeviceProfile(null, null, null, null, null, null, null, emptyList(), null, null, null, null,
        null, null, null, null, null, null, null, null, null, null, null)

    @Test fun emptyProfileIsAllUnknownOrPhysical() {
        val items = HardwareChecklist.build(empty)
        assertTrue(items.none { it.status == CheckStatus.CONFIRMED })
        assertEquals(4, HardwareChecklist.summary(items)[CheckStatus.NEEDS_PHYSICAL_TEST])
    }

    @Test fun physicalTestsNeverAutoConfirmed() {
        val full = empty.copy(manufacturer = "X", model = "Y", supportedAbis = listOf("armeabi-v7a"), androidRelease = "10",
            sdkInt = 29, totalRamBytes = 2L * 1_073_741_824, screenWidthPx = 600, screenHeightPx = 1024, densityDpi = 160,
            touchscreen = TouchType.MULTITOUCH_DISTINCT, hasBluetooth = true, hasCamera = true)
        val items = HardwareChecklist.build(full).associateBy { it.key }
        assertEquals(CheckStatus.NEEDS_PHYSICAL_TEST, items.getValue("reverse_cam").status)
        assertEquals(CheckStatus.NEEDS_PHYSICAL_TEST, items.getValue("swc").status)
        assertEquals("1024x600 px @ 160 dpi", items.getValue("screen").value)
        assertEquals("2.00 GB", items.getValue("ram").value)
        assertEquals("Android 10 (API 29)", items.getValue("android").value)
    }

    @Test fun plainTextHasNoSerialField() {
        assertFalse(HardwareChecklist.toPlainText(HardwareChecklist.build(empty)).contains("serial", ignoreCase = true))
    }
}
