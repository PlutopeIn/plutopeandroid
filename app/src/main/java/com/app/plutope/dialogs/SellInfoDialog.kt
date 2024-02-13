package com.app.plutope.dialogs


import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.WindowManager
import com.app.plutope.R
import com.app.plutope.databinding.DialogSellInfoBinding


class SellInfoDialog private constructor() {
    lateinit var binding: DialogSellInfoBinding

    companion object {
        var singleInstance: SellInfoDialog? = null

        fun getInstance(): SellInfoDialog {
            if (singleInstance == null) {
                singleInstance = SellInfoDialog()
            }
            return singleInstance!!
        }
    }

    private var dialogPushNotification: Dialog? = null

    fun show(
        context: Context,
        title: String = "",
        description: String = "",
        listener: DialogOnClickBtnListner
    ) {

        if (dialogPushNotification == null) {
            dialogPushNotification =
                Dialog(context, android.R.style.Theme_DeviceDefault_Dialog_NoActionBar_MinWidth)
        }




        binding = DialogSellInfoBinding.inflate(LayoutInflater.from(context))

        dialogPushNotification!!.setContentView(binding.root)

        val layoutParams = dialogPushNotification?.window!!.attributes
        dialogPushNotification?.window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        dialogPushNotification?.window?.setBackgroundDrawableResource(R.color.transparent)
        dialogPushNotification?.window!!.attributes = layoutParams
        dialogPushNotification?.setCancelable(false)


        binding.txtTitle.text = title
        binding.txtUserInfo.text = description

        binding.imgClose.setOnClickListener {
            dialogPushNotification?.dismiss()
        }

        if (!dialogPushNotification!!.isShowing) {
            try {
                dialogPushNotification?.show()
            } catch (e: WindowManager.BadTokenException) {
                e.printStackTrace()
            }
        } else {
            dialogPushNotification?.dismiss()
        }

        binding.executePendingBindings()
    }

    fun dismiss() {
        if (dialogPushNotification!!.isShowing) {
            dialogPushNotification?.dismiss()
        }
    }


    interface DialogOnClickBtnListner {
        fun onSubmitClicked(selectedList: String)
    }

}