package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.utils.DurationUtils

/**
 * Picks a playlist's songs: a search field, then every song with a
 * checkbox, the ticked ones first. Cancel, a scrim tap and Back hand back
 * [selectedSongIds] unchanged; Done hands back the new selection.
 */
@Composable
fun PlaylistManageSongsDialog(
    context: ViewContext,
    selectedSongIds: List<String>,
    onDone: (List<String>) -> Unit,
) {
    val allSongIds by context.symphony.groove.song.all.collectAsState()
    val nSelectedSongIds = remember { selectedSongIds.toMutableStateList() }
    var terms by remember { mutableStateOf(TextFieldValue("")) }
    val initiallySelected = remember(selectedSongIds) { selectedSongIds.toHashSet() }
    val songIds by remember(allSongIds, terms.text, initiallySelected) {
        derivedStateOf {
            context.symphony.groove.song.search(allSongIds, terms.text, limit = -1)
                .map { it.entity }
                .sortedBy { !initiallySelected.contains(it) }
        }
    }

    BentoSheetDialog(
        title = context.symphony.t.ManageSongs,
        subtitle = context.symphony.t.XSongs(nSelectedSongIds.size.toString()),
        onDismissRequest = { onDone(selectedSongIds) },
        scrollable = false,
        tall = true,
        actions = {
            BentoSheetButton(
                label = context.symphony.t.Cancel,
                onClick = { onDone(selectedSongIds) },
            )
            BentoSheetButton(
                label = context.symphony.t.Done,
                primary = true,
                onClick = { onDone(nSelectedSongIds.toList()) },
            )
        },
    ) {
        BentoSheetTextField(
            value = terms,
            onValueChange = { terms = it },
            placeholder = context.symphony.t.SearchYourMusic,
            leadingIcon = Icons.Rounded.Search,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        when {
            songIds.isEmpty() -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                SubtleCaptionText(context.symphony.t.DamnThisIsSoEmpty)
            }

            else -> LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(songIds) { songId ->
                    context.symphony.groove.song.get(songId)?.let { song ->
                        BentoSheetCheckRow(
                            title = song.title,
                            subtitle = (song.artists.toList() + DurationUtils.formatMs(song.duration))
                                .joinToString(" · "),
                            checked = nSelectedSongIds.contains(song.id),
                            onCheckedChange = { checked ->
                                when {
                                    checked -> nSelectedSongIds.add(song.id)
                                    else -> nSelectedSongIds.remove(song.id)
                                }
                            },
                            leading = {
                                BentoSheetArt(
                                    song.createArtworkImageRequest(context.symphony).build(),
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}
