package com.app.plutope.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class BlurImageView : AppCompatImageView {

    private val blurRadius = 25f // Adjust this value to control the intensity of the blur

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    override fun onDraw(canvas: Canvas) {
        val image = getBitmapFromImageView()
        if (image != null) {
            val blurredImage = applyBlur(image, blurRadius)
            canvas.drawBitmap(
                blurredImage,
                null,
                RectF(0f, 0f, width.toFloat(), height.toFloat()),
                null
            )
            blurredImage.recycle()
        } else {
            super.onDraw(canvas)
        }
    }

    private fun getBitmapFromImageView(): Bitmap? {
        drawable?.let { drawable ->
            if (drawable.intrinsicWidth > 0 && drawable.intrinsicHeight > 0) {
                val bitmap = Bitmap.createBitmap(
                    drawable.intrinsicWidth,
                    drawable.intrinsicHeight,
                    Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                return bitmap
            }
        }
        return null
    }

    private fun applyBlur(image: Bitmap, blurRadius: Float): Bitmap {
        val blurredBitmap = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)

        val rs = RenderScript.create(context)
        val input = Allocation.createFromBitmap(rs, image)
        val output = Allocation.createFromBitmap(rs, blurredBitmap)

        val script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        script.setInput(input)
        script.setRadius(blurRadius)
        script.forEach(output)

        output.copyTo(blurredBitmap)
        rs.destroy()

        return blurredBitmap
    }
}
