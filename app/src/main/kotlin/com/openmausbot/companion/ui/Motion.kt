package com.openmausbot.companion.ui

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role

/*
 * The app's motion and touch language, measured from the reference app at 60 fps
 * (docs/redesign/reference/motion-spec.md). Short, decelerating, never bouncy
 * except for the small spring a pressed control settles back with, and never a
 * ripple: a pressed thing shrinks a little and dims.
 */

/** Ease-out cubic: the curve of the reference's sheets and folder reveals. */
internal val EaseOutCubic: Easing = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)

/** Ease-in cubic: how a folder collapses — speeding up, stopping dead. */
internal val EaseInCubic: Easing = CubicBezierEasing(0.32f, 0f, 0.67f, 0f)

internal object Durations {
    /** Opening a chat; the reverse is [POP]. */
    const val PUSH = 100
    const val POP = 80
    const val FOLDER_EXPAND = 230
    const val FOLDER_COLLAPSE = 130
    const val CHEVRON = 120
    const val SHEET_OPEN = 440
    const val SHEET_CLOSE = 320
    const val SEARCH_CROSSFADE = 120
    const val PRESS_IN = 70
}

/** How far a pressed control shrinks and how much it dims. */
internal enum class Press(val scale: Float, val alpha: Float) {
    /** Round buttons and pills. */
    CONTROL(0.97f, 0.88f),

    /** A list row: its content, not its background. */
    ROW(0.96f, 0.77f),

    /** Visible feedback off: folder headers, which only respond to the tap. */
    NONE(1f, 1f),
}

/**
 * Shrink and dim while pressed, then settle back on a soft spring (damping 0.75,
 * a hair of overshoot). Driven by [interaction], so it follows the same press the
 * click does, including a press that slides off and cancels.
 */
@Composable
internal fun Modifier.pressFeedback(interaction: MutableInteractionSource, press: Press): Modifier {
    if (press == Press.NONE) return this
    val pressed by interaction.collectIsPressedAsState()
    val spec = if (pressed) {
        tween<Float>(Durations.PRESS_IN, easing = FastOutSlowInEasing)
    } else {
        spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow)
    }
    val scale by animateFloatAsState(if (pressed) press.scale else 1f, spec, label = "press-scale")
    val alpha by animateFloatAsState(if (pressed) press.alpha else 1f, spec, label = "press-alpha")
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
        this.alpha = alpha
    }
}

/**
 * The app's clickable: no ripple, [Press] feedback instead. [onLongClick] adds a
 * long press without changing how the short tap feels.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun Modifier.pressable(
    onClick: () -> Unit,
    press: Press = Press.CONTROL,
    enabled: Boolean = true,
    role: Role? = Role.Button,
    onClickLabel: String? = null,
    onLongClick: (() -> Unit)? = null,
): Modifier {
    val interaction = remember { MutableInteractionSource() }
    return this
        .pressFeedback(interaction, press)
        .combinedClickable(
            interactionSource = interaction,
            indication = null,
            enabled = enabled,
            role = role,
            onClickLabel = onClickLabel,
            onLongClick = onLongClick,
            onClick = onClick,
        )
}

/**
 * The hairline around floating controls, popups and sheet cards: #4B4B4D on the
 * dark page. Light is provisional until the reference is sampled in light mode.
 */
internal val AppColors.hairline: Color
    @Composable get() =
        if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF4B4B4D) else Color(0xFFD9D9DE)
