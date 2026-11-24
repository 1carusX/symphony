package io.github.zyrouge.symphony.ui.view.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.github.zyrouge.symphony.ui.components.BentoSheetButton
import io.github.zyrouge.symphony.ui.components.BentoSheetDialog
import io.github.zyrouge.symphony.ui.components.BentoSheetSwitchRow
import io.github.zyrouge.symphony.ui.helpers.ViewContext

/**
 * The Now playing settings (prototype v16 `nowp`) in a sheet: seek buttons
 * beside play, the audio format line and keeping the screen on for lyrics.
 * A sheet rather than a page, so wide windows do not stretch the rows.
 */
@Composable
fun NowPlayingSettingsDialog(context: ViewContext, onDismissRequest: () -> Unit) {
    val settings = context.symphony.settings
    val seekControls by settings.nowPlayingSeekControls.flow.collectAsState()
    val additionalInfo by settings.nowPlayingAdditionalInfo.flow.collectAsState()
    val keepScreenAwake by settings.lyricsKeepScreenAwake.flow.collectAsState()
    val seekBack by settings.seekBackDuration.flow.collectAsState()
    val seekForward by settings.seekForwardDuration.flow.collectAsState()

    BentoSheetDialog(
        title = context.symphony.t.NowPlaying,
        onDismissRequest = onDismissRequest,
        actions = {
            BentoSheetButton(
                label = context.symphony.t.Done,
                primary = true,
                onClick = onDismissRequest,
            )
        },
    ) {
        BentoSheetSwitchRow(
            title = context.symphony.t.BackAndForwardButtons,
            subtitle = context.symphony.t.BesidePlayXBackYAhead(seekBack.toString(), seekForward.toString()),
            checked = seekControls,
            onCheckedChange = { settings.nowPlayingSeekControls.setValue(it) },
        )
        BentoSheetSwitchRow(
            title = context.symphony.t.AudioFormat,
            subtitle = context.symphony.t.AudioFormatSummary,
            checked = additionalInfo,
            onCheckedChange = { settings.nowPlayingAdditionalInfo.setValue(it) },
        )
        BentoSheetSwitchRow(
            title = context.symphony.t.KeepScreenOnForLyrics,
            subtitle = context.symphony.t.WhileLyricsShowing,
            checked = keepScreenAwake,
            onCheckedChange = { settings.lyricsKeepScreenAwake.setValue(it) },
        )
    }
}
