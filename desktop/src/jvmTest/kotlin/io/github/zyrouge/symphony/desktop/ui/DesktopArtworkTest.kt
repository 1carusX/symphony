package io.github.zyrouge.symphony.desktop.ui

import io.github.zyrouge.symphony.desktop.player.repoRoot
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class DesktopArtworkTest {
    @Test
    fun `loads embedded artwork from the shared audio fixture`() {
        val artwork = loadArtwork(repoRoot().resolve("app/src/test/resources/audio.flac"))
        assertNotNull(artwork)
    }
}
