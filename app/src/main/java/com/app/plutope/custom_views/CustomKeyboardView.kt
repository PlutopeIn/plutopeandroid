package com.app.plutope.custom_views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.GridLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.app.plutope.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class CustomKeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : GridLayout(context, attrs, defStyleAttr) {

    private var customKeyboardView: View =
        LayoutInflater.from(context).inflate(R.layout.custom_keybord_view, this, true)

    private var text0: TextView = customKeyboardView.findViewById(R.id.text_0) as TextView
    private var text1: TextView = customKeyboardView.findViewById(R.id.text_1) as TextView
    private var text2: TextView = customKeyboardView.findViewById(R.id.text_2) as TextView
    private var text3: TextView = customKeyboardView.findViewById(R.id.text_3) as TextView
    private var text4: TextView = customKeyboardView.findViewById(R.id.text_4) as TextView
    private var text5: TextView = customKeyboardView.findViewById(R.id.text_5) as TextView
    private var text6: TextView = customKeyboardView.findViewById(R.id.text_6) as TextView
    private var text7: TextView = customKeyboardView.findViewById(R.id.text_7) as TextView
    private var text8: TextView = customKeyboardView.findViewById(R.id.text_8) as TextView
    private var text9: TextView = customKeyboardView.findViewById(R.id.text_9) as TextView
    private var textClose: ConstraintLayout =
        customKeyboardView.findViewById(R.id.text_d) as ConstraintLayout
    private var textCancel: TextView = customKeyboardView.findViewById(R.id.text_cencel) as TextView


    var listner: NotifyKeyListener? = null

    init {

        text0.setOnClickListener {
            /* CoroutineScope(Dispatchers.Main).launch {
                 listner?.getValue(text0.text.toString())
             }*/

            handleButtonClick(text0.text.toString())
        }

        text1.setOnClickListener {
            handleButtonClick(text1.text.toString())
        }

        text2.setOnClickListener {
            handleButtonClick(text2.text.toString())
        }
        text3.setOnClickListener {
            handleButtonClick(text3.text.toString())
        }

        text4.setOnClickListener {
            handleButtonClick(text4.text.toString())
        }
        text5.setOnClickListener {
            handleButtonClick(text5.text.toString())
        }
        text6.setOnClickListener {
            handleButtonClick(text6.text.toString())
        }
        text7.setOnClickListener {
            handleButtonClick(text7.text.toString())
        }
        text8.setOnClickListener {
            handleButtonClick(text8.text.toString())
        }
        text9.setOnClickListener {
            handleButtonClick(text9.text.toString())
        }
        textClose.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                listner?.removeValue()
            }
        }
        textCancel.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                listner?.removeAllValue()
            }
        }

    }

    private fun handleButtonClick(value: String) {
        CoroutineScope(Dispatchers.Main).launch {

            listner?.getValue(value)
            // Notify the listener on the main thread

        }
    }


    interface NotifyKeyListener {
        fun getValue(value: String)
        fun removeValue()
        fun removeAllValue()
    }
}