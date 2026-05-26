package com.smiletimer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smiletimer.TimerMode
import com.smiletimer.TimerState
import com.smiletimer.VolumeLevel
import com.smiletimer.ui.theme.BackgroundDark
import com.smiletimer.ui.theme.ButtonModeActive
import com.smiletimer.ui.theme.ButtonPause
import com.smiletimer.ui.theme.ButtonReset
import com.smiletimer.ui.theme.ButtonStart
import com.smiletimer.ui.theme.SurfaceDark
import com.smiletimer.ui.theme.SurfaceVariant
import com.smiletimer.ui.theme.TextPrimary
import com.smiletimer.ui.theme.TextSecondary

// ── Mode Selector ─────────────────────────────────────────────────────────────

@Composable
fun ModeSelector(
    currentMode: TimerMode,
    isIdle: Boolean,
    onModeChange: (TimerMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(SurfaceVariant)
            .fillMaxWidth()
            .height(52.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ModeTab(
            label     = "⏬  Count Down",
            isActive  = currentMode == TimerMode.COUNTDOWN,
            isEnabled = isIdle,
            onClick   = { onModeChange(TimerMode.COUNTDOWN) },
            modifier  = Modifier.weight(1f)
        )
        ModeTab(
            label     = "⏫  Count Up",
            isActive  = currentMode == TimerMode.COUNTUP,
            isEnabled = isIdle,
            onClick   = { onModeChange(TimerMode.COUNTUP) },
            modifier  = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ModeTab(
    label: String,
    isActive: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalButton(
        onClick  = { if (isEnabled) onClick() },
        enabled  = isEnabled,
        shape    = RoundedCornerShape(50),
        colors   = ButtonDefaults.filledTonalButtonColors(
            containerColor         = if (isActive) ButtonModeActive else Color.Transparent,
            contentColor           = TextPrimary,
            disabledContainerColor = if (isActive) ButtonModeActive.copy(alpha = 0.5f) else Color.Transparent,
            disabledContentColor   = TextPrimary.copy(alpha = 0.5f)
        ),
        modifier = modifier.height(48.dp)
    ) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

// ── Time Setter ───────────────────────────────────────────────────────────────

@Composable
fun TimeSetter(
    minutes: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier              = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        // Minus button
        RoundIconButton(
            label   = "−",
            onClick = onDecrement,
            enabled = minutes > 1
        )

        Spacer(Modifier.width(28.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text       = "%02d".format(minutes),
                color      = TextPrimary,
                fontSize   = 52.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 56.sp
            )
            Text(
                text     = "minutes",
                color    = TextSecondary,
                fontSize = 14.sp
            )
        }

        Spacer(Modifier.width(28.dp))

        // Plus button
        RoundIconButton(
            label   = "+",
            onClick = onIncrement,
            enabled = minutes < 99
        )
    }
}

@Composable
private fun RoundIconButton(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    FilledTonalButton(
        onClick          = onClick,
        enabled          = enabled,
        shape            = CircleShape,
        contentPadding   = PaddingValues(0.dp),
        colors           = ButtonDefaults.filledTonalButtonColors(
            containerColor         = SurfaceVariant,
            contentColor           = TextPrimary,
            disabledContainerColor = SurfaceDark,
            disabledContentColor   = TextSecondary
        ),
        modifier         = Modifier.size(68.dp)
    ) {
        Text(
            text       = label,
            fontSize   = 32.sp,
            fontWeight = FontWeight.Bold,
            textAlign  = TextAlign.Center
        )
    }
}

// ── Main control buttons ──────────────────────────────────────────────────────

@Composable
fun ControlButtons(
    timerState: TimerState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier              = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        when (timerState) {
            TimerState.IDLE -> {
                MainButton(
                    label    = "▶  START",
                    color    = ButtonStart,
                    onClick  = onStart,
                    modifier = Modifier.weight(1f)
                )
            }
            TimerState.RUNNING -> {
                MainButton(
                    label    = "⏸  PAUSE",
                    color    = ButtonPause,
                    onClick  = onPause,
                    modifier = Modifier.weight(1f)
                )
                MainButton(
                    label    = "↺  RESET",
                    color    = SurfaceVariant,
                    onClick  = onReset,
                    modifier = Modifier.weight(1f)
                )
            }
            TimerState.PAUSED -> {
                MainButton(
                    label    = "▶  RESUME",
                    color    = ButtonStart,
                    onClick  = onResume,
                    modifier = Modifier.weight(1f)
                )
                MainButton(
                    label    = "↺  RESET",
                    color    = SurfaceVariant,
                    onClick  = onReset,
                    modifier = Modifier.weight(1f)
                )
            }
            TimerState.COMPLETE -> {
                MainButton(
                    label    = "↺  RESET",
                    color    = ButtonReset,
                    onClick  = onReset,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun MainButton(
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalButton(
        onClick  = onClick,
        shape    = RoundedCornerShape(50),
        colors   = ButtonDefaults.filledTonalButtonColors(
            containerColor = color,
            contentColor   = BackgroundDark
        ),
        modifier = modifier.height(58.dp)
    ) {
        Text(
            text          = label,
            fontSize      = 17.sp,
            fontWeight    = FontWeight.ExtraBold,
            letterSpacing = 0.8.sp
        )
    }
}

// ── Volume Selector ───────────────────────────────────────────────────────────

@Composable
fun VolumeSelector(
    volumeLevel: VolumeLevel,
    onVolumeChange: (VolumeLevel) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(SurfaceVariant)
            .fillMaxWidth()
            .height(52.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        VolumeTab("🔊  High", volumeLevel == VolumeLevel.HIGH,
            { onVolumeChange(VolumeLevel.HIGH) },   Modifier.weight(1f))
        VolumeTab("🔉  Low",  volumeLevel == VolumeLevel.LOW,
            { onVolumeChange(VolumeLevel.LOW) },    Modifier.weight(1f))
        VolumeTab("🔇  Mute", volumeLevel == VolumeLevel.MUTE,
            { onVolumeChange(VolumeLevel.MUTE) },   Modifier.weight(1f))
    }
}

@Composable
private fun VolumeTab(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalButton(
        onClick  = onClick,
        shape    = RoundedCornerShape(50),
        colors   = ButtonDefaults.filledTonalButtonColors(
            containerColor = if (isActive) ButtonModeActive else Color.Transparent,
            contentColor   = TextPrimary
        ),
        modifier = modifier.height(48.dp)
    ) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}
