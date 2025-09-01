package com.app.plutope.ui.fragment.transactions.buy.buy_btc

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.custom_views.CustomKeyboardView
import com.app.plutope.databinding.FragmentBuyBTCBinding
import com.app.plutope.model.CoinCode
import com.app.plutope.model.CurrencyModel
import com.app.plutope.model.Wallet
import com.app.plutope.networkConfig.Chain
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.ui.fragment.currency.Currency
import com.app.plutope.ui.fragment.providers.ProviderModel
import com.app.plutope.ui.fragment.providers.Providers
import com.app.plutope.utils.coinTypeEnum.CoinType
import com.app.plutope.utils.constant.BASE_URL_PLUTO_PE_IMAGES
import com.app.plutope.utils.decimalInputFilter2
import com.app.plutope.utils.enableDisableButton
import com.app.plutope.utils.findDecimalFromString
import com.app.plutope.utils.hideLoader
import com.app.plutope.utils.loge
import com.app.plutope.utils.network.NetworkState
import com.app.plutope.utils.safeNavigate
import com.app.plutope.utils.showToast
import com.bumptech.glide.Glide
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.math.RoundingMode


@AndroidEntryPoint
class BuyBTC : BaseFragment<FragmentBuyBTCBinding, BuyBTCViewModel>() {

    private var networkType: String = ""
    private var fiatType: String = ""
    private var providerList: ArrayList<ProviderModel> = arrayListOf()
    private var selectedValue: ProviderModel? = null
    private var selectedCurrencyValue: CurrencyModel? = null
    private val buyBTCViewModel: BuyBTCViewModel by viewModels()
    private var input: String = ""
    private val args: BuyBTCArgs by navArgs()
    private var myWalletAddress: String = ""
    var apiCount = 0
    var isApiCalled = false
    private var isProgrammaticChange = false
    private var isFirstTime: Boolean = true
    private var tokenDecimal: Int? = 18
    var latestEnteredValue = "0"


    companion object {
        const val BIG_TEXT_SIZE = 40.0F
        const val MEDIUM_TEXT_SIZE = 28.0F
        const val SMALL_TEXT_SIZE = 20.0F

        const val SIXTEEN_TEXT_SIZE = 16.0F
        const val TWELVE_TEXT_SIZE = 14.0F
        const val TEN_TEXT_SIZE = 11.0F
    }

    override fun getViewModel(): BuyBTCViewModel {
        return buyBTCViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.buyBTCViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_buy_b_t_c
    }

    override fun setupToolbarText(): String {
        return ""
    }


    override fun setupUI() {
        loge("TokenModel", "${args.tokenModel}")

        //urlForAlpyne()

        viewDataBinding?.edtPrice?.requestFocus()

        CoroutineScope(Dispatchers.Main).launch {
            args.tokenModel.callFunction.getDecimal {
                tokenDecimal = it
            }
        }

        setFragmentResultListeners()
        setResultCurrency()

        setFiatTypeNetwork()
        setOnClickListeners()
        setDetail()
        if (!isApiCalled) {
            apiCallForAllProviderBestPrice()
        }
    }


    private fun checkAllAPIsCompleted() {
        if (providerList.isNotEmpty()) {
            getBestPriceFromAllBestPrices()
        } else {
            viewDataBinding?.progressAmount?.visibility = GONE
            viewDataBinding?.txtLabelChooseProvider?.visibility = GONE
            viewDataBinding?.layoutProvider?.visibility = GONE
            viewDataBinding?.txtCoinMargin?.visibility = VISIBLE
            viewDataBinding?.txtCoinMargin?.text = getString(R.string.not_available)
            enableButton(false)
        }

    }

