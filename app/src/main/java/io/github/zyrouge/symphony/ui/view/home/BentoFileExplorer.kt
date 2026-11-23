package io.github.zyrouge.symphony.ui.view.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.services.radio.Radio
import io.github.zyrouge.symphony.ui.components.BentoSongRow
import io.github.zyrouge.symphony.ui.components.IconTextBody
import io.github.zyrouge.symphony.ui.components.SongDropdownMenu
import io.github.zyrouge.symphony.ui.components.bentoBottomInset
import io.github.zyrouge.symphony.ui.components.rememberBentoPlayingSong
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.helpers.navigateToFolder
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import io.github.zyrouge.symphony.utils.SimpleFileSystem
import io.github.zyrouge.symphony.utils.SimplePath

@Composable
fun BentoFileExplorer(
    context: ViewContext,
    key: Any?,
    initialPath: SimplePath?,
    explorer: SimpleFileSystem.Folder,
    onPathChange: (SimplePath) -> Unit,
) {
    var currentFolder by remember(key) {
        mutableStateOf(initialPath?.let { explorer.navigateToFolder(it) } ?: explorer)
    }
    // Paths are stored relative to the explorer root, as navigateToFolder reads them.
    LaunchedEffect(currentFolder) {
        onPathChange(SimplePath(currentFolder.fullPath.parts.drop(1)))
    }
    val sortBy by context.symphony.settings.lastUsedBrowserSortBy.flow.collectAsState()
    val sortReverse by context.symphony.settings.lastUsedBrowserSortReverse.flow.collectAsState()
    val playingSong = rememberBentoPlayingSong(context)
    val favorites by context.symphony.groove.playlist.favorites.collectAsState()

    val categorized = currentFolder.listedChildren()
    val folders = if (sortReverse) categorized.folders.sortedBy { it.name }.reversed()
    else categorized.folders.sortedBy { it.name }
    val songIds = context.symphony.groove.song
        .sort(categorized.files.keys.toList(), sortBy, sortReverse)
    val songs = songIds.mapNotNull { context.symphony.groove.song.get(it) }

    BackHandler(enabled = currentFolder.parent != null) {
        currentFolder.parent?.let { currentFolder = it }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        PathRow(
            rootLabel = context.symphony.t.LocalStorage,
            path = currentFolder.fullPath,
            onJump = { depth ->
                val path = SimplePath(currentFolder.fullPath.parts.subList(1, depth + 1))
                explorer.navigateToFolder(path)?.let { currentFolder = it }
            },
        )
        ActionRow(
            label = context.symphony.t.XFoldersYFiles(
                folders.size.toString(),
                songs.size.toString(),
            ),
            canPlay = songs.isNotEmpty(),
            onPlay = {
                context.symphony.radio.shorty.playQueue(songIds, shuffle = false)
            },
            onShuffle = {
                context.symphony.radio.shorty.playQueue(songIds, shuffle = true)
            },
        )
        when {
            folders.isEmpty() && songs.isEmpty() -> IconTextBody(
                icon = { modifier -> Icon(Icons.Filled.MusicNote, null, modifier = modifier) },
                content = { Text(context.symphony.t.DamnThisIsSoEmpty) },
            )

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 12.dp,
                    end = 12.dp,
                    bottom = bentoBottomInset(),
                ),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(folders, key = { it.fullPath.pathString }) { folder ->
                    val folderCount = folder.childFoldersCount
                    val songCount = folder.children.values.count { it is SimpleFileSystem.File }
                    FolderRow(
                        name = folder.name,
                        sub = listOfNotNull(
                            folderCount.takeIf { it > 0 }
                                ?.let { context.symphony.t.XFolders(it.toString()) },
                            context.symphony.t.XSongs(songCount.toString()),
                        ).joinToString(" · "),
                        onClick = { currentFolder = folder },
                    )
                }
                items(songs, key = { it.id }) { song ->
                    BentoSongRow(
                        context = context,
                        song = song,
                        playing = playingSong.songId == song.id,
                        isPlaying = playingSong.isPlaying,
                        menu = { expanded, onDismissRequest ->
                            SongDropdownMenu(
                                context = context,
                                song = song,
                                isFavorite = favorites.contains(song.id),
                                expanded = expanded,
                                onDismissRequest = onDismissRequest,
                            )
                        },
                        onClick = {
                            context.symphony.radio.shorty.playQueue(
                                songIds,
                                options = Radio.PlayOptions(index = songs.indexOf(song)),
                            )
                        },
                    )
                }
            }
        }
    }
}

