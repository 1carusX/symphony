package io.github.zyrouge.symphony.ui.theme.bento

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class BentoBreakpointsTest {

    @ParameterizedTest(name = "[{index}] width={0} height={1} expected={2}")
    @MethodSource("windowClassCases")
    fun `window class resolves at width and height boundaries`(
        widthDp: Int,
        heightDp: Int,
        expected: BentoWindowClass,
    ) {
        assertEquals(expected, resolveBentoWindowClass(widthDp, heightDp))
    }

    @ParameterizedTest(name = "[{index}] width={0} height={1} expected={2}")
    @MethodSource("gridClassCases")
    fun `grid class resolves at width boundaries and on short windows`(
        mainColumnWidthDp: Int,
        windowHeightDp: Int,
        expected: BentoGridClass,
    ) {
        assertEquals(expected, resolveBentoGridClass(mainColumnWidthDp, windowHeightDp))
    }

    @Test
    fun `grid classes carry columns, row heights and gaps from the design tokens`() {
        assertEquals(4, BentoGridClass.C4.columns)
        assertEquals(84, BentoGridClass.C4.rowHeightDp)
        assertEquals(12, BentoGridClass.C4.gapDp)
        assertEquals(6, BentoGridClass.C6.columns)
        assertEquals(110, BentoGridClass.C6.rowHeightDp)
        assertEquals(16, BentoGridClass.C6.gapDp)
        assertEquals(8, BentoGridClass.C8.columns)
        assertEquals(112, BentoGridClass.C8.rowHeightDp)
        assertEquals(16, BentoGridClass.C8.gapDp)
        assertEquals(12, BentoGridClass.C12.columns)
        assertEquals(120, BentoGridClass.C12.rowHeightDp)
        assertEquals(16, BentoGridClass.C12.gapDp)
        assertEquals(8, BentoGridClass.SHORT.columns)
        assertEquals(92, BentoGridClass.SHORT.rowHeightDp)
        assertEquals(16, BentoGridClass.SHORT.gapDp)
    }

    companion object {
        @JvmStatic
        fun windowClassCases(): Stream<Arguments> = Stream.of(
            Arguments.of(599, 800, BentoWindowClass.COMPACT),
            Arguments.of(600, 800, BentoWindowClass.MEDIUM),
            Arguments.of(839, 800, BentoWindowClass.MEDIUM),
            Arguments.of(840, 800, BentoWindowClass.EXPANDED),
            Arguments.of(1199, 800, BentoWindowClass.EXPANDED),
            Arguments.of(1200, 800, BentoWindowClass.LARGE),
            Arguments.of(1599, 800, BentoWindowClass.LARGE),
            Arguments.of(1600, 800, BentoWindowClass.EXTRA_LARGE),
            // Short-height rule: height < 480 and width > height wins over the width class.
            Arguments.of(900, 479, BentoWindowClass.SHORT_HEIGHT),
            Arguments.of(900, 480, BentoWindowClass.EXPANDED),
            Arguments.of(480, 360, BentoWindowClass.SHORT_HEIGHT),
            Arguments.of(360, 480, BentoWindowClass.COMPACT),
            Arguments.of(400, 500, BentoWindowClass.COMPACT),
            Arguments.of(2000, 300, BentoWindowClass.SHORT_HEIGHT),
            // Sanity outside the boundaries.
            Arguments.of(700, 900, BentoWindowClass.MEDIUM),
        )

        @JvmStatic
        fun gridClassCases(): Stream<Arguments> = Stream.of(
            Arguments.of(559, 800, BentoGridClass.C4),
            Arguments.of(560, 800, BentoGridClass.C6),
            Arguments.of(879, 800, BentoGridClass.C6),
            Arguments.of(880, 800, BentoGridClass.C8),
            Arguments.of(1299, 800, BentoGridClass.C8),
            Arguments.of(1300, 800, BentoGridClass.C12),
            // Short grid: short-height window with width >= 600.
            Arguments.of(599, 400, BentoGridClass.C6),
            Arguments.of(600, 400, BentoGridClass.SHORT),
            Arguments.of(880, 400, BentoGridClass.SHORT),
            Arguments.of(1300, 400, BentoGridClass.SHORT),
            Arguments.of(600, 479, BentoGridClass.SHORT),
            // Height 480 is not short.
            Arguments.of(600, 480, BentoGridClass.C6),
            // Short-height window narrower than 600 falls back to the width class.
            Arguments.of(300, 250, BentoGridClass.C4),
        )
    }
}
