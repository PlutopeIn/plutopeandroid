package com.app.plutope.dialogs


import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.WindowManager
import com.app.plutope.R
import com.app.plutope.databinding.DialogConfirmationAlertBinding
import com.app.plutope.databinding.DialogSwapProgressBinding


class SwapProgressDialog private constructor() {
    lateinit var binding: DialogSwapProgressBinding

    companion object {
        var singleInstance: SwapProgressDialog? = null

        fun getInstance(): SwapProgressDialog {
            if (singleInstance == null) {
                singleInstance = SwapProgressDialog()
            }
            return singleInstance!!
        }
    }

    private var dialogConfirmationAlert: Dialog? = null

    fun show(
        context: Context,
        title:String,
        subtitle:String,
        listener: DialogOnClickBtnListner
    ) {

        if (dialogConfirmationAlert == null) {
            dialogConfirmationAlert =
                Dialog(context, android.R.style.Theme_DeviceDefault_Dialog_NoActionBar_MinWidth)
        }


        binding = DialogSwapProgressBinding.inflate(LayoutInflater.from(context))

        dialogConfirmationAlert!!.setContentView(binding.root)

        val layoutParams = dialogConfirmationAlert?.window!!.attributes
        dialogConfirmationAlert?.window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        dialogConfirmationAlert?.window?.setBackgroundDrawableResource(R.color.transparent)
        dialogConfirmationAlert?.window!!.attributes = layoutParams
        dialogConfirmationAlert?.setCancelable(false)

        binding.txtTitle.text = title
        binding.txtSubTitle.text = subtitle


        binding.btnOk.setOnClickListener {
            listener.onOkClick()
            dialogConfirmationAlert?.dismiss()
        }
        binding.imgCancel.setOnClickListener {
            dialogConfirmationAlert?.dismiss()
        }

        if (!dialogConfirmationAlert!!.isShowing) {
            try {
                dialogConfirmationAlert?.show()
            } catch (e: WindowManager.BadTokenException) {
                e.printStackTrace()
            }
        } else {
            dialogConfirmationAlert?.dismiss()
        }

        binding.executePendingBindings()
    }

    fun dismiss() {
        if (dialogConfirmationAlert!!.isShowing) {
            dialogConfirmationAlert?.dismiss()
        }
    }


    interface DialogOnClickBtnListner {
        fun onOkClick()
    }

}