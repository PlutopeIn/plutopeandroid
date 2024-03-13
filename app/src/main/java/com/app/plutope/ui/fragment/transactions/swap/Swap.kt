package com.app.plutope.ui.fragment.transactions.swap

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.os.postDelayed
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentSwapBinding
import com.app.plutope.dialogs.CoinSearchBottomSheetDialog
import com.app.plutope.dialogs.SwapProgressDialog
import com.app.plutope.model.AvailablePairsResponseModel
import com.app.plutope.model.CoinCode
import com.app.plutope.model.Data1
import com.app.plutope.model.ExchangeRequestModel
import com.app.plutope.model.PreviewSwapDetail
import com.app.plutope.model.Tokens
import com.app.plutope.model.Wallet
import com.app.plutope.ui.base.BaseActivity
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.ui.fragment.providers.ProviderModel
import com.app.plutope.ui.fragment.token.TokenViewModel
import com.app.plutope.ui.fragment.transactions.buy.graph.GraphDetailViewModel
import com.app.plutope.ui.fragment.transactions.swap.previewSwap.PreviewSwapFragment.Companion.KEY_BUNDLE_PREVIEWSWAP
import com.app.plutope.utils.coinTypeEnum.CoinType
import com.app.plutope.utils.constant.COIN_GEKO_MARKET_API
import com.app.plutope.utils.constant.DEFAULT_CHAIN_ADDRESS
import com.app.plutope.utils.constant.EXCHANGE_API
import com.app.plutope.utils.constant.KIP_20
import com.app.plutope.utils.convertScientificToBigDecimal
import com.app.plutope.utils.convertWeiToEther
import com.app.plutope.utils.customSnackbar.CustomSnackbar
import com.app.plutope.utils.date_formate.toAny
import com.app.plutope.utils.date_formate.ymdHMS
import com.app.plutope.utils.extras.PreferenceHelper
import com.app.plutope.utils.getActualDigits
import com.app.plutope.utils.getNetworkForRangoExchange
import com.app.plutope.utils.getNetworkString
import com.app.plutope.utils.hideLoader
import com.app.plutope.utils.loge
import com.app.plutope.utils.network.NetworkState
import com.app.plutope.utils.roundTo
import com.app.plutope.utils.safeNavigate
import com.app.plutope.utils.setBalanceText
import com.app.plutope.utils.setProgress
import com.app.plutope.utils.showLoader
import com.app.plutope.utils.showSnackBar
import com.app.plutope.utils.showToast
import com.app.plutope.utils.weiToEther
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.web3j.utils.Convert
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.util.Calendar

@AndroidEntryPoint
class Swap : BaseFragment<FragmentSwapBinding, SwapViewModel>() {
    var latestEnteredValue = "0"
    private var storedDecimalInRango: Int? = 18
    private var routerListData: Data1? = null
    private var dexCotractAddress: String? = ""
    private lateinit var tokenListDialog: CoinSearchBottomSheetDialog
    private val swapViewModel: SwapViewModel by viewModels()
    private lateinit var tokenModel: Tokens
    private val args: SwapArgs by navArgs()
    private var fromNetwork: String? = ""
    private var toNetwork: String? = ""
    private var isFromGet: Boolean = false
    private var youPayObj = Tokens()
    private var youGetObj = Tokens()
    private var transactionID: String? = ""
    private lateinit var URL_BY_ID: String
    private val tokenViewModel: TokenViewModel by viewModels()
    private var filterList: MutableList<Tokens> = mutableListOf()
    private var indexPairApi: Int = 0
    private var originalWidth: Int? = 0
    private var originalHeight: Int? = 0
    private var apiSuccessCount = 0
    private val graphDetailViewModel: GraphDetailViewModel by viewModels()
    private var isApiCalled = false
    private var isProgrammaticChange = false

    private var providerList: ArrayList<ProviderModel> = arrayListOf()

    var selectedProvider = ProviderModel(coinCode = CoinCode.CHANGENOW)

    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null
    override fun getViewModel(): SwapViewModel {
        return swapViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.swapViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_swap
    }

    override fun setupToolbarText(): String {
        return getString(R.string.swap)
    }

