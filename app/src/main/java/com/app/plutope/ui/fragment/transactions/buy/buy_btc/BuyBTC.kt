package com.app.plutope.ui.fragment.transactions.buy.buy_btc

import android.annotation.SuppressLint
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.res.ResourcesCompat
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
import com.app.plutope.model.BuyCrypto
import com.app.plutope.model.CoinCode
import com.app.plutope.model.CurrencyModel
import com.app.plutope.model.MeldRequestModel
import com.app.plutope.model.OnMetaBestPriceModel
import com.app.plutope.model.OnRampBestPriceRequestModel
import com.app.plutope.model.Wallet
import com.app.plutope.networkConfig.Chain
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.ui.fragment.currency.Currency
import com.app.plutope.ui.fragment.providers.ProviderModel
import com.app.plutope.ui.fragment.providers.Providers
import com.app.plutope.utils.EventObserver
import com.app.plutope.utils.OnRampType
import com.app.plutope.utils.coinTypeEnum.CoinType
import com.app.plutope.utils.constant.ALCHEMY_PAY_APP_ID
import com.app.plutope.utils.constant.ALCHEMY_PAY_BEST_PRICE_URL
import com.app.plutope.utils.constant.ALCHEMY_PAY_SECRET_KEY
import com.app.plutope.utils.constant.CHANGE_NOW_BEST_PRICE
import com.app.plutope.utils.constant.ON_MELD_BEST_PRICE
import com.app.plutope.utils.constant.ON_META_API_KEY
import com.app.plutope.utils.constant.ON_META_BEST_PRICE_API
import com.app.plutope.utils.constant.ON_RAMP_BEST_PRICE_URL
import com.app.plutope.utils.constant.RAMPABLE_SECRET_KEY
import com.app.plutope.utils.constant.typeBuy
import com.app.plutope.utils.decimalInputFilter2
import com.app.plutope.utils.extras.PreferenceHelper
import com.app.plutope.utils.findDecimalFromString
import com.app.plutope.utils.generateSignature
import com.app.plutope.utils.getNetworkString
import com.app.plutope.utils.hideLoader
import com.app.plutope.utils.loge
import com.app.plutope.utils.network.NetworkState
import com.app.plutope.utils.safeNavigate
import com.app.plutope.utils.showLoader
import com.app.plutope.utils.showToast
import com.app.plutope.utils.toHexString
import com.app.plutope.utils.toISOString
import com.bumptech.glide.Glide
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.math.RoundingMode
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.Base64
import java.util.Calendar
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec


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


    fun generateSignature(timestamp: Long, apiSecret: String, apiSalt: String): String {
        // hashing algorithm to be used for sign generation
        val hashingAlgo = "HmacSHA512"

        // convert timestamp to a 10-digit long integer (timestamp in seconds)
        val partialTimestamp = timestamp.toString().substring(0, 10)

        // concatenated string of the form `{API_SECRET}{partialTimestamp}`
        // to be used to generate hash
        val finalString = apiSecret + partialTimestamp

        // HMAC created with finalString as the string to be hashed,
        // and apiSalt passed as hashing key for HMAC.
        val hmac = Mac.getInstance(hashingAlgo)
        val secretKey = SecretKeySpec(apiSalt.toByteArray(), hashingAlgo)
        hmac.init(secretKey)

        // final output as a hex string
        val signBytes = hmac.doFinal(finalString.toByteArray())
        val signHex = bytesToHex(signBytes)

        // the first 64 characters of the hex string are the signature
        return signHex.substring(0, 64)
    }

    // Helper function to convert bytes to a hexadecimal string
    fun bytesToHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        for (i in bytes.indices) {
            val v = bytes[i].toInt() and 0xFF
            hexChars[i * 2] = "0123456789ABCDEF"[v ushr 4]
            hexChars[i * 2 + 1] = "0123456789ABCDEF"[v and 0x0F]
        }
        return String(hexChars)
    }

    override fun setupUI() {

        urlForAlpyne()



        setFragmentResultListeners()
        setResultCurrency()

        setDetail()

        setFiatTypeNetwork()
        setOnClickListeners()

        if (!isApiCalled)
            apiCallForAllProviderBestPrice()
    }

    private fun urlForAlpyne() {
        val iframeUrl = "https://poc.alpyne.tech/app/v1/landing"
        val apiSecret = "uXLvobeyZAlDSaU2XvQ9ajbxaIE"
        val genSign = generateSignature(
            Calendar.getInstance().timeInMillis,
            "uXLvobeyZAlDSaU2XvQ9ajbxaIE",
            "oLywPEJRWDPPsJ8k"
        )
        val recipientAddress = Wallet.getPublicWalletAddress(CoinType.ETHEREUM)
        val vendorCode = "VENDOR_CODE"
        val inrAmount = "2500"
        val chainCode = "Ethereum"
        val tokenCode = "ETH"

        val url =
            "$iframeUrl?timestamp=${Calendar.getInstance().timeInMillis}&api_secret=$apiSecret&vendor_user_id=192839183&sign=$genSign&address=$recipientAddress&vendor_code=$vendorCode&amount=$inrAmount&chain=$chainCode&token=$tokenCode"

        loge("BuyBTC", "$url")
    }

    private fun checkAllAPIsCompleted() {
        apiCount += 1
        if (apiCount == providerList.size) {
            getBestPriceFromAllBestPrices()
        }

    }

    @SuppressLint("SetTextI18n")
    private fun getBestPriceFromAllBestPrices() {
        viewDataBinding?.progressAmount?.visibility = GONE

        val maxBestPriceModel = providerList.maxBy { it.bestPrice.toDouble() }
        if (maxBestPriceModel.bestPrice.toDouble() <= 0.0) {
            viewDataBinding?.layoutProvider?.visibility = GONE
            viewDataBinding?.txtCoinMargin?.visibility = VISIBLE
            viewDataBinding?.txtCoinMargin?.text = "Not Available!"
            enableButton(false)
        } else {
            viewDataBinding?.txtCoinMargin?.visibility = VISIBLE
            viewDataBinding?.layoutProvider?.visibility = VISIBLE

            enableButton(true)
            selectedValue = maxBestPriceModel
            buyBTCViewModel.holdSelectedProvider = selectedValue
            buyBTCViewModel.holdBestPrice = selectedValue?.bestPrice
            viewDataBinding!!.txtProvider.text = selectedValue?.providerName

            viewDataBinding!!.txtCoinMargin.text =
                "~" + selectedValue?.bestPrice?.toBigDecimal()?.setScale(6, RoundingMode.DOWN)
                    .toString() + " ${selectedValue?.symbol}"


            Glide.with(requireContext()).load(selectedValue?.icon)
                .into(viewDataBinding?.imgProviderView!!)
        }
    }

    private fun apiCallForAllProviderBestPrice() {


        viewDataBinding?.progressAmount?.visibility = VISIBLE
        viewDataBinding?.txtCoinMargin?.visibility = GONE

        val tempProvider = arrayListOf<ProviderModel>()

        if (PreferenceHelper(requireContext()).getSelectedCurrency()?.code == "INR") {
            tempProvider.addAll(providerList.filter { it.coinCode.name != CoinCode.CHANGENOW.name } as ArrayList<ProviderModel>)
        } else {
            tempProvider.addAll(providerList)
        }

        providerList.forEach {

            when (it.coinCode.name) {
                CoinCode.ONRAMP.name -> {
                    callOnRampBestPriceApi()
                }

                CoinCode.ONMETA.name -> {
                    val requestModel = OnMetaBestPriceModel(
                        args.tokenModel.t_address.toString(),
                        args.tokenModel.t_symbol.toString(),
                        args.tokenModel.chain?.chainIdHex?.toInt()!!,
                        viewDataBinding?.edtPrice?.text.toString().toDouble(),
                        selectedCurrencyValue?.code.toString()
                    )
                    buyBTCViewModel.executeOnMetaBestPrice(
                        ON_META_BEST_PRICE_API,
                        body = requestModel
                    )
                }

                CoinCode.CHANGENOW.name -> {
                    val currency: String?
                    if (args.tokenModel.t_address != "") {
                        currency = when (args.tokenModel.chain) {
                            Chain.Polygon -> "${args.tokenModel.t_symbol?.lowercase()}matic"
                            Chain.BinanceSmartChain -> "${args.tokenModel.t_symbol?.lowercase()}bsc"
                            Chain.Ethereum -> args.tokenModel.t_symbol?.lowercase().toString()
                            Chain.OKC -> ""
                            Chain.Bitcoin -> args.tokenModel.t_symbol?.lowercase().toString()
                            else -> ""
                        }
                    } else {
                        currency = when (args.tokenModel.chain) {
                            Chain.Polygon -> "${args.tokenModel.t_symbol?.lowercase()}mainnet"
                            Chain.BinanceSmartChain -> "${args.tokenModel.t_symbol?.lowercase()}bsc"
                            Chain.Ethereum -> args.tokenModel.t_symbol?.lowercase()
                            Chain.OKC -> ""
                            Chain.Bitcoin -> args.tokenModel.t_symbol?.lowercase().toString()
                            else -> ""
                        }
                    }

                    val url =
                        CHANGE_NOW_BEST_PRICE + "fromCurrency=${
                            selectedCurrencyValue?.code.toString().lowercase()
                        }&toCurrency=${/*args.tokenModel.t_symbol?.lowercase()*/currency}&fromAmount=${viewDataBinding?.edtPrice?.text.toString()}"
                    buyBTCViewModel.executeChangeNowBestPrice(url)
                }

                CoinCode.MELD.name -> {
                    val meldRequestModel = MeldRequestModel(
                        selectedCurrencyValue?.code?.substring(0, 2).toString(),
                        args.tokenModel.t_symbol.toString(),
                        viewDataBinding?.edtPrice?.text.toString(),
                        selectedCurrencyValue?.code?.toString()!!
                    )

                    buyBTCViewModel.executeOnMeldBestPrice(ON_MELD_BEST_PRICE, meldRequestModel)
                }

                CoinCode.ALCHEMYPAY.name -> {
                    callAlchemyPayApi()
                }

                CoinCode.UNLIMIT.name -> {

                    val url =
                        "https://plutope.app/api/unlimit-quote-buy?payment=BANKCARD&crypto=${args.tokenModel.t_symbol}-${args.tokenModel.t_type}&fiat=${selectedCurrencyValue?.code}&amount=${viewDataBinding?.edtPrice?.text.toString()}&region=US"
                    callUnlimitBestPrice(url)
                }

            }

        }

    }

    private fun callUnlimitBestPrice(url: String) {

        buyBTCViewModel.callUnlimiteBestPrice(url)
    }

    private fun callAlchemyPayApi() {
        val body = mapOf(
            "crypto" to args.tokenModel.t_symbol?.lowercase(),
            "network" to getAlchemyNetwork(),
            "fiat" to selectedCurrencyValue?.code,
            "country" to selectedCurrencyValue?.code?.substring(0, 2).toString(),
            "side" to "BUY",
            "amount" to viewDataBinding?.edtPrice?.text.toString(),
            "payWayCode" to "",
        )

        val currentTime = Calendar.getInstance().time
        val timestamp = currentTime.toISOString() // Current timestamp in UTC
        val bodystr = body.toString()

        // Replace 'yourSecretKey' with your actual secret key
        val secretKey = ALCHEMY_PAY_SECRET_KEY
        val signature =
            generateSignature(timestamp, "POST", "/api/v3/merchant/order/quote", bodystr, secretKey)

        // Convert timestamp to milliseconds with 13 digits
        val timestampMilliseconds = currentTime.time

        buyBTCViewModel.executeAlchemyPayBestPrice(
            signature, timestampMilliseconds.toString(),
            ALCHEMY_PAY_BEST_PRICE_URL
        )
    }

    private fun setOnClickListeners() {
        viewDataBinding!!.imgBack.setOnClickListener {
            findNavController().navigateUp()
        }

        viewDataBinding!!.layoutProvider.setOnClickListener {
            val filterList = providerList.filterNot { it.bestPrice.toDouble() <= 0.0 }
                .sortedByDescending { it.bestPrice.toDouble() }
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
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            @SuppressLint("SetTextI18n")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty()) {
                    try {
                        val inputAmount = s.toString().toDouble()
                        adjustTextSizeBasedOnLength(inputAmount)
                        if (inputAmount > 0.0) {
                            isApiCalled = false
                            if (!isProgrammaticChange) {
                                apiCallForAllProviderBestPrice()
                                // enableButton(true)
                            }
                            isProgrammaticChange = false
                        } else {
                            viewDataBinding?.layoutProvider?.visibility = View.GONE
                            viewDataBinding?.txtCoinMargin?.text = "Not available!"
                            // enableButton(false)
                        }
                    } catch (e: NumberFormatException) {
                        viewDataBinding?.layoutProvider?.visibility = View.GONE
                        viewDataBinding?.txtCoinMargin?.text = "Invalid input"
                        // enableButton(false)
                    }
                } else {
                    viewDataBinding?.layoutProvider?.visibility = View.GONE
                    viewDataBinding?.txtCoinMargin?.text = "Not available!"
                    // enableButton(false)
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        viewDataBinding!!.btnNext.setOnClickListener {
            if (selectedValue != null) {
                var url = ""
                when (selectedValue!!.coinCode.name) {
                    CoinCode.MELD.name -> {

                        url = BuyCrypto.buildURL(
                            BuyCrypto.Domain.MELD(
                                selectedCurrencyValue?.code?.substring(0, 2).toString(),
                                viewDataBinding!!.edtPrice.text.toString(),
                                selectedCurrencyValue?.code!!.toString(),
                                args.tokenModel.t_symbol.toString(),
                                myWalletAddress,
                                "WQ4Ds5T7qMmwTitbyH6eVv:6385eFN8rGk4fubQx2quWB7B7bzGhWwaMdcG",
                                networkType,
                                args.tokenModel.t_address
                            )
                        )
                    }

                    CoinCode.CHANGENOW.name -> {
                        val toCurrency = when (args.tokenModel.chain) {
                            Chain.Polygon -> "${args.tokenModel.t_symbol?.lowercase()}mainnet"
                            Chain.BinanceSmartChain -> "${args.tokenModel.t_symbol?.lowercase()}bsc"
                            Chain.Ethereum -> args.tokenModel.t_symbol?.lowercase()
                            Chain.OKC -> ""
                            else -> ""
                        }
                        url = BuyCrypto.buildURL(
                            BuyCrypto.Domain.ChangeNow(
                                "",
                                from = if (args.pageType == typeBuy) /*preferenceHelper.getSelectedCurrency()?.code.toString()*/ selectedCurrencyValue?.code!!.toString() else getNetworkString(
                                    args.tokenModel.chain
                                ),
                                to = if (args.pageType == typeBuy) toCurrency.toString() else /*preferenceHelper.getSelectedCurrency()?.code.toString()*/ selectedCurrencyValue?.code!!.toString(),
                                fiatMode = "true",
                                amount = viewDataBinding!!.edtPrice.text.toString(),
                                recipientAddress = myWalletAddress
                            )
                        )
                    }

                    CoinCode.ONMETA.name -> {
                        url = BuyCrypto.buildURL(
                            BuyCrypto.Domain.OnMeta(
                                name = "widget",
                                apiKey = ON_META_API_KEY,
                                fiatAmount = viewDataBinding!!.edtPrice.text.toString(),
                                walletAddress = myWalletAddress,
                                chainId = args.tokenModel.chain?.chainIdHex.toString(),
                                tokenAddress = args.tokenModel.t_address,
                                isBuyOrSellType = args.pageType.lowercase(),
                                tokenSymbol = args.tokenModel.t_symbol
                            )
                        )
                    }

                    CoinCode.ONRAMP.name -> {
                        url = BuyCrypto.buildURL(
                            BuyCrypto.Domain.OnRamp(
                                args.tokenModel.t_symbol.toString(),
                                myWalletAddress,
                                viewDataBinding!!.edtPrice.text.toString(),
                                network = networkType,//args.tokenModel.chain?.tokenStandard?.lowercase(),
                                isBuyOrSellType = args.pageType.lowercase()

                            )
                        )

                    }

                    CoinCode.ALCHEMYPAY.name -> {
                        val network = getAlchemyNetwork()
                        url = BuyCrypto.buildURL(
                            BuyCrypto.Domain.Alchemypay(
                                ALCHEMY_PAY_APP_ID,
                                crypto = args.tokenModel.t_symbol.toString(),
                                network = network,
                                fiat = selectedCurrencyValue?.code.toString(),
                                country = selectedCurrencyValue?.code?.substring(0, 2).toString(),
                                cryptoAmount = viewDataBinding!!.edtPrice.text.toString(),
                                walletAddress = myWalletAddress,
                                callbackUrl = ""
                            )
                        )

                    }

                    CoinCode.UNLIMIT.name -> {
                        url = BuyCrypto.buildURL(
                            BuyCrypto.Domain.Unlimit(
                                "8d16f1c7-a6d1-46ac-bdf3-621284be889b",
                                args.tokenModel.t_symbol.toString(),
                                network = networkType,//args.tokenModel.chain?.tokenStandard?.lowercase(),
                                fiatAmount = viewDataBinding!!.edtPrice.text.toString(),
                                selectedCurrencyValue!!.code,
                                myWalletAddress = myWalletAddress
                            )
                        )

                    }

                    CoinCode.RAMPABLE.name -> {
                        url = BuyCrypto.buildURL(
                            BuyCrypto.Domain.Rampable(
                                RAMPABLE_SECRET_KEY,
                                true,
                                currency = selectedCurrencyValue!!.code.uppercase(),
                                "usdc-polygon",
                                "USD",
                                inputAmount = viewDataBinding!!.edtPrice.text.toString(),
                                "0",
                                true
                            )
                        )
                    }

                    else -> {


                    }
                }


                if (!PreferenceHelper.getInstance().isActiveWallet) {
                    buyBTCViewModel.setWalletActiveCall(
                        Wallet.getPublicWalletAddress(
                            CoinType.ETHEREUM
                        )!!
                    )
                }


                /*  findNavController().safeNavigate(
                      BuyBTCDirections.actionBuyBTCToWebView(
                          url,
                          "",
                          "Rampable"
                      )
                  )*/


                /*
                                findNavController().safeNavigate(
                                    BuyBTCDirections.actionBuyBTCToBrowser(url)
                                )
                */


                val intent = CustomTabsIntent.Builder().build()
                intent.launchUrl(requireContext(), Uri.parse(url))


            } else {
                requireContext().showToast("Select Provider first!")
            }


        }
    }

    private fun getAlchemyNetwork(): String {
        return when {
            args.tokenModel.t_type?.lowercase()
                .toString() == "BEP20".lowercase() -> {
                "BSC"
            }

            args.tokenModel.t_type?.lowercase()
                .toString() == "ERC20".lowercase() -> {
                "eth"
            }

            args.tokenModel.t_type?.lowercase()
                .toString() == "POLYGON".lowercase() -> {
                "polygon"
            }

            else -> {
                ""
            }
        }
    }

    private fun callOnRampBestPriceApi() {

        val body = mapOf(
            "coinCode" to args.tokenModel.t_symbol?.lowercase(),
            "chainId" to args.tokenModel.chain?.chainIdHex?.lowercase(),
            "network" to networkType.lowercase(),
            "fiatAmount" to viewDataBinding?.edtPrice?.text.toString(),
            "fiatType" to fiatType,
            "type" to 1
        )
        val onRampBestPriceRequestModel = OnRampBestPriceRequestModel(
            args.tokenModel.chain?.chainIdHex?.lowercase().toString(),
            args.tokenModel.t_symbol?.lowercase().toString(),
            viewDataBinding?.edtPrice?.text.toString(),
            fiatType,
            networkType,
            OnRampType.onRamp.value//1
        )

        val timestamp = Instant.now().toEpochMilli()

        val payload = mapOf(
            "timestamp" to timestamp,
            "body" to body
        )
        val gson = Gson()
        val payloadJson = gson.toJson(payload)
        // Convert payload to base64
        val payloadBase64 = Base64.getEncoder().encodeToString(
            payloadJson.toByteArray(
                StandardCharsets.UTF_8
            )
        )

        // Generate the signature using the API secret
        val secret = "SQ1VFzoM0rhsR3c1raojzawQwU190AG3"
        val hmacSHA512 = Mac.getInstance("HmacSHA512")
        val secretKey = SecretKeySpec(secret.toByteArray(StandardCharsets.UTF_8), "HmacSHA512")
        hmacSHA512.init(secretKey)
        val signatureBytes = hmacSHA512.doFinal(payloadBase64.toByteArray(StandardCharsets.UTF_8))
        val signature = signatureBytes.toHexString()

        buyBTCViewModel.executeOnRampBestPrice(
            signature,
            payloadBase64,
            ON_RAMP_BEST_PRICE_URL,
            onRampBestPriceRequestModel
        )

    }


    @SuppressLint("SetTextI18n")
    private fun setFragmentResultListeners() {
        setFragmentResultListener(Providers.keyBundleProvider) { _, bundle ->
            isApiCalled = false
            selectedValue = bundle.getParcelable(Providers.keyProvider) as? ProviderModel
            viewDataBinding!!.txtProvider.text = selectedValue?.providerName
            buyBTCViewModel.holdSelectedProvider = selectedValue
            buyBTCViewModel.holdBestPrice = selectedValue?.bestPrice
            viewDataBinding!!.txtCoinMargin.text =
                "~" + selectedValue?.bestPrice?.toBigDecimal()?.setScale(6, RoundingMode.DOWN)
                    .toString() + " ${selectedValue?.symbol}"
            Glide.with(requireContext()).load(selectedValue?.icon)
                .into(viewDataBinding?.imgProviderView!!)
        }
    }

    private fun setDetail() {
        myWalletAddress =
            Wallet.getPublicWalletAddress(args.tokenModel.chain?.coinType!!).toString()

        isProgrammaticChange = true

        if (isFirstTime) {
            input = "2500"
            viewDataBinding?.edtPrice?.setText(input)
            isFirstTime = false
        }

        enableButton(false)
        if (isApiCalled) viewDataBinding?.layoutProvider?.visibility =
            VISIBLE else viewDataBinding?.layoutProvider?.visibility = GONE



        selectedCurrencyValue =
            if (selectedCurrencyValue != null) selectedCurrencyValue else preferenceHelper.getSelectedCurrency()

        viewDataBinding?.txtCurrencySymbole?.text = selectedCurrencyValue?.symbol
        viewDataBinding?.txtCurrency?.text = selectedCurrencyValue?.code

        val symbol = args.tokenModel.t_symbol.toString()

        // providerList =   args.tokenModel.chain?.providers as ArrayList<ProviderModel>

        providerList = if (selectedCurrencyValue?.code == "INR") {
            (args.tokenModel.chain?.providers as ArrayList<ProviderModel>).filter { it.coinCode.name != CoinCode.CHANGENOW.name } as ArrayList<ProviderModel>
        } else {
            args.tokenModel.chain?.providers as ArrayList<ProviderModel>
        }


        providerList.forEach {
            it.symbol = symbol
        }

        selectedValue =
            if (buyBTCViewModel.holdSelectedProvider != null)
                buyBTCViewModel.holdSelectedProvider
            else {
                if (providerList.size > 3)
                    providerList[3]
                else
                    providerList[0]
            }
        viewDataBinding!!.txtProvider.text = selectedValue?.providerName
        Glide.with(requireContext()).load(selectedValue?.icon)
            .into(viewDataBinding?.imgProviderView!!)


        viewDataBinding!!.txtToolbarTitle.text =
            getString(R.string.type_t_symbol, args.pageType, args.tokenModel.t_symbol)
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
            if (args.tokenModel.t_type?.lowercase().toString() == "polygon") {
                "matic20"
            } else {
                args.tokenModel.t_type?.lowercase().toString()
            }
    }

    @SuppressLint("SetTextI18n")
    override fun setupObserver() {
        /* lifecycleScope.launch {
             repeatOnLifecycle(Lifecycle.State.CREATED) {
                 buyBTCViewModel.getOnRampDetailResponse.collect {
                     when (it) {
                         is NetworkState.Success -> {
                             hideLoader()
                             onRampMinimumAmount =
                                 if (it.data?.data?.minimumBuyAmount?.x0 != null) it.data.data?.minimumBuyAmount?.x0!!.toDouble() else 0.0

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
         }*/

        buyBTCViewModel.unlimiBestPriceResponse.observe(
            viewLifecycleOwner,
            EventObserver { resource ->
                when (resource) {
                    is NetworkState.Success -> {
                        if (resource.data?.amountOut != null) {
                            val amount = resource.data.amountOut
                            providerList.filter { it.coinCode == CoinCode.UNLIMIT }.forEach {
                                it.bestPrice = String.format("%.6f", amount?.toDouble())
                            }

                        } else {
                            providerList.filter { it.coinCode == CoinCode.UNLIMIT }.forEach {
                                it.bestPrice = String.format(
                                    "%.6f",
                                    0.0
                                )
                            }

                        }
                        checkAllAPIsCompleted()

                    }

                    is NetworkState.Loading -> {

                    }

                    is NetworkState.Error -> {
                        providerList.filter { it.coinCode == CoinCode.UNLIMIT }.forEach {
                            it.bestPrice = String.format(
                                "%.6f",
                                0.0
                            )
                        }
                        checkAllAPIsCompleted()

                    }

                    is NetworkState.SessionOut -> {

                    }

                    else -> {

                    }
                }
            })


        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                buyBTCViewModel.getChangeNowEstimation.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            hideLoader()
                        }

                        is NetworkState.Loading -> {
                            requireContext().showLoader()
                        }

                        is NetworkState.Error -> {
                            hideLoader()
                        }

                        is NetworkState.SessionOut -> {

                        }

                        else -> {
                            hideLoader()
                        }
                    }
                }
            }
        }


        buyBTCViewModel.executeOnMetaResponse.observe(
            viewLifecycleOwner,
            EventObserver { resource ->
                when (resource) {
                    is NetworkState.Success -> {
                        if (resource.data != null) {

                            val amount =
                                if (resource.data.receivedTokens == "") 0.0 else resource.data.receivedTokens.toDouble()

                            providerList.filter { it.coinCode == CoinCode.ONMETA }.forEach {
                                it.bestPrice = amount.toBigDecimal().setScale(6, RoundingMode.DOWN)
                                    .toString()//String.format("%.6f", amount)
                            }

                        }
                        checkAllAPIsCompleted()


                    }

                    is NetworkState.Loading -> {

                    }

                    is NetworkState.Error -> {
                        providerList.filter { it.coinCode == CoinCode.ONMETA }.forEach {
                            it.bestPrice = String.format(
                                "%.6f",
                                0.0
                            )
                        }
                        checkAllAPIsCompleted()

                    }

                    is NetworkState.SessionOut -> {

                    }

                    else -> {

                    }
                }
            })

        buyBTCViewModel.getChangeNowBestPrice.observe(
            viewLifecycleOwner,
            EventObserver { resource ->
                when (resource) {
                    is NetworkState.Success -> {
                        if (resource.data != null) {

                            val amount = resource.data.toAmount

                            providerList.filter { it.coinCode == CoinCode.CHANGENOW }.forEach {
                                it.bestPrice =
                                    amount.toBigDecimal().setScale(6, RoundingMode.DOWN).toString()
                            }

                        }
                        checkAllAPIsCompleted()


                    }

                    is NetworkState.Loading -> {

                    }

                    is NetworkState.Error -> {
                        providerList.filter { it.coinCode == CoinCode.CHANGENOW }.forEach {
                            it.bestPrice = String.format(
                                "%.6f",
                                0.0
                            )
                        }
                        checkAllAPIsCompleted()

                    }

                    is NetworkState.SessionOut -> {

                    }

                    else -> {

                    }
                }
            })
        buyBTCViewModel.executeOnRampResponse.observe(
            viewLifecycleOwner,
            EventObserver { resource ->
                when (resource) {
                    is NetworkState.Success -> {
                        if (resource.data?.response != null) {
                            val amount = resource.data.response.quantity

                            providerList.filter { it.coinCode == CoinCode.ONRAMP }.forEach {
                                it.bestPrice =
                                    amount.toBigDecimal().setScale(6, RoundingMode.DOWN).toString()
                            }

                        }
                        checkAllAPIsCompleted()


                    }

                    is NetworkState.Loading -> {

                    }

                    is NetworkState.Error -> {
                        providerList.filter { it.coinCode == CoinCode.ONRAMP }.forEach {
                            it.bestPrice = String.format(
                                "%.6f",
                                0.0
                            )
                        }
                        checkAllAPIsCompleted()

                    }

                    is NetworkState.SessionOut -> {

                    }

                    else -> {

                    }
                }
            })

        buyBTCViewModel.executeOnMeldResponse.observe(
            viewLifecycleOwner,
            EventObserver { resource ->
                when (resource) {
                    is NetworkState.Success -> {
                        if (resource.data != null) {
                            val maxAmount = resource.data.quotes.maxBy { it.destinationAmount }
                            val amount = maxAmount.destinationAmount
                            providerList.filter { it.coinCode == CoinCode.MELD }.forEach {
                                it.bestPrice =
                                    amount.toBigDecimal().setScale(6, RoundingMode.DOWN).toString()
                            }
                        }
                        checkAllAPIsCompleted()


                    }

                    is NetworkState.Loading -> {

                    }

                    is NetworkState.Error -> {
                        providerList.filter { it.coinCode == CoinCode.MELD }.forEach {
                            it.bestPrice = String.format(
                                "%.6f",
                                0.0
                            )
                        }
                        checkAllAPIsCompleted()

                    }

                    is NetworkState.SessionOut -> {

                    }

                    else -> {

                    }
                }
            })

        buyBTCViewModel.executeAlchemyPayResponse.observe(
            viewLifecycleOwner,
            EventObserver { resource ->
                when (resource) {
                    is NetworkState.Success -> {
                        if (resource.data != null) {
                            val amount = resource.data.resultList.cryptoQuantity

                            providerList.filter { it.coinCode == CoinCode.ALCHEMYPAY }.forEach {
                                it.bestPrice =
                                    amount.toBigDecimal().setScale(6, RoundingMode.DOWN).toString()
                            }
                        }
                        checkAllAPIsCompleted()


                    }

                    is NetworkState.Loading -> {

                    }

                    is NetworkState.Error -> {
                        providerList.filter { it.coinCode == CoinCode.ALCHEMYPAY }.forEach {
                            it.bestPrice = String.format(
                                "%.6f",
                                0.0
                            )
                        }
                        checkAllAPIsCompleted()

                    }

                    is NetworkState.SessionOut -> {

                    }

                    else -> {

                    }
                }
            })


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
        if (isValid) {
            viewDataBinding!!.btnNext.apply {
                isEnabled = true
                background =
                    ResourcesCompat.getDrawable(resources, R.drawable.button_gradient_26, null)
                setTextColor(ResourcesCompat.getColor(resources, R.color.white, null))
            }
        } else {
            viewDataBinding!!.btnNext.apply {
                isEnabled = false
                background =
                    ResourcesCompat.getDrawable(resources, R.drawable.button_disable, null)
                setTextColor(ResourcesCompat.getColor(resources, R.color.green_02303B, null))

            }

        }

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
                    txtCurrencySymbole.setTextSize( TypedValue.COMPLEX_UNIT_SP,
                        BIG_TEXT_SIZE)
                    txtCoinMargin.setTextSize( TypedValue.COMPLEX_UNIT_SP,
                        SIXTEEN_TEXT_SIZE)

                }

                inputAmountString.length in 8..12 -> {
                    edtPrice.setTextSize(
                        TypedValue.COMPLEX_UNIT_SP,
                        MEDIUM_TEXT_SIZE
                    )
                    txtCurrencySymbole.setTextSize( TypedValue.COMPLEX_UNIT_SP,
                        MEDIUM_TEXT_SIZE)
                    txtCoinMargin.setTextSize( TypedValue.COMPLEX_UNIT_SP,
                        TWELVE_TEXT_SIZE)
                }

                else -> {
                    edtPrice.setTextSize(
                        TypedValue.COMPLEX_UNIT_SP,
                        SMALL_TEXT_SIZE
                    )
                    txtCurrencySymbole.setTextSize( TypedValue.COMPLEX_UNIT_SP,
                        SMALL_TEXT_SIZE)
                    txtCoinMargin.setTextSize( TypedValue.COMPLEX_UNIT_SP,
                        TEN_TEXT_SIZE)
                }
            }
        }

    }


}
