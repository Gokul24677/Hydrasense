package com.example.hydrasense

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class HydrationMeterView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var percentage: Int = 0
    private val paintCircle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 40f
        strokeCap = Paint.Cap.ROUND
    }
    
    private val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 80f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val rectF = RectF()

    fun setPercentage(value: Int) {
        percentage = value.coerceIn(0, 100)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val center = width / 2f
        val radius = (Math.min(width, height) / 2f) - 60f
        rectF.set(center - radius, center - radius, center + radius, center + radius)

        // Background Circle (Light Grey)
        paintCircle.color = Color.parseColor("#EEEEEE")
        canvas.drawArc(rectF, 135f, 270f, false, paintCircle)

        // Foreground Meter (Gradient)
        val color = when {
            percentage < 30 -> "#F44336" // Red
            percentage < 60 -> "#FF9800" // Orange
            else -> "#2196F3"            // Blue
        }
        paintCircle.color = Color.parseColor(color)
        val sweepAngle = (percentage / 100f) * 270f
        canvas.drawArc(rectF, 135f, sweepAngle, false, paintCircle)

        // Text
        canvas.drawText("$percentage%", center, center + 30f, paintText)
    }
}
