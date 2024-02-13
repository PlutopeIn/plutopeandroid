package com.app.plutope.passcode.pass2

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.TargetApi
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.CycleInterpolator
import com.app.plutope.R


class PassCodeView : View {
    private val DEBUG = false
    private val KEYS_COUNT = 12
    private val eraseChar = "\u232B"
    private val KEY_PAD_COLS = 3
    private val KEY_PAD_ROWS = 4
    private var digits = 0
    private var filledCount = 0
    private var filledDrawable: Bitmap? = null
    private var emptyDrawable: Bitmap? = null
    private var paint: Paint? = null
    private var digitVerticalPadding = 0
    private var drawableWidth = 0
    private var drawableHeight = 0
    private var drawableStartX = 0
    private var drawableStartY = 0
    private var digitHorizontalPadding = 0
    private var kpStartX = 0
    private var kpStartY = 0
    private val keyRects: ArrayList<KeyRect> = ArrayList<KeyRect>()
    private var keyWidth = 0
    private var keyHeight = 0

    private val shakeAnimDuration: Long = 300
    private var shakeAnimator: ValueAnimator? = null

    /**
     * Get current passcode entered
     *
     * @return - `String` current passcode entered
     */
    var passCodeText = ""
        private set
    private var textChangeListener: TextChangeListener? = null
    private val touchXMap: MutableMap<Int, Int> = HashMap()
    private val touchYMap: MutableMap<Int, Int> = HashMap()
    private var typeFace: Typeface? = null
    private var textPaint: TextPaint? = null
    private var keyTextSize = 0f
    private val animDuration: Long = 200
    private var circlePaint: Paint? = null
    private var dividerVisible = false
    private var dividerStartX = 0f
    private var dividerStartY = 0f
    private var dividerEndX = 0f
    private var dividerEndY = 0f
    private var context: Context? = null

    constructor(context: Context) : super(context) {
        init(context, null, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs, defStyleAttr, 0)
    }

