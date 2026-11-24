package io.github.zyrouge.symphony.ui.components.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.components.BentoTextField
import io.github.zyrouge.symphony.ui.theme.LocalStrings
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors

/** An accent row that starts adding an entry, e.g. by opening a folder picker. */
@Composable
fun BentoListAddRow(label: String, onClick: () -> Unit) {
    val colors = LocalBentoColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(BentoRadii.ROW.dp))
            .background(colors.accent)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(Icons.Filled.Add, null, tint = colors.onAccent)
        Text(label, style = BentoTypography.rowTitle, color = colors.onAccent)
    }
}

/** A text field with an add button, for entries typed by hand (tag separators). */
@Composable
fun BentoListTextAddRow(placeholder: String, onAdd: (String) -> Unit) {
    val colors = LocalBentoColors.current
    var text by remember { mutableStateOf("") }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BentoTextField(
            value = text,
            onValueChange = { text = it },
            label = placeholder,
            modifier = Modifier.weight(1f),
        )
        Box(
            modifier = Modifier
                .size(BentoSizes.ICON_BUTTON.dp)
                .clip(CircleShape)
                .background(colors.accent)
                .clickable {
                    // Separators are matched literally, so keep spaces that are part of one (" & ").
                    if (text.isNotEmpty()) onAdd(text)
                    text = ""
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Add, LocalStrings.current.Add, tint = colors.onAccent)
        }
    }
}
