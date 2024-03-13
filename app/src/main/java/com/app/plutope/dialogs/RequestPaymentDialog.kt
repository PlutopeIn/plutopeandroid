package com.app.plutope.dialogs


import android.app.Dialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.WindowManager
import com.app.plutope.R
import com.app.plutope.databinding.DialogRequestPaymentBinding
import com.app.plutope.model.CurrencyModel
import com.app.plutope.model.Tokens
import com.app.plutope.utils.convertAmountToCurrency
import com.app.plutope.utils.extras.PreferenceHelper
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode


class RequestPaymentDialog private constructor() {
    lateinit var binding: DialogRequestPaymentBinding


    companion object {
        var singleInstance: RequestPaymentDialog? = null
        fun getInstance(): RequestPaymentDialog {
            if (singleInstance == null) {
                singleInstance = RequestPaymentDialog()
            }
            return singleInstance!!
        }
    }

    private var dialogRequestPayment: Dialog? = null

    fun show(
        context: Context,
        token: Tokens,
        listener: DialogOnClickBtnListner
    ) {

        var isCurrencySelected: Boolean = false
        var currencyPrice: BigDecimal? = BigDecimal.ZERO
        var convertPrice: Double? = 0.0
        var selectedCurrency: CurrencyModel? = null

        if (dialogRequestPayment == null) {
            dialogRequestPayment =
                Dialog(context, android.R.style.Theme_DeviceDefault_Dialog_NoActionBar_MinWidth)
        }

        binding = DialogRequestPaymentBinding.inflate(LayoutInflater.from(context))
        dialogRequestPayment!!.setContentView(binding.root)
        val layoutParams = dialogRequestPayment?.window!!.attributes
        dialogRequestPayment?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )

        dialogRequestPayment?.window?.setBackgroundDrawableResource(R.color.transparent)
        dialogRequestPayment?.window!!.attributes = layoutParams
        dialogRequestPayment?.setCancelable(false)

        binding.model = token
        selectedCurrency = PreferenceHelper.getInstance().getSelectedCurrency()

        binding.btnConfirm.setOnClickListener {
            if (binding.edtAmount.text.isNotEmpty()) {

                val amount =
                    if (!isCurrencySelected) binding.edtAmount.text.toString() else currencyPrice.toString()
                listener.onSubmitClicked(amount)
                dialogRequestPayment?.dismiss()

            } else {
                binding.edtAmount.error = "Amount required"
            }
        }

        binding.imgClose.setOnClickListener {
            dialogRequestPayment?.dismiss()
        }

        binding.txtCoinType.setOnClickListener {
            isCurrencySelected = !isCurrencySelected
            binding.edtAmount.setText("")
            if (isCurrencySelected) {
                binding.txtCoinType.text = selectedCurrency?.code
                binding.amountBtc.text =
                    context.getString(R.string.amount_label) + " " + selectedCurrency?.code

                //binding.edtAmount.setText(convertPrice.toString())


            } else {

                binding.txtCoinType.text = token.t_symbol
                binding.amountBtc.text =
                    context.getString(R.string.amount_label) + " " + token.t_symbol

                //  binding.edtAmount.setText(convertPrice.toString())


            }
        }


        binding.edtAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty()) {
                    val amt = if (s.toString().startsWith(".")) {
                        "0" + s.toString()
                    } else {
                        s.toString()
                    }

                    val decimalValue = try {
                        BigDecimal(amt).setScale(10, RoundingMode.DOWN)
                    } catch (e: Exception) {
                        BigDecimal.ZERO // If invalid input, use 0
                    }

                    val convertedEstPrice = try {
                        convertAmountToCurrency(
                            amt.toDouble().toBigDecimal() ?: BigDecimal.ZERO,
                            token.t_price.toString().toDouble().toBigDecimal()
                        )
                    } catch (e: NumberFormatException) {
                        BigDecimal.ZERO
                    }


                    currencyPrice = try {
                        val tPrice = token.t_price.toString().toBigDecimal()
                        decimalValue.divide(tPrice, MathContext.DECIMAL128)
                            .setScale(18, RoundingMode.DOWN)

                    } catch (e: ArithmeticException) {
                        BigDecimal.ZERO
                    }


                    convertPrice = convertedEstPrice.toDouble() ?: 0.0
                    if (!isCurrencySelected) {

                        binding.txtConvertBalance.text =
                            if (s.toString().isNotEmpty() && token.t_price.toString()
                                    .isNotEmpty()
                            ) "~ " + selectedCurrency?.symbol + "" + convertPrice else "0"
                    } else {

                        binding.txtConvertBalance.text =
                            if (s.toString().isNotEmpty() && token.t_price.toString()
                                    .isNotEmpty()
                            ) "~ $currencyPrice" + " ${token.t_symbol}" else "0"

                    }

                } else {
                    binding.txtConvertBalance.text = ""
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })



        if (!dialogRequestPayment!!.isShowing) {
            try {
                dialogRequestPayment?.show()
            } catch (e: WindowManager.BadTokenException) {
                e.printStackTrace()
            }
        } else {
            dialogRequestPayment?.dismiss()
        }

        binding.executePendingBindings()
    }


    fun dismiss() {
        if (dialogRequestPayment!!.isShowing) {
            dialogRequestPayment?.dismiss()
        }
    }


    interface DialogOnClickBtnListner {
        fun onSubmitClicked(selectedList: String)
    }

}