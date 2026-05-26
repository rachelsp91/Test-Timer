package com.smiletimer.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.smiletimer.ui.theme.FaceBlush
import com.smiletimer.ui.theme.FaceMouth
import com.smiletimer.ui.theme.FaceStar
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Static cute smiley face — always happy 😊.
 *
 * Designed to match the reference LED clock timer product:
 *   • Large white ⭐ star eyes
 *   • Small, gentle smile positioned between the eyes
 *   • Pink blush circles pushed out near the face edge
 *   • No eyebrows
 */
@Composable
fun SmileFace(
    expressionValue: Float = 1f,   // reserved for future use; currently always 1f
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val cx         = size.width  * 0.5f
        val cy         = size.height * 0.45f    // slightly above vertical centre
        val faceRadius = minOf(size.width, size.height) * 0.36f

        // ── Dark face circle ──────────────────────────────────────────────────
        drawCircle(
            color  = Color(0xFF1A1A2E),
            radius = faceRadius,
            center = Offset(cx, cy)
        )
        // Subtle white ring border
        drawCircle(
            color  = Color.White.copy(alpha = 0.22f),
            radius = faceRadius,
            center = Offset(cx, cy),
            style  = Stroke(width = faceRadius * 0.04f)
        )

        // ── Cheek blush — near the face edge ─────────────────────────────────
        val blushR = faceRadius * 0.14f
        val blushY = cy + faceRadius * 0.14f
        val blushX = faceRadius * 0.60f          // pushed out close to the edge
        drawCircle(FaceBlush.copy(alpha = 0.50f), radius = blushR,
            center = Offset(cx - blushX, blushY))
        drawCircle(FaceBlush.copy(alpha = 0.50f), radius = blushR,
            center = Offset(cx + blushX, blushY))

        // ── Star eyes — large ─────────────────────────────────────────────────
        val eyeOffsetX = faceRadius * 0.30f
        val eyeY       = cy - faceRadius * 0.10f
        val starOuter  = faceRadius * 0.22f      // bigger than before
        val starInner  = starOuter * 0.38f

        drawStar(Offset(cx - eyeOffsetX, eyeY), starOuter, starInner, color = FaceStar)
        drawStar(Offset(cx + eyeOffsetX, eyeY), starOuter, starInner, color = FaceStar)

        // ── No eyebrows ───────────────────────────────────────────────────────

        // ── Smile — small and close to the eyes ──────────────────────────────
        // Width roughly spans the gap between the two eyes (not edge-to-edge)
        val mouthW    = faceRadius * 0.24f
        val mouthY    = cy + faceRadius * 0.20f  // just below eye level
        val mouthCtrl = faceRadius * 0.09f       // shallow, gentle curve

        val mouth = Path().apply {
            moveTo(cx - mouthW, mouthY)
            quadraticBezierTo(cx, mouthY + mouthCtrl, cx + mouthW, mouthY)
        }
        drawPath(
            mouth,
            FaceMouth,
            style = Stroke(width = faceRadius * 0.068f, cap = StrokeCap.Round)
        )
    }
}

// ── Canvas helper ─────────────────────────────────────────────────────────────

/** Draws a filled N-pointed star centred at [center], tip pointing up. */
private fun DrawScope.drawStar(
    center: Offset,
    outerR: Float,
    innerR: Float,
    numPoints: Int = 5,
    color: Color
) {
    val path       = Path()
    val step       = (PI / numPoints).toFloat()
    val startAngle = (-PI / 2).toFloat()          // top-tip at 12 o'clock

    for (i in 0 until numPoints * 2) {
        val angle = startAngle + i * step
        val r     = if (i % 2 == 0) outerR else innerR
        val x     = center.x + r * cos(angle)
        val y     = center.y + r * sin(angle)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color)
}
