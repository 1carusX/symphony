package io.github.zyrouge.symphony.ui.view.player

import android.icu.text.MeasureFormat
import android.icu.util.Measure
import android.icu.util.MeasureUnit
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.services.radio.Radio
import io.github.zyrouge.symphony.ui.components.BentoSheetButton
import io.github.zyrouge.symphony.ui.components.BentoSheetChip
import io.github.zyrouge.symphony.ui.components.BentoSheetDialog
import io.github.zyrouge.symphony.ui.components.BentoSheetSwitchRow
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import io.github.zyrouge.symphony.utils.DurationUtils
import kotlinx.coroutines.delay
import java.time.Duration
import java.util.Locale

private val sleepTimerPresetMinutes = listOf(15, 30, 45, 60, 120)
private val sleepTimerExtendMinutes = listOf(5, 15, 30)
private const val SLEEP_TIMER_DEFAULT_MINUTES = 30
private const val SLEEP_TIMER_STEP_MINUTES = 5
private const val SLEEP_TIMER_MAX_MINUTES = 600

/** The running timer: time left, extend chips, quit on end and stop. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NowPlayingSleepTimerDialog(
    context: ViewContext,
    sleepTimer: Radio.SleepTimer,
    onDismissRequest: () -> Unit,
) {
    val locale = LocalConfiguration.current.locales[0]
    var endsIn by remember(sleepTimer) {
        mutableLongStateOf(sleepTimer.endsAt - System.currentTimeMillis())
    }
    var quitOnEnd by remember(sleepTimer) { mutableStateOf(sleepTimer.quitOnEnd) }

    LaunchedEffect(sleepTimer) {
        while (true) {
            endsIn = (sleepTimer.endsAt - System.currentTimeMillis()).coerceAtLeast(0L)
            delay(500L)
        }
    }

    BentoSheetDialog(
        title = context.symphony.t.SleepTimer,
        onDismissRequest = onDismissRequest,
        actions = {
            BentoSheetButton(
                label = context.symphony.t.Stop,
                onClick = {
                    context.symphony.radio.clearSleepTimer()
                    onDismissRequest()
                },
            )
            BentoSheetButton(
                label = context.symphony.t.Done,
                primary = true,
                onClick = onDismissRequest,
            )
        },
    ) {
        SleepTimerReadout(
            text = DurationUtils.formatMs(endsIn),
            modifier = Modifier.fillMaxWidth(),
        )
        FlowRow(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            sleepTimerExtendMinutes.forEach { minutes ->
                BentoSheetChip(
                    label = "+" + formatSleepTimerMinutes(locale, minutes),
                    selected = false,
                    onClick = {
                        val left = (sleepTimer.endsAt - System.currentTimeMillis())
                            .coerceAtLeast(0L)
                        context.symphony.radio.setSleepTimer(
                            duration = left + Duration.ofMinutes(minutes.toLong()).toMillis(),
                            quitOnEnd = quitOnEnd,
                        )
                    },
                )
            }
        }
        BentoSheetSwitchRow(
            title = context.symphony.t.QuitAppOnEnd,
            checked = quitOnEnd,
            onCheckedChange = {
                quitOnEnd = it
                sleepTimer.quitOnEnd = it
            },
        )
    }
}

/** Sets a timer: minutes readout with steppers, presets, pause after song, quit on end. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NowPlayingSleepTimerSetDialog(
    context: ViewContext,
    onDismissRequest: () -> Unit,
) {
    val locale = LocalConfiguration.current.locales[0]
    var minutes by remember { mutableIntStateOf(SLEEP_TIMER_DEFAULT_MINUTES) }
    var quitOnEnd by remember { mutableStateOf(false) }
    val pauseOnCurrentSongEnd by context.symphony.radio.observatory.pauseOnCurrentSongEnd
        .collectAsState()

    BentoSheetDialog(
        title = context.symphony.t.SleepTimer,
        onDismissRequest = onDismissRequest,
        actions = {
            BentoSheetButton(
                label = context.symphony.t.Cancel,
                onClick = onDismissRequest,
            )
            BentoSheetButton(
                label = context.symphony.t.Done,
                primary = true,
                onClick = {
                    context.symphony.radio.setSleepTimer(
                        duration = Duration.ofMinutes(minutes.toLong()).toMillis(),
                        quitOnEnd = quitOnEnd,
                    )
                    onDismissRequest()
                },
            )
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SleepTimerStepButton(
                icon = Icons.Rounded.Remove,
                label = "-" + formatSleepTimerMinutes(locale, SLEEP_TIMER_STEP_MINUTES),
                enabled = minutes > SLEEP_TIMER_STEP_MINUTES,
                onClick = {
                    minutes = (minutes - SLEEP_TIMER_STEP_MINUTES)
                        .coerceAtLeast(SLEEP_TIMER_STEP_MINUTES)
                },
            )
            SleepTimerReadout(
                text = when {
                    minutes < 60 -> minutes.toString()
                    else -> "%d:%02d".format(locale, minutes / 60, minutes % 60)
                },
                modifier = Modifier.widthIn(min = 112.dp),
            )
            SleepTimerStepButton(
                icon = Icons.Rounded.Add,
                label = "+" + formatSleepTimerMinutes(locale, SLEEP_TIMER_STEP_MINUTES),
                enabled = minutes < SLEEP_TIMER_MAX_MINUTES,
                onClick = {
                    minutes = (minutes + SLEEP_TIMER_STEP_MINUTES)
                        .coerceAtMost(SLEEP_TIMER_MAX_MINUTES)
                },
            )
        }
        Text(
            when {
                minutes < 60 -> context.symphony.t.Minutes
                else -> "${context.symphony.t.Hours} : ${context.symphony.t.Minutes}"
            },
            style = BentoTypography.secondary,
            color = LocalBentoColors.current.sub,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        FlowRow(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            sleepTimerPresetMinutes.forEach { preset ->
                BentoSheetChip(
                    label = formatSleepTimerMinutes(locale, preset),
                    selected = preset == minutes,
                    onClick = { minutes = preset },
                )
            }
        }
        BentoSheetSwitchRow(
            title = context.symphony.t.PauseOnCurrentSongEnd,
            checked = pauseOnCurrentSongEnd,
            onCheckedChange = { context.symphony.radio.setPauseOnCurrentSongEnd(it) },
        )
        BentoSheetSwitchRow(
            title = context.symphony.t.QuitAppOnEnd,
            checked = quitOnEnd,
            onCheckedChange = { quitOnEnd = it },
        )
    }
}

@Composable
private fun SleepTimerReadout(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = BentoTypography.statLarge.copy(fontFeatureSettings = "tnum"),
        color = LocalBentoColors.current.fg,
        textAlign = TextAlign.Center,
        maxLines = 1,
        modifier = modifier.padding(vertical = 8.dp),
    )
}

@Composable
private fun SleepTimerStepButton(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val colors = LocalBentoColors.current
    Box(
        modifier = Modifier
            .size(48.dp)
            .alpha(if (enabled) 1f else 0.4f)
            .clip(CircleShape)
            .background(colors.raised)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, label, tint = colors.fg)
    }
}

/** "15 min", "1 hr", "1 hr, 30 min", in the app language. */
private fun formatSleepTimerMinutes(locale: Locale, minutes: Int): String {
    val format = MeasureFormat.getInstance(locale, MeasureFormat.FormatWidth.SHORT)
    val hours = minutes / 60
    val rest = minutes % 60
    return when {
        hours == 0 -> format.format(Measure(minutes, MeasureUnit.MINUTE))
        rest == 0 -> format.format(Measure(hours, MeasureUnit.HOUR))
        else -> format.formatMeasures(
            Measure(hours, MeasureUnit.HOUR),
            Measure(rest, MeasureUnit.MINUTE),
        )
    }
}
