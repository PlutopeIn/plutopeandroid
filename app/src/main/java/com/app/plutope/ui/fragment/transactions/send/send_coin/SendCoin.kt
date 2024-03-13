package com.app.plutope.ui.fragment.transactions.send.send_coin

import android.content.ClipboardManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.core.os.postDelayed
import androidx.core.text.HtmlCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentSendCoinBinding
import com.app.plutope.dialogs.AddContactDialog
import com.app.plutope.dialogs.DeviceLockFullScreenDialog
import com.app.plutope.dialogs.SendTransferPreviewDialog
import com.app.plutope.model.ContactModel
import com.app.plutope.model.CurrencyModel
import com.app.plutope.model.Tokens
import com.app.plutope.model.Wallet
import com.app.plutope.ui.base.BaseActivity
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.ui.fragment.contact.ContactListFragment
import com.app.plutope.ui.fragment.contact.ContactListViewModel
import com.app.plutope.ui.fragment.token.TokenViewModel
import com.app.plutope.ui.fragment.transactions.buy.graph.GraphDetailViewModel
import com.app.plutope.utils.BitcoinAddressValidator
import com.app.plutope.utils.Securities
import com.app.plutope.utils.bigIntegerToString
import com.app.plutope.utils.coinTypeEnum.CoinType
import com.app.plutope.utils.constant.COIN_GEKO_MARKET_API
import com.app.plutope.utils.constant.ENTER_AMOUNT
import com.app.plutope.utils.constant.ENTER_SENDER_ADDRESS
import com.app.plutope.utils.constant.INVALID_BALANCE
import com.app.plutope.utils.constant.INVALID_SENDER_ADDRESS
import com.app.plutope.utils.constant.PLEASE_ENTER_SOME_AMOUNT
import com.app.plutope.utils.constant.lastSelectedSlippage
import com.app.plutope.utils.convertAmountToCurrency
import com.app.plutope.utils.convertWeiToEther
import com.app.plutope.utils.customSnackbar.CustomSnackbar
import com.app.plutope.utils.enableDisableButton
import com.app.plutope.utils.extractQRCodeScannerInfo
import com.app.plutope.utils.extras.BiometricResult
import com.app.plutope.utils.extras.InputFilterMinMax
import com.app.plutope.utils.extras.buttonClickedWithEffect
import com.app.plutope.utils.extras.setBioMetric
import com.app.plutope.utils.extras.setSafeOnClickListener
import com.app.plutope.utils.formatDecimal
import com.app.plutope.utils.hideKeyboard
import com.app.plutope.utils.hideLoader
import com.app.plutope.utils.isScientificNotation
import com.app.plutope.utils.loge
import com.app.plutope.utils.network.NetworkState
import com.app.plutope.utils.safeNavigate
import com.app.plutope.utils.showLoader
import com.app.plutope.utils.showLoaderAnyHow
import com.app.plutope.utils.showToast
import com.app.plutope.utils.stringToBigInteger
import dagger.hilt.android.AndroidEntryPoint
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanQRCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.web3j.crypto.WalletUtils
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

@AndroidEntryPoint
class SendCoin : BaseFragment<FragmentSendCoinBinding, SendCoinViewModel>() {
    private var convertPrice: Double? = 0.0
    private var tokenList: List<Tokens> = listOf()
    private var currencyPrice: BigDecimal? = BigDecimal.ZERO
    private var selectedCurrency: CurrencyModel? = null

    private val sendCoinViewModel: SendCoinViewModel by activityViewModels()

    //  private lateinit var sendCoinViewModel: SendCoinViewModel
    private val contactViewModel: ContactListViewModel by viewModels()
    private val tokenViewModel: TokenViewModel by viewModels()
    private var isCurrencySelected: Boolean = false
    private val graphDetailViewModel: GraphDetailViewModel by viewModels()
    val args: SendCoinArgs by navArgs()

    private val scanQrCode = registerForActivityResult(ScanQRCode()) { result: QRResult ->
        when (result) {
            is QRResult.QRSuccess -> {
                handleQRCodeResult(result.content.rawValue)
            }

            QRResult.QRUserCanceled -> Toast.makeText(
                requireContext(),
                "Cancelled",
                Toast.LENGTH_LONG
            ).show()

            QRResult.QRMissingPermission -> Toast.makeText(
                requireContext(),
                "Missing permission",
                Toast.LENGTH_LONG
            ).show()

            is QRResult.QRError -> "${result.exception.javaClass.simpleName}: ${result.exception.localizedMessage}"
        }


    }

