package com.example.hydrasense

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.sin
import kotlin.random.Random

class WaveWaterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    /* ---------- PAINTS ---------- */

    private val waterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2196F3")
        style = Paint.Style.FILL
    }

    private val mainTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 42f
        typeface = Typeface.DEFAULT_BOLD
    }

    private val subTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 26f
    }

    private val lipPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        alpha = 60
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }

    private val frostPaint = Paint().apply {
        color = Color.WHITE
        alpha = 25
    }

    private val bubblePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        alpha = 80
    }

    /* ---------- PATHS ---------- */

    private val wavePath = Path()
    private val clipPath = Path()
    private val rect = RectF()

    /* ---------- WAVE PROPERTIES ---------- */

    private var waveOffset = 0f
    private var waveAmplitude = 20f     // ← will be ESP32-driven
    private val waveLength = 200f

    /* ---------- ML DATA ---------- */

    private var fillPercent = 0
    private var percentText = "0%"
    private var statusText = "Analyzing..."
    private var waterLeftText = "0.0 L left"

    /* ---------- BUBBLES ---------- */

    data class Bubble(var x: Float, var y: Float, var r: Float)
    private val bubbles = mutableListOf<Bubble>()

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)

        repeat(15) {
            bubbles.add(
                Bubble(
                    Random.nextFloat() * 300f,
                    Random.nextFloat() * 600f,
                    4f + Random.nextFloat() * 6f
                )
            )
        }
    }

    /* ---------- PUBLIC APIs ---------- */

    fun updateMlData(percent: Int, status: String, waterLeft: Double) {
        fillPercent = percent
        percentText = "$percent%"
        statusText = status
        waterLeftText = String.format("%.1f L left", waterLeft)
        invalidate()
    }

    // 🔗 ESP32 / Firebase hook
    fun updateFromEsp32(sensorValue: Int) {
        waveAmplitude = 10f + (sensorValue * 0.3f)
        invalidate()
    }

    fun startWave() {
        post(object : Runnable {
            override fun run() {
                waveOffset += 8f
                invalidate()
                postDelayed(this, 16) // ~60 FPS
            }
        })
    }

    /* ---------- DRAW ---------- */

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()

        /* ---- Dynamic corner radius ---- */
        val topRadius = 16f + (fillPercent * 0.4f)
        val bottomRadius = 12f

        rect.set(0f, 0f, w, h)
        clipPath.reset()
        clipPath.addRoundRect(
            rect,
            floatArrayOf(
                topRadius, topRadius,
                topRadius, topRadius,
                bottomRadius, bottomRadius,
                bottomRadius, bottomRadius
            ),
            Path.Direction.CW
        )

        canvas.save()
        canvas.clipPath(clipPath)

        /* ---- Water waves ---- */
        wavePath.reset()
        wavePath.moveTo(0f, h)

        var x = 0f
        while (x <= w) {
            val y = waveAmplitude *
                    sin((2 * Math.PI / waveLength) * (x + waveOffset)).toFloat()
            wavePath.lineTo(x, y)
            x += 10f
        }

        wavePath.lineTo(w, h)
        wavePath.close()
        canvas.drawPath(wavePath, waterPaint)

        /* ---- Bubble animation ---- */
        for (b in bubbles) {
            b.y -= 1.5f
            if (b.y < 0) b.y = h
            canvas.drawCircle(b.x, b.y, b.r, bubblePaint)
        }

        /* ---- ML text ---- */
        val cx = w / 2
        val cy = h / 2
        canvas.drawText(percentText, cx, cy - 20f, mainTextPaint)
        canvas.drawText(statusText, cx, cy + 18f, subTextPaint)
        canvas.drawText(waterLeftText, cx, cy + 50f, subTextPaint)

        /* ---- Glass lip highlight ---- */
        canvas.drawLine(
            12f,
            waveAmplitude + 6f,
            w - 12f,
            waveAmplitude + 6f,
            lipPaint
        )

        canvas.restore()

        /* ---- Frosted glass overlay ---- */
        canvas.drawRect(0f, 0f, w, h, frostPaint)
    }
}