    @TargetApi(21)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs, defStyleAttr, defStyleRes)
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        this.context = context
        val values = context.theme
            .obtainStyledAttributes(attrs, R.styleable.PasswordView, defStyleAttr, defStyleRes)

        try {
            digits = values.getInteger(R.styleable.PasswordView_digits, 4)
            val digitSize = values.getDimension(
                R.styleable.PasswordView_digit_size,
                resources.getDimension(R.dimen.drawableDimen)
            )
            keyTextSize = values.getDimension(
                R.styleable.PasswordView_key_text_size,
                resources.getDimension(R.dimen.key_text_size)
            )
            dividerVisible = values.getBoolean(R.styleable.PasswordView_divider_visible, false)
            digitHorizontalPadding = values.getDimension(
                R.styleable.PasswordView_digit_spacing,
                resources.getDimension(R.dimen.digit_horizontal_padding)
            ).toInt()
            digitVerticalPadding = values.getDimension(
                R.styleable.PasswordView_digit_vertical_padding,
                resources.getDimension(R.dimen.digit_vertical_padding)
            ).toInt()
            drawableWidth = digitSize.toInt() //DEFAULT_DRAWABLE_DIM;
            drawableHeight = digitSize.toInt() //DEFAULT_DRAWABLE_DIM;
            setFilledDrawable(values.getResourceId(R.styleable.PasswordView_filled_drawable, -1))
            setEmptyDrawable(values.getResourceId(R.styleable.PasswordView_empty_drawable, -1))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        values.recycle()
        preparePaint()
    }

    private fun preparePaint() {
        paint = Paint(TextPaint.ANTI_ALIAS_FLAG)
        textPaint = TextPaint(TextPaint.ANTI_ALIAS_FLAG)
        circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        circlePaint!!.style = Paint.Style.FILL
        paint!!.style = Paint.Style.FILL
        textPaint!!.style = Paint.Style.FILL
        textPaint!!.color = Color.argb(255, 0, 0, 0)
        textPaint!!.density = resources.displayMetrics.density
        textPaint!!.textSize = keyTextSize
        textPaint!!.textAlign = Paint.Align.CENTER
    }


    /**
     * Set color for the keypad text
     *
     * @param color - Resource id of the color to be set
     */
    fun setKeyTextColor(color: Int) {
        val colorStateList = ColorStateList.valueOf(color)
        textPaint!!.color = colorStateList.getColorForState(drawableState, 0)
        invalidate()
    }

    /**
     * Set size of keypad text
     *
     * @param size - Text size value to be set
     */
    fun setKeyTextSize(size: Float) {
        textPaint!!.textSize = size
        requestLayout()
        invalidate()
    }

    /**
     * Compute the start point(x, y) to draw drawables showing filled and empty
     * pin code digits.
     */
    private fun computeDrawableStartXY() {
        val totalDrawableWidth = digits * drawableWidth
        val totalPaddingWidth = digitHorizontalPadding * (digits - 1)
        val totalReqWidth = totalDrawableWidth + totalPaddingWidth
        drawableStartX = measuredWidth / 2 - totalReqWidth / 2
        drawableStartY = (drawableHeight + digitVerticalPadding) / 2 - drawableHeight / 2
        // computeKeyboardStartXY()
    }

    /**
     * Compute the start point(x, y) to draw keyboard keys
     */
    private fun computeKeyboardStartXY() {
        kpStartX = 0
        kpStartY = drawableHeight + digitVerticalPadding
        keyWidth = measuredWidth / KEY_PAD_COLS
        keyHeight = (measuredHeight - (drawableHeight + digitVerticalPadding)) / KEY_PAD_ROWS
        initialiseKeyRects()
        if (dividerVisible) {
            computeDividerPos()
        }
    }

    private fun computeDividerPos() {
        val widthFactor = 10f
        dividerStartX = keyWidth / 2 - widthFactor
        dividerStartY = (drawableHeight + digitVerticalPadding).toFloat()
        dividerEndX = measuredWidth - keyWidth / 2 + widthFactor
        dividerEndY = dividerStartY
    }

    /**
     * Initialise a [KeyRect] for each key in keyboard which holds details
     * of key like position, value etc.
     */
    private fun initialiseKeyRects() {
        keyRects.clear()
        var x = kpStartX
        var y = kpStartY
        for (i in 1..KEYS_COUNT) {
            keyRects.add(
                KeyRect(this, Rect(x, y, x + keyWidth, y + keyHeight), i.toString())
            )
            x += keyWidth
            if (i % 3 == 0) {
                y += keyHeight
                x = kpStartX
            }
        }
        keyRects[9].value = ""
        keyRects[10].value = "0"
        keyRects[11].value = eraseChar
    }

    /**
     * Create a [Bitmap] for the given @param resId
     *
     * @param resId - The resource id of the drawable for which the bitmap should be
     * created
     * @return [Bitmap] of the drawable whose resource id is passed in
     */
    private fun getBitmap(resId: Int): Bitmap {
        val drawable = resources.getDrawable(resId)
        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(drawableWidth, drawableHeight, Bitmap.Config.ARGB_8888)
        canvas.setBitmap(bitmap)
        drawable.setBounds(0, 0, drawableWidth, drawableHeight)
        drawable.draw(canvas)
        return bitmap
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawDigitDrawable(canvas)
        if (dividerVisible) {
            drawDivider(canvas)
        }
        drawKeyPad(canvas)
    }

    private fun drawDivider(canvas: Canvas) {
        paint!!.alpha = 40
        canvas.drawLine(dividerStartX, dividerStartY, dividerEndX, dividerEndY, paint!!)
    }

    /**
     * Draw the keys of keypad on the canvas starting from the previously computed start
     * point if keyboard
     *
     * @param canvas - [Canvas] on which the keypad should be drawn
     */
    private fun drawKeyPad(canvas: Canvas) {
        val centerHalf = (textPaint!!.descent() + textPaint!!.ascent()) / 2
        for (rect in keyRects) {
            canvas.drawText(
                rect.value,
                rect.rect.exactCenterX(),
                rect.rect.exactCenterY() - centerHalf,
                textPaint!!
            )
            if (rect.hasRippleEffect) {
                circlePaint!!.alpha = rect.circleAlpha
                canvas.drawCircle(
                    rect.rect.exactCenterX(), rect.rect.exactCenterY(), rect.rippleRadius.toFloat(),
                    circlePaint!!
                )
            }
            if (DEBUG) {
                canvas.drawLine(
                    rect.rect.left.toFloat(),
                    rect.rect.centerY().toFloat(),
                    rect.rect.right.toFloat(),
                    rect.rect.centerY().toFloat(),
                    textPaint!!
                )
                canvas.drawLine(
                    rect.rect.centerX().toFloat(),
                    rect.rect.top.toFloat(),
                    rect.rect.centerX().toFloat(),
                    rect.rect.bottom.toFloat(),
                    textPaint!!
                )
                canvas.drawRect(rect.rect, textPaint!!)
            }
        }
    }

    /**
     * Draw the [Bitmap] of the drawable which indicated filled and empty
     * passcode digits
     *
     * @param canvas - [Canvas] on which the drawable should be drawn
     */
    private fun drawDigitDrawable(canvas: Canvas) {
        paint!!.alpha = 255
        var x = drawableStartX
        val y = drawableStartY
        val totalContentWidth = drawableWidth + digitHorizontalPadding
        for (i in 1..filledCount) {
            canvas.drawBitmap(filledDrawable!!, x.toFloat(), y.toFloat(), paint)
            x += totalContentWidth
        }
        for (i in 1..digits - filledCount) {
            canvas.drawBitmap(emptyDrawable!!, x.toFloat(), y.toFloat(), paint)
            x += totalContentWidth
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val paddingLeft = paddingLeft
        val paddingRight = paddingRight
        val paddingTop = paddingTop
        val paddingBottom = paddingBottom
        var measuredWidth = 0
        var measuredHeight = 0
        if (widthMode == MeasureSpec.EXACTLY || widthMode == MeasureSpec.AT_MOST) {
            measuredWidth = MeasureSpec.getSize(widthMeasureSpec)
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            measuredHeight = MeasureSpec.getSize(heightMeasureSpec)
        } else if (heightMode == MeasureSpec.AT_MOST) {
            val height = MeasureSpec.getSize(heightMeasureSpec) * 0.8
            measuredHeight = height.toInt() + paddingTop + paddingBottom
        }
        measuredHeight =
            Math.max(measuredHeight.toFloat(), resources.getDimension(R.dimen.key_pad_min_height))
                .toInt()
        setMeasuredDimension(measuredWidth, measuredHeight)

        computeDrawableStartXY()
    }

    /**
     * Set the count of digits entered by user
     *
     * @param count - [int] value of the digits filled
     */
    private fun setFilledCount(count: Int) {
        filledCount = if (count > digits) digits else count
        /*invalidate(drawableStartX,
                drawableStartX,
                drawableStartX + getMeasuredWidth(),
                drawableStartY + getMeasuredHeight());*/
        /* The coordinates passed to `invalidate` method above is wrong
            which makes the View not be drawn correctly
        		hence calling the default invalidate method */invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return processTouch(event)
    }

    /**
     * Process the [MotionEvent] and detect the key pressed and perform
     * appropriate action
     *
     * @param event - [MotionEvent] triggered by user action
     * @return {code boolean} whether event is consumed or not
     */
    private fun processTouch(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val pointerDownId = event.getPointerId(event.actionIndex)
                touchXMap[pointerDownId] = event.x.toInt()
                touchYMap[pointerDownId] = event.y.toInt()
            }

            MotionEvent.ACTION_UP -> {
                val pointerUpId = event.getPointerId(event.actionIndex)
                val pointerUpIndex = event.findPointerIndex(pointerUpId)
                val eventX = event.getX(pointerUpIndex).toInt()
                val eventY = event.getY(pointerUpIndex).toInt()
                val touchX = touchXMap[pointerUpId]
                val touchY = touchYMap[pointerUpId]

                if (touchX != null && touchY != null) {
                    findKeyPressed(touchX, touchY, eventX, eventY)
                }
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                val pointerActionDownId = event.getPointerId(event.actionIndex)
                touchXMap[pointerActionDownId] = event.getX(event.actionIndex).toInt()
                touchYMap[pointerActionDownId] = event.getY(event.actionIndex).toInt()
            }

            MotionEvent.ACTION_POINTER_UP -> {
                val pointerActionUpIndex = event.actionIndex
                val pointerActionUpId = event.getPointerId(pointerActionUpIndex)
                val eventPointerX = event.getX(pointerActionUpIndex).toInt()
                val eventPointerY = event.getY(pointerActionUpIndex).toInt()

                val touchX = touchXMap[pointerActionUpId]
                val touchY = touchYMap[pointerActionUpId]

                if (touchX != null && touchY != null) {
                    findKeyPressed(touchX, touchY, eventPointerX, eventPointerY)
                }
            }

            MotionEvent.ACTION_MOVE, MotionEvent.ACTION_CANCEL -> return false
        }
        return true
    }

    /**
     * Find the key which is pressed by find the [Rect] which container the
     * passed in touch event points
     *
     * @param downEventX - X co-ordinate of the pointer down event
     * @param downEventY - Y co-ordinate of the pointer down event
     * @param upEventX - X co-ordinate of the pointer up event
     * @param upEventY - Y co-ordinate of the pointer up event
     */
    private fun findKeyPressed(downEventX: Int, downEventY: Int, upEventX: Int, upEventY: Int) {
        for (keyRect in keyRects) {
            if (keyRect.rect.contains(downEventX, downEventY) && keyRect.rect.contains(
                    upEventX,
                    upEventY
                )
            ) {
                keyRect.playRippleAnim(object : KeyRect.RippleAnimListener {
                    override fun onStart() {
                        val length = passCodeText.length
                        if (keyRect.value == eraseChar) {
                            if (length > 0) {
                                passCodeText = passCodeText.substring(0, passCodeText.length - 1)
                                setFilledCount(passCodeText.length)
                            }
                        } else if (keyRect.value.isNotEmpty() && length < digits) {
                            passCodeText += keyRect.value
                            setFilledCount(passCodeText.length)
                        }
                    }

                    override fun onEnd() {
                        if (keyRect.value.isNotEmpty()) {
                            notifyListener()
                        }
                    }
                })
            }
        }
    }

    private fun setEmptyDrawable(resId: Int) {
        emptyDrawable = getBitmap(resId)
    }

    fun setFilledDrawable(resId: Int) {
        filledDrawable = getBitmap(resId)
    }

    /**
     * Reset the code to empty and redraw the view
     */
    fun reset() {
        passCodeText = ""
        invalidateAndNotifyListener()
    }

    /**
     * Interface to get notified on text change
     */
    interface TextChangeListener {
        fun onTextChanged(text: String?)
    }

    /**
     * Set the filled count of the passcode view and
     * redraw based on the new value and notify the
     * attached listener of the change
     */
    private fun invalidateAndNotifyListener() {
        setFilledCount(passCodeText.length)
        if (textChangeListener != null) {
            textChangeListener!!.onTextChanged(passCodeText)
        }
    }

    private fun notifyListener() {
        if (textChangeListener != null) {
            textChangeListener!!.onTextChanged(passCodeText)
        }
    }

    /**
     * Attach [TextChangeListener] to get notified on text changes
     *
     * @param listener - [TextChangeListener] object to be attached and notified
     */
    fun setOnTextChangeListener(listener: TextChangeListener?) {
        textChangeListener = listener
    }

    /**
     * Remove the attached `TextChangeListener`
     */
    fun removeOnTextChangeListener() {
        textChangeListener = null
    }

    /**
     * Show error feedback to the user
     *
     * @param reset - `boolean` value whether to reset the pass code before showing
     * error feedback
     */
    fun setError(reset: Boolean) {
        if (reset) {
            reset()
        }
        for (keyRect in keyRects) {
            keyRect.setError()
        }
    }
    /**
     * get digit length
     *
     * @return - `int` current length of passcode
     */
    /**
     * Set passcode digit length
     *
     * @param length - `int` digit length to be set
     */
    var digitLength: Int
        get() = digits
        set(length) {
            digits = length
            invalidate()
        }

    /**
     * Set current passcode text
     *
     * @param code - `String` passcode string to be set
     */
    fun setPassCode(code: String) {
        passCodeText = code
        setFilledCount(code.length)
        invalidate()
        notifyListener()
    }

    /*
         fun startShakeAnimation() {
            if (shakeAnimator == null) {
                val startX = translationX
                shakeAnimator = ValueAnimator.ofFloat(startX, startX - 20f, startX + 20f, startX)
                shakeAnimator?.apply {
                    duration = shakeAnimDuration
                    interpolator = CycleInterpolator(1f)
                    addUpdateListener { animation ->
                        val animatedValue = animation.animatedValue as Float
                        translationX = animatedValue
                    }
                    addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                            translationX = 0f // Reset the translation after animation ends
                        }
                    })
                }
            }
            shakeAnimator?.start()
        }
    */


    fun startShakeAnimation() {
        if (shakeAnimator == null) {
            val startX = translationX

            shakeAnimator = ValueAnimator.ofFloat(startX, startX - 20f, startX + 20f, startX)
            shakeAnimator?.apply {
                duration = shakeAnimDuration
                interpolator = CycleInterpolator(1f)
                addUpdateListener { animation ->
                    val animatedValue = animation.animatedValue as Float
                    translationX = animatedValue
                    // Update text color to red during animation
                    val progress =
                        (animation.currentPlayTime.toFloat() / shakeAnimDuration).coerceIn(0f, 1f)
                    val redColor = Color.argb(
                        255,
                        255,
                        (255 * (1 - progress)).toInt(),
                        (255 * (1 - progress)).toInt()
                    )
                    textPaint?.color = redColor

                    invalidate()
                }
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        translationX = 0f
                        setFilledDrawable(R.drawable.ic_asterisk_selected)
                        invalidate()
                    }
                })
            }
        }
        shakeAnimator?.start()
    }

}