# Grok Bot (ai.x.grok.bot): motion, layout and colour spec

Measured on a OnePlus 8T (1080x2400, density 2.625, Android 14) on 2026-11-23. Recordings were made with scrcpy at 60 fps, which writes only frames that changed, so a run of frames about 16 ms apart is an animation. Positions come from frame-by-frame pixel tracking. Video frames were 360x800 (1/3 scale), so motion positions are accurate to about 3 device px. Static geometry comes from full-resolution screenshots. dp = px / 2.625.

Caveat: scrcpy sometimes drops the first 1–3 frames of a large change (for example a sheet entering). Durations below are measured from the first frame that moved to the last frame that changed, and are rounded.

---

## 1. Colours

### Dark theme (the theme actually observed)
| Token | Hex | Where |
|---|---|---|
| background | `#141414` (reads `#131313`–`#141414`) | home, chat, search |
| sheet background | `#1E1E1E` | account / create-bot / connect-apps sheets |
| surface (cards, pills, composer, menus, unselected chips) | `#2C2C2E` (chips `#2B2B2E`) | |
| chat bubble fill | `#212020` (measured 33,32,32) | bot message cards. Darker than `#2C2C2E`. |
| raised round button fill (K avatar) | `#3D3D42` | |
| hairline border on round buttons / pills / menus | `#4B4B4D` (75,75,77), 1 px | every floating circle and pill has it |
| card border (sheet cards) | `#373739`, 1 px | |
| divider | `#373739`, 1 px, inset 34 dp from the screen edge (18 dp inside the card) | |
| primary text | `#FFFFFF` | titles, body |
| secondary text | `#95959B` | previews, folder headers, sheet subtitles |
| tertiary text (timestamps) | about `#555558` (sampled max 85,85,88) | thread-row times |
| K monogram glyph | `#A5A5AD` | |
| unread dot | `#116DD2` | 10 dp circle |
| primary-bot badge | `#FF9800` | star badge on the home avatar |
| selected chip | `#FFFFFF` fill | search filter "All" |
| primary action | white fill, black glyph/text | voice button, "Upgrade" |
| toggle | track `#99999F`, black thumb | |
| destructive menu item | salmon / red, about `#F08080` (visual estimate) | folder long-press "Delete" |
| sheet scrim | black at about 30% (home bg `#141414` reads `#0E0E0E` under it) | fades with the sheet |
| popup-menu scrim | none (background unchanged under ⋯ menu, + menu and folder menu) | |
| top/bottom content fade (chat) | gradient of the background colour, see 4.6 | |

### Light theme
**Not observed.** The brief says the phone was in light mode, but `adb shell cmd uimode night` returned `Night mode: yes` and the app rendered dark throughout. I left system settings alone, as the rules require, so no light-theme values were sampled.

---

## 2. Geometry (dp)

### Global
- Horizontal screen margin for floating controls and cards: **17–18 dp** (46 px).
- Floating circular buttons (K, +, back, ⋯, grid, sheet ✕): **42.7 dp** diameter (112 px), fill `#2C2C2E` (K uses `#3D3D42`), 1 px `#4B4B4D` ring, glyph about 19 dp.
- Top floating row: button tops at 45 dp from the screen top (y=118 px), so they sit just below the status bar. They float over content (see 4.6).

### Home
- Bot hero avatar: 93 dp circle (244 px), centred. Star badge 30 dp, overlapping the bottom-right with a dark ring. Name label about 7 dp below.
- Folder header row: height 41.5 dp, text starting at 19.4 dp. Collapsed: "Name  7  ›". Expanded: "Name ⌄" with the count hidden.
- Thread row: **height 78 dp** (204 px). Avatar 46.5 dp at x=19.4 dp. Text column starts at 80 dp. Title and preview are single-line with ellipsis. Time is right-aligned with its right edge 21 dp from the screen edge. Unread dot is 10 dp, right-aligned, on the preview baseline.
- Group avatar: a 2x2 cluster of 26 dp blobs with a "+3" label.
- Bottom bar: circular grid button (42.7 dp), a 12.6 dp gap, then a **Search pill, 43 dp tall, full-round**, running to the right margin. Bar bottom is 27 dp above the screen bottom.

