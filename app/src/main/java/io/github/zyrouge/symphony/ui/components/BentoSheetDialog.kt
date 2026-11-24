package io.github.zyrouge.symphony.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import io.github.zyrouge.symphony.ui.theme.LocalStrings
import io.github.zyrouge.symphony.ui.theme.bento.BentoMotion
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.BentoWindowClass
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import io.github.zyrouge.symphony.ui.theme.bento.rememberBentoWindowClass
import kotlinx.coroutines.launch

private const val SHEET_MAX_WIDTH_DP = 560
private const val CARD_MAX_WIDTH_DP = 480
private const val CARD_TALL_MAX_HEIGHT_DP = 720

/** A drag on the sheet header further than this closes it. */
private const val DRAG_DISMISS_DP = 96

/** A downward flick faster than this (0.45 dp/ms, as the player sheet) closes it. */
private const val FLING_DISMISS_DP_PER_SECOND = 450

/**
 * Shared container of the bento dialogs (prototype `.dl`).
 *
 * - Phones (compact windows): a bottom sheet 8 dp from the edges with radius
 *   28 and a drag handle, over the `scrim`. It clears the gesture bar and the
 *   keyboard, and closes on a scrim tap, Back, or a drag down on its header.
 * - Wider windows: a centered card up to 480 wide with the `float` fill, a
 *   1 dp `edge` ring and the floating shadow.
 *
 * The content scrolls when it is taller than the window. Content that
 * scrolls by itself (a lazy list) passes [scrollable] = false and gives the
 * list `Modifier.weight(1f)`. [tall] makes the surface take the available
 * height, so a filtered list does not resize it. [overlay] draws over the
 * whole window above the sheet, for toasts about what the sheet did.
 * [onBack] puts a back arrow before the title and takes the Back gesture, for
 * a sheet that has pushed one of its own pages (see [BentoSheetPages]).
 */