    private var biometricListener = object : BiometricResult {
        override fun success() {

            /* PreferenceHelper.getInstance().isBiometricAllow = true
             DeviceLockFullScreenDialog.getInstance().dismiss()*/

            sendTransactionCall()

        }

        override fun failure(errorCode: Int, errorMessage: String) {

            when (errorCode) {

                BiometricPrompt.ERROR_LOCKOUT -> (requireActivity() as BaseActivity).continueWithoutBiometric(
                    "Maximum number of attempts exceeds! Try again later",
                    useDevicePassword = true
                )

                BiometricPrompt.ERROR_USER_CANCELED, BiometricPrompt.ERROR_NEGATIVE_BUTTON, BiometricPrompt.ERROR_CANCELED -> {

                    /*  (requireActivity() as BaseActivity).continueWithoutBiometric(
                          "Unlock with Face ID/ Touch ID or password",
                          true
                      )*/

                }

                else -> (requireActivity() as BaseActivity).continueWithoutBiometric(errorMessage)
            }
        }

        override fun successCustomPasscode() {
            sendTransactionCall()
        }

    }

    override fun getViewModel(): SendCoinViewModel {
        // sendCoinViewModel = ViewModelProvider(requireActivity())[SendCoinViewModel::class.java]
        return sendCoinViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.sendCoinViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_send_coin
    }

    override fun setupToolbarText(): String {
        //  return "Send ${args.tokenModel.t_symbol}(${args.tokenModel.t_type})"
        return "${context?.getString(R.string.send)} ${args.tokenModel.t_symbol}"


    }

    override fun setupUI() {

        viewDataBinding!!.model = args.tokenModel
        selectedCurrency = preferenceHelper.getSelectedCurrency()

        /*CoroutineScope(Dispatchers.Main).launch {
            loge("ENS","Address => ${resolveENSName()}")
        }*/

        requireContext().hideKeyboard(viewDataBinding!!.root, viewDataBinding!!.edtAmount)

        if (args.tokenModel.chain?.coinType == CoinType.BITCOIN) {
            viewDataBinding!!.btnContinue.text = getString(R.string.send_bitcoin)
        } else {
            viewDataBinding!!.btnContinue.text = getString(R.string.next)
        }

        val inputFilterMinMax = InputFilterMinMax()
        inputFilterMinMax.digitsInputFilter(1000, 18, Long.MAX_VALUE.toDouble())
        viewDataBinding!!.edtAmount.filters = arrayOf(inputFilterMinMax)

        setOnClickListner()

        setFragmentResultListener(ContactListFragment.keyContact) { _, bundle ->
            val selectedAddress =
                bundle.getParcelable(ContactListFragment.keyContactSelect) as? ContactModel
            viewDataBinding?.edtContractAddress?.setText("")
            viewDataBinding?.edtContractAddress?.setText(selectedAddress?.address.toString())
            viewDataBinding?.edtContractAddress?.setSelection(viewDataBinding!!.edtContractAddress.length())
        }

        graphDetailViewModel.executeGetMarketResponse("$COIN_GEKO_MARKET_API?vs_currency=${preferenceHelper.getSelectedCurrency()?.code}&sparkline=false&locale=en&ids=${args.tokenModel.tokenId}")

    }

