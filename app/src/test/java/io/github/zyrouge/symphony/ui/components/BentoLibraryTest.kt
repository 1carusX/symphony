package io.github.zyrouge.symphony.ui.components

import io.github.zyrouge.symphony.services.groove.Album
import io.github.zyrouge.symphony.ui.theme.bento.BentoGridClass
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BentoLibraryTest {

    // -- letter grouping -----------------------------------------------------

    @Test
    fun `stripping a leading The is case-insensitive and keeps other words`() {
        assertEquals("Beatles", bentoStripLeadingThe("The Beatles"))
        assertEquals("beatles", bentoStripLeadingThe("the beatles"))
        // The article strips whatever its case (prototype: /^The\s+/i).
        assertEquals("Beatles", bentoStripLeadingThe("THE Beatles"))
        assertEquals("Them Song", bentoStripLeadingThe("Them Song"))
        assertEquals("No article", bentoStripLeadingThe("No article"))
        assertEquals("", bentoStripLeadingThe(""))
    }

    @Test
    fun `letters resolve from the sort key with everything else under hash`() {
        assertEquals("A", resolveBentoLetter("Around the World"))
        assertEquals("Z", resolveBentoLetter("zoo"))
        assertEquals("#", resolveBentoLetter("99 Luftballons"))
        assertEquals("#", resolveBentoLetter(""))
        assertEquals("#", resolveBentoLetter("Übel"))
    }

    @Test
    fun `entries group into consecutive letter runs`() {
        val entries = listOf("Alpha", "And", "Beta", "Beta 2", "alpha 2", "12 Songs")
        val groups = groupBentoEntriesByLetter(entries, lettered = true, sortKeyOf = { it })
        assertEquals(
            listOf(
                BentoLetterGroup("A", listOf("Alpha", "And")),
                BentoLetterGroup("B", listOf("Beta", "Beta 2")),
                // "alpha 2" reopens an A run after the B run closed.
                BentoLetterGroup("A", listOf("alpha 2")),
                BentoLetterGroup("#", listOf("12 Songs")),
            ),
            groups,
        )
    }

    @Test
    fun `non-lettered sorts keep one plain group`() {
        val entries = listOf("a", "b", "c")
        val groups = groupBentoEntriesByLetter(entries, lettered = false, sortKeyOf = { it })
        assertEquals(listOf(BentoLetterGroup(null, entries)), groups)
    }

    // -- scrubber -------------------------------------------------------------

    @Test
    fun `scrub skips empty letters forward then backward`() {
        val present = setOf("A", "M", "Z")
        assertEquals("A", resolveBentoScrubLetter(present, "A"))
        // Pressing an empty letter lands on the next one with songs.
        assertEquals("M", resolveBentoScrubLetter(present, "B"))
        // Past the last letter it walks back.
        assertEquals("Z", resolveBentoScrubLetter(present, "#"))
        assertEquals(null, resolveBentoScrubLetter(emptySet(), "A"))
    }

    // -- masonry and even grid -------------------------------------------------

    @Test
    fun `masonry assigns each tile to the shortest column`() {
        val heights = listOf(100f, 100f, 300f, 100f)
        val columns = distributeBentoMasonry(heights, columns = 2) { it }
        // 100 -> col 0, 100 -> col 1 (tied columns keep the first), 300 ->
        // col 0 (100 < 100 is false, first wins), 100 -> col 1 (100 < 400).
        assertEquals(
            listOf(listOf(100f, 300f), listOf(100f, 100f)),
            columns,
        )
    }

    @Test
    fun `masonry keeps a single column untouched`() {
        val entries = listOf(5f, 6f, 7f)
        val columns = distributeBentoMasonry(entries, columns = 1) { it }
        assertEquals(listOf(entries), columns)
    }

    @Test
    fun `even grid picks the column count with the fewest empty cells`() {
        // 4 groups, 3 columns: waste(3) = 5-4 = 1; waste(2) = 0 -> 2 columns.
        assertEquals(2, resolveBentoGroupColumnCount(4, 3))
        // 3 groups, 3 columns: waste(3) = 0, waste(2) = 1 -> stays 3.
        assertEquals(3, resolveBentoGroupColumnCount(3, 3))
        // 5 groups, 3 columns: waste(3) = 1, waste(2) = 1 -> tie keeps wider.
        assertEquals(3, resolveBentoGroupColumnCount(5, 3))
        // 7 groups, 3 columns: more than 2 x columns -> masonry over all 3.
        assertEquals(3, resolveBentoGroupColumnCount(7, 3))
        // One column stays one.
        assertEquals(1, resolveBentoGroupColumnCount(9, 1))
    }

    @Test
    fun `few groups form an even grid and many flow masonry`() {
        assertEquals(true, isBentoGroupEvenGrid(6, 3))
        assertEquals(false, isBentoGroupEvenGrid(7, 3))
        assertEquals(false, isBentoGroupEvenGrid(9, 1))
    }

    @Test
    fun `group tile heights count the visible rows`() {
        // 96 of chrome plus 44 dp per row; six rows when longer than six
        // songs (five songs plus the "Show all" row).
        assertEquals(96f + 3 * 44f, bentoGroupTileHeightDp(3))
        assertEquals(96f + 6 * 44f, bentoGroupTileHeightDp(6))
        assertEquals(96f + 6 * 44f, bentoGroupTileHeightDp(10))
        assertEquals(6, bentoGroupVisibleRowCount(7))
        assertEquals(2, bentoGroupVisibleRowCount(2))
    }

    // -- album placement ---------------------------------------------------------

    @Test
    fun `album tracks row span follows the appendix formula`() {
        // c4: ceil((62 + 48 + 12) / (84 + 12)) = ceil(122/96) = 2.
        assertEquals(2, bentoAlbumTracksRowSpan(1, BentoGridClass.C4))
        // c8, 9 tracks: ceil((62 + 432 + 16) / (112 + 16)) = ceil(510/128).
        assertEquals(4, bentoAlbumTracksRowSpan(9, BentoGridClass.C8))
        // c12, 30 tracks: ceil((62 + 1440 + 16) / (120 + 16)) = ceil(1518/136).
        assertEquals(12, bentoAlbumTracksRowSpan(30, BentoGridClass.C12))
    }

    // -- group ordering ------------------------------------------------------------

    private fun albumOf(name: String, vararg artists: String, year: Int? = null) = Album(
        id = name,
        name = name,
        artists = mutableSetOf(*artists),
        startYear = year,
        endYear = year,
        numberOfTracks = 1,
        duration = kotlin.time.Duration.ZERO,
    )

    @Test
    fun `albums order by artist then year with The stripped`() {
        val albums = listOf(
            albumOf("Later", "The Beatles", year = 1969),
            albumOf("Zebra", "Ada", year = 2020),
            albumOf("Early", "Beatles", year = 1963),
        )
        val sorted = albums.sortedWith(
            bentoAlbumsGroupComparator(
                artistNameOf = { it.artists.firstOrNull() },
                yearOf = { it.startYear },
                caseSensitive = false,
            ),
        )
        // "The Beatles" files under Beatles; Ada sorts before Beatles.
        assertEquals(
            listOf("Zebra", "Early", "Later"),
            sorted.map { it.name },
        )
    }

    @Test
    fun `albums without a year sort last within their artist`() {
        val albums = listOf(
            albumOf("Unknown", "Ada"),
            albumOf("Known", "Ada", year = 2000),
        )
        val sorted = albums.sortedWith(
            bentoAlbumsGroupComparator(
                artistNameOf = { it.artists.firstOrNull() },
                yearOf = { it.startYear },
                caseSensitive = false,
            ),
        )
        assertEquals(listOf("Known", "Unknown"), sorted.map { it.name })
    }

    @Test
    fun `names order with The stripped for artists genres and folders`() {
        val names = listOf("The XX", "Beta", "alpha")
        val sorted = names.sortedWith(
            bentoNamesGroupComparator(nameOf = { it }, caseSensitive = false),
        )
        assertEquals(listOf("alpha", "Beta", "The XX"), sorted)
    }

    // -- search helpers ----------------------------------------------------------

    @Test
    fun `queries match title artist and album case-insensitively`() {
        assertEquals(
            true,
            bentoSongMatchesQuery(
                title = "Night Swim",
                artists = setOf("Paper Lanterns"),
                album = "Night Swim LP",
                query = "night",
            ),
        )
        assertEquals(
            true,
            bentoSongMatchesQuery(
                title = "Night Swim",
                artists = setOf("Paper Lanterns"),
                album = "Night Swim LP",
                query = "LANTERNS",
            ),
        )
        assertEquals(
            true,
            bentoSongMatchesQuery(
                title = "Night Swim",
                artists = setOf("Paper Lanterns"),
                album = "Night Swim LP",
                query = "swim lp",
            ),
        )
        assertEquals(
            false,
            bentoSongMatchesQuery(
                title = "Night Swim",
                artists = setOf("Paper Lanterns"),
                album = "Night Swim LP",
                query = "morning",
            ),
        )
    }

    @Test
    fun `artist and album hits match names case-insensitively`() {
        assertEquals(true, bentoArtistNameMatchesQuery("Marisol Vega", "vega"))
        val album = albumOf("Low Tide Radio", "Marisol Vega")
        assertEquals(true, bentoAlbumMatchesQuery(album, "low tide"))
        assertEquals(true, bentoAlbumMatchesQuery(album, "marisol"))
        assertEquals(false, bentoAlbumMatchesQuery(album, "jazz"))
    }

    @Test
    fun `lossless detection uses the encoder and the file extension`() {
        assertEquals(
            true,
            isBentoLosslessSong(path = "/music/a.flac", encoder = "FLAC 1.4"),
        )
        assertEquals(
            true,
            isBentoLosslessSong(path = "/music/a.mp3", encoder = "FLAC reference lib"),
        )
        assertEquals(
            true,
            isBentoLosslessSong(path = "/music/a.wav", encoder = null),
        )
        assertEquals(
            false,
            isBentoLosslessSong(path = "/music/a.MP3", encoder = null),
        )
        assertEquals(
            false,
            isBentoLosslessSong(path = "/music/a.mp3", encoder = "LAME"),
        )
    }

    @Test
    fun `recently added covers thirty days and formats days ago`() {
        val day = 86_400_000L
        val now = 100 * day
        assertEquals(true, isBentoRecentlyAddedSong(now - 29 * day, now))
        assertEquals(false, isBentoRecentlyAddedSong(now - 31 * day, now))
        assertEquals("Today", formatBentoDaysAgo(now, now))
        assertEquals("3 d ago", formatBentoDaysAgo(now - 3 * day, now))
    }
}
