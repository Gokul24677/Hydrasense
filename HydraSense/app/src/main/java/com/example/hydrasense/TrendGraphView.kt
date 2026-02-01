package com.example.hydrasense

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class TrendGraphView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val phPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#42A5F5") // Blue
        style = Paint.Style.STROKE
        strokeWidth = 6f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val colorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFCA28") // Amber/Yellow
        style = Paint.Style.STROKE
        strokeWidth = 6f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.LTGRAY
        strokeWidth = 2f
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        textSize = 24f
    }

    private var phData = listOf<Double>()
    private var colorData = listOf<Double>()
    private val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    fun setData(phList: List<Double>, colorList: List<Double>) {
        this.phData = phList
        this.colorData = colorList
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (phData.isEmpty() || colorData.isEmpty()) return

        val padding = 60f
        val graphHeight = height - padding * 2
        val graphWidth = width - padding * 2
        val stepX = if (phData.size > 1) graphWidth / (phData.size - 1) else 0f

        // Draw axes
        canvas.drawLine(padding, padding, padding, height - padding, axisPaint)
        canvas.drawLine(padding, height - padding, width - padding, height - padding, axisPaint)

        // Draw pH line
        val phPath = Path()
        phData.forEachIndexed { index, value ->
            // Normalize pH 5.0 to 9.0
            val y = height - padding - ((value - 5.0) / 4.0 * graphHeight).toFloat()
            val x = padding + index * stepX
            if (index == 0) phPath.moveTo(x, y) else phPath.lineTo(x, y)
            
            // Draw x-axis labels
            if (index < days.size) {
                canvas.drawText(days[index], x - 20f, height - padding / 2, textPaint)
            }
        }
        canvas.drawPath(phPath, phPaint)

        // Draw Color line (normalize 0 to 5000)
        val colorPath = Path()
        colorData.forEachIndexed { index, value ->
            val y = height - padding - (value / 5000.0 * graphHeight).toFloat()
            val x = padding + index * stepX
            if (index == 0) colorPath.moveTo(x, y) else colorPath.lineTo(x, y)
        }
        canvas.drawPath(colorPath, colorPaint)
        
        // Legends
        canvas.drawCircle(padding + 20f, padding / 2, 8f, phPaint)
        canvas.drawText("pH", padding + 40f, padding / 2 + 10f, textPaint)
        
        canvas.drawCircle(padding + 100f, padding / 2, 8f, colorPaint)
        canvas.drawText("Color", padding + 120f, padding / 2 + 10f, textPaint)
    }
}