    private fun setOnClickListner() {
        viewDataBinding?.apply {
            txtPaste.setSafeOnClickListener {
                val clipboard =
                    requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val item = clipboard.primaryClip?.getItemAt(0)
                val pasteData = item?.text
                viewDataBinding?.edtContractAddress?.setText("")
                viewDataBinding?.edtContractAddress?.setText(pasteData)
                viewDataBinding?.edtContractAddress?.setSelection(viewDataBinding!!.edtContractAddress.length())

                if (WalletUtils.isValidAddress(viewDataBinding?.edtContractAddress?.text.toString())) {
                    contactViewModel.getSpecificContactList(viewDataBinding!!.edtContractAddress.text.toString())
                }
            }

            imgScanner.setSafeOnClickListener {
                scanQrCode.launch(null)
            }


            edtAmount.addTextChangedListener(object : TextWatcher {
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
                                args.tokenModel.t_price.toString().toDouble().toBigDecimal()
                            )
                        } catch (e: NumberFormatException) {
                            BigDecimal.ZERO
                        }


                        currencyPrice = try {
                            val tPrice = args.tokenModel.t_price.toString().toBigDecimal()
                            decimalValue.divide(tPrice, MathContext.DECIMAL128)
                                .setScale(10, RoundingMode.DOWN)

                        } catch (e: ArithmeticException) {
                            BigDecimal.ZERO
                        }


                        convertPrice = convertedEstPrice.toDouble() ?: 0.0
                        if (!isCurrencySelected) {

                            viewDataBinding?.txtConvertBalance?.text =
                                if (s.toString().isNotEmpty() && args.tokenModel.t_price.toString()
                                        .isNotEmpty()
                                ) "~ " + selectedCurrency?.symbol + "" + convertPrice else "0"
                        } else {

                            viewDataBinding?.txtConvertBalance?.text =
                                if (s.toString().isNotEmpty() && args.tokenModel.t_price.toString()
                                        .isNotEmpty()
                                ) "~ $currencyPrice" + " ${args.tokenModel.t_symbol}" else "0"

                        }

                    } else {
                        viewDataBinding?.txtConvertBalance?.text = ""
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            txtMax.setSafeOnClickListener {
                val balanceValue = args.tokenModel.t_balance.toDoubleOrNull() ?: 0.0
                val formattedBalance = if (balanceValue == 0.0) "0" else balanceValue.toString()


                viewDataBinding!!.edtAmount.setText(formattedBalance)
                // viewDataBinding?.edtAmount?.setSelection(viewDataBinding!!.edtContractAddress.length())

            }

            txtCoinType.setOnClickListener {
                isCurrencySelected = !isCurrencySelected
                viewDataBinding?.edtAmount?.setText("")
                loge("isCurrencySelected", "=>$isCurrencySelected")
                if (isCurrencySelected) {
                    viewDataBinding!!.txtMax.visibility = GONE
                    viewDataBinding!!.txtCoinType.text = selectedCurrency?.code
                    viewDataBinding!!.amountBtc.text =
                        getString(R.string.amount_label) + " " + selectedCurrency?.code
                } else {
                    viewDataBinding!!.txtMax.visibility = VISIBLE
                    viewDataBinding!!.txtCoinType.text = viewDataBinding!!.model?.t_symbol
                    viewDataBinding!!.amountBtc.text =
                        getString(R.string.amount_label) + " " + model?.t_symbol

                }

            }



            btnContinue.buttonClickedWithEffect {

                requireContext().hideKeyboard(requireActivity().window.decorView.windowToken)

                when {
                    viewDataBinding?.edtContractAddress?.text?.isEmpty() == true -> {
                        requireContext().showToast(ENTER_SENDER_ADDRESS)
                    }

                    viewDataBinding?.edtAmount?.text?.isEmpty() == true -> {
                        requireContext().showToast(ENTER_AMOUNT)
                    }

                    viewDataBinding?.edtAmount?.text?.toString()!!.toDouble() <= 0.0 -> {
                        requireContext().showToast(PLEASE_ENTER_SOME_AMOUNT)
                    }

                    !WalletUtils.isValidAddress(viewDataBinding?.edtContractAddress?.text.toString()) && !BitcoinAddressValidator.validateBitcoinAddress(
                        viewDataBinding?.edtContractAddress?.text.toString()
                    ) -> {
                        requireContext().showToast(INVALID_SENDER_ADDRESS)
                    }


                    else -> {

                        /*
                                                sendCoinViewModel?.sendBTCTransactionCall(
                                                    Securities.encrypt(Wallet.getPrivateKeyData(CoinType.BITCOIN)),
                                                    if (!isCurrencySelected) viewDataBinding?.edtAmount?.text.toString()
                                                    else currencyPrice.toString(),
                                                    viewDataBinding?.edtContractAddress?.text.toString(),
                                                    "mainnet",
                                                    Wallet.getPublicWalletAddress(CoinType.BITCOIN)!!
                                                )
                        */




                        if (!isCurrencySelected && viewDataBinding?.edtAmount?.text.toString()
                                .toBigDecimal() > args.tokenModel.t_balance.toBigDecimal()
                        ) {
                            requireContext().showToast(INVALID_BALANCE)
                        } else if (isCurrencySelected && currencyPrice!! > args.tokenModel.t_balance.toBigDecimal()) {
                            requireContext().showToast(INVALID_BALANCE)
                        } else {

                            if (args.tokenModel.chain?.coinType == CoinType.BITCOIN) {
                                sendCoinViewModel?.sendBTCTransactionCall(
                                    Securities.encrypt(Wallet.getPrivateKeyData(CoinType.BITCOIN)),
                                    if (!isCurrencySelected) viewDataBinding?.edtAmount?.text.toString()
                                    else currencyPrice.toString(),
                                    viewDataBinding?.edtContractAddress?.text.toString(),
                                    "mainnet",
                                    Wallet.getPublicWalletAddress(CoinType.BITCOIN)!!
                                )
                            } else {
                                lifecycleScope.launch(Dispatchers.IO) {
                                    tokenList = tokenViewModel.getAllTokensList()
                                    requireActivity().runOnUiThread {
                                        sendCoinViewModel?.isFromLaverageChange?.value = false
                                        sendCoinViewModel?.customGasPrice?.value = 0.toBigInteger()
                                        sendCoinViewModel?.customGasLimit?.value = "0"
                                        sendCoinViewModel?.customNonce?.value = "0"
                                        sendCoinViewModel?.customTransactionData?.value = ""
                                        openSendTransferPreviewDialog()
                                    }
                                }
                            }

                        }


                    }
                }
            }

            txtAddAddressContact.setSafeOnClickListener {
                openAddAddressDialog()
            }

            imgContact.setSafeOnClickListener {
                findNavController().navigate(
                    SendCoinDirections.actionSendCoinToContactListFragment(
                        true
                    )
                )
            }
        }

    }

