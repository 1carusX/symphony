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
import io.github.zyrouge.symphony.i18n.Strings

/**
 * Names a new playlist. [initialSongCount] is only used for the subtitle;
 * the platform creates the playlist in [onCreate] with the trimmed title.
 */
@Composable
fun BentoNewPlaylistDialog(
    strings: Strings,
    onCreate: (title: String) -> Unit,
    onDismissRequest: () -> Unit,
    initialSongCount: Int = 0,
) {
    var input by remember { mutableStateOf(TextFieldValue("")) }
    val focusRequester = remember { FocusRequester() }
    val title = input.text.trim()

    fun create() {
        if (title.isEmpty()) return
        onCreate(title)
    }

    BentoSheetDialog(
        title = strings.NewPlaylist,
        subtitle = initialSongCount
            .takeIf { it > 0 }
            ?.let { strings.XSongs(it.toString()) },
        onDismissRequest = onDismissRequest,
        actions = {
            BentoSheetButton(
                label = strings.Cancel,
                onClick = onDismissRequest,
            )
            BentoSheetButton(
                label = strings.Done,
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
            label = strings.Name,
            placeholder = strings.Playlist,
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
