package com.app.plutope.dialogs

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import com.app.plutope.R
import com.app.plutope.databinding.DialogTransactionConfirmationDialogBinding
import com.app.plutope.model.Tokens
import com.app.plutope.model.Wallet
import com.app.plutope.ui.fragment.ens.ENSListModel
import com.app.plutope.utils.coinTypeEnum.CoinType
import com.app.plutope.utils.hexToMatic
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.math.RoundingMode


class DialogConfirmationTransaction private constructor() {

    private var binding: DialogTransactionConfirmationDialogBinding? = null

    companion object {
        var singleInstence: DialogConfirmationTransaction? = null
        fun getInstance(): DialogConfirmationTransaction? {
            if (singleInstence == null) {
                singleInstence = DialogConfirmationTransaction()
            }
            return singleInstence
        }
    }

    private var alertDialogLocation: BottomSheetDialog? = null
    fun show(
        context: Context?,
        model: ENSListModel,
        token: Tokens,
        unit: (ENSListModel) -> Any
    ) {

        if (alertDialogLocation == null) {
            alertDialogLocation = BottomSheetDialog(
                context!!,
                android.R.style.Theme_DeviceDefault_Dialog_NoActionBar_MinWidth
            )
        }
        alertDialogLocation?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(alertDialogLocation?.window?.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.BOTTOM
        lp.windowAnimations = R.style.DialogAnimation
        alertDialogLocation?.window?.attributes = lp
        alertDialogLocation?.behavior?.peekHeight = 2000
        alertDialogLocation?.setCancelable(true)

        binding = DialogTransactionConfirmationDialogBinding.inflate(LayoutInflater.from(context))
        alertDialogLocation?.setContentView(binding!!.root)


        binding!!.apply {

            imgBack.setOnClickListener {
                alertDialogLocation?.dismiss()
            }


            txtAssetValue.text = "${token.t_name}(${token.t_symbol})"
            txtFromValue.text = Wallet.getPublicWalletAddress(CoinType.ETHEREUM)
            txtToValue.text = model.data?.tx?.params?.to

            val matic = hexToMatic(model.data?.tx?.arguments?.price!!)
            matic.toBigDecimal().setScale(2, RoundingMode.DOWN)

            txtBalance.text = "-" + matic + " " + token.t_symbol

            val price = model.data?.availability?.price?.subTotal?.usdCents?.div(100)
            txtPrice.apply {
                text = txtPrice.context.getString(R.string.price, "$price")
            }

            // txtPrice.text = coinDetail.convertedPrice.replace("~", "")

            btnConfirm.setOnClickListener {
                unit.invoke(model)
                alertDialogLocation?.dismiss()
            }


        }



        if (alertDialogLocation != null) {
            if (alertDialogLocation!!.isShowing) {
                alertDialogLocation?.dismiss()
            } else {
                try {
                    alertDialogLocation?.show()
                } catch (e: WindowManager.BadTokenException) {
                    e.printStackTrace()
                }

            }
        }
    }

    fun dismiss() {
        if (alertDialogLocation != null) {
            alertDialogLocation?.dismiss()
        }
    }

}