package com.smiletimer.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.smiletimer.ui.theme.FaceBlush
import com.smiletimer.ui.theme.FaceMouth
import com.smiletimer.ui.theme.FaceStar
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Animated smiley face drawn on a Canvas.
 *
 * [expressionValue] 1.0 = very happy, 0.0 = very sad.
 * The face adapts expression, eyebrow angle, cheek blush, and mouth curve.
 */
@Composable
fun SmileFace(
    expressionValue: Float,
    modifier: Modifier = Modifier
) {
    // Smooth the expression transitions
    val animExpr by animateFloatAsState(
        targetValue = expressionValue.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600),
        label = "expression"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w * 0.5f
        val cy = h * 0.45f               // Shift face up slightly to leave room for time
        val faceRadius = minOf(w, h) * 0.36f

        // ── Face circle (semi-transparent dark fill with subtle glow) ─────────
        drawCircle(
            color = Color(0xFF1A1A2E),
            radius = faceRadius,
            center = Offset(cx, cy)
        )
        // Subtle coloured border ring that fades to white as time is ample
        val ringAlpha = 0.25f + animExpr * 0.3f
        drawCircle(
            color = Color.White.copy(alpha = ringAlpha),
            radius = faceRadius,
            center = Offset(cx, cy),
            style = Stroke(width = faceRadius * 0.04f)
        )

        // ── Cheek blush (visible only when happy) ────────────────────────────
        if (animExpr > 0.4f) {
            val blushAlpha = ((animExpr - 0.4f) / 0.6f) * 0.45f
            val blushR = faceRadius * 0.18f
            val blushY = cy + faceRadius * 0.22f
            drawCircle(
                color = FaceBlush.copy(alpha = blushAlpha),
                radius = blushR,
                center = Offset(cx - faceRadius * 0.42f, blushY)
            )
            drawCircle(
                color = FaceBlush.copy(alpha = blushAlpha),
                radius = blushR,
                center = Offset(cx + faceRadius * 0.42f, blushY)
            )
        }

        // ── Eye positions ─────────────────────────────────────────────────────
        val eyeOffsetX = faceRadius * 0.32f
        val eyeY       = cy - faceRadius * 0.12f
        val starOuter  = faceRadius * 0.155f
        val starInner  = starOuter * 0.42f

        // Left eye star
        drawStar(
            center    = Offset(cx - eyeOffsetX, eyeY),
            outerR    = starOuter,
            innerR    = starInner,
            numPoints = 5,
            color     = FaceStar
        )
        // Right eye star
        drawStar(
            center    = Offset(cx + eyeOffsetX, eyeY),
            outerR    = starOuter,
            innerR    = starInner,
            numPoints = 5,
            color     = FaceStar
        )

        // ── Eyebrows ──────────────────────────────────────────────────────────
        // Neutral (happy) → inner ends raised, outer ends lower = worried brows
        val browTilt   = (1f - animExpr) * faceRadius * 0.12f
        val browW      = faceRadius * 0.24f
        val browY      = eyeY - starOuter * 1.6f
        val browThick  = faceRadius * 0.055f

        // Left brow: outer-left is low, inner-right is raised when sad
        val leftBrow = Path().apply {
            moveTo(cx - eyeOffsetX - browW, browY + browTilt)
            quadraticBezierTo(
                cx - eyeOffsetX, browY - faceRadius * 0.04f,
                cx - eyeOffsetX + browW * 0.7f, browY - browTilt
            )
        }
        drawPath(leftBrow, FaceStar.copy(alpha = 0.85f),
            style = Stroke(width = browThick, cap = StrokeCap.Round))

        // Right brow (mirror)
        val rightBrow = Path().apply {
            moveTo(cx + eyeOffsetX - browW * 0.7f, browY - browTilt)
            quadraticBezierTo(
                cx + eyeOffsetX, browY - faceRadius * 0.04f,
                cx + eyeOffsetX + browW, browY + browTilt
            )
        }
        drawPath(rightBrow, FaceStar.copy(alpha = 0.85f),
            style = Stroke(width = browThick, cap = StrokeCap.Round))

        // ── Mouth ─────────────────────────────────────────────────────────────
        // animExpr=1 → big smile, animExpr=0 → deep frown
        val mouthW   = faceRadius * 0.52f
        val mouthY   = cy + faceRadius * 0.36f
        // control-point Y: positive = smile (below baseline), negative = frown
        val mouthCtrl = lerp(-mouthW * 0.55f, mouthW * 0.60f, animExpr)

        val mouth = Path().apply {
            moveTo(cx - mouthW, mouthY)
            quadraticBezierTo(cx, mouthY + mouthCtrl, cx + mouthW, mouthY)
        }

        // Fill the grin with white when very happy
        if (animExpr > 0.72f) {
            val fillAlpha = ((animExpr - 0.72f) / 0.28f) * 0.25f
            val grinFill = Path().apply {
                moveTo(cx - mouthW, mouthY)
                quadraticBezierTo(cx, mouthY + mouthCtrl, cx + mouthW, mouthY)
                lineTo(cx + mouthW, mouthY + mouthCtrl * 0.5f)
                quadraticBezierTo(cx, mouthY + mouthCtrl * 1.3f, cx - mouthW, mouthY + mouthCtrl * 0.5f)
                close()
            }
            drawPath(grinFill, Color.White.copy(alpha = fillAlpha), style = Fill)
        }

        drawPath(
            mouth,
            FaceMouth,
            style = Stroke(width = faceRadius * 0.072f, cap = StrokeCap.Round)
        )
    }
}

// ── Canvas helpers ─────────────────────────────────────────────────────────────

/** Draw a filled N-pointed star centred at [center]. */
private fun DrawScope.drawStar(
    center: Offset,
    outerR: Float,
    innerR: Float,
    numPoints: Int = 5,
    color: Color
) {
    val path = Path()
    val step = (PI / numPoints).toFloat()        // half the angular spacing
    val startAngle = (-PI / 2).toFloat()          // tip pointing up

    for (i in 0 until numPoints * 2) {
        val angle = startAngle + i * step
        val r = if (i % 2 == 0) outerR else innerR
        val x = center.x + r * cos(angle)
        val y = center.y + r * sin(angle)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color)
}

/** Linear interpolation. */
private fun lerp(start: Float, stop: Float, fraction: Float): Float =
    start + (stop - start) * fraction.coerceIn(0f, 1f)
