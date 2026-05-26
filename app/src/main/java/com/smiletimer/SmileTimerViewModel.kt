package com.smiletimer

import android.app.Application
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.media.ToneGenerator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class TimerState { IDLE, RUNNING, PAUSED, COMPLETE }
enum class TimerMode { COUNTDOWN, COUNTUP }
enum class VolumeLevel { HIGH, LOW, MUTE }

data class TimerUiState(
    val totalMinutes: Int = 5,
    val displayMinutes: Int = 5,
    val displaySeconds: Int = 0,
    val fractionRemaining: Float = 1f,
    val timerState: TimerState = TimerState.IDLE,
    val timerMode: TimerMode = TimerMode.COUNTDOWN,
    val volumeLevel: VolumeLevel = VolumeLevel.HIGH,
    val isFlashing: Boolean = false
)

class SmileTimerViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var flashJob: Job? = null
    private var toneGenerator: ToneGenerator? = null
    private var mediaPlayer: MediaPlayer? = null

    /** Total time in seconds for the current session */
    private var totalSeconds: Int = 5 * 60

    /**
     * Tracks progress seconds:
     * - COUNTDOWN: starts at totalSeconds, decrements to 0
     * - COUNTUP:   starts at 0, increments to totalSeconds
     */
    private var progressSeconds: Int = 5 * 60

    // ──────────────────────────────────────────────────────────────────────────
    // Public control methods
    // ──────────────────────────────────────────────────────────────────────────

    fun incrementMinutes() {
        if (_uiState.value.timerState != TimerState.IDLE) return
        val newMin = (_uiState.value.totalMinutes + 1).coerceAtMost(99)
        applyIdleMinutes(newMin)
    }

    fun decrementMinutes() {
        if (_uiState.value.timerState != TimerState.IDLE) return
        val newMin = (_uiState.value.totalMinutes - 1).coerceAtLeast(1)
        applyIdleMinutes(newMin)
    }

    fun setTimerMode(mode: TimerMode) {
        if (_uiState.value.timerState != TimerState.IDLE) return
        val minutes = _uiState.value.totalMinutes
        _uiState.update {
            it.copy(
                timerMode = mode,
                displayMinutes = if (mode == TimerMode.COUNTDOWN) minutes else 0,
                displaySeconds = 0,
                fractionRemaining = 1f
            )
        }
    }

    fun setVolumeLevel(level: VolumeLevel) {
        _uiState.update { it.copy(volumeLevel = level) }
    }

    fun startTimer() {
        val state = _uiState.value
        if (state.timerState == TimerState.RUNNING) return

        if (state.timerState == TimerState.IDLE) {
            // Fresh start – initialise progressSeconds for the selected mode
            progressSeconds = if (state.timerMode == TimerMode.COUNTDOWN) totalSeconds else 0
        }

        _uiState.update { it.copy(timerState = TimerState.RUNNING) }
        launchTimerLoop()
    }

    fun pauseTimer() {
        if (_uiState.value.timerState != TimerState.RUNNING) return
        timerJob?.cancel()
        timerJob = null
        _uiState.update { it.copy(timerState = TimerState.PAUSED) }
    }

    fun resumeTimer() {
        if (_uiState.value.timerState != TimerState.PAUSED) return
        _uiState.update { it.copy(timerState = TimerState.RUNNING) }
        launchTimerLoop()
    }

    fun resetTimer() {
        timerJob?.cancel()
        timerJob = null
        flashJob?.cancel()
        flashJob = null
        stopAlarm()

        val mode = _uiState.value.timerMode
        val minutes = _uiState.value.totalMinutes
        progressSeconds = if (mode == TimerMode.COUNTDOWN) totalSeconds else 0

        _uiState.update {
            it.copy(
                timerState = TimerState.IDLE,
                displayMinutes = if (mode == TimerMode.COUNTDOWN) minutes else 0,
                displaySeconds = 0,
                fractionRemaining = 1f,
                isFlashing = false
            )
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Internal timer loop
    // ──────────────────────────────────────────────────────────────────────────

    private fun launchTimerLoop() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            val mode = _uiState.value.timerMode

            while (true) {
                delay(1_000L)
                if (_uiState.value.timerState != TimerState.RUNNING) break

                when (mode) {
                    TimerMode.COUNTDOWN -> {
                        progressSeconds = (progressSeconds - 1).coerceAtLeast(0)
                        val fraction = progressSeconds.toFloat() / totalSeconds.toFloat()
                        _uiState.update {
                            it.copy(
                                displayMinutes = progressSeconds / 60,
                                displaySeconds = progressSeconds % 60,
                                fractionRemaining = fraction
                            )
                        }
                        if (progressSeconds <= 0) { onTimerComplete(mode); break }
                    }
                    TimerMode.COUNTUP -> {
                        progressSeconds = (progressSeconds + 1).coerceAtMost(totalSeconds)
                        val fraction =
                            1f - (progressSeconds.toFloat() / totalSeconds.toFloat())
                        _uiState.update {
                            it.copy(
                                displayMinutes = progressSeconds / 60,
                                displaySeconds = progressSeconds % 60,
                                fractionRemaining = fraction.coerceIn(0f, 1f)
                            )
                        }
                        if (progressSeconds >= totalSeconds) { onTimerComplete(mode); break }
                    }
                }
            }
        }
    }

    private fun onTimerComplete(mode: TimerMode) {
        _uiState.update {
            it.copy(
                timerState = TimerState.COMPLETE,
                fractionRemaining = 0f,
                displayMinutes = if (mode == TimerMode.COUNTDOWN) 0 else it.totalMinutes,
                displaySeconds = 0,
                isFlashing = true
            )
        }
        playAlarm()
        startFlashLoop()
    }

    private fun startFlashLoop() {
        flashJob?.cancel()
        flashJob = viewModelScope.launch {
            repeat(8) {
                _uiState.update { it.copy(isFlashing = !it.isFlashing) }
                delay(350L)
            }
            _uiState.update { it.copy(isFlashing = false) }
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Sound
    // ──────────────────────────────────────────────────────────────────────────

    private fun playAlarm() {
        val level = _uiState.value.volumeLevel
        if (level == VolumeLevel.MUTE) return

        val volumePct = if (level == VolumeLevel.HIGH) 100 else 30

        // Try system alarm ringtone first
        try {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            if (uri != null) {
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    setDataSource(getApplication(), uri)
                    prepare()
                    setVolume(volumePct / 100f, volumePct / 100f)
                    start()
                }
                viewModelScope.launch {
                    delay(3_000)
                    mediaPlayer?.stop()
                    mediaPlayer?.release()
                    mediaPlayer = null
                }
                return
            }
        } catch (_: Exception) { /* fall through */ }

        // Fallback: ToneGenerator
        try {
            toneGenerator?.release()
            toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, volumePct)
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 3_000)
        } catch (_: Exception) { /* ignore */ }
    }

    private fun stopAlarm() {
        try { if (mediaPlayer?.isPlaying == true) mediaPlayer?.stop() } catch (_: Exception) {}
        mediaPlayer?.release()
        mediaPlayer = null
        toneGenerator?.stopTone()
        toneGenerator?.release()
        toneGenerator = null
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────────────────

    private fun applyIdleMinutes(minutes: Int) {
        totalSeconds = minutes * 60
        val mode = _uiState.value.timerMode
        progressSeconds = if (mode == TimerMode.COUNTDOWN) totalSeconds else 0
        _uiState.update {
            it.copy(
                totalMinutes = minutes,
                displayMinutes = if (mode == TimerMode.COUNTDOWN) minutes else 0,
                displaySeconds = 0,
                fractionRemaining = 1f
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        flashJob?.cancel()
        stopAlarm()
    }
}
