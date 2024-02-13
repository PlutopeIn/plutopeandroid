package com.app.plutope.ui.fragment.transactions.swap


import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.WindowManager
import com.app.plutope.databinding.DialogSwapProviderListBinding
import com.app.plutope.model.Tokens
import com.app.plutope.ui.fragment.providers.ProviderModel


class DialogSwapProviderList private constructor() {
    lateinit var binding: DialogSwapProviderListBinding

    companion object {
        var singleInstance: DialogSwapProviderList? = null

        fun getInstance(): DialogSwapProviderList {
            if (singleInstance == null) {
                singleInstance = DialogSwapProviderList()
            }
            return singleInstance!!
        }
    }

    private var dialogPushNotification: Dialog? = null

    fun show(
        context: Context,
        list: MutableList<ProviderModel> = arrayListOf(),
        youPayObj: Tokens,
        youGetObj: Tokens,
        listener: DialogOnClickBtnListner
    ) {

        if (dialogPushNotification == null) {
            dialogPushNotification =
                Dialog(context, android.R.style.Theme_DeviceDefault_Dialog_NoActionBar_MinWidth)
        }


        binding = DialogSwapProviderListBinding.inflate(LayoutInflater.from(context))
        dialogPushNotification!!.setContentView(binding.root)

        val layoutParams = dialogPushNotification?.window!!.attributes
        dialogPushNotification?.window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        dialogPushNotification?.window?.setBackgroundDrawableResource(com.app.plutope.R.color.transparent)
        dialogPushNotification?.window!!.attributes = layoutParams
        dialogPushNotification?.setCancelable(false)

        val adapter = SwapProviderListAdapter {
            listener.onSubmitClicked(it)
            dismiss()
        }

        binding.txtReceiveAmount.text = "${youGetObj.t_symbol} \nReceiving"
        binding.rvProviderList.adapter = adapter


        val maxBestPrice = list.maxByOrNull { it.bestPrice.toDouble() }?.bestPrice
        list.forEach { provider ->
            provider.isBestPrise = (provider.bestPrice == maxBestPrice)

            provider.bestPrice = "%.6f".format((provider.bestPrice.toDoubleOrNull() ?: 0.0))
            val maxFormattedPrice =
                list.map { it.bestPrice.toDoubleOrNull() ?: 0.0 }.maxOrNull() ?: 0.0
            val percentageDifference = if (maxFormattedPrice != 0.0) {
                ((provider.bestPrice.toDoubleOrNull() ?: 0.0) / maxFormattedPrice - 1.0) * 100
            } else {
                0.0
            }
            provider.percentageBestPrice = "%.2f%%".format(percentageDifference)

        }


        adapter.submitList(list.filter { providerModel -> providerModel.bestPrice.toDouble() > 0.0 }
            .distinctBy { it.providerName }.sortedByDescending { it.bestPrice.toDouble() })

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
        fun onSubmitClicked(model: ProviderModel)
    }

}