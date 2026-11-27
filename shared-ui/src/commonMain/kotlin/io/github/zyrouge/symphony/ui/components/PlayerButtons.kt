package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors

/** Accent play button with the paused/playing icon swap. */
@Composable
fun BentoPlayButton(
    playing: Boolean,
    size: Dp,
    iconSize: Dp,
    label: String,
    onClick: () -> Unit,
) {
    val colors = LocalBentoColors.current
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(colors.accent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
            label,
            tint = colors.onAccent,
            modifier = Modifier.size(iconSize),
        )
    }
}

/**
 * A round player transport button with an icon, a tint and tone background,
 * an optional selected state, a short badge and a long-press action.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BentoPlayerIconButton(
    icon: ImageVector,
    label: String,
    size: Dp,
    iconSize: Dp,
    tint: Color,
    background: Color = Color.Transparent,
    selected: Boolean? = null,
    badge: String? = null,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
) {
    val colors = LocalBentoColors.current
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(background)
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    role = Role.Button,
                    onClick = onClick,
                    onLongClick = onLongClick,
                )
                .semantics {
                    selected?.let {
                        toggleableState = if (it) ToggleableState.On else ToggleableState.Off
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                label,
                tint = tint,
                modifier = Modifier.size(iconSize),
            )
        }
        if (badge != null) {
            Text(
                badge,
                style = bentoTabularStyle(BentoTypography.label.copy(fontWeight = FontWeight.Bold)),
                color = colors.onAccent,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-6).dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(colors.accent)
                    .padding(horizontal = 5.dp),
                maxLines = 1,
            )
        }
    }
}
