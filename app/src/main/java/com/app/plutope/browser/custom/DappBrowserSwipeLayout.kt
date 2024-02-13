package com.app.plutope.browser.custom

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

/**
 * Created by Pravin on 15/01/2024.
 * Ahmedabad in India
 *
 * This class overrides the SwipeRefreshLayout and makes the swipe refresh less sensitive.
 * To create a swipe refresh event user must make a quick, medium to large downward swipe of less than 300ms;
 * Otherwise a slower event will be treated as a browser scroll event.
 *
 */
class DAppBrowserSwipeLayout : SwipeRefreshLayout {
    private var trackMove = 0f
    private var alwaysDown = false
    private var lastY = 0f
    private var canRefresh = false
    private var refreshInterface: DAppBrowserSwipeInterface? = null

    constructor(context: Context?) : super(context!!)
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    )

    fun setRefreshInterface(refresh: DAppBrowserSwipeInterface?) {
        refreshInterface = refresh
        alwaysDown = true
        trackMove = 0.0f
        canRefresh = true
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                trackMove = ev.y
                canRefresh = refreshInterface!!.currentScrollPosition == 0 && trackMove < 300
                lastY = trackMove
                alwaysDown = true
            }

            MotionEvent.ACTION_UP -> {
                val flingDistance = ev.y - trackMove
                if (canRefresh && alwaysDown && flingDistance > 500 && ev.eventTime - ev.downTime < 500) //User wants a swipe refresh
                {
                    refreshInterface!!.refreshEvent()
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (ev.y - lastY < 0) alwaysDown = false
                lastY = ev.y
            }

            else -> {}
        }
        return false
    }
}
