# 😊 Smile Timer

A visual countdown/count-up timer for children — including kids with ADHD and autism — built with Kotlin and Jetpack Compose.

---

## Features

| Feature | Details |
|---|---|
| **Timer modes** | Countdown or Count-Up, 1–99 minutes |
| **Visual progress** | 36 LED-style segments arranged in a clock circle |
| **Countdown colours** | Green → Yellow → Orange → Red as time runs out |
| **Count-up colours** | Red → Orange → Yellow → Green as goal approaches |
| **Smiley face** | Cute static happy face with ⭐ star eyes throughout |
| **Arc direction** | Countdown depletes clockwise; Count-up fills in reverse |
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

## Mode behaviour at a glance

| | **⏬ Countdown** | **⏫ Count-up** |
|---|---|---|
| Arc | Starts full, depletes toward 12 o'clock | Starts empty, fills away from 12 o'clock |
| Colour | Green → Red | Red → Green |
| Good for | Time limits ("5 more minutes") | Reward goals ("work for 10 minutes") |

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

## Architecture

```
app/src/main/java/com/smiletimer/
├── MainActivity.kt               # Activity shell (enableEdgeToEdge)
├── SmileTimerViewModel.kt        # Timer state, alarm sound, tone persistence
└── ui/
    ├── SmileTimerApp.kt          # Root composable, ringtone picker launcher,
    │                             #   screen-wake (keepScreenOn), visualProgress
    ├── SmileFace.kt              # Canvas-drawn happy smiley face
    │                             #   (large star eyes, blush, gentle smile)
    ├── SegmentedTimerArc.kt      # 36-segment LED-style progress arc
    ├── TimerControls.kt          # Mode tabs, ±buttons, action buttons, volume tabs
    └── theme/
        ├── Color.kt
        ├── Theme.kt
        └── Type.kt
```

### Key implementation notes

- **Timer loop** — coroutine-based (`viewModelScope.launch` + `delay(1000)`), supports pause/resume cleanly
- **visualProgress** — computed in the UI layer: `fractionRemaining` for countdown, `1 − fractionRemaining` for count-up; always 0→1 as timer advances; drives the arc colour and fill
- **Arc direction** — countdown lights segments `i < litCount` (clockwise from 12); count-up lights `i >= TOTAL − litCount` (reverse fill), creating a mirror animation
- **Alarm** — `MediaPlayer` with user-chosen URI; loops (`isLooping = true`) until `resetTimer()` calls `stopAlarm()`; falls back to `ToneGenerator` if URI fails
- **Tone persistence** — chosen URI stored in `SharedPreferences`, restored in `ViewModel.init`
- **Screen wake** — `View.keepScreenOn` toggled via `DisposableEffect`; no `WAKE_LOCK` permission needed
- **Ringtone picker** — Android's built-in `RingtoneManager.ACTION_RINGTONE_PICKER`; result handled via `rememberLauncherForActivityResult`

---

## Design inspiration

Inspired by LED clock visual timers widely used in ADHD/autism therapy settings — chunky coloured segments, a friendly face, zero ticking sounds.

---

Minimum SDK: **26** (Android 8.0 Oreo)  
Target SDK: **35** (Android 15)
