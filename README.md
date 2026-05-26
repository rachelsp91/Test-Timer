# 😊 Smile Timer

A visual countdown/count-up timer for children — including kids with ADHD and autism — built with Kotlin and Jetpack Compose.

---

## Features

| Feature | Details |
|---|---|
| **Timer modes** | Countdown or Count-Up, 1–99 minutes |
| **Visual progress** | 36 LED-style segments arranged in a clock circle |
| **Colour stages** | Green → Yellow → Orange → Red as time runs out (smooth transitions) |
| **Smiley face** | Animated ⭐ star eyes + expression that shifts happy → sad |
| **Controls** | Large +/− buttons to set minutes; Start / Pause / Resume / Reset |
| **Alarm** | Rings continuously when timer ends — stops when you tap Reset |
| **Alarm tone** | 🎵 Choose any system ringtone, alarm, or notification sound — saved between sessions |
| **Volume** | High / Low / Mute |
| **Screen stays on** | Display never sleeps while the timer is running |
| **Dark UI** | High-contrast, readable from across a room |

---

## How to use

1. Tap **+** / **−** to set the number of minutes (1–99)
2. Choose **⏬ Count Down** or **⏫ Count Up** mode
3. Tap **▶ START**
4. When the timer finishes the alarm rings — tap **↺ RESET** to stop it
5. Tap **🎵 Alarm Tone** at any time to choose a different alarm sound

---

## Screen layout

```
╭─────────────────────────────╮
│        😊 Smile Timer        │
│  [⏬ Count Down] [⏫ Count Up] │
│                              │
│     🟢🟢🟢🟡🟡🟡            │
│   🟢        🟡  🟠           │
│  🟢  ⭐   ⭐  🟠 🟠          │
│  🟢   05:00    🔴 🔴         │
│   🟢         🔴              │
│     🔴🔴🔴🔴🔴               │
│                              │
│     −    05 min    +         │
│    [▶  START]                │
│  [🔊 High] [🔉 Low] [🔇 Mute] │
│  [🎵 Alarm Tone  Argon  ›]   │
╰─────────────────────────────╯
```

---

## Building

### Requirements

- Android Studio Hedgehog (2023.1) or newer
- Android SDK 26+
- Kotlin 2.0+

### Run in Android Studio

1. **File → New → Project from Version Control**
2. Paste `https://github.com/rachelsp91/test-timer.git`
3. Switch to branch `claude/smile-timer-android-app-FdOi8`
4. Let Gradle sync, then press **▶ Run**

### Build a debug APK

**Build → Build Bundle(s) / APK(s) → Build APK(s)**

The APK is saved to:
```
app/build/outputs/apk/debug/app-debug.apk
```

Install on any Android phone by enabling **Settings → Apps → Install unknown apps**, then tapping the APK file.

---

## Architecture

```
app/src/main/java/com/smiletimer/
├── MainActivity.kt               # Activity shell (enableEdgeToEdge)
├── SmileTimerViewModel.kt        # Timer state, alarm sound, tone persistence
└── ui/
    ├── SmileTimerApp.kt          # Root composable, ringtone picker launcher,
    │                             #   screen-wake (keepScreenOn)
    ├── SmileFace.kt              # Canvas-drawn animated smiley face
    │                             #   (star eyes, bezier mouth, brows, blush)
    ├── SegmentedTimerArc.kt      # 36-segment LED-style progress arc
    ├── TimerControls.kt          # Mode tabs, ±buttons, action buttons, volume tabs
    └── theme/
        ├── Color.kt
        ├── Theme.kt
        └── Type.kt
```

### Key implementation notes

- **Timer loop** — coroutine-based (`viewModelScope.launch` + `delay(1000)`), supports pause/resume cleanly
- **Alarm** — `MediaPlayer` with user-chosen URI; loops until `resetTimer()` calls `stopAlarm()`; falls back to `ToneGenerator` if the URI fails
- **Tone persistence** — chosen URI stored in `SharedPreferences`, restored in `ViewModel.init`
- **Screen wake** — `View.keepScreenOn` toggled via `DisposableEffect`; no `WAKE_LOCK` permission needed
- **Ringtone picker** — Android's built-in `RingtoneManager.ACTION_RINGTONE_PICKER`; result handled via `rememberLauncherForActivityResult`

---

## Design inspiration

Inspired by LED clock visual timers widely used in ADHD/autism therapy settings — chunky coloured segments, a friendly face, zero ticking sounds.

---

Minimum SDK: **26** (Android 8.0 Oreo)  
Target SDK: **35** (Android 15)
