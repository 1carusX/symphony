package io.github.zyrouge.symphony.ui.view.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.github.zyrouge.symphony.ui.components.bentoGestureBarPadding
import io.github.zyrouge.symphony.ui.components.BentoPageHeader
import io.github.zyrouge.symphony.ui.components.BentoSettingGroup
import io.github.zyrouge.symphony.ui.components.BentoSwitch
import io.github.zyrouge.symphony.ui.components.BentoSettingRow
import io.github.zyrouge.symphony.ui.components.BentoSettingsColumns
import io.github.zyrouge.symphony.ui.components.BentoSliderDialog
import io.github.zyrouge.symphony.ui.components.BentoToggleDialog
import io.github.zyrouge.symphony.ui.components.formatSettingSecs
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import io.github.zyrouge.symphony.ui.theme.bento.rememberBentoWindowClass
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

@Serializable
object PlayerSettingsViewRoute

private enum class PlayerSettingPopup {
    FADE,
    FADE_DURATION,
    REQUIRE_AUDIO_FOCUS,
    IGNORE_AUDIO_FOCUS_LOSS,
    PLAY_ON_HEADPHONES,
    PAUSE_ON_HEADPHONES,
    SEEK_BACK,
    SEEK_FORWARD,
    GAPLESS,
}

private val SEEK_RANGE = 3f..60f

