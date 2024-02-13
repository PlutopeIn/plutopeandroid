package com.app.plutope.custom_views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.SweepGradient
import android.util.AttributeSet
import android.view.View


class ProgressiveCircleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Define the border gradient colors here


    val color1 = Color.parseColor("#C693FE")
    val color2 = Color.parseColor("#9654D9")
    val color3 = Color.parseColor("#5ABEF8")
    val color4 = Color.parseColor("#00C6FB")
    val color5 = Color.parseColor("#C471F5")
    val color6 = Color.parseColor("#FA71CD")
    val color7 = Color.parseColor("#48C6EF")
    val color8 = Color.parseColor("#6F86D6")

    val color9 = Color.parseColor("#9654D9")
    val color10 = Color.parseColor("#C693FE")


    private val borderColors = intArrayOf(
        color10, color9, color8, color7, color6, color5, color4, color3, color2, color1
    )

    // Define the inner color
    private val innerColor = Color.TRANSPARENT

    // Animation properties
    private var sweepAngle = 0F
    private val animatorDuration = 0L // 3000 Duration of animation in milliseconds
    private val animator = ValueAnimator.ofFloat(0F, 360F).apply {
        duration = animatorDuration
        /* repeatMode = ValueAnimator.RESTART
         repeatCount = ValueAnimator.INFINITE*/
        addUpdateListener {
            sweepAngle = it.animatedValue as Float
            invalidate()
        }
    }

    init {
        animator.start()
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2F
        val centerY = height / 2F
        val radius = width / 4F

        // Draw the outer border with a gradient
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        borderPaint.style = Paint.Style.STROKE
        val displayMetrics = resources.displayMetrics
        val dpi = displayMetrics.densityDpi.toFloat()
        val dpValue = 10f   // Replace this with your desired value in dp
        val pxValue = dpValue * (dpi / 160)
        borderPaint.strokeWidth = pxValue
        val borderSweepGradient = SweepGradient(centerX, centerY, borderColors, null)
        borderPaint.shader = borderSweepGradient

        // Draw the outer circle
        canvas.drawArc(
            centerX - radius, centerY - radius, centerX + radius, centerY + radius,
            -90F, sweepAngle, false, borderPaint
        )

        // Draw the inner circle with a solid color
        val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        innerPaint.style = Paint.Style.FILL
        innerPaint.color = innerColor

        // Adjust the inner circle radius to create the desired thickness of the border
        val innerRadius = radius - borderPaint.strokeWidth / 2
        canvas.drawCircle(centerX, centerY, innerRadius, innerPaint)
    }
}