    @SuppressLint("SetTextI18n")
    private fun getBestPriceFromAllBestPrices() {
        viewDataBinding?.progressAmount?.visibility = GONE

        // val maxBestPriceModel = providerList.maxBy { it.bestPrice.toDouble() }
        val maxBestPriceModel = providerList[0]

        if (maxBestPriceModel.bestPrice.toDouble() <= 0.0) {
            viewDataBinding?.txtLabelChooseProvider?.visibility = GONE
            viewDataBinding?.layoutProvider?.visibility = GONE
            viewDataBinding?.txtCoinMargin?.visibility = VISIBLE
            viewDataBinding?.txtCoinMargin?.text = getString(R.string.not_available)
            enableButton(false)
        } else {
            viewDataBinding?.txtCoinMargin?.visibility = VISIBLE
            viewDataBinding?.txtLabelChooseProvider?.visibility = VISIBLE
            viewDataBinding?.layoutProvider?.visibility = VISIBLE

            enableButton(true)
            selectedValue = maxBestPriceModel
            buyBTCViewModel.holdSelectedProvider = selectedValue
            buyBTCViewModel.holdBestPrice = selectedValue?.bestPrice
            viewDataBinding!!.txtProvider.text = selectedValue?.name

            viewDataBinding!!.txtCoinMargin.text =
                "~" + selectedValue?.bestPrice?.toBigDecimal()?.setScale(6, RoundingMode.DOWN)
                    .toString() + " ${selectedValue?.symbol}"


            Glide.with(requireContext()).load(selectedValue?.providerIcon)
                .into(viewDataBinding?.imgProviderView!!)
        }
    }

    private fun apiCallForAllProviderBestPrice() {


        val currency: String?
        if (args.tokenModel.t_address != "") {
            currency = when (args.tokenModel.chain) {
                Chain.Polygon -> "${args.tokenModel.t_symbol.lowercase()}matic"
                Chain.BinanceSmartChain -> "${args.tokenModel.t_symbol.lowercase()}bsc"
                Chain.Ethereum -> args.tokenModel.t_symbol.lowercase().toString()
                Chain.OKC -> ""
                Chain.Bitcoin -> args.tokenModel.t_symbol.lowercase().toString()
                Chain.Optimism -> args.tokenModel.t_symbol.lowercase().toString()
                else -> ""
            }
        } else {
            currency = when (args.tokenModel.chain) {
                Chain.Polygon -> "${args.tokenModel.t_symbol.lowercase()}mainnet"
                Chain.BinanceSmartChain -> "${args.tokenModel.t_symbol.lowercase()}bsc"
                Chain.Ethereum -> args.tokenModel.t_symbol.lowercase()
                Chain.OKC -> ""
                Chain.Bitcoin -> args.tokenModel.t_symbol.lowercase().toString()
                Chain.Optimism -> args.tokenModel.t_symbol.lowercase().toString()
                else -> ""
            }
        }

        loge("toCurrency", "$currency")


        viewDataBinding?.progressAmount?.visibility = VISIBLE
        viewDataBinding?.txtCoinMargin?.visibility = GONE


        loge("selectedCurrencyValue", "==>$selectedCurrencyValue")
        loge(
            "selectedCurrencyValue",
            "==>${selectedCurrencyValue?.code?.substring(0, 2).toString()}"
        )

        buyBTCViewModel.buyQuoteSingleCall(
            BuyRequestModel(
                amount = if (viewDataBinding?.edtPrice?.text.toString()
                        .isNotEmpty()
                ) viewDataBinding?.edtPrice?.text.toString().toDouble() else 0.0,
                chainId = args.tokenModel.chain?.chainIdHex?.toInt(),
                chainName = args.tokenModel.chain?.chainName,
                countryCode = selectedCurrencyValue?.code?.substring(0, 2).toString(),
                currency = selectedCurrencyValue?.code.toString(),
                decimals = tokenDecimal,
                tokenAddress = args.tokenModel.t_address,
                tokenSymbol = args.tokenModel.t_symbol,
                walletAddress = Wallet.getPublicWalletAddress(args.tokenModel.chain!!.coinType)
                    ?.lowercase()
            )
        )
    }


