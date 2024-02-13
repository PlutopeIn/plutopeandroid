package com.app.plutope.dialogs


import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.WindowManager
import com.app.plutope.R
import com.app.plutope.databinding.DialogPushNotificationBinding
import com.app.plutope.model.PointModel
import com.app.plutope.ui.adapter.PointListAdapter


class PushNotificationDialog private constructor() {
    lateinit var binding: DialogPushNotificationBinding

    companion object {
        var singleInstance: PushNotificationDialog? = null

        fun getInstance(): PushNotificationDialog {
            if (singleInstance == null) {
                singleInstance = PushNotificationDialog()
            }
            return singleInstance!!
        }
    }

    private var dialogPushNotification: Dialog? = null

    fun show(
        context: Context,
        list: MutableList<PointModel> = arrayListOf(),
        listener: DialogOnClickBtnListner
    ) {

        if (dialogPushNotification == null) {
            dialogPushNotification =
                Dialog(context, android.R.style.Theme_DeviceDefault_Dialog_NoActionBar_MinWidth)
        }
        binding = DialogPushNotificationBinding.inflate(LayoutInflater.from(context))
        dialogPushNotification!!.setContentView(binding.root)
        val layoutParams = dialogPushNotification?.window!!.attributes
        dialogPushNotification?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )

        dialogPushNotification?.window?.setBackgroundDrawableResource(R.color.transparent)
        dialogPushNotification?.window!!.attributes = layoutParams
        dialogPushNotification?.setCancelable(false)

        val adapter = PointListAdapter {}
         adapter.submitList(list)
         binding.btnEnable.setOnClickListener {
             listener.onSubmitClicked("")
             dialogPushNotification?.dismiss()
         }

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