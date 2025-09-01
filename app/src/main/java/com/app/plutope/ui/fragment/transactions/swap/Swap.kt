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
import com.app.plutope.model.CoinCode
import com.app.plutope.model.Data1
import com.app.plutope.model.PreviewSwapDetail
import com.app.plutope.model.Tokens
import com.app.plutope.model.Wallet
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.ui.fragment.providers.ProviderModel
import com.app.plutope.ui.fragment.token.TokenViewModel
import com.app.plutope.ui.fragment.transactions.buy.graph.GraphDetailViewModel
import com.app.plutope.ui.fragment.transactions.swap.previewSwap.PreviewSwapFragment.Companion.KEY_BUNDLE_PREVIEWSWAP
import com.app.plutope.utils.coinTypeEnum.CoinType
import com.app.plutope.utils.constant.BASE_URL_PLUTO_PE_IMAGES
import com.app.plutope.utils.constant.DEFAULT_CHAIN_ADDRESS
import com.app.plutope.utils.constant.KIP_20
import com.app.plutope.utils.convertToWei
import com.app.plutope.utils.convertWeiToEther
import com.app.plutope.utils.customSnackbar.CustomSnackbar
import com.app.plutope.utils.date_formate.toAny
import com.app.plutope.utils.date_formate.ymdHMS
import com.app.plutope.utils.extras.PreferenceHelper
import com.app.plutope.utils.extras.setSafeOnClickListener
import com.app.plutope.utils.getActualDigits
import com.app.plutope.utils.getNetworkForRangoExchange
import com.app.plutope.utils.getNetworkString
import com.app.plutope.utils.hideLoader
import com.app.plutope.utils.loge
import com.app.plutope.utils.network.NetworkState
import com.app.plutope.utils.safeNavigate
import com.app.plutope.utils.setBalanceText
import com.app.plutope.utils.showSnackBar
import com.app.plutope.utils.showToast
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.util.Calendar

@AndroidEntryPoint
class Swap : BaseFragment<FragmentSwapBinding, SwapViewModel>() {
    private var allTokenList: MutableList<Tokens> = mutableListOf()
    private var payObjDecimal: Int? = 18
    private var getObjDecimal: Int? = 18
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
        loge("PairCall", "Start 1: ${Calendar.getInstance().toAny(ymdHMS)}")
        setFragmentResultListeners()


//        setProgress(viewDataBinding!!.root, 1, (requireActivity() as BaseActivity))
        tokenModel = args.tokenModel
        fromNetwork = getNetworkString(youPayObj.chain)
        toNetwork = getNetworkString(youGetObj.chain)

        originalWidth = viewDataBinding?.imgSwapChange?.width!!
        originalHeight = viewDataBinding?.imgSwapChange?.height!!


//        startShimmerInnerCard(false)
        setData()
        // addProviders(youPayObj)
        setOnClickListner()

        viewDataBinding!!.txtAll.setOnClickListener {
            //viewDataBinding!!.edtYouPay.setText(String.format("%.6f", youPayObj.t_balance.toDouble()))
            viewDataBinding!!.edtYouPay.setText(youPayObj.t_balance)
            viewDataBinding!!.edtYouPay.setSelection(youPayObj.t_balance.length)
        }
        viewDataBinding!!.txtMin.setOnClickListener {
            viewDataBinding!!.edtYouPay.setText((youPayObj.t_balance.toDouble() * 20 / 100).toString())
            viewDataBinding!!.edtYouPay.setSelection(viewDataBinding!!.edtYouPay.text.toString().length)
        }
        viewDataBinding!!.txtHalf.setOnClickListener {
            viewDataBinding!!.edtYouPay.setText((youPayObj.t_balance.toDouble() / 2).toString())
            viewDataBinding!!.edtYouPay.setSelection(viewDataBinding!!.edtYouPay.text.toString().length)
        }

