package io.github.zyrouge.symphony.desktop.library

import io.github.zyrouge.symphony.desktop.player.repoRoot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermissions

class DesktopLibraryTest {
    @TempDir
    lateinit var temp: Path

    private val fixtures: Path = repoRoot().resolve("app/src/test/resources")

    @Test
    fun `empty directory scans to nothing`() {
        val scan = DesktopLibrary.scan(temp)
        assertEquals(emptyList<DesktopTrack>(), scan.tracks)
        assertEquals(0, scan.skippedCount)
        assertNull(scan.unavailableReason)
    }

    @Test
    fun `fixture directory yields all six tracks`() {
        val scan = DesktopLibrary.scan(fixtures)
        assertNull(scan.unavailableReason)
        assertEquals(6, scan.tracks.size, "tracks=${scan.tracks.map { it.path.fileName }}")
        assertEquals(0, scan.skippedCount)

        for (track in scan.tracks) {
            assertEquals("Demo Audio", track.fields.title, "title of ${track.path.fileName}")
            assertEquals("Demo Album", track.fields.album, "album of ${track.path.fileName}")
            assertTrue(
                track.fields.artists.contains("Demo Artist 1"),
                "artists of ${track.path.fileName} were ${track.fields.artists}",
            )
            assertTrue(
                track.fields.artists.contains("Demo Artist 2"),
                "artists of ${track.path.fileName} were ${track.fields.artists}",
            )
            assertTrue(track.fields.duration > 0, "duration of ${track.path.fileName}")
            assertNotNull(track.fields.coverFile, "cover of ${track.path.fileName}")
        }
    }

    @Test
    fun `tracks are sorted by path and ids are stable across scans`() {
        val first = DesktopLibrary.scan(fixtures)
        val second = DesktopLibrary.scan(fixtures)
        assertEquals(
            first.tracks.map { it.path },
            first.tracks.map { it.path }.sorted(),
        )
        assertEquals(
            first.tracks.map { it.fields.id },
            second.tracks.map { it.fields.id },
        )
        assertEquals(
            first.tracks.map { it.path.toString() },
            first.tracks.map { it.fields.id },
        )
    }

    @Test
    fun `mixed directory keeps audio files and counts broken ones`() {
        Files.copy(fixtures.resolve("audio.mp3"), temp.resolve("audio.mp3"))
        Files.copy(fixtures.resolve("audio.flac"), temp.resolve("audio.flac"))
        Files.writeString(temp.resolve("notes.txt"), "not audio")
        Files.write(temp.resolve("broken.mp3"), ByteArray(2048) { 0 })

        val scan = DesktopLibrary.scan(temp)
        assertNull(scan.unavailableReason)
        assertEquals(
            listOf("audio.flac", "audio.mp3"),
            scan.tracks.map { it.path.fileName.toString() },
        )
        assertEquals(1, scan.skippedCount)
        assertEquals("broken.mp3", scan.skipped.single().path.fileName.toString())
        assertTrue(scan.skipped.single().reason.isNotBlank())
    }

    @Test
    fun `genres split on the configured separators`() {
        val scan = DesktopLibrary.scan(fixtures)
        val flac = scan.tracks.single { it.path.fileName.toString() == "audio.flac" }
        assertEquals(setOf("Rap", "Rock"), flac.fields.genres)
    }

    @Test
    fun `missing directory reports why`() {
        val scan = DesktopLibrary.scan(temp.resolve("absent"))
        assertEquals(emptyList<DesktopTrack>(), scan.tracks)
        assertEquals("does not exist", scan.unavailableReason)
    }

    @Test
    fun `a file instead of a directory reports why`() {
        val file = Files.createFile(temp.resolve("single.mp3"))
        assertEquals("is not a directory", DesktopLibrary.scan(file).unavailableReason)
    }

    @Test
    fun `unreadable directory reports why`() {
        val locked = Files.createDirectory(temp.resolve("locked"))
        Files.setPosixFilePermissions(locked, PosixFilePermissions.fromString("---------"))
        try {
            if (Files.isReadable(locked)) return
            val scan = DesktopLibrary.scan(locked)
            assertEquals("is not readable", scan.unavailableReason)
            assertEquals(emptyList<DesktopTrack>(), scan.tracks)
        } finally {
            Files.setPosixFilePermissions(locked, PosixFilePermissions.fromString("rwxr-xr-x"))
        }
    }

    @Test
    fun `unreadable file inside a readable directory is skipped`() {
        Files.copy(fixtures.resolve("audio.mp3"), temp.resolve("audio.mp3"))
        val locked = Files.copy(fixtures.resolve("audio.flac"), temp.resolve("locked.flac"))
        Files.setPosixFilePermissions(locked, PosixFilePermissions.fromString("---------"))
        try {
            if (Files.isReadable(locked)) return
            val scan = DesktopLibrary.scan(temp)
            assertEquals(listOf("audio.mp3"), scan.tracks.map { it.path.fileName.toString() })
            assertEquals(listOf("locked.flac"), scan.skipped.map { it.path.fileName.toString() })
        } finally {
            Files.setPosixFilePermissions(locked, PosixFilePermissions.fromString("rw-r--r--"))
        }
    }
}
