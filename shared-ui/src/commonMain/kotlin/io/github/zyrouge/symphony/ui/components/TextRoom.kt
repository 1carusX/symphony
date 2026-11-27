package io.github.zyrouge.symphony.ui.components

/**
 * The least width, in dp divided by the font scale, at which text fits the
 * phone layouts (a 4-column grid, labelled action tiles). Narrower, as with a
 * large font or content scale, pages switch to their roomier layouts.
 */
const val BENTO_TEXT_ROOM_MIN_WIDTH_DP = 250f

/** True when [widthDp] leaves too little room for text at [fontScale]. */
fun bentoTooNarrowForText(widthDp: Float, fontScale: Float) =
    widthDp / fontScale < BENTO_TEXT_ROOM_MIN_WIDTH_DP
