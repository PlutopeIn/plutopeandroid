package com.app.plutope.ui.fragment.transactions.send.send_coin

import android.content.ClipboardManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
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
import com.app.plutope.model.TokenDetail
import com.app.plutope.model.TokenInfo
import com.app.plutope.model.Tokens
import com.app.plutope.model.TransactionType
import com.app.plutope.model.TransferTraceDetail
import com.app.plutope.model.Wallet
import com.app.plutope.networkConfig.getWalletAddress
import com.app.plutope.ui.base.BaseActivity
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.ui.fragment.contact.ContactListFragment
import com.app.plutope.ui.fragment.contact.ContactListViewModel
import com.app.plutope.ui.fragment.token.TokenViewModel
import com.app.plutope.ui.fragment.transactions.buy.graph.GraphDetailViewModel
import com.app.plutope.utils.BitcoinAddressValidator
import com.app.plutope.utils.Securities
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
import com.app.plutope.utils.extras.PreferenceHelper
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
import com.app.plutope.utils.setBalanceText
import com.app.plutope.utils.showLoader
import com.app.plutope.utils.showLoaderAnyHow
import com.app.plutope.utils.showSuccessToast
import com.app.plutope.utils.showToast
import com.app.plutope.utils.stringToBigInteger
import com.bumptech.glide.Glide
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanQRCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bitcoinj.core.Address
import org.bitcoinj.core.Coin
import org.bitcoinj.core.ECKey
import org.bitcoinj.core.Utils
import org.bitcoinj.kits.WalletAppKit
import org.bitcoinj.params.MainNetParams
import org.web3j.crypto.WalletUtils
import java.io.File
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.util.concurrent.CompletableFuture

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

    private var isSwitchOn = false

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

    /* private fun toggleSwitch(isOn: Boolean) {
         isSwitchOn = isOn
         updateSwitchState()
     }

     private fun updateSwitchState() {
         if (isSwitchOn) {
             viewDataBinding?.switchButton!!.x =
                 (viewDataBinding!!.switchBackground.width - viewDataBinding!!.switchButton.width).toFloat()
             viewDataBinding!!.switchBackground.setBackgroundResource(R.drawable.switch_background_on)
             viewDataBinding!!.switchButton.setBackgroundResource(R.drawable.switch_button_on)
             viewDataBinding!!.switchBackground.text = "ON"
             viewDataBinding!!.switchBackground.setTextColor(resources.getColor(android.R.color.white))
         } else {
             viewDataBinding!!.switchButton.x = 0f
             viewDataBinding!!.switchBackground.setBackgroundResource(R.drawable.switch_background_on)
             viewDataBinding!!.switchButton.setBackgroundResource(R.drawable.switch_button_off)
             viewDataBinding!!.switchBackground.text = "OFF"
             viewDataBinding!!.switchBackground.setTextColor(resources.getColor(android.R.color.black))
         }
     }*/


    override fun setupUI() {

        loge("Send", "Token : ${args.tokenModel}")


        /* viewDataBinding?.switchButton?.setOnTouchListener { v, event ->
             when (event.action) {
                 MotionEvent.ACTION_MOVE -> {
                     val x = event.rawX - (viewDataBinding?.switchButton!!.width / 2)
                     if (x < 0) {
                         viewDataBinding?.switchButton!!.x = 0f
                     } else if (x > viewDataBinding?.switchBackground!!.width - viewDataBinding?.switchButton!!.width) {
                         viewDataBinding?.switchButton!!.x =
                             (viewDataBinding?.switchBackground!!.width - viewDataBinding?.switchButton!!.width).toFloat()
                     } else {
                         viewDataBinding?.switchButton!!.x = x
                     }
                     true
                 }

                 MotionEvent.ACTION_UP -> {
                     toggleSwitch(viewDataBinding?.switchButton!!.x > viewDataBinding?.switchBackground!!.width / 2)
                     true
                 }

                 else -> false
             }
         }*/




        setTokenDetails()

        CoroutineScope(Dispatchers.Main).launch {
            args.tokenModel.callFunction.getBalance {
                args.tokenModel.t_balance = it!!
            }
        }

        viewDataBinding!!.txtToolbarTitle.text =
            "${context?.getString(R.string.send)} ${args.tokenModel.t_symbol}"
        viewDataBinding!!.model = args.tokenModel
        selectedCurrency = preferenceHelper.getSelectedCurrency()

        viewDataBinding!!.txtCurrencyType.text = selectedCurrency?.code

        /*CoroutineScope(Dispatchers.Main).launch {
            loge("ENS","Address => ${resolveENSName()}")
        }*/

        requireContext().hideKeyboard(viewDataBinding!!.root, viewDataBinding!!.edtAmount)

        if (args.tokenModel.chain?.coinType == CoinType.BITCOIN) {
            viewDataBinding!!.btnContinue.text = getString(R.string.send_bitcoin)
        } else {
            viewDataBinding!!.btnContinue.text = getString(R.string.next)
            if (args.result.trim() != "") {


                handleQRCodeResult(args.result)
            }

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

    private fun setTokenDetails() {
        val tokenModel = args.tokenModel

        PreferenceHelper.getInstance().getSelectedCurrency()?.symbol ?: ""
        if (tokenModel.isCustomTokens) {
            val img = when (tokenModel.t_type.lowercase()) {
                "erc20" -> R.drawable.ic_erc
                "bep20" -> R.drawable.ic_bep
                "polygon" -> R.drawable.ic_polygon
                "kip20" -> R.drawable.ic_kip
                else -> {
                    R.drawable.ic_erc
                }
            }
            Glide.with(requireContext()).load(img).into(viewDataBinding?.imgCoin!!)

        } else {

            val imageUrl =
                if (tokenModel.t_logouri != "" || tokenModel.t_logouri.isNotEmpty()) tokenModel.t_logouri else tokenModel.chain?.icon

            val img = when (tokenModel.t_type.lowercase()) {
                "erc20" -> R.drawable.img_eth_logo
                "bep20" -> R.drawable.ic_bep
                "polygon" -> R.drawable.ic_polygon
                "kip20" -> R.drawable.ic_kip
                else -> {
                    R.drawable.img_eth_logo
                }
            }


            Glide.with(viewDataBinding?.imgCoin!!.context).load(imageUrl)
                .placeholder(img)
                .error(img)
                .into(viewDataBinding?.imgCoin!!)

            // Glide.with(requireContext()).load(tokenModel.t_logouri).into(viewDataBinding?.imgCoin!!)
        }

        // viewDataBinding?.txtToolbarTitle?.text = tokenModel.t_name

        viewDataBinding?.txtBalance?.text = setBalanceText(
            tokenModel.t_balance.toBigDecimal(),
            tokenModel.t_symbol.toString(),
            7
        )
        viewDataBinding?.txtNetworkName?.text = tokenModel.t_type

        val priceDouble = tokenModel.t_price.toDoubleOrNull() ?: 0.0
        val priceText = String.format("%.2f", priceDouble)
        val percentChange = tokenModel.t_last_price_change_impact.toDoubleOrNull() ?: 0.0
        val color = if (percentChange < 0.0) context?.resources!!.getColor(
            R.color.red,
            null
        ) else context?.resources!!.getColor(R.color.green_00A323, null)

        val pricePercent = if (percentChange < 0.0) String.format(
            "%.2f",
            percentChange
        ) else "+" + String.format("%.2f", percentChange)

        viewDataBinding?.txtPrice?.text =
            "1 ${tokenModel.t_symbol} = " + preferenceHelper.getSelectedCurrency()?.symbol + "" + priceText

        /* viewDataBinding?.txtCryptoDiffrencePercentage?.text = "$pricePercent%"
         viewDataBinding?.txtCryptoDiffrencePercentage?.setTextColor(color)*/


        if (isCurrencySelected) {
            viewDataBinding!!.txtMax.visibility = GONE
            viewDataBinding!!.maxUnderline.visibility = GONE
            viewDataBinding!!.txtCoinType.text = selectedCurrency?.code
            viewDataBinding!!.txtCurrencyType.text = viewDataBinding!!.model?.t_symbol

        } else {
            viewDataBinding!!.txtMax.visibility = VISIBLE
            viewDataBinding!!.maxUnderline.visibility = VISIBLE
            viewDataBinding!!.txtCoinType.text = viewDataBinding!!.model?.t_symbol
            viewDataBinding!!.txtCurrencyType.text = selectedCurrency?.code
        }


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
                                amt.toDouble().toBigDecimal(),
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


                        convertPrice = convertedEstPrice.toDouble()
                        if (!isCurrencySelected) {

                            viewDataBinding?.txtConvertBalance?.text =
                                if (s.toString().isNotEmpty() && args.tokenModel.t_price.toString()
                                        .isNotEmpty()
                                ) "= " + selectedCurrency?.symbol + "" + convertPrice else "0"
                        } else {

                            viewDataBinding?.txtConvertBalance?.text =
                                if (s.toString().isNotEmpty() && args.tokenModel.t_price.toString()
                                        .isNotEmpty()
                                ) "= $currencyPrice" + " ${args.tokenModel.t_symbol}" else "0"

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
                viewDataBinding?.edtAmount?.setSelection(viewDataBinding!!.edtAmount.length())

            }

            layoutSwap.setOnClickListener {
                isCurrencySelected = !isCurrencySelected
                viewDataBinding?.edtAmount?.setText("")
                loge("isCurrencySelected", "=>$isCurrencySelected")
                if (isCurrencySelected) {
                    viewDataBinding!!.txtMax.visibility = GONE
                    viewDataBinding!!.maxUnderline.visibility = GONE
                    viewDataBinding!!.txtCoinType.text = selectedCurrency?.code
                    viewDataBinding!!.txtCurrencyType.text = viewDataBinding!!.model?.t_symbol


                    viewDataBinding!!.amountBtc.text =
                        getString(R.string.amount_label) + " " + selectedCurrency?.code
                } else {
                    viewDataBinding!!.txtMax.visibility = VISIBLE
                    viewDataBinding!!.maxUnderline.visibility = VISIBLE
                    viewDataBinding!!.txtCoinType.text = viewDataBinding!!.model?.t_symbol
                    viewDataBinding!!.txtCurrencyType.text = selectedCurrency?.code

                    viewDataBinding!!.amountBtc.text =
                        getString(R.string.amount_label) + " " + model?.t_symbol

                }

            }

            viewDataBinding?.txtCurrencyType?.setOnClickListener {
                layoutSwap.performClick()
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


                                /*val senderPrivateKey = Wallet.getPrivateKeyData(CoinType.BITCOIN)
                                val recipientAddress = viewDataBinding?.edtContractAddress?.text.toString()

                                val valueInSatoshi = btcToSatoshi(if (!isCurrencySelected) viewDataBinding?.edtAmount?.text.toString().toDouble()
                                else currencyPrice.toString().toDouble())

                                loge("bTCTOSATOSHI","${valueInSatoshi}")

                                val amountToSend =
                                    Coin.valueOf(valueInSatoshi) // Amount in satoshis (e.g., 0.001 BTC)

                                sendBitcoinTransaction(
                                    senderPrivateKey,
                                    recipientAddress,
                                    amountToSend
                                )*/


                            } else {
                                lifecycleScope.launch(Dispatchers.IO) {
                                    tokenList = tokenViewModel.getAllTokensList()
                                    requireActivity().runOnUiThread {
                                        sendCoinViewModel?.isFromLaverageChange?.value = false
                                        sendCoinViewModel?.customGasPrice?.value = 0.toBigDecimal()
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

            imgBack.setSafeOnClickListener {
                findNavController().popBackStack()
            }
        }

    }

    fun btcToSatoshi(btcAmount: Double): Long {
        val satoshiPerBitcoin = 100_000_000
        return (btcAmount * satoshiPerBitcoin).toLong()
    }

    private fun openSendTransferPreviewDialog() {

        lastSelectedSlippage = 0
        sendCoinViewModel.setCoinDetail(
            viewDataBinding?.edtContractAddress?.text.toString(),
            if (!isCurrencySelected) viewDataBinding?.edtAmount?.text.toString()
                .toBigDecimal() else currencyPrice!!,
            args.tokenModel,
            tokenList,
            viewDataBinding?.txtConvertBalance?.text.toString()
        )

        SendTransferPreviewDialog.newInstance(dialogDismissListner = object :
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
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                contactViewModel.insertContactResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            hideLoader()
                            //detail page
                            if (it.data != null) {
                                if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                                    showSuccessToast("Contact add successfully")
                                }
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

                                viewDataBinding?.model?.t_price = cryptoModel.current_price ?: "0"
                                viewDataBinding?.model?.t_last_price_change_impact =
                                    cryptoModel.price_change_percentage_24h ?: "0"

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
            ) { success, errorMessage, transactionHash ->

                if (success) {
                    requireActivity().runOnUiThread {

                        sendWholeTransactionTraceDetail(
                            TransferTraceDetail(
                                walletAddress = getWalletAddress(CoinType.ETHEREUM)!!,
                                transactionType = TransactionType.SEND.value,
                                providerType = "",
                                transactionHash = transactionHash!!,
                                requestId = "",
                                tokenDetailArrayList = arrayListOf(
                                    TokenDetail(
                                        from = TokenInfo(
                                            chainId = args.tokenModel.chain!!.chainIdHex,
                                            address = getWalletAddress(
                                                args.tokenModel.chain?.coinType ?: CoinType.ETHEREUM
                                            )!!,
                                            symbol = args.tokenModel.t_symbol
                                        ),
                                        to = TokenInfo(
                                            chainId = args.tokenModel.chain!!.chainIdHex,
                                            address = address,
                                            symbol = args.tokenModel.t_symbol
                                        )
                                    )
                                )
                            )
                        )



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
                        if (transactionHash?.isEmpty() == true) {
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
                    gasAmount = sendCoinViewModel.customGasPrice.value!!.toPlainString(),
                    gasLimit = stringToBigInteger(sendCoinViewModel.customGasLimit.value!!),
                    decimal = sendCoinViewModel.decimal.value!!,
                    tokenList = tokenList
                ) { success, errorMessage, transactionHash ->

                    if (success) {
                        requireActivity().runOnUiThread {

                            sendWholeTransactionTraceDetail(
                                TransferTraceDetail(
                                    walletAddress = getWalletAddress(CoinType.ETHEREUM)!!,
                                    transactionType = TransactionType.SEND.value,
                                    providerType = "",
                                    transactionHash = transactionHash!!,
                                    requestId = "",
                                    tokenDetailArrayList = arrayListOf(
                                        TokenDetail(
                                            from = TokenInfo(
                                                chainId = args.tokenModel.chain!!.chainIdHex,
                                                address = getWalletAddress(
                                                    args.tokenModel.chain?.coinType
                                                        ?: CoinType.ETHEREUM
                                                )!!,
                                                symbol = args.tokenModel.t_symbol
                                            ),
                                            to = TokenInfo(
                                                chainId = args.tokenModel.chain!!.chainIdHex,
                                                address = address,
                                                symbol = args.tokenModel.t_symbol
                                            )
                                        )
                                    )
                                )
                            )

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
                            if (transactionHash?.isEmpty() == true) {
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
            viewDataBinding?.edtContractAddress?.setSelection(viewDataBinding!!.edtContractAddress.length())
        } else {
            //  viewDataBinding!!.edtContractAddress.setText(cleanEthereumAddress(result))
            viewDataBinding!!.edtContractAddress.setText(result)
            viewDataBinding?.edtContractAddress?.setSelection(viewDataBinding!!.edtContractAddress.length())
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


        if (isCurrencySelected) {
            viewDataBinding!!.txtMax.visibility = GONE
            viewDataBinding!!.maxUnderline.visibility = GONE
            viewDataBinding!!.txtCoinType.text = selectedCurrency?.code
            viewDataBinding!!.txtCurrencyType.text = viewDataBinding!!.model?.t_symbol


        } else {
            viewDataBinding!!.txtMax.visibility = VISIBLE
            viewDataBinding!!.maxUnderline.visibility = VISIBLE
            viewDataBinding!!.txtCoinType.text = viewDataBinding!!.model?.t_symbol
            viewDataBinding!!.txtCurrencyType.text = selectedCurrency?.code
        }

        if (qrResult.third.lowercase().trim() == args.tokenModel.t_address.lowercase().trim()
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


    fun sendBitcoinTransaction(senderPrivateKey: String, recipientAddress: String, amount: Coin) {
        try {
            val params = MainNetParams.get()
            val kit = WalletAppKit(params, File("."), "endrequest-example")
            //WalletAppKit.launch(TestNet3Params.get(), File("."), "sendrequest-example")

            /* kit.startAsync()
             kit.awaitRunning()*/

            val wallet = kit.wallet()
            val privateKey = ECKey.fromPrivate(Utils.HEX.decode(senderPrivateKey))
            wallet.importKey(privateKey)

            val recipient = Address.fromString(params, recipientAddress)

            try {
                val result: org.bitcoinj.wallet.Wallet.SendResult =
                    kit.wallet().sendCoins(kit.peerGroup(), recipient, amount)
                println("coins sent. transaction hash: ${result.tx.txId}")
            } catch (e: org.bitcoinj.wallet.Wallet.CouldNotAdjustDownwards) {
                println("Not enough coins in your wallet. Missing ${e.message} satoshis are missing (including fees)")
                println("Send money to: ${kit.wallet().currentReceiveAddress()}")

                val balanceFuture: CompletableFuture<Coin> = kit.wallet().getBalanceFuture(
                    amount,
                    org.bitcoinj.wallet.Wallet.BalanceType.AVAILABLE
                ) as CompletableFuture<Coin>
                balanceFuture.whenComplete { balance, throwable ->
                    if (balance != null) {
                        println("coins arrived and the wallet now has enough balance")
                    } else {
                        println("something went wrong")
                    }
                }
            }

            kit.stopAsync()
            kit.awaitTerminated()


            /* val sendRequest = SendRequest.to(recipient, amount)
             wallet.completeTx(sendRequest)
             wallet.commitTx(sendRequest.tx)
             loge("TXData", "${sendRequest.tx}")

             kit.stopAsync()
             kit.awaitTerminated()*/


        } catch (e: Exception) {
            // Handle exceptions appropriately
            loge("Error", "Exception: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun sendWholeTransactionTraceDetail(
        transferModel: TransferTraceDetail
    ) {
        loge("TransferModel", "transferModel = > ${Gson().toJson(transferModel)}")
        tokenViewModel.traceActivityLogCall(transferModel)

    }


}