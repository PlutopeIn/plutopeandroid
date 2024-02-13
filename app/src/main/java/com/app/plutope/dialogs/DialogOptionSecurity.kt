package com.app.plutope.dialogs


import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.WindowManager
import com.app.plutope.R
import com.app.plutope.databinding.DialogCommonModelListBinding
import com.app.plutope.model.CommonOptionModel
import com.app.plutope.ui.adapter.CommonOptionAdapter

class DialogOptionSecurity private constructor() {
    lateinit var binding: DialogCommonModelListBinding

    companion object {
        var singleInstance: DialogOptionSecurity? = null

        fun getInstance(): DialogOptionSecurity {
            if (singleInstance == null) {
                singleInstance = DialogOptionSecurity()
            }
            return singleInstance!!
        }
    }

    private var securityDialog: Dialog? = null

    fun show(
        context: Context,
        list: MutableList<CommonOptionModel>,
        split: Int,
        listener: DialogOnClickBtnListner
    ) {

        if (securityDialog == null) {
            securityDialog = Dialog(context, R.style.full_screen)
        }



        binding = DialogCommonModelListBinding.inflate(LayoutInflater.from(context))

        securityDialog!!.setContentView(binding.root)

        val layoutParams = securityDialog?.window!!.attributes
        securityDialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )

        securityDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        securityDialog?.window!!.attributes = layoutParams
        securityDialog?.setCancelable(false)



        binding.rvOptionList.adapter = CommonOptionAdapter(list) {
            listener.onSelectedItemClicked(it.name)
            securityDialog?.dismiss()
        }

        binding.imgBack.setOnClickListener {
            securityDialog?.dismiss()
        }

        if (!securityDialog!!.isShowing) {
            try {
                securityDialog?.show()
            } catch (e: WindowManager.BadTokenException) {
                e.printStackTrace()
            }
        } else {
            securityDialog?.dismiss()
        }

        binding.executePendingBindings()
    }

    fun dismiss() {
        if (securityDialog!!.isShowing) {
            securityDialog?.dismiss()
        }
    }


    interface DialogOnClickBtnListner {
        fun onSelectedItemClicked(selected: String)
    }

}