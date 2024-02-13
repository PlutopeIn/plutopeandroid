package com.app.plutope.passcode.animation

import com.app.plutope.passcode.CircleView

internal class FillColorChangeAnimation(
    private val circleView: CircleView
) : ColorChangeAnimation(circleView) {

    override fun getColor(): Int = circleView.getFillCircleColor()

    override fun setColor(color: Int) {
        circleView.setFillCircleColor(color)
    }
}