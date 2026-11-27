package io.github.zyrouge.symphony.ui.view.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.github.zyrouge.symphony.ui.components.BentoSettingsRow
import io.github.zyrouge.symphony.ui.components.BentoSettingsSheet
import io.github.zyrouge.symphony.ui.helpers.ViewContext

/**
 * The Now playing settings (prototype v16 `nowp`) in the shared settings
 * sheet: seek buttons beside play, the audio format line and keeping the
 * screen on for lyrics.
 */
@Composable
fun NowPlayingSettingsDialog(context: ViewContext, onDismissRequest: () -> Unit) {
    val settings = context.symphony.settings
    val t = context.symphony.t
    val seekControls by settings.nowPlayingSeekControls.flow.collectAsState()
    val additionalInfo by settings.nowPlayingAdditionalInfo.flow.collectAsState()
    val keepScreenAwake by settings.lyricsKeepScreenAwake.flow.collectAsState()
    val seekBack by settings.seekBackDuration.flow.collectAsState()
    val seekForward by settings.seekForwardDuration.flow.collectAsState()

    BentoSettingsSheet(
        title = t.NowPlaying,
        doneLabel = t.Done,
        onDismissRequest = onDismissRequest,
        rows = listOf(
            BentoSettingsRow.Switch(
                title = t.BackAndForwardButtons,
                subtitle = t.BesidePlayXBackYAhead(
                    seekBack.toString(),
                    seekForward.toString(),
                ),
                checked = seekControls,
                onCheckedChange = { settings.nowPlayingSeekControls.setValue(it) },
            ),
            BentoSettingsRow.Switch(
                title = t.AudioFormat,
                subtitle = t.AudioFormatSummary,
                checked = additionalInfo,
                onCheckedChange = { settings.nowPlayingAdditionalInfo.setValue(it) },
            ),
            BentoSettingsRow.Switch(
                title = t.KeepScreenOnForLyrics,
                subtitle = t.WhileLyricsShowing,
                checked = keepScreenAwake,
                onCheckedChange = { settings.lyricsKeepScreenAwake.setValue(it) },
            ),
        ),
    )
}
