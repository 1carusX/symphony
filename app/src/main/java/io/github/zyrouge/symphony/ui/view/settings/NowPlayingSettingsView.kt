package io.github.zyrouge.symphony.ui.view.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import io.github.zyrouge.symphony.ui.components.BentoPageHeader
import io.github.zyrouge.symphony.ui.components.BentoSettingGroup
import io.github.zyrouge.symphony.ui.components.BentoSettingSwitchRow
import io.github.zyrouge.symphony.ui.components.BentoSettingsColumns
import io.github.zyrouge.symphony.ui.components.bentoGestureBarPadding
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import io.github.zyrouge.symphony.ui.theme.bento.rememberBentoWindowClass
import kotlinx.serialization.Serializable

@Serializable
object NowPlayingSettingsViewRoute

/**
 * The Now playing settings page (prototype v16 `nowp`): seek buttons beside
 * play, the audio format line and keeping the screen on for lyrics.
 */
@Composable
fun NowPlayingSettingsView(context: ViewContext) {
    val windowClass = rememberBentoWindowClass()
    val colors = LocalBentoColors.current
    val settings = context.symphony.settings
    val seekControls by settings.nowPlayingSeekControls.flow.collectAsState()
    val additionalInfo by settings.nowPlayingAdditionalInfo.flow.collectAsState()
    val keepScreenAwake by settings.lyricsKeepScreenAwake.flow.collectAsState()
    val seekBack by settings.seekBackDuration.flow.collectAsState()
    val seekForward by settings.seekForwardDuration.flow.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState()),
        ) {
            BentoPageHeader(
                title = context.symphony.t.NowPlaying,
                windowClass = windowClass,
                onBack = { context.navController.popBackStack() },
            )
            BentoSettingsColumns {
                BentoSettingGroup(title = context.symphony.t.Player) {
                    BentoSettingSwitchRow(
                        label = "Back and forward buttons",
                        sub = "Beside play: $seekBack s back, $seekForward s ahead",
                        checked = seekControls,
                        onCheckedChange = { settings.nowPlayingSeekControls.setValue(it) },
                    )
                    BentoSettingSwitchRow(
                        label = "Audio format",
                        sub = "Shows the format, sample rate and bitrate under the title",
                        checked = additionalInfo,
                        onCheckedChange = { settings.nowPlayingAdditionalInfo.setValue(it) },
                    )
                    BentoSettingSwitchRow(
                        label = "Keep the screen on for lyrics",
                        sub = "While lyrics are showing",
                        checked = keepScreenAwake,
                        onCheckedChange = { settings.lyricsKeepScreenAwake.setValue(it) },
                    )
                }
            }
            Box(Modifier.bentoGestureBarPadding())
        }
    }
}
