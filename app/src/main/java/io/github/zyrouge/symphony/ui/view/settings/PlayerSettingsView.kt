package io.github.zyrouge.symphony.ui.view.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.github.zyrouge.symphony.ui.components.BentoSettingsRow
import io.github.zyrouge.symphony.ui.components.BentoSettingsSheet
import io.github.zyrouge.symphony.ui.components.formatSettingSecs
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import kotlin.math.roundToInt

private val SEEK_RANGE = 3f..60f

/**
 * The Player settings (prototype v16 `player`) in the shared settings sheet:
 * playback, audio focus, headphones and the seek button lengths.
 */
@Composable
fun PlayerSettingsDialog(context: ViewContext, onDismissRequest: () -> Unit) {
    val settings = context.symphony.settings
    val t = context.symphony.t
    val fadePlayback by settings.fadePlayback.flow.collectAsState()
    val fadeDuration by settings.fadePlaybackDuration.flow.collectAsState()
    val requireAudioFocus by settings.requireAudioFocus.flow.collectAsState()
    val ignoreAudioFocusLoss by settings.ignoreAudioFocusLoss.flow.collectAsState()
    val playOnHeadphones by settings.playOnHeadphonesConnect.flow.collectAsState()
    val pauseOnHeadphones by settings.pauseOnHeadphonesDisconnect.flow.collectAsState()
    val seekBack by settings.seekBackDuration.flow.collectAsState()
    val seekForward by settings.seekForwardDuration.flow.collectAsState()
    val gapless by settings.gaplessPlayback.flow.collectAsState()

    BentoSettingsSheet(
        title = t.Player,
        doneLabel = t.Done,
        onDismissRequest = onDismissRequest,
        rows = listOf(
            BentoSettingsRow.Section(t.Playback),
            BentoSettingsRow.Switch(
                title = t.GaplessPlayback,
                checked = gapless,
                onCheckedChange = { settings.gaplessPlayback.setValue(it) },
            ),
            BentoSettingsRow.Switch(
                title = t.FadePlaybackInOut,
                checked = fadePlayback,
                onCheckedChange = { settings.fadePlayback.setValue(it) },
            ),
            BentoSettingsRow.Slider(
                title = t.Duration,
                value = fadeDuration,
                range = 0.5f..6f,
                step = 0.5f,
                label = { formatSettingSecs(it, t) },
                onChange = { settings.fadePlaybackDuration.setValue(it) },
                defaultValue = settings.fadePlaybackDuration.defaultValue,
                onReset = {
                    settings.fadePlaybackDuration.setValue(
                        settings.fadePlaybackDuration.defaultValue,
                    )
                },
            ),
            BentoSettingsRow.Section(t.AudioFocus),
            BentoSettingsRow.Switch(
                title = t.RequireAudioFocus,
                checked = requireAudioFocus,
                onCheckedChange = { settings.requireAudioFocus.setValue(it) },
            ),
            BentoSettingsRow.Switch(
                title = t.IgnoreAudioFocusLoss,
                checked = ignoreAudioFocusLoss,
                onCheckedChange = { settings.ignoreAudioFocusLoss.setValue(it) },
            ),
            BentoSettingsRow.Section(t.Headphones),
            BentoSettingsRow.Switch(
                title = t.PlayOnHeadphonesConnect,
                checked = playOnHeadphones,
                onCheckedChange = { settings.playOnHeadphonesConnect.setValue(it) },
            ),
            BentoSettingsRow.Switch(
                title = t.PauseOnHeadphonesDisconnect,
                checked = pauseOnHeadphones,
                onCheckedChange = { settings.pauseOnHeadphonesDisconnect.setValue(it) },
            ),
            BentoSettingsRow.Section(t.SeekButtons),
            BentoSettingsRow.Slider(
                title = t.FastRewindDuration,
                value = seekBack.toFloat(),
                range = SEEK_RANGE,
                step = 1f,
                label = { formatSettingSecs(it, t) },
                onChange = { settings.seekBackDuration.setValue(it.roundToInt()) },
                defaultValue = settings.seekBackDuration.defaultValue.toFloat(),
                onReset = {
                    settings.seekBackDuration.setValue(
                        settings.seekBackDuration.defaultValue,
                    )
                },
            ),
            BentoSettingsRow.Slider(
                title = t.FastForwardDuration,
                value = seekForward.toFloat(),
                range = SEEK_RANGE,
                step = 1f,
                label = { formatSettingSecs(it, t) },
                onChange = { settings.seekForwardDuration.setValue(it.roundToInt()) },
                defaultValue = settings.seekForwardDuration.defaultValue.toFloat(),
                onReset = {
                    settings.seekForwardDuration.setValue(
                        settings.seekForwardDuration.defaultValue,
                    )
                },
            ),
        ),
    )
}
