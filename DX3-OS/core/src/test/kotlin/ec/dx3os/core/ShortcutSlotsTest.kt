package ec.dx3os.core

import ec.dx3os.core.launcher.ShortcutSlots
import ec.dx3os.core.launcher.ShortcutTarget
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ShortcutSlotsTest {
    private val maps = ShortcutTarget("com.example.maps", "com.example.maps.Main")
    private val music = ShortcutTarget("com.example.music", ".Player")

    @Test fun roundTrip() {
        val s = ShortcutSlots.empty().with(0, maps).with(2, music)
        assertEquals("com.example.maps/com.example.maps.Main||com.example.music/.Player|", s.encode())
        assertEquals(s, ShortcutSlots.decode(s.encode()))
    }

    @Test fun decodeIsTolerant() {
        assertEquals(ShortcutSlots.empty(), ShortcutSlots.decode(null))
        assertEquals(ShortcutSlots.empty(), ShortcutSlots.decode("basura|a/b/c|/x|"))
        val s = ShortcutSlots.decode("a/b|c/d|e/f|g/h|i/j|k/l")
        assertEquals(4, s.slots.size)
    }

    @Test fun pruneKeepsPositions() {
        val s = ShortcutSlots.empty().with(0, maps).with(1, music).pruneUnavailable { it == music }
        assertNull(s.slots[0])
        assertEquals(music, s.slots[1])
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsSeparator() { ShortcutTarget("a|b", "c") }
}
