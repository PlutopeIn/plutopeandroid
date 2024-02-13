package com.app.plutope.ui.fragment.transactions.sell

import android.annotation.SuppressLint
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.custom_views.CustomKeyboardView
import com.app.plutope.databinding.FragmentSellBinding
import com.app.plutope.dialogs.SellInfoDialog
import com.app.plutope.model.BuyCrypto
import com.app.plutope.model.CoinCode
import com.app.plutope.model.CurrencyModel
import com.app.plutope.model.MeldRequestModel
import com.app.plutope.model.OnMetaSellBestPriceModel
import com.app.plutope.model.OnRampSellBestPriceRequestModel
import com.app.plutope.model.Wallet
import com.app.plutope.networkConfig.Chain
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.ui.fragment.currency.Currency
import com.app.plutope.ui.fragment.providers.ProviderModel
import com.app.plutope.ui.fragment.providers.Providers
import com.app.plutope.ui.fragment.transactions.buy.buy_btc.BuyBTCViewModel
import com.app.plutope.utils.EventObserver
import com.app.plutope.utils.OnRampType
import com.app.plutope.utils.constant.ALCHEMY_PAY_APP_ID
import com.app.plutope.utils.constant.ALCHEMY_PAY_BEST_PRICE_URL
import com.app.plutope.utils.constant.ALCHEMY_PAY_SECRET_KEY
import com.app.plutope.utils.constant.CHANGE_NOW_BEST_PRICE
import com.app.plutope.utils.constant.ON_MELD_BEST_PRICE
import com.app.plutope.utils.constant.ON_META_API_KEY
import com.app.plutope.utils.constant.ON_META_BEST_PRICE_SELL_API
import com.app.plutope.utils.constant.ON_RAMP_BEST_PRICE_URL
import com.app.plutope.utils.constant.typeSell
import com.app.plutope.utils.convertAmountToCurrency
import com.app.plutope.utils.decimalInputFilter2
import com.app.plutope.utils.findDecimalFromString
import com.app.plutope.utils.generateSignature
import com.app.plutope.utils.network.NetworkState
import com.app.plutope.utils.safeNavigate
import com.app.plutope.utils.showToast
import com.app.plutope.utils.toHexString
import com.app.plutope.utils.toISOString
import com.bumptech.glide.Glide
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.Base64
import java.util.Calendar
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@AndroidEntryPoint
class SellFragment : BaseFragment<FragmentSellBinding, SellViewModel>() {
    private val sellViewModel: SellViewModel by viewModels()
    private var input: String = ""
    private var providerList: ArrayList<ProviderModel> = arrayListOf()
    private var selectedValue: ProviderModel? = null
    private var selectedCurrencyValue: CurrencyModel? = null
    private var myWalletAddress: String = ""
    private val args: SellFragmentArgs by navArgs()
    private var networkType: String = ""
    private var fiatType: String = ""
    private val buyBTCViewModel: BuyBTCViewModel by viewModels()
    var apiCount = 0
    var isApiCalled = false
    private var isProgrammaticChange = false

    override fun getViewModel(): SellViewModel {
        return sellViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.sellViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_sell
    }

    override fun setupToolbarText(): String {
        return ""
    }

    override fun setupUI() {
        setDetail()
        setFragmentResultListeners()
        setResultCurrency()
        setFiatTypeNetwork()
        setOnClickListeners()

        if (!isApiCalled && args.tokenModel.t_balance.toDouble() >= input.toDouble()) {
            apiCallForAllProviderBestPrice()
        }


    }

