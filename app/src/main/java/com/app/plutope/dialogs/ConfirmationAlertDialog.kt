package com.app.plutope.dialogs


import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.WindowManager
import com.app.plutope.R
import com.app.plutope.databinding.DialogConfirmationAlertBinding


class ConfirmationAlertDialog private constructor() {
    lateinit var binding: DialogConfirmationAlertBinding

    companion object {
        var singleInstance: ConfirmationAlertDialog? = null

        fun getInstance(): ConfirmationAlertDialog {
            if (singleInstance == null) {
                singleInstance = ConfirmationAlertDialog()
            }
            return singleInstance!!
        }
    }

    private var dialogConfirmationAlert: Dialog? = null

    fun show(
        context: Context,
        title:String,
        subtitle:String,
        isFromRecovery:Boolean=false,
        listener: DialogOnClickBtnListner
    ) {

        if (dialogConfirmationAlert == null) {
            dialogConfirmationAlert =
                Dialog(context, android.R.style.Theme_DeviceDefault_Dialog_NoActionBar_MinWidth)
        }


        binding = DialogConfirmationAlertBinding.inflate(LayoutInflater.from(context))

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

        if(isFromRecovery){
            binding.btnCancel.visibility=GONE
            binding.btnDelete.text="Start Now"
            binding.imgWarning.visibility= VISIBLE
        }
        binding.btnCancel.setOnClickListener {
            dialogConfirmationAlert?.dismiss()
        }


        binding.btnDelete.setOnClickListener {

            listener.onDeleteClick()
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
        fun onDeleteClick()
    }

}