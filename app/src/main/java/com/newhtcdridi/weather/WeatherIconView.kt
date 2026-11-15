package com.newhtcdridi.weather

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Weather icon drawn with plain Canvas calls: it looks identical on every device
 * (no emoji or font dependency) and can animate gently (turning sun rays, falling rain...).
 *
 * All geometry is expressed as a fraction of the square that fits in the view.
 */
class WeatherIconView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    var condition: Condition = Condition.CLEAR
        set(value) {
            if (field != value) {
                field = value
                invalidate()
            }
        }

    /** Selects sun or moon for clear / partly cloudy conditions. */
    var daytime: Boolean = true
        set(value) {
            if (field != value) {
                field = value
                invalidate()
            }
        }

    /** When true the icon animates (only while the view is attached to a window). */
    var animated: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                updateAnimator()
            }
        }

    private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val line = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val path = Path()
    private val cutout = Path()

    /** Animation progress, 0..1, looping every [LOOP_MS]. */
    private var phase = 0f
    private var animator: ValueAnimator? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        updateAnimator()
    }

    override fun onDetachedFromWindow() {
        animator?.cancel()
        animator = null
        super.onDetachedFromWindow()
    }

    private fun updateAnimator() {
        if (animated && isAttachedToWindow) {
            if (animator == null) {
                animator = ValueAnimator.ofFloat(0f, 1f).apply {
                    duration = LOOP_MS
                    repeatCount = ValueAnimator.INFINITE
                    interpolator = LinearInterpolator()
                    addUpdateListener {
                        phase = it.animatedValue as Float
                        invalidate()
                    }
                    start()
                }
            }
        } else {
            animator?.cancel()
            animator = null
            phase = 0f
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val size = min(width, height).toFloat()
        if (size <= 0f) return

        canvas.save()
        canvas.translate((width - size) / 2f, (height - size) / 2f)
        when (condition) {
            Condition.CLEAR -> {
                if (daytime) {
                    drawSun(canvas, size, 0.5f, 0.5f, 0.2f)
                } else {
                    drawMoon(canvas, size, 0.5f, 0.5f, 0.24f)
                }
            }
            Condition.PARTLY_CLOUDY -> {
                if (daytime) {
                    drawSun(canvas, size, 0.36f, 0.36f, 0.15f)
                } else {
                    drawMoon(canvas, size, 0.36f, 0.36f, 0.17f)
                }
                drawCloud(canvas, size, 0.56f, 0.6f, 0.62f, CLOUD_LIGHT)
            }
            Condition.CLOUDY -> {
                drawCloud(canvas, size, 0.64f, 0.44f, 0.5f, CLOUD_DARK)
                drawCloud(canvas, size, 0.46f, 0.58f, 0.7f, CLOUD_LIGHT)
            }
            Condition.FOG -> {
                drawCloud(canvas, size, 0.5f, 0.36f, 0.66f, CLOUD_DARK)
                drawFogLines(canvas, size)
            }
            Condition.DRIZZLE -> {
                drawCloud(canvas, size, 0.5f, 0.38f, 0.72f, CLOUD_DARK)
                drawFalling(canvas, size, 0.5f, 0.64f, 0.44f, 3, false)
            }
            Condition.RAIN -> {
                drawCloud(canvas, size, 0.5f, 0.38f, 0.72f, CLOUD_DARK)
                drawFalling(canvas, size, 0.5f, 0.64f, 0.44f, 4, false)
            }
            Condition.SNOW -> {
                drawCloud(canvas, size, 0.5f, 0.38f, 0.72f, CLOUD_LIGHT)
                drawFalling(canvas, size, 0.5f, 0.64f, 0.44f, 4, true)
            }
            Condition.THUNDER -> {
                drawCloud(canvas, size, 0.5f, 0.36f, 0.74f, CLOUD_DARK)
                drawBolt(canvas, size, 0.5f, 0.36f, 0.74f)
            }
        }
        canvas.restore()
    }

    private fun drawSun(c: Canvas, s: Float, cx: Float, cy: Float, r: Float) {
        val x = cx * s
        val y = cy * s
        val radius = r * s
        fill.color = SUN
        c.drawCircle(x, y, radius, fill)

        line.color = SUN
        line.strokeWidth = radius * 0.32f
        // 8 rays: turning 90 degrees per loop is seamless because of the 45 degree symmetry.
        val rotation = Math.toRadians((phase * 90f).toDouble())
        for (i in 0 until 8) {
            val angle = rotation + i * PI / 4.0
            val dx = cos(angle).toFloat()
            val dy = sin(angle).toFloat()
            c.drawLine(
                x + dx * radius * 1.45f, y + dy * radius * 1.45f,
                x + dx * radius * 1.85f, y + dy * radius * 1.85f,
                line,
            )
        }
    }

    private fun drawMoon(c: Canvas, s: Float, cx: Float, cy: Float, r: Float) {
        val x = cx * s
        val y = cy * s
        val radius = r * s
        path.reset()
        path.addCircle(x, y, radius, Path.Direction.CW)
        cutout.reset()
        cutout.addCircle(x + radius * 0.55f, y - radius * 0.35f, radius * 0.85f, Path.Direction.CW)
        path.op(cutout, Path.Op.DIFFERENCE)
        fill.color = MOON
        c.drawPath(path, fill)
    }

    /** A cloud of width [w] (fraction of the view) centred horizontally on [cx]. */
    private fun drawCloud(c: Canvas, s: Float, cx: Float, cy: Float, w: Float, color: Int) {
        val u = w * s
        val x = cx * s
        val bob = sin(phase * 2f * PI.toFloat() * 4f) * 0.012f * s
        val y = cy * s + bob
        fill.color = color
        c.drawRoundRect(x - 0.5f * u, y - 0.05f * u, x + 0.5f * u, y + 0.25f * u, 0.15f * u, 0.15f * u, fill)
        c.drawCircle(x - 0.22f * u, y - 0.05f * u, 0.2f * u, fill)
        c.drawCircle(x + 0.05f * u, y - 0.18f * u, 0.28f * u, fill)
        c.drawCircle(x + 0.3f * u, y - 0.02f * u, 0.2f * u, fill)
    }

    /** Rain streaks or snow flakes falling below a cloud. */
    private fun drawFalling(
        c: Canvas,
        s: Float,
        cx: Float,
        top: Float,
        span: Float,
        count: Int,
        snow: Boolean,
    ) {
        val speed = if (snow) 5f else 10f // whole numbers keep the loop seamless
        for (i in 0 until count) {
            val t = (phase * speed + i.toFloat() / count) % 1f
            val x = (cx - span / 2f + span * (i + 0.5f) / count) * s
            val y = (top + t * 0.22f) * s
            // Fade while falling; static icons (forecast row) keep every drop fully visible.
            val alpha = if (animated) (255f * (1f - t * t)).toInt().coerceIn(0, 255) else 255
            if (snow) {
                fill.color = SNOW
                fill.alpha = alpha
                c.drawCircle(x + sin(t * 2f * PI.toFloat()) * 0.02f * s, y, 0.025f * s, fill)
            } else {
                line.color = RAIN
                line.alpha = alpha
                line.strokeWidth = 0.035f * s
                c.drawLine(x, y, x - 0.03f * s, y + 0.08f * s, line)
            }
        }
    }

    private fun drawBolt(c: Canvas, s: Float, cx: Float, cy: Float, w: Float) {
        val x = cx * s
        val y = cy * s
        val k = w * s
        path.reset()
        path.moveTo(x + 0.02f * k, y + 0.12f * k)
        path.lineTo(x - 0.12f * k, y + 0.40f * k)
        path.lineTo(x - 0.01f * k, y + 0.40f * k)
        path.lineTo(x - 0.08f * k, y + 0.70f * k)
        path.lineTo(x + 0.14f * k, y + 0.32f * k)
        path.lineTo(x + 0.03f * k, y + 0.32f * k)
        path.lineTo(x + 0.12f * k, y + 0.12f * k)
        path.close()
        fill.color = BOLT
        c.drawPath(path, fill)
    }

    private fun drawFogLines(c: Canvas, s: Float) {
        line.color = FOG
        line.strokeWidth = 0.055f * s
        val drift = sin(phase * 2f * PI.toFloat() * 2f) * 0.025f
        val ys = floatArrayOf(0.66f, 0.77f, 0.88f)
        val insets = floatArrayOf(0f, 0.08f, 0.03f)
        for (i in ys.indices) {
            val shift = if (i % 2 == 0) drift else -drift
            c.drawLine(
                (0.2f + insets[i] + shift) * s, ys[i] * s,
                (0.8f - insets[i] + shift) * s, ys[i] * s,
                line,
            )
        }
    }

    private companion object {
        const val LOOP_MS = 12_000L

        val SUN = 0xFFE0B767.toInt()
        val MOON = 0xFFE8E6D8.toInt()
        val CLOUD_LIGHT = 0xFFD7DCE5.toInt()
        val CLOUD_DARK = 0xFF8E97A6.toInt()
        val RAIN = 0xFF6CB4EE.toInt()
        val SNOW = 0xFFFFFFFF.toInt()
        val BOLT = 0xFFF5C84C.toInt()
        val FOG = 0xFFAEB6C2.toInt()
    }
}
