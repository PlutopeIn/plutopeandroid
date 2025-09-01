package com.app.plutope.dialogs


import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.app.plutope.R
import com.app.plutope.databinding.DialogRequestPaymentBinding
import com.app.plutope.model.CurrencyModel
import com.app.plutope.model.Tokens
import com.app.plutope.ui.base.BaseDialogFragment
import com.app.plutope.ui.fragment.transactions.receive.receive_coin_detail.ReceiveCoinViewModel
import com.app.plutope.utils.convertAmountToCurrency
import com.app.plutope.utils.extras.PreferenceHelper
import dagger.hilt.android.AndroidEntryPoint
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode


/*
class RequestPaymentDialog private constructor() {
    lateinit var _binding: DialogRequestPaymentBinding

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

        if (context is BaseActivity && (context.isFinishing || context.isDestroyed)) {
            return
        }

        var isCurrencySelected: Boolean = false
        var currencyPrice: BigDecimal? = BigDecimal.ZERO
        var convertPrice: Double? = 0.0
        var selectedCurrency: CurrencyModel? = null

        if (dialogRequestPayment == null) {
            dialogRequestPayment =
                Dialog(context, android.R.style.Theme_DeviceDefault_Dialog_NoActionBar_MinWidth)
        }

        _binding = DialogRequestPaymentBinding.inflate(LayoutInflater.from(context))
        dialogRequestPayment!!.setContentView(_binding.root)
        val layoutParams = dialogRequestPayment?.window!!.attributes
        dialogRequestPayment?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )

        dialogRequestPayment?.window?.setBackgroundDrawableResource(R.color.transparent)
        dialogRequestPayment?.window!!.attributes = layoutParams
        dialogRequestPayment?.setCancelable(false)

        _binding.model = token
        selectedCurrency = PreferenceHelper.getInstance().getSelectedCurrency()

        _binding.btnConfirm.setOnClickListener {
            if (_binding.edtAmount.text.isNotEmpty()) {

                val amount =
                    if (!isCurrencySelected) _binding.edtAmount.text.toString() else currencyPrice.toString()
                listener.onSubmitClicked(amount)
                dialogRequestPayment?.dismiss()

            } else {
                _binding.edtAmount.error = "Amount required"
            }
        }

        _binding.imgClose.setOnClickListener {
            dialogRequestPayment?.dismiss()
        }

        _binding.txtCoinType.setOnClickListener {
            isCurrencySelected = !isCurrencySelected
            _binding.edtAmount.setText("")
            if (isCurrencySelected) {
                _binding.txtCoinType.text = selectedCurrency?.code
                _binding.amountBtc.text =
                    context.getString(R.string.amount_label) + " " + selectedCurrency?.code

                //_binding.edtAmount.setText(convertPrice.toString())


            } else {

                _binding.txtCoinType.text = token.t_symbol
                _binding.amountBtc.text =
                    context.getString(R.string.amount_label) + " " + token.t_symbol

                //  _binding.edtAmount.setText(convertPrice.toString())


            }
        }


        _binding.edtAmount.addTextChangedListener(object : TextWatcher {
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
                            amt.toDouble().toBigDecimal(),
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


                    convertPrice = convertedEstPrice.toDouble()
                    if (!isCurrencySelected) {

                        _binding.txtConvertBalance.text =
                            if (s.toString().isNotEmpty() && token.t_price.toString()
                                    .isNotEmpty()
                            ) "= " + selectedCurrency?.symbol + "" + convertPrice else "0"
                    } else {

                        _binding.txtConvertBalance.text =
                            if (s.toString().isNotEmpty() && token.t_price.toString()
                                    .isNotEmpty()
                            ) "= $currencyPrice" + " ${token.t_symbol}" else "0"

                    }

                } else {
                    _binding.txtConvertBalance.text = ""
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

        _binding.executePendingBindings()
    }


    fun dismiss() {
        if (dialogRequestPayment!!.isShowing) {
            dialogRequestPayment?.dismiss()
        }
    }


    interface DialogOnClickBtnListner {
        fun onSubmitClicked(selectedList: String)
    }

}*/