### Chat
- Header: back circle 42.7 dp, then an 8 dp gap, then a name pill (43 dp tall, 23 dp avatar, 16 sp name). The ⋯ circle sits on the right. All float over the scrolling content.
- Bot message card (1:1): x from 15.6 dp; max width about 337 dp, leaving about 58 dp free on the right. **Corner radius about 18 dp (48 px), no tail.** Inner horizontal padding 14 dp. Fill `#212020`.
- Group chat: sender name (11–12 sp, secondary colour) above the card. An 18 dp sender avatar sits at the card's bottom-left in a 42 dp gutter, and the card starts at 42 dp.
- Composer: pill 44.6 dp tall, full-round, fill `#2C2C2E` with a hairline. It contains a 29 dp round "+" (fill `#3D3D42`), the placeholder "Ask Thor", a 29 dp mic and a **29 dp white circular voice button**.
- Usage banner card above the composer: radius about 18 dp. Pill buttons are 33 dp tall ("Buy credits" `#3D3D42`, "Upgrade" white).

### Popup menus (⋯ menu, composer + menu, folder long-press menu)
- Width **240 dp** (630 px). Item pitch 41 dp. 17 dp leading icon (secondary grey), label 16 sp.
- Fill `#2C2C2E`, 1 px `#4B4B4D` border, corner radius about 15–16 dp.
- ⋯ menu: right edge aligned to the ⋯ button's right edge, top 10 dp below the button.
- + menu: left edge aligned to the composer, bottom 10 dp above the composer.

### Bottom sheet (Account)
- The sheet top sits at y=103 px (**39 dp**), just under the status bar, so the sheet is nearly full height. Top corner radius about **15 dp** (40 px).
- ✕ circle at top-left, 42.7 dp, in the same position as the home K button (about 10 dp lower).
- Grouped cards: inset 16 dp, **radius 15 dp**, 1 px `#373739` border. Rows are 48 dp single-line and 67 dp two-line. Chevrons are right-aligned 21 dp in. Section label ("Bot") is 13 sp secondary.

### Search
- Filter chips row at the top (y=113–200 px): chip height 33 dp, full-round, 8 dp gap, horizontally scrollable.
- Bottom: the grid button is removed. The Search pill takes the left part, and a circular ✕ button appears to its right.

---

## 3. Typography (estimated from cap height ÷ 0.70)
| Element | Size | Weight |
|---|---|---|
| Chat body text | **16 sp**, line height about 21 sp (56 px pitch) | Regular. Bold run-ins are SemiBold. Code is monospace. |
| Thread title (home) | 16 sp | Medium |
| Thread preview, folder header | 14 sp | Regular, `#95959B` |
| Thread time | 13–14 sp | Regular, tertiary |
| Header name pill | 16 sp | Medium |
| Home hero name ("Thor") | 13 sp | Regular |
| Group sender label | 11–12 sp | Regular, secondary |
| Menu items | 16 sp | Regular |
| Sheet row title / subtitle | 16 sp / 14 sp | Regular / Regular secondary |

The font is a neutral geometric/grotesk sans, similar to the system font with tabular-looking digits.

---

## 4. Motion

### 4.1 Folder expand / collapse (tap folder header)
- **Expand:**
  - The count "7" disappears and the chevron jumps next to the title in the first frame, with no animation.
  - The chevron rotates **› → ⌄ (0° → 90°) over about 120 ms**, ease-out.
  - The rows reveal through a **height (expandVertically) animation over about 220–250 ms**, decelerating. The content below (the next folder header) is pushed 540 dp down. Progress at frame times: 6%, 41%, 54%, 66%, 75%, 83%, 89%, 97% (~150 ms), 99%. The data fits an ease-out cubic of about 230 ms.
  - Rows fade in fast: the first frame is dim, and they are fully opaque by the second frame (about 30 ms).
  - Rows are not staggered. All rows are revealed by the growing clip, top-down.
- **Collapse:**
  - The rows fade out almost immediately (within one frame, under 30 ms).
  - Then the height collapses **accelerating (ease-in) over about 120–140 ms**. Progress: 19%, 29%, 42%, 58%, 77%, 100%, ending abruptly with no settle.
  - The count reappears.
- No press highlight on the header.

### 4.2 Back from chat to home
- **Back button, system BACK key and edge swipe all give the same transition:**
  - The chat **slides right about 6–8% of the width (20–25 dp) while fading out**, gone in about 50 ms.
  - Home **fades in in place, with no slide and no scale**. It is fully settled about 70–90 ms after the transition starts.
  - Easing reads as linear/fast. Overall it is a ~80 ms crossfade, the mirror of opening a chat.
- **Edge swipe:** OxygenOS draws its own back-arrow indicator at the edge during the drag. **There is no predictive-back preview**: the chat does not move or scale during the gesture. The transition above runs only after release.

