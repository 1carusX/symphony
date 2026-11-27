package io.github.zyrouge.symphony.ui.view.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.components.bentoSystemBottomInset
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.LocalStrings
import io.github.zyrouge.symphony.ui.theme.bento.BentoSpacing

/**
 * The phone player sheet (DESIGN.md 6.2, 6.5) with the app's header, stage,
 * title and controls.
 */
@Composable
internal fun BentoPhonePlayerSheet(
    context: ViewContext,
    data: BentoPlayerData,
    level: BentoPlayerLevel,
    stage: BentoPlayerStage,
    containerHeight: Dp,
    smallHeight: Dp,
    mediumHeight: Dp,
    coverSize: Dp,
    onLevelChange: (BentoPlayerLevel) -> Unit,
    onStageChange: (BentoPlayerStage) -> Unit,
    onClose: () -> Unit,
    onScrimAlphaChange: (Float) -> Unit,
    onShowSpeed: () -> Unit,
    onShowPitch: () -> Unit,
    onShowSleepTimer: () -> Unit,
    modifier: Modifier = Modifier,
    floating: Boolean = false,
    canResize: Boolean = true,
) {
    PhonePlayerSheet(
        level = level,
        containerHeight = containerHeight,
        smallHeight = smallHeight,
        mediumHeight = mediumHeight,
        coverSize = coverSize,
        canResize = canResize,
        navigationBottomInset = bentoSystemBottomInset(),
        onLevelChange = onLevelChange,
        onClose = onClose,
        onScrimAlphaChange = onScrimAlphaChange,
        floating = floating,
        modifier = modifier,
        header = { progress, headerModifier ->
            Row(
                modifier = headerModifier.padding(start = 20.dp, end = 8.dp, top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CrossFadeHeader(
                    progress = progress,
                    modifier = Modifier.weight(1f),
                    small = {
                        PlayerSmallHeaderInfo(
                            context = context,
                            data = data,
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                        )
                    },
                    medium = { PlayerMediumHeaderInfo(context = context) },
                )
                PlayerCloseButton(contentDescription = LocalStrings.current.ClosePlayer, onClose = onClose)
            }
        },
        stage = { progress, stageModifier ->
            BentoPlayerStageView(
                context = context,
                data = data,
                stage = stage,
                onStageChange = onStageChange,
                modifier = stageModifier.growingSquare(coverSize, progress),
            )
        },
        title = { progress ->
            Box(modifier = Modifier.collapseVertically(progress)) {
                Box(modifier = Modifier.padding(top = 14.dp)) {
                    BentoPlayerTitle(context, data)
                }
            }
        },
        controls = {
            Column(
                modifier = Modifier.padding(
                    start = BentoSpacing.PLAYER_CONTENT_INSET.dp,
                    end = BentoSpacing.PLAYER_CONTENT_INSET.dp,
                    top = 12.dp,
                    bottom = 16.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                BentoPlayerSeek(context, data)
                BentoPlayerTransport(context, data, false, onShowSleepTimer)
                BentoPlayerExtras(
                    context = context,
                    data = data,
                    stage = stage,
                    compact = !floating,
                    onStageChange = onStageChange,
                    onShowSpeed = onShowSpeed,
                    onShowPitch = onShowPitch,
                    onShowSleepTimer = onShowSleepTimer,
                    onClosePlayer = onClose,
                )
            }
        },
    )
}
