package io.github.zyrouge.symphony.ui.view.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
internal fun PlayerSmallContent(
    context: ViewContext,
    data: BentoPlayerData,
    stage: BentoPlayerStage,
    compact: Boolean,
    short: Boolean,
    onStageChange: (BentoPlayerStage) -> Unit,
    onShowSpeed: () -> Unit,
    onShowPitch: () -> Unit,
    onShowSleepTimer: () -> Unit,
    onClosePlayer: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BentoPlayerSeek(context, data)
        BentoPlayerTransport(context, data, short, onShowSleepTimer)
        BentoPlayerExtras(
            context = context,
            data = data,
            stage = stage,
            compact = compact,
            onStageChange = onStageChange,
            onShowSpeed = onShowSpeed,
            onShowPitch = onShowPitch,
            onShowSleepTimer = onShowSleepTimer,
            onClosePlayer = onClosePlayer,
        )
    }
}

@Composable
internal fun ColumnScope.PlayerMediumContent(
    context: ViewContext,
    data: BentoPlayerData,
    stage: BentoPlayerStage,
    compact: Boolean,
    phoneCoverSize: Dp,
    onStageChange: (BentoPlayerStage) -> Unit,
    onShowSpeed: () -> Unit,
    onShowPitch: () -> Unit,
    onShowSleepTimer: () -> Unit,
    onClosePlayer: () -> Unit,
) {
    Column(
        modifier = Modifier
            .then(if (compact) Modifier else Modifier.weight(1f))
            .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        BentoPlayerStageView(
            context = context,
            data = data,
            stage = stage,
            onStageChange = onStageChange,
            modifier = when {
                compact -> Modifier.size(phoneCoverSize)
                else -> Modifier
                    .weight(1f)
                    .fillMaxWidth()
            },
        )
        PlayerMediumControls(
            context = context,
            data = data,
            stage = stage,
            compact = compact,
            short = false,
            onStageChange = onStageChange,
            onShowSpeed = onShowSpeed,
            onShowPitch = onShowPitch,
            onShowSleepTimer = onShowSleepTimer,
            onClosePlayer = onClosePlayer,
        )
    }
}

@Composable
internal fun ColumnScope.PlayerMediumLandscapeContent(
    context: ViewContext,
    data: BentoPlayerData,
    stage: BentoPlayerStage,
    compact: Boolean,
    onStageChange: (BentoPlayerStage) -> Unit,
    onShowSpeed: () -> Unit,
    onShowPitch: () -> Unit,
    onShowSleepTimer: () -> Unit,
    onClosePlayer: () -> Unit,
) {
    Row(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        BentoPlayerStageView(
            context = context,
            data = data,
            stage = stage,
            onStageChange = onStageChange,
            modifier = Modifier
                .weight(0.9f)
                .fillMaxHeight(),
        )
        Column(
            modifier = Modifier
                .weight(1.1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
        ) {
            PlayerMediumControls(
                context = context,
                data = data,
                stage = stage,
                compact = compact,
                short = true,
                onStageChange = onStageChange,
                onShowSpeed = onShowSpeed,
                onShowPitch = onShowPitch,
                onShowSleepTimer = onShowSleepTimer,
                onClosePlayer = onClosePlayer,
            )
        }
    }
}

@Composable
private fun PlayerMediumControls(
    context: ViewContext,
    data: BentoPlayerData,
    stage: BentoPlayerStage,
    compact: Boolean,
    short: Boolean,
    onStageChange: (BentoPlayerStage) -> Unit,
    onShowSpeed: () -> Unit,
    onShowPitch: () -> Unit,
    onShowSleepTimer: () -> Unit,
    onClosePlayer: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        BentoPlayerTitle(context, data)
        BentoPlayerSeek(context, data)
        BentoPlayerTransport(context, data, short, onShowSleepTimer)
        BentoPlayerExtras(
            context = context,
            data = data,
            stage = stage,
            compact = compact,
            onStageChange = onStageChange,
            onShowSpeed = onShowSpeed,
            onShowPitch = onShowPitch,
            onShowSleepTimer = onShowSleepTimer,
            onClosePlayer = onClosePlayer,
        )
    }
}
