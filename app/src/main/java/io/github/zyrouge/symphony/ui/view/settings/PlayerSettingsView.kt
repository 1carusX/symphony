package io.github.zyrouge.symphony.ui.view.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.components.BentoSheetButton
import io.github.zyrouge.symphony.ui.components.BentoSheetDialog
import io.github.zyrouge.symphony.ui.components.BentoSheetSliderRow
import io.github.zyrouge.symphony.ui.components.BentoSheetSwitchRow
import io.github.zyrouge.symphony.ui.components.formatSettingSecs
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import kotlin.math.roundToInt

private val SEEK_RANGE = 3f..60f

/**
 * The Player settings (prototype v16 `player`) in a sheet: playback, audio
 * focus, headphones and the seek button lengths. A sheet rather than a page,
 * so wide windows do not stretch the rows. Durations are set in place.
 */
@Composable
fun PlayerSettingsDialog(context: ViewContext, onDismissRequest: () -> Unit) {
    val settings = context.symphony.settings
    val fadePlayback by settings.fadePlayback.flow.collectAsState()
    val fadeDuration by settings.fadePlaybackDuration.flow.collectAsState()
    val requireAudioFocus by settings.requireAudioFocus.flow.collectAsState()
    val ignoreAudioFocusLoss by settings.ignoreAudioFocusLoss.flow.collectAsState()
    val playOnHeadphones by settings.playOnHeadphonesConnect.flow.collectAsState()
    val pauseOnHeadphones by settings.pauseOnHeadphonesDisconnect.flow.collectAsState()
    val seekBack by settings.seekBackDuration.flow.collectAsState()
    val seekForward by settings.seekForwardDuration.flow.collectAsState()
    val gapless by settings.gaplessPlayback.flow.collectAsState()

    BentoSheetDialog(
        title = context.symphony.t.Player,
        onDismissRequest = onDismissRequest,
        actions = {
            BentoSheetButton(
                label = context.symphony.t.Done,
                primary = true,
                onClick = onDismissRequest,
            )
        },
    ) {
        SettingsSheetSectionLabel(context.symphony.t.Playback)
        BentoSheetSwitchRow(
            title = context.symphony.t.GaplessPlayback,
            checked = gapless,
            onCheckedChange = { settings.gaplessPlayback.setValue(it) },
        )
        BentoSheetSwitchRow(
            title = context.symphony.t.FadePlaybackInOut,
            checked = fadePlayback,
            onCheckedChange = { settings.fadePlayback.setValue(it) },
        )
        BentoSheetSliderRow(
            title = context.symphony.t.Duration,
            value = fadeDuration,
            range = 0.5f..6f,
            step = 0.5f,
            label = { formatSettingSecs(it, context.symphony.t) },
            onChange = { settings.fadePlaybackDuration.setValue(it) },
            defaultValue = settings.fadePlaybackDuration.defaultValue,
            onReset = {
                settings.fadePlaybackDuration.setValue(settings.fadePlaybackDuration.defaultValue)
            },
        )
        SettingsSheetSectionLabel(context.symphony.t.AudioFocus)
        BentoSheetSwitchRow(
            title = context.symphony.t.RequireAudioFocus,
            checked = requireAudioFocus,
            onCheckedChange = { settings.requireAudioFocus.setValue(it) },
        )
        BentoSheetSwitchRow(
            title = context.symphony.t.IgnoreAudioFocusLoss,
            checked = ignoreAudioFocusLoss,
            onCheckedChange = { settings.ignoreAudioFocusLoss.setValue(it) },
        )
        SettingsSheetSectionLabel(context.symphony.t.Headphones)
        BentoSheetSwitchRow(
            title = context.symphony.t.PlayOnHeadphonesConnect,
            checked = playOnHeadphones,
            onCheckedChange = { settings.playOnHeadphonesConnect.setValue(it) },
        )
        BentoSheetSwitchRow(
            title = context.symphony.t.PauseOnHeadphonesDisconnect,
            checked = pauseOnHeadphones,
            onCheckedChange = { settings.pauseOnHeadphonesDisconnect.setValue(it) },
        )
        SettingsSheetSectionLabel(context.symphony.t.SeekButtons)
        BentoSheetSliderRow(
            title = context.symphony.t.FastRewindDuration,
            value = seekBack.toFloat(),
            range = SEEK_RANGE,
            step = 1f,
            label = { formatSettingSecs(it, context.symphony.t) },
            onChange = { settings.seekBackDuration.setValue(it.roundToInt()) },
            defaultValue = settings.seekBackDuration.defaultValue.toFloat(),
            onReset = { settings.seekBackDuration.setValue(settings.seekBackDuration.defaultValue) },
        )
        BentoSheetSliderRow(
            title = context.symphony.t.FastForwardDuration,
            value = seekForward.toFloat(),
            range = SEEK_RANGE,
            step = 1f,
            label = { formatSettingSecs(it, context.symphony.t) },
            onChange = { settings.seekForwardDuration.setValue(it.roundToInt()) },
            defaultValue = settings.seekForwardDuration.defaultValue.toFloat(),
            onReset = { settings.seekForwardDuration.setValue(settings.seekForwardDuration.defaultValue) },
        )
    }
}

/** A small heading between groups of rows in a settings sheet. */
@Composable
internal fun SettingsSheetSectionLabel(text: String) {
    Text(
        text,
        style = BentoTypography.secondary.copy(fontWeight = FontWeight.SemiBold),
        color = LocalBentoColors.current.sub,
        modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 2.dp),
    )
}
