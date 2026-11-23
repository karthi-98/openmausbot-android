package com.openmausbot.companion.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openmausbot.companion.R
import com.openmausbot.companion.core.Bot
import com.openmausbot.companion.core.Chat
import com.openmausbot.companion.core.ChatSummary
import com.openmausbot.companion.core.Room
import com.openmausbot.companion.core.SearchHit
import com.openmausbot.companion.core.Session
import com.openmausbot.companion.core.chat
import com.openmausbot.companion.core.chatSummaries
import com.openmausbot.companion.core.forTask
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * The roster — the port of `ios/App/ChatListView.swift`.
 *
 * Messages-shaped: a header with you on the left and settings on the right, your
 * groups across the top, every bot below with the unread dot in the bot's own
 * colour at the left edge, and a bar floating at the bottom. The bar's pill is
 * Updates — only the bots that need you, are working, or have something you have
 * not read — beside round search and new-bot buttons. Everything scrolls under
 * the bar, which is why the list leaves [BAR_CLEARANCE] below its last row.
 *
 * Ordering is not decided here. `chatSummaries` (`:core`) folds pinned → unread →
 * last activity and hides hidden bots; [RosterLayout] decides which of those are
 * rows and which are tiles; the screen renders what it is handed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RosterScreen(navigator: CompanionNavigator) {
    val environment = LocalCompanion.current
    val session = environment.session
    val scope = rememberCoroutineScope()
    val haptics = rememberHaptics()
    val state by session.state.collectAsState()
    val connection by session.connection.collectAsState()
    val status by session.status.collectAsState()
    // The same preference the transcript folds by, from the same store: a reader
    // who turned activity off must not still read tool names here.
    val activityDetail by environment.chatPreferences.activityDetail.collectAsState()

    var bar by rememberSaveable(stateSaver = RosterBarSaver) { mutableStateOf(RosterBar()) }
    var hits by remember { mutableStateOf<List<SearchHit>>(emptyList()) }
    var searching by remember { mutableStateOf(false) }
    var refreshing by remember { mutableStateOf(false) }
    var showingUpdates by remember { mutableStateOf(false) }
    var showingNewGroup by remember { mutableStateOf(false) }
    var showingNewSection by remember { mutableStateOf(false) }
    // One createBot at a time: a second tap while the first is in flight
    // would race two bots into existence.
    var creatingBot by remember { mutableStateOf(false) }
    var managingThreads by remember { mutableStateOf<Chat?>(null) }

    val query = bar.query

    // Local filtering is always on; the computer is only asked past two
    // characters and after a quiet moment (§10).
    LaunchedEffect(query) {
        when (val decision = SearchPolicy.decide(query)) {
            SearchPolicy.Decision.Clear -> {
                hits = emptyList()
                searching = false
            }
            is SearchPolicy.Decision.Remote -> {
                searching = true
                delay(SearchPolicy.DEBOUNCE_MILLIS)
                hits = session.search(decision.query)
                searching = false
            }
        }
    }

    // Folding the fleet walks every thread's transcript, so it is keyed on the
    // state and the activity level alone: typing filters the fold instead of
    // repeating it.
    val summaries = remember(state, activityDetail) { state.chatSummaries(activityDetail) }
    // Only a search has rows to filter; the unsearched roster is assembled
    // section by section below.
    val rows = remember(summaries, query, state.queuedThreadIds) {
        rosterThreadRows(summaries, query, state.queuedThreadIds)
    }
    val approvals = remember(state) { state.pendingApprovals }
    val waiting = remember(state, approvals) { RosterLayout.waitingChats(state, approvals) }
    // One pass over the fleet rather than one per row: resolving a face walks the
    // chat's visible transcript.
    val faces = remember(state, summaries) {
        summaries.associate { it.id to MausState.forChat(it.chat, state) }
    }
    val summariesById = remember(summaries) { summaries.associateBy { it.id } }
    // Hoisted out of the list: read inside a lazy item, `state` would make that
    // item's recompose scope the whole fleet.
    val rooms = state.rooms
    val tiles = remember(state) {
        rooms.associate { it.id to RosterLayout.memberBots(state, it) }
    }
    // Read by the bar over the list and by nothing inside it, so the rows never
    // recompose for it. `approvals` is handed over rather than walked again.
    val updates = remember(state, approvals) { state.updates(approvals) }
    // The cross-bot Needs attention section rides above every roster section.
    val attention = remember(state) { state.crossBotAttention() }

    // The same rule the sheet picks from: two copies of "which bots can be
    // sectioned" could disagree about a hidden one.
    val canCreateSection = remember(state) { SectionRules.selectable(state).isNotEmpty() }
    var searchFilter by rememberSaveable { mutableStateOf(SearchFilter.ALL) }
    var plusMenuOpen by remember { mutableStateOf(false) }
    // Which folders are open. The groups and bots you have not filed are open by
    // default, the sections you filed them into closed — the way the reference
    // greets you with folders folded.
    var openFolders by rememberSaveable(stateSaver = StringSetSaver) {
        mutableStateOf(setOf(FOLDER_ATTENTION, FOLDER_GROUPS, FOLDER_BOTS))
    }
    val now = remember(state) { System.currentTimeMillis() }

    // The face at the top of home: the Chief of Staff, else the first pinned bot,
    // else the first bot. It is not listed again below.
    val hero: Bot? = remember(state) {
        state.unsectionedChief ?: state.pinnedBots.firstOrNull() ?: state.unsectionedBots.firstOrNull()
    }

    val openChat: (Chat) -> Unit = { chat ->
        navigator.open(
            if (chat is Chat.BotChat) environment.chatPreferences.restoringThread(chat, connection?.id) else chat,
        )
    }

    val row: @Composable (ChatSummary) -> Unit = { summary ->
        HomeRow(
            chat = summary.chat,
            preview = summary.preview,
            time = if (summary.lastActivity > 0) {
                RelativeStamp.list(summary.lastActivity, now, locale = Locale.getDefault())
            } else {
                ""
            },
            face = faces[summary.id] ?: MausState.IDLE,
            waiting = summary.id in waiting,
            onClick = { openChat(summary.chat) },
        )
    }

    val toggle: (String) -> Unit = { key ->
        haptics.play(HapticCue.SELECT)
        openFolders = if (key in openFolders) openFolders - key else openFolders + key
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = refreshing,
            onRefresh = {
                scope.launch {
                    refreshing = true
                    // Session.refresh waits until the stream leaves connecting
                    // (or 10s), so the spinner means what it appears to mean.
                    session.refresh()
                    refreshing = false
                }
            },
            modifier = Modifier.fillMaxSize(),
        ) {
            val searchingNow = query.isNotEmpty()
            val nothingToList =
                if (!searchingNow) !RosterLayout.listsAnyBot(summaries) else rows.isEmpty()
            if (nothingToList && hits.isEmpty()) {
                EmptyState(
                    title = if (!searchingNow) "No bots yet" else "Nothing matches",
                    description = if (!searchingNow) {
                        "Bots you create on your computer show up here."
                    } else {
                        "No thread matches “$query”."
                    },
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = TOP_CLEARANCE, bottom = BAR_CLEARANCE),
            ) {
                item(key = "status") { StatusBanner() }

                if (!searchingNow) {
                    hero?.let { bot ->
                        item(key = "hero-${bot.id}") {
                            Box(Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 22.dp), contentAlignment = Alignment.Center) {
                                HomeHero(
                                    bot = bot,
                                    face = faces[bot.id] ?: MausState.IDLE,
                                    chief = bot.chiefOfStaff == true,
                                    onClick = { openChat(Chat.BotChat(bot)) },
                                )
                            }
                        }
                    }

                    if (attention.isNotEmpty()) {
                        item(key = "folder-attention") {
                            HomeFolder(
                                title = "Needs you",
                                count = attention.size,
                                expanded = FOLDER_ATTENTION in openFolders,
                                onToggle = { toggle(FOLDER_ATTENTION) },
                            ) {
                                attention.forEach { entry ->
                                    AttentionRow(entry = entry, onOpen = {
                                        state.bots.firstOrNull { it.id == entry.botId }
                                            ?.let { bot -> Chat.BotChat(bot.forTask(entry.task.threadId) ?: bot) }
                                            ?.let(navigator::open)
                                    })
                                }
                                if (updates.isNotEmpty()) {
                                    Text(
                                        text = "All updates",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier
                                            .pressable(onClick = {
                                                haptics.play(TactileAction.OPEN_UPDATES)
                                                showingUpdates = true
                                            })
                                            .padding(horizontal = 19.dp, vertical = 10.dp),
                                    )
                                }
                            }
                        }
                    }

                    // Pinned bots stay loose under the hero, like favourites.
                    val pinned = state.pinnedBots.filter { it.id != hero?.id }.mapNotNull { summariesById[it.id] }
                    items(pinned, key = { "pinned-${it.id}" }) { row(it) }

                    val groups = (state.unsectionedChannels + state.botChats).mapNotNull { summariesById[it.id] }
                    if (groups.isNotEmpty()) {
                        item(key = "folder-groups") {
                            HomeFolder(
                                title = "Groups",
                                count = groups.size,
                                expanded = FOLDER_GROUPS in openFolders,
                                onToggle = { toggle(FOLDER_GROUPS) },
                            ) { groups.forEach { row(it) } }
                        }
                    }

                    val loose = state.unsectionedBots.filter { it.id != hero?.id }.mapNotNull { summariesById[it.id] }
                    if (loose.isNotEmpty()) {
                        item(key = "folder-bots") {
                            HomeFolder(
                                title = "Bots",
                                count = loose.size,
                                expanded = FOLDER_BOTS in openFolders,
                                onToggle = { toggle(FOLDER_BOTS) },
                            ) { loose.forEach { row(it) } }
                        }
                    }

                    // The desktop's sidebar sections, as folders: chiefs, then the
                    // section's groups, then its bots.
                    state.sidebarSections.forEach { section ->
                        val inFolder = section.chiefs.filter { it.id != hero?.id }.mapNotNull { summariesById[it.id] } +
                            section.channels.mapNotNull { summariesById[it.id] } +
                            section.bots.filter { it.id != hero?.id }.mapNotNull { summariesById[it.id] }
                        if (inFolder.isNotEmpty()) {
                            val key = "section:${section.id}"
                            item(key = "folder-$key") {
                                HomeFolder(
                                    title = section.name,
                                    count = inFolder.size,
                                    expanded = key in openFolders,
                                    onToggle = { toggle(key) },
                                ) { inFolder.forEach { row(it) } }
                            }
                        }
                    }
                } else {
                    val showsMessages = searchFilter == SearchFilter.ALL || searchFilter == SearchFilter.MESSAGES
                    if (showsMessages && hits.isNotEmpty()) {
                        item(key = "search-header") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp, bottom = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                SectionLabel("Messages")
                                Spacer(Modifier.weight(1f))
                                if (searching) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .padding(end = 20.dp)
                                            .size(14.dp),
                                        strokeWidth = 2.dp,
                                    )
                                }
                            }
                        }
                        items(hits, key = { it.id }) { hit ->
                            SearchHitRow(
                                hit = hit,
                                onClick = {
                                    scope.launch {
                                        // Session.open switches task, sets the
                                        // active branch, loads the `around` page
                                        // and focuses the message — the chat
                                        // screen honours the focus when it opens.
                                        session.open(hit)?.let {
                                            haptics.play(TactileAction.OPEN_SEARCH_RESULT)
                                            navigator.open(it)
                                        }
                                    }
                                },
                            )
                        }
                    }
                    val shown = rows.filter {
                        when (searchFilter) {
                            SearchFilter.ALL -> true
                            SearchFilter.MESSAGES -> false
                            SearchFilter.BOTS -> it.chat is Chat.BotChat
                            SearchFilter.GROUPS -> it.chat is Chat.RoomChat
                        }
                    }
                    if (shown.isNotEmpty() && showsMessages && hits.isNotEmpty()) {
                        item(key = "chats-label") {
                            SectionLabel("Threads", Modifier.padding(top = 14.dp, bottom = 4.dp))
                        }
                    }
                    items(shown, key = { "hit-row-${it.chat.threadId}" }) { row(it) }
                }
            }
        }

        // The page fades out under the floating buttons instead of meeting them
        // edge to edge (the reference: ~90% at the top, gone by ~126 dp).
        EdgeFade(top = true, height = TOP_FADE, modifier = Modifier.align(Alignment.TopCenter))
        EdgeFade(top = false, height = BOTTOM_FADE, modifier = Modifier.align(Alignment.BottomCenter))

        // Top: account and plus, or — while searching — the filter chips. They
        // cross-fade in 120 ms; nothing slides.
        Crossfade(
            targetState = bar.searchOpen,
            animationSpec = tween(Durations.SEARCH_CROSSFADE),
            label = "home-top",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 6.dp),
        ) { open ->
            if (open) {
                SearchChips(selected = searchFilter, onSelect = { searchFilter = it })
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PAGE_MARGIN),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AccountButton(name = connection?.name, onClick = { navigator.push(Destination.Settings) })
                    Spacer(Modifier.weight(1f))
                    Box {
                        ChromeButton(
                            icon = Icons.Filled.Add,
                            contentDescription = "New",
                            onClick = { plusMenuOpen = true },
                            size = CHROME_BUTTON,
                        )
                        AnchoredMenu(
                            expanded = plusMenuOpen,
                            onDismiss = { plusMenuOpen = false },
                            alignEnd = true,
                            items = listOf(
                                MenuItem("New bot", Icons.Filled.Create, enabled = !creatingBot) {
                                    if (!creatingBot) {
                                        creatingBot = true
                                        scope.launch {
                                            try {
                                                session.createBot()?.let {
                                                    haptics.play(TactileAction.CREATE_BOT_SUCCESS)
                                                    navigator.open(Chat.BotChat(it))
                                                }
                                            } finally {
                                                creatingBot = false
                                            }
                                        }
                                    }
                                },
                                MenuItem("New group", Icons.Filled.Person) {
                                    haptics.play(TactileAction.START_NEW_GROUP)
                                    showingNewGroup = true
                                },
                                MenuItem("New section", Icons.AutoMirrored.Filled.List, enabled = canCreateSection) {
                                    haptics.play(TactileAction.START_NEW_SECTION)
                                    showingNewSection = true
                                },
                            ),
                        )
                    }
                }
            }
        }

        HomeBottomBar(
            bar = bar,
            onBar = { bar = it },
            onOpenSearch = {
                haptics.play(TactileAction.OPEN_SEARCH)
                bar = bar.openSearch()
            },
            onConnectApps = { navigator.push(Destination.ConnectedApps) },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }

    if (showingUpdates) {
        UpdatesSheet(
            onOpen = { chat ->
                showingUpdates = false
                navigator.open(chat)
            },
            onDismiss = { showingUpdates = false },
        )
    }

    if (showingNewGroup) {
        NewGroupSheet(
            onCreated = { room ->
                showingNewGroup = false
                navigator.open(Chat.RoomChat(room))
            },
            onDismiss = { showingNewGroup = false },
        )
    }

    if (showingNewSection) {
        NewSectionSheet(onDismiss = { showingNewSection = false })
    }

    managingThreads?.let { chat ->
        TaskSheet(
            chat = chat,
            onDismiss = { managingThreads = null },
            onSelectTask = { target ->
                managingThreads = null
                session.state.value.chat(target)?.let(navigator::open)
            },
        )
    }
}

