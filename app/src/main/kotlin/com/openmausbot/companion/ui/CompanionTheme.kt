package com.openmausbot.companion.ui

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDirection

/*
 * Material You.
 *
 * On Android 12+ the whole scheme comes from the wallpaper
 * ([dynamicLightColorScheme] / [dynamicDarkColorScheme]), so the app takes on the
 * colours of the phone it runs on, the way the system apps do.
 *
 * Below 12 there is no wallpaper palette, so the schemes below stand in: the full
 * Material 3 tonal-spot scheme generated (material-color-utilities 0.3.0,
 * `SchemeTonalSpot`, contrast 0) from the MausBot green `#009957`. Every role is
 * filled in, so surfaces, containers and outlines stay coherent instead of
 * falling back to Material's purple baseline for whatever was left unset.
 */
private val BrandLightColors = lightColorScheme(
    primary = Color(0xFF2D6A45),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFB1F1C2),
    onPrimaryContainer = Color(0xFF10512E),
    inversePrimary = Color(0xFF95D5A7),
    secondary = Color(0xFF4F6353),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD2E8D4),
    onSecondaryContainer = Color(0xFF384B3D),
    tertiary = Color(0xFF3A646F),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFBEEAF7),
    onTertiaryContainer = Color(0xFF214C57),
    background = Color(0xFFF6FBF3),
    onBackground = Color(0xFF181D18),
    surface = Color(0xFFF6FBF3),
    onSurface = Color(0xFF181D18),
    surfaceVariant = Color(0xFFDCE5DB),
    onSurfaceVariant = Color(0xFF414942),
    surfaceTint = Color(0xFF2D6A45),
    inverseSurface = Color(0xFF2C322D),
    inverseOnSurface = Color(0xFFEDF2EB),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),
    outline = Color(0xFF717971),
    outlineVariant = Color(0xFFC0C9BF),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFFF6FBF3),
    surfaceContainer = Color(0xFFEAEFE8),
    surfaceContainerHigh = Color(0xFFE5EAE2),
    surfaceContainerHighest = Color(0xFFDFE4DD),
    surfaceContainerLow = Color(0xFFF0F5ED),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceDim = Color(0xFFD6DBD4),
)

private val BrandDarkColors = darkColorScheme(
    primary = Color(0xFF95D5A7),
    onPrimary = Color(0xFF00391D),
    primaryContainer = Color(0xFF10512E),
    onPrimaryContainer = Color(0xFFB1F1C2),
    inversePrimary = Color(0xFF2D6A45),
    secondary = Color(0xFFB6CCB9),
    onSecondary = Color(0xFF223527),
    secondaryContainer = Color(0xFF384B3D),
    onSecondaryContainer = Color(0xFFD2E8D4),
    tertiary = Color(0xFFA2CDDA),
    onTertiary = Color(0xFF023640),
    tertiaryContainer = Color(0xFF214C57),
    onTertiaryContainer = Color(0xFFBEEAF7),
    background = Color(0xFF101510),
    onBackground = Color(0xFFDFE4DD),
    surface = Color(0xFF101510),
    onSurface = Color(0xFFDFE4DD),
    surfaceVariant = Color(0xFF414942),
    onSurfaceVariant = Color(0xFFC0C9BF),
    surfaceTint = Color(0xFF95D5A7),
    inverseSurface = Color(0xFFDFE4DD),
    inverseOnSurface = Color(0xFF2C322D),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF8B938A),
    outlineVariant = Color(0xFF414942),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFF353B36),
    surfaceContainer = Color(0xFF1C211C),
    surfaceContainerHigh = Color(0xFF262B27),
    surfaceContainerHighest = Color(0xFF313631),
    surfaceContainerLow = Color(0xFF181D18),
    surfaceContainerLowest = Color(0xFF0A0F0B),
    surfaceDim = Color(0xFF101510),
)

/**
 * The two meanings Material's scheme has no role for: something went right
 * (a voice provider is configured, a step succeeded) and something needs a look
 * (a degraded call, an incomplete setting). Errors use the scheme's own `error`
 * roles.
 *
 * Material tones (40 / 100 / 90 / 10 light, 80 / 20 / 30 / 90 dark) of the mascot
 * green and orange, harmonized toward the brand seed so they sit with the scheme.
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
    success = Color(0xFF006D3C),
    onSuccess = Color(0xFFFFFFFF),
    successContainer = Color(0xFF81FAAD),
    onSuccessContainer = Color(0xFF00210E),
    warning = Color(0xFF855300),
    onWarning = Color(0xFFFFFFFF),
    warningContainer = Color(0xFFFFDDB8),
    onWarningContainer = Color(0xFF2A1700),
)

private val DarkStatusColors = StatusColors(
    success = Color(0xFF64DD93),
    onSuccess = Color(0xFF00391D),
    successContainer = Color(0xFF00522C),
    onSuccessContainer = Color(0xFF81FAAD),
    warning = Color(0xFFFFB95F),
    onWarning = Color(0xFF472A00),
    warningContainer = Color(0xFF653E00),
    onWarningContainer = Color(0xFFFFDDB8),
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
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme: ColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> BrandDarkColors
        else -> BrandLightColors
    }
    CompositionLocalProvider(LocalStatusColors provides if (darkTheme) DarkStatusColors else LightStatusColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = ContentDirectedTypography,
            content = content,
        )
    }
}

/** The muted foreground for secondary text and icons: Material's `onSurfaceVariant`. */
val secondaryTint: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
