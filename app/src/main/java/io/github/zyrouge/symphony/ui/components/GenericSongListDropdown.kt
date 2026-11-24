package io.github.zyrouge.symphony.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
fun GenericSongListDropdown(
    context: ViewContext,
    songIds: List<String>,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    header: (@Composable () -> Unit)? = null,
    source: String? = null,
) {
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }

    BentoMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        header = header,
    ) {
        BentoQueueMenuItems(
            context = context,
            orderedSongIds = { songIds },
            source = source,
            onDismissRequest = onDismissRequest,
            onAddToPlaylist = { showAddToPlaylistDialog = true },
        )
    }

    if (showAddToPlaylistDialog) {
        AddToPlaylistDialog(
            context,
            songIds = songIds,
            onDismissRequest = {
                showAddToPlaylistDialog = false
            }
        )
    }
}