/** Two flags and a string: enough to survive a rotation with the search still up. */
private val RosterBarSaver = listSaver<RosterBar, Any>(
    save = { listOf(it.searchOpen, it.query) },
    restore = { RosterBar(searchOpen = it[0] as Boolean, query = it[1] as String) },
)

private val StringSetSaver = listSaver<Set<String>, String>(
    save = { it.toList() },
    restore = { it.toSet() },
)

/** A heading over a stretch of the list. */
@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = RosterLayout.sectionLabel(text),
        style = MaterialTheme.typography.labelLarge,
        letterSpacing = 0.4.sp,
        color = secondaryTint,
        modifier = modifier.padding(horizontal = 20.dp),
    )
}

private const val FOLDER_ATTENTION = "attention"
private const val FOLDER_GROUPS = "groups"
private const val FOLDER_BOTS = "bots"

/** Clear of the floating buttons at the top: 6 dp + a 43 dp button + 14 dp. */
private val TOP_CLEARANCE = 63.dp

/** Room for the floating bar, so the last row can scroll clear of it. */
private val BAR_CLEARANCE = 80.dp

private val TOP_FADE = 96.dp
private val BOTTOM_FADE = 64.dp

/** What a search shows: the reference's filter chips, for what this app has. */
internal enum class SearchFilter(val label: String) {
    ALL("All"),
    MESSAGES("Messages"),
    BOTS("Bots"),
    GROUPS("Groups"),
}

