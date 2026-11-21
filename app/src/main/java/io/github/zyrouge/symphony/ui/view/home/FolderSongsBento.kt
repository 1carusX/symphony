package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.components.bentoGestureBarPadding
import io.github.zyrouge.symphony.ui.components.BentoActionTile
import io.github.zyrouge.symphony.ui.components.BentoSongRow
import io.github.zyrouge.symphony.ui.components.BentoTile
import io.github.zyrouge.symphony.ui.components.BentoTileSizeClass
import io.github.zyrouge.symphony.ui.components.BentoTileTone
import io.github.zyrouge.symphony.ui.components.formatBentoDurationMinutes
import io.github.zyrouge.symphony.ui.components.rememberBentoPlayingSong
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import io.github.zyrouge.symphony.ui.theme.bento.rememberBentoWindowClass
import io.github.zyrouge.symphony.utils.SimpleFileSystem

@Composable
internal fun FolderSongsView(
    context: ViewContext,
    folder: SimpleFileSystem.Folder,
    onBack: () -> Unit,
) {
    val colors = LocalBentoColors.current
    val windowClass = rememberBentoWindowClass()
    val songIds by remember(folder) {
        derivedStateOf {
            folder.children.values.mapNotNull {
                when (it) {
                    is SimpleFileSystem.File -> it.data as String
                    else -> null
                }
            }
        }
    }
    val sortBy by context.symphony.settings.lastUsedSongsSortBy.flow.collectAsState()
    val sortReverse by context.symphony.settings.lastUsedSongsSortReverse.flow.collectAsState()
    val sortedSongIds by remember(folder, sortBy, sortReverse) {
        derivedStateOf {
            context.symphony.groove.song.sort(songIds, sortBy, sortReverse)
        }
    }
    val songs by remember(sortedSongIds) {
        derivedStateOf { context.symphony.groove.song.get(sortedSongIds) }
    }
    val playingSong = rememberBentoPlayingSong(context)
    val appFontFamily = MaterialTheme.typography.titleLarge.fontFamily
    val titleStyle = BentoTypography.title.copy(fontFamily = appFontFamily)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.bg),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 24.dp, top = 14.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowBack,
                        null,
                        tint = colors.fg,
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        folder.name,
                        style = titleStyle,
                        color = colors.fg,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    folder.parent?.let { parent ->
                        Text(
                            parent.fullPath.toString().removePrefix("root/primary:"),
                            style = BentoTypography.secondary,
                            color = colors.sub,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.weight(2f)) {
                        BentoActionTile(
                            icon = Icons.Filled.PlayArrow,
                            label = context.symphony.t.PlayAll,
                            sub = context.symphony.t.XSongs(songs.size.toString()) + " · " +
                                formatBentoDurationMinutes(songs.sumOf { it.duration }),
                            tone = BentoTileTone.ACCENT,
                            main = true,
                            narrow = windowClass ==
                                io.github.zyrouge.symphony.ui.theme.bento.BentoWindowClass.COMPACT,
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
                            narrow = windowClass ==
                                io.github.zyrouge.symphony.ui.theme.bento.BentoWindowClass.COMPACT,
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
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(BentoRadii.ROW.dp)),
                    ) {
                        songs.forEachIndexed { index, song ->
                            BentoSongRow(
                                context = context,
                                song = song,
                                playing = playingSong.songId == song.id,
                                isPlaying = playingSong.isPlaying,
                                onClick = {
                                    context.symphony.radio.shorty.playQueue(
                                        sortedSongIds,
                                        options = io.github.zyrouge.symphony.services.radio.Radio.PlayOptions(
                                            index = index,
                                        ),
                                    )
                                },
                            )
                        }
                    }
                }
                Box(Modifier.bentoGestureBarPadding())
            }
        }
    }
}
