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
| **Alarm** | System alarm at timer end — High / Low / Mute volume |
| **Display** | Dark, high-contrast UI readable from across a room |

## Screenshots (design)

```
╭─────────────────────────────╮
│      😊 Smile Timer         │
│  [⏬ Count Down] [⏫ Count Up] │
│                             │
│    🟢🟢🟢🟡🟡🟡             │
│  🟢        🟡  🟠           │
│ 🟢  ⭐   ⭐  🟠 🟠          │
│ 🟢   05:00    🔴 🔴         │
│  🟢         🔴              │
│    🔴🔴🔴🔴🔴               │
│                             │
│    −    05 min    +         │
│   [▶  START]                │
│  [🔊 High] [🔉 Low] [🔇 Mute]│
╰─────────────────────────────╯
```

## Building

### Requirements

- Android Studio Hedgehog (2023.1) or newer
- Android SDK 26+
- Kotlin 2.0+

### Steps

```bash
git clone <repo>
cd Test-Timer
./gradlew assembleDebug
```

Or open in Android Studio and press **Run ▶**.

## Architecture

```
app/src/main/java/com/smiletimer/
├── MainActivity.kt               # Activity shell
├── SmileTimerViewModel.kt        # Timer state + sound logic
└── ui/
    ├── SmileTimerApp.kt          # Root composable / screen layout
    ├── SmileFace.kt              # Canvas-drawn animated smiley face
    ├── SegmentedTimerArc.kt      # 36-segment LED-style progress arc
    ├── TimerControls.kt          # Buttons, mode tabs, volume tabs
    └── theme/
        ├── Color.kt
        ├── Theme.kt
        └── Type.kt
```

## Design inspiration

Inspired by the style of LED clock visual timers widely used in ADHD/autism therapy settings — chunky coloured segments, a friendly face, zero ticking sounds.

---

Minimum SDK: **26** (Android 8.0 Oreo)  
Target SDK: **35** (Android 15)