/**
 * The page colour fading into the content at an edge, so rows pass under the
 * floating controls instead of meeting them edge to edge.
 */
@Composable
private fun EdgeFade(top: Boolean, height: Dp, modifier: Modifier = Modifier) {
    val page = MaterialTheme.colorScheme.background
    val solid = page.copy(alpha = 0.92f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(
                Brush.verticalGradient(
                    if (top) listOf(solid, page.copy(alpha = 0f)) else listOf(page.copy(alpha = 0f), solid),
                ),
            ),
    )
}

/** The chip row that replaces account and plus while searching. */
@Composable
private fun SearchChips(selected: SearchFilter, onSelect: (SearchFilter) -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = PAGE_MARGIN),
        modifier = Modifier
            .fillMaxWidth()
            .height(CHROME_BUTTON),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items(SearchFilter.entries.toList(), key = { it.name }) { filter ->
            val on = filter == selected
            Box(
                modifier = Modifier
                    .height(33.dp)
                    .background(
                        if (on) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                        CircleShape,
                    )
                    .border(Dp.Hairline, if (on) Color.Transparent else AppColors.hairline, CircleShape)
                    .pressable(onClick = { onSelect(filter) }, role = Role.Tab)
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = filter.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (on) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

/**
 * The bar along the bottom: connect-apps beside a full-round Search pill or,
 * once search is open, the pill as a field beside a round ✕. The swap is
 * instant, as in the reference; the pill rides the keyboard because the screen's
 * safe-drawing padding includes the IME.
 */
@Composable
private fun HomeBottomBar(
    bar: RosterBar,
    onBar: (RosterBar) -> Unit,
    onOpenSearch: () -> Unit,
    onConnectApps: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focus = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    LaunchedEffect(bar.searchOpen) {
        if (bar.searchOpen) focus.requestFocus() else focusManager.clearFocus()
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = PAGE_MARGIN, end = PAGE_MARGIN, bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!bar.searchOpen) {
            ChromeButton(
                painter = painterResource(R.drawable.ic_connect_apps),
                contentDescription = "Connect apps",
                onClick = onConnectApps,
                size = CHROME_BUTTON,
            )
        }
        Row(
            modifier = Modifier
                .weight(1f)
                .height(PILL_HEIGHT)
                .chromeCapsule()
                .then(if (bar.searchOpen) Modifier else Modifier.pressable(onClick = onOpenSearch))
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
            Box(modifier = Modifier.weight(1f)) {
                if (!bar.searchOpen || bar.query.isEmpty()) {
                    Text("Search", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (bar.searchOpen) {
                    BasicTextField(
                        value = bar.query,
                        onValueChange = { onBar(bar.typed(it)) },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focus),
                    )
                }
            }
            if (bar.searchOpen && bar.query.isNotEmpty()) {
                TouchTarget(onClick = { onBar(bar.clearQuery()) }, size = 24.dp) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Clear search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
        if (bar.searchOpen) {
            ChromeButton(
                icon = Icons.Filled.Close,
                contentDescription = "Close search",
                onClick = { onBar(bar.cancelSearch()) },
                size = CHROME_BUTTON,
            )
        }
    }
}

private val PILL_HEIGHT = 43.dp

@Composable
private fun SearchHitRow(hit: SearchHit, onClick: () -> Unit) {
    val now = remember(hit.id) { System.currentTimeMillis() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        // Who said it. Two hits with the same words are otherwise identical.
        Icon(
            // A person for what you said, the Maus mark for what a bot said —
            // iOS makes the same split with person.fill and a speech bubble.
            painter = if (SearchHitRole.isFromUser(hit.role)) {
                rememberVectorPainter(Icons.Filled.Person)
            } else {
                painterResource(R.drawable.ic_maus_mark)
            },
            contentDescription = SearchHitRole.contentDescription(hit.role, hit.name),
            tint = secondaryTint,
            modifier = Modifier
                .size(26.dp)
                .background(secondaryTint.copy(alpha = 0.13f), CircleShape)
                .padding(5.dp),
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(hit.name, style = MaterialTheme.typography.titleSmall)
                hit.task?.takeIf { it.isNotEmpty() }?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = secondaryTint)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = RelativeStamp.list(hit.at, now, locale = Locale.getDefault()),
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryTint,
                )
            }
            Text(
                text = hit.snippet,
                style = MaterialTheme.typography.bodyMedium,
                color = secondaryTint,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Connection state, shown only when it is not "fine". */
@Composable
fun StatusBanner() {
    val session = LocalCompanion.current.session
    val status by session.status.collectAsState()
    val banner: Pair<String, Color>? = when (val current = status) {
        Session.Status.Live, Session.Status.Unpaired -> null
        Session.Status.Connecting -> "Connecting…" to secondaryTint
        is Session.Status.Offline -> current.message to Color(MausPalette.argb("orange"))
        Session.Status.Unauthorized ->
            "This phone was unpaired on the computer." to MaterialTheme.colorScheme.error
    }
    val (text, tint) = banner ?: return
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = tint,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .background(secondaryTint.copy(alpha = 0.12f), CircleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}
