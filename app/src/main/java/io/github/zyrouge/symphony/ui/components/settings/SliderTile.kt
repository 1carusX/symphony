package io.github.zyrouge.symphony.ui.components.settings

import io.github.zyrouge.symphony.ui.components.BentoTextButton
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.components.ScaffoldDialog
import io.github.zyrouge.symphony.ui.components.Slider
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSliderDialog(
    context: ViewContext,
    title: @Composable () -> Unit,
    label: @Composable (Float) -> Unit,
    initialValue: Float,
    range: ClosedFloatingPointRange<Float>,
    onValue: (Float) -> Float = { it },
    onChange: (Float) -> Unit,
    onReset: (() -> Unit)? = null,
    onDismissRequest: () -> Unit,
) {
    var value by remember { mutableFloatStateOf(initialValue) }

    ScaffoldDialog(
        onDismissRequest = onDismissRequest,
        title = title,
        content = {
            Slider(
                value = value,
                onChange = { nValue ->
                    value = onValue(nValue)
                },
                range = range,
                label = label,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .verticalScroll(rememberScrollState())
            )
        },
        actions = {
            onReset?.let {
                BentoTextButton(
                    onClick = {
                        it()
                        onDismissRequest()
                    }
                ) {
                    Text(context.symphony.t.Reset)
                }
            }
            BentoTextButton(
                onClick = {
                    onChange(value)
                    onDismissRequest()
                }
            ) {
                Text(context.symphony.t.Done)
            }
        },
    )
}
