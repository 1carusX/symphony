package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.i18n.Strings
import io.github.zyrouge.symphony.ui.theme.bento.BentoGridClass
import io.github.zyrouge.symphony.ui.theme.bento.BentoSpacing
import io.github.zyrouge.symphony.ui.theme.bento.BentoWindowClass
import io.github.zyrouge.symphony.ui.theme.bento.rememberBentoGridClass
import kotlin.math.roundToInt

/** One shortcut tile of the settings root page. */
data class SettingsShortcut(
    val key: String,
    val icon: ImageVector,
    val label: String,
    val summary: String,
    val onClick: () -> Unit,
)

private fun settingsPlacements(gridClass: BentoGridClass) = when (gridClass) {
    BentoGridClass.C4 -> listOf(
        BentoTilePlacement("theme", 1, 1, 4, 4),
        BentoTilePlacement("lib", 1, 5, 4, 1),
        BentoTilePlacement("appearance", 1, 6, 2, 1),
        BentoTilePlacement("player", 3, 6, 2, 1),
        BentoTilePlacement("nowp", 1, 7, 2, 1),
        BentoTilePlacement("mini", 3, 7, 2, 1),
        BentoTilePlacement("home", 1, 8, 2, 1),
        BentoTilePlacement("folders", 3, 8, 2, 1),
        BentoTilePlacement("updates", 1, 9, 2, 1),
        BentoTilePlacement("about", 3, 9, 2, 1),
    )

    BentoGridClass.C6 -> listOf(
        BentoTilePlacement("theme", 1, 1, 4, 3),
        BentoTilePlacement("lib", 5, 1, 2, 1),
        BentoTilePlacement("appearance", 5, 2, 2, 1),
        BentoTilePlacement("player", 5, 3, 2, 1),
        BentoTilePlacement("nowp", 1, 4, 2, 1),
        BentoTilePlacement("mini", 3, 4, 2, 1),
        BentoTilePlacement("home", 5, 4, 2, 1),
        BentoTilePlacement("folders", 1, 5, 2, 1),
        BentoTilePlacement("updates", 3, 5, 2, 1),
        BentoTilePlacement("about", 5, 5, 2, 1),
    )

    BentoGridClass.C8, BentoGridClass.SHORT -> listOf(
        BentoTilePlacement("theme", 1, 1, 4, 3),
        BentoTilePlacement("lib", 5, 1, 4, 1),
        BentoTilePlacement("appearance", 5, 2, 2, 1),
        BentoTilePlacement("player", 7, 2, 2, 1),
        BentoTilePlacement("nowp", 5, 3, 2, 1),
        BentoTilePlacement("mini", 7, 3, 2, 1),
        BentoTilePlacement("home", 1, 4, 2, 1),
        BentoTilePlacement("folders", 3, 4, 2, 1),
        BentoTilePlacement("updates", 5, 4, 2, 1),
        BentoTilePlacement("about", 7, 4, 2, 1),
    )

    BentoGridClass.C12 -> listOf(
        BentoTilePlacement("theme", 1, 1, 5, 3),
        BentoTilePlacement("lib", 6, 1, 7, 1),
        BentoTilePlacement("appearance", 6, 2, 3, 1),
        BentoTilePlacement("player", 9, 2, 4, 1),
        BentoTilePlacement("nowp", 6, 3, 3, 1),
        BentoTilePlacement("mini", 9, 3, 4, 1),
        BentoTilePlacement("home", 1, 4, 3, 1),
        BentoTilePlacement("folders", 4, 4, 3, 1),
        BentoTilePlacement("updates", 7, 4, 3, 1),
        BentoTilePlacement("about", 10, 4, 3, 1),
    )
}

/**
 * The settings root page (DESIGN.md prototype v16 `SETL2`): header, an
 * optional contribution banner, the theme tile beside the library stat tile,
 * shortcut tiles and platform links. The platform supplies the theme, library
 * and about tiles plus the shortcut list.
 */
@Composable
fun SettingsRootPage(
    windowClass: BentoWindowClass,
    strings: Strings,
    shortcuts: List<SettingsShortcut>,
    onBack: () -> Unit,
    themeTile: @Composable (BentoTileGeometry) -> Unit,
    libraryTile: @Composable (BentoTileGeometry) -> Unit,
    aboutTile: @Composable (BentoTileGeometry) -> Unit,
    contributing: @Composable () -> Unit,
    links: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    placements: ((BentoGridClass) -> List<BentoTilePlacement>)? = null,
) {
    Column(modifier = modifier.fillMaxSize()) {
        BentoPageHeader(
            title = strings.Settings,
            windowClass = windowClass,
            onBack = onBack,
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            contributing()
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val gridClass = rememberBentoGridClass(maxWidth.value.roundToInt())
                val crowded = bentoTooNarrowForText(
                    maxWidth.value - BentoSpacing.GUTTER_COMPACT * 2f,
                    LocalDensity.current.fontScale,
                )
                BentoGrid(
                    gridClass = gridClass,
                    // With crowded text the theme tile stacks its options, so it gets two more rows.
                    placements = (placements ?: ::settingsPlacements)(gridClass).map {
                        when {
                            !crowded -> it
                            it.tileId == "theme" -> it.copy(rowSpan = it.rowSpan + 2)
                            else -> it.copy(row = it.row + 2)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { tileId, _, geometry ->
                    when (tileId) {
                        "theme" -> themeTile(geometry)
                        "lib" -> libraryTile(geometry)
                        "about" -> aboutTile(geometry)
                        else -> {
                            val shortcut = shortcuts.firstOrNull { it.key == tileId }
                            if (shortcut != null) {
                                BentoShortcutTile(
                                    geometry = geometry,
                                    icon = shortcut.icon,
                                    label = shortcut.label,
                                    sub = shortcut.summary,
                                    onClick = shortcut.onClick,
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            links()
        }
    }
}