    private fun openSendTransferPreviewDialog() {

        lastSelectedSlippage = 2
        sendCoinViewModel.setCoinDetail(
            viewDataBinding?.edtContractAddress?.text.toString(),
            if (!isCurrencySelected) viewDataBinding?.edtAmount?.text.toString()
                .toBigDecimal() else currencyPrice!!,
            args.tokenModel,
            tokenList,
            viewDataBinding?.txtConvertBalance?.text.toString()
        )

        SendTransferPreviewDialog(listner = object :
            SendTransferPreviewDialog.DialogOnClickBtnListner {
            override fun onConfirmClickListner() {
                requireActivity().runOnUiThread {
                    if (preferenceHelper.isTransactionSignIn) {
                        if (preferenceHelper.isAppLock) {
                            if (!preferenceHelper.isLockModePassword) {
                                setBioMetric(biometricListener)
                            } else
                                DeviceLockFullScreenDialog.getInstance().show(
                                    requireContext(),
                                    object : DeviceLockFullScreenDialog.DialogOnClickBtnListner {
                                        override fun onSubmitClicked(selectedList: String) {
                                            sendTransactionCall()
                                        }
                                    })

                        } else {
                            sendTransactionCall()
                        }
                    } else {
                        sendTransactionCall()
                    }
                }
            }

            override fun onDismissClickListner() {

            }

            override fun onSettingClick(transactionNetworkModel: TransferNetworkDetail?) {

            }

        }).show(childFragmentManager, "Send")
    }

    private fun openAddAddressDialog() {
        AddContactDialog.getInstance().show(
            requireContext(),
            mutableListOf(),
            object : AddContactDialog.DialogOnClickBtnListner {
                override fun onSubmitClicked(name: String) {
                    contactViewModel.executeInsertTokens(
                        ContactModel(
                            name = name,
                            address = viewDataBinding?.edtContractAddress?.text.toString()
                        )
                    )
                }
            })
    }

    override fun setupObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                contactViewModel.contactsSpecificResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            hideLoader()
                            //detail page