@Composable
fun PlayerSettingsView(context: ViewContext) {
    val scrollState = rememberScrollState()
    val windowClass = rememberBentoWindowClass()
    val colors = LocalBentoColors.current
    val fadePlayback by context.symphony.settings.fadePlayback.flow.collectAsState()
    val fadeDuration by context.symphony.settings.fadePlaybackDuration.flow.collectAsState()
    val requireAudioFocus by context.symphony.settings.requireAudioFocus.flow.collectAsState()
    val ignoreAudioFocusLoss by context.symphony.settings.ignoreAudioFocusLoss.flow.collectAsState()
    val playOnHeadphones by context.symphony.settings.playOnHeadphonesConnect.flow.collectAsState()
    val pauseOnHeadphones by context.symphony.settings.pauseOnHeadphonesDisconnect.flow.collectAsState()
    val seekBack by context.symphony.settings.seekBackDuration.flow.collectAsState()
    val seekForward by context.symphony.settings.seekForwardDuration.flow.collectAsState()
    val gapless by context.symphony.settings.gaplessPlayback.flow.collectAsState()
    var popup by remember { mutableStateOf<PlayerSettingPopup?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(scrollState),
        ) {
            BentoPageHeader(
                title = context.symphony.t.Player,
                windowClass = windowClass,
                onBack = { context.navController.popBackStack() },
            )
            BentoSettingsColumns {
                BentoSettingGroup(title = "Playback") {
                    BentoSettingRow(
                        label = context.symphony.t.GaplessPlayback,
                        value = null,
                        onClick = {
                            context.symphony.settings.gaplessPlayback.setValue(!gapless)
                        },
                        trailing = {
                            BentoSwitch(
                                checked = gapless,
                                onCheckedChange = {
                                    context.symphony.settings.gaplessPlayback.setValue(it)
                                },
                            )
                        },
                    )
                    BentoSettingRow(
                        label = context.symphony.t.FadePlaybackInOut,
                        value = null,
                        onClick = {
                            context.symphony.settings.fadePlayback.setValue(!fadePlayback)
                        },
                        trailing = {
                            BentoSwitch(
                                checked = fadePlayback,
                                onCheckedChange = {
                                    context.symphony.settings.fadePlayback.setValue(it)
                                },
                            )
                        },
                    )
                    BentoSettingRow(
                        label = context.symphony.t.Duration,
                        value = formatSettingSecs(fadeDuration),
                        onClick = { popup = PlayerSettingPopup.FADE_DURATION },
                    )
                }
                BentoSettingGroup(title = "Audio focus") {
                    BentoSettingRow(
                        label = context.symphony.t.RequireAudioFocus,
                        value = null,
                        onClick = {
                            context.symphony.settings.requireAudioFocus.setValue(!requireAudioFocus)
                        },
                        trailing = {
                            BentoSwitch(
                                checked = requireAudioFocus,
                                onCheckedChange = {
                                    context.symphony.settings.requireAudioFocus.setValue(it)
                                },
                            )
                        },
                    )
                    BentoSettingRow(
                        label = context.symphony.t.IgnoreAudioFocusLoss,
                        value = null,
                        onClick = {
                            context.symphony.settings.ignoreAudioFocusLoss.setValue(!ignoreAudioFocusLoss)
                        },
                        trailing = {
                            BentoSwitch(
                                checked = ignoreAudioFocusLoss,
                                onCheckedChange = {
                                    context.symphony.settings.ignoreAudioFocusLoss.setValue(it)
                                },
                            )
                        },
                    )
                }
                BentoSettingGroup(title = "Headphones") {
                    BentoSettingRow(
                        label = context.symphony.t.PlayOnHeadphonesConnect,
                        value = null,
                        onClick = {
                            context.symphony.settings.playOnHeadphonesConnect.setValue(!playOnHeadphones)
                        },
                        trailing = {
                            BentoSwitch(
                                checked = playOnHeadphones,
                                onCheckedChange = {
                                    context.symphony.settings.playOnHeadphonesConnect.setValue(it)
                                },
                            )
                        },
                    )
                    BentoSettingRow(
                        label = context.symphony.t.PauseOnHeadphonesDisconnect,
                        value = null,
                        onClick = {
                            context.symphony.settings.pauseOnHeadphonesDisconnect.setValue(!pauseOnHeadphones)
                        },
                        trailing = {
                            BentoSwitch(
                                checked = pauseOnHeadphones,
                                onCheckedChange = {
                                    context.symphony.settings.pauseOnHeadphonesDisconnect.setValue(it)
                                },
                            )
                        },
                    )
                }
                BentoSettingGroup(title = "Seek buttons") {
                    BentoSettingRow(
                        label = context.symphony.t.FastRewindDuration,
                        value = formatSettingSecs(seekBack.toFloat()),
                        onClick = { popup = PlayerSettingPopup.SEEK_BACK },
                    )
                    BentoSettingRow(
                        label = context.symphony.t.FastForwardDuration,
                        value = formatSettingSecs(seekForward.toFloat()),
                        onClick = { popup = PlayerSettingPopup.SEEK_FORWARD },
                    )
                }
            }
            Box(Modifier.bentoGestureBarPadding())
        }
    }

    when (popup) {
        PlayerSettingPopup.FADE -> BentoToggleDialog(
            title = context.symphony.t.FadePlaybackInOut,
            checked = fadePlayback,
            onChange = { context.symphony.settings.fadePlayback.setValue(it) },
            onDismiss = { popup = null },
        )

        PlayerSettingPopup.FADE_DURATION -> BentoSliderDialog(
            title = context.symphony.t.FadePlaybackInOut,
            value = fadeDuration,
            range = 0.5f..6f,
            onValue = { value -> value.times(2).roundToInt().toFloat().div(2) },
            label = ::formatSettingSecs,
            onChange = { context.symphony.settings.fadePlaybackDuration.setValue(it) },
            onReset = {
                context.symphony.settings.fadePlaybackDuration.setValue(
                    context.symphony.settings.fadePlaybackDuration.defaultValue,
                )
            },
            onDismiss = { popup = null },
        )

        PlayerSettingPopup.REQUIRE_AUDIO_FOCUS -> BentoToggleDialog(
            title = context.symphony.t.RequireAudioFocus,
            checked = requireAudioFocus,
            onChange = { context.symphony.settings.requireAudioFocus.setValue(it) },
            onDismiss = { popup = null },
        )

        PlayerSettingPopup.IGNORE_AUDIO_FOCUS_LOSS -> BentoToggleDialog(
            title = context.symphony.t.IgnoreAudioFocusLoss,
            checked = ignoreAudioFocusLoss,
            onChange = { context.symphony.settings.ignoreAudioFocusLoss.setValue(it) },
            onDismiss = { popup = null },
        )

        PlayerSettingPopup.PLAY_ON_HEADPHONES -> BentoToggleDialog(
            title = context.symphony.t.PlayOnHeadphonesConnect,
            checked = playOnHeadphones,
            onChange = { context.symphony.settings.playOnHeadphonesConnect.setValue(it) },
            onDismiss = { popup = null },
        )

        PlayerSettingPopup.PAUSE_ON_HEADPHONES -> BentoToggleDialog(
            title = context.symphony.t.PauseOnHeadphonesDisconnect,
            checked = pauseOnHeadphones,
            onChange = { context.symphony.settings.pauseOnHeadphonesDisconnect.setValue(it) },
            onDismiss = { popup = null },
        )

        PlayerSettingPopup.SEEK_BACK -> BentoSliderDialog(
            title = context.symphony.t.FastRewindDuration,
            value = seekBack.toFloat(),
            range = SEEK_RANGE,
            onValue = { it.roundToInt().toFloat() },
            label = ::formatSettingSecs,
            onChange = { context.symphony.settings.seekBackDuration.setValue(it.roundToInt()) },
            onReset = {
                context.symphony.settings.seekBackDuration.setValue(
                    context.symphony.settings.seekBackDuration.defaultValue,
                )
            },
            onDismiss = { popup = null },
        )

        PlayerSettingPopup.SEEK_FORWARD -> BentoSliderDialog(
            title = context.symphony.t.FastForwardDuration,
            value = seekForward.toFloat(),
            range = SEEK_RANGE,
            onValue = { it.roundToInt().toFloat() },
            label = ::formatSettingSecs,
            onChange = { context.symphony.settings.seekForwardDuration.setValue(it.roundToInt()) },
            onReset = {
                context.symphony.settings.seekForwardDuration.setValue(
                    context.symphony.settings.seekForwardDuration.defaultValue,
                )
            },
            onDismiss = { popup = null },
        )

        PlayerSettingPopup.GAPLESS -> BentoToggleDialog(
            title = context.symphony.t.GaplessPlayback,
            checked = gapless,
            onChange = { context.symphony.settings.gaplessPlayback.setValue(it) },
            onDismiss = { popup = null },
        )

        null -> Unit
    }
}
