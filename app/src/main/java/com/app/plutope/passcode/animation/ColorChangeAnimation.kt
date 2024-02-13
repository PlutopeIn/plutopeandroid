package com.app.plutope.passcode.animation

import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import com.app.plutope.passcode.CircleView

internal abstract class ColorChangeAnimation(
    target: CircleView
) : Animator(target) {

    private var animator: ValueAnimator? = null
    var toColor: Int = 0
    var duration: Long = 200
    var startDelay: Long = 100

    override fun start() {
        animator = ValueAnimator.ofArgb(getColor(), toColor).apply {
            duration = this@ColorChangeAnimation.duration
            startDelay = this@ColorChangeAnimation.startDelay
            addUpdateListener {
                setColor(it.animatedValue as Int)
            }
        }
        animator?.start()
    }

    override fun addListener(listener: AnimatorListener) {
        animator?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                listener.onAnimationEnd()
            }
        })
    }

    protected abstract fun getColor(): Int
    abstract fun setColor(color: Int)
}