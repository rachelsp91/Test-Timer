package com.smiletimer.ui

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smiletimer.SmileTimerViewModel
import com.smiletimer.TimerState
import com.smiletimer.TimerUiState
import com.smiletimer.TimerMode
import com.smiletimer.VolumeLevel
import com.smiletimer.ui.theme.BackgroundDark
import com.smiletimer.ui.theme.SmileTimerTheme
import com.smiletimer.ui.theme.SurfaceVariant
import com.smiletimer.ui.theme.TextPrimary
import com.smiletimer.ui.theme.TextSecondary

// ── Screen-wake helper ────────────────────────────────────────────────────────

/**
 * Keeps the screen awake while [enabled] is true.
 * Uses View.keepScreenOn — no extra permissions required.
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

    // Ringtone picker — launched when the user taps the Alarm Tone row
    val ringtoneLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.data?.getParcelableExtra(
                    RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java
                )
            } else {
                @Suppress("DEPRECATION")
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            }
            viewModel.setAlarmToneUri(uri)
        }
    }

    SmileTimerTheme {
        SmileTimerScreen(
            uiState        = uiState,
            onStart        = viewModel::startTimer,
            onPause        = viewModel::pauseTimer,
            onResume       = viewModel::resumeTimer,
            onReset        = viewModel::resetTimer,
            onIncrement    = viewModel::incrementMinutes,
            onDecrement    = viewModel::decrementMinutes,
            onModeChange   = viewModel::setTimerMode,
            onVolumeChange = viewModel::setVolumeLevel,
            onPickTone     = {
                val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                    putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Choose Alarm Tone")
                    putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                    putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                    putExtra(
                        RingtoneManager.EXTRA_RINGTONE_TYPE,
                        RingtoneManager.TYPE_ALL        // shows alarms, ringtones & notifications
                    )
                    putExtra(
                        RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    )
                    // Pre-select the currently active tone in the list
                    uiState.alarmToneUri?.let {
                        putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, it)
                    }
                }
                ringtoneLauncher.launch(intent)
            }
        )
    }
}

// ── Main screen ───────────────────────────────────────────────────────────────

@Composable
fun SmileTimerScreen(
    uiState: TimerUiState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onReset: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onModeChange: (TimerMode) -> Unit,
    onVolumeChange: (VolumeLevel) -> Unit,
    onPickTone: () -> Unit
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
        // ── Visual progress: always 0.0 (start) → 1.0 (goal reached) ────────────
        // Countdown: fractionRemaining goes 1→0, so use it directly.
        // Count-up:  fractionRemaining also goes 1→0 internally, so invert it
        //            so that the arc fills and the face brightens as time elapses.
        val isCountUp      = uiState.timerMode == TimerMode.COUNTUP
        val visualProgress = if (isCountUp) 1f - uiState.fractionRemaining
                             else           uiState.fractionRemaining

        Box(
            modifier         = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            SegmentedTimerArc(
                visualProgress = visualProgress,
                isFlashing     = uiState.isFlashing,
                isCountUp      = isCountUp,
                modifier       = Modifier.fillMaxSize()
            )

            Column(
                modifier            = Modifier.fillMaxSize(0.58f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                SmileFace(
                    expressionValue = 1f,   // always happy 😊
                    modifier        = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
                Spacer(Modifier.height(4.dp))
                TimeDisplay(minutes = uiState.displayMinutes, seconds = uiState.displaySeconds)
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

        // ── Alarm tone picker ────────────────────────────────────────────────
        AlarmToneRow(
            currentUri = uiState.alarmToneUri,
            onClick    = onPickTone
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
        text      = "%02d:%02d".format(minutes, seconds),
        color     = TextPrimary,
        style     = MaterialTheme.typography.displayLarge,
        textAlign = TextAlign.Center,
        modifier  = modifier
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
            text       = label,
            color      = TextSecondary,
            fontSize   = 14.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign  = TextAlign.Center
        )
    }
}

/**
 * Tappable row that opens the system ringtone picker.
 * Displays the name of the currently selected tone.
 */
@Composable
private fun AlarmToneRow(
    currentUri: Uri?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Resolve the human-readable name of the current tone
    val toneName = remember(currentUri) {
        when {
            currentUri == null ->
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?.let { RingtoneManager.getRingtone(context, it)?.getTitle(context) }
                    ?: "Default Alarm"
            else ->
                try { RingtoneManager.getRingtone(context, currentUri)?.getTitle(context) ?: "Custom Tone" }
                catch (_: Exception) { "Custom Tone" }
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("🎵", fontSize = 22.sp)
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = "Alarm Tone",
                color      = TextSecondary,
                fontSize   = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text       = toneName,
                color      = TextPrimary,
                fontSize   = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Text(
            text     = "›",
            color    = TextSecondary,
            fontSize = 22.sp
        )
    }
}
