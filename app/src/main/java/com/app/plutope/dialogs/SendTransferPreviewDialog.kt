package com.app.plutope.dialogs

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.app.plutope.R
import com.app.plutope.databinding.DialogSendTransferPreviewBinding
import com.app.plutope.model.Wallet
import com.app.plutope.ui.base.BaseBottomSheetDialog
import com.app.plutope.ui.fragment.transactions.send.PreviewSendEditPrioritiesDialog
import com.app.plutope.ui.fragment.transactions.send.send_coin.PreviewSendAdvanceDialog
import com.app.plutope.ui.fragment.transactions.send.send_coin.SendCoinDetail
import com.app.plutope.ui.fragment.transactions.send.send_coin.SendCoinViewModel
import com.app.plutope.ui.fragment.transactions.send.send_coin.TransferNetworkDetail
import com.app.plutope.utils.bigIntegerToString
import com.app.plutope.utils.coinTypeEnum.CoinType
import com.app.plutope.utils.convertWeiToEther
import com.app.plutope.utils.extras.PreferenceHelper
import com.app.plutope.utils.loge
import com.app.plutope.utils.stringToBigDecimal
import com.app.plutope.utils.stringToBigInteger
import com.app.plutope.utils.underlineText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.math.BigInteger
import java.math.RoundingMode
import javax.inject.Inject

