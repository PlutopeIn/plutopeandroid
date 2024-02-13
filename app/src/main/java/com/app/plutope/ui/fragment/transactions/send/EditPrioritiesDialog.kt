package com.app.plutope.ui.fragment.transactions.send

import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doOnTextChanged
import com.app.plutope.R
import com.app.plutope.databinding.DialogSendEditPriorityBinding
import com.app.plutope.ui.fragment.transactions.send.send_coin.SendCoinDetail
import com.app.plutope.ui.fragment.transactions.send.send_coin.TransferNetworkDetail
import com.app.plutope.utils.bigIntegerToString
import com.app.plutope.utils.constant.ENTER_GAS_LIMIT
import com.app.plutope.utils.constant.ENTER_GAS_PRICE
import com.app.plutope.utils.constant.ENTER_GAS_PRICE_EMPTY
import com.app.plutope.utils.constant.ENTER_NONCE
import com.app.plutope.utils.constant.isFullScreenLockDialogOpen
import com.app.plutope.utils.constant.lastSelectedSlippage
import com.app.plutope.utils.convertWeiToEther
import com.app.plutope.utils.extras.PreferenceHelper
import com.app.plutope.utils.gweiToWei
import com.app.plutope.utils.loge
import com.app.plutope.utils.showToast
import com.app.plutope.utils.stringToBigInteger
import com.app.plutope.utils.weiToGwei
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.math.BigInteger
import java.math.RoundingMode


class PreviewSendEditPrioritiesDialog private constructor() {
    private var input: String = ""
    lateinit var binding: DialogSendEditPriorityBinding
    private val shakeDelay: Long = 300
    private val handler = Handler(Looper.getMainLooper())
    private var isVisibleAdvanceOption: Boolean = false

    private var percentageValue: Double = 0.0
    private var baseGasLimit: BigInteger = 0.toBigInteger()


    companion object {
        var singleInstance: PreviewSendEditPrioritiesDialog? = null

        fun getInstance(): PreviewSendEditPrioritiesDialog {
            if (singleInstance == null) {
                singleInstance = PreviewSendEditPrioritiesDialog()
            }
            return singleInstance!!
        }
    }

    private var fullScreenSuccessDialog: Dialog? = null

    fun show(
        context: Context,
        transactionNetworkModel: TransferNetworkDetail?,
        coinDetail: SendCoinDetail,
        listener: DialogOnClickBtnListner
    ) {
        isFullScreenLockDialogOpen = true
        if (fullScreenSuccessDialog == null) {
            fullScreenSuccessDialog = Dialog(context, R.style.full_screen)
        }

        isVisibleAdvanceOption = false

        binding = DialogSendEditPriorityBinding.inflate(LayoutInflater.from(context))

        fullScreenSuccessDialog!!.setContentView(binding.root)

        val layoutParams = fullScreenSuccessDialog?.window!!.attributes
        fullScreenSuccessDialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )

        fullScreenSuccessDialog?.window?.setBackgroundDrawableResource(android.R.color.white)
        fullScreenSuccessDialog?.window!!.attributes = layoutParams
        fullScreenSuccessDialog?.setCancelable(false)



        //  binding.txtMainBalance.text = "-" + coinDetail.amount.toString() + " " + coinDetail.tokenModel.t_symbol

        val convertedPrice = weiToGwei(stringToBigInteger(transactionNetworkModel?.gasPrice!!))
        binding.edtGasPrice.setText(bigIntegerToString(convertedPrice))
        baseGasLimit = transactionNetworkModel.gasLimit
        binding.edtGasLimit.setText(transactionNetworkModel.gasLimit.toString())
        binding.edtNonce.setText(transactionNetworkModel.nonce.toString())