                            viewDataBinding?.txtAddAddressContact?.visibility = GONE

                        }

                        is NetworkState.Loading -> {

                        }

                        is NetworkState.Error -> {
                            viewDataBinding?.txtAddAddressContact?.visibility = VISIBLE
                        }

                        is NetworkState.SessionOut -> {}

                        else -> {

                        }
                    }
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                contactViewModel.insertContactResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            hideLoader()
                            //detail page
                            if (it.data != null) {
                                Toast.makeText(requireContext(), "Success", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }

                        is NetworkState.Loading -> {
                            requireContext().showLoader()
                        }

                        is NetworkState.Error -> {
                            hideLoader()
                        }

                        is NetworkState.SessionOut -> {}

                        else -> {
                            hideLoader()
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                graphDetailViewModel.getGetMarketResponse.collect { networkState ->
                    when (networkState) {
                        is NetworkState.Success -> {
                            val cryptoList = networkState.data
                            if (cryptoList?.isNotEmpty() == true) {
                                val cryptoModel = cryptoList[0]

                                viewDataBinding?.model?.t_price = cryptoModel.current_price
                                viewDataBinding?.model?.t_last_price_change_impact =
                                    cryptoModel.price_change_percentage_24h

                                lifecycleScope.launch(Dispatchers.IO) {
                                    viewDataBinding?.model?.callFunction?.getBalance {

                                        viewDataBinding?.model?.t_balance = it.toString()

                                    }
                                }

                            }
                            hideLoader()

                        }

                        is NetworkState.Loading -> {
                            requireContext().showLoader()
                        }

                        is NetworkState.Error -> {
                            hideLoader()

                        }

                        is NetworkState.SessionOut -> {
                            hideLoader()
                            CustomSnackbar.make(
                                requireActivity().window.decorView.rootView as ViewGroup,
                                networkState.message.toString()
                            ).show()
                        }

                        else -> {
                            hideLoader()
                        }
                    }
                }
            }
        }


        GlobalScope.launch(Dispatchers.IO) {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                sendCoinViewModel.setWalletActive.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            //  hideLoader()

                        }

                        is NetworkState.Loading -> {

                        }

                        is NetworkState.Error -> {
                            //  hideLoader()
                        }

                        is NetworkState.SessionOut -> {}

                        else -> {
                            //  hideLoader()
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                sendCoinViewModel.sendBTCTransactionResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            /* if (!PreferenceHelper.getInstance().isActiveWallet) {
                                 sendCoinViewModel.setWalletActiveCall(
                                     Wallet.getPublicWalletAddress(
                                         CoinType.BITCOIN
                                     )!!, viewDataBinding?.edtContractAddress?.text.toString()
                                 )
                             }*/

                            sendCoinViewModel.setWalletActiveCall(
                                Wallet.getPublicWalletAddress(
                                    CoinType.BITCOIN
                                )!!, viewDataBinding?.edtContractAddress?.text.toString()
                            )


                            Handler(Looper.getMainLooper()).postDelayed(5000) {
                                requireContext().showToast(it.toString())
                                findNavController().safeNavigate(SendCoinDirections.actionSendCoinToDashboard())
                                hideLoader()

                            }

                        }

                        is NetworkState.Loading -> {
                            requireContext().showLoaderAnyHow()
                        }

                        is NetworkState.Error -> {
                            hideLoader()
                            requireContext().showToast(it.message.toString())
                        }

                        is NetworkState.SessionOut -> {}

                        else -> {
                            hideLoader()
                        }
                    }
                }
            }
        }


    }

    private fun sendTransaction(
        amount: Double,
        address: String,
        isFromCustomised: Boolean = sendCoinViewModel.isFromLaverageChange.value!!
    ) {

        viewDataBinding!!.btnContinue.enableDisableButton(false)
        requireContext().showLoaderAnyHow()
        if (!isFromCustomised) {
            loge("Amount", "==> $amount")
            args.tokenModel.callFunction.sendTokenOrCoin(
                address, amount, tokenList
            ) { success, errorMessage, code ->

                if (success) {
                    requireActivity().runOnUiThread {
                        sendCoinViewModel.setWalletActiveCall(
                            Wallet.getPublicWalletAddress(
                                CoinType.ETHEREUM
                            )!!, address
                        )


                        Handler(Looper.getMainLooper()).postDelayed(5000) {
                            requireContext().showToast("Success")
                            findNavController().safeNavigate(SendCoinDirections.actionSendCoinToDashboard())
                            hideLoader()
                            viewDataBinding?.btnContinue?.enableDisableButton(true)
                        }

                    }

                } else {
                    requireActivity().runOnUiThread {
                        hideLoader()
                        if (code?.isEmpty() == true) {
                            requireContext().showToast(errorMessage.toString())
                        } else {
                            requireContext().showToast(errorMessage.toString())
                        }
                        viewDataBinding?.btnContinue?.enableDisableButton(true)
                    }
                }

            }

        } else {
            CoroutineScope(Dispatchers.Main).launch {
                args.tokenModel.callFunction.sendTokenOrCoinWithLavrageFee(
                    receiverAddress = address,
                    tokenAmount = amount,
                    nonce = stringToBigInteger(sendCoinViewModel.customNonce.value!!),
                    gasAmount = bigIntegerToString(sendCoinViewModel.customGasPrice.value!!),
                    gasLimit = stringToBigInteger(sendCoinViewModel.customGasLimit.value!!),
                    decimal = sendCoinViewModel.decimal.value!!,
                    tokenList = tokenList
                ) { success, errorMessage, code ->

                    if (success) {
                        requireActivity().runOnUiThread {
                            sendCoinViewModel.setWalletActiveCall(
                                Wallet.getPublicWalletAddress(
                                    CoinType.ETHEREUM
                                )!!, address
                            )

                            Handler(Looper.getMainLooper()).postDelayed(5000) {
                                requireContext().showToast("Success")
                                findNavController().safeNavigate(SendCoinDirections.actionSendCoinToDashboard())
                                hideLoader()
                                viewDataBinding?.btnContinue?.enableDisableButton(true)
                            }

                        }

                    } else {
                        requireActivity().runOnUiThread {
                            hideLoader()
                            if (code?.isEmpty() == true) {
                                requireContext().showToast(errorMessage.toString())
                            } else {
                                requireContext().showToast(errorMessage.toString())
                            }
                            viewDataBinding?.btnContinue?.enableDisableButton(true)
                        }
                    }


                }
            }

        }
    }


    private fun sendTransactionCall() {
        sendTransaction(
            if (!isCurrencySelected) viewDataBinding?.edtAmount?.text.toString()
                .toDouble() else currencyPrice!!.toDouble(),
            viewDataBinding?.edtContractAddress?.text.toString()
        )
    }


    private fun handleQRCodeResult(result: String) {

        loge("QR", result)

        val qrResult = extractQRCodeScannerInfo(result)


        loge("qrResult", "${qrResult?.first}  :: ${qrResult?.second} :: ${qrResult?.third}")


        if (qrResult?.first != null) {
            // viewDataBinding!!.edtContractAddress.setText(cleanEthereumAddress(qrResult.first))
            viewDataBinding!!.edtContractAddress.setText(qrResult.first)
        } else {
            //  viewDataBinding!!.edtContractAddress.setText(cleanEthereumAddress(result))
            viewDataBinding!!.edtContractAddress.setText(result)
        }

        if (qrResult?.second != null && qrResult.second.isNotBlank()) {
            handleCurrencySelection(qrResult)
        } else {
            viewDataBinding!!.edtAmount.setText("")
        }

        contactViewModel.getSpecificContactList(viewDataBinding!!.edtContractAddress.text.toString())
    }

    private fun handleCurrencySelection(qrResult: Triple<String, String, String>) {

        //  isCurrencySelected = !isCurrencySelected

        loge("", "")

        if (isCurrencySelected) {
            viewDataBinding!!.txtMax.visibility = View.GONE
            viewDataBinding!!.txtCoinType.text = selectedCurrency?.code
        } else {
            viewDataBinding!!.txtMax.visibility = View.VISIBLE
            viewDataBinding!!.txtCoinType.text = viewDataBinding!!.model?.t_symbol
        }

        if (qrResult.third.lowercase().trim() == args.tokenModel.t_address?.lowercase()?.trim()
            || args.tokenModel.chain?.name?.lowercase()?.replace(" ", "")
                ?.contains(qrResult.third.lowercase().trim(), ignoreCase = true) == true
        ) {
            loge("handleValidQRCode", "handleValidQRCode")
            handleValidQRCode(qrResult)
        } else {

            loge("handleInvalidQRCode", "handleInvalidQRCode")
            handleInvalidQRCode(qrResult)
        }
    }

    private fun handleValidQRCode(qrResult: Triple<String, String, String>) {
        if (isScientificNotation(qrResult.second)) {
            val decimalValue = try {
                BigDecimal(qrResult.second).setScale(10, RoundingMode.DOWN).stripTrailingZeros()
            } catch (e: Exception) {
                BigDecimal.ZERO // If invalid input, use 0
            }

            val weiToEther = convertWeiToEther(decimalValue.toString(), 18)

            viewDataBinding!!.edtAmount.setText(
                HtmlCompat.fromHtml(
                    formatDecimal(weiToEther),
                    0
                )
            )
        } else {

            viewDataBinding!!.edtAmount.setText(
                HtmlCompat.fromHtml(
                    qrResult.second,
                    0
                )
            )
        }
    }

    private fun handleInvalidQRCode(qrResult: Triple<String, String, String>) {
        if (qrResult.first != "") {
            viewDataBinding!!.edtContractAddress.setText(qrResult.first)
        }
        viewDataBinding!!.edtAmount.setText("")
    }

    /*
        suspend fun resolveENSName(ensName: String ="web3j.eth"): String? {

            */
    /*  var mutableList = mutableListOf(1,"",true,1.5)
              var arraList = arrayListOf(1,"",)*//*



        val web3j = Web3j.build(HttpService("wss://ethereum.publicnode.com"))
        val resolver = EnsResolver(web3j)

        return resolver.resolve(ensName)


    }
*/

}