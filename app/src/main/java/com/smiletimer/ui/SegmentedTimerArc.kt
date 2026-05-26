package com.smiletimer.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp
import com.smiletimer.ui.theme.SegmentDim
import com.smiletimer.ui.theme.SegmentGreen
import com.smiletimer.ui.theme.SegmentOrange
import com.smiletimer.ui.theme.SegmentRed
import com.smiletimer.ui.theme.SegmentYellow
import kotlin.math.PI
import kotlin.math.roundToInt

private const val TOTAL_SEGMENTS = 36

/**
 * Circular segmented progress arc.
 *
 * [visualProgress] is always in the range 0.0–1.0:
 *   • Countdown: 1.0 at start → 0.0 at end  (caller passes fractionRemaining)
 *   • Count-up:  0.0 at start → 1.0 at end  (caller passes 1 − fractionRemaining)
 *
 * [isCountUp] reverses which segments are lit so the arc fills in the opposite
 * direction from countdown — creating a true mirror animation.
 *
 * Colour always tracks [visualProgress]: 0 = red, 1 = green.
 */
@Composable
fun SegmentedTimerArc(
    visualProgress: Float,
    isFlashing: Boolean,
    isCountUp: Boolean = false,
    modifier: Modifier = Modifier
) {
    val targetColor = progressColor(visualProgress)

    val animColor by animateColorAsState(
        targetValue   = if (isFlashing) Color.White else targetColor,
        animationSpec = tween(durationMillis = 350),
        label         = "arcColor"
    )

    val animProgress by animateFloatAsState(
        targetValue   = visualProgress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800),
        label         = "arcProgress"
    )

    Canvas(modifier = modifier) {
        val canvasSize  = minOf(size.width, size.height)
        val center      = Offset(canvasSize / 2f, canvasSize / 2f)
        val strokeWidth = canvasSize * 0.072f
        val outerRadius = canvasSize / 2f - 4.dp.toPx()
        val midRadius   = outerRadius - strokeWidth / 2f

        val segTangential = midRadius * 2f * PI.toFloat() / TOTAL_SEGMENTS * 0.72f
        val segRadial     = strokeWidth * 0.85f
        val cornerSize    = segTangential * 0.32f

        val litCount = (animProgress * TOTAL_SEGMENTS).roundToInt().coerceIn(0, TOTAL_SEGMENTS)

        for (i in 0 until TOTAL_SEGMENTS) {
            val angleDeg = 360f * i / TOTAL_SEGMENTS

            // Countdown: fill clockwise from 12 o'clock (indices 0 … litCount-1 are lit).
            // Count-up:  fill from the clockwise end back toward 12 o'clock — the exact
            //            reverse — so the arc grows from the position where countdown
            //            would last shrink to.
            val isLit = if (isCountUp) {
                i >= (TOTAL_SEGMENTS - litCount)
            } else {
                i < litCount
            }

            withTransform({ rotate(degrees = angleDeg, pivot = center) }) {
                drawRoundRect(
                    color        = if (isLit) animColor else SegmentDim,
                    topLeft      = Offset(
                        x = center.x - segTangential / 2f,
                        y = center.y - midRadius - segRadial / 2f
                    ),
                    size         = Size(segTangential, segRadial),
                    cornerRadius = CornerRadius(cornerSize, cornerSize)
                )
            }
        }
    }
}

/** Maps progress (0 = red, 1 = green) to a smooth colour. */
private fun progressColor(progress: Float): Color {
    val p = progress.coerceIn(0f, 1f)
    return when {
        p >= 0.75f -> lerp(SegmentYellow, SegmentGreen,  (p - 0.75f) / 0.25f)
        p >= 0.50f -> lerp(SegmentOrange, SegmentYellow, (p - 0.50f) / 0.25f)
        p >= 0.25f -> lerp(SegmentRed,    SegmentOrange, (p - 0.25f) / 0.25f)
        else       -> SegmentRed
    }
}

private fun lerp(a: Color, b: Color, t: Float): Color {
    val tc = t.coerceIn(0f, 1f)
    return Color(
        red   = a.red   + (b.red   - a.red)   * tc,
        green = a.green + (b.green - a.green) * tc,
        blue  = a.blue  + (b.blue  - a.blue)  * tc,
        alpha = a.alpha + (b.alpha - a.alpha) * tc
    )
}
