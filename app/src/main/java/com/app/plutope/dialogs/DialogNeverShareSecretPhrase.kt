package com.app.plutope.dialogs


import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.WindowManager
import com.app.plutope.databinding.DialogNeverShareSecretPhraseBinding
import com.app.plutope.model.PointModel
import com.app.plutope.ui.adapter.PointListAdapter


class DialogNeverShareSecretPhrase private constructor() {
    lateinit var binding: DialogNeverShareSecretPhraseBinding

    companion object {
        var singleInstance: DialogNeverShareSecretPhrase? = null

        fun getInstance(): DialogNeverShareSecretPhrase {
            if (singleInstance == null) {
                singleInstance = DialogNeverShareSecretPhrase()
            }
            return singleInstance!!
        }
    }

    private var dialogNeverShareSecretPhrase: Dialog? = null

    fun show(
        context: Context,
        list: MutableList<PointModel> = arrayListOf(),
        listener: DialogOnClickBtnListner
    ) {

        if (dialogNeverShareSecretPhrase == null) {
            dialogNeverShareSecretPhrase =
                Dialog(context, android.R.style.Theme_DeviceDefault_Dialog_NoActionBar_MinWidth)
        }


        binding = DialogNeverShareSecretPhraseBinding.inflate(LayoutInflater.from(context))

        dialogNeverShareSecretPhrase!!.setContentView(binding.root)

        val layoutParams = dialogNeverShareSecretPhrase?.window!!.attributes
        dialogNeverShareSecretPhrase?.window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        dialogNeverShareSecretPhrase?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialogNeverShareSecretPhrase?.window!!.attributes = layoutParams
        dialogNeverShareSecretPhrase?.setCancelable(false)

        val adapter = PointListAdapter {}
        adapter.submitList(list)
        binding.rvPointList.adapter = adapter




        binding.btnContinue.setOnClickListener {
            listener.onSubmitClicked("")
            dialogNeverShareSecretPhrase?.dismiss()
        }

        if (!dialogNeverShareSecretPhrase!!.isShowing) {
            try {
                dialogNeverShareSecretPhrase?.show()
            } catch (e: WindowManager.BadTokenException) {
                e.printStackTrace()
            }
        } else {
            dialogNeverShareSecretPhrase?.dismiss()
        }

        binding.executePendingBindings()
    }

    fun dismiss() {
        if (dialogNeverShareSecretPhrase!!.isShowing) {
            dialogNeverShareSecretPhrase?.dismiss()
        }
    }


    interface DialogOnClickBtnListner {
        fun onSubmitClicked(selectedList: String)
    }

}