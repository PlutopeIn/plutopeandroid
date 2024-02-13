package com.app.plutope.dialogs


import android.app.Dialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.WindowManager
import com.app.plutope.R
import com.app.plutope.databinding.DialogAddContactBinding
import com.app.plutope.model.PointModel
import com.app.plutope.ui.adapter.PointListAdapter
import com.app.plutope.utils.enableDisableButton


class AddContactDialog private constructor() {
    lateinit var binding: DialogAddContactBinding

    companion object {
        var singleInstance: AddContactDialog? = null

        fun getInstance(): AddContactDialog {
            if (singleInstance == null) {
                singleInstance = AddContactDialog()
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


        binding = DialogAddContactBinding.inflate(LayoutInflater.from(context))

        dialogPushNotification!!.setContentView(binding.root)

        val layoutParams = dialogPushNotification?.window!!.attributes
        dialogPushNotification?.window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        dialogPushNotification?.window?.setBackgroundDrawableResource(R.color.transparent)
        dialogPushNotification?.window!!.attributes = layoutParams
        dialogPushNotification?.setCancelable(false)

        val adapter = PointListAdapter {}
        adapter.submitList(list)
        binding.btnCancel.setOnClickListener {
            dialogPushNotification?.dismiss()
        }
        binding.btnSave.enableDisableButton(false)
        binding.edtAliasName.addTextChangedListener(object:TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(s?.toString()?.isNotEmpty() == true){
                    binding.btnSave.enableDisableButton(true)
                }else{
                    binding.btnSave.enableDisableButton(false)
                }
            }

            override fun afterTextChanged(s: Editable?) {}

        })
        binding.btnSave.setOnClickListener {

            listener.onSubmitClicked(binding.edtAliasName.text.toString())
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