### 4.3 Bottom sheet (Account "K")
- **Open:**
  - The sheet slides up from below the screen to its top at 39 dp, travelling about 2300 px (about 875 dp).
  - **Ease-out cubic, about 440 ms**, with no overshoot and no bounce. Progress per frame: 39%, 47%, 55%, 61%, 67%, 73%, 77%, 82%, 85%, 88%, 91%, 93%, 95%, 96%, 98%, 99% (~285 ms), with settle complete at about 350 ms.
  - The scrim fades from 0 to about 30% black alongside the sheet.
  - **The home behind does not scale, move or blur.** It only dims.
- **Close (✕ or BACK, identical):**
  - Slides down, starting at full speed and decelerating: **ease-out cubic about 300–360 ms**. It leaves the screen at about 270 ms. Progress: 11%, 22%, 32%, 41%, 50%, 57%, 64%, 70%, 76%, 81%, 85%, 89%, 92%, 95%, 97%.
  - The scrim fades out over the same time.
- **Shape:** corner radius about 15 dp, full width with no side inset, sheet colour `#1E1E1E`.

### 4.4 Anchored popup menus (chat ⋯, composer +, folder long-press)
- **No animation.** The menu appears in a single frame and disappears in a single frame. That rules out scale-from-anchor and fade, in both directions.
- There is no scrim. Dismiss with BACK or an outside tap.
- The **K long-press account switcher** ("Karthick ✓ / Add Account") is the exception. It appears about 300 ms after touch-down (the long-press timeout) and **fades in over about 80–100 ms** with a light dim of the background.

### 4.5 Search
- **Tap the Search pill:**
  1. The bottom bar swaps instantly: the grid button goes away, the pill shrinks to the left part, and a ✕ circle appears on the right.
  2. **0–120 ms:** the home content crossfades out while the search results list crossfades in. At the same time, the top K/+ buttons crossfade into the filter-chip row (chip alpha 0 → 1 over about 120 ms, linear-ish). Nothing slides.
  3. **About 170 ms after the tap,** the IME starts opening. **The pill rides on top of the keyboard frame by frame** (inset-animation driven), moving 253 dp up over **about 260 ms, ease-out** (system IME curve). Progress: 0, 27%, 49%, 60%, 68%, 73%, 78%, 81%, 87%, 98%, 100%.
- **First BACK:** hides the keyboard. The pill follows it down in **about 130–150 ms** (faster, accelerating).
- **Second BACK:** the bottom bar swaps back instantly. The chips crossfade back to K/+, and results crossfade to home in **about 150 ms**. The controls change first, then the list fades, with no slide.

### 4.6 Chat scrolling and the floating header
- Content scrolls under the status bar and under the floating back / name-pill / ⋯ controls. The controls are opaque (`#2C2C2E` + hairline), so text is hidden only directly behind them. There is **no blur**.
- **Top edge fade:** a vertical gradient of the background colour sits over the content. It is about 90% opaque at the screen top and falls roughly linearly to 0 at **about 126 dp (330 px)** from the top. Measured text visibility: 3% at 8 dp, 28% at 38 dp, 61% at 76 dp, 93% at 114 dp, 100% at 126 dp.
- **Bottom edge:** a short fade (about 30 dp) appears just above the floating banner/composer stack. Text that passes between the banner and the composer shows at about 35% visibility.
- Fling and overscroll follow standard Android behaviour. No custom stretch was observed beyond the system one.

### 4.7 Live status and mascot
- **"Sending a message" status row and mascot face: not observed.** No chat was mid-reply during the session. I saw neither in the Thor chat nor the IndieTide SEO group chat, and I did not send messages, per the rules.
- **Home mascot: no idle animation observed.** An 8 s idle recording of home produced zero changed frames, and the mascot was also static after returning from a chat. The only visible change was dimming under scrims. If it animates, it is either triggered by events I did not observe or loops on a period longer than 8 s.

---

## 5. Press feedback
No ripples or highlight fills appear anywhere. Feedback is **scale-down plus content dim**, released with a soft spring.

