package com.example.hydrasense

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation

class RadarView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#801E88E5")
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    private var radius = 0f
    private var maxRadius = 0f

    fun startAnimation() {
        val anim = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                radius = maxRadius * interpolatedTime
                invalidate()
            }
        }
        anim.duration = 2000
        anim.repeatCount = Animation.INFINITE
        startAnimation(anim)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        maxRadius = Math.min(w, h) / 2f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawCircle(width / 2f, height / 2f, radius, paint)
        canvas.drawCircle(width / 2f, height / 2f, radius * 0.7f, paint)
        canvas.drawCircle(width / 2f, height / 2f, radius * 0.4f, paint)
    }

    fun stopAnimation() {
        clearAnimation()
    }
}
