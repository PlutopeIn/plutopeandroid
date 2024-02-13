package com.app.plutope.dialogs.walletConnectionDialog

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import com.app.plutope.R
import com.app.plutope.databinding.DialogWalletAprooveTransactionBinding
import com.app.plutope.model.Tokens
import com.app.plutope.model.TransactionModelDApp
import com.app.plutope.model.Wallet
import com.app.plutope.ui.fragment.transactions.send.send_coin.TransferNetworkDetail
import com.app.plutope.utils.coinTypeEnum.CoinType
import com.app.plutope.utils.convertWeiToEther
import com.app.plutope.utils.extras.PreferenceHelper
import com.app.plutope.utils.hexToMaticWithDecimal
import com.app.plutope.utils.loge
import com.app.plutope.utils.underlineText
import com.app.plutope.utils.walletConnection.WalletConnectionUtils
import com.app.plutope.utils.walletConnection.session_request.SessionRequestUI
import com.app.plutope.utils.walletConnection.session_request.SessionRequestViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.math.RoundingMode


class DialogWalletApproveTransaction private constructor() {

    private var binding: DialogWalletAprooveTransactionBinding? = null

    companion object {
        var singleInstence: DialogWalletApproveTransaction? = null
        fun getInstance(): DialogWalletApproveTransaction? {
            if (singleInstence == null) {
                singleInstence = DialogWalletApproveTransaction()
            }
            return singleInstence
        }

        fun clearDialogWalletApproveInstence() {
            singleInstence = null
        }
    }

    private var approveDialog: BottomSheetDialog? = null
    fun show(
        context: Context?,
        model: TransactionModelDApp,
        token: Tokens,
        wrapData: TransferNetworkDetail?,
        unit: (dialog: BottomSheetDialog, TransactionModelDApp, Boolean) -> Any
    ) {

        if (approveDialog == null) {
            approveDialog = BottomSheetDialog(
                context!!,
                android.R.style.Theme_DeviceDefault_Dialog_NoActionBar_MinWidth
            )
        }
        approveDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(approveDialog?.window?.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.BOTTOM
        lp.windowAnimations = R.style.DialogAnimation
        approveDialog?.window?.attributes = lp
        approveDialog?.behavior?.peekHeight = 2500
        approveDialog?.setCancelable(false)

        binding = DialogWalletAprooveTransactionBinding.inflate(LayoutInflater.from(context))
        approveDialog?.setContentView(binding!!.root)


        val sessionRequestViewModel = SessionRequestViewModel()
        val sessionRequestUI: SessionRequestUI = sessionRequestViewModel.generateSessionRequestUI()
        val sessionRequest = sessionRequestUI as? SessionRequestUI.Content

        loge("sessionRequestUI ==> ", "$sessionRequestUI")

        // val model = parseData(data)
        // val model = parseData(data)

        binding!!.apply {

            imgBack.setOnClickListener {
                approveDialog?.dismiss()
            }
            txtAssetValue.text = "${token.t_name}(${token.t_symbol})"
            txtFromValue.text = Wallet.getPublicWalletAddress(CoinType.ETHEREUM)
            txtToValue.text = model.transactionDetails[0].to

            txtDAppConnectorName.text = model.exchange

            loge("Transe :", "${model}")

            if (model.transactionDetails[0].value != "0") {
                val matic = hexToMaticWithDecimal(model.transactionDetails[0].value)
                matic.setScale(2, RoundingMode.DOWN)
                txtBalance.text = "-" + matic + " " + token.t_symbol
            } else {
                txtBalance.text = "-" + "0" + " " + token.t_symbol
            }

            calculateNetworkFees(wrapData, token, context)

            if (model.transactionType == WalletConnectionUtils.WalletConnectionMethod.personalSignIn || model.transactionType == WalletConnectionUtils.WalletConnectionMethod.ethSignTypedDataV4) {
                txtToolbarTitle.text = context?.getString(R.string.signature_request)
                txtBalance.visibility = View.GONE
                btnConfirm.text = context?.getString(R.string.sign)
                layoutFinanceDetail.visibility = View.GONE
                layoutSignDetail.visibility = View.VISIBLE

                txtSignMessage.text = sessionRequest?.param
            } else {
                txtToolbarTitle.text = context?.getString(R.string.transactions)
                txtBalance.visibility = View.VISIBLE
                btnConfirm.text = context?.getString(R.string.approve)
                layoutFinanceDetail.visibility = View.VISIBLE
                layoutSignDetail.visibility = View.GONE
            }

            btnConfirm.setOnClickListener {
                unit.invoke(approveDialog!!, model, true)
                // dismiss()

            }

            btnCancel.setOnClickListener {
                unit.invoke(approveDialog!!, model, false)
                // approveDialog?.dismiss()
            }

        }


        if (approveDialog != null) {
            if (approveDialog!!.isShowing) {
                approveDialog?.dismiss()
            } else {
                try {
                    approveDialog?.show()
                } catch (e: WindowManager.BadTokenException) {
                    e.printStackTrace()
                }

            }
        }
    }

    private fun calculateNetworkFees(
        wrapData: TransferNetworkDetail?,
        token: Tokens,
        context: Context?
    ) {
        val fee = wrapData?.gasPrice?.toBigInteger()?.times(wrapData.gasLimit)
        val gasAmount = convertWeiToEther(
            fee.toString(),
            wrapData?.decimal!!
        )

        // val chainList = coinDetail.tokenList.filter { it.t_address == "" && it.t_type?.lowercase() == coinDetail.tokenModel.t_type?.lowercase() && it.t_symbol?.lowercase() == coinDetail.tokenModel.chain?.symbol?.lowercase() }
        val chainPrice = token.t_price?.toDoubleOrNull()
        val gp = if (token.t_address != "") (gasAmount.toDouble() * (chainPrice
            ?: 0.0)) / 1 else (gasAmount.toDouble() * (token.t_price?.toDoubleOrNull()
            ?: 0.0)) / 1
        val networkFee = gasAmount.toBigDecimal().setScale(6, RoundingMode.DOWN).toString()

        loge("GasAmount", "$gasAmount :: GP =>$gp  :: networkFee => $networkFee")

        val correctGP = gp.toBigDecimal().setScale(2, RoundingMode.DOWN).toString()

        val networkFees =
            (networkFee + " " + token.chain?.symbol + "(${PreferenceHelper(context!!).getSelectedCurrency()?.symbol}${
                correctGP
            })")

        binding!!.txtNetworkFeeValue.underlineText(networkFees)


        val maxTotal = gp
        binding!!.txtMaxTotalValue.text =
            PreferenceHelper(context).getSelectedCurrency()?.symbol + maxTotal.toBigDecimal()
                .setScale(2, RoundingMode.DOWN).toString()


    }

    fun dismiss() {
        if (approveDialog != null) {
            approveDialog?.dismiss()
        }
    }




}