    @SuppressLint("SetTextI18n")
    private fun setDetail() {
        myWalletAddress =
            Wallet.getPublicWalletAddress(args.tokenModel.chain?.coinType!!).toString()


       // if (!isApiCalled) {
            isProgrammaticChange = true
            input = "10"
            viewDataBinding?.edtPrice?.setText(input)
        //}
        if (input.toDouble() >= args.tokenModel.t_balance.toDouble() && isProgrammaticChange) {
            viewDataBinding?.txtError?.text = getString(R.string.exceeds_balance)
            viewDataBinding?.txtError?.visibility = VISIBLE
            viewDataBinding?.layoutProvider?.visibility = GONE
            enableButton(false)
        } else {
            viewDataBinding?.txtError?.visibility = GONE
            enableButton(true)
        }


        if (isApiCalled) viewDataBinding?.layoutProvider?.visibility =
            VISIBLE else viewDataBinding?.layoutProvider?.visibility = GONE


        viewDataBinding?.btnNext?.visibility = VISIBLE
        selectedCurrencyValue =
            if (selectedCurrencyValue != null) selectedCurrencyValue else preferenceHelper.getSelectedCurrency()

        viewDataBinding?.txtCurrencySymbole?.text =
            args.tokenModel.t_symbol

        viewDataBinding?.txtCurrency?.text = selectedCurrencyValue?.code

        val symbol = args.tokenModel.t_symbol.toString()

        providerList = args.tokenModel.chain?.sellProviders as ArrayList<ProviderModel>
        providerList.forEach {
            it.symbol = symbol
            it.isFromSell = true
            it.currencyCode = selectedCurrencyValue?.symbol!!
        }


        selectedValue =
            if (buyBTCViewModel.holdSelectedProvider != null) buyBTCViewModel.holdSelectedProvider else {
                if (providerList.size > 1)
                    providerList[2]
                else
                /*providerList[0]*/ null
            }
        viewDataBinding!!.txtProvider.text = selectedValue?.providerName


        Glide.with(requireContext()).load(selectedValue?.icon)
            .into(viewDataBinding?.imgProviderView!!)


        viewDataBinding!!.txtToolbarTitle.text = "Sell ${args.tokenModel.t_symbol}"
        if (buyBTCViewModel.holdBestPrice != null) {
            viewDataBinding!!.txtCoinMargin.text =
                "~${selectedCurrencyValue?.symbol}" + buyBTCViewModel.holdBestPrice.toString() + " "
        } else {
            viewDataBinding!!.txtCoinMargin.text = "Not available!"

        }


        viewDataBinding!!.txtBalance.text = "Balance:" + " " + if (args.tokenModel.t_balance.toDouble() <= 0.0) "0" else {
            val formatted = String.format("%.7f", args.tokenModel.t_balance.toDouble())
            formatted.trimEnd('0', '.')
        }

        viewDataBinding!!.edtPrice.filters = arrayOf(decimalInputFilter2)
    }

