package com.app.plutope.utils.extras

import android.os.SystemClock
import android.view.MotionEvent
import android.view.View

class SafeClickListener(
    private var defaultInterval: Int = 3000,
    private val onSafeCLick: (View) -> Unit
) : View.OnClickListener {
    private var lastTimeClicked: Long = 0
    override fun onClick(v: View) {
        if (SystemClock.elapsedRealtime() - lastTimeClicked < defaultInterval) {
            return
        }
        lastTimeClicked = SystemClock.elapsedRealtime()
        onSafeCLick(v)
    }
}

fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {
    val safeClickListener = SafeClickListener {
        onSafeClick(it)
    }
    setOnClickListener(safeClickListener)
}

fun View.buttonClickedWithEffect(clicked: () -> Unit) {
    this.setOnTouchListener { view, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // view.animate().scaleX(1.1f).scaleY(1.1f).setDuration(0).start()
            }

            MotionEvent.ACTION_UP -> {
                // view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(50).start()
                clicked.invoke()
                view.performClick()
            }
        }
        true
    }
}