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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.services.groove.Playlist
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
fun RenamePlaylistDialog(
    context: ViewContext,
    playlist: Playlist,
    onRename: () -> Unit = {},
    onDismissRequest: () -> Unit,
) {
    var input by remember {
        mutableStateOf(TextFieldValue(playlist.title, TextRange(playlist.title.length)))
    }
    val focusRequester = remember { FocusRequester() }
    val title = input.text.trim()
    val canRename = title.isNotEmpty() && title != playlist.title

    fun rename() {
        if (!canRename) return
        onRename()
        onDismissRequest()
        context.symphony.groove.playlist.renamePlaylist(playlist, title)
    }

    BentoSheetDialog(
        title = context.symphony.t.RenamePlaylist,
        onDismissRequest = onDismissRequest,
        actions = {
            BentoSheetButton(
                label = context.symphony.t.Cancel,
                onClick = onDismissRequest,
            )
            BentoSheetButton(
                label = context.symphony.t.Rename,
                primary = true,
                enabled = canRename,
                onClick = ::rename,
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
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { rename() }),
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .focusRequester(focusRequester),
        )
    }
}