        viewDataBinding!!.layoutBestProvider.setOnClickListener {
            DialogSwapProviderList.getInstance().show(
                requireContext(),
                providerList.filter { it.bestPrice.toDouble() > 0.0 } as MutableList<ProviderModel>,
                youPayObj,
                youGetObj,
                object : DialogSwapProviderList.DialogOnClickBtnListner {
                    @SuppressLint("SetTextI18n")
                    override fun onSubmitClicked(model: ProviderModel) {
                        selectedProvider = model
                        requireActivity().runOnUiThread {
                            viewDataBinding?.edtYouGet?.setText(
                                String.format(
                                    "%.7f",
                                    selectedProvider.bestPrice.toDouble()
                                ) + " ${youGetObj.t_symbol}"
                            )

                            val price = selectedProvider.bestPrice.toDouble()
                                .toBigDecimal() * youGetObj.t_price.toBigDecimal()
                            val formattedPrice = price.setScale(2, RoundingMode.DOWN).toString()

                            viewDataBinding!!.txtConvertedYouGet.text =
                                if (selectedProvider.bestPrice.toBigDecimal() > 0.toBigDecimal()) {
                                    "${
                                        PreferenceHelper.getInstance().getSelectedCurrency()?.symbol
                                    }$formattedPrice"
                                } else {
                                    ""
                                }
                            viewDataBinding!!.txtConvertedYouGet.visibility = VISIBLE

                            viewDataBinding!!.txtBestQuote.text = selectedProvider.name
                            Glide.with(requireContext()).load(selectedProvider.providerIcon)
                                .placeholder(R.drawable.img_pluto_pe_logo_with_bg)
                                .into(viewDataBinding!!.imgProvider)

                            val filteredList = providerList.filter { it.bestPrice.toDouble() > 0.0 }
                            viewDataBinding!!.txtBestPrice.visibility =
                                if (filteredList.isNotEmpty() && model == filteredList[0]) VISIBLE else GONE
                        }
                    }
                }
            )
        }

        loge("SetGetObj", "$youGetObj")

        tokenListDialog = CoinSearchBottomSheetDialog.newInstance(
            isFromGet,
            true,
            youPayObj.chain?.symbol?.lowercase(),
            youPayObj.t_symbol.toString(),
            youGetObj.chain?.symbol?.lowercase() ?: "bsc",
            payObj = youPayObj,
            getObj = youGetObj,
            dialogDismissListner = { token, dismissed ->
                loge("isFromGet", "isDissmised : $dismissed  :: $isFromGet")

                if (token.t_name == "Base") {
                    token.t_address = "0xd07379a755a8f11b57610154861d694b2a0f615a"
                }
                if (token.t_name == "Arbitrum") {
                    token.t_address = "0x912ce59144191c1204e64559fe8253a0e49e6548"
                }

                setSelectedTokenDetail(token, dismissed)

            })



        loge("PairCall", "Start 2: ${Calendar.getInstance().toAny(ymdHMS)}")
        lifecycleScope.launch(Dispatchers.IO) {
            allTokenList = tokenViewModel.getAllTokensList() as MutableList<Tokens>
            val tokenList = allTokenList.sortedByDescending { it.t_balance.toBigDecimalOrNull() }
                .distinctBy { it.t_pk }
            try {
                requireActivity().runOnUiThread {
                    if (youPayObj.t_type.lowercase() == KIP_20.lowercase()) {
                        val newTokenList =
                            tokenList.filter { it.t_type.lowercase() == KIP_20.lowercase() && it.tokenId.lowercase() != youPayObj.tokenId.lowercase() }
                        if (newTokenList.isNotEmpty()) {
//                            stopShimmerInnerCard(false)
                            youGetObj = newTokenList[0]
                            setDetail()
                            filterList.clear()
                            filterList.addAll(newTokenList)
                            filterList.distinct()
                            tokenListDialog.setSwapPairTokenList(filterList)

                        }
                    } else {
                        val newTokenList = tokenList.filter { it != youPayObj }
                        if (newTokenList.isNotEmpty()) {
//                            stopShimmerInnerCard(false)
                            youGetObj = newTokenList[0]
                            setDetail()
                            filterList.clear()
                            filterList.addAll(newTokenList)
                            filterList.distinct()
                            tokenListDialog.setSwapPairTokenList(filterList)

                        }
                    }

                    if (allTokenList.isNotEmpty())
                        quickSwapData()
                    else {
                        viewDataBinding!!.txtLabelQuickSwap.visibility = GONE
                        viewDataBinding!!.rvQuickSwap.visibility = GONE
                    }

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    private fun setSelectedTokenDetail(token: Tokens, dismissed: Boolean) {
        lifecycleScope.launch(Dispatchers.Main) {
            token.callFunction.getBalance {
                token.t_balance = it.toString()
                requireActivity().runOnUiThread {
                    viewDataBinding?.apply {
                        edtYouPay.setText("")
                        edtYouGet.setText("")
                        viewDataBinding!!.txtConvertedYouPay.text = ""
                        viewDataBinding!!.txtConvertedYouGet.text = ""
                        viewDataBinding!!.txtConvertedYouGet.visibility = GONE
                        viewDataBinding!!.txtConvertedYouPay.visibility = GONE
                        providerList.clear()
                        viewDataBinding!!.layoutBestProvider.visibility = GONE
                        viewDataBinding!!.txtLabelChooseProvider.visibility = GONE
                        viewDataBinding!!.txtProviderNotFound.visibility = GONE
                    }
                    if (dismissed) {
                        youGetObj = token
                    } else {
                        youPayObj = token
                    }
                    setDetail()
                }

            }
        }
    }

    private fun quickSwapData() {
        val quickSwapPairList: ArrayList<Pair<Tokens, Tokens>> = arrayListOf()
        val enabledTokenList =
            allTokenList.filter { it.t_address == "" && (it.isEnable == true || it.t_balance.toDouble() > 0.0) }

        val trendingTokenPairs = ArrayList<Pair<String, String>>()
        trendingTokenPairs.add(Pair("Ethereum", "Bitcoin"))
        trendingTokenPairs.add(Pair("Ethereum", "BNB"))
        trendingTokenPairs.add(Pair("BNB", "POL (ex-MATIC)"))
        trendingTokenPairs.add(Pair("POL (ex-MATIC)", "Arbitrum"))
        trendingTokenPairs.add(Pair("Ethereum", "POL (ex-MATIC)"))

        for (pair in trendingTokenPairs) {
            val token1 = enabledTokenList.find {
                it.t_name.equals(
                    pair.first,
                    ignoreCase = true
                )
            }
            loge("Pair1Token", "${token1}")
            val token2 =
                enabledTokenList.find { it.t_name.equals(pair.second, ignoreCase = true) }
            if (token1 != null && token2 != null) {
                quickSwapPairList.add(Pair(token1, token2))
            }
        }

        /*
        //Random pairs from enabled token list
        for (i in 0 until enabledTokenList.size - 1 step 2) {
            val pair = Pair(enabledTokenList[i], enabledTokenList[i + 1])
            quickSwapPairList.add(pair)
        }*/

        val adapter = QuickSwapAdapter(quickSwapPairList) {


            youPayObj = it.first
            youGetObj = it.second
            // setDetail()

            // chain.t_name == "Base" && chain.t_symbol == "Base" && chain.t_address == ""

            if (youPayObj.t_name == "Base" && youPayObj.t_symbol == "Base" && youPayObj.t_address == "") {
                youPayObj.t_address = "0xd07379a755a8f11b57610154861d694b2a0f615a"
            }
            if (youPayObj.t_name == "Arbitrum" && youPayObj.t_symbol == "Arbitrum" && youPayObj.t_address == "") {
                youPayObj.t_address = "0x912ce59144191c1204e64559fe8253a0e49e6548"
            }

            if (youGetObj.t_name == "Base" && youGetObj.t_symbol == "Base" && youGetObj.t_address == "") {
                youGetObj.t_address = "0xd07379a755a8f11b57610154861d694b2a0f615a"
            }
            if (youGetObj.t_name == "Arbitrum" && youGetObj.t_symbol == "Arbitrum" && youGetObj.t_address == "") {
                youGetObj.t_address = "0x912ce59144191c1204e64559fe8253a0e49e6548"
            }

            viewDataBinding!!.edtYouPay.setText("")
            viewDataBinding!!.edtYouGet.setText("")
            viewDataBinding!!.txtConvertedYouPay.text = ""
            viewDataBinding!!.txtConvertedYouGet.text = ""
            viewDataBinding!!.txtConvertedYouPay.visibility = GONE
            viewDataBinding!!.txtConvertedYouGet.visibility = GONE

            setDetail()
            providerList.clear()
            viewDataBinding!!.layoutBestProvider.visibility = GONE
            viewDataBinding!!.txtLabelChooseProvider.visibility = GONE
            viewDataBinding!!.txtProviderNotFound.visibility = GONE

        }
        viewDataBinding!!.rvQuickSwap.adapter = adapter
    }

    /*  private fun startShimmerInnerCard(isPayObject: Boolean) {
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

      }*/


    private fun setFragmentResultListeners() {
        setFragmentResultListener(KEY_BUNDLE_PREVIEWSWAP) { _, _ ->
            isProgrammaticChange = false
            tokenListDialog.setSwapPairTokenList(filterList)
        }
    }


    private fun setOnClickListner() {
        viewDataBinding?.imgBack?.setOnClickListener {
            findNavController().navigateUp()
        }

        viewDataBinding?.btnSwap?.setSafeOnClickListener {
            if (viewDataBinding?.edtYouPay?.text.toString()
                    .isNotEmpty() && (viewDataBinding?.edtYouPay?.text.toString().toDouble() > 0.0)
            ) {

                if ((viewDataBinding?.edtYouPay?.text.toString()
                        .toDouble() > youPayObj.t_balance.toDouble())
                ) {
                    viewDataBinding?.root?.showSnackBar("You don't have enough ${youPayObj.t_symbol} in your account.")

                    /* findNavController().safeNavigate(
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
                     )*/

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
                                    "1 " + viewDataBinding!!.txtCoinName.text.toString() + " = " + viewDataBinding!!.edtYouGet.text.toString()
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
            /*  tokenListDialog.payObj = youPayObj
              tokenListDialog.getObj = youGetObj
              tokenListDialog.fromNetWork = youPayObj.chain?.symbol!!.lowercase()
              tokenListDialog.fromCurrency = youPayObj.t_symbol.toString()
              tokenListDialog.show(childFragmentManager, "")*/


            CoinSearchBottomSheetDialog.newInstance(
                isFromGet,
                true,
                youPayObj.chain?.symbol?.lowercase(),
                youPayObj.t_symbol.toString(),
                youGetObj.chain?.symbol?.lowercase() ?: "bsc",
                payObj = youPayObj,
                getObj = youGetObj,
                dialogDismissListner = { token, dismissed ->
                    loge("isFromGet", "isDissmised : $dismissed  :: $isFromGet")

                    // chain.t_name == "Base" && chain.t_symbol == "Base" && chain.t_address == ""

                    if (token.t_name == "Base" && token.t_symbol == "Base" && token.t_address == "") {
                        token.t_address = "0xd07379a755a8f11b57610154861d694b2a0f615a"
                    }
                    if (token.t_name == "Arbitrum" && token.t_symbol == "Arbitrum" && token.t_address == "") {
                        token.t_address = "0x912ce59144191c1204e64559fe8253a0e49e6548"
                    }

                    setSelectedTokenDetail(token, dismissed)

                }).show(childFragmentManager, "")

        }

        viewDataBinding?.cardInnerCoinSwapped?.setOnClickListener {
            isFromGet = true
            /* tokenListDialog.payObj = youPayObj
             tokenListDialog.getObj = youGetObj
             tokenListDialog.fromNetWork = youPayObj.chain?.symbol!!.lowercase()
             tokenListDialog.fromCurrency = youPayObj.t_symbol.toString()
             tokenListDialog.show(childFragmentManager, "")*/



            CoinSearchBottomSheetDialog.newInstance(
                isFromGet,
                true,
                youPayObj.chain?.symbol?.lowercase(),
                youPayObj.t_symbol.toString(),
                youGetObj.chain?.symbol?.lowercase() ?: "bsc",
                payObj = youPayObj,
                getObj = youGetObj,
                dialogDismissListner = { token, dismissed ->
                    loge("isFromGet", "isDissmised : $dismissed  :: $isFromGet")

                    if (token.t_name == "Base" && token.t_symbol == "Base" && token.t_address == "") {
                        token.t_address = "0xd07379a755a8f11b57610154861d694b2a0f615a"
                    }
                    if (token.t_name == "Arbitrum" && token.t_symbol == "Arbitrum" && token.t_address == "") {
                        token.t_address = "0x912ce59144191c1204e64559fe8253a0e49e6548"
                    }

                    setSelectedTokenDetail(token, dismissed)

                }).show(childFragmentManager, "")

        }

        viewDataBinding?.edtYouPay?.addTextChangedListener(object : TextWatcher {
            private val handler = Handler(Looper.getMainLooper())
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                latestEnteredValue = s.toString()
            }

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
                            viewDataBinding!!.txtConvertedYouGet.visibility = GONE
                        }
                    }


                    handler.removeCallbacksAndMessages(null)
                    handler.postDelayed({
                        loge("InputValue==>", latestEnteredValue)
                        if (latestEnteredValue != "") {
                            debouncingPriceCalculation()
                        } else {
                            try {
                                requireActivity().runOnUiThread {
                                    viewDataBinding!!.progressAmount.visibility = GONE
                                    viewDataBinding!!.edtYouGet.visibility = VISIBLE
                                    viewDataBinding?.edtYouGet?.setText("0")
                                    viewDataBinding!!.txtConvertedYouPay.text = ""
                                    viewDataBinding!!.txtConvertedYouGet.text = ""
                                    viewDataBinding!!.txtConvertedYouPay.visibility = GONE
                                    viewDataBinding!!.txtConvertedYouGet.visibility = GONE

                                    providerList.clear()
                                    viewDataBinding!!.layoutBestProvider.visibility = GONE
                                    viewDataBinding!!.txtLabelChooseProvider.visibility = GONE
                                    viewDataBinding!!.txtProviderNotFound.visibility = GONE


                                    // checkAllAPIsCompleted()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                    }, 3000)

                } else {
                    try {
                        requireActivity().runOnUiThread {
                            viewDataBinding?.edtYouGet?.setText("0")
                            viewDataBinding!!.txtConvertedYouPay.text = ""
                            viewDataBinding!!.txtConvertedYouGet.text = ""
                            viewDataBinding!!.txtConvertedYouPay.visibility = GONE
                            viewDataBinding!!.txtConvertedYouGet.visibility = GONE

                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }

            }

        })

    }

    private fun debouncingPriceCalculation() {
        if (!isProgrammaticChange) {
            if (latestEnteredValue.isNotEmpty()) {
                val inputValue = latestEnteredValue
                try {
                    val numericValue = if (inputValue.startsWith(".")) {
                        "0$inputValue"
                    } else {
                        inputValue
                    }.toDouble()

                    if (numericValue > 0.0) {
                        val price =
                            numericValue.toBigDecimal() * youPayObj.t_price.toBigDecimal()
                        val formattedPrice = price.setScale(2, RoundingMode.DOWN).toString()
                        loge("Price")
                        viewDataBinding!!.txtConvertedYouPay.visibility = VISIBLE
                        viewDataBinding!!.txtConvertedYouPay.text = "${
                            PreferenceHelper.getInstance().getSelectedCurrency()?.symbol
                        }$formattedPrice"
                        apiCallForAllProviderBestPrice(numericValue.toString())


                    } else {
                        try {
                            requireActivity().runOnUiThread {
                                Handler(Looper.getMainLooper()).postDelayed(1000) {
                                    viewDataBinding?.edtYouGet?.setText("0")
                                    viewDataBinding!!.txtConvertedYouPay.text = ""
                                    viewDataBinding!!.txtConvertedYouPay.visibility = GONE
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
                        viewDataBinding!!.txtConvertedYouPay.visibility = GONE
                        viewDataBinding!!.txtConvertedYouGet.visibility = GONE
                        providerList.clear()
                        checkAllAPIsCompleted()

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
        viewDataBinding!!.txtConvertedYouPay.visibility = GONE
        viewDataBinding!!.txtConvertedYouGet.visibility = GONE

        setDetail()
        providerList.clear()
        viewDataBinding!!.layoutBestProvider.visibility = GONE
        viewDataBinding!!.txtLabelChooseProvider.visibility = GONE
        viewDataBinding!!.txtProviderNotFound.visibility = GONE

    }

    private fun setData() {
        youPayObj = tokenModel
        setDetail()
    }

    @SuppressLint("SetTextI18n")
    private fun setDetail() {

        viewDataBinding?.txtFromBalanceValue?.text = setBalanceText(
            youPayObj.t_balance.toBigDecimal(), "", 6
        )

        viewDataBinding?.txtCoinName?.text = youPayObj.t_symbol
        viewDataBinding?.txtChainName?.text = youPayObj.t_name
        viewDataBinding?.txtNetworkNameTop?.text = youPayObj.t_type

        viewDataBinding?.txtYouGetBalanceValue?.text = setBalanceText(
            youGetObj.t_balance.toBigDecimal(), "", 6
        )


        viewDataBinding?.txtCoinNameSwapped?.text = youGetObj.t_symbol
        viewDataBinding?.txtChainNameSwapped?.text = youGetObj.t_name
        viewDataBinding?.txtNetworkNameBottom?.text = youGetObj.t_type


        val imageUrlPayObj =
            if (youPayObj.t_logouri != "" || youPayObj.t_logouri.isNotEmpty()) youPayObj.t_logouri else youPayObj.chain?.icon
        val imageUrlGetObj =
            if (youGetObj.t_logouri != "" || youGetObj.t_logouri.isNotEmpty()) youGetObj.t_logouri else youGetObj.chain?.icon

        val imgPayObj = when (youPayObj.t_type.lowercase()) {
            "erc20" -> R.drawable.img_eth_logo
            "bep20" -> R.drawable.ic_bep
            "polygon" -> R.drawable.ic_polygon
            "kip20" -> R.drawable.ic_kip
            else -> {
                R.drawable.img_eth_logo
            }
        }
        val imgGetObj = when (youGetObj.t_type.lowercase()) {
            "erc20" -> R.drawable.img_eth_logo
            "bep20" -> R.drawable.ic_bep
            "polygon" -> R.drawable.ic_polygon
            "kip20" -> R.drawable.ic_kip
            else -> {
                R.drawable.img_eth_logo
            }
        }

        Glide.with(viewDataBinding?.imgCoin!!.context).load(imageUrlPayObj)
            .placeholder(imgPayObj)
            .error(imgPayObj)
            .into(viewDataBinding?.imgCoin!!)


        Glide.with(viewDataBinding?.imgCoinSwapped!!.context).load(imageUrlGetObj)
            .placeholder(imgGetObj)
            .error(imgGetObj)
            .into(viewDataBinding?.imgCoinSwapped!!)


        // Glide.with(requireContext()).load(youPayObj.t_logouri).into(viewDataBinding?.imgCoin!!)
        // Glide.with(requireContext()).load(youGetObj.t_logouri).into(viewDataBinding?.imgCoinSwapped!!)


        viewDataBinding?.btnSwap?.text = getString(R.string.preview_swap)

        CoroutineScope(Dispatchers.IO).launch {
            youPayObj.callFunction.getDecimal {
                payObjDecimal = it
            }

            youGetObj.callFunction.getDecimal {
                getObjDecimal = it
            }
        }


    }

    @SuppressLint("SetTextI18n")
    override fun setupObserver() {

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                swapViewModel.swapQuoteSingleCallResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            providerList.clear()
                            val response = it.data
                            if (response?.data!!.isNotEmpty()) {
                                response.data.forEach { data ->
                                    if (data?.providerName == "rangoexchange") {
                                        if (data.response?.route != null) {
                                            val amt = convertWeiToEther(
                                                data.quoteAmount!!,
                                                getObjDecimal!!
                                            )
                                            var feesAmount: BigDecimal = 0.toBigDecimal()
                                            data.response.route.fee?.forEach { swapperFees ->
                                                feesAmount += if (swapperFees?.expenseType == "FROM_SOURCE_WALLET" && swapperFees.name == "Swapper Fee") {
                                                    convertWeiToEther(
                                                        swapperFees.amount!!,
                                                        swapperFees.token?.decimals!!
                                                    ).toBigDecimal()
                                                } else 0.toBigDecimal()
                                            }

                                            val amount =
                                                if (youGetObj.chain?.coinType == CoinType.BITCOIN) {
                                                    setBalanceText(
                                                        getActualDigits(amt).toBigDecimal(),
                                                        "",
                                                        18
                                                    )
                                                } else {
                                                    setBalanceText(
                                                        getActualDigits(amt).toBigDecimal(),
                                                        "",
                                                        getObjDecimal!!
                                                    )
                                                }


                                            providerList.add(
                                                ProviderModel(
                                                    providerName = data.providerName,
                                                    CoinCode.RANGO,
                                                    name = data.name!!,
                                                    bestPrice = amount,
                                                    swapperFees = setBalanceText(
                                                        feesAmount, "", 6
                                                    ),
                                                    providerIcon = BASE_URL_PLUTO_PE_IMAGES + data.image
                                                )
                                            )
                                        }

                                    } else if (data?.providerName == "exodus") {

                                        val exodusId = data.response?.id
                                        val exodusTransactionId = data.response?.payInAddress

                                        providerList.add(
                                            ProviderModel(
                                                providerName = data.providerName,
                                                CoinCode.EXODUS,
                                                name = data.name!!,
                                                bestPrice = data.quoteAmount!!,
                                                providerIcon = BASE_URL_PLUTO_PE_IMAGES + data.image,
                                                exodusId = exodusId,
                                                exodusTransactionId = exodusTransactionId

                                            )
                                        )

                                    } else {
                                        providerList.add(
                                            ProviderModel(
                                                providerName = data?.providerName!!,
                                                CoinCode.CHANGENOW,
                                                name = data.name!!,
                                                bestPrice = data.quoteAmount!!,
                                                providerIcon = BASE_URL_PLUTO_PE_IMAGES + data.image
                                            )
                                        )
                                    }
                                }

                                checkAllAPIsCompleted()
                            } else {
                                checkAllAPIsCompleted()
                            }

                        }

                        is NetworkState.Loading -> {
                            viewDataBinding!!.layoutBestProvider.visibility = GONE
                            viewDataBinding!!.txtLabelChooseProvider.visibility = GONE
                            viewDataBinding!!.txtProviderNotFound.visibility = VISIBLE
                            viewDataBinding!!.txtProviderNotFound.text = "Provider finding...."

                        }

                        is NetworkState.Error -> {
                            requireActivity().runOnUiThread {
                                viewDataBinding!!.progressAmount.visibility = GONE
                                viewDataBinding!!.edtYouGet.visibility = VISIBLE
                                viewDataBinding!!.imgProvider.visibility = GONE
                                viewDataBinding!!.layoutBestProvider.visibility = GONE
                                viewDataBinding!!.txtLabelChooseProvider.visibility = GONE
                                viewDataBinding!!.txtProviderNotFound.visibility = VISIBLE
                                viewDataBinding!!.txtProviderNotFound.text = "Provider not found"

                            }
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

                        }
                    }
                }
            }
        }


    }


    override fun onPause() {
        super.onPause()
        isApiCalled = true
        isProgrammaticChange = true
    }


    override fun onResume() {
        super.onResume()
        if (isProgrammaticChange) isProgrammaticChange = false
    }


    private fun apiCallForAllProviderBestPrice(numericValue: String) {
        viewDataBinding!!.progressAmount.visibility = VISIBLE
        viewDataBinding!!.edtYouGet.visibility = GONE
        viewDataBinding!!.edtYouGet.setText("0")
        viewDataBinding!!.txtConvertedYouGet.text = ""
        viewDataBinding!!.txtConvertedYouGet.visibility = GONE


        val amountInWei: BigInteger = convertToWei(numericValue.toDouble(), payObjDecimal!!)
        loge("payObjDecimal :: ${payObjDecimal} :: ${amountInWei}")
        CoroutineScope(Dispatchers.IO).launch {
            youPayObj.callFunction.getDecimal {
                payObjDecimal = it
            }
            youGetObj.callFunction.getDecimal {
                getObjDecimal = it
            }
        }


        val changeNow = ChangeNow(
            address = Wallet.getPublicWalletAddress(youGetObj.chain?.coinType ?: CoinType.ETHEREUM)
                .toString(),
            fromAmount = numericValue.toString(),
            fromCurrency = youPayObj.t_symbol.toString().lowercase(),
            fromNetwork = getNetworkString(youPayObj.chain),
            toAmount = "",
            toCurrency = youGetObj.t_symbol.toString().lowercase(),
            toNetwork = getNetworkString(youGetObj.chain)
        )


        val okx = Okx(
            amount = amountInWei.toString(),
            chainId = youPayObj.chain?.chainIdHex.toString(),
            fromTokenAddress = youPayObj.t_address,
            slippage = "0.1",
            toTokenAddress = youGetObj.t_address,
            userWalletAddress = Wallet.getPublicWalletAddress(youPayObj.chain?.coinType!!)
                .toString()
        )

        val rango = Rango(
            fromBlockchain = getNetworkForRangoExchange(youPayObj.chain).uppercase(),
            fromTokenSymbol = youPayObj.t_symbol.toString().uppercase(),
            fromWalletAddress = Wallet.getPublicWalletAddress(youPayObj.chain?.coinType!!)!!,
            price = amountInWei.toString(),
            rangotoTokenAddress = youGetObj.t_address.toString()
                .ifEmpty { DEFAULT_CHAIN_ADDRESS },
            toBlockchain = getNetworkForRangoExchange(youGetObj.chain).uppercase(),
            toTokenSymbol = youGetObj.t_symbol.toString().lowercase().uppercase(),
            toWalletAddress = Wallet.getPublicWalletAddress(youGetObj.chain?.coinType!!)!!,
            fromTokenAddress = youPayObj.t_address.toString()
                .ifEmpty { DEFAULT_CHAIN_ADDRESS },
            toTokenAddress = youGetObj.t_address.toString()
                .ifEmpty { DEFAULT_CHAIN_ADDRESS },

            )


        loge("ChainName", youPayObj.chain!!.chainName)

        swapViewModel.swapQuoteSingleCall(
            SwapQuoteRequestModel(
                changeNow = changeNow,
                okx = okx,
                rango = rango,
                fromBlockchain = getNetworkForRangoExchange(youPayObj.chain).uppercase(),
                fromTokenSymbol = youPayObj.t_symbol.toString().uppercase(),
                toBlockchain = getNetworkForRangoExchange(youGetObj.chain).uppercase(),
                toTokenSymbol = youGetObj.t_symbol.toString().lowercase().uppercase(),
                amount = numericValue.toString(),
                amountInGwei = amountInWei.toString(),
                fromTokenAddress = youPayObj.t_address.toString()
                    .ifEmpty { DEFAULT_CHAIN_ADDRESS },
                toTokenAddress = youGetObj.t_address.toString()
                    .ifEmpty { DEFAULT_CHAIN_ADDRESS },
                fromWalletAddress = Wallet.getPublicWalletAddress(youPayObj.chain?.coinType!!)!!,
                toWalletAddress = Wallet.getPublicWalletAddress(youGetObj.chain?.coinType!!)!!


            )
        )

    }

    private fun checkAllAPIsCompleted() {
        if (providerList.isNotEmpty()) {
            getBestPriceFromAllBestPrices()
        } else {
            requireActivity().runOnUiThread {
                viewDataBinding!!.progressAmount.visibility = GONE
                viewDataBinding!!.edtYouGet.visibility = VISIBLE
                viewDataBinding!!.imgProvider.visibility = GONE
                viewDataBinding!!.layoutBestProvider.visibility = GONE
                viewDataBinding!!.txtLabelChooseProvider.visibility = GONE
                viewDataBinding!!.txtProviderNotFound.visibility = VISIBLE
                viewDataBinding!!.txtProviderNotFound.text = "Provider not found"

            }
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

            val maxBestPriceModel = providerList[0]
            selectedProvider = maxBestPriceModel
            apiSuccessCount = 0


            val price = maxBestPriceModel.bestPrice.toDouble()
                .toBigDecimal() * youGetObj.t_price.toBigDecimal()
            loge(
                "CurrencyConvert",
                "price : ${price} :: ${maxBestPriceModel.bestPrice} * ${youGetObj.t_price}"
            )
            val formattedPrice = price.setScale(2, RoundingMode.DOWN).toString()

            requireActivity().runOnUiThread {
                if (maxBestPriceModel.bestPrice.toDouble() > 0.0) {
                    val amount = if (youGetObj.chain?.coinType == CoinType.BITCOIN) {
                        setBalanceText(
                            maxBestPriceModel.bestPrice.toBigDecimal(), "", 18
                        )
                    } else {
                        setBalanceText(
                            maxBestPriceModel.bestPrice.toBigDecimal(), "", 6
                        )
                    }

                    viewDataBinding!!.layoutBestProvider.visibility = VISIBLE
                    viewDataBinding!!.txtLabelChooseProvider.visibility = VISIBLE
                    viewDataBinding?.edtYouGet?.setText(amount + " ${youGetObj.t_symbol}")
                    viewDataBinding!!.imgProvider.visibility = VISIBLE
                    viewDataBinding!!.txtBestQuote.text = selectedProvider.name
//                    viewDataBinding!!.txtBestQuotePrice.text = selectedProvider.bestPrice
                    Glide.with(requireContext()).load(selectedProvider.providerIcon)
                        .placeholder(R.drawable.img_pluto_pe_logo_with_bg)
                        .into(viewDataBinding!!.imgProvider)


                } else {
                    viewDataBinding?.edtYouGet?.setText("0")
                }

                viewDataBinding!!.txtConvertedYouGet.text =
                    if (maxBestPriceModel.bestPrice.toBigDecimal() > 0.toBigDecimal()) "${
                        PreferenceHelper.getInstance().getSelectedCurrency()?.symbol
                    }$formattedPrice" else {
                        ""
                    }
                viewDataBinding!!.txtConvertedYouGet.visibility = VISIBLE

                viewDataBinding!!.txtProviderNotFound.visibility = GONE

            }

            loge("Swap", "getBestPriceFromAllBestPrices: MaxBestPrice $maxBestPriceModel")
        } else {

            selectedProvider = ProviderModel(coinCode = CoinCode.CHANGENOW)

            requireActivity().runOnUiThread {
                /* viewDataBinding!!.progressAmount.visibility = GONE
                 viewDataBinding!!.edtYouGet.visibility = VISIBLE*/

                viewDataBinding?.edtYouGet?.setText("0" + " ${youGetObj.t_symbol}")
                viewDataBinding!!.txtConvertedYouGet.text = ""
                viewDataBinding!!.txtConvertedYouGet.visibility = GONE
                apiSuccessCount = 0
            }

        }
    }
}