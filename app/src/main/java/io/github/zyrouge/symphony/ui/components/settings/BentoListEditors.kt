package io.github.zyrouge.symphony.ui.components.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.components.BentoDialog
import io.github.zyrouge.symphony.ui.components.BentoTextField
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors

/**
 * Edits a set of strings (folders, tag separators) as a list: each entry has
 * a remove button and the last row adds one. The set is handed back once, when
 * the popup closes, and only if it changed, so closing it never triggers a
 * rescan by itself.
 */
@Composable
fun BentoListEditorDialog(
    context: ViewContext,
    title: String,
    values: Set<String>,
    onDone: (Set<String>) -> Unit,
    onDismiss: () -> Unit,
    addRow: @Composable (onAdd: (String) -> Unit) -> Unit,
) {
    val colors = LocalBentoColors.current
    val entries = remember(values) { mutableStateListOf<String>().apply { addAll(values) } }

    BentoDialog(
        title = title,
        onDismiss = {
            val edited = entries.toSet()
            if (edited != values) onDone(edited)
            onDismiss()
        },
    ) {
        LazyColumn(
            modifier = Modifier.heightIn(max = 320.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items(entries.toList(), key = { it }) { entry ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(BentoRadii.ROW.dp))
                        .background(colors.raised)
                        .padding(start = 14.dp, end = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        entry,
                        style = BentoTypography.secondary,
                        color = colors.fg,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Box(
                        modifier = Modifier
                            .size(BentoSizes.ICON_BUTTON.dp)
                            .clip(CircleShape)
                            .clickable { entries.remove(entry) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.Close,
                            "${context.symphony.t.Delete} $entry",
                            tint = colors.sub,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
        addRow { value ->
            if (value.isNotEmpty() && value !in entries) entries.add(value)
        }
    }
}

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
            Icon(Icons.Filled.Add, "Add", tint = colors.onAccent)
        }
    }
}
