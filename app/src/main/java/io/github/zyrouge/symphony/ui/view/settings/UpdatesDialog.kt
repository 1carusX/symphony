package io.github.zyrouge.symphony.ui.view.settings

import io.github.zyrouge.symphony.ui.components.BentoSwitch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.components.BentoDialog
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors

@Composable
fun UpdatesDialog(context: ViewContext, onDismiss: () -> Unit) {
    val checkForUpdates by context.symphony.settings.checkForUpdates.flow.collectAsState()
    val showUpdateToast by context.symphony.settings.showUpdateToast.flow.collectAsState()

    BentoDialog(title = context.symphony.t.Updates, onDismiss = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            UpdateSwitchRow(
                label = context.symphony.t.CheckForUpdates,
                checked = checkForUpdates,
                onChange = { context.symphony.settings.checkForUpdates.setValue(it) },
            )
            UpdateSwitchRow(
                label = context.symphony.t.ShowUpdateToast,
                checked = showUpdateToast,
                onChange = { context.symphony.settings.showUpdateToast.setValue(it) },
            )
        }
    }
}

@Composable
private fun UpdateSwitchRow(
    label: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
) {
    val colors = LocalBentoColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            label,
            style = BentoTypography.rowTitle,
            color = colors.fg,
            modifier = Modifier.weight(1f),
        )
        BentoSwitch(
            checked = checked,
            onCheckedChange = onChange,
        )
    }
}
