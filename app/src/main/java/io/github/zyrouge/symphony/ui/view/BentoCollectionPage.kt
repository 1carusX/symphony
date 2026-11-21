package io.github.zyrouge.symphony.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.services.radio.Radio
import io.github.zyrouge.symphony.ui.components.bentoGestureBarPadding
import io.github.zyrouge.symphony.ui.components.BentoActionTile
import io.github.zyrouge.symphony.ui.components.BentoSongRow
import io.github.zyrouge.symphony.ui.components.BentoTile
import io.github.zyrouge.symphony.ui.components.BentoTileSizeClass
import io.github.zyrouge.symphony.ui.components.BentoTileTone
import io.github.zyrouge.symphony.ui.components.formatBentoDurationMinutes
import io.github.zyrouge.symphony.ui.components.rememberBentoPlayingSong
import io.github.zyrouge.symphony.ui.components.BentoPageHeader
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import io.github.zyrouge.symphony.ui.theme.bento.rememberBentoWindowClass

/**
 * A bento page for a collection of songs (artist, playlist, genre, folder):
 * back and title header, play and shuffle action tiles, and one tile holding
 * the sorted song rows. Sorting follows the songs page settings.
 */
@Composable
fun BentoCollectionPage(
    context: ViewContext,
    title: String,
    subtitle: String?,
    songIds: List<String>,
    onBack: () -> Unit,
    emptyMessage: String,
    modifier: Modifier = Modifier,
    trailingMenu: (@Composable () -> Unit)? = null,
    leading: (@Composable () -> Unit)? = null,
    rowMenu: (@Composable androidx.compose.foundation.layout.RowScope.(io.github.zyrouge.symphony.services.groove.Song) -> Unit)? = null,
) {
    val colors = LocalBentoColors.current
    val windowClass = rememberBentoWindowClass()
    val appFontFamily = MaterialTheme.typography.titleLarge.fontFamily
    val sortBy by context.symphony.settings.lastUsedSongsSortBy.flow.collectAsState()
    val sortReverse by context.symphony.settings.lastUsedSongsSortReverse.flow.collectAsState()
    val sortedSongIds by remember(songIds, sortBy, sortReverse) {
        derivedStateOf {
            context.symphony.groove.song.sort(songIds, sortBy, sortReverse)
        }
    }
    val songs by remember(sortedSongIds) {
        derivedStateOf { context.symphony.groove.song.get(sortedSongIds) }
    }
    val playingSong = rememberBentoPlayingSong(context)
    val compact = windowClass == io.github.zyrouge.symphony.ui.theme.bento.BentoWindowClass.COMPACT

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding(),
        ) {
            BentoPageHeader(
                title = title,
                subtitle = subtitle,
                windowClass = windowClass,
                onBack = onBack,
                trailing = trailingMenu,
            )
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (songIds.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(
                                Icons.Filled.MusicNote,
                                null,
                                tint = colors.sub,
                                modifier = Modifier.size(28.dp),
                            )
                            Text(
                                emptyMessage,
                                style = BentoTypography.rowTitle,
                                color = colors.sub,
                            )
                        }
                    }
                } else {
                    leading?.invoke()
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.weight(2f)) {
                            BentoActionTile(
                                icon = Icons.Filled.PlayArrow,
                                label = context.symphony.t.PlayAll,
                                sub = context.symphony.t.XSongs(songs.size.toString()) + " · " +
                                    formatBentoDurationMinutes(songs.sumOf { it.duration }),
                                tone = BentoTileTone.ACCENT,
                                main = true,
                                narrow = compact,
                                onClick = {
                                    context.symphony.radio.shorty.playQueue(sortedSongIds)
                                },
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            BentoActionTile(
                                icon = Icons.Filled.Shuffle,
                                label = context.symphony.t.ShufflePlay,
                                sub = "Random order",
                                tone = BentoTileTone.TONAL,
                                narrow = compact,
                                onClick = {
                                    context.symphony.radio.shorty.playQueue(
                                        sortedSongIds,
                                        shuffle = true,
                                    )
                                },
                            )
                        }
                    }
                    BentoTile(
                        tone = BentoTileTone.PLAIN,
                        sizeClass = BentoTileSizeClass.L,
                        contentPadding = 8.dp,
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            songs.forEachIndexed { index, song ->
                                BentoSongRow(
                                    context = context,
                                    song = song,
                                    playing = playingSong.songId == song.id,
                                    isPlaying = playingSong.isPlaying,
                                    trailing = when {
                                        rowMenu == null -> null
                                        else -> {
                                            { rowMenu.invoke(this, song) }
                                        }
                                    },
                                    onClick = {
                                        context.symphony.radio.shorty.playQueue(
                                            sortedSongIds,
                                            options = Radio.PlayOptions(index = index),
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
                Box(Modifier.bentoGestureBarPadding())
            }
        }
    }
}
