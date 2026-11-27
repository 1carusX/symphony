package io.github.zyrouge.symphony.ui.theme.bento

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing

/**
 * One motion token (tokens.json motion). Durations in milliseconds; the
 * easing is null where the kit records only a duration and a start state.
 */
data class BentoMotionSpec(val durationMs: Int, val easing: Easing? = null)

object BentoMotion {
    /** cubic-bezier(.2, .9, .25, 1). */
    val DECELERATE = CubicBezierEasing(0.2f, 0.9f, 0.25f, 1f)

    /** cubic-bezier(.4, 0, 1, 1). */
    val ACCELERATE = CubicBezierEasing(0.4f, 0f, 1f, 1f)

    /** cubic-bezier(.3, 1.4, .5, 1). */
    val OVERSHOOT = CubicBezierEasing(0.3f, 1.4f, 0.5f, 1f)

    /** CSS ease-out, cubic-bezier(0, 0, .58, 1). */
    val EASE_OUT = CubicBezierEasing(0f, 0f, 0.58f, 1f)

    val PHONE_SHEET_OPEN = BentoMotionSpec(420, DECELERATE)
    val PHONE_SHEET_CLOSE = BentoMotionSpec(320, ACCELERATE)

    /** Enters from translateY(24) scale(.92), opacity 0, origin bottom right. */
    val FLOATING_PLAYER = BentoMotionSpec(300, DECELERATE)

    /** Hidden at translateY(28) scale(.94), opacity 0. */
    val CAPSULE_SHOW_HIDE = BentoMotionSpec(340, DECELERATE)

    val PLAY_BUTTON_MORPH = BentoMotionSpec(400, OVERSHOOT)

    /** Level changes in the player fade the content in. */
    val LEVEL_CONTENT_FADE = BentoMotionSpec(280, EASE_OUT)

    /** Enters from scale(.96), opacity 0. */
    val MENU_POPOVER = BentoMotionSpec(180)

    /** Enters from translateY(40), opacity 0. */
    val ACTION_SHEET = BentoMotionSpec(260)

    const val TOAST_DURATION = 2400
}
