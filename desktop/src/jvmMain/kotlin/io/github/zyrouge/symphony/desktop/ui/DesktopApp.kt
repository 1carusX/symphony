package io.github.zyrouge.symphony.desktop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.desktop.library.DesktopLibrary
import io.github.zyrouge.symphony.desktop.library.DesktopTrack
import io.github.zyrouge.symphony.desktop.player.PlaybackFailure
import io.github.zyrouge.symphony.desktop.player.PlaybackState
import io.github.zyrouge.symphony.desktop.player.VlcjAudioPlayer
import kotlinx.coroutines.delay
import java.awt.Window
import java.nio.file.Path
import javax.swing.JFileChooser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesktopApp(window: Window, initialDirectory: Path? = null) {
    val playerState = remember { mutableStateOf<PlaybackState>(PlaybackState.Idle) }
    val player = remember { VlcjAudioPlayer(onStateChanged = { playerState.value = it }) }
    val tracks = remember { mutableStateOf<List<DesktopTrack>>(emptyList()) }
    val status = remember { mutableStateOf("Choose a folder to load your music") }
    val positionMs = remember { mutableStateOf(0L) }
    val durationMs = remember { mutableStateOf(0L) }
    val volume = remember { mutableStateOf(80) }

    fun load(directory: Path) {
        status.value = "Scanning $directory"
        Thread {
            val scan = DesktopLibrary.scan(directory)
            tracks.value = scan.tracks
            status.value = buildString {
                append(directory)
                append(" - ")
                append(scan.tracks.size)
                append(" tracks")
                if (scan.skippedCount > 0) {
                    append(", ")
                    append(scan.skippedCount)
                    append(" skipped")
                }
                scan.unavailableReason?.let {
                    append(" (")
                    append(it)
                    append(")")
                }
            }
        }.apply { isDaemon = true }.start()
    }

    fun chooseFolder() {
        val chooser = JFileChooser().apply {
            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            dialogTitle = "Choose music folder"
        }
        if (chooser.showOpenDialog(window) == JFileChooser.APPROVE_OPTION) {
            load(chooser.selectedFile.toPath())
        }
    }

    LaunchedEffect(initialDirectory) {
        initialDirectory?.let { load(it) }
    }

    LaunchedEffect(player) {
        player.setVolume(volume.value)
        while (true) {
            positionMs.value = player.positionMs()
            durationMs.value = player.durationMs()
            delay(250)
        }
    }

    DisposableEffect(player) {
        onDispose { player.close() }
    }

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Symphony Desktop") },
                    actions = {
                        TextButton(onClick = { chooseFolder() }) { Text("Open folder") }
                    },
                )
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface),
            ) {
                Text(
                    text = status.value,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
                HorizontalDivider()
                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    items(tracks.value, key = { it.fields.id }) { track ->
                        TrackRow(
                            track = track,
                            selected = currentPath(playerState.value) == track.path,
                            onClick = { player.play(track.path) },
                        )
                    }
                }
                HorizontalDivider()
                NowPlayingBar(
                    state = playerState.value,
                    positionMs = positionMs.value,
                    durationMs = durationMs.value,
                    volume = volume.value,
                    onTogglePlay = {
                        when (val state = playerState.value) {
                            is PlaybackState.Playing -> player.pause()
                            is PlaybackState.Paused -> player.resume()
                            is PlaybackState.Loading -> Unit
                            is PlaybackState.Ended -> player.play(state.path)
                            is PlaybackState.Failed -> {
                                val target = state.path ?: tracks.value.firstOrNull()?.path
                                if (target != null) player.play(target)
                            }
                            PlaybackState.Idle -> tracks.value.firstOrNull()?.let { player.play(it.path) }
                        }
                    },
                    onSeek = { player.seekTo(it) },
                    onVolume = {
                        volume.value = it
                        player.setVolume(it)
                    },
                )
            }
        }
    }
}

@Composable
private fun TrackRow(track: DesktopTrack, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
                },
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(track.fields.title, fontWeight = FontWeight.Medium)
            Text(
                text = listOfNotNull(
                    track.fields.artists.takeIf { it.isNotEmpty() }?.joinToString(", "),
                    track.fields.album,
                ).joinToString(" - "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = formatTime(track.fields.duration),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun NowPlayingBar(
    state: PlaybackState,
    positionMs: Long,
    durationMs: Long,
    volume: Int,
    onTogglePlay: () -> Unit,
    onSeek: (Long) -> Unit,
    onVolume: (Int) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text(
            text = describe(state),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
        Spacer(modifier = Modifier.padding(top = 8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = onTogglePlay) {
                Text(
                    when (state) {
                        is PlaybackState.Playing -> "Pause"
                        is PlaybackState.Paused -> "Resume"
                        else -> "Play"
                    },
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(formatTime(positionMs), style = MaterialTheme.typography.bodySmall)
            Slider(
                value = positionMs.coerceIn(0L, durationMs.coerceAtLeast(0L)).toFloat(),
                onValueChange = { onSeek(it.toLong()) },
                valueRange = 0f..durationMs.coerceAtLeast(1L).toFloat(),
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
            )
            Text(formatTime(durationMs), style = MaterialTheme.typography.bodySmall)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Volume", style = MaterialTheme.typography.bodySmall)
            Slider(
                value = volume.toFloat(),
                onValueChange = { onVolume(it.toInt()) },
                valueRange = 0f..100f,
                modifier = Modifier.width(200.dp).padding(start = 12.dp),
            )
        }
    }
}

private fun currentPath(state: PlaybackState): Path? = when (state) {
    PlaybackState.Idle -> null
    is PlaybackState.Loading -> state.path
    is PlaybackState.Playing -> state.path
    is PlaybackState.Paused -> state.path
    is PlaybackState.Ended -> state.path
    is PlaybackState.Failed -> state.path
}

private fun describe(state: PlaybackState): String = when (state) {
    PlaybackState.Idle -> "Nothing playing"
    is PlaybackState.Loading -> "Loading ${state.path.fileName}"
    is PlaybackState.Playing -> "Playing ${state.path.fileName}"
    is PlaybackState.Paused -> "Paused ${state.path.fileName}"
    is PlaybackState.Ended -> "Ended ${state.path.fileName}"
    is PlaybackState.Failed -> when (state.reason) {
        PlaybackFailure.BackendUnavailable -> "LibVLC is not installed"
        PlaybackFailure.FileNotFound -> "File not found"
        PlaybackFailure.Unreadable -> "File is not readable"
        PlaybackFailure.UnsupportedFormat -> "Unsupported format"
        PlaybackFailure.PlaybackError -> "Playback failed"
    }
}

private fun formatTime(milliseconds: Long): String {
    if (milliseconds <= 0L) return "0:00"
    val totalSeconds = milliseconds / 1000L
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "%d:%02d".format(minutes, seconds)
}
