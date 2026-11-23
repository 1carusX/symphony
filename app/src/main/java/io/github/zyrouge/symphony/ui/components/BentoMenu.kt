package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import io.github.zyrouge.symphony.ui.theme.bento.BentoWindowClass
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import io.github.zyrouge.symphony.ui.theme.bento.rememberBentoWindowClass
import kotlinx.coroutines.launch

/** Widest the phone action sheet gets (DESIGN.md 4.5). */
private const val BENTO_MENU_SHEET_MAX_WIDTH = 560

/** Space between the phone action sheet and the window edges (DESIGN.md 4.5). */
private const val BENTO_MENU_SHEET_MARGIN = 8

/**
 * A bento menu (DESIGN.md 4.5).
 *
 * - Phones (compact windows): an action sheet anchored to the bottom, 8 dp
 *   from the edges, up to 560 wide, radius 28, over the `scrim`. It slides
 *   up, clears the gesture bar, and the scrim and Back dismiss it.
 * - Wider windows: a 288 dp popover next to the trigger, radius 24, on
 *   `high` with a 1 dp `edge` ring and a deep shadow. Place the menu in the
 *   same Box as its trigger so the popover anchors there.
 *
 * [header] shows above the items with a line under it; [BentoMenuHeader]
 * renders the usual 44 dp art, title and subtitle.
 */
@Composable
fun BentoMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    header: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val compact = rememberBentoWindowClass() == BentoWindowClass.COMPACT
    when {
        compact -> if (expanded) {
            BentoMenuSheet(
                onDismissRequest = onDismissRequest,
                modifier = modifier,
                header = header,
                content = content,
            )
        }

        else -> BentoMenuPopover(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            header = header,
            content = content,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BentoMenuSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    header: (@Composable () -> Unit)?,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = LocalBentoColors.current
    val shape = RoundedCornerShape(BentoRadii.MENU_SHEET.dp)
    val margin = BENTO_MENU_SHEET_MARGIN.dp
    // Measured in the app window, like every other bottom inset in the app.
    val bottomInset = bentoSystemBottomInset()
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val windowHeight = LocalWindowInfo.current.containerDpSize.height
    val maxSheetHeight = (windowHeight - topInset - bottomInset - margin * 4)
        .coerceAtLeast(BentoSizes.MENU_ITEM.dp * 3)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val currentOnDismissRequest by rememberUpdatedState(onDismissRequest)

    fun hideThenDismiss() {
        coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                currentOnDismissRequest()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        sheetMaxWidth = (BENTO_MENU_SHEET_MAX_WIDTH + BENTO_MENU_SHEET_MARGIN * 2).dp,
        shape = RectangleShape,
        containerColor = Color.Transparent,
        contentColor = colors.fg,
        tonalElevation = 0.dp,
        scrimColor = colors.scrim,
        dragHandle = null,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
    ) {
        // The transparent margin around the sheet dismisses like the scrim.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) { detectTapGestures { hideThenDismiss() } }
                .padding(
                    start = margin,
                    end = margin,
                    top = margin * 2,
                    bottom = margin + bottomInset,
                ),
        ) {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .heightIn(max = maxSheetHeight)
                    .bentoFloatingSurface(shape, colors.high)
                    // Taps on the header or between items stay in the sheet.
                    .pointerInput(Unit) { detectTapGestures { } }
                    .verticalScroll(rememberScrollState())
                    .padding(start = 10.dp, end = 10.dp, top = 10.dp, bottom = 14.dp),
            ) {
                BentoMenuBody(header = header, content = content)
            }
        }
    }
}

@Composable
private fun BentoMenuPopover(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    header: (@Composable () -> Unit)?,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = LocalBentoColors.current
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier.width(BentoSizes.MENU_WIDTH.dp),
        shape = RoundedCornerShape(BentoRadii.MENU.dp),
        containerColor = colors.high,
        tonalElevation = 0.dp,
        shadowElevation = BENTO_MENU_SHADOW_ELEVATION.dp,
        border = BorderStroke(1.dp, colors.edge),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
        ) {
            BentoMenuBody(header = header, content = content)
        }
    }
}

@Composable
private fun ColumnScope.BentoMenuBody(
    header: (@Composable () -> Unit)?,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = LocalBentoColors.current
    header?.let {
        it()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.line),
        )
        Spacer(Modifier.height(6.dp))
    }
    // Callers that still pass Material menu items get the bento colors.
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            surface = colors.high,
            surfaceContainer = colors.high,
            surfaceContainerHigh = colors.high,
            primary = colors.accent,
            onPrimary = colors.onAccent,
            onSurface = colors.fg,
            onSurfaceVariant = colors.sub,
        ),
    ) {
        content()
    }
}

/** Depth of the floating menu shadow (tokens.json elevation, first layer offset). */
private const val BENTO_MENU_SHADOW_ELEVATION = 20
