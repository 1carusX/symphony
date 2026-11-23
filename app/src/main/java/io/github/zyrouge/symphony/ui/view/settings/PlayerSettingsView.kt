package io.github.zyrouge.symphony.ui.view.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.components.BentoSheetActionRow
import io.github.zyrouge.symphony.ui.components.BentoSheetButton
import io.github.zyrouge.symphony.ui.components.BentoSheetDialog
import io.github.zyrouge.symphony.ui.components.BentoSheetSwitchRow
import io.github.zyrouge.symphony.ui.components.BentoSliderDialog
import io.github.zyrouge.symphony.ui.components.formatSettingSecs
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import kotlin.math.roundToInt

private enum class PlayerSettingPopup {
    FADE_DURATION,
    SEEK_BACK,
    SEEK_FORWARD,
}

private val SEEK_RANGE = 3f..60f

/**
 * The Player settings (prototype v16 `player`) in a sheet: playback, audio
 * focus, headphones and the seek button lengths. A sheet rather than a page,
 * so wide windows do not stretch the rows. Durations open a slider dialog.
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
    var popup by remember { mutableStateOf<PlayerSettingPopup?>(null) }

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
        SettingsSheetSectionLabel("Playback")
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
        BentoSheetActionRow(
            title = context.symphony.t.Duration,
            subtitle = formatSettingSecs(fadeDuration),
            onClick = { popup = PlayerSettingPopup.FADE_DURATION },
        )
        SettingsSheetSectionLabel("Audio focus")
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
        SettingsSheetSectionLabel("Headphones")
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
        SettingsSheetSectionLabel("Seek buttons")
        BentoSheetActionRow(
            title = context.symphony.t.FastRewindDuration,
            subtitle = formatSettingSecs(seekBack.toFloat()),
            onClick = { popup = PlayerSettingPopup.SEEK_BACK },
        )
        BentoSheetActionRow(
            title = context.symphony.t.FastForwardDuration,
            subtitle = formatSettingSecs(seekForward.toFloat()),
            onClick = { popup = PlayerSettingPopup.SEEK_FORWARD },
        )
    }

    when (popup) {
        PlayerSettingPopup.FADE_DURATION -> BentoSliderDialog(
            title = context.symphony.t.FadePlaybackInOut,
            value = fadeDuration,
            range = 0.5f..6f,
            onValue = { value -> value.times(2).roundToInt().toFloat().div(2) },
            label = ::formatSettingSecs,
            onChange = { settings.fadePlaybackDuration.setValue(it) },
            onReset = {
                settings.fadePlaybackDuration.setValue(settings.fadePlaybackDuration.defaultValue)
            },
            onDismiss = { popup = null },
        )

        PlayerSettingPopup.SEEK_BACK -> BentoSliderDialog(
            title = context.symphony.t.FastRewindDuration,
            value = seekBack.toFloat(),
            range = SEEK_RANGE,
            onValue = { it.roundToInt().toFloat() },
            label = ::formatSettingSecs,
            onChange = { settings.seekBackDuration.setValue(it.roundToInt()) },
            onReset = {
                settings.seekBackDuration.setValue(settings.seekBackDuration.defaultValue)
            },
            onDismiss = { popup = null },
        )

        PlayerSettingPopup.SEEK_FORWARD -> BentoSliderDialog(
            title = context.symphony.t.FastForwardDuration,
            value = seekForward.toFloat(),
            range = SEEK_RANGE,
            onValue = { it.roundToInt().toFloat() },
            label = ::formatSettingSecs,
            onChange = { settings.seekForwardDuration.setValue(it.roundToInt()) },
            onReset = {
                settings.seekForwardDuration.setValue(settings.seekForwardDuration.defaultValue)
            },
            onDismiss = { popup = null },
        )

        null -> Unit
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