@Composable
fun BentoSheetDialog(
    title: String,
    onDismissRequest: () -> Unit,
    subtitle: String? = null,
    scrollable: Boolean = true,
    tall: Boolean = false,
    actions: (@Composable RowScope.() -> Unit)? = null,
    overlay: (@Composable BoxScope.() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    BentoSheetDialog(
        title = { Text(title, maxLines = 2, overflow = TextOverflow.Ellipsis) },
        onDismissRequest = onDismissRequest,
        subtitle = subtitle?.let { { Text(it) } },
        scrollable = scrollable,
        tall = tall,
        actions = actions,
        overlay = overlay,
        onBack = onBack,
        content = content,
    )
}

/** [BentoSheetDialog] with composable title and subtitle slots. */
@Composable
fun BentoSheetDialog(
    title: @Composable () -> Unit,
    onDismissRequest: () -> Unit,
    subtitle: (@Composable () -> Unit)? = null,
    scrollable: Boolean = true,
    tall: Boolean = false,
    actions: (@Composable RowScope.() -> Unit)? = null,
    overlay: (@Composable BoxScope.() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val compact = rememberBentoWindowClass() == BentoWindowClass.COMPACT
    // Read in the activity window; the dialog window is laid out edge to edge.
    val systemBottom = bentoSystemBottomInset()
    val systemTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val currentOnDismissRequest by rememberUpdatedState(onDismissRequest)
    val coroutineScope = rememberCoroutineScope()
    val shown = remember { Animatable(0f) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var closing by remember { mutableStateOf(false) }

    // Plays the exit motion, then hands over to the caller.
    fun close() {
        if (closing) return
        closing = true
        coroutineScope.launch {
            shown.animateTo(
                0f,
                tween(BentoMotion.MENU_POPOVER.durationMs, easing = BentoMotion.ACCELERATE),
            )
            currentOnDismissRequest()
        }
    }

    LaunchedEffect(Unit) {
        shown.animateTo(
            1f,
            tween(BentoMotion.ACTION_SHEET.durationMs, easing = BentoMotion.DECELERATE),
        )
    }

    BentoAppDialog(
        onDismissRequest = { close() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        val view = LocalView.current
        LaunchedEffect(view) {
            // The scrim below is the only dimming.
            (view as? DialogWindowProvider)?.window?.setDimAmount(0f)
        }
        val colors = LocalBentoColors.current
        val density = LocalDensity.current
        val contentScroll = rememberScrollState()
        val imeBottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
        val appFontFamily = MaterialTheme.typography.titleLarge.fontFamily
        val shape = RoundedCornerShape(BentoRadii.MENU_SHEET.dp)
        val dragState = rememberDraggableState { delta ->
            dragOffset = (dragOffset + delta).coerceAtLeast(0f)
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = shown.value }
                    .background(colors.scrim)
                    .pointerInput(Unit) { detectTapGestures { close() } },
            )
            Column(
                modifier = Modifier
                    .align(if (compact) Alignment.BottomCenter else Alignment.Center)
                    .padding(
                        start = if (compact) 8.dp else 24.dp,
                        end = if (compact) 8.dp else 24.dp,
                        top = systemTop + if (compact) 24.dp else 32.dp,
                        bottom = maxOf(systemBottom, imeBottom) + if (compact) 8.dp else 32.dp,
                    )
                    .widthIn(max = if (compact) SHEET_MAX_WIDTH_DP.dp else CARD_MAX_WIDTH_DP.dp)
                    .fillMaxWidth()
                    .then(
                        when {
                            !tall -> Modifier
                            compact -> Modifier.fillMaxHeight()
                            else -> Modifier
                                .heightIn(max = CARD_TALL_MAX_HEIGHT_DP.dp)
                                .fillMaxHeight()
                        },
                    )
                    .graphicsLayer {
                        alpha = shown.value
                        if (compact) {
                            translationY = dragOffset + (1f - shown.value) * 40.dp.toPx()
                        } else {
                            val scale = 0.96f + 0.04f * shown.value
                            scaleX = scale
                            scaleY = scale
                        }
                    }
                    .bentoFloatingSurface(shape, colors.`float`)
                    // Swallow taps that miss a control so they do not reach the scrim.
                    .pointerInput(Unit) { detectTapGestures { } },
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            when {
                                compact -> Modifier.draggable(
                                    state = dragState,
                                    orientation = Orientation.Vertical,
                                    onDragStopped = { velocity ->
                                        val distance = with(density) { DRAG_DISMISS_DP.dp.toPx() }
                                        val fling =
                                            with(density) { FLING_DISMISS_DP_PER_SECOND.dp.toPx() }
                                        when {
                                            dragOffset > distance || velocity > fling -> close()
                                            else -> coroutineScope.launch {
                                                animate(dragOffset, 0f) { value, _ ->
                                                    dragOffset = value
                                                }
                                            }
                                        }
                                    },
                                )

                                else -> Modifier
                            },
                        ),
                ) {
                    if (compact) {
                        Box(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .size(width = 36.dp, height = 4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(colors.muted.copy(alpha = 0.7f))
                                .align(Alignment.CenterHorizontally),
                        )
                    }
                    Column(
                        modifier = Modifier.padding(
                            start = if (onBack != null) 12.dp else 24.dp,
                            end = 24.dp,
                            top = if (compact) 14.dp else 22.dp,
                            bottom = 4.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (onBack != null) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .clickable(role = Role.Button, onClick = onBack),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Rounded.ArrowBack,
                                        LocalStrings.current.Back,
                                        tint = colors.fg,
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Box(modifier = Modifier.semantics { heading() }) {
                                ProvideTextStyle(
                                    BentoTypography.title.copy(
                                        fontFamily = appFontFamily,
                                        color = colors.fg,
                                    ),
                                ) {
                                    title()
                                }
                            }
                        }
                        subtitle?.let {
                            ProvideTextStyle(BentoTypography.body.copy(color = colors.sub)) {
                                it()
                            }
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .weight(1f, fill = tall)
                        .fillMaxWidth()
                        .then(
                            when {
                                scrollable -> Modifier
                                    .bentoScrollbar(contentScroll, colors.muted)
                                    .verticalScroll(contentScroll)
                                else -> Modifier
                            },
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    content = content,
                )
                when {
                    actions != null -> Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                        content = actions,
                    )

                    else -> Spacer(modifier = Modifier.height(12.dp))
                }
            }
            overlay?.invoke(this)
        }
        // A pushed page goes back first; only the root page lets Back close the sheet.
        BackHandler(enabled = onBack != null) { onBack?.invoke() }
    }
}

/**
 * Action button of a [BentoSheetDialog]: `raised` for Cancel and other
 * secondary actions, `accent` filled for the [primary] one.
 */
@Composable
fun BentoSheetButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    val colors = LocalBentoColors.current
    val contentColor = if (primary) colors.onAccent else colors.fg
    Row(
        modifier = modifier
            .heightIn(min = 44.dp)
            .widthIn(min = 96.dp)
            .alpha(if (enabled) 1f else 0.4f)
            .clip(RoundedCornerShape(22.dp))
            .background(if (primary) colors.accent else colors.raised)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon?.let {
            Icon(it, null, tint = contentColor, modifier = Modifier.size(18.dp))
        }
        Text(
            label,
            style = BentoTypography.body.copy(fontWeight = FontWeight.SemiBold),
            color = contentColor,
            maxLines = 1,
        )
    }
}

/**
 * Single-line text field of a [BentoSheetDialog]: an optional [label] above a
 * `raised` field with a [placeholder]. A [leadingIcon] turns it into a search
 * pill.
 */
@Composable
fun BentoSheetTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    fontFamily: FontFamily? = null,
) {
    val colors = LocalBentoColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val pill = leadingIcon != null
    val shape = RoundedCornerShape(if (pill) 24.dp else BentoRadii.ROW.dp)
    // BasicTextField does not inherit the app font from the theme.
    val textStyle = BentoTypography.rowTitle.copy(
        fontWeight = FontWeight.Normal,
        fontFamily = fontFamily ?: MaterialTheme.typography.titleLarge.fontFamily,
    )
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        label?.let {
            Text(
                it,
                style = BentoTypography.secondary.copy(fontWeight = FontWeight.SemiBold),
                color = colors.sub,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            interactionSource = interactionSource,
            textStyle = textStyle.copy(color = colors.fg),
            cursorBrush = SolidColor(colors.accent),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (pill) 48.dp else 52.dp)
                        .clip(shape)
                        .background(colors.raised)
                        .then(
                            when {
                                focused -> Modifier.border(2.dp, colors.accent, shape)
                                else -> Modifier
                            },
                        )
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    leadingIcon?.let {
                        Icon(it, null, tint = colors.sub, modifier = Modifier.size(20.dp))
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        if (value.text.isEmpty() && placeholder != null) {
                            Text(
                                placeholder,
                                style = textStyle,
                                color = colors.muted,
                                maxLines = 1,
                            )
                        }
                        inner()
                    }
                }
            },
        )
    }
}