    private fun setOnClickListeners() {
        viewDataBinding!!.imgBack.setOnClickListener {
            findNavController().navigateUp()
        }

        viewDataBinding!!.layoutProvider.setOnClickListener {
            val filterList = providerList.filterNot { it.bestPrice.toDouble() <= 0.0 }
                .sortedByDescending { it.bestPrice.toDouble() }
            filterList[0].isBestPrise = true
            apiCount = 0
            findNavController().safeNavigate(
                BuyBTCDirections.actionBuyBTCToProviders(
                    Gson().toJson(
                        filterList
                    )
                )
            )
        }

        viewDataBinding!!.txtCurrency.setOnClickListener {
            findNavController().safeNavigate(BuyBTCDirections.actionBuyBTCToCurrency(false))
        }

        viewDataBinding!!.keyboardView.findViewById<TextView>(R.id.text_cencel).text = "."
        viewDataBinding!!.keyboardView.listner = object : CustomKeyboardView.NotifyKeyListener {
            override fun getValue(value: String) {
                if (input.length < 11) {
                    apiCount = 0
                    appendInputText(value)
                    viewDataBinding!!.edtPrice.setText(input)
                } else {
                    requireContext().showToast("You have exceeds your limits")
                }
            }

            override fun removeValue() {
                apiCount = 0
                removeInputText()
                if (isProgrammaticChange)
                    isProgrammaticChange = false
                viewDataBinding!!.edtPrice.setText(input)
            }

            override fun removeAllValue() {
                if (!findDecimalFromString(input)) {
                    apiCount = 0
                    if (!input.contains("."))
                        appendInputText(".")
                    viewDataBinding!!.edtPrice.setText(input)
                }
            }
        }

        viewDataBinding!!.edtPrice.addTextChangedListener(object : TextWatcher {
            private val handler = Handler(Looper.getMainLooper())

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            @SuppressLint("SetTextI18n")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                latestEnteredValue = s.toString()

            }

            override fun afterTextChanged(s: Editable?) {
                enableButton(false)
                if (s.toString().isNotEmpty()) {

                    val inputValue = s.toString()
                    latestEnteredValue = inputValue
                    handler.removeCallbacksAndMessages(null)
                    handler.postDelayed({
                        loge("InputValue==>", latestEnteredValue)
                        if (latestEnteredValue != "") {
                            debouncingPriceCalulation()

                        } else {
                            try {
                                requireActivity().runOnUiThread {
                                    viewDataBinding?.layoutProvider?.visibility = GONE
                                    viewDataBinding?.txtLabelChooseProvider?.visibility = GONE
                                    viewDataBinding?.txtCoinMargin?.text = "Not available!"
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                    }, 2000)

                } else {
                    viewDataBinding?.layoutProvider?.visibility = GONE
                    viewDataBinding?.txtLabelChooseProvider?.visibility = GONE
                    viewDataBinding?.txtCoinMargin?.text = "Not available!"
                }
            }

        })

        viewDataBinding!!.btnNext.setOnClickListener {
            if (selectedValue != null) {
                buyBTCViewModel.setWalletActiveCall(
                    Wallet.getPublicWalletAddress(
                        CoinType.ETHEREUM
                    )!!, ""
                )
                loge("Url", "${selectedValue?.webUrl}")
                val intent = CustomTabsIntent.Builder().build()
                intent.launchUrl(requireContext(), Uri.parse(selectedValue?.webUrl?.trim()))

            } else {
                requireContext().showToast("Select Provider first!")
            }

        }
    }

    private fun debouncingPriceCalulation() {
        try {
            val inputAmount = latestEnteredValue.toDouble()
//            adjustTextSizeBasedOnLength(inputAmount)
            if (inputAmount > 0.0) {
                isApiCalled = false
                if (!isProgrammaticChange) {
                    apiCallForAllProviderBestPrice()
                    // enableButton(true)
                } else {
                    enableButton(true)
                }
                isProgrammaticChange = false
            } else {
                viewDataBinding?.layoutProvider?.visibility = GONE
                viewDataBinding?.txtLabelChooseProvider?.visibility = GONE
                viewDataBinding?.txtCoinMargin?.text = "Not available!"
                // enableButton(false)
            }
        } catch (e: NumberFormatException) {
            viewDataBinding?.layoutProvider?.visibility = GONE
            viewDataBinding?.txtLabelChooseProvider?.visibility = GONE
            viewDataBinding?.txtCoinMargin?.text = "Invalid input"
            // enableButton(false)
        }

    }

    @SuppressLint("SetTextI18n")
    private fun setFragmentResultListeners() {
        setFragmentResultListener(Providers.keyBundleProvider) { _, bundle ->
            isApiCalled = false
            // isApiCalled = true
            selectedValue = bundle.getParcelable(Providers.keyProvider) as? ProviderModel

            if (selectedValue!!.isBestPrise) {
                viewDataBinding!!.txtBestPrice.visibility = VISIBLE
            } else {
                viewDataBinding!!.txtBestPrice.visibility = GONE
            }

            viewDataBinding!!.txtProvider.text = selectedValue?.name
            buyBTCViewModel.holdSelectedProvider = selectedValue
            buyBTCViewModel.holdBestPrice = selectedValue?.bestPrice
            viewDataBinding!!.txtCoinMargin.text =
                "~" + selectedValue?.bestPrice?.toBigDecimal()?.setScale(6, RoundingMode.DOWN)
                    .toString() + " ${selectedValue?.symbol}"
            Glide.with(requireContext()).load(selectedValue?.providerIcon)
                .into(viewDataBinding?.imgProviderView!!)
        }
    }

    private fun setDetail() {
        myWalletAddress =
            Wallet.getPublicWalletAddress(args.tokenModel.chain?.coinType!!).toString()
        isProgrammaticChange = true
        if (isFirstTime) {
            input = "5200"
            viewDataBinding?.edtPrice?.setText(input)
            isFirstTime = false
        }
        enableButton(false)
        if (isApiCalled) {
            viewDataBinding?.layoutProvider?.visibility = VISIBLE
            viewDataBinding?.txtLabelChooseProvider?.visibility = VISIBLE
        } else {
            viewDataBinding?.txtLabelChooseProvider?.visibility = GONE
            viewDataBinding?.layoutProvider?.visibility = GONE
        }

        selectedCurrencyValue =
            if (selectedCurrencyValue != null) selectedCurrencyValue else preferenceHelper.getSelectedCurrency()

        viewDataBinding?.txtCurrencySymbole?.text = selectedCurrencyValue?.symbol
        viewDataBinding?.txtCurrency?.text = selectedCurrencyValue?.code

        val symbol = args.tokenModel.t_symbol.toString()
        providerList = if (selectedCurrencyValue?.code == "INR") {
            (args.tokenModel.chain?.providers as ArrayList<ProviderModel>).filter { it.coinCode.name != CoinCode.CHANGENOW.name } as ArrayList<ProviderModel>
        } else {
            args.tokenModel.chain?.providers as ArrayList<ProviderModel>
        }

        providerList.forEach {
            it.symbol = symbol
        }

        viewDataBinding!!.txtProvider.text = selectedValue?.name
        Glide.with(requireContext()).load(selectedValue?.icon)
            .into(viewDataBinding?.imgProviderView!!)

        if (args.pageType == "Buy") {
            viewDataBinding!!.txtToolbarTitle.text =
                getString(R.string.type_t_symbol, getString(R.string.buy), args.tokenModel.t_symbol)
        } else {
            viewDataBinding!!.txtToolbarTitle.text =
                getString(R.string.type_t_symbol, args.pageType, args.tokenModel.t_symbol)
        }

        val num = if (buyBTCViewModel.holdBestPrice != null) buyBTCViewModel.holdBestPrice else 0.0

        viewDataBinding!!.txtCoinMargin.text =
            getString(
                R.string.margins,
                num.toString(),
                getString(R.string.coin_margin, args.tokenModel.t_symbol)
            )

        viewDataBinding!!.edtPrice.filters = arrayOf(decimalInputFilter2)


    }

    private fun setResultCurrency() {
        setFragmentResultListener(Currency.keyBundleCurrency) { _, bundle ->
            isApiCalled = false
            selectedCurrencyValue = bundle.getParcelable(Currency.keyCurrency) as? CurrencyModel
            viewDataBinding?.txtCurrency?.text = selectedCurrencyValue?.code
            viewDataBinding?.txtCurrencySymbole?.text = selectedCurrencyValue?.symbol

            setDetail()
            setFiatTypeNetwork()
            apiCallForAllProviderBestPrice()

        }
    }

    private fun setFiatTypeNetwork() {
        fiatType = when (selectedCurrencyValue?.code) {
            "INR" -> "1"
            "TRY" -> "2"
            "AED" -> "3"
            "MXN" -> "4"
            else -> ""
        }
        networkType =
            if (args.tokenModel.t_type.lowercase().toString() == "polygon") {
                "matic20"
            } else {
                args.tokenModel.t_type.lowercase().toString()
            }
    }

    @SuppressLint("SetTextI18n")
    override fun setupObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                buyBTCViewModel.buyQuoteSingleCallResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            // hideLoader()
                            providerList.clear()
                            if (!it.data?.data.isNullOrEmpty()) {
                                val responseData = it.data?.data

                                responseData?.forEach { provider ->
                                    loge(
                                        "ImageUrl",
                                        "${BASE_URL_PLUTO_PE_IMAGES + provider?.image}"
                                    )
                                    providerList.add(
                                        ProviderModel(
                                            coinCode = CoinCode.CHANGENOW,
                                            providerName = provider?.providerName!!,
                                            name = provider.name!!,
                                            providerIcon = BASE_URL_PLUTO_PE_IMAGES + provider.image,
                                            bestPrice = provider.amount,
                                            webUrl = provider.url

                                        )
                                    )

                                }

                            }

                            checkAllAPIsCompleted()

                        }

                        is NetworkState.Loading -> {
                            // requireContext().showLoader()
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

    fun appendInputText(text: String) {
        if (text.length + input.length > 20) {
            return
        }
        input += text
    }

    /**
     * remove last characters from input values and run not input animation
     */
    fun removeInputText() {
        if (input.isEmpty()) {
            return
        }

        input = input.dropLast(1)
    }


    private fun enableButton(isValid: Boolean) {

        viewDataBinding?.btnNext?.enableDisableButton(isValid)

        /* if (isValid) {
             viewDataBinding!!.btnNext.apply {
                 isEnabled = true
                 background =
                     ResourcesCompat.getDrawable(resources, R.drawable.button_gradient_26, null)
                 setTextColor(ResourcesCompat.getColor(resources, R.color.bg_white, null))
             }
         } else {
             viewDataBinding!!.btnNext.apply {
                 isEnabled = false
                 background =
                     ResourcesCompat.getDrawable(resources, R.drawable.button_disable, null)
                 setTextColor(ResourcesCompat.getColor(resources, R.color.green_02303B, null))

             }

         }*/

    }

    override fun onPause() {
        super.onPause()
        isApiCalled = true
        apiCount = 0

    }


    fun adjustTextSizeBasedOnLength(inputAmount: Double) {
        val inputAmountString = inputAmount.toString()
        viewDataBinding?.apply {
            when {
                inputAmountString.length <= 8 -> {
                    edtPrice.setTextSize(
                        TypedValue.COMPLEX_UNIT_SP,
                        BIG_TEXT_SIZE
                    )
                    txtCurrencySymbole.setTextSize(
                        TypedValue.COMPLEX_UNIT_SP,
                        BIG_TEXT_SIZE
                    )
                    txtCoinMargin.setTextSize(
                        TypedValue.COMPLEX_UNIT_SP,
                        SIXTEEN_TEXT_SIZE
                    )

                }

                inputAmountString.length in 8..12 -> {
                    edtPrice.setTextSize(
                        TypedValue.COMPLEX_UNIT_SP,
                        MEDIUM_TEXT_SIZE
                    )
                    txtCurrencySymbole.setTextSize(
                        TypedValue.COMPLEX_UNIT_SP,
                        MEDIUM_TEXT_SIZE
                    )
                    txtCoinMargin.setTextSize(
                        TypedValue.COMPLEX_UNIT_SP,
                        TWELVE_TEXT_SIZE
                    )
                }

                else -> {
                    edtPrice.setTextSize(
                        TypedValue.COMPLEX_UNIT_SP,
                        SMALL_TEXT_SIZE
                    )
                    txtCurrencySymbole.setTextSize(
                        TypedValue.COMPLEX_UNIT_SP,
                        SMALL_TEXT_SIZE
                    )
                    txtCoinMargin.setTextSize(
                        TypedValue.COMPLEX_UNIT_SP,
                        TEN_TEXT_SIZE
                    )
                }
            }
        }

    }


}