@AndroidEntryPoint
class RequestPaymentDialogFragment :
    BaseDialogFragment<DialogRequestPaymentBinding, ReceiveCoinViewModel>() {

    private lateinit var _binding: DialogRequestPaymentBinding
    private val _viewModel: ReceiveCoinViewModel by viewModels()

    // private var paymentDialog: Dialog? = null
    private var listener: DialogOnClickBtnListner? = null

    companion object {
        val requestPaymentDialogFragment = "RequestPaymentDialogFragment"

        fun newInstance(token: Tokens): RequestPaymentDialogFragment {
            val fragment = RequestPaymentDialogFragment()
            val args = Bundle()
            args.putParcelable("token", token) // Assuming Tokens implements Parcelable
            fragment.arguments = args
            return fragment
        }
    }

    override fun onStart() {
        super.onStart()
        dialog!!.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        dialog?.setCancelable(false)
    }

    /* override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
         paymentDialog = Dialog(
             requireContext(),
             android.R.style.Theme_DeviceDefault_Dialog_NoActionBar_MinWidth
         )
         _binding = DialogRequestPaymentBinding.inflate(layoutInflater)
         paymentDialog?.setContentView(_binding.root)
         paymentDialog?.setCancelable(true)


         // Set up dialog properties
         paymentDialog?.window?.setLayout(
             WindowManager.LayoutParams.MATCH_PARENT,
             WindowManager.LayoutParams.MATCH_PARENT
         )
         paymentDialog?.window?.setBackgroundDrawableResource(R.color.transparent)
         paymentDialog?.setCancelable(false)

         return paymentDialog!!
     }
     */

    override fun getLayoutID(): Int {
        return R.layout.dialog_request_payment
    }

    override fun setBindingVariables() {
        // _viewModel
    }

    override fun getBindedViewModel(): ReceiveCoinViewModel {
        return _viewModel
    }

    override fun setupObservers() {

    }

    override fun setUpUI() {
        val token: Tokens = arguments?.getParcelable("token") ?: return
        _binding.model = token

        var isCurrencySelected = false
        var currencyPrice: BigDecimal? = BigDecimal.ZERO
        var convertPrice: Double? = 0.0
        val selectedCurrency: CurrencyModel? = PreferenceHelper.getInstance().getSelectedCurrency()

        _binding.btnConfirm.setOnClickListener {
            if (_binding.edtAmount.text.isNotEmpty()) {
                val amount =
                    if (!isCurrencySelected) _binding.edtAmount.text.toString() else currencyPrice.toString()
                listener?.onSubmitClicked(amount)
                dialog?.dismiss()
            } else {
                _binding.edtAmount.error = "Amount required"
            }
        }

        _binding.imgClose.setOnClickListener {
            dialog?.dismiss()
        }

        _binding.txtCoinType.setOnClickListener {
            isCurrencySelected = !isCurrencySelected
            _binding.edtAmount.setText("")
            if (isCurrencySelected) {
                _binding.txtCoinType.text = selectedCurrency?.code
                _binding.amountBtc.text =
                    getString(R.string.amount_label) + " " + selectedCurrency?.code
            } else {
                _binding.txtCoinType.text = token.t_symbol
                _binding.amountBtc.text = getString(R.string.amount_label) + " " + token.t_symbol
            }
        }

        _binding.edtAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty()) {
                    val amt = if (s.toString().startsWith(".")) "0" + s.toString() else s.toString()

                    val decimalValue = try {
                        BigDecimal(amt).setScale(10, RoundingMode.DOWN)
                    } catch (e: Exception) {
                        BigDecimal.ZERO
                    }

                    val convertedEstPrice = try {
                        convertAmountToCurrency(
                            amt.toDouble().toBigDecimal(),
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

                    convertPrice = convertedEstPrice.toDouble()
                    if (!isCurrencySelected) {
                        _binding.txtConvertBalance.text =
                            if (s.toString().isNotEmpty() && token.t_price.toString()
                                    .isNotEmpty()
                            ) {
                                "= " + selectedCurrency?.symbol + convertPrice
                            } else {
                                "0"
                            }
                    } else {
                        _binding.txtConvertBalance.text =
                            if (s.toString().isNotEmpty() && token.t_price.toString()
                                    .isNotEmpty()
                            ) {
                                "= $currencyPrice ${token.t_symbol}"
                            } else {
                                "0"
                            }
                    }
                } else {
                    _binding.txtConvertBalance.text = ""
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        _binding.executePendingBindings()

        // setupListeners(token)
    }

    override fun showToolbar(): Boolean {
        return false
    }

    override fun setDataBinding(vb: DialogRequestPaymentBinding) {
        _binding = vb
    }


    fun setDialogListener(listener: DialogOnClickBtnListner) {
        this.listener = listener
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        setFragmentResult(requestPaymentDialogFragment, Bundle())
    }

    interface DialogOnClickBtnListner {
        fun onSubmitClicked(amount: String)
    }
}
