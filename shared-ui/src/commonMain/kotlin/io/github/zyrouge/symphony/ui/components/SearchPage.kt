package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.i18n.Strings
import io.github.zyrouge.symphony.ui.model.BentoSearchFilter
import io.github.zyrouge.symphony.ui.model.SearchHit
import io.github.zyrouge.symphony.ui.model.UiSong
import io.github.zyrouge.symphony.ui.model.UiSongSort
import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import io.github.zyrouge.symphony.ui.theme.bento.BentoSpacing
import io.github.zyrouge.symphony.ui.theme.bento.BentoWindowClass
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import io.github.zyrouge.symphony.ui.theme.bento.rememberBentoWindowClass

/**
 * The search page (DESIGN.md 5.5): a back button and a search field with the
 * sort menu, smart filters and genre chips, and the results tile. On wide
 * windows the filters move to a 320 dp sticky side column; elsewhere the
 * header, filters and results scroll as one list. The platform supplies the
 * query, the filtered results, the artwork and the playback callbacks.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchPage(
    strings: Strings,
    terms: String,
    onTermsChange: (String) -> Unit,
    focusRequester: FocusRequester,
    sortBy: UiSongSort,
    sortReverse: Boolean,
    onSortByChange: (UiSongSort) -> Unit,
    onSortReverseChange: (Boolean) -> Unit,
    filterCounts: Map<BentoSearchFilter, Int>,
    selectedFilter: BentoSearchFilter?,
    onFilterChange: (BentoSearchFilter?) -> Unit,
    genreNames: List<String>,
    selectedGenre: String?,
    onGenreChange: (String?) -> Unit,
    songs: List<UiSong>,
    hits: List<SearchHit>,
    ready: Boolean,
    narrowed: Boolean,
    playingSongId: String?,
    isPlaying: Boolean,
    nowMs: Long,
    durationText: (UiSong) -> String,
    artwork: @Composable (UiSong, Modifier) -> Unit,
    hitArtwork: @Composable (SearchHit, Modifier) -> Unit,
    onPlayAll: (List<UiSong>) -> Unit,
    onShuffleAll: (List<UiSong>) -> Unit,
    onSongClick: (UiSong) -> Unit,
    onHitClick: (SearchHit) -> Unit,
    modifier: Modifier = Modifier,
    menu: (@Composable (UiSong, Boolean, () -> Unit) -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    backContentDescription: String = strings.SearchYourMusic,
    autoFocus: Boolean = true,
    bottomInset: Dp = 0.dp,
) {
    val colors = LocalBentoColors.current
    var showSortMenu by remember { mutableStateOf(false) }
    var focused by remember { mutableStateOf(false) }
    val windowClass = rememberBentoWindowClass()

    val header: @Composable () -> Unit = {
        LaunchedEffect(Unit) {
            if (autoFocus && !focused) {
                focused = true
                focusRequester.requestFocus()
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = BentoSpacing.GUTTER_OTHER.dp,
                    end = BentoSpacing.GUTTER_OTHER.dp,
                    top = 8.dp,
                    bottom = 8.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowBack,
                        backContentDescription,
                        tint = colors.fg,
                    )
                }
            }
            SearchField(
                terms = terms,
                onTermsChange = onTermsChange,
                focusRequester = focusRequester,
                placeholder = strings.SearchYourMusic,
                compactPlaceholder = strings.Search,
                modifier = Modifier.weight(1f),
                trailing = {
                    Box {
                        IconButton(onClick = { showSortMenu = !showSortMenu }) {
                            Icon(
                                Icons.AutoMirrored.Rounded.Sort,
                                strings.SortSongsBy,
                                tint = colors.sub,
                            )
                        }
                        BentoSortMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false },
                            title = strings.SortSongsBy,
                            options = UiSongSort.entries.map {
                                BentoSortOption(it, it.label(strings))
                            },
                            selected = sortBy,
                            onSelect = onSortByChange,
                            reverse = sortReverse,
                            reverseLabel = strings.ReverseOrder,
                            onReverseChange = onSortReverseChange,
                            onValue = strings.On,
                            offValue = strings.Off,
                        )
                    }
                },
            )
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg),
    ) {
        val gutter = if (maxWidth < 600.dp) {
            BentoSpacing.GUTTER_COMPACT.dp
        } else {
            BentoSpacing.GUTTER_OTHER.dp
        }
        val tileGap = if (maxWidth < 600.dp) {
            BentoSpacing.GAP_COMPACT.dp
        } else {
            BentoSpacing.GAP_OTHER.dp
        }
        val innerWidth = maxWidth - gutter * 2
        val wide = innerWidth >= 720.dp && windowClass != BentoWindowClass.SHORT_HEIGHT

        when {
            wide -> Column(modifier = Modifier.fillMaxSize()) {
                header()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = gutter),
                    horizontalArrangement = Arrangement.spacedBy(tileGap),
                ) {
                    Column(
                        modifier = Modifier
                            .width(BentoSizes.SIDE_COLUMN.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        BentoSearchFilterTiles(
                            selectedFilter = selectedFilter,
                            filterCounts = filterCounts,
                            t = strings,
                            onFilterChange = onFilterChange,
                            sideColumn = true,
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            BentoGenreChips(
                                genreNames = genreNames,
                                selectedGenre = selectedGenre,
                                allGenresLabel = strings.AllGenres,
                                onGenreChange = onGenreChange,
                            )
                        }
                    }
                    BentoSearchResultsTile(
                        songs = songs,
                        ready = ready,
                        hits = hits,
                        selectedFilter = selectedFilter,
                        narrowed = narrowed,
                        playingSongId = playingSongId,
                        isPlaying = isPlaying,
                        nowMs = nowMs,
                        t = strings,
                        durationText = durationText,
                        moreContentDescription = strings.More,
                        artwork = artwork,
                        hitArtwork = hitArtwork,
                        menu = menu,
                        onPlayAll = { onPlayAll(songs) },
                        onShuffleAll = { onShuffleAll(songs) },
                        onSongClick = onSongClick,
                        onHitClick = onHitClick,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = bottomInset),
            ) {
                item(key = "header") { header() }
                item(key = "filters") {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = gutter)
                            .padding(bottom = tileGap),
                    ) {
                        BentoSearchFilterTiles(
                            selectedFilter = selectedFilter,
                            filterCounts = filterCounts,
                            t = strings,
                            onFilterChange = onFilterChange,
                            sideColumn = false,
                        )
                    }
                }
                item(key = "genres") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = gutter, end = gutter, bottom = tileGap)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        BentoGenreChips(
                            genreNames = genreNames,
                            selectedGenre = selectedGenre,
                            allGenresLabel = strings.AllGenres,
                            onGenreChange = onGenreChange,
                        )
                    }
                }
                bentoSearchResultsTileItems(
                    songs = songs,
                    ready = ready,
                    hits = hits,
                    selectedFilter = selectedFilter,
                    narrowed = narrowed,
                    playingSongId = playingSongId,
                    isPlaying = isPlaying,
                    nowMs = nowMs,
                    t = strings,
                    durationText = durationText,
                    moreContentDescription = strings.More,
                    artwork = artwork,
                    hitArtwork = hitArtwork,
                    menu = menu,
                    onPlayAll = { onPlayAll(songs) },
                    onShuffleAll = { onShuffleAll(songs) },
                    onSongClick = onSongClick,
                    onHitClick = onHitClick,
                )
            }
        }
    }
}
