package ec.dx3os.core

import ec.dx3os.core.media.MediaFormat
import ec.dx3os.core.media.NowPlaying
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MediaFormatTest {
    private val np = NowPlaying("Canción", "Artista", "Reproductor", true, 200_000, 50_000, true, false)

    @Test fun durations() {
        assertEquals("0:00", MediaFormat.duration(0))
        assertEquals("1:05", MediaFormat.duration(65_000))
        assertEquals("1:02:05", MediaFormat.duration(3_725_000))
        assertEquals("--:--", MediaFormat.duration(null))
        assertEquals("--:--", MediaFormat.duration(-1))
    }

    @Test fun fallbacks() {
        assertEquals("Sin título", MediaFormat.titleOrFallback(np.copy(title = " ")))
        assertEquals("Artista · Reproductor", MediaFormat.subtitle(np))
        assertEquals("Fuente desconocida", MediaFormat.subtitle(np.copy(artist = null, sourceAppLabel = "")))
    }

    @Test fun progress() {
        assertEquals(250, MediaFormat.progressPermille(np))
        assertEquals(1000, MediaFormat.progressPermille(np.copy(positionMs = 999_999)))
        assertNull(MediaFormat.progressPermille(np.copy(durationMs = null)))
        assertNull(MediaFormat.progressPermille(np.copy(durationMs = 0)))
    }
}
