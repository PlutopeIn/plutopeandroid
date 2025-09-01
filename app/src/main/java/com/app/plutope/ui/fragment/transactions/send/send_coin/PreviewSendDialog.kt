package com.app.plutope.ui.fragment.transactions.send.send_coin

import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.WindowManager
import com.app.plutope.R
import com.app.plutope.databinding.DialogSendAdvancedPriviewBinding
import com.app.plutope.utils.constant.ENTER_GAS_LIMIT
import com.app.plutope.utils.constant.ENTER_GAS_PRICE
import com.app.plutope.utils.constant.ENTER_GAS_PRICE_EMPTY
import com.app.plutope.utils.constant.ENTER_NONCE
import com.app.plutope.utils.constant.isFullScreenLockDialogOpen
import com.app.plutope.utils.gweiToWei
import com.app.plutope.utils.showToast
import com.app.plutope.utils.stringToBigDecimal
import com.app.plutope.utils.weiToGwei
import java.math.BigDecimal


class PreviewSendAdvanceDialog private constructor() {

    private var input: String = ""
    lateinit var binding: DialogSendAdvancedPriviewBinding
    private val shakeDelay: Long = 300
    private val handler = Handler(Looper.getMainLooper())

    companion object {
        var singleInstance: PreviewSendAdvanceDialog? = null

        fun getInstance(): PreviewSendAdvanceDialog {
            if (singleInstance == null) {
                singleInstance = PreviewSendAdvanceDialog()
            }
            return singleInstance!!
        }
    }

    private var fullScreenSuccessDialog: Dialog? = null

    fun show(
        context: Context,
        transactionNetworkModel: TransferNetworkDetail?,
        listener: DialogOnClickBtnListner
    ) {
        isFullScreenLockDialogOpen = true
        if (fullScreenSuccessDialog == null) {
            fullScreenSuccessDialog = Dialog(context, R.style.full_screen)
        }


        binding = DialogSendAdvancedPriviewBinding.inflate(LayoutInflater.from(context))

        fullScreenSuccessDialog!!.setContentView(binding.root)

        val layoutParams = fullScreenSuccessDialog?.window!!.attributes
        fullScreenSuccessDialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )

        fullScreenSuccessDialog?.window?.setBackgroundDrawableResource(android.R.color.white)
        fullScreenSuccessDialog?.window!!.attributes = layoutParams
        fullScreenSuccessDialog?.setCancelable(false)

        val convertedPrice = weiToGwei(stringToBigDecimal(transactionNetworkModel?.gasPrice!!))

        binding.edtGasPrice.setText(convertedPrice.toPlainString())
        binding.edtGasLimit.setText(transactionNetworkModel.gasLimit.toString())
        binding.edtNonce.setText(transactionNetworkModel.nonce.toString())

        binding.imgBack.setOnClickListener {
            dismiss()
        }

        binding.btnSave.setOnClickListener {


            when {

                binding.edtGasPrice.text?.toString()!!.isEmpty() -> {
                    binding.constRoot.context.showToast(ENTER_GAS_PRICE_EMPTY)
                }

                binding.edtGasPrice.text?.toString()!!.toDouble() <= 0.0 -> {
                    binding.constRoot.context.showToast(ENTER_GAS_PRICE)
                }

                binding.edtGasLimit.text?.isEmpty() == true -> {
                    binding.constRoot.context.showToast(ENTER_GAS_LIMIT)
                }

                binding.edtNonce.text?.isEmpty() == true -> {
                    binding.constRoot.context.showToast(ENTER_NONCE)
                }

                else -> {

                    val gp = stringToBigDecimal(binding.edtGasPrice.text.toString())
                    gweiToWei(gp)


                    listener.onSubmitClicked(
                        gweiToWei(gp),
                        binding.edtGasLimit.text.toString(),
                        binding.edtNonce.text.toString(),
                        binding.edtTransactionData.text.toString()
                    )

                    dismiss()

                }
            }


        }


        if (!fullScreenSuccessDialog!!.isShowing) {
            try {
                fullScreenSuccessDialog?.show()
            } catch (e: WindowManager.BadTokenException) {
                e.printStackTrace()
            }
        } else {
            // fullScreenSuccessDialog?.dismiss()
        }

        binding.executePendingBindings()
    }

    fun dismiss() {
        if (fullScreenSuccessDialog!!.isShowing) {
            fullScreenSuccessDialog?.dismiss()
        }
    }

    interface DialogOnClickBtnListner {
        fun onSubmitClicked(
            gasPrice: BigDecimal,
            gasLimit: String,
            nonce: String,
            transactionData: String
        )
    }

}