    override fun setupObserver() {


        buyBTCViewModel.executeOnRampSellResponse.observe(
            viewLifecycleOwner,
            EventObserver { resource ->
                when (resource) {
                    is NetworkState.Success -> {
                        if (resource.data?.response != null) {

                            val amount = resource.data.response.quantity

                            providerList.filter { it.coinCode == CoinCode.ONRAMP }.forEach {
                                it.bestPrice = String.format(
                                    "%.6f",
                                    amount
                                )
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

        buyBTCViewModel.executeOnMetaSellResponse.observe(
            viewLifecycleOwner,
            EventObserver { resource ->
                when (resource) {
                    is NetworkState.Success -> {
                        if (resource.data != null) {

                            val amount = resource.data.fiatAmount.toDouble()

                            providerList.filter { it.coinCode == CoinCode.ONMETA }.forEach {
                                it.bestPrice = String.format(
                                    "%.6f",
                                    amount
                                )
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

        buyBTCViewModel.executeOnMeldResponse.observe(
            viewLifecycleOwner,
            EventObserver { resource ->
                when (resource) {
                    is NetworkState.Success -> {
                        if (resource.data != null) {
                            val maxAmount = resource.data.quotes.maxBy { it.destinationAmount }
                            val amount = maxAmount.destinationAmount
                            providerList.filter { it.coinCode == CoinCode.MELD }.forEach {
                                it.bestPrice = String.format(
                                    "%.6f",
                                    amount
                                )
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
                                it.bestPrice = String.format(
                                    "%.6f",
                                    amount
                                )
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
        buyBTCViewModel.getChangeNowBestPrice.observe(
            viewLifecycleOwner,
            EventObserver { resource ->
                when (resource) {
                    is NetworkState.Success -> {
                        if (resource.data != null) {

                            val amount = resource.data.toAmount

                            providerList.filter { it.coinCode == CoinCode.CHANGENOW }.forEach {
                                it.bestPrice = String.format(
                                    "%.6f",
                                    amount
                                )
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

    private fun setResultCurrency() {
        setFragmentResultListener(Currency.keyBundleCurrency) { _, bundle ->
            selectedCurrencyValue = bundle.getParcelable(Currency.keyCurrency) as? CurrencyModel
            viewDataBinding?.txtCurrency?.text = selectedCurrencyValue?.code
            //  viewDataBinding?.txtCurrencySymbole?.text = selectedCurrencyValue?.symbol

            buyBTCViewModel.holdBestPrice = selectedValue?.bestPrice
            viewDataBinding!!.txtCoinMargin.text =
                "~${selectedCurrencyValue?.symbol}" + String.format(
                    "%.6f",
                    selectedValue?.bestPrice?.toDouble()
                )
            setFiatTypeNetwork()
            if (viewDataBinding?.edtPrice?.text.toString()
                    .isNotEmpty() && args.tokenModel.t_balance.toDouble() >= viewDataBinding?.edtPrice?.text.toString()
                    .toDouble()
            ) {
                apiCallForAllProviderBestPrice()
            }
        }
    }

    private fun setFragmentResultListeners() {
        setFragmentResultListener(Providers.keyBundleProvider) { _, bundle ->

            selectedValue = bundle.getParcelable(Providers.keyProvider) as? ProviderModel
            viewDataBinding!!.txtProvider.text = selectedValue?.providerName
            buyBTCViewModel.holdSelectedProvider = selectedValue
            buyBTCViewModel.holdBestPrice = selectedValue?.bestPrice
            Glide.with(requireContext()).load(selectedValue?.icon)
                .into(viewDataBinding?.imgProviderView!!)
        }
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
                SellFragmentDirections.actionSellFragmentToProviders(
                    Gson().toJson(
                        filterList
                    )
                )
            )
        }

        viewDataBinding!!.txtCurrency.setOnClickListener {

            findNavController().safeNavigate(
                SellFragmentDirections.actionSellFragmentToCurrency(
                    false
                )
            )

        }

        viewDataBinding!!.keyboardView.findViewById<TextView>(R.id.text_cencel).text = "."

        viewDataBinding!!.keyboardView.listner = object : CustomKeyboardView.NotifyKeyListener {
            override fun getValue(value: String) {
                apiCount = 0
                appendInputText(value)
                viewDataBinding!!.edtPrice.setText(input)
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
                        if (s.toString().toDouble() > 0.0) {
                            isApiCalled = false
                            viewDataBinding?.txtError?.visibility = GONE
                            if (!isProgrammaticChange) {
                                if (s.toString().toDouble() <= args.tokenModel.t_balance.toDouble()) {
                                    apiCallForAllProviderBestPrice()
                            viewDataBinding?.txtError?.visibility = GONE
                                    enableButton(true)
                                } else {
                                    viewDataBinding?.txtError?.text =
                                        getString(R.string.exceeds_balance)
                                    viewDataBinding?.txtError?.visibility = VISIBLE
                                    viewDataBinding?.layoutProvider?.visibility = GONE

                                }
                            }
                            isProgrammaticChange = false
                        }
                    } catch (n: NumberFormatException) {
                        n.printStackTrace()
                    }
                } else {
                    viewDataBinding?.txtError?.visibility = GONE
                    viewDataBinding?.layoutProvider?.visibility = GONE
                    enableButton(false)
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
                                "INR",
                                args.tokenModel.t_symbol.toString(),
                                myWalletAddress,
                                "WQ4Ds5T7qMmwTitbyH6eVv:6385eFN8rGk4fubQx2quWB7B7bzGhWwaMdcG",
                                networkType
                            )
                        )
                    }

                    CoinCode.CHANGENOW.name -> {

                        val currency = when (args.tokenModel.chain) {
                            Chain.Polygon -> "${args.tokenModel.t_symbol?.lowercase()}mainnet"
                            Chain.BinanceSmartChain -> "${args.tokenModel.t_symbol?.lowercase()}bsc"
                            Chain.Ethereum -> args.tokenModel.t_symbol?.lowercase()
                            Chain.OKC -> ""
                            else -> ""
                        }
                        url = BuyCrypto.buildURL(
                            BuyCrypto.Domain.ChangeNow(
                                "",
                                from = currency.toString(),
                                to = selectedCurrencyValue?.code.toString(),
                                fiatMode = "true",
                                amount = viewDataBinding!!.edtPrice.text.toString(),
                                recipientAddress = myWalletAddress
                            )
                        )
                    }

                    CoinCode.ONMETA.name -> {
                        val convertedPrice = convertAmountToCurrency(
                            viewDataBinding?.edtPrice?.text.toString().toDouble().toBigDecimal(),
                            args.tokenModel.t_price.toString().toDouble().toBigDecimal()
                        )
                        url = BuyCrypto.buildURL(
                            BuyCrypto.Domain.OnMeta(
                                name = "widget",
                                apiKey = ON_META_API_KEY,
                                fiatAmount = convertedPrice.toInt()
                                    .toString(),//viewDataBinding!!.edtPrice.text.toString(),
                                walletAddress = myWalletAddress,
                                chainId = args.tokenModel.chain?.chainIdHex.toString(),
                                tokenAddress = args.tokenModel.t_address,
                                isBuyOrSellType = typeSell,
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
                                network = args.tokenModel.chain?.tokenStandard?.lowercase(),
                                isBuyOrSellType = typeSell

                            )
                        )

                    }

                    CoinCode.ALCHEMYPAY.name -> {
                        val network = if (args.tokenModel.t_type?.lowercase()
                                .toString() == "BEP20".lowercase()
                        ) {
                            "BSC"
                        } else {
                            args.tokenModel.t_type?.lowercase().toString()
                        }
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


                    else -> {

                    }
                }


                val intent = CustomTabsIntent.Builder()
                    .build()
                intent.launchUrl(requireContext(), Uri.parse(url))

                /*
                                findNavController().safeNavigate(
                                    SellFragmentDirections.actionSellFragmentToWebView(
                                        url,
                                        selectedValue!!.coinCode.name,
                                        title = "Sell ${selectedValue?.symbol}"
                                    )
                                )
                */


            } else {
                requireContext().showToast("Select Provider first!")
            }


        }

        viewDataBinding!!.imgInfo.setOnClickListener {
            SellInfoDialog.getInstance().show(
                requireContext(),
                "",
                "In accordance with regulatory requirements and to ensure the security and legitimacy of transactions, the \"Sell\" functionality on our platform requires users to complete the KYC (Know Your Customer) process. ",
                object : SellInfoDialog.DialogOnClickBtnListner {
                    override fun onSubmitClicked(selectedList: String) {

                    }
                })
        }
    }

    private fun callOffRampBestPriceApi() {

        val body = mapOf(
            "coinCode" to args.tokenModel.t_symbol?.lowercase(),
            "chainId" to args.tokenModel.chain?.chainIdHex?.lowercase(),
            "network" to networkType.lowercase(),
            "quantity" to viewDataBinding?.edtPrice?.text.toString(),
            "fiatType" to fiatType,
            "type" to OnRampType.offRamp.value
        )
        val onRampBestPriceRequestModel = OnRampSellBestPriceRequestModel(
            args.tokenModel.chain?.chainIdHex?.lowercase().toString(),
            args.tokenModel.t_symbol?.lowercase().toString(),
            viewDataBinding?.edtPrice?.text.toString(),
            fiatType,
            networkType,
            OnRampType.offRamp.value
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

        buyBTCViewModel.executeOnRampSellBestPrice(
            signature,
            payloadBase64,
            ON_RAMP_BEST_PRICE_URL,
            onRampBestPriceRequestModel
        )

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

        selectedValue = maxBestPriceModel
        if (maxBestPriceModel.bestPrice.toDouble() <= 0.0) {
            viewDataBinding?.layoutProvider?.visibility = GONE
            viewDataBinding?.txtCoinMargin?.visibility = VISIBLE
            viewDataBinding?.txtCoinMargin?.text = "Not Available!"
            enableButton(false)
        } else {
            viewDataBinding?.txtCoinMargin?.visibility = VISIBLE
            viewDataBinding?.layoutProvider?.visibility = VISIBLE

            enableButton(true)
            buyBTCViewModel.holdBestPrice = selectedValue?.bestPrice
            buyBTCViewModel.holdSelectedProvider = selectedValue

            viewDataBinding!!.txtProvider.text = selectedValue?.providerName

            viewDataBinding!!.txtCoinMargin.text =
                "~${selectedCurrencyValue?.symbol}" + String.format(
                    "%.6f",
                    selectedValue?.bestPrice?.toDouble()
                )

            Glide.with(requireContext()).load(selectedValue?.icon)
                .into(viewDataBinding?.imgProviderView!!)
        }
    }

    private fun apiCallForAllProviderBestPrice() {
        viewDataBinding?.progressAmount?.visibility = VISIBLE
        viewDataBinding?.txtCoinMargin?.visibility = GONE

        providerList.forEach {
            when (it.coinCode.name) {
                CoinCode.ONRAMP.name -> {
                    callOffRampBestPriceApi()
                }

                CoinCode.ONMETA.name -> {
                    val convertedPrice = convertAmountToCurrency(
                        viewDataBinding?.edtPrice?.text.toString().toDouble().toBigDecimal(),
                        args.tokenModel.t_price.toString().toDouble().toBigDecimal()
                    )
                    val requestModel = OnMetaSellBestPriceModel(
                        args.tokenModel.chain?.chainIdHex?.toInt()!!,
                        convertedPrice.toInt(),
                        selectedCurrencyValue?.code.toString(),
                        args.tokenModel.t_address.toString(),
                        args.tokenModel.t_symbol.toString(),
                        myWalletAddress
                    )

                    buyBTCViewModel.executeOnMetaSellBestPrice(
                        ON_META_BEST_PRICE_SELL_API,
                        body = requestModel
                    )
                }

                CoinCode.CHANGENOW.name -> {
                    var currency: String? = ""
                    if (args.tokenModel.t_address != "") {
                        currency = when (args.tokenModel.chain) {
                            Chain.Polygon -> "${args.tokenModel.t_symbol?.lowercase()}matic"
                            Chain.BinanceSmartChain -> "${args.tokenModel.t_symbol?.lowercase()}bsc"
                            Chain.Ethereum -> args.tokenModel.t_symbol?.lowercase().toString()
                            Chain.OKC -> ""
                            else -> ""
                        }
                    } else {
                        currency = when (args.tokenModel.chain) {
                            Chain.Polygon -> "${args.tokenModel.t_symbol?.lowercase()}mainnet"
                            Chain.BinanceSmartChain -> "${args.tokenModel.t_symbol?.lowercase()}bsc"
                            Chain.Ethereum -> args.tokenModel.t_symbol?.lowercase()
                            Chain.OKC -> ""
                            else -> ""
                        }
                    }

                    val url =
                        CHANGE_NOW_BEST_PRICE + "fromCurrency=${
                            currency
                        }&toCurrency=${
                            selectedCurrencyValue?.code.toString().lowercase()
                        }&fromAmount=${viewDataBinding?.edtPrice?.text.toString()}"
                    buyBTCViewModel.executeChangeNowBestPrice(url)
                }

                CoinCode.MELD.name -> {
                    val meldRequestModel = MeldRequestModel(
                        selectedCurrencyValue?.code?.substring(0, 2).toString(),
                        args.tokenModel.t_symbol.toString(),
                        viewDataBinding?.edtPrice?.text.toString(),
                        selectedCurrencyValue?.code!!
                    )

                    buyBTCViewModel.executeOnMeldBestPrice(ON_MELD_BEST_PRICE, meldRequestModel)
                }

                CoinCode.ALCHEMYPAY.name -> {
                    callAlchemyPayApi()
                }

            }

        }

    }

    private fun callAlchemyPayApi() {

        val network = when {
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
        val body = mapOf(
            "crypto" to args.tokenModel.t_symbol?.lowercase(),
            "network" to network,
            "fiat" to selectedCurrencyValue?.code,
            "side" to "SELL",
            "amount" to viewDataBinding?.edtPrice?.text.toString()
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

    override fun onPause() {
        super.onPause()
        isApiCalled = true
        apiCount = 0
    }

}