/** Row with a trailing checkbox (prototype `.op` with `.ck`). */
@Composable
fun BentoSheetCheckRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leading: (@Composable () -> Unit)? = null,
) {
    BentoSheetRowLayout(
        title = title,
        subtitle = subtitle,
        leading = leading,
        modifier = modifier
            .bentoSheetRowShape()
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = onCheckedChange),
    ) {
        BentoSheetCheckbox(checked)
    }
}

/** Row with a trailing switch (prototype `.op` with `.swt`). */
@Composable
fun BentoSheetSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    BentoSheetRowLayout(
        title = title,
        subtitle = subtitle,
        leading = null,
        modifier = modifier
            .bentoSheetRowShape()
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange),
    ) {
        BentoSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

/** Tappable row, such as "New playlist". */
@Composable
fun BentoSheetActionRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leading: (@Composable () -> Unit)? = null,
) {
    BentoSheetRowLayout(
        title = title,
        subtitle = subtitle,
        leading = leading,
        modifier = modifier
            .bentoSheetRowShape()
            .clickable(role = Role.Button, onClick = onClick),
        trailing = null,
    )
}

/** 44 dp cover in a sheet row. */
@Composable
fun BentoSheetArt(image: ImageRequest) {
    AsyncImage(
        image,
        null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(BentoRadii.SONG_ART_44.dp))
            .background(LocalBentoColors.current.raised),
    )
}

/** 44 dp `tonal` icon well in a sheet row. */
@Composable
fun BentoSheetIconWell(icon: ImageVector) {
    val colors = LocalBentoColors.current
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(BentoRadii.SONG_ART_44.dp))
            .background(colors.tonal),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = colors.onTonal, modifier = Modifier.size(22.dp))
    }
}

/** Preset chip (prototype `.pchip`); the [selected] one is `tonal`. */
@Composable
fun BentoSheetChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fontFamily: FontFamily? = null,
) {
    val colors = LocalBentoColors.current
    Box(
        modifier = modifier
            .heightIn(min = 40.dp)
            .widthIn(min = 56.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) colors.tonal else colors.raised)
            .semantics { this.selected = selected }
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = BentoTypography.body.copy(
                fontWeight = FontWeight.SemiBold,
                fontFeatureSettings = "tnum",
                fontFamily = fontFamily,
            ),
            color = if (selected) colors.onTonal else colors.fg,
            maxLines = 1,
        )
    }
}

internal fun Modifier.bentoSheetRowShape() = this
    .fillMaxWidth()
    .heightIn(min = 56.dp)
    .clip(RoundedCornerShape(BentoRadii.ROW.dp))

@Composable
internal fun BentoSheetRowLayout(
    title: String,
    subtitle: String?,
    leading: (@Composable () -> Unit)?,
    modifier: Modifier,
    trailing: (@Composable () -> Unit)?,
) {
    val colors = LocalBentoColors.current
    // The texts wrap rather than cut off; a trailing control that crowds them moves under them.
    BentoAdaptiveRow(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        gap = 14.dp,
        leading = leading,
        trailing = trailing,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                title,
                style = BentoTypography.rowTitle,
                color = colors.fg,
            )
            subtitle?.let {
                Text(
                    it,
                    style = BentoTypography.secondary,
                    color = colors.sub,
                )
            }
        }
    }
}

@Composable
private fun BentoSheetCheckbox(checked: Boolean) {
    val colors = LocalBentoColors.current
    val shape = RoundedCornerShape(7.dp)
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(shape)
            .then(
                when {
                    checked -> Modifier.background(colors.accent)
                    else -> Modifier.border(2.dp, colors.muted, shape)
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Icon(
                Icons.Rounded.Check,
                null,
                tint = colors.onAccent,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
