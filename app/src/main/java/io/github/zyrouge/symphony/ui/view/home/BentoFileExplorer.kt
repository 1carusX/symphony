package io.github.zyrouge.symphony.ui.view.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.services.radio.Radio
import io.github.zyrouge.symphony.services.groove.repositories.SongRepository
import io.github.zyrouge.symphony.ui.components.BentoSongRow
import io.github.zyrouge.symphony.ui.components.IconTextBody
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
    val sortBy by context.symphony.settings.lastUsedBrowserSortBy.flow.collectAsState()
    val sortReverse by context.symphony.settings.lastUsedBrowserSortReverse.flow.collectAsState()
    val playingSong = rememberBentoPlayingSong(context)

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
                    FolderRow(
                        name = folder.name,
                        count = folder.children.size,
                        onClick = { currentFolder = folder },
                    )
                }
                items(songs, key = { it.id }) { song ->
                    BentoSongRow(
                        context = context,
                        song = song,
                        playing = playingSong.songId == song.id,
                        isPlaying = playingSong.isPlaying,
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

@Composable
fun BentoTreeExplorer(
    context: ViewContext,
    key: Any?,
    explorer: SimpleFileSystem.Folder,
    disabledPaths: Set<String>,
) {
    var expanded by remember(key) { mutableStateOf(disabledPaths) }
    val playingSong = rememberBentoPlayingSong(context)

    fun toggle(folder: SimpleFileSystem.Folder) {
        val path = folder.fullPath.pathString
        expanded = if (expanded.contains(path)) expanded - path else expanded + path
    }

    val rows = mutableListOf<Any>()
    fun walk(folder: SimpleFileSystem.Folder, depth: Int) {
        val categorized = folder.listedChildren()
        categorized.folders.sortedBy { it.name }.forEach {
            val isOpen = it.fullPath.pathString !in expanded
            rows.add(Triple(it, depth, isOpen))
            if (isOpen) walk(it, depth + 1)
        }
        context.symphony.groove.song
            .sort(categorized.files.keys.toList(), SongRepository.SortBy.TITLE, false)
            .mapNotNull { context.symphony.groove.song.get(it) }
            .forEach { rows.add(Triple(it, depth, false)) }
    }
    walk(explorer, 0)

    if (rows.isEmpty()) {
        IconTextBody(
            icon = { modifier -> Icon(Icons.Filled.MusicNote, null, modifier = modifier) },
            content = { Text(context.symphony.t.DamnThisIsSoEmpty) },
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 12.dp,
            end = 12.dp,
            bottom = bentoBottomInset(),
        ),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(rows) { row ->
            val entry = row as Triple<*, Int, Boolean>
            when (val value = entry.first) {
                is SimpleFileSystem.Folder -> FolderRow(
                    name = value.name,
                    count = value.children.size,
                    depth = entry.second,
                    onClick = { toggle(value) },
                )

                is Song -> Row(modifier = Modifier.padding(start = (entry.second * 16).dp)) {
                    BentoSongRow(
                        context = context,
                        song = value,
                        playing = playingSong.songId == value.id,
                        isPlaying = playingSong.isPlaying,
                        onClick = {
                            context.symphony.radio.shorty.playQueue(value.id)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun PathRow(path: SimplePath, onJump: (Int) -> Unit) {
    val colors = LocalBentoColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        path.parts.forEachIndexed { index, part ->
            Text(
                part,
                style = BentoTypography.secondary,
                color = if (index == path.parts.lastIndex) colors.fg else colors.sub,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .clip(RoundedCornerShape(BentoRadii.TILE_SMALL.dp))
                    .clickable { onJump(index) }
                    .padding(horizontal = 6.dp, vertical = 4.dp),
            )
            if (index != path.parts.lastIndex) {
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

@Composable
private fun FolderRow(
    name: String,
    count: Int,
    depth: Int = 0,
    onClick: () -> Unit,
) {
    val colors = LocalBentoColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (depth * 16).dp)
            .height(BentoSizes.SONG_ROW.dp)
            .clip(RoundedCornerShape(BentoRadii.ROW.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(BentoRadii.SONG_ART_44.dp))
                .background(colors.raised)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Folder, null, tint = colors.sub)
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
                count.toString(),
                style = BentoTypography.secondary,
                color = colors.sub,
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