        CoroutineScope(Dispatchers.IO).launch {
            coinDetail.tokenModel.callFunction.getTokenOrCoinNetworkDetailBeforeSend(
                coinDetail.address,
                coinDetail.amount.toDouble(),
                coinDetail.tokenList
            ) { _, model, _ ->
                val chainList =
                    coinDetail.tokenList.filter { it.t_address == "" && it.t_type?.lowercase() == coinDetail.tokenModel.t_type?.lowercase() && it.t_symbol?.lowercase() == coinDetail.tokenModel.chain?.symbol?.lowercase() }
                var chainPrice = coinDetail.tokenModel.t_price?.toDoubleOrNull()
                if (chainList.isNotEmpty()) {
                    chainPrice = chainList[0].t_price?.toDoubleOrNull()
                }

                val gasPrice =
                    if (coinDetail.tokenModel.t_address != "") ((model?.gasAmount?.toDouble()
                        ?: 0.0) * (chainPrice ?: 0.0)) / 1 else ((model?.gasAmount?.toDouble()
                        ?: 0.0) * (coinDetail.tokenModel.t_price?.toDoubleOrNull() ?: 0.0)) / 1
                val networkFee =
                    model?.gasAmount?.toBigDecimal()?.setScale(6, RoundingMode.DOWN).toString()



                val networkFeeForSet =
                    (networkFee + " " + coinDetail.tokenModel.chain?.symbol + "(${
                        PreferenceHelper.getInstance().getSelectedCurrency()?.symbol
                    }${
                        String.format(
                            "%.2f",
                            gasPrice
                        )
                    })")


                loge("EditPriorities", "==>$model")

                val cp = weiToGwei(stringToBigInteger(model?.gasPrice!!))
                binding.edtGasPrice.setText(bigIntegerToString(cp))

                binding.txtMainBalance.text = networkFeeForSet
                binding.progressMainBalance.visibility = View.GONE
                binding.txtMainBalance.visibility = View.VISIBLE

                when (lastSelectedSlippage) {
                    1 -> {
                        firstProgress(model, coinDetail)
                    }

                    3 -> {
                        thirdProgress(model, coinDetail)
                    }

                    else -> {
                        secondProgress(model, coinDetail)
                    }
                }

            }

        }




        binding.txtAdvanceOption.setOnClickListener {
            loge("ClickME", "$isVisibleAdvanceOption")
            if (!isVisibleAdvanceOption) {
                binding.layoutAdvanceOption.visibility = View.VISIBLE
                binding.imgArrow.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        binding.imgArrow.resources,
                        R.drawable.ic_arrow_up,
                        null
                    )
                )

                isVisibleAdvanceOption = true

            } else {
                binding.layoutAdvanceOption.visibility = View.GONE
                binding.imgArrow.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        binding.imgArrow.resources,
                        R.drawable.ic_arrow_down,
                        null
                    )
                )
                isVisibleAdvanceOption = false
            }
        }

        binding.imgBack.setOnClickListener {
            dismiss()
        }

        binding.imgAddGasPrice.setOnClickListener {
            val addGas = binding.edtGasPrice.text.toString().toInt() + 1
            binding.edtGasPrice.setText(addGas.toString())
            binding.edtGasPrice.onHoverChanged(true)
            binding.edtGasPrice.setSelection((binding.edtGasPrice.text.length))

            val gp = stringToBigInteger(binding.edtGasPrice.text.toString().ifEmpty { "0" })
            increasePercentage(
                transactionNetworkModel, coinDetail,
                gweiToWei(gp),
                binding.edtGasLimit.text.toString(),
                binding.edtNonce.text.toString(),
                binding.edtTransactionData.text.toString(),
                percentageValue
            )

        }
        binding.imgMinusGasPrice.setOnClickListener {
            if (binding.edtGasPrice.text.toString().toInt() > 1) {
                val minusGas = binding.edtGasPrice.text.toString().toInt() - 1
                binding.edtGasPrice.setText(minusGas.toString())
                binding.edtGasPrice.setSelection((binding.edtGasPrice.text.length))

                val gp = stringToBigInteger(binding.edtGasPrice.text.toString().ifEmpty { "0" })
                increasePercentage(
                    transactionNetworkModel, coinDetail,
                    gweiToWei(gp),
                    binding.edtGasLimit.text.toString(),
                    binding.edtNonce.text.toString(),
                    binding.edtTransactionData.text.toString(),
                    percentageValue
                )
            }

        }
        binding.imgAddGasLimit.setOnClickListener {
            val addGasLimit = binding.edtGasLimit.text.toString().toInt() + 1000
            binding.edtGasLimit.setText(addGasLimit.toString())
            binding.edtGasLimit.setSelection((binding.edtGasLimit.text.length))

            val gp = stringToBigInteger(binding.edtGasPrice.text.toString().ifEmpty { "0" })
            increasePercentage(
                transactionNetworkModel, coinDetail,
                gweiToWei(gp),
                binding.edtGasLimit.text.toString(),
                binding.edtNonce.text.toString(),
                binding.edtTransactionData.text.toString(),
                percentageValue
            )
        }
        binding.imgMinusGasLimit.setOnClickListener {
            if (binding.edtGasLimit.text.toString().toInt() > 21000) {
                val minusGasLimit = binding.edtGasLimit.text.toString().toInt() - 1000
                binding.edtGasLimit.setText(minusGasLimit.toString())
                binding.edtGasLimit.setSelection((binding.edtGasLimit.text.length))

                val gp = stringToBigInteger(binding.edtGasPrice.text.toString().ifEmpty { "0" })
                increasePercentage(
                    transactionNetworkModel, coinDetail,
                    gweiToWei(gp),
                    binding.edtGasLimit.text.toString(),
                    binding.edtNonce.text.toString(),
                    binding.edtTransactionData.text.toString(),
                    percentageValue
                )
            }
        }


        // secondProgress(transactionNetworkModel, coinDetail)

        binding.firstProgress.setOnClickListener {
            firstProgress(transactionNetworkModel, coinDetail)

            /* binding.progress.setProgress(1, true)
             setFeeProgress(0)
             percentageValue = 0.1
             val gp = stringToBigInteger(binding.edtGasPrice.text.toString())
             increasePercentage(
                 transactionNetworkModel, coinDetail,
                 gweiToWei(gp),
                 baseGasLimit.toString(),
                 binding.edtNonce.text.toString(),
                 binding.edtTransactionData.text.toString(),
                 0.1
             )*/


        }
        binding.secondProgress.setOnClickListener {

            secondProgress(transactionNetworkModel, coinDetail)

            /* binding.progress.setProgress(51, true)
             setFeeProgress(1)
             percentageValue = 0.5
             val gp = stringToBigInteger(binding.edtGasPrice.text.toString())
             increasePercentage(
                 transactionNetworkModel, coinDetail,
                 gweiToWei(gp),
                 baseGasLimit.toString(),
                 binding.edtNonce.text.toString(),
                 binding.edtTransactionData.text.toString(),
                 0.5
             )*/
        }
        binding.thirdProgress.setOnClickListener {

            thirdProgress(transactionNetworkModel, coinDetail)

            /* binding.progress.setProgress(100, true)
             setFeeProgress(2)
             percentageValue = 1.0
             val gp = stringToBigInteger(binding.edtGasPrice.text.toString())
             increasePercentage(
                 transactionNetworkModel, coinDetail,
                 gweiToWei(gp),
                 baseGasLimit.toString(),
                 binding.edtNonce.text.toString(),
                 binding.edtTransactionData.text.toString(),
                 1.0
             )*/

        }

        var tempGasPrice = ""
        binding.edtGasPrice.doOnTextChanged { text, start, _, _ ->
            if (start > 0) {
                tempGasPrice = text.toString()
                val gp = stringToBigInteger(binding.edtGasPrice.text.toString().ifEmpty { "0" })
                increasePercentage(
                    transactionNetworkModel, coinDetail,
                    gweiToWei(gp),
                    binding.edtGasLimit.text.toString(),
                    binding.edtNonce.text.toString(),
                    binding.edtTransactionData.text.toString(),
                    percentageValue
                )
            } else {
                if (binding.edtGasPrice.text.isEmpty()) {
                    binding.edtGasPrice.setText(tempGasPrice)
                    binding.edtGasPrice.setSelection((binding.edtGasPrice.text.length))
                }
            }
        }

        var tempGasLimit = ""
        binding.edtGasLimit.doOnTextChanged { text, start, _, _ ->
            if (start > 0) {
                tempGasLimit = text.toString()
                val gp = stringToBigInteger(binding.edtGasPrice.text.toString().ifEmpty { "0" })
                increasePercentage(
                    transactionNetworkModel, coinDetail,
                    gweiToWei(gp),
                    binding.edtGasLimit.text.toString(),
                    binding.edtNonce.text.toString(),
                    binding.edtTransactionData.text.toString(),
                    percentageValue
                )
            } else {
                if (binding.edtGasLimit.text.isEmpty()) {
                    binding.edtGasLimit.setText(tempGasLimit)
                    binding.edtGasLimit.setSelection(tempGasLimit.length)
                }
            }
        }

        var tempNonce = ""
        binding.edtNonce.doOnTextChanged { text, start, _, _ ->
            if (start > 0) {
                tempNonce = text.toString()
                val gp = stringToBigInteger(binding.edtGasPrice.text.toString().ifEmpty { "0" })
                increasePercentage(
                    transactionNetworkModel, coinDetail,
                    gweiToWei(gp),
                    binding.edtGasLimit.text.toString(),
                    binding.edtNonce.text.toString(),
                    binding.edtTransactionData.text.toString(),
                    percentageValue
                )
            } else {
                if (binding.edtNonce.text.isEmpty()) {
                    binding.edtNonce.setText(tempNonce)
                    binding.edtNonce.setSelection(tempNonce.length)
                }
            }
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

                    val gp = stringToBigInteger(binding.edtGasPrice.text.toString())
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


    private fun firstProgress(
        transactionNetworkModel: TransferNetworkDetail,
        coinDetail: SendCoinDetail
    ) {

        lastSelectedSlippage = 1
        binding.progress.setProgress(1, true)
        setFeeProgress(0)
        percentageValue = 0.1
        val gp = stringToBigInteger(binding.edtGasPrice.text.toString())
        increasePercentage(
            transactionNetworkModel, coinDetail,
            gweiToWei(gp),
            baseGasLimit.toString(),
            binding.edtNonce.text.toString(),
            binding.edtTransactionData.text.toString(),
            0.1
        )
    }

    private fun secondProgress(
        transactionNetworkModel: TransferNetworkDetail,
        coinDetail: SendCoinDetail
    ) {
        lastSelectedSlippage = 2
        binding.progress.setProgress(51, true)
        setFeeProgress(1)
        percentageValue = 0.5
        val gp = stringToBigInteger(binding.edtGasPrice.text.toString())
        increasePercentage(
            transactionNetworkModel, coinDetail,
            gweiToWei(gp),
            baseGasLimit.toString(),
            binding.edtNonce.text.toString(),
            binding.edtTransactionData.text.toString(),
            0.5
        )
    }

    private fun thirdProgress(
        transactionNetworkModel: TransferNetworkDetail,
        coinDetail: SendCoinDetail
    ) {
        lastSelectedSlippage = 3
        binding.progress.setProgress(100, true)
        setFeeProgress(2)
        percentageValue = 1.0
        val gp = stringToBigInteger(binding.edtGasPrice.text.toString())
        increasePercentage(
            transactionNetworkModel, coinDetail,
            gweiToWei(gp),
            baseGasLimit.toString(),
            binding.edtNonce.text.toString(),
            binding.edtTransactionData.text.toString(),
            1.0
        )
    }


    private fun setFeeProgress(progress: Int) {
        when (progress) {
            0 -> {
                binding.firstProgress.setImageResource(R.drawable.img_select_radio)
                binding.secondProgress.setImageResource(R.drawable.img_unselect_radio)
                binding.thirdProgress.setImageResource(R.drawable.img_unselect_radio)
            }

            1 -> {
                binding.firstProgress.setImageResource(R.drawable.img_unselect_radio)
                binding.secondProgress.setImageResource(R.drawable.img_select_radio)
                binding.thirdProgress.setImageResource(R.drawable.img_unselect_radio)
            }

            2 -> {
                binding.firstProgress.setImageResource(R.drawable.img_unselect_radio)
                binding.secondProgress.setImageResource(R.drawable.img_unselect_radio)
                binding.thirdProgress.setImageResource(R.drawable.img_select_radio)
            }
        }

    }

    fun dismiss() {
        if (fullScreenSuccessDialog!!.isShowing) {
            fullScreenSuccessDialog?.dismiss()
        }
    }

    interface DialogOnClickBtnListner {
        fun onSubmitClicked(
            gasPrice: BigInteger,
            gasLimit: String,
            nonce: String,
            transactionData: String
        )
    }

    private fun increasePercentage(
        transactionNetworkModel: TransferNetworkDetail?,
        coinDetail: SendCoinDetail,
        gasPrice: BigInteger,
        gasLimit: String,
        nonce: String,
        transactionData: String,
        percentage: Double
    ) {


        val additional =
            gasLimit.toBigDecimal() + (gasLimit.toBigDecimal() * percentage.toBigDecimal() / 100.toBigDecimal())
        binding.edtGasLimit.setText(additional.setScale(0, RoundingMode.DOWN).toString())
        binding.edtGasLimit.setSelection(binding.edtGasLimit.text.length)

        val fee = gasPrice.toBigDecimal() * additional /*stringToBigInteger(gasLimit)*/
        val gasAmount = convertWeiToEther(
            fee.toString(),
            transactionNetworkModel?.decimal!!
        )

        val chainList =
            coinDetail.tokenList.filter { it.t_address == "" && it.t_type?.lowercase() == coinDetail.tokenModel.t_type?.lowercase() && it.t_symbol?.lowercase() == coinDetail.tokenModel.chain?.symbol?.lowercase() }
        var chainPrice = coinDetail.tokenModel.t_price?.toDoubleOrNull()
        if (chainList.isNotEmpty()) {
            chainPrice = chainList[0].t_price?.toDoubleOrNull()
        }

        val gp =
            if (coinDetail.tokenModel.t_address != "") (gasAmount.toDouble() * (chainPrice
                ?: 0.0)) / 1 else (gasAmount.toDouble() * (coinDetail.tokenModel.t_price?.toDoubleOrNull()
                ?: 0.0)) / 1
        val networkkFee = gasAmount.toBigDecimal().setScale(6, RoundingMode.DOWN).toString()


        val networkFee = (networkkFee + " " + coinDetail.tokenModel.chain?.symbol + "(${
            PreferenceHelper.getInstance().getSelectedCurrency()?.symbol
        }${
            String.format(
                "%.2f",
                gp.toDouble()
            )
        })")

        binding.txtMainBalance.text = networkFee

        /*txtNetworkFeeValue.underlineText(networkFee)


        val maxTotal =
            coinDetail.convertedPrice.replace(Regex("[^0-9.]"), "")
                .toDouble() + gp
        txtMaxTotalValue.text =
            preferenceHelper.getSelectedCurrency()?.symbol + maxTotal.toBigDecimal()
                .setScale(2, RoundingMode.DOWN).toString()*/


        //  }

    }

}