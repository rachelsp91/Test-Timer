package com.smiletimer.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smiletimer.SmileTimerViewModel
import com.smiletimer.TimerState
import com.smiletimer.ui.theme.BackgroundDark
import com.smiletimer.ui.theme.SmileTimerTheme
import com.smiletimer.ui.theme.TextPrimary
import com.smiletimer.ui.theme.TextSecondary

// ── Screen-wake helper ────────────────────────────────────────────────────────

/**
 * Keeps the screen awake while [enabled] is true.
 * Uses View.keepScreenOn — no extra permissions required.
 * Automatically releases when the composable leaves the composition.
 */
@Composable
fun KeepScreenOn(enabled: Boolean) {
    val view = LocalView.current
    DisposableEffect(enabled) {
        view.keepScreenOn = enabled
        onDispose { view.keepScreenOn = false }
    }
}

// ── App entry point ───────────────────────────────────────────────────────────

@Composable
fun SmileTimerApp() {
    val viewModel: SmileTimerViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SmileTimerTheme {
        SmileTimerScreen(
            uiState       = uiState,
            onStart       = viewModel::startTimer,
            onPause       = viewModel::pauseTimer,
            onResume      = viewModel::resumeTimer,
            onReset       = viewModel::resetTimer,
            onIncrement   = viewModel::incrementMinutes,
            onDecrement   = viewModel::decrementMinutes,
            onModeChange  = viewModel::setTimerMode,
            onVolumeChange = viewModel::setVolumeLevel
        )
    }
}

// ── Main screen ───────────────────────────────────────────────────────────────

@Composable
fun SmileTimerScreen(
    uiState: com.smiletimer.TimerUiState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onReset: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onModeChange: (com.smiletimer.TimerMode) -> Unit,
    onVolumeChange: (com.smiletimer.VolumeLevel) -> Unit
) {
    // Keep screen on while the timer is actively running
    KeepScreenOn(enabled = uiState.timerState == TimerState.RUNNING)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        // ── App title ────────────────────────────────────────────────────────
        Text(
            text       = "😊 Smile Timer",
            color      = TextPrimary,
            fontSize   = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign  = TextAlign.Center
        )

        // ── Mode toggle ──────────────────────────────────────────────────────
        ModeSelector(
            currentMode  = uiState.timerMode,
            isIdle       = uiState.timerState == TimerState.IDLE,
            onModeChange = onModeChange
        )

        // ── Circular timer display ────────────────────────────────────────────
        Box(
            modifier        = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            // Segmented arc
            SegmentedTimerArc(
                fractionRemaining = uiState.fractionRemaining,
                isFlashing        = uiState.isFlashing,
                modifier          = Modifier.fillMaxSize()
            )

            // Smiley face + time overlay (60 % of the circle diameter)
            Column(
                modifier              = Modifier.fillMaxSize(0.58f),
                horizontalAlignment   = Alignment.CenterHorizontally,
                verticalArrangement   = Arrangement.Center
            ) {
                // Smiley face (top 60% of inner area)
                SmileFace(
                    expressionValue = uiState.fractionRemaining,
                    modifier        = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                Spacer(Modifier.height(4.dp))

                // Digital time display
                TimeDisplay(
                    minutes   = uiState.displayMinutes,
                    seconds   = uiState.displaySeconds
                )

                // Status hint below time
                StatusHint(uiState.timerState)

                Spacer(Modifier.height(8.dp))
            }
        }

        // ── Time setter (only while idle) ─────────────────────────────────────
        AnimatedVisibility(
            visible = uiState.timerState == TimerState.IDLE,
            enter   = fadeIn() + slideInVertically(),
            exit    = fadeOut() + slideOutVertically()
        ) {
            TimeSetter(
                minutes     = uiState.totalMinutes,
                onIncrement = onIncrement,
                onDecrement = onDecrement
            )
        }

        // ── Control buttons ──────────────────────────────────────────────────
        ControlButtons(
            timerState = uiState.timerState,
            onStart    = onStart,
            onPause    = onPause,
            onResume   = onResume,
            onReset    = onReset
        )

        // ── Volume selector ──────────────────────────────────────────────────
        VolumeSelector(
            volumeLevel    = uiState.volumeLevel,
            onVolumeChange = onVolumeChange
        )

        Spacer(Modifier.height(8.dp))
    }
}

// ── Sub-composables ────────────────────────────────────────────────────────────

/** Large MM:SS digital display. */
@Composable
fun TimeDisplay(
    minutes: Int,
    seconds: Int,
    modifier: Modifier = Modifier
) {
    Text(
        text       = "%02d:%02d".format(minutes, seconds),
        color      = TextPrimary,
        style      = MaterialTheme.typography.displayLarge,
        textAlign  = TextAlign.Center,
        modifier   = modifier
    )
}

/** Small status hint below the time (e.g. PAUSED). */
@Composable
private fun StatusHint(timerState: TimerState) {
    val label = when (timerState) {
        TimerState.PAUSED   -> "⏸  PAUSED"
        TimerState.COMPLETE -> "🎉  DONE!"
        else                -> ""
    }
    if (label.isNotEmpty()) {
        Text(
            text      = label,
            color     = TextSecondary,
            fontSize  = 14.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}