@AndroidEntryPoint
class SendTransferPreviewDialog(private val listner: DialogOnClickBtnListner) :
    BaseBottomSheetDialog() {
    private lateinit var sendCoinViewModel: SendCoinViewModel
    private var binding: DialogSendTransferPreviewBinding? = null

    var transactionNetworkModel: TransferNetworkDetail? = null

    var gasLimitTemp = ""
    var nounce = ""

    @Inject
    lateinit var preferenceHelper: PreferenceHelper

    private lateinit var coinDetail: SendCoinDetail
    override fun setUpObservers() {
        sendCoinViewModel.getCoinDetail().observe(viewLifecycleOwner) {
            coinDetail = it
            setDetail()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setDetail() {
        binding?.apply {
            txtAssetValue.text =
                "${coinDetail.tokenModel.t_name}(${coinDetail.tokenModel.t_symbol})"
            txtFromValue.text = Wallet.getPublicWalletAddress(CoinType.ETHEREUM)
            txtToValue.text = coinDetail.address
            txtBalance.text =
                "-" + coinDetail.amount.toString() + " " + coinDetail.tokenModel.t_symbol
            txtPrice.text = coinDetail.convertedPrice.replace("~", "")


            viewLifecycleOwner.lifecycleScope.launch {

                coinDetail.tokenModel.callFunction.getTokenOrCoinNetworkDetailBeforeSend(
                    coinDetail.address,
                    coinDetail.amount.toDouble(),
                    coinDetail.tokenList
                ) { success, model, code ->

                    if (!success) {
                        return@getTokenOrCoinNetworkDetailBeforeSend
                    } else {

                        val chainList =
                            coinDetail.tokenList.filter { it.t_address == "" && it.t_type?.lowercase() == coinDetail.tokenModel.t_type?.lowercase() && it.t_symbol?.lowercase() == coinDetail.tokenModel.chain?.symbol?.lowercase() }
                        var chainPrice = coinDetail.tokenModel.t_price?.toDoubleOrNull()
                        if (chainList.isNotEmpty()) {
                            chainPrice = chainList[0].t_price?.toDoubleOrNull()
                        }

                        val gasPrice =
                            if (coinDetail.tokenModel.t_address != "") ((model?.gasAmount?.toDouble()
                                ?: 0.0) * (chainPrice
                                ?: 0.0)) / 1 else ((model?.gasAmount?.toDouble()
                                ?: 0.0) * (coinDetail.tokenModel.t_price?.toDoubleOrNull()
                                ?: 0.0)) / 1
                        val networkFee =
                            model?.gasAmount?.toBigDecimal()?.setScale(6, RoundingMode.DOWN)
                                .toString()
                        loge(
                            "SendTrans::",
                            "setDetail: ${coinDetail.tokenModel}  :: $success :: $model :: $code"
                        )

                        transactionNetworkModel = model

                        if (isAdded) {
                            requireActivity().runOnUiThread {
                                val networkFee =
                                    (networkFee + " " + coinDetail.tokenModel.chain?.symbol + "(${preferenceHelper.getSelectedCurrency()?.symbol}${
                                        String.format(
                                            "%.2f",
                                            gasPrice
                                        )
                                    })")

                                txtNetworkFeeValue.underlineText(networkFee)
                                val maxTotal =
                                    coinDetail.convertedPrice.replace(Regex("[^0-9.]"), "")
                                        .toDouble() + gasPrice
                                txtMaxTotalValue.text =
                                    preferenceHelper.getSelectedCurrency()?.symbol + maxTotal.toBigDecimal()
                                        .setScale(2, RoundingMode.DOWN).toString()

                                // imgSetting.visibility = View.VISIBLE
                                progressNetworkFee.visibility = View.GONE
                                progressMaxTotal.visibility = View.GONE

                                btnConfirm.isEnabled = success
                            }
                        }
                    }


                    // }
                }

            }
        }
    }

    override fun setUpUI() {
        requireActivity().runOnUiThread {
            setOnClickListner()
        }

    }

    private fun setOnClickListner() {
        binding?.apply {
            imgBack.setOnClickListener {
                dialog?.dismiss()
                listner.onDismissClickListner()
            }

            btnConfirm.setOnClickListener {

                sendCoinViewModel.customGasLimit.value = gasLimitTemp

                dialog?.dismiss()
                listner.onConfirmClickListner()
            }

            txtNetworkFeeValue.setOnClickListener {

                val storedTransaction = TransferNetworkDetail(
                    gasLimit = if (sendCoinViewModel.customGasLimit.value!! != "0") stringToBigInteger(
                        sendCoinViewModel.customGasLimit.value!!
                    ) else transactionNetworkModel!!.gasLimit,
                    nonce = if (sendCoinViewModel.customNonce.value?.toInt() != 0) sendCoinViewModel.customNonce.value!!.toInt() else transactionNetworkModel!!.nonce,
                    gasFee = transactionNetworkModel!!.gasFee,
                    gasAmount = transactionNetworkModel!!.gasAmount,
                    gasPrice = if (bigIntegerToString(sendCoinViewModel.customGasPrice.value!!) != "0") bigIntegerToString(
                        sendCoinViewModel.customGasPrice.value!!
                    ) else transactionNetworkModel!!.gasPrice,
                    decimal = sendCoinViewModel.decimal.value
                )

                PreviewSendEditPrioritiesDialog.getInstance().show(
                    requireContext(),
                    /*transactionNetworkModel*/storedTransaction, coinDetail,
                    object : PreviewSendEditPrioritiesDialog.DialogOnClickBtnListner {
                        @SuppressLint("SetTextI18n")
                        override fun onSubmitClicked(
                            gasPrice: BigInteger,
                            gasLimit: String,
                            nonce: String,
                            transactionData: String
                        ) {

                            sendCoinViewModel.isFromLaverageChange.value = true
                            sendCoinViewModel.customGasPrice.value = gasPrice
                            //  sendCoinViewModel.customGasLimit.value = gasLimit

                            gasLimitTemp = gasLimit
                            sendCoinViewModel.customNonce.value = nonce
                            sendCoinViewModel.decimal.value = transactionNetworkModel?.decimal!!
                            sendCoinViewModel.customTransactionData.value = ""


                            val fee = gasPrice.toBigDecimal() * stringToBigDecimal(gasLimit)
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
                            val networkkFee =
                                gasAmount.toBigDecimal().setScale(6, RoundingMode.DOWN).toString()
                            loge(
                                "TAG",
                                "SendTransactionPreview :: $networkkFee :: $fee :: $gasAmount"
                            )
                            requireActivity().runOnUiThread {

                                val networkFee =
                                    (networkkFee + " " + coinDetail.tokenModel.chain?.symbol + "(${preferenceHelper.getSelectedCurrency()?.symbol}${
                                        String.format(
                                            "%.2f",
                                            gp.toDouble()
                                        )
                                    })")

                                txtNetworkFeeValue.underlineText(networkFee)

                                val maxTotal =
                                    coinDetail.convertedPrice.replace(Regex("[^0-9.]"), "")
                                        .toDouble() + gp
                                txtMaxTotalValue.text =
                                    preferenceHelper.getSelectedCurrency()?.symbol + maxTotal.toBigDecimal()
                                        .setScale(2, RoundingMode.DOWN).toString()
                                btnConfirm.isEnabled = true
                            }


                        }
                    })


            }


            imgSetting.setOnClickListener {
                // listner.onSettingClick(transactionNetworkModel)

                val storedTransaction = TransferNetworkDetail(
                    gasLimit = if (sendCoinViewModel.customGasLimit.value!! != "0") stringToBigInteger(
                        sendCoinViewModel.customGasLimit.value!!
                    ) else transactionNetworkModel!!.gasLimit,
                    nonce = if (sendCoinViewModel.customNonce.value?.toInt() != 0) sendCoinViewModel.customNonce.value!!.toInt() else transactionNetworkModel!!.nonce,
                    gasFee = transactionNetworkModel!!.gasFee,
                    gasAmount = transactionNetworkModel!!.gasAmount,
                    gasPrice = if (bigIntegerToString(sendCoinViewModel.customGasPrice.value!!) != "0") bigIntegerToString(
                        sendCoinViewModel.customGasPrice.value!!
                    ) else transactionNetworkModel!!.gasPrice,
                    decimal = sendCoinViewModel.decimal.value
                )

                PreviewSendAdvanceDialog.getInstance().show(
                    requireContext(),
                    /*transactionNetworkModel*/storedTransaction,
                    object : PreviewSendAdvanceDialog.DialogOnClickBtnListner {
                        @SuppressLint("SetTextI18n")
                        override fun onSubmitClicked(
                            gasPrice: BigInteger,
                            gasLimit: String,
                            nonce: String,
                            transactionData: String
                        ) {

                            sendCoinViewModel.isFromLaverageChange.value = true
                            sendCoinViewModel.customGasPrice.value = gasPrice
                            sendCoinViewModel.customGasLimit.value = gasLimit
                            sendCoinViewModel.customNonce.value = nonce
                            sendCoinViewModel.decimal.value = transactionNetworkModel?.decimal!!
                            sendCoinViewModel.customTransactionData.value = ""


                            val fee = gasPrice * stringToBigInteger(gasLimit)
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
                            val networkkFee =
                                gasAmount.toBigDecimal().setScale(6, RoundingMode.DOWN).toString()
                            Log.e(
                                "TAG",
                                "SendTransactionPreview :: $networkkFee :: $fee :: $gasAmount"
                            )
                            requireActivity().runOnUiThread {

                                val networkFee =
                                    (networkkFee + " " + coinDetail.tokenModel.chain?.symbol + "(${preferenceHelper.getSelectedCurrency()?.symbol}${
                                        String.format(
                                            "%.2f",
                                            gp.toDouble()
                                        )
                                    })")

                                txtNetworkFeeValue.underlineText(networkFee)


                                val maxTotal =
                                    coinDetail.convertedPrice.replace(Regex("[^0-9.]"), "")
                                        .toDouble() + gp
                                txtMaxTotalValue.text =
                                    preferenceHelper.getSelectedCurrency()?.symbol + maxTotal.toBigDecimal()
                                        .setScale(2, RoundingMode.DOWN).toString()
                                btnConfirm.isEnabled = true
                            }


                        }
                    })


            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.dialog_send_transfer_preview,
                container,
                false
            )

        // binding!!.root.setBackgroundColor(Color.TRANSPARENT)
        sendCoinViewModel = ViewModelProvider(requireActivity())[SendCoinViewModel::class.java]

        return binding!!.root
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dialog.dismiss()
    }


    interface DialogOnClickBtnListner {
        fun onConfirmClickListner()
        fun onDismissClickListner()

        fun onSettingClick(transactionNetworkModel: TransferNetworkDetail?)
    }



}