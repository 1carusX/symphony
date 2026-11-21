package io.github.zyrouge.symphony.ui.theme.bento

/**
 * Window classes (tokens.json breakpoints.windowClass).
 */
enum class BentoWindowClass {
    COMPACT,
    MEDIUM,
    EXPANDED,
    LARGE,
    EXTRA_LARGE,

    /**
     * Short-height window: height < 480 and width > height (phone landscape).
     * Takes precedence over the width-based classes.
     */
    SHORT_HEIGHT,
}

/**
 * Bento grid classes (tokens.json breakpoints.bentoGrid).
 */
enum class BentoGridClass(val columns: Int, val rowHeightDp: Int, val gapDp: Int) {
    /** Main column width < 560. */
    C4(4, 84, 12),

    /** Main column width 560-879. */
    C6(6, 110, 16),

    /** Main column width 880-1299. */
    C8(8, 112, 16),

    /** Main column width >= 1300. */
    C12(12, 120, 16),

    /** Short-height window with a main column width of at least 600. */
    SHORT(8, 92, 16),
}

/**
 * Short-height rule from tokens.json: height < 480 and width > height
 * (phone landscape).
 */
fun isShortHeightWindow(widthDp: Int, heightDp: Int): Boolean =
    heightDp < 480 && widthDp > heightDp

/**
 * Resolves the window class from the window size: compact < 600, medium
 * 600-839, expanded 840-1199, large 1200-1599, extra-large >= 1600. A
 * short-height window is its own class and wins over the width-based class.
 */
fun resolveBentoWindowClass(widthDp: Int, heightDp: Int): BentoWindowClass {
    if (isShortHeightWindow(widthDp, heightDp)) {
        return BentoWindowClass.SHORT_HEIGHT
    }
    return when {
        widthDp < 600 -> BentoWindowClass.COMPACT
        widthDp < 840 -> BentoWindowClass.MEDIUM
        widthDp < 1200 -> BentoWindowClass.EXPANDED
        widthDp < 1600 -> BentoWindowClass.LARGE
        else -> BentoWindowClass.EXTRA_LARGE
    }
}

/**
 * Resolves the bento grid class from the main column width (window minus
 * navigation) and the window height. The short grid applies on short-height
 * windows whose main column is at least 600 wide; otherwise the class follows
 * the width: c4 < 560, c6 560-879, c8 880-1299, c12 >= 1300.
 */
fun resolveBentoGridClass(mainColumnWidthDp: Int, windowHeightDp: Int): BentoGridClass {
    if (windowHeightDp < 480 && mainColumnWidthDp > windowHeightDp && mainColumnWidthDp >= 600) {
        return BentoGridClass.SHORT
    }
    return when {
        mainColumnWidthDp < 560 -> BentoGridClass.C4
        mainColumnWidthDp < 880 -> BentoGridClass.C6
        mainColumnWidthDp < 1300 -> BentoGridClass.C8
        else -> BentoGridClass.C12
    }
}
