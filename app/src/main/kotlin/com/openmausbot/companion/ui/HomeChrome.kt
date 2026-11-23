package com.openmausbot.companion.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.openmausbot.companion.core.Bot
import com.openmausbot.companion.core.Chat

/*
 * The home screen's pieces, at the reference's measurements
 * (docs/redesign/reference/motion-spec.md, §2 Home): a big centred bot, folders
 * that open in place, 78 dp rows, round buttons floating at the corners and a
 * search pill along the bottom.
 */

/** Left and right margin of everything that floats or lines up on the page. */
internal val PAGE_MARGIN: Dp = 17.dp

/** The account button: the initial of the person paired, on the raised grey. */
@Composable
internal fun AccountButton(name: String?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TouchTarget(onClick = onClick, size = CHROME_BUTTON, contentDescription = "Account and settings", modifier = modifier) {
        Box(
            modifier = Modifier
                .size(CHROME_BUTTON)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape)
                .border(Dp.Hairline, AppColors.hairline, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = name?.trim()?.firstOrNull()?.uppercase() ?: "•",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

/**
 * The bot at the top of home: a 93 dp face, a star when it is the Chief of
 * Staff, the name beneath in small type.
 */
@Composable
internal fun HomeHero(bot: Bot, face: MausState, chief: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.pressable(onClick = onClick, onClickLabel = "Chat with ${bot.name}"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.size(HERO + 6.dp)) {
            BotAvatar(bot = bot, size = HERO, state = face, modifier = Modifier.align(Alignment.Center))
            if (chief) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 2.dp, y = (-4).dp)
                        .size(30.dp)
                        .background(MaterialTheme.colorScheme.background, CircleShape)
                        .padding(3.dp)
                        .background(AppColors.status.warning, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Chief of Staff",
                        tint = Color.White,
                        modifier = Modifier.size(15.dp),
                    )
                }
            }
        }
        Spacer(Modifier.height(7.dp))
        Text(
            text = bot.name,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 18.sp),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private val HERO = 93.dp

/**
 * A folder of rows that opens in place. Collapsed: "Name  7  ›". Open: "Name ⌄",
 * the count gone at once, the chevron turning over 120 ms, the rows revealed by
 * a 230 ms ease-out height animation and folded away by a 130 ms ease-in one.
 * The header itself shows no press feedback, as in the reference.
 */
@Composable
internal fun HomeFolder(
    title: String,
    count: Int,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val turn by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f,
        animationSpec = tween(Durations.CHEVRON, easing = EaseOutCubic),
        label = "folder-chevron",
    )
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(FOLDER_HEADER)
                .pressable(
                    onClick = onToggle,
                    press = Press.NONE,
                    onClickLabel = if (expanded) "Collapse $title" else "Expand $title",
                )
                .padding(horizontal = 19.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            if (!expanded) {
                Spacer(Modifier.width(10.dp))
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
            Spacer(Modifier.width(6.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer { rotationZ = turn },
            )
        }
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(tween(Durations.FOLDER_EXPAND, easing = EaseOutCubic), expandFrom = Alignment.Top) +
                fadeIn(tween(40)),
            exit = fadeOut(tween(30)) +
                shrinkVertically(tween(Durations.FOLDER_COLLAPSE, easing = EaseInCubic), shrinkTowards = Alignment.Top),
        ) {
            Column(content = content)
        }
    }
}

private val FOLDER_HEADER = 42.dp

/**
 * One conversation, 78 dp: a 46.5 dp face, the name in medium weight with the
 * time at the right, one line of preview beneath with the unread dot at its end.
 * Pressed, the content shrinks to 96% and dims to 77%; the background never
 * changes.
 */
@Composable
internal fun HomeRow(
    chat: Chat,
    preview: String,
    time: String,
    face: MausState,
    waiting: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(ROW_HEIGHT)
            .pressable(onClick = onClick, press = Press.ROW)
            .padding(start = 19.dp, end = 21.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ChatAvatar(chat = chat, size = ROW_AVATAR, state = face, animated = false)
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = chat.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (time.isNotEmpty()) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = time,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (waiting) "Waiting on you" else preview.ifEmpty { " " },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (waiting) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                when {
                    chat.busy -> {
                        Spacer(Modifier.width(8.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            strokeWidth = 1.5.dp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    chat.unread -> {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            Modifier
                                .size(10.dp)
                                .background(MaterialTheme.colorScheme.tertiary, CircleShape),
                        )
                    }
                }
            }
        }
    }
}

private val ROW_HEIGHT = 78.dp
private val ROW_AVATAR = 46.5.dp

/** One line of an anchored menu. */
internal data class MenuItem(
    val label: String,
    val icon: ImageVector,
    val enabled: Boolean = true,
    val onClick: () -> Unit,
)

/**
 * An anchored menu the way the reference draws one: 240 dp wide, 16 dp corners,
 * a hairline, 41 dp lines with a 17 dp grey glyph, 10 dp from what opened it —
 * and no animation at all, in or out, and no scrim.
 */
@Composable
internal fun AnchoredMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    items: List<MenuItem>,
    alignEnd: Boolean,
    above: Boolean = false,
) {
    if (!expanded) return
    val gap = with(androidx.compose.ui.platform.LocalDensity.current) { 10.dp.roundToPx() }
    Popup(
        popupPositionProvider = AnchorBelow(gap, alignEnd, above),
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true),
    ) {
        Column(
            modifier = Modifier
                .width(240.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(16.dp))
                .border(Dp.Hairline, AppColors.hairline, RoundedCornerShape(16.dp))
                .padding(vertical = 6.dp),
        ) {
            items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(41.dp + 6.dp)
                        .pressable(
                            onClick = {
                                onDismiss()
                                item.onClick()
                            },
                            press = Press.ROW,
                            enabled = item.enabled,
                        )
                        .padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(19.dp),
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (item.enabled) 1f else 0.38f),
                    )
                }
            }
        }
    }
}

/** Under (or over) the anchor, flush with its left or right edge. */
private class AnchorBelow(private val gap: Int, private val alignEnd: Boolean, private val above: Boolean) :
    PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val x = if (alignEnd) anchorBounds.right - popupContentSize.width else anchorBounds.left
        val y = if (above) anchorBounds.top - gap - popupContentSize.height else anchorBounds.bottom + gap
        return IntOffset(
            x.coerceIn(0, (windowSize.width - popupContentSize.width).coerceAtLeast(0)),
            y.coerceIn(0, (windowSize.height - popupContentSize.height).coerceAtLeast(0)),
        )
    }
}
