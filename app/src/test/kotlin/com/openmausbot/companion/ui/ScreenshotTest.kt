package com.openmausbot.companion.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import com.openmausbot.companion.core.Bot
import com.openmausbot.companion.core.BotTask
import com.openmausbot.companion.core.Chat
import com.openmausbot.companion.core.Connection
import com.openmausbot.companion.core.Fleet
import com.openmausbot.companion.core.Frame
import com.openmausbot.companion.core.StreamFrame
import com.openmausbot.companion.core.target
import java.io.File
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.flow
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Renders the main screens to PNG for design review, with no device or emulator:
 * `app/build/screenshots/<screen>-<light|dark>.png`.
 *
 * Not a golden test. It asserts nothing about pixels, so it never fails on a
 * redesign; it exists so each redesign step can be looked at, before and after.
 * Uses the brand fallback scheme (no wallpaper under Robolectric), which is what
 * an Android 8-11 phone shows.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w393dp-h852dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@OptIn(ExperimentalTestApi::class)
class ScreenshotTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>(
        // The mascot animates forever; reduced motion lets the clock settle.
        effectContext = object : MotionDurationScale { override val scaleFactor = 0f },
    )

    private lateinit var server: MockWebServer
    private lateinit var scene: WiringScene

    private val scout = bot(id = "scout", name = "Scout", title = "Research").copy(
        threadId = "t-scout",
        color = "green",
        tasks = listOf(BotTask(threadId = "t-scout", title = "Competitor pricing", createdAt = 0.0)),
    )
    private val ada = bot(id = "ada", name = "Ada", title = "Code review").copy(
        threadId = "t-ada", color = "blue", unread = true,
    )
    private val penny = bot(id = "penny", name = "Penny", title = "Finance").copy(
        threadId = "t-penny", color = "orange",
    )

    private val history = """
        {"messages":[
          {"id":"m1","at":1786742400000,"role":"user","kind":"text",
           "text":"Can you compare the pricing pages of our three main competitors?"},
          {"id":"m2","at":1786742430000,"parentId":"m1","role":"bot","kind":"text",
           "text":"Sure. Here's what I found:\n\n- **Acme**: three tiers, from ${'$'}12 per seat\n- **Globex**: usage based, free up to 1,000 calls\n- **Initech**: a single ${'$'}49 plan\n\nAcme is the closest match to ours. Want a table?"},
          {"id":"m3","at":1786742460000,"parentId":"m2","role":"user","kind":"text",
           "text":"Yes please, and flag anything that changed this month."},
          {"id":"m4","at":1786742490000,"parentId":"m3","role":"bot","kind":"text",
           "text":"Globex raised its overage price from `${'$'}0.002` to `${'$'}0.003` per call on the 3rd. Nothing else moved."}
        ],"hasMore":false}
    """.trimIndent()

    @Before
    fun startServer() {
        server = MockWebServer()
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse = when {
                request.path == "/api/instances" -> json("""{"instances":[]}""")
                request.path?.startsWith("/api/threads/") == true -> json(history)
                else -> MockResponse().setResponseCode(404)
            }
        }
        server.start()
    }

    @After
    fun stopServer() {
        if (::scene.isInitialized) scene.session.disconnect()
        server.shutdown()
    }

    @Test fun rosterLight() = shoot("roster", dark = false, readyText = "Penny") { RosterScreen(CompanionNavigator()) }
    @Test fun rosterDark() = shoot("roster", dark = true, readyText = "Penny") { RosterScreen(CompanionNavigator()) }
    @Test fun chatLight() = shoot("chat", dark = false, readyText = "Nothing else moved.") { Chat() }
    @Test fun chatDark() = shoot("chat", dark = true, readyText = "Nothing else moved.") { Chat() }

    @Composable
    private fun Chat() {
        val destination = Destination.Chat(Chat.BotChat(scout).target)
        ChatScreen(
            destination = destination,
            onResolved = {},
            onBack = {},
            onOpenComputer = {},
            onOpenOverview = {},
            retainsDraft = { false },
        )
    }

    private fun shoot(name: String, dark: Boolean, readyText: String, content: @Composable () -> Unit) {
        val bots: List<Bot> = listOf(scout, ada, penny)
        scene = WiringScene(
            connection = Connection(id = "shots", name = "Studio Mac", host = "127.0.0.1", port = server.port),
            fleet = Fleet(bots, emptyList()),
            events = {
                flow {
                    emit(StreamFrame(Frame.Hello(cursor = "shots:1", resumed = false), seq = 1))
                    awaitCancellation()
                }
            },
        )
        compose.setContent {
            CompositionLocalProvider(LocalCompanion provides scene.environment) {
                CompanionTheme(darkTheme = dark, dynamicColor = false) {
                    // As RootScreen does: a Surface sets the content colour, so text
                    // and icons that inherit it read correctly in dark mode.
                    Surface(Modifier.fillMaxSize()) {
                        val state by scene.session.state.collectAsState()
                        if (state.bot(scout.id) != null) content()
                    }
                }
            }
        }
        compose.runOnIdle { scene.session.connect() }
        compose.waitUntil(5_000) { scene.session.state.value.bot(scout.id) != null }
        // Wait for what the screen is about (the last message, the last bot), not a clock.
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText(readyText, substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        compose.mainClock.advanceTimeBy(1_000)
        compose.waitForIdle()

        val out = File("build/screenshots").apply { mkdirs() }
        val bitmap = compose.runOnIdle {
            val view = compose.activity.window.decorView
            Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888).also { view.draw(Canvas(it)) }
        }
        File(out, "$name-${if (dark) "dark" else "light"}.png").outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }

    private fun json(body: String) = MockResponse().setHeader("Content-Type", "application/json").setBody(body)
}