/**
 * The breadcrumbs (the prototype's `fb-path`): 40 dp chips in a `surface`
 * tile, the current folder on `tonal`. The explorer root reads as
 * [rootLabel] and the primary volume prefix is dropped.
 */
@Composable
private fun PathRow(rootLabel: String, path: SimplePath, onJump: (Int) -> Unit) {
    val colors = LocalBentoColors.current
    val scrollState = rememberScrollState()
    LaunchedEffect(path.pathString, scrollState.maxValue) {
        scrollState.scrollTo(scrollState.maxValue)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(BentoRadii.TILE.dp))
            .background(colors.surface)
            .horizontalScroll(scrollState)
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        path.parts.forEachIndexed { index, part ->
            val current = index == path.parts.lastIndex
            Box(
                modifier = Modifier
                    .height(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (current) colors.tonal else Color.Transparent)
                    .clickable { onJump(index) }
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    when (index) {
                        0 -> rootLabel
                        1 -> part.removePrefix("primary:").ifEmpty { part }
                        else -> part
                    },
                    style = BentoTypography.body.copy(fontWeight = FontWeight.SemiBold),
                    color = if (current) colors.onTonal else colors.sub,
                    maxLines = 1,
                )
            }
            if (!current) {
                Text("/", style = BentoTypography.secondary, color = colors.muted)
            }
        }
    }
}

@Composable
private fun ActionRow(
    label: String,
    canPlay: Boolean,
    onPlay: () -> Unit,
    onShuffle: () -> Unit,
) {
    val colors = LocalBentoColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = BentoTypography.secondary,
            color = colors.sub,
            modifier = Modifier.weight(1f),
        )
        if (canPlay) {
            PillAction(icon = { Icon(Icons.Filled.PlayArrow, "Play all", tint = colors.fg) }, onClick = onPlay)
            PillAction(icon = { Icon(Icons.Filled.Shuffle, "Shuffle", tint = colors.fg) }, onClick = onShuffle)
        }
    }
}

@Composable
private fun PillAction(icon: @Composable () -> Unit, onClick: () -> Unit) {
    val colors = LocalBentoColors.current
    Box(
        modifier = Modifier
            .height(BentoSizes.TOUCH_TARGET_MIN.dp)
            .clip(RoundedCornerShape(BentoRadii.TILE_SMALL.dp))
            .background(colors.raised)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        icon()
    }
}

/**
 * A subfolder tile (the prototype's `fb-t`): 72 dp on `surface`, the folder
 * icon on `tonal`, the name and its folder and song counts.
 */
@Composable
private fun FolderRow(
    name: String,
    sub: String,
    onClick: () -> Unit,
) {
    val colors = LocalBentoColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(BentoSizes.ACTION_TILE.dp)
            .clip(RoundedCornerShape(BentoRadii.TILE_SMALL.dp))
            .background(colors.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(BentoRadii.ICON_WELL_40.dp))
                .background(colors.tonal),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Folder, null, tint = colors.onTonal, modifier = Modifier.size(22.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                name,
                style = BentoTypography.rowTitle,
                color = colors.fg,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                sub,
                style = BentoTypography.secondary,
                color = colors.sub,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private data class ExplorerChildren(
    val folders: List<SimpleFileSystem.Folder>,
    val files: Map<String, SimpleFileSystem.File>,
)

private fun SimpleFileSystem.Folder.listedChildren() = ExplorerChildren(
    folders = children.values.mapNotNull { it as? SimpleFileSystem.Folder },
    files = children.values
        .mapNotNull { it as? SimpleFileSystem.File }
        .associateBy { it.data as String },
)
