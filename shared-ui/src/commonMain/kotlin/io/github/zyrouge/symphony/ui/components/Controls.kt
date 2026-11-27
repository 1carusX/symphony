package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors

@Composable
fun BentoSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalBentoColors.current
    Row(
        modifier = modifier
            .size(width = 44.dp, height = 24.dp)
            .clip(CircleShape)
            .background(
                if (checked) colors.accent else colors.muted.copy(alpha = 0.28f),
            )
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (checked) Arrangement.End else Arrangement.Start,
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(if (checked) colors.onAccent else colors.sub),
        )
    }
}

@Composable
fun BentoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    val colors = LocalBentoColors.current
    var text by remember(value) { mutableStateOf(value) }
    BasicTextField(
        value = text,
        onValueChange = {
            text = it
            onValueChange(it)
        },
        singleLine = true,
        keyboardOptions = keyboardOptions,
        textStyle = TextStyle(
            color = colors.fg,
            fontSize = BentoTypography.rowTitle.fontSize,
            fontWeight = BentoTypography.rowTitle.fontWeight,
            // BasicTextField does not inherit the app font from the theme.
            fontFamily = MaterialTheme.typography.titleLarge.fontFamily,
        ),
        modifier = modifier
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
            if (text.isEmpty()) {
                Text(
                    label,
                    style = BentoTypography.secondary,
                    color = colors.muted,
                )
            }
            inner()
        },
    )
}

@Composable
fun BentoButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = false,
    enabled: Boolean = true,
) {
    val colors = LocalBentoColors.current
    val background = when {
        !enabled -> colors.raised
        filled -> colors.accent
        else -> colors.raised
    }
    val content = when {
        !enabled -> colors.muted
        filled -> colors.onAccent
        else -> colors.fg
    }
    Box(
        modifier = modifier
            .heightIn(min = 40.dp)
            .clip(RoundedCornerShape(BentoRadii.TILE_SMALL.dp))
            .background(background)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = BentoTypography.rowTitle,
            color = content,
            maxLines = 1,
        )
    }
}

@Composable
fun BentoTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit,
) {
    val colors = LocalBentoColors.current
    Row(
        modifier = modifier
            .heightIn(min = 40.dp)
            .clip(RoundedCornerShape(BentoRadii.TILE_SMALL.dp))
            .background(colors.raised)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        content = content,
    )
}
