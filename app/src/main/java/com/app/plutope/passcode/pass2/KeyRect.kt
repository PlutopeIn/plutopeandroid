package com.app.plutope.passcode.pass2

import android.animation.Animator
import android.animation.ValueAnimator
import android.graphics.Rect
import android.view.View


class KeyRect(private val view: View, var rect: Rect, var value: String) {
    var rippleRadius = 0
    var requiredRadius: Int = 0
    var circleAlpha: Int = 0
    var hasRippleEffect = false
    var animator: ValueAnimator? = null
    private val MAX_RIPPLE_ALPHA = 180
    private var interpolatedValueListener: InterpolatedValueListener? = null
    private var rippleAnimListener: RippleAnimListener? = null

    init {
        requiredRadius = (this.rect.right - this.rect.left) / 4
        setUpAnimator()
    }

    /**
     * Initialize the fields and listener for the ripple effect animator
     */
    private fun setUpAnimator() {
        animator = ValueAnimator.ofFloat(0f, requiredRadius.toFloat())
        animator?.duration = 400
        val circleAlphaOffset = MAX_RIPPLE_ALPHA / requiredRadius.toFloat()
        animator?.addUpdateListener { animation ->
            if (hasRippleEffect) {
                val animatedValue = animation.animatedValue as Float
                rippleRadius = animatedValue.toInt()
                println("Ripple start, radius $rippleRadius")
                circleAlpha = (MAX_RIPPLE_ALPHA - animatedValue * circleAlphaOffset).toInt()
                interpolatedValueListener?.onValueUpdated()
            }
        }

        animator?.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                hasRippleEffect = true
            }

            override fun onAnimationEnd(animation: Animator) {
                hasRippleEffect = false
                rippleRadius = 0
                rippleAnimListener?.onEnd()
            }

            override fun onAnimationCancel(animation: Animator) {}

            override fun onAnimationRepeat(animation: Animator) {}


        })
    }

    /**
     * Set the value of this Key
     *
     * @param value - Value to be set for this key
     */
    /*
        fun setValue(value: String) {
            this.value = value
        }
    */

    /**
     * Show animation indicating an invalid pin code
     */
    fun setError() {
        val goLeftAnimator = ValueAnimator.ofInt(0, 5)
        goLeftAnimator.interpolator = android.view.animation.CycleInterpolator(2f)
        goLeftAnimator.addUpdateListener { animation ->
            rect.left += animation.animatedValue as Int
            rect.right += animation.animatedValue as Int
            view.invalidate()
        }
        goLeftAnimator.start()
    }

    interface InterpolatedValueListener {
        fun onValueUpdated()
    }

    fun setOnValueUpdateListener(listener: InterpolatedValueListener) {
        this.interpolatedValueListener = listener
    }

    /**
     * Start playing ripple animation and notify the listener accordingly
     *
     * @param listener - RippleAnimListener object to be notified
     */
    fun playRippleAnim(listener: RippleAnimListener) {
        this.rippleAnimListener = listener
        setOnValueUpdateListener(object : InterpolatedValueListener {
            override fun onValueUpdated() {
                view.invalidate(rect)
            }
        })
        rippleAnimListener?.onStart()
        animator?.start()
    }

    /**
     * Interface to get notified of the ripple animation status
     */
    interface RippleAnimListener {
        fun onStart()
        fun onEnd()
    }
}





