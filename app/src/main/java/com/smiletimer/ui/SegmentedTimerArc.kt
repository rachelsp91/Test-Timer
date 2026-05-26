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
 * Circular segmented progress arc inspired by LED clock timers.
 *
 * Segments are arranged clockwise from 12 o'clock.
 * Lit segments share a single animated colour that shifts from green → red
 * as [fractionRemaining] decreases.
 *
 * [isFlashing] causes the lit colour to pulse white for the completion alert.
 */
@Composable
fun SegmentedTimerArc(
    fractionRemaining: Float,
    isFlashing: Boolean,
    modifier: Modifier = Modifier
) {
    // ── Colour based on fraction remaining ────────────────────────────────────
    val targetColor = progressColor(fractionRemaining)
    val flashColor  = Color.White

    val animColor by animateColorAsState(
        targetValue  = if (isFlashing) flashColor else targetColor,
        animationSpec = tween(durationMillis = 350),
        label        = "arcColor"
    )

    // ── Smooth lit-segment count ───────────────────────────────────────────────
    val animFraction by animateFloatAsState(
        targetValue   = fractionRemaining.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800),
        label         = "arcFraction"
    )

    Canvas(modifier = modifier) {
        val canvasSize   = minOf(size.width, size.height)
        val center       = Offset(canvasSize / 2f, canvasSize / 2f)

        // Stroke geometry
        val strokeWidth  = canvasSize * 0.072f
        val outerRadius  = canvasSize / 2f - 4.dp.toPx()
        val midRadius    = outerRadius - strokeWidth / 2f

        // Segment rectangle dimensions (tangential × radial)
        val segTangential = midRadius * 2f * PI.toFloat() / TOTAL_SEGMENTS * 0.72f
        val segRadial     = strokeWidth * 0.85f
        val cornerSize    = segTangential * 0.32f

        val litCount = (animFraction * TOTAL_SEGMENTS).roundToInt().coerceIn(0, TOTAL_SEGMENTS)

        for (i in 0 until TOTAL_SEGMENTS) {
            // i=0 sits at 12 o'clock; segments go clockwise
            val angleDeg = 360f * i / TOTAL_SEGMENTS
            val isLit    = i < litCount
            val segColor = if (isLit) animColor else SegmentDim

            withTransform({
                rotate(degrees = angleDeg, pivot = center)
            }) {
                // After rotation, draw the segment above center (the "12 o'clock" position)
                drawRoundRect(
                    color        = segColor,
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

/** Interpolate between colour stops based on [fraction] (0=red, 1=green). */
private fun progressColor(fraction: Float): Color {
    val f = fraction.coerceIn(0f, 1f)
    return when {
        f >= 0.75f -> lerp(SegmentYellow, SegmentGreen,  (f - 0.75f) / 0.25f)
        f >= 0.50f -> lerp(SegmentOrange, SegmentYellow, (f - 0.50f) / 0.25f)
        f >= 0.25f -> lerp(SegmentRed,    SegmentOrange, (f - 0.25f) / 0.25f)
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
