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

/** The neutral palette, and which roles the bubbles and status colours come from. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CompanionThemeTest {
    @get:Rule
    val compose = createComposeRule()

    private class Seen {
        var background = Color.Unspecified
        var primary = Color.Unspecified
        var card = Color.Unspecified
        var secondaryText = Color.Unspecified
        var primaryContainer = Color.Unspecified
        var onPrimaryContainer = Color.Unspecified
        var surfaceContainer = Color.Unspecified
        var mine = Color.Unspecified
        var mineText = Color.Unspecified
        var theirs = Color.Unspecified
        var success = Color.Unspecified
    }

    /** Light and dark, drawn side by side in one composition. */
    private fun renderBoth(): Map<Boolean, Seen> {
        val seen = mapOf(false to Seen(), true to Seen())
        compose.setContent {
            for ((dark, out) in seen) {
                CompanionTheme(darkTheme = dark) {
                    val scheme = MaterialTheme.colorScheme
                    out.background = scheme.background
                    out.primary = scheme.primary
                    out.card = scheme.surfaceContainerHigh
                    out.secondaryText = scheme.onSurfaceVariant
                    out.primaryContainer = scheme.primaryContainer
                    out.onPrimaryContainer = scheme.onPrimaryContainer
                    out.surfaceContainer = scheme.surfaceContainer
                    out.mine = BubbleColor.mine
                    out.mineText = BubbleColor.mineText
                    out.theirs = BubbleColor.theirs
                    out.success = AppColors.status.success
                }
            }
        }
        compose.waitForIdle()
        return seen
    }

    @Test
    fun darkIsTheNeutralPaletteSampledFromTheReference() {
        val dark = renderBoth().getValue(true)
        assertEquals(Color(0xFF141414), dark.background)
        assertEquals(Color(0xFFFFFFFF), dark.primary)
        assertEquals(Color(0xFF2C2C2E), dark.card)
        assertEquals(Color(0xFF95959B), dark.secondaryText)
        assertEquals(Color(0xFF212121), dark.theirs)
    }

    @Test
    fun lightInvertsTheOneFilledAction() {
        val light = renderBoth().getValue(false)
        assertEquals(Color(0xFFFFFFFF), light.background)
        assertEquals(Color(0xFF000000), light.primary)
    }

    @Test
    fun bubblesAreDrawnFromTheSchemeAndStayDistinct() {
        for ((dark, seen) in renderBoth()) {
            assertEquals(seen.primaryContainer, seen.mine, "mine dark=$dark")
            assertEquals(seen.onPrimaryContainer, seen.mineText, "mineText dark=$dark")
            assertEquals(seen.surfaceContainer, seen.theirs, "theirs dark=$dark")
            assertNotEquals(seen.mine, seen.theirs, "bubbles distinguishable dark=$dark")
            assertNotEquals(seen.background, seen.theirs, "bot bubble stands off the page dark=$dark")
        }
    }
}
