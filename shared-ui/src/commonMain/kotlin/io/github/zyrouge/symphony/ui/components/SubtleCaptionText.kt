package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors

@Composable
fun SubtleCaptionText(text: String) {
    Text(
        text,
        textAlign = TextAlign.Center,
        style = BentoTypography.secondary,
        color = LocalBentoColors.current.sub,
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp, 20.dp),
    )
}
