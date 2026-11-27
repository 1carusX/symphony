package io.github.zyrouge.symphony.ui.view.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors

/** A small heading between groups of rows in a settings sheet. */
@Composable
internal fun SettingsSheetSectionLabel(text: String) {
    Text(
        text,
        style = BentoTypography.secondary.copy(fontWeight = FontWeight.SemiBold),
        color = LocalBentoColors.current.sub,
        modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 2.dp),
    )
}
