package com.app.plutope.utils.extras

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import androidx.core.view.GestureDetectorCompat


/*abstract class DoubleClickListener : View.OnClickListener {
    var lastClickTime: Long = 0
    override fun onClick(v: View?) {
        val clickTime = System.currentTimeMillis()
        if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
            onDoubleClick(v)
        }
        lastClickTime = clickTime
    }

    abstract fun onDoubleClick(v: View?)

    companion object {
        private const val DOUBLE_CLICK_TIME_DELTA: Long = 300 //milliseconds
    }
}*/

abstract class DoubleClickListener : View.OnClickListener {
    var lastClickTime: Long = 0
    override fun onClick(v: View?) {
        val clickTime = System.currentTimeMillis()
        if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
            onDoubleClick(v)
            lastClickTime = 0
        } else {
            onSingleClick(v)
        }
        lastClickTime = clickTime
    }

    abstract fun onSingleClick(v: View?)
    abstract fun onDoubleClick(v: View?)

    companion object {
        private const val DOUBLE_CLICK_TIME_DELTA: Long = 300 //milliseconds
    }
}


class MyView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {
    var gestureDetector: GestureDetector

    init {
        // creating new gesture detector
        gestureDetector = GestureDetector(context, GestureListener())
    }

    // skipping measure calculation and drawing
    // delegate the event to the gesture detector
    override fun onTouchEvent(e: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(e)
    }

    private inner class GestureListener : SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        // event when double tap occurs
        override fun onDoubleTap(e: MotionEvent): Boolean {
            val x = e.x
            val y = e.y
            return true
        }
    }
}

fun View.setOnDoubleTapListener(action: () -> Unit) {

    // instantiate GestureDetectorCompat
    val gDetector = GestureDetectorCompat(
        this.context,
        SimpleOnGestureListener()
    )

    // Create anonymous class extend OnTouchListener and SimpleOnGestureListener
    val touchListener =
        object : View.OnTouchListener, SimpleOnGestureListener() {
            override fun onTouch(view: View?, event: MotionEvent?): Boolean {

                gDetector.onTouchEvent(event!!)
                gDetector.setOnDoubleTapListener(this)

                return true
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                action()
                return true
            }
        }

    this.setOnTouchListener(touchListener)

}