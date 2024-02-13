package com.app.plutope.custom_views

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Html
import android.text.SpannableStringBuilder
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import com.app.plutope.R
import com.app.plutope.utils.extras.setSafeOnClickListener


class CustomAlertDialog(context: Context) : Dialog(context) {

    var title: String = ""
        set(value) {
            field = value
            setDialogTitle(value)
        }

    var message = "Message"
        set(value) {
            field = value
            setDialogMessage(value)
        }
    var spannableStringBuilder: SpannableStringBuilder? = null
        set(value) {
            field = value
            setDialogMessage(value!!)
        }
    var positiveButtonText = "Ok"
        set(value) {
            field = value
            setPositiveButton(value)
        }
    var negativeButtonText = ""
        set(value) {
            field = value
            setNegativeButton(value)
        }

    var isCancellable = false


    private var dialog: Dialog? = null
    private var txtTitle: TextView? = null
    private var txtMessage: TextView? = null
    private var customView: FrameLayout? = null
    private var btnPositive: TextView? = null
    private var btnNegative: TextView? = null
    private var view: View? = null

    init {
        if (dialog == null) {
            dialog = Dialog(context, R.style.MyAlertDialog)

        }
        dialog?.setContentView(R.layout.layout_custom_dialog)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        txtTitle = dialog?.findViewById(R.id.txtTitle)
        txtMessage = dialog?.findViewById(R.id.txtMessage)

        btnNegative = dialog?.findViewById(R.id.btnNegative)
        btnPositive = dialog?.findViewById(R.id.btnPositive)
        view = dialog?.findViewById(R.id.view)
        customView = dialog?.findViewById(R.id.view_container)

        setInitData()
    }

    private fun setInitData() {
        title = ""
        message = "Message"
        positiveButtonText = "Ok"
    }

    override fun show() {

        if (negativeButtonText.isNotEmpty()) {
            btnNegative?.visibility = View.VISIBLE
            view?.visibility = View.VISIBLE
        }
        dialog?.setCancelable(isCancellable)

        if (!dialog!!.isShowing) {
            try {
                dialog?.show()
            } catch (e: WindowManager.BadTokenException) {
                e.printStackTrace()
            }
        } else {
            dialog?.dismiss()
        }

    }

    override fun dismiss() {
        if (dialog!!.isShowing) {
            dialog?.dismiss()
        }
    }


    fun setPositiveButton(text: String, onClick: (() -> Unit)? = null) {
        btnPositive?.text = text
        btnPositive?.setSafeOnClickListener {
            if (onClick == null) dialog?.dismiss()
            onClick?.invoke()
        }
    }


    fun setNegativeButton(text: String, onClick: (() -> Unit)? = null) {
        btnNegative?.visibility = View.VISIBLE
        view?.visibility = View.VISIBLE
        btnNegative?.text = text
        btnNegative?.setSafeOnClickListener {
            if (onClick == null) dialog?.dismiss()
            onClick?.invoke()
        }
    }


    private fun setDialogTitle(string: String) {

        if (string.isEmpty())
            txtTitle?.visibility = View.GONE
        else txtTitle?.visibility = View.VISIBLE
        txtTitle?.text = string

    }

    private fun setDialogMessage(string: String) {

        txtMessage?.text = Html.fromHtml(string, Html.FROM_HTML_MODE_LEGACY).toString()
    }

    private fun setDialogMessage(string: SpannableStringBuilder) {
        txtMessage?.setText(string, TextView.BufferType.SPANNABLE)
    }

    fun addView(v: View) {
        txtMessage?.visibility = View.GONE
        customView?.visibility = View.VISIBLE
        customView?.addView(v)
    }
}