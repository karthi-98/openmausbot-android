package com.openmausbot.companion.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The Material You theme: the brand fallback scheme, and which scheme roles the
 * bubbles and status colours are drawn from.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CompanionThemeTest {
    @get:Rule
    val compose = createComposeRule()

    private class Seen {
        var primary = Color.Unspecified
        var primaryContainer = Color.Unspecified
        var onPrimaryContainer = Color.Unspecified
        var surfaceContainerHigh = Color.Unspecified
        var mine = Color.Unspecified
        var mineText = Color.Unspecified
        var theirs = Color.Unspecified
        var success = Color.Unspecified
        var warning = Color.Unspecified
    }

    /** Every (dark, dynamic) combination, drawn side by side in one composition. */
    private fun renderAll(): Map<Pair<Boolean, Boolean>, Seen> {
        val combos = listOf(false to false, true to false, false to true, true to true)
        val seen = combos.associateWith { Seen() }
        compose.setContent {
            for ((dark, dynamic) in combos) {
                val out = seen.getValue(dark to dynamic)
                CompanionTheme(darkTheme = dark, dynamicColor = dynamic) {
                    val scheme = MaterialTheme.colorScheme
                    out.primary = scheme.primary
                    out.primaryContainer = scheme.primaryContainer
                    out.onPrimaryContainer = scheme.onPrimaryContainer
                    out.surfaceContainerHigh = scheme.surfaceContainerHigh
                    out.mine = BubbleColor.mine
                    out.mineText = BubbleColor.mineText
                    out.theirs = BubbleColor.theirs
                    out.success = AppColors.status.success
                    out.warning = AppColors.status.warning
                }
            }
        }
        compose.waitForIdle()
        return seen
    }

    @Test
    fun withoutDynamicColourTheLightSchemeIsTheOneGeneratedFromTheBrandGreen() {
        val seen = renderAll().getValue(false to false)
        assertEquals(Color(0xFF2D6A45), seen.primary)
        assertEquals(Color(0xFFB1F1C2), seen.primaryContainer)
        assertEquals(Color(0xFF006D3C), seen.success)
        assertEquals(Color(0xFF855300), seen.warning)
    }

    @Test
    fun withoutDynamicColourTheDarkSchemeIsTheOneGeneratedFromTheBrandGreen() {
        val seen = renderAll().getValue(true to false)
        assertEquals(Color(0xFF95D5A7), seen.primary)
        assertEquals(Color(0xFF10512E), seen.primaryContainer)
        assertEquals(Color(0xFF64DD93), seen.success)
        assertEquals(Color(0xFFFFB95F), seen.warning)
    }

    @Test
    fun bubblesAreDrawnFromTheSchemeSoTheyFollowMaterialYou() {
        for ((combo, seen) in renderAll()) {
            val (dark, dynamic) = combo
            run {
                assertEquals(seen.primaryContainer, seen.mine, "mine dark=$dark dynamic=$dynamic")
                assertEquals(seen.onPrimaryContainer, seen.mineText, "mineText dark=$dark dynamic=$dynamic")
                assertEquals(seen.surfaceContainerHigh, seen.theirs, "theirs dark=$dark dynamic=$dynamic")
                assertNotEquals(seen.mine, seen.theirs, "bubbles distinguishable dark=$dark dynamic=$dynamic")
            }
        }
    }
}
