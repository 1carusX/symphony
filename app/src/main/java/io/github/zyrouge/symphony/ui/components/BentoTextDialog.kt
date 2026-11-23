package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors

/**
 * Popup with a single line of text. The text is handed back once, when the
 * popup closes, and only if it changed and [isValid] accepts it; invalid text
 * shows [errorText] and is discarded on close.
 */
@Composable
fun BentoTextDialog(
    title: String,
    value: String,
    placeholder: String?,
    onDone: (String) -> Unit,
    onDismiss: () -> Unit,
    isValid: (String) -> Boolean = { true },
    errorText: String? = null,
) {
    val colors = LocalBentoColors.current
    var text by remember { mutableStateOf(value) }
    val valid = isValid(text)
    BentoDialog(
        title = title,
        onDismiss = {
            if (valid && text != value) onDone(text)
            onDismiss()
        },
    ) {
        BasicTextField(
            value = text,
            onValueChange = { text = it },
            singleLine = true,
            textStyle = TextStyle(
                color = colors.fg,
                fontSize = BentoTypography.rowTitle.fontSize,
                fontWeight = BentoTypography.rowTitle.fontWeight,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .background(
                    color = colors.raised,
                    shape = RoundedCornerShape(BentoRadii.TILE_SMALL.dp),
                )
                .border(
                    width = 1.dp,
                    color = colors.line,
                    shape = RoundedCornerShape(BentoRadii.TILE_SMALL.dp),
                )
                .padding(horizontal = 14.dp, vertical = 12.dp),
            decorationBox = { inner ->
                if (text.isEmpty() && placeholder != null) {
                    Text(
                        placeholder,
                        style = BentoTypography.secondary,
                        color = colors.muted,
                    )
                }
                inner()
            },
        )
        if (!valid && errorText != null) {
            Text(errorText, style = BentoTypography.secondary, color = colors.accent)
        }
    }
}
