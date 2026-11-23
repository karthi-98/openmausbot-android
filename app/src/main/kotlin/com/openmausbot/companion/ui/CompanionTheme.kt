package com.openmausbot.companion.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDirection

/*
 * The palette: neutral greys with white (dark) or black (light) as the only
 * "colour" on controls, the way the reference app is built. Colour in the app
 * comes from the bots' mascots, not from the chrome. Fixed, not from the
 * wallpaper: the same greys on every phone.
 *
 * Dark values are sampled from the reference on a phone (sRGB). How they map:
 *
 *   background / surface        #141414  the page
 *   surfaceContainerLow         #1E1E1E  sheets
 *   surfaceContainer            #212121  a bot's message
 *   surfaceContainerHigh        #2C2C2E  cards, pills, the composer, menus
 *   surfaceContainerHighest     #3D3D42  raised round buttons
 *   outlineVariant              #373739  dividers
 *   onSurfaceVariant            #95959B  secondary text and icons
 *   outline                     #66666A  placeholders
 *   primary / onPrimary         white / black: the one filled action (send, voice)
 *   tertiary                    #116DD2  the unread dot
 *
 * surfaceTint matches the surface, so Material's tonal elevation adds no tint.
 */
private val NeutralDarkColors = darkColorScheme(
    primary = Color(0xFFFFFFFF),
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF2C2C2E),
    onPrimaryContainer = Color(0xFFFFFFFF),
    inversePrimary = Color(0xFF000000),
    secondary = Color(0xFF95959B),
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFF3D3D42),
    onSecondaryContainer = Color(0xFFFFFFFF),
    tertiary = Color(0xFF116DD2),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF0B3A6E),
    onTertiaryContainer = Color(0xFFD6E6FA),
    background = Color(0xFF141414),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF141414),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF2C2C2E),
    onSurfaceVariant = Color(0xFF95959B),
    surfaceTint = Color(0xFF141414),
    inverseSurface = Color(0xFFFFFFFF),
    inverseOnSurface = Color(0xFF141414),
    error = Color(0xFFFF453A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFF3E1714),
    onErrorContainer = Color(0xFFFFB4AB),
    outline = Color(0xFF66666A),
    outlineVariant = Color(0xFF373739),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFF3D3D42),
    surfaceDim = Color(0xFF141414),
    surfaceContainerLowest = Color(0xFF0E0E0E),
    surfaceContainerLow = Color(0xFF1E1E1E),
    surfaceContainer = Color(0xFF212121),
    surfaceContainerHigh = Color(0xFF2C2C2E),
    surfaceContainerHighest = Color(0xFF3D3D42),
)

/** The same roles in light. Provisional: to be replaced by values sampled from the reference in light mode. */
private val NeutralLightColors = lightColorScheme(
    primary = Color(0xFF000000),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEFEFF0),
    onPrimaryContainer = Color(0xFF000000),
    inversePrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF6E6E73),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE5E5EA),
    onSecondaryContainer = Color(0xFF000000),
    tertiary = Color(0xFF116DD2),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD6E6FA),
    onTertiaryContainer = Color(0xFF0B3A6E),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF000000),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF000000),
    surfaceVariant = Color(0xFFEFEFF0),
    onSurfaceVariant = Color(0xFF6E6E73),
    surfaceTint = Color(0xFFFFFFFF),
    inverseSurface = Color(0xFF141414),
    inverseOnSurface = Color(0xFFFFFFFF),
    error = Color(0xFFFF3B30),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFE5E3),
    onErrorContainer = Color(0xFF8A1C14),
    outline = Color(0xFFA1A1A6),
    outlineVariant = Color(0xFFE5E5EA),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFFFFFFFF),
    surfaceDim = Color(0xFFEDEDED),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF7F7F7),
    surfaceContainer = Color(0xFFF4F4F4),
    surfaceContainerHigh = Color(0xFFEFEFF0),
    surfaceContainerHighest = Color(0xFFE5E5EA),
)

/**
 * The two meanings Material's scheme has no role for: something went right
 * (a voice provider is configured, a step succeeded) and something needs a look
 * (a degraded call, an incomplete setting). Errors use the scheme's own `error`
 * roles.
 *
 * System-style green and orange, chosen to read on the neutral greys in each theme.
 */
@Immutable
data class StatusColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
)

private val LightStatusColors = StatusColors(
    success = Color(0xFF248A3D),
    onSuccess = Color(0xFFFFFFFF),
    successContainer = Color(0xFFDDF5E3),
    onSuccessContainer = Color(0xFF0B3D17),
    warning = Color(0xFFC93400),
    onWarning = Color(0xFFFFFFFF),
    warningContainer = Color(0xFFFFE8D6),
    onWarningContainer = Color(0xFF4A1A00),
)

private val DarkStatusColors = StatusColors(
    success = Color(0xFF30D158),
    onSuccess = Color(0xFF000000),
    successContainer = Color(0xFF123D1E),
    onSuccessContainer = Color(0xFFC9F5D4),
    warning = Color(0xFFFF9F0A),
    onWarning = Color(0xFF000000),
    warningContainer = Color(0xFF4A2E00),
    onWarningContainer = Color(0xFFFFE2B8),
)