| Element | Press-in | Held | Release |
|---|---|---|---|
| Search pill | width ×0.97 (843 → 819 px), icon/text dim ~10–18%, over **60–80 ms** | stays | back over about 100 ms with a **tiny overshoot (~+0.4%)**, settled by about 230 ms. Reads as a spring with damping ~0.7–0.8. |
| Circular buttons (+, K) | scale ~0.97–0.98, glyph dims ~5% (+) to ~17% (K), over about 65 ms | stays | same spring return. Long-pressing K opens the account switcher. |
| Thread row | **whole row content** (avatar + text) scales ~0.96 and drops to alpha ~0.77, over about **70 ms**. Background unchanged. | stays | back over about 100 ms, barely perceptible overshoot |
| Folder header row | **no visible feedback** at 250 ms hold. Long-press (~400–500 ms) opens a Rename / Move Up / Move Down / Delete popup. | | |
| Popup menu items | not measured (I avoided pressing items) | | |

Moving the finger off a pressed element cancels the click and animates back with the same release spring.

---

## 6. Implementation notes for Jetpack Compose
1. **Screen transitions (home ↔ chat):**
   - Enter: `fadeIn(tween(100))`. Exit: `fadeOut(tween(60)) + slideOutHorizontally(tween(100)) { -it / 25 }`.
   - Pop is the mirror: the chat exits with `fadeOut(tween(60)) + slideOutHorizontally { it / 14 }` and home uses `fadeIn(tween(80))`.
   - Keep it very short (≤100 ms) and use no predictive-back preview, which matches the app.
2. **Folder expand:**
   - `AnimatedVisibility(enter = expandVertically(tween(230, easing = EaseOutCubic), expandFrom = Alignment.Top) + fadeIn(tween(40)))`.
   - Collapse: `exit = fadeOut(tween(30)) + shrinkVertically(tween(130, easing = EaseInCubic))`.
   - Chevron: `animateFloatAsState(if (expanded) 90f else 0f, tween(120, easing = EaseOutCubic))`.
   - Hide the count with no animation.
3. **Bottom sheet:**
   - Custom sheet or `ModalBottomSheet` with its motion overridden: offset tween **440 ms `EaseOutCubic`** to open and **320 ms `EaseOutCubic`** to close.
   - Scrim `Color.Black.copy(alpha = 0.30f)`, animated with the same spec.
   - Top corners 15 dp, colour `#1E1E1E`, top inset 39 dp. Do not scale or blur the content behind.
4. **Popup menus:** `Popup`/`DropdownMenu` with **no enter/exit transition** (`EnterTransition.None`). Width 240 dp, radius 16 dp, 1 dp border `#4B4B4D`, anchored 10 dp from the anchor. No scrim.
5. **Press feedback:** set `indication = null`. Drive `graphicsLayer { scaleX = s; scaleY = s; alpha = a }` from `interactionSource.collectIsPressedAsState()`:
   - Press-in: `tween(70, easing = FastOutSlowInEasing)`. Release: `spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow)`.
   - Pills and circles: s = 0.97, a = 0.88.
   - List rows: s = 0.96, a = 0.77, applied to the row content, with no background change.
6. **Search:**
   - Crossfade the content and top bar with `Crossfade(animationSpec = tween(120))`.
   - Swap the bottom bar layout instantly.
   - Put the search pill in a bottom-aligned container with `Modifier.imePadding()` (or `windowInsetsPadding(WindowInsets.ime)`) so it tracks the IME frame by frame. Do not animate it separately.
7. **Chat chrome:**
   - Draw edge-to-edge content under the status bar.
   - Overlay a `Brush.verticalGradient(0f to bg.copy(alpha = .9f), 1f to Color.Transparent)` 126 dp tall at the top and a ~30 dp fade above the composer stack.
   - Floating controls are opaque `#2C2C2E` circles and pills with a 1 dp `#4B4B4D` border, with no blur and no elevation shadow.
8. **Shapes:** circles and pills `CircleShape`. Message cards 18 dp. Sheet cards 15 dp. Sheet top 15 dp. Menus about 16 dp. Bubbles have no tails.
9. **Light theme:** not captured. Repeat the colour pass with the system in light mode if needed.

---

### Source artefacts (in the same folder)
`f-collapse.mp4` (expand), `f-coll2.mp4` (collapse), `backbtn.mp4`, `backkey.mp4`, `edge.mp4`, `sheet.mp4` (open + BACK close), `sheetx.mp4` (✕ close), `menu.mp4`, `plus.mp4`, `scroll.mp4`, `idle.mp4`, `search.mp4`, `searchx.mp4`, `pressK.mp4`, `press2.mp4` (search pill), `press3.mp4` (thread row). Screenshots: `L-home.png`, `chat.png`, `grp.png`, `menu-open.png`, `plus-open.png`, `sheet-open.png`, `rest2.png` (folder long-press menu), `plus-pressed.png`. Frame folders are under `fr/`.
