package com.app.plutope.utils.extras

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.ColorDrawable
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.app.plutope.model.Tokens

abstract class SwipeToDeleteCallback internal constructor(
    mContext: Context,
    private val itemList: List<Tokens>
) :
    ItemTouchHelper.Callback() {
    private val mClearPaint: Paint = Paint()
    private val mBackground: ColorDrawable = ColorDrawable()
    var backgroundColor: Int = Color.parseColor("#b80f0a")
    var textPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    var deleteText: String = "Delete"
    val textSize: Float = 40f

    init {
        mClearPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        textPaint.color = Color.WHITE
        textPaint.textSize = textSize
        textPaint.textAlign = Paint.Align.RIGHT
    }


    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        return makeMovementFlags(0, ItemTouchHelper.LEFT)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        viewHolder1: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        val itemView = viewHolder.itemView
        val itemHeight = itemView.height
        val isCancelled = dX == 0f && !isCurrentlyActive


        val position = viewHolder.adapterPosition

        textPaint.color = Color.WHITE
        if (position >= 0 && position < itemList.size) {
            val item = itemList[position]
            mBackground.color =
                if (item.isCustomTokens!!) backgroundColor else Color.parseColor("#44456E")

            deleteText = if (item.isCustomTokens!!) "Delete" else "Disable"

        } else {
            mBackground.color = Color.parseColor("#44456E")
            deleteText = "Disable"
        }

        mBackground.setBounds(
            itemView.right + dX.toInt(),
            itemView.top,
            itemView.right + 30,
            itemView.bottom
        )
        mBackground.draw(c)

        // Draw the deleteText on the canvas (fixed position)
        val textX =
            itemView.right.toFloat() - 20 // Adjust the position of the text from the right end
        val textY =
            itemView.top + itemHeight / 2f + textSize / 2f // Adjust the position of the text
        c.drawText(deleteText, textX, textY, textPaint)

        // If the swipe is canceled, draw the text back
        if (isCancelled) {
            c.drawText(deleteText, textX, textY, mClearPaint)
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }


    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return 0.7f
    }

    abstract override fun onSwiped(viewHolder: RecyclerView.ViewHolder, i: Int)
}