    override fun setupUI() {
        (activity as BaseActivity).showToolbarTransparentBack()
        loge("PairCall", "Start 1: ${Calendar.getInstance().toAny(ymdHMS)}")
        setFragmentResultListeners()
        setProgress(viewDataBinding!!.root, 1, (requireActivity() as BaseActivity))
        tokenModel = args.tokenModel
        fromNetwork = getNetworkString(youPayObj.chain)
        toNetwork = getNetworkString(youGetObj.chain)

        originalWidth = viewDataBinding?.imgSwapChange?.width!!
        originalHeight = viewDataBinding?.imgSwapChange?.height!!

        startShimmerInnerCard(false)
        setData()
        addProviders(youPayObj)
        setOnClickListner()

        viewDataBinding!!.txtMax.setOnClickListener {
            //viewDataBinding!!.edtYouPay.setText(String.format("%.6f", youPayObj.t_balance.toDouble()))
            viewDataBinding!!.edtYouPay.setText(youPayObj.t_balance)
        }

        viewDataBinding!!.cardBestProvider.setOnClickListener {
            DialogSwapProviderList.getInstance().show(requireContext(),
                providerList.filter { it.bestPrice.toDouble() > 0.0 } as MutableList<ProviderModel>,
                youPayObj,
                youGetObj,
                object : DialogSwapProviderList.DialogOnClickBtnListner {
                    @SuppressLint("SetTextI18n")
                    override fun onSubmitClicked(model: ProviderModel) {
                        selectedProvider = model
                        requireActivity().runOnUiThread {

                            /* viewDataBinding!!.progressAmount.visibility = GONE
                             viewDataBinding!!.edtYouGet.visibility = VISIBLE*/

                            viewDataBinding?.edtYouGet?.setText(
                                String.format(
                                    "%.7f", selectedProvider.bestPrice.toDouble()
                                ) + " ${youGetObj.t_symbol}"
                            )


                            val price = selectedProvider.bestPrice.toDouble()
                                .toBigDecimal() * youGetObj.t_price!!.toBigDecimal()
                            val formattedPrice = price.setScale(2, RoundingMode.DOWN).toString()

                            viewDataBinding!!.txtConvertedYouGet.text =
                                if (selectedProvider.bestPrice.toBigDecimal() > 0.toBigDecimal()) "${
                                    PreferenceHelper.getInstance().getSelectedCurrency()?.symbol
                                }$formattedPrice" else {
                                    ""
                                }


                        }


                    }
                })
        }

        tokenListDialog = CoinSearchBottomSheetDialog(
            isFromGet,
            true,
            youPayObj.chain?.symbol?.lowercase(),
            youPayObj.t_symbol.toString(),
            youGetObj.chain?.symbol?.lowercase() ?: "bsc",
            payObj = youPayObj,
            getObj = youGetObj,
            dialogDismissListner = { token, dismissed ->

                loge("isFromGet", "isDissmised : $dismissed  :: $isFromGet")

                lifecycleScope.launch(Dispatchers.Main) {
                    // requireContext().showLoaderAnyHow()

                    startShimmerInnerCard(!dismissed)
                    token.callFunction.getBalance {
                        token.t_balance = it.toString()
                        requireActivity().runOnUiThread {
                            viewDataBinding?.apply {
                                edtYouPay.setText("")
                                edtYouGet.setText("")
                                viewDataBinding!!.txtConvertedYouPay.text = ""
                                viewDataBinding!!.txtConvertedYouGet.text = ""

                            }
                            if (dismissed) {
                                youGetObj = token

                            } else {
                                youPayObj = token
                                addProviders(youPayObj)

                            }
                            // hideLoader()
                            stopShimmerInnerCard(!dismissed)
                            setDetail()
                        }

                    }
                }
            })

        loge("PairCall", "Start 2: ${Calendar.getInstance().toAny(ymdHMS)}")

        if (youPayObj.t_type?.lowercase() != KIP_20.lowercase()) {
            if (!isApiCalled) {
                /*
                                requireActivity().runOnUiThread {
                                    requireContext().showLoader()
                                }
                */
                loge("PairCall", "Start 3: ${Calendar.getInstance().toAny(ymdHMS)}")
                // requireContext().showLoader()
                exchangePair(indexPairApi)
            }
        } else {
            lifecycleScope.launch(Dispatchers.IO) {
                val tokenList = tokenViewModel.getAllTokensList() as MutableList<Tokens>
                try {
                    requireActivity().runOnUiThread {
                        val newTokenList =
                            tokenList.filter { it.t_type?.lowercase() == KIP_20.lowercase() && it.tokenId?.lowercase() != youPayObj.tokenId?.lowercase() }

                        if (newTokenList.isNotEmpty()) {
                            youGetObj = newTokenList[0]
                            setDetail()
                            filterList.clear()
                            filterList.addAll(newTokenList)
                            filterList.distinct()
                            tokenListDialog.setSwapPairTokenList(filterList)
                        }

                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }


    }

    private fun startShimmerInnerCard(isPayObject: Boolean) {
        if (isPayObject) {
            viewDataBinding?.shimmerLayoutPay?.startShimmer()
            viewDataBinding?.shimmerLayoutPay?.visibility = VISIBLE
            viewDataBinding?.cardInnerCoin?.visibility = GONE
        } else {
            viewDataBinding?.shimmerLayout?.startShimmer()
            viewDataBinding?.shimmerLayout?.visibility = VISIBLE
            viewDataBinding?.cardInnerCoinSwapped?.visibility = GONE
        }

    }

    private fun stopShimmerInnerCard(isPayObject: Boolean) {
        if (isPayObject) {
            viewDataBinding?.shimmerLayoutPay?.stopShimmer()
            viewDataBinding?.shimmerLayoutPay?.visibility = GONE
            viewDataBinding?.cardInnerCoin?.visibility = VISIBLE
        } else {
            viewDataBinding?.shimmerLayout?.stopShimmer()
            viewDataBinding?.shimmerLayout?.visibility = GONE
            viewDataBinding?.cardInnerCoinSwapped?.visibility = VISIBLE
        }

    }

    private fun addProviders(token: Tokens) {
        providerList.clear()
        if (token.chain?.coinType == CoinType.BITCOIN) {
            providerList.add(ProviderModel("CHANGENOW", CoinCode.CHANGENOW))
        } else {
            providerList.add(ProviderModel("OKX", CoinCode.OKX))
            providerList.add(ProviderModel("CHANGENOW", CoinCode.CHANGENOW))
            providerList.add(ProviderModel("RANGO", CoinCode.RANGO))
        }

    }

    private fun setFragmentResultListeners() {
        setFragmentResultListener(KEY_BUNDLE_PREVIEWSWAP) { _, _ ->
            isProgrammaticChange = false
            tokenListDialog.setSwapPairTokenList(filterList)
        }
    }

    private fun exchangePair(index: Int) {
        val arrayChains = arrayOf("eth", "bsc", "matic", "btc")

        swapViewModel.executeSwapPairResponse(
            youPayObj.t_symbol.toString().lowercase(),
            getNetworkString(youPayObj.chain),
            arrayChains[index]
        )
    }

    private fun setOnClickListner() {
        viewDataBinding?.btnSwap?.setOnClickListener {
            if (viewDataBinding?.edtYouPay?.text.toString()
                    .isNotEmpty() && (viewDataBinding?.edtYouPay?.text.toString().toDouble() > 0.0)
            ) {


                /*
                                findNavController().safeNavigate(
                                    SwapDirections.actionSwapToPreviewSwapFragment(
                                        PreviewSwapDetail(
                                            youPayObj,
                                            youGetObj,
                                            routerListData,
                                            viewDataBinding?.edtYouPay?.text.toString(),
                                            viewDataBinding?.edtYouGet?.text.toString(),
                                            viewDataBinding?.txtFirstPrice?.text.toString(),
                                            ""
                                        ), selectedProvider
                                    )
                                )
                */



                if ((viewDataBinding?.edtYouPay?.text.toString()
                        .toDouble() > youPayObj.t_balance.toDouble())
                ) {
                    viewDataBinding?.root?.showSnackBar("You don't have enough ${youPayObj.t_symbol} in your account.")
                } else {

                    if (providerList.none { it.bestPrice.toDouble() > 0.0 }) {
                        requireContext().showToast("Provider not found!")
                    } else {

                        findNavController().safeNavigate(
                            SwapDirections.actionSwapToPreviewSwapFragment(
                                PreviewSwapDetail(
                                    youPayObj,
                                    youGetObj,
                                    routerListData,
                                    viewDataBinding?.edtYouPay?.text.toString(),
                                    viewDataBinding?.edtYouGet?.text.toString(),
                                    viewDataBinding?.txtFirstPrice?.text.toString()
                                ), selectedProvider
                            )
                        )

                    }

                }


            } else {
                requireContext().showToast("Paying amount can't be empty or 0")
            }
        }

        viewDataBinding?.imgSwapChange?.setOnClickListener {
            swapChangeBlankObject()
        }

        viewDataBinding?.cardInnerCoin?.setOnClickListener {
            isFromGet = false
            tokenListDialog.isFromGet = false
            tokenListDialog.fromNetWork = youPayObj.chain?.symbol!!.lowercase()
            tokenListDialog.fromCurrency = youPayObj.t_symbol.toString()
            tokenListDialog.show(childFragmentManager, "")
        }

        viewDataBinding?.cardInnerCoinSwapped?.setOnClickListener {
            isFromGet = true
            tokenListDialog.isFromGet = true
            tokenListDialog.fromNetWork = youPayObj.chain?.symbol!!.lowercase()
            tokenListDialog.fromCurrency = youPayObj.t_symbol.toString()
            tokenListDialog.show(childFragmentManager, "")
        }

        viewDataBinding?.edtYouPay?.addTextChangedListener(object : TextWatcher {

            private val handler = Handler(Looper.getMainLooper())

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                latestEnteredValue = s.toString()

                //  debouncingPriceCalulation()

                /*
                                if (!isProgrammaticChange) {
                                    if (latestEnteredValue.isNotEmpty()) {
                                        val inputValue = latestEnteredValue
                                        try {
                                            // Check if the input starts with a dot
                                            val numericValue = if (inputValue.startsWith(".")) {
                                                "0$inputValue"
                                            } else {
                                                inputValue
                                            }.toDouble()

                                            if (numericValue > 0.0) {


                                                // convertAmountToCurrency(numericValue.toString())

                                                val price =
                                                    numericValue.toBigDecimal() * youPayObj.t_price!!.toBigDecimal()
                                                val formattedPrice = price.setScale(2, RoundingMode.DOWN).toString()
                                               loge("Price")
                                                viewDataBinding!!.txtConvertedYouPay.text = */
                /*if (price > 0.toBigDecimal()) *//*
"${PreferenceHelper.getInstance().getSelectedCurrency()?.symbol}$formattedPrice"*/
                /* else { "" }*//*


                                CoroutineScope(Dispatchers.Main).launch {
                                    requireActivity().runOnUiThread {
                                        viewDataBinding!!.progressAmount.visibility = VISIBLE
                                        viewDataBinding!!.edtYouGet.visibility = GONE
                                        viewDataBinding!!.edtYouGet.setText("0")
                                        viewDataBinding!!.txtConvertedYouGet.text = ""
                                    }

                                    loge(
                                        "TimeDuration",
                                        "Start => ${Calendar.getInstance().toAny()}"
                                    )
                                   // delay(5000)
                                    apiCallForAllProviderBestPrice(numericValue.toString())
                                }


                            } else {
                                try {
                                    requireActivity().runOnUiThread {
                                        Handler(Looper.getMainLooper()).postDelayed(1000) {
                                            viewDataBinding?.edtYouGet?.setText("0")
                                            viewDataBinding!!.txtConvertedYouPay.text = ""
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        } catch (e: NumberFormatException) {
                            e.printStackTrace()
                        }

                    } else {
                        try {
                            requireActivity().runOnUiThread {
                                viewDataBinding?.edtYouGet?.setText("0")
                                viewDataBinding!!.txtConvertedYouPay.text = ""
                                viewDataBinding!!.txtConvertedYouGet.text = ""
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }

                } else {
                    try {
                        requireActivity().runOnUiThread {
                            Handler(Looper.getMainLooper()).postDelayed(1000) {
                                viewDataBinding?.edtYouGet?.setText("0")
                                viewDataBinding!!.txtConvertedYouPay.text = ""
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }
*/


            }/*   handler.postDelayed(runnable!!, 1000)
           }*/

            override fun afterTextChanged(s: Editable?) {
                if (s.toString().isNotEmpty()) {
                    val inputValue = s.toString()
                    latestEnteredValue = inputValue

                    CoroutineScope(Dispatchers.Main).launch {
                        requireActivity().runOnUiThread {
                            viewDataBinding!!.progressAmount.visibility = VISIBLE
                            viewDataBinding!!.edtYouGet.visibility = GONE
                            viewDataBinding!!.edtYouGet.setText("0")
                            viewDataBinding!!.txtConvertedYouGet.text = ""
                        }

                    }


                    handler.removeCallbacksAndMessages(null)
                    handler.postDelayed({
                        loge("InputValue==>", latestEnteredValue)
                        if (latestEnteredValue != "") {
                            debouncingPriceCalulation()
                        } else {
                            try {
                                requireActivity().runOnUiThread {
                                    viewDataBinding!!.progressAmount.visibility = GONE
                                    viewDataBinding!!.edtYouGet.visibility = VISIBLE
                                    viewDataBinding?.edtYouGet?.setText("0")
                                    viewDataBinding!!.txtConvertedYouPay.text = ""
                                    viewDataBinding!!.txtConvertedYouGet.text = ""
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                    }, 5000)

                } else {
                    try {
                        requireActivity().runOnUiThread {
                            viewDataBinding?.edtYouGet?.setText("0")
                            viewDataBinding!!.txtConvertedYouPay.text = ""
                            viewDataBinding!!.txtConvertedYouGet.text = ""
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }

            }

        })

    }

    private fun debouncingPriceCalulation() {
        if (!isProgrammaticChange) {
            if (latestEnteredValue.isNotEmpty()) {
                val inputValue = latestEnteredValue
                try {
                    // Check if the input starts with a dot
                    val numericValue = if (inputValue.startsWith(".")) {
                        "0$inputValue"
                    } else {
                        inputValue
                    }.toDouble()

                    if (numericValue > 0.0) {


                        // convertAmountToCurrency(numericValue.toString())

                        val price =
                            numericValue.toBigDecimal() * youPayObj.t_price!!.toBigDecimal()
                        val formattedPrice = price.setScale(2, RoundingMode.DOWN).toString()
                        loge("Price")
                        viewDataBinding!!.txtConvertedYouPay.text = "${
                            PreferenceHelper.getInstance().getSelectedCurrency()?.symbol
                        }$formattedPrice"

                        /*
                                                CoroutineScope(Dispatchers.Main).launch {
                                                    requireActivity().runOnUiThread {
                                                        viewDataBinding!!.progressAmount.visibility = VISIBLE
                                                        viewDataBinding!!.edtYouGet.visibility = GONE
                                                        viewDataBinding!!.edtYouGet.setText("0")
                                                        viewDataBinding!!.txtConvertedYouGet.text = ""
                                                    }

                                                    loge(
                                                        "TimeDuration",
                                                        "Start => ${Calendar.getInstance().toAny()}"
                                                    )
                                                    // delay(5000)
                                                    apiCallForAllProviderBestPrice(numericValue.toString())
                                                }
                        */

                        apiCallForAllProviderBestPrice(numericValue.toString())


                    } else {
                        try {
                            requireActivity().runOnUiThread {
                                Handler(Looper.getMainLooper()).postDelayed(1000) {
                                    viewDataBinding?.edtYouGet?.setText("0")
                                    viewDataBinding!!.txtConvertedYouPay.text = ""
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                }

            } else {
                try {
                    requireActivity().runOnUiThread {
                        viewDataBinding?.edtYouGet?.setText("0")
                        viewDataBinding!!.txtConvertedYouPay.text = ""
                        viewDataBinding!!.txtConvertedYouGet.text = ""
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

        } else {
            try {
                requireActivity().runOnUiThread {
                    Handler(Looper.getMainLooper()).postDelayed(1000) {
                        viewDataBinding?.edtYouGet?.setText("0")
                        viewDataBinding!!.txtConvertedYouPay.text = ""
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

    }


    private fun swapChangeBlankObject() {
        val temp: Tokens = youPayObj
        youPayObj = youGetObj
        youGetObj = temp

        viewDataBinding!!.edtYouPay.setText("")
        viewDataBinding!!.edtYouGet.setText("")
        viewDataBinding!!.txtConvertedYouPay.text = ""
        viewDataBinding!!.txtConvertedYouGet.text = ""

        setDetail()
    }

    private fun setData() {
        youPayObj = tokenModel
        setDetail()
    }

    @SuppressLint("SetTextI18n")
    private fun setDetail() {
        loge("PairCall", "Start detail: ${Calendar.getInstance().toAny(ymdHMS)}")
        // viewDataBinding?.txtFromBalanceValue?.text =  String.format("%.6f", youPayObj.t_balance.toDouble())
        viewDataBinding?.txtFromBalanceValue?.text = setBalanceText(
            youPayObj.t_balance.toBigDecimal() ?: 0.toBigDecimal(), "", 6
        )


        viewDataBinding?.txtCoinName?.text = youPayObj.t_symbol
        viewDataBinding?.txtNetworkNameTop?.text = youPayObj.t_type


        // viewDataBinding?.txtYouGetBalanceValue?.text = String.format("%.6f", youGetObj.t_balance.toDouble())
        viewDataBinding?.txtYouGetBalanceValue?.text = setBalanceText(
            youGetObj.t_balance.toBigDecimal() ?: 0.toBigDecimal(), "", 6
        )


        viewDataBinding?.txtCoinNameSwapped?.text = youGetObj.t_symbol
        viewDataBinding?.txtNetworkNameBottom?.text = youGetObj.t_type

        Glide.with(requireContext()).load(youPayObj.t_logouri).into(viewDataBinding?.imgCoin!!)
        Glide.with(requireContext()).load(youGetObj.t_logouri)
            .into(viewDataBinding?.imgCoinSwapped!!)


        if (indexPairApi == 3) graphDetailViewModel.executeGetMarketResponse("$COIN_GEKO_MARKET_API?vs_currency=${preferenceHelper.getSelectedCurrency()?.code}&sparkline=false&locale=en&ids=${youGetObj.tokenId},${youPayObj.tokenId}")

        /* if (youPayObj.t_type?.lowercase() == youGetObj.t_type?.lowercase()) {
             viewDataBinding?.btnSwap?.text = "Preview Swap"
         } else {
             viewDataBinding?.btnSwap?.text = "Confirm Swap"
         }*/

        viewDataBinding?.btnSwap?.text = getString(R.string.preview_swap)

        loge("PairCall", "end detail: ${Calendar.getInstance().toAny(ymdHMS)}")

    }

    @SuppressLint("SetTextI18n")
    override fun setupObserver() {


        lifecycleScope.launch {
            swapViewModel.executeSwapUsingOkxResponse.collect {
                when (it) {
                    is NetworkState.Success -> {
                        if (it.data?.data1?.isNotEmpty() == true) {
                            val response = it.data.data1[0].tx

                            val amountSend: BigInteger = Convert.toWei(
                                viewDataBinding?.edtYouPay?.text.toString(), Convert.Unit.ETHER
                            ).toBigInteger()

                            viewDataBinding?.edtYouGet?.setText(
                                String.format(
                                    "%.7f",
                                    weiToEther(it.data.data1[0].tx.minReceiveAmount.toBigInteger())
                                ) + " ${youGetObj.t_symbol}"
                            )

                            youPayObj.callFunction.swapTokenOrCoinOkx(
                                response.to,
                                response.data,
                                response.gasPrice,
                                response.gas,
                                amountSend,
                                dexCotractAddress,
                                { success, errorMessage, _ ->

                                    if (success) {
                                        requireActivity().runOnUiThread {
                                            hideLoader()
                                            setProgress(
                                                viewDataBinding!!.root,
                                                3,
                                                (requireActivity() as BaseActivity)
                                            )
                                            Handler(Looper.getMainLooper()).postDelayed(5000) {
                                                findNavController().safeNavigate(SwapDirections.actionSwapToDashboard())
                                            }


                                        }

                                    } else {
                                        requireActivity().runOnUiThread {
                                            hideLoader()
                                            requireContext().showToast(errorMessage.toString())
                                        }
                                    }

                                },
                                { success, _, _ ->
                                    if (success) {
                                        requireActivity().runOnUiThread {
                                            setProgress(
                                                viewDataBinding!!.root,
                                                2,
                                                (requireActivity() as BaseActivity)
                                            )
                                        }
                                    } else {
                                        requireActivity().runOnUiThread {
                                            hideLoader()
                                        }
                                    }
                                })

                        }

                    }

                    is NetworkState.Loading -> {
                        requireActivity().runOnUiThread {
                            requireContext().showLoader()
                        }
                    }

                    is NetworkState.Error -> {
                        hideLoader()
                        requireContext().showToast(it.message.toString())
                    }

                    is NetworkState.SessionOut -> {
                        hideLoader()
                        CustomSnackbar.make(
                            requireActivity().window.decorView.rootView as ViewGroup,
                            it.message.toString()
                        ).show()
                    }

                    else -> {
                        //hideLoader()
                    }
                }
            }
        }


        lifecycleScope.launch(Dispatchers.Main) {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                swapViewModel.executeSwapUsingSwapPairResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            loge("PairCall", "Start : ${Calendar.getInstance().toAny(ymdHMS)}")
                            val response = it.data as MutableList<AvailablePairsResponseModel>
                            val filterResult = swapViewModel.filterTokensFromPair(
                                response, tokenViewModel.getAllTokensList() as MutableList<Tokens>
                            )
                            filterList.addAll(filterResult)
                            if (indexPairApi == 0) {
                                val randomObject =
                                    filterList.first { token -> token.t_type?.lowercase() == "ERC20".lowercase() || token.t_type?.lowercase() == "btc" }
                                youGetObj = randomObject
                                // setData()
                            }


                            if (indexPairApi < 3) {
                                indexPairApi += 1
                                exchangePair(indexPairApi)
                            } else {
                                filterList.distinct()
                                stopShimmerInnerCard(false)
                                setDetail()
                                viewDataBinding?.progressToken?.visibility = GONE
                                tokenListDialog.setSwapPairTokenList(filterList)
                            }

                            loge("PairCall", "end : ${Calendar.getInstance().toAny(ymdHMS)}")

                        }

                        is NetworkState.Loading -> {
                            // viewDataBinding?.progressToken?.visibility=VISIBLE
                        }

                        is NetworkState.Error -> {
                            try {
                                requireActivity().runOnUiThread {

                                    lifecycleScope.launch(Dispatchers.IO) {
                                        val tokenList =
                                            tokenViewModel.getAllTokensList() as MutableList<Tokens>
                                        try {
                                            requireActivity().runOnUiThread {
                                                val newTokenList =
                                                    tokenList.filter { token -> token.t_type?.lowercase() == youPayObj.t_type?.lowercase() && token.tokenId?.lowercase() != youPayObj.tokenId?.lowercase() }

                                                if (newTokenList.isNotEmpty()) {
                                                    filterList.clear()
                                                    filterList.addAll(newTokenList)
                                                    youGetObj = newTokenList[0]
                                                    setDetail()
                                                    stopShimmerInnerCard(false)
                                                    tokenListDialog.setSwapPairTokenList(filterList)
                                                }
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }

                                    hideLoader()
                                    stopShimmerInnerCard(false)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                        is NetworkState.SessionOut -> {
                            hideLoader()
                            stopShimmerInnerCard(false)
                            CustomSnackbar.make(
                                requireActivity().window.decorView.rootView as ViewGroup,
                                it.message.toString()
                            ).show()
                        }

                        else -> {
                            // hideLoader()
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                graphDetailViewModel.getGetMarketResponse.collect { networkState ->
                    when (networkState) {
                        is NetworkState.Success -> {
                            if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                                val cryptoList = networkState.data
                                if (cryptoList?.isNotEmpty() == true) {
                                    cryptoList.forEach { coinMarket ->
                                        if (coinMarket.id == youGetObj.tokenId) {
                                            youGetObj.t_price = coinMarket.current_price
                                            youGetObj.t_last_price_change_impact =
                                                coinMarket.price_change_percentage_24h
                                            youGetObj.t_logouri = coinMarket.image
                                        } else {
                                            youPayObj.t_price = coinMarket.current_price
                                            youPayObj.t_last_price_change_impact =
                                                coinMarket.price_change_percentage_24h
                                            youPayObj.t_logouri = coinMarket.image
                                        }

                                    }

                                    viewDataBinding!!.edtYouPay.setText("")
                                    estimatePrice(youGetObj, youPayObj)
                                    hideLoader()
                                }
                            }
                        }

                        is NetworkState.Loading -> {
                            // requireContext().showLoaderAnyHow()
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
                            // hideLoader()
                        }
                    }
                }
            }
        }


        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                swapViewModel.executeSwapUsingOkxEstimatResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {

                            if (it.data?.data1?.isNotEmpty() == true) {
                                routerListData = it.data.data1[0]
                                //here
                                var decimal: Int
                                CoroutineScope(Dispatchers.IO).launch {
                                    youGetObj.callFunction.getDecimal { dec ->
                                        decimal = dec!!
                                        val amt = convertWeiToEther(
                                            it.data.data1[0].routerResult.toTokenAmount,
                                            if (decimal == 0) 8 else decimal
                                        )

                                        loge("OKX_response", amt)
                                        loge(
                                            "OkxEstimet",
                                            "edittext ${viewDataBinding!!.edtYouPay.text.toString()} == ${it.data.lastEnteredAmount}"
                                        )

                                        if (it.data.lastEnteredAmount == viewDataBinding!!.edtYouPay.text.toString()) {
                                            providerList.filter { provider -> provider.coinCode == CoinCode.OKX }
                                                .forEach { provideModel ->
                                                    val price = setBalanceText(
                                                        amt.toBigDecimal(), "", 6
                                                    )
                                                    provideModel.bestPrice = price
                                                }
                                        }

                                        checkAllAPIsCompleted()
                                    }
                                }

                            } else {
                                providerList.filter { provider -> provider.coinCode == CoinCode.OKX }
                                    .forEach { innerProvider ->
                                        innerProvider.bestPrice = "0.0"
                                    }

                                checkAllAPIsCompleted()
                            }


                        }

                        is NetworkState.Loading -> {

                        }

                        is NetworkState.Error -> {

                            providerList.filter { provider -> provider.coinCode == CoinCode.OKX }
                                .forEach { provider ->
                                    provider.bestPrice = "0.0"
                                }

                            checkAllAPIsCompleted()
                            // requireContext().showToast(it.message.toString())
                        }

                        is NetworkState.SessionOut -> {
                            hideLoader()
                            CustomSnackbar.make(
                                requireActivity().window.decorView.rootView as ViewGroup,
                                it.message.toString()
                            ).show()
                        }

                        else -> {

                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                swapViewModel.executeEstimateExchangeResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            //hideLoader()
                            loge(
                                "ChangeNow",
                                "edittext ${viewDataBinding!!.edtYouPay.text} == ${it.data?.lastEnteredAmount}"
                            )
                            val response = it.data
                            if (it.data?.lastEnteredAmount == viewDataBinding!!.edtYouPay.text.toString()) {
                                providerList.filter { provider -> provider.coinCode == CoinCode.CHANGENOW }
                                    .forEach { provider ->
                                        val price = setBalanceText(
                                            response?.toAmount?.toBigDecimal() ?: 0.toBigDecimal(),
                                            "",
                                            6
                                        )
                                        provider.bestPrice = price
                                    }
                            }

                            transactionID = response?.id
                            checkAllAPIsCompleted()
                        }

                        is NetworkState.Loading -> {
                            //  requireContext().showLoader()
                        }

                        is NetworkState.Error -> {
                            //   hideLoader()

                            providerList.filter { provider -> provider.coinCode == CoinCode.CHANGENOW }
                                .forEach { provider ->
                                    provider.bestPrice = "0.0"
                                }

                            checkAllAPIsCompleted()

                            // requireContext().showToast(it.message.toString())

                        }

                        is NetworkState.SessionOut -> {
                            hideLoader()
                            CustomSnackbar.make(
                                requireActivity().window.decorView.rootView as ViewGroup,
                                it.message.toString()
                            ).show()
                        }

                        else -> {
                            //   hideLoader()
                        }
                    }
                }
            }
        }




        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                swapViewModel.responseRengoExcQuoteResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            //hideLoader()
                            val response = it.data

                            loge(
                                "RangoSwap",
                                "edittext : ${viewDataBinding!!.edtYouPay.text.toString()} == ${response?.enteredAmount}"
                            )

                            if (response?.route != null && viewDataBinding!!.edtYouPay.text.toString() == response.enteredAmount) {
                                val amount = response.route!!.outputAmount
                                CoroutineScope(Dispatchers.IO).launch {
                                    youGetObj.callFunction.getDecimal { dec ->
                                        val decimal = dec!!
                                        val amt = convertWeiToEther(amount!!, decimal)
                                        var feesAmount: BigDecimal = 0.toBigDecimal()
                                        it.data.route?.fee?.forEach { swapperFees ->
                                            loge(
                                                "calculateFee",
                                                "${swapperFees?.expenseType} == ${swapperFees?.amount}"
                                            )

                                            feesAmount += if (swapperFees?.expenseType == "FROM_SOURCE_WALLET" && swapperFees.name == "Swapper Fee") {
                                                convertWeiToEther(
                                                    swapperFees.amount!!,
                                                    swapperFees.token?.decimals!!
                                                ).toBigDecimal()
                                            } else 0.toBigDecimal()
                                        }

                                        loge(
                                            "Decimal",
                                            "Rango: $decimal :: amount =>${getActualDigits(amt)} feesAmount : $feesAmount : actualfee : ${
                                                convertScientificToBigDecimal("$feesAmount")
                                            } :: $feesAmount"
                                        )


                                        val amount =
                                            if (youGetObj.chain?.coinType == CoinType.BITCOIN) {
                                                setBalanceText(
                                                    getActualDigits(amt).toBigDecimal(), "", 18
                                                )
                                            } else {
                                                setBalanceText(
                                                    getActualDigits(amt).toBigDecimal(), "", decimal
                                                )
                                            }

                                        providerList.filter { it.coinCode == CoinCode.RANGO }
                                            .forEach { provider ->
                                                provider.bestPrice = amount
                                                provider.swapperFees = setBalanceText(
                                                    feesAmount, "", 6
                                                )
                                            }


                                        checkAllAPIsCompleted()
                                    }
                                }

                            } else {

                                providerList.filter { provider -> provider.coinCode == CoinCode.RANGO }
                                    .forEach { provider ->
                                        provider.bestPrice = "0.0"
                                        provider.swapperFees = "0.0"
                                    }

                                checkAllAPIsCompleted()
                            }


                        }

                        is NetworkState.Loading -> {
                            //  requireContext().showLoader()
                        }

                        is NetworkState.Error -> {
                            //   hideLoader()

                            providerList.filter { provider -> provider.coinCode == CoinCode.RANGO }
                                .forEach { provider ->
                                    provider.bestPrice = "0.0"
                                    provider.swapperFees = "0.0"

                                }

                            checkAllAPIsCompleted()

                            // requireContext().showToast(it.message.toString())

                        }

                        is NetworkState.SessionOut -> {
                            hideLoader()
                            CustomSnackbar.make(
                                requireActivity().window.decorView.rootView as ViewGroup,
                                it.message.toString()
                            ).show()
                        }

                        else -> {
                            //   hideLoader()
                        }
                    }
                }
            }
        }


    }


    @SuppressLint("SetTextI18n")
    private fun estimatePrice(getCoinDetail: Tokens?, payCoinDetail: Tokens?) {
        val getPrice = getCoinDetail?.t_price?.toDoubleOrNull() ?: 0.0
        val payPrice = payCoinDetail?.t_price?.toDoubleOrNull() ?: 0.0
        var estPrice = payPrice / getPrice
        estPrice = estPrice.roundTo(6)
        viewDataBinding?.txtFirstPrice?.text =
            "1 ${payCoinDetail?.t_symbol ?: ""} ≈ $estPrice ${getCoinDetail?.t_symbol ?: ""}"

    }

    override fun onPause() {
        super.onPause()
        isApiCalled = true
        isProgrammaticChange = true
    }

    private fun openSwapProgressDialog(title: String, subtitle: String) {
        SwapProgressDialog.getInstance().show(requireContext(),
            title,
            subtitle,
            listener = object : SwapProgressDialog.DialogOnClickBtnListner {
                override fun onOkClick() {
                    findNavController().safeNavigate(SwapDirections.actionSwapToDashboard())
                }
            })
    }

    override fun onResume() {
        super.onResume()
        if (isProgrammaticChange) isProgrammaticChange = false
    }


    private fun apiCallForAllProviderBestPrice(numericValue: String) {

        loge("TimeDuration", "times => ${Calendar.getInstance().toAny()}")
        viewDataBinding!!.progressAmount.visibility = VISIBLE
        viewDataBinding!!.edtYouGet.visibility = GONE
        viewDataBinding!!.edtYouGet.setText("0")
        viewDataBinding!!.txtConvertedYouGet.text = ""

        providerList.forEach {

            when (it.coinCode.name) {

                CoinCode.CHANGENOW.name -> {
                    fromNetwork = getNetworkString(youPayObj.chain)
                    toNetwork = getNetworkString(youGetObj.chain)

                    swapViewModel.executeEstimateExchange(
                        EXCHANGE_API, ExchangeRequestModel(
                            youPayObj.t_symbol.toString().lowercase(),
                            youGetObj.t_symbol.toString().lowercase(),
                            fromNetwork.toString(),
                            toNetwork.toString(),
                            numericValue.toString(),
                            Wallet.getPublicWalletAddress(
                                youGetObj.chain?.coinType ?: CoinType.ETHEREUM
                            ).toString()
                        ), lastEnteredAmount = viewDataBinding!!.edtYouPay.text.toString()
                    )


                }


                CoinCode.OKX.name -> {


                    if (youGetObj.t_type == youPayObj.t_type && youGetObj.t_address != "" && youPayObj.t_address != "") {

                        CoroutineScope(Dispatchers.IO).launch {
                            youPayObj.callFunction.getDecimal {

                                swapViewModel.executeSwapOkxEstimat(
                                    numericValue.toString(),
                                    youPayObj.chain?.chainIdHex.toString(),
                                    youGetObj.t_address.toString()
                                        .ifEmpty { DEFAULT_CHAIN_ADDRESS },
                                    youPayObj.t_address.toString()
                                        .ifEmpty { DEFAULT_CHAIN_ADDRESS },
                                    Wallet.getPublicWalletAddress(youPayObj.chain?.coinType!!)
                                        .toString(),
                                    it,
                                    lastEnteredAmount = viewDataBinding!!.edtYouPay.text.toString()
                                )
                            }
                        }
                    } else {
                        checkAllAPIsCompleted()
                    }
                }

                CoinCode.RANGO.name -> {


                    fromNetwork = getNetworkForRangoExchange(youPayObj.chain)
                    toNetwork = getNetworkForRangoExchange(youGetObj.chain)


                    CoroutineScope(Dispatchers.IO).launch {
                        youPayObj.callFunction.getDecimal { decimal ->
                            storedDecimalInRango = decimal

                            loge("storedDecimalInRango", "$storedDecimalInRango")

                            swapViewModel.executeRangoExchangeQuote(
                                fromBlockchain = fromNetwork!!,
                                fromTokenSymbol = youPayObj.t_symbol.toString(),
                                fromTokenAddress = youPayObj.t_address.toString()
                                    .ifEmpty { DEFAULT_CHAIN_ADDRESS },
                                toBlockchain = toNetwork!!,
                                toTokenSymbol = youGetObj.t_symbol.toString().lowercase(),
                                toTokenAddress = youGetObj.t_address.toString()
                                    .ifEmpty { DEFAULT_CHAIN_ADDRESS },
                                walletAddress = Wallet.getPublicWalletAddress(args.tokenModel.chain?.coinType!!)
                                    .toString(),
                                numericValue,
                                decimal = storedDecimalInRango,
                                fromWalletAddress = Wallet.getPublicWalletAddress(youPayObj.chain?.coinType!!)!!,
                                toWalletAddress = Wallet.getPublicWalletAddress(youGetObj.chain?.coinType!!)!!,
                                lastEnteredAmount = viewDataBinding!!.edtYouPay.text.toString()

                            )


                        }


                    }


                }


            }

        }

    }

    private fun checkAllAPIsCompleted() {
        apiSuccessCount += 1
        loge("TAG", "checkAllAPIsCompleted: apicount $apiSuccessCount")
        if (apiSuccessCount == providerList.size) {
            getBestPriceFromAllBestPrices()
        }

    }

    @SuppressLint("SetTextI18n")
    private fun getBestPriceFromAllBestPrices() {
        loge("Provider", "getBestPriceFromAllBestPrices: $providerList")

        requireActivity().runOnUiThread {
            viewDataBinding!!.progressAmount.visibility = GONE
            viewDataBinding!!.edtYouGet.visibility = VISIBLE
        }

        if (viewDataBinding?.edtYouPay?.text.toString() != "") {

            val maxBestPriceModel = providerList.maxBy {
                it.bestPrice.toDouble()
            }

            selectedProvider = maxBestPriceModel
            apiSuccessCount = 0


            val price = maxBestPriceModel.bestPrice.toDouble()
                .toBigDecimal() * youGetObj.t_price!!.toBigDecimal()
            val formattedPrice = price.setScale(2, RoundingMode.DOWN).toString()

            requireActivity().runOnUiThread {

                /* viewDataBinding!!.progressAmount.visibility = GONE
                 viewDataBinding!!.edtYouGet.visibility = VISIBLE*/

                if (maxBestPriceModel.bestPrice.toDouble() > 0.0) {

                    val amount = if (youGetObj.chain?.coinType == CoinType.BITCOIN) {
                        // String.format("%.15f", maxBestPriceModel.bestPrice.toDouble())

                        setBalanceText(
                            maxBestPriceModel.bestPrice.toBigDecimal(), "", 18
                        )
                    } else {

                        setBalanceText(
                            maxBestPriceModel.bestPrice.toBigDecimal(), "", 6
                        )

                        //  String.format("%.6f", maxBestPriceModel.bestPrice.toDouble())

                    }

                    viewDataBinding?.edtYouGet?.setText(amount + " ${youGetObj.t_symbol}")


                } else {
                    viewDataBinding?.edtYouGet?.setText("0")
                }

                viewDataBinding!!.txtConvertedYouGet.text =
                    if (maxBestPriceModel.bestPrice.toBigDecimal() > 0.toBigDecimal()) "${
                        PreferenceHelper.getInstance().getSelectedCurrency()?.symbol
                    }$formattedPrice" else {
                        ""
                    }
            }

            loge("Swap", "getBestPriceFromAllBestPrices: MaxBestPrice $maxBestPriceModel")
        } else {

            selectedProvider = ProviderModel(coinCode = CoinCode.CHANGENOW)

            requireActivity().runOnUiThread {
                /* viewDataBinding!!.progressAmount.visibility = GONE
                 viewDataBinding!!.edtYouGet.visibility = VISIBLE*/

                viewDataBinding?.edtYouGet?.setText("0" + " ${youGetObj.t_symbol}")
                viewDataBinding!!.txtConvertedYouGet.text = ""
                apiSuccessCount = 0
            }

        }
    }

}