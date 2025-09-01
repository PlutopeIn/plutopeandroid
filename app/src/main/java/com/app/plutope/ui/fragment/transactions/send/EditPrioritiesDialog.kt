package com.app.plutope.ui.fragment.transactions.send

import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import com.app.plutope.R
import com.app.plutope.databinding.DialogSendEditPriorityBinding
import com.app.plutope.ui.base.BaseDialogFragment
import com.app.plutope.ui.fragment.transactions.send.send_coin.SendCoinDetail
import com.app.plutope.ui.fragment.transactions.send.send_coin.SendCoinViewModel
import com.app.plutope.ui.fragment.transactions.send.send_coin.TransferNetworkDetail
import com.app.plutope.utils.constant.ENTER_GAS_LIMIT
import com.app.plutope.utils.constant.ENTER_GAS_PRICE
import com.app.plutope.utils.constant.ENTER_GAS_PRICE_EMPTY
import com.app.plutope.utils.constant.ENTER_NONCE
import com.app.plutope.utils.constant.lastSelectedSlippage
import com.app.plutope.utils.convertWeiToEther
import com.app.plutope.utils.extras.PreferenceHelper
import com.app.plutope.utils.gweiToWei
import com.app.plutope.utils.loge
import com.app.plutope.utils.showToast
import com.app.plutope.utils.stringToBigDecimal
import com.app.plutope.utils.weiToGwei
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