private val LocalStatusColors = staticCompositionLocalOf { LightStatusColors }

/** Success and warning colours for the scheme in force: `AppColors.status.warning`. */
object AppColors {
    val status: StatusColors
        @Composable get() = LocalStatusColors.current
}

/**
 * Reading direction comes from the words, not from the locale's layout.
 *
 * Compose's default is [TextDirection.Unspecified], and — unlike the platform's
 * own `TextView`, whose default is `TEXT_DIRECTION_FIRST_STRONG` — it resolves
 * straight off `LocalLayoutDirection`: an RTL locale gives *every* paragraph an
 * RTL base level, English or not. On an `ar-EG` device that produced three
 * measured defects, all of them the same bidi reordering:
 *
 *  - trailing punctuation jumped to the front — *"What do you mostly want help
 *    with?"* drew as *"?What do you mostly want help with"*, because a neutral
 *    `?` at the end of an LTR run inside an RTL paragraph resolves to the
 *    paragraph level and is reordered to the paragraph's leading edge;
 *  - a counted list came out backwards — the bot's *"1 2 3 … 20"* drew as
 *    *"16 15 … 1"*, because digits are weak, the spaces between them resolve to
 *    R, and the whole sequence lays out right to left;
 *  - one-line roster previews lost their opening characters — *"error: CUA
 *    Driver is not ready…"* drew as *"ror: CUA Driver…"*, the line being aligned
 *    to the right and ellipsised at what the paragraph thought was its start.
 *
 * [TextDirection.ContentOrLtr] resolves the base level from the first strong
 * character instead: English stays LTR with its punctuation in place, Arabic a
 * user types stays RTL, and text with no strong character at all — a bare
 * number, a timestamp, a path — falls back to LTR rather than to the locale.
 * That last fallback is the one place this goes past `TextView`'s
 * `FIRST_STRONG` (which falls back to the layout direction), and it is what
 * fixes the counted list.
 *
 * It is a choice about this product, not a fact about text, and it has a price:
 * a paragraph with no strong character is anchored LTR even when an Arabic user
 * typed it — Arabic-Indic digits, an emoji-only line, bare punctuation from the
 * composer. The choice is made for what this app actually carries: counts,
 * clock times, file paths, code and command output, read left to right whoever
 * is holding the phone. If the app ever ships an RTL translation, or grows text
 * where a neutral line should follow the reader rather than the content, this
 * is the line to come back to.
 *
 * This is only about the direction glyphs are *ordered* in. Where the box sits
 * is untouched: `Row`/`Column`, `padding(start=)`, `placeRelative` and the
 * auto-mirrored icons all keep reading `LocalLayoutDirection`, so the chrome,
 * the bubble sides and the table's mirrored columns still follow the locale.
 *
 * It is set on the theme's [Typography] rather than at every call site, so it
 * reaches whatever Compose draws under this theme — the message bubbles, the
 * roster previews, the labels — by way of `LocalTextStyle` and the
 * `MaterialTheme.typography` slots, with nothing new to remember at the next
 * call site.
 *
 * Its reach ends where the theme does. Notifications are drawn by SystemUI in
 * its own window, from strings that never pass through a Compose typography, so
 * the same policy has to be carried there inside the string itself — see
 * `notifications.NotificationText`, which anchors each notification paragraph
 * that would otherwise take the shade's base direction.
 */
private fun TextStyle.readingFromContent(): TextStyle =
    copy(textDirection = TextDirection.ContentOrLtr)

private val ContentDirectedTypography: Typography = Typography().run {
    copy(
        displayLarge = displayLarge.readingFromContent(),
        displayMedium = displayMedium.readingFromContent(),
        displaySmall = displaySmall.readingFromContent(),
        headlineLarge = headlineLarge.readingFromContent(),
        headlineMedium = headlineMedium.readingFromContent(),
        headlineSmall = headlineSmall.readingFromContent(),
        titleLarge = titleLarge.readingFromContent(),
        titleMedium = titleMedium.readingFromContent(),
        titleSmall = titleSmall.readingFromContent(),
        bodyLarge = bodyLarge.readingFromContent(),
        bodyMedium = bodyMedium.readingFromContent(),
        bodySmall = bodySmall.readingFromContent(),
        labelLarge = labelLarge.readingFromContent(),
        labelMedium = labelMedium.readingFromContent(),
        labelSmall = labelSmall.readingFromContent(),
    )
}

@Composable
fun CompanionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalStatusColors provides if (darkTheme) DarkStatusColors else LightStatusColors) {
        MaterialTheme(
            colorScheme = if (darkTheme) NeutralDarkColors else NeutralLightColors,
            typography = ContentDirectedTypography,
            content = content,
        )
    }
}

/** The muted foreground for secondary text and icons: Material's `onSurfaceVariant`. */
val secondaryTint: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
