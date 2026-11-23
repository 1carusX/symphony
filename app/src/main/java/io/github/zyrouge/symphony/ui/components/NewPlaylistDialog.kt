package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.services.groove.Playlist
import io.github.zyrouge.symphony.ui.helpers.ViewContext

/**
 * Names a new playlist. It starts with [initialSongIds]; the subtitle says
 * how many.
 */
@Composable
fun NewPlaylistDialog(
    context: ViewContext,
    initialSongIds: List<String> = listOf(),
    onDone: (Playlist) -> Unit,
    onDismissRequest: () -> Unit,
) {
    var input by remember { mutableStateOf(TextFieldValue("")) }
    val focusRequester = remember { FocusRequester() }
    val title = input.text.trim()

    fun create() {
        if (title.isEmpty()) return
        val playlist = context.symphony.groove.playlist.create(
            title = title,
            songIds = initialSongIds,
        )
        onDone(playlist)
    }

    BentoSheetDialog(
        title = context.symphony.t.NewPlaylist,
        subtitle = initialSongIds
            .takeIf { it.isNotEmpty() }
            ?.let { context.symphony.t.XSongs(it.size.toString()) },
        onDismissRequest = onDismissRequest,
        actions = {
            BentoSheetButton(
                label = context.symphony.t.Cancel,
                onClick = onDismissRequest,
            )
            BentoSheetButton(
                label = context.symphony.t.Done,
                primary = true,
                enabled = title.isNotEmpty(),
                onClick = ::create,
            )
        },
    ) {
        // Inside the dialog's composition, so the field is attached when it runs.
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
        BentoSheetTextField(
            value = input,
            onValueChange = { input = it },
            label = context.symphony.t.Name,
            placeholder = context.symphony.t.Playlist,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { create() }),
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .focusRequester(focusRequester),
        )
    }
}