@AndroidEntryPoint
class PreviewSendEditPrioritiesDialog :
    BaseDialogFragment<DialogSendEditPriorityBinding, SendCoinViewModel>() {
    private var input: String = ""
    private val _sendCoinViewModel: SendCoinViewModel by viewModels()
    var _binding: DialogSendEditPriorityBinding? = null
    private val shakeDelay: Long = 300
    private val handler = Handler(Looper.getMainLooper())
    private var isVisibleAdvanceOption: Boolean = false

    private var percentageValue: Double = 0.0
    private var baseGasLimit: BigInteger = 0.toBigInteger()

    var listener: DialogOnClickBtnListner? = null

    companion object {
        /* var singleInstance: PreviewSendEditPrioritiesDialog? = null

         fun getInstance(): PreviewSendEditPrioritiesDialog {
             if (singleInstance == null) {
                 singleInstance = PreviewSendEditPrioritiesDialog()
             }
             return singleInstance!!
         }*/

        fun newInstance(
            transactionNetworkModel: TransferNetworkDetail?,
            coinDetail: SendCoinDetail,
            listener: DialogOnClickBtnListner
        ): PreviewSendEditPrioritiesDialog {
            val dialog = PreviewSendEditPrioritiesDialog()
            dialog.arguments = Bundle().apply {
                putParcelable("transactionNetworkModel", transactionNetworkModel)
                putParcelable("coinDetail", coinDetail)
            }
            dialog.listener = listener
            return dialog
        }

    }

    override fun getLayoutID(): Int {
        return R.layout.dialog_send_edit_priority
    }

    override fun setBindingVariables() {

    }

    override fun onStart() {
        super.onStart()
        dialog!!.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        dialog?.setCancelable(false)
    }

    override fun getBindedViewModel(): SendCoinViewModel {
        return _sendCoinViewModel
    }

    override fun setupObservers() {

    }

    override fun setUpUI() {
        val transactionModel: TransferNetworkDetail =
            arguments?.getParcelable("transactionNetworkModel") ?: return
        val coinDetail: SendCoinDetail = arguments?.getParcelable("coinDetail") ?: return
        setViewUI(coinDetail = coinDetail, transactionNetworkModel = transactionModel)
    }

    override fun showToolbar(): Boolean {
        return false
    }

    override fun setDataBinding(vb: DialogSendEditPriorityBinding) {
        _binding = vb
    }


    // private var fullScreenSuccessDialog: Dialog? = null

    private fun setViewUI(
        transactionNetworkModel: TransferNetworkDetail?, coinDetail: SendCoinDetail

    ) {

        _binding?.progress?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                _binding?.txtProgressive?.text = "$progress%"
                val bounds: Rect = _binding?.progress?.thumb?.bounds!!
                _binding?.txtProgressive?.x =
                    (_binding?.progress?.left!! + bounds.left + 15).toFloat()
                lastSelectedSlippage = progress
                percentageValue = lastSelectedSlippage.toDouble()
                val gp = stringToBigDecimal(_binding?.edtGasPrice?.text.toString())
                increasePercentage(
                    transactionNetworkModel,
                    coinDetail,
                    gweiToWei(gp),
                    baseGasLimit.toString(),
                    _binding?.edtNonce?.text.toString(),
                    _binding?.edtTransactionData?.text.toString(),
                    percentageValue, fromButton = "progress"
                )

            }


            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })


        //  _binding?.txtMainBalance.text = "-" + coinDetail.amount.toString() + " " + coinDetail.tokenModel.t_symbol

        val convertedPrice = weiToGwei(stringToBigDecimal(transactionNetworkModel?.gasPrice!!))


        loge(
            "TRAANSDATA",
            "${transactionNetworkModel.gasPrice} :: ${stringToBigDecimal(transactionNetworkModel.gasPrice)} :: ${convertedPrice} "
        )
        _binding?.edtGasPrice?.setText(convertedPrice.toPlainString())
        baseGasLimit = transactionNetworkModel.gasLimit
        _binding?.edtGasLimit?.setText(transactionNetworkModel.gasLimit.toString())
        _binding?.edtNonce?.setText(transactionNetworkModel.nonce.toString())


        CoroutineScope(Dispatchers.IO).launch {
            coinDetail.tokenModel.callFunction.getTokenOrCoinNetworkDetailBeforeSend(
                coinDetail.address, coinDetail.amount.toDouble(), coinDetail.tokenList
            ) { _, model, _ ->
                val chainList =
                    coinDetail.tokenList.filter { it.t_address == "" && it.t_type.lowercase() == coinDetail.tokenModel.t_type.lowercase() /*&& it.t_symbol?.lowercase() == coinDetail.tokenModel.chain?.symbol?.lowercase()*/ }
                var chainPrice = coinDetail.tokenModel.t_price.toDoubleOrNull()
                if (chainList.isNotEmpty()) {
                    chainPrice = chainList[0].t_price.toDoubleOrNull()
                }

                val gasPrice =
                    if (coinDetail.tokenModel.t_address != "") ((model?.gasAmount?.toDouble()
                        ?: 0.0) * (chainPrice ?: 0.0)) / 1 else ((model?.gasAmount?.toDouble()
                        ?: 0.0) * (coinDetail.tokenModel.t_price.toDoubleOrNull() ?: 0.0)) / 1
                val networkFee =
                    model?.gasAmount?.toBigDecimal()?.setScale(6, RoundingMode.DOWN).toString()


                val networkFeeForSet =
                    (networkFee + " " + chainList[0].t_symbol + "(${
                        PreferenceHelper.getInstance().getSelectedCurrency()?.symbol
                    }${
                        String.format(
                            "%.2f", gasPrice
                        )
                    })")


                loge("EditPriorities", "==>$model")

                val cp = weiToGwei(stringToBigDecimal(model?.gasPrice ?: "0.0"))


                requireActivity().runOnUiThread {
                    _binding?.edtGasPrice?.setText(/*bigIntegerToString(cp)*/cp.toPlainString())
                    _binding?.txtMainBalance?.text = networkFeeForSet
                    _binding?.progressMainBalance?.visibility = View.GONE
                    _binding?.txtMainBalance?.visibility = View.VISIBLE

                    _binding?.progress?.progress = lastSelectedSlippage
                    percentageValue = lastSelectedSlippage.toDouble()
                    val gp = stringToBigDecimal(_binding?.edtGasPrice?.text.toString())
                    increasePercentage(
                        transactionNetworkModel,
                        coinDetail,
                        gweiToWei(gp),
                        baseGasLimit.toString(),
                        _binding?.edtNonce?.text.toString(),
                        _binding?.edtTransactionData?.text.toString(),
                        percentageValue
                    )
                }


            }

        }

        _binding?.txtAdvanceOption?.setOnClickListener {
            loge("ClickME", "$isVisibleAdvanceOption")
            if (!isVisibleAdvanceOption) {
                _binding?.layoutAdvanceOption?.visibility = View.VISIBLE
                _binding?.imgArrow?.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        _binding?.imgArrow!!.resources, R.drawable.ic_arrow_up, null
                    )
                )

                isVisibleAdvanceOption = true

            } else {
                _binding?.layoutAdvanceOption?.visibility = View.GONE
                _binding?.imgArrow?.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        _binding?.imgArrow!!.resources, R.drawable.ic_arrow_down_with_bg, null
                    )
                )
                isVisibleAdvanceOption = false
            }
        }

        _binding?.imgBack?.setOnClickListener {
            dismiss()
        }

        _binding?.imgAddGasPrice?.setOnClickListener {
            val addGas = _binding?.edtGasPrice?.text.toString().toBigDecimal() + 1.toBigDecimal()
            _binding?.edtGasPrice?.setText(addGas.toString())
            _binding?.edtGasPrice?.onHoverChanged(true)
            _binding?.edtGasPrice?.setSelection((_binding?.edtGasPrice?.text?.length!!))

            val gp = stringToBigDecimal(_binding?.edtGasPrice?.text.toString().ifEmpty { "0" })
            increasePercentage(
                transactionNetworkModel,
                coinDetail,
                gweiToWei(gp),
                _binding?.edtGasLimit?.text.toString(),
                _binding?.edtNonce?.text.toString(),
                _binding?.edtTransactionData?.text.toString(),
                percentageValue
            )

        }
        _binding?.imgMinusGasPrice?.setOnClickListener {
            if (_binding?.edtGasPrice?.text.toString().toBigDecimal() > 1.toBigDecimal()) {
                val minusGas =
                    _binding?.edtGasPrice?.text.toString().toBigDecimal() - 1.toBigDecimal()
                _binding?.edtGasPrice?.setText(minusGas.toString())
                _binding?.edtGasPrice?.setSelection((_binding?.edtGasPrice?.text?.length!!))

                val gp = stringToBigDecimal(_binding?.edtGasPrice?.text.toString().ifEmpty { "0" })
                increasePercentage(
                    transactionNetworkModel,
                    coinDetail,
                    gweiToWei(gp),
                    _binding?.edtGasLimit?.text.toString(),
                    _binding?.edtNonce?.text.toString(),
                    _binding?.edtTransactionData?.text.toString(),
                    percentageValue
                )
            }

        }
        _binding?.imgAddGasLimit?.setOnClickListener {
            val addGasLimit =
                _binding?.edtGasLimit?.text.toString().toBigInteger() + 1000.toBigInteger()
            _binding?.edtGasLimit?.setText(addGasLimit.toString())
            _binding?.edtGasLimit?.setSelection((_binding?.edtGasLimit?.text?.length!!))

            percentageValue += 1
            _binding?.progress?.progress = percentageValue.toInt()

            val gp = stringToBigDecimal(_binding?.edtGasPrice?.text.toString().ifEmpty { "0" })
            increasePercentage(
                transactionNetworkModel,
                coinDetail,
                gweiToWei(gp),
                _binding?.edtGasLimit?.text.toString(),
                _binding?.edtNonce?.text.toString(),
                _binding?.edtTransactionData?.text.toString(),
                percentageValue
            )
        }
        _binding?.imgMinusGasLimit?.setOnClickListener {
            if (_binding?.edtGasLimit?.text.toString().toBigInteger() > 21000.toBigInteger()) {
                val minusGasLimit =
                    _binding?.edtGasLimit?.text.toString().toBigInteger() - 1000.toBigInteger()
                _binding?.edtGasLimit?.setText(minusGasLimit.toString())
                _binding?.edtGasLimit?.setSelection((_binding?.edtGasLimit?.text?.length!!))

                loge("minusGasLimit", "$minusGasLimit")

                percentageValue -= 1
                _binding?.progress?.progress = percentageValue.toInt()


                val gp = stringToBigDecimal(_binding?.edtGasPrice?.text.toString().ifEmpty { "0" })
                increasePercentage(
                    transactionNetworkModel,
                    coinDetail,
                    gweiToWei(gp),
                    _binding?.edtGasLimit?.text.toString(),
                    _binding?.edtNonce?.text.toString(),
                    _binding?.edtTransactionData?.text.toString(),
                    percentageValue, fromButton = "imgMinusGasLimit"
                )
            }
        }


        // secondProgress(transactionNetworkModel, coinDetail)

        _binding?.firstProgress?.setOnClickListener {
            firstProgress(transactionNetworkModel, coinDetail)
        }
        _binding?.secondProgress?.setOnClickListener {
            secondProgress(transactionNetworkModel, coinDetail)
        }
        _binding?.thirdProgress?.setOnClickListener {
            thirdProgress(transactionNetworkModel, coinDetail)
        }

        var tempGasPrice = ""
        _binding?.edtGasPrice?.doOnTextChanged { text, start, _, _ ->
            if (start > 0) {
                tempGasPrice = text.toString()
                val gp = stringToBigDecimal(_binding?.edtGasPrice?.text.toString().ifEmpty { "0" })
                increasePercentage(
                    transactionNetworkModel,
                    coinDetail,
                    gweiToWei(gp),
                    _binding?.edtGasLimit?.text.toString(),
                    _binding?.edtNonce?.text.toString(),
                    _binding?.edtTransactionData?.text.toString(),
                    percentageValue
                )
            } else {
                if (_binding?.edtGasPrice?.text!!.isEmpty()) {
                    _binding?.edtGasPrice?.setText(tempGasPrice)
                    _binding?.edtGasPrice?.setSelection((_binding?.edtGasPrice?.text?.length!!))
                }
            }
        }

        var tempGasLimit = ""
        _binding?.edtGasLimit?.doOnTextChanged { text, start, _, _ ->
            if (start > 0) {
                tempGasLimit = text.toString()
                val gp = stringToBigDecimal(_binding?.edtGasPrice?.text.toString().ifEmpty { "0" })
                increasePercentage(
                    transactionNetworkModel,
                    coinDetail,
                    gweiToWei(gp),
                    _binding?.edtGasLimit?.text.toString(),
                    _binding?.edtNonce?.text.toString(),
                    _binding?.edtTransactionData?.text.toString(),
                    percentageValue
                )
            } else {
                if (_binding?.edtGasLimit?.text!!.isEmpty()) {
                    _binding?.edtGasLimit?.setText(tempGasLimit)
                    _binding?.edtGasLimit?.setSelection(tempGasLimit.length)
                }
            }
        }

        var tempNonce = ""
        _binding?.edtNonce?.doOnTextChanged { text, start, _, _ ->
            if (start > 0) {
                tempNonce = text.toString()
                val gp = stringToBigDecimal(_binding?.edtGasPrice?.text.toString().ifEmpty { "0" })
                increasePercentage(
                    transactionNetworkModel,
                    coinDetail,
                    gweiToWei(gp),
                    _binding?.edtGasLimit?.text.toString(),
                    _binding?.edtNonce?.text.toString(),
                    _binding?.edtTransactionData?.text.toString(),
                    percentageValue
                )
            } else {
                if (_binding?.edtNonce?.text!!.isEmpty()) {
                    _binding?.edtNonce?.setText(tempNonce)
                    _binding?.edtNonce?.setSelection(tempNonce.length)
                }
            }
        }


        _binding?.btnSave?.setOnClickListener {
            when {

                _binding?.edtGasPrice?.text?.toString()!!.isEmpty() -> {
                    _binding?.constRoot?.context?.showToast(ENTER_GAS_PRICE_EMPTY)
                }

                _binding?.edtGasPrice?.text?.toString()!!.toDouble() <= 0.0 -> {
                    _binding?.constRoot?.context?.showToast(ENTER_GAS_PRICE)
                }

                _binding?.edtGasLimit?.text?.isEmpty() == true -> {
                    _binding?.constRoot?.context?.showToast(ENTER_GAS_LIMIT)
                }

                _binding?.edtNonce?.text?.isEmpty() == true -> {
                    _binding?.constRoot?.context?.showToast(ENTER_NONCE)
                }

                else -> {

                    val gp = stringToBigDecimal(_binding?.edtGasPrice?.text.toString())
                    gweiToWei(gp)

                    listener?.onSubmitClicked(
                        gweiToWei(gp),
                        _binding?.edtGasLimit?.text.toString(),
                        _binding?.edtNonce?.text.toString(),
                        _binding?.edtTransactionData?.text.toString()
                    )
                    dismiss()

                }
            }


        }


        _binding?.executePendingBindings()
    }


    private fun firstProgress(
        transactionNetworkModel: TransferNetworkDetail, coinDetail: SendCoinDetail
    ) {

        lastSelectedSlippage = 1
        _binding?.progress?.setProgress(1, true)
        setFeeProgress(0)
        percentageValue = 0.1
        val gp = stringToBigDecimal(_binding?.edtGasPrice?.text.toString())
        increasePercentage(
            transactionNetworkModel,
            coinDetail,
            gweiToWei(gp),
            baseGasLimit.toString(),
            _binding?.edtNonce?.text.toString(),
            _binding?.edtTransactionData?.text.toString(),
            0.1
        )
    }

    private fun secondProgress(
        transactionNetworkModel: TransferNetworkDetail, coinDetail: SendCoinDetail
    ) {
        lastSelectedSlippage = 2
        _binding?.progress?.setProgress(51, true)
        setFeeProgress(1)
        percentageValue = 0.5
        val gp = stringToBigDecimal(_binding?.edtGasPrice?.text.toString())
        increasePercentage(
            transactionNetworkModel,
            coinDetail,
            gweiToWei(gp),
            baseGasLimit.toString(),
            _binding?.edtNonce?.text.toString(),
            _binding?.edtTransactionData?.text.toString(),
            0.5
        )
    }

    private fun thirdProgress(
        transactionNetworkModel: TransferNetworkDetail, coinDetail: SendCoinDetail
    ) {
        lastSelectedSlippage = 3
        _binding?.progress?.setProgress(100, true)
        setFeeProgress(2)
        percentageValue = 1.0
        val gp = stringToBigDecimal(_binding?.edtGasPrice?.text.toString())
        increasePercentage(
            transactionNetworkModel,
            coinDetail,
            gweiToWei(gp),
            baseGasLimit.toString(),
            _binding?.edtNonce?.text.toString(),
            _binding?.edtTransactionData?.text.toString(),
            1.0
        )
    }


    private fun setFeeProgress(progress: Int) {
        when (progress) {
            0 -> {
                _binding?.firstProgress?.setImageResource(R.drawable.img_select_radio)
                _binding?.secondProgress?.setImageResource(R.drawable.img_unselect_radio)
                _binding?.thirdProgress?.setImageResource(R.drawable.img_unselect_radio)
            }

            1 -> {
                _binding?.firstProgress?.setImageResource(R.drawable.img_unselect_radio)
                _binding?.secondProgress?.setImageResource(R.drawable.img_select_radio)
                _binding?.thirdProgress?.setImageResource(R.drawable.img_unselect_radio)
            }

            2 -> {
                _binding?.firstProgress?.setImageResource(R.drawable.img_unselect_radio)
                _binding?.secondProgress?.setImageResource(R.drawable.img_unselect_radio)
                _binding?.thirdProgress?.setImageResource(R.drawable.img_select_radio)
            }
        }

    }


    interface DialogOnClickBtnListner {
        fun onSubmitClicked(
            gasPrice: BigDecimal, gasLimit: String, nonce: String, transactionData: String
        )
    }

    private fun increasePercentage(
        transactionNetworkModel: TransferNetworkDetail?,
        coinDetail: SendCoinDetail,
        gasPrice: BigDecimal,
        gasLimit: String,
        nonce: String,
        transactionData: String,
        percentage: Double,
        fromButton: String? = ""
    ) {

        loge(
            "PercentageCR",
            "$percentage :: ${(percentage.toBigDecimal() * 1000.toBigDecimal())}"
        )

        val percentageGasLimit =
            if (percentage > 0) (percentage.toBigDecimal() * 1000.toBigDecimal()) else 0.toBigDecimal()
        val additional = gasLimit.toBigDecimal() + percentageGasLimit
        // val additional = gasLimit.toBigDecimal() + (gasLimit.toBigDecimal() * percentage.toBigDecimal() / 100.toBigDecimal())

        if (fromButton == "progress") {
            _binding?.edtGasLimit?.setText(additional.setScale(0, RoundingMode.DOWN).toString())
            _binding?.edtGasLimit?.setSelection(_binding?.edtGasLimit?.text?.length!!)
        }

        val fee = gasPrice * additional /*stringToBigInteger(gasLimit)*/
        val gasAmount = convertWeiToEther(
            fee.toString(), transactionNetworkModel?.decimal!!
        )

        val chainList =
            coinDetail.tokenList.filter { it.t_address == "" && it.t_type.lowercase() == coinDetail.tokenModel.t_type.lowercase() /*&& it.t_symbol.lowercase() == coinDetail.tokenModel.chain?.symbol?.lowercase()*/ }
        var chainPrice = coinDetail.tokenModel.t_price.toDoubleOrNull()
        if (chainList.isNotEmpty()) {
            chainPrice = chainList[0].t_price.toDoubleOrNull()
        }

        val gp = if (coinDetail.tokenModel.t_address != "") (gasAmount.toDouble() * (chainPrice
            ?: 0.0)) / 1 else (gasAmount.toDouble() * (coinDetail.tokenModel.t_price.toDoubleOrNull()
            ?: 0.0)) / 1
        val networkkFee = gasAmount.toBigDecimal().setScale(6, RoundingMode.DOWN).toString()


        val networkFee = (networkkFee + " " + chainList[0].t_symbol + "(${
            PreferenceHelper.getInstance().getSelectedCurrency()?.symbol
        }${
            String.format(
                "%.2f", gp.toDouble()
            )
        })")

        _binding?.txtMainBalance?.text = networkFee

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