package com.app.plutope.ui.fragment.dashboard.assets

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentAssetsBinding
import com.app.plutope.dialogs.CoinSearchBottomSheetDialog
import com.app.plutope.dialogs.DashboardSearchBottomSheet
import com.app.plutope.model.Tokens
import com.app.plutope.model.Wallet
import com.app.plutope.networkConfig.Chains
import com.app.plutope.ui.base.BaseActivity
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.ui.fragment.dashboard.DashboardDirections
import com.app.plutope.ui.fragment.dashboard.DashboardViewModel
import com.app.plutope.ui.fragment.token.TokenViewModel
import com.app.plutope.ui.fragment.transactions.buy.graph.GraphDetailViewModel
import com.app.plutope.utils.coinTypeEnum.CoinType
import com.app.plutope.utils.constant.COIN_GEKO_PLUTO_PE_SERVER_URL_NEW
import com.app.plutope.utils.constant.isFromReceived
import com.app.plutope.utils.constant.isFromTransactionDetail
import com.app.plutope.utils.customSnackbar.CustomSnackbar
import com.app.plutope.utils.extras.SwipeToDeleteCallback
import com.app.plutope.utils.extras.buttonClickedWithEffect
import com.app.plutope.utils.hideLoader
import com.app.plutope.utils.loge
import com.app.plutope.utils.network.NetworkState
import com.app.plutope.utils.safeNavigate
import com.app.plutope.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.Locale

@AndroidEntryPoint
class Assets : BaseFragment<FragmentAssetsBinding, AssetsViewModel>(),
    AssetsAdapter.DataLoadedCallback {
    private val assetsViewModel: AssetsViewModel by viewModels()
    private var adapter: AssetsAdapter? = null
    private val tokenViewModel: TokenViewModel by viewModels()
    private val graphDetailViewModel: GraphDetailViewModel by viewModels()
    private var dataList: MutableList<Tokens> = mutableListOf()
    private var isFirstTimeCall: Boolean = false
    private var isFromTokenImageUpdate: Boolean = false
    private val dashboardViewModel: DashboardViewModel by activityViewModels()

    private var btcBalance = "0"

    private var callback: ((data: String) -> Unit)? = null

    companion object {
        fun newInstance(callback: (data: String) -> Unit): Assets {
            val fragment = Assets()
            fragment.callback = callback
            return fragment
        }
    }


    override fun getViewModel(): AssetsViewModel {
        return assetsViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.assetsViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_assets
    }

    override fun setupToolbarText(): String {
        return ""
    }

    override fun onPause() {
        super.onPause()
        isFirstTimeCall = true
    }

    override fun setupUI() {

        startShimmerEffect()
        isFromReceived = false

        dashboardViewModel.clickEvent.observe(viewLifecycleOwner) { clicked ->
            if (clicked == true) {
                /* viewDataBinding?.edtSearch?.performClick()
                 dashboardViewModel.resetClickEvent()*/

                DashboardSearchBottomSheet.newInstance(
                    dialogDismissListner = { token ->
                        isFromTransactionDetail = false
                        findNavController().safeNavigate(
                            DashboardDirections.actionDashboardToBuyDetails(
                                token
                            )
                        )
                    }).show(childFragmentManager, "")
            }
        }

        adapter = AssetsAdapter(
            dataList, listener = { model ->
                if (isAdded) {
                    requireActivity().runOnUiThread {

                        /* CoroutineScope(Dispatchers.Main).launch {
                             model.callFunction.getDecimal { decimal ->
                                 model.t_decimal = decimal!!
                             }
                         }*/
                        isFromTransactionDetail = false
                        findNavController().safeNavigate(
                            DashboardDirections.actionDashboardToBuyDetails(model)
                        )

                    }
                }
            }, callback = this
        )

        if (dashboardViewModel.storedList.value!!.isNotEmpty()) {
            startShimmerEffect()
            CoroutineScope(Dispatchers.Main).launch {
                adapter?.list = dashboardViewModel.storedList.value!!
                adapter?.notifyDataSetChanged()
                requireActivity().runOnUiThread {
                    adapter?.sortListByPrice()
                    enableSwipeToDeleteAndUndo()
                }
                //  delay(100)
                stopShimmerEffect()
            }

        }


        viewDataBinding?.rvAssetsList?.adapter = adapter
        tokenViewModel.executeUpdateTokens(Wallet.getPublicWalletAddress(CoinType.BITCOIN)!!/*"bc1q4ce2w4z6q8m6v7fehau640dpp0fpqqzjqzpgsk"*/)
        // tokenViewModel.executeUpdateTokens("bc1q4ce2w4z6q8m6v7fehau640dpp0fpqqzjqzpgsk")

        CoroutineScope(Dispatchers.Main).launch {
            val walletId = Wallet.walletObject.w_id
            loge("walletId=>", "$walletId")
            tokenViewModel.getWalletTokenOfSpecificWalletId(walletId)
        }




        viewDataBinding?.swipeRefreshLayout?.setOnRefreshListener {
            (requireActivity() as BaseActivity).checkInternetConnection()
            startShimmerEffect()
            if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                exploreNewToken()
            }
            tokenViewModel.executeUpdateTokens(Wallet.getPublicWalletAddress(CoinType.BITCOIN)!!)
            tokenViewModel.getWalletTokenOfSpecificWalletId(Wallet.walletObject.w_id)
        }

        viewDataBinding!!.edtSearch.setOnClickListener {
            CoinSearchBottomSheetDialog.newInstance(isFromGet = false,
                isFromSwap = false,
                dialogDismissListner = { token, _ ->
                    startShimmerEffect()
                    tokenViewModel.getWalletTokenOfSpecificWalletId(Wallet.walletObject.w_id)

                }).show(childFragmentManager, "")

        }

        viewDataBinding!!.btnExploreToken.buttonClickedWithEffect {
            if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                exploreNewToken()
            }

        }

        if (!preferenceHelper.isTokenImageUpdateCalled) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val tokenlist = tokenViewModel.getAllTokensList()
                    val tokenImageList = tokenViewModel.getAllTokensImageList()
                    val filteredList = tokenlist.filter { item1 ->
                        tokenImageList.any { item2Update ->
                            item1.tokenId == item2Update.coin_id && item1.t_symbol.lowercase() == item2Update.symbol.lowercase()
                        }
                    }

                    filteredList.forEach { item1 ->
                        val item2Update = tokenImageList.find { it.coin_id == item1.tokenId }
                        item1.t_logouri =
                            item2Update?.image ?: "https://plutope.app/api/images/applogo.png"
                    }

                    CoroutineScope(Dispatchers.IO).launch {
                        tokenViewModel.executeUpdateTokens(filteredList as MutableList<Tokens>)
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            isFromTokenImageUpdate = true
        }
    }


    private fun startShimmerEffect() {
        viewDataBinding!!.shimmerLayout.startShimmer()
        viewDataBinding!!.shimmerLayout.visibility = View.VISIBLE
        viewDataBinding!!.rvAssetsList.visibility = View.GONE
    }

    private fun stopShimmerEffect() {
        viewDataBinding!!.shimmerLayout.stopShimmer()
        viewDataBinding?.shimmerLayout?.visibility = View.INVISIBLE
        viewDataBinding!!.rvAssetsList.visibility = View.VISIBLE
    }


    private fun enableSwipeToDeleteAndUndo() {
        val swipeToDeleteCallback: SwipeToDeleteCallback =
            object : SwipeToDeleteCallback(requireContext(), adapter!!.filteredList) {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, i: Int) {
                    startShimmerEffect()
                    val position = viewHolder.adapterPosition
                    val item = adapter!!.filteredList[position]
                    item.isEnable = false
                    tokenViewModel.executeUpdateWalletToken(item)

                    //requireContext().showToast("Swiped")
                }
            }

        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(viewDataBinding!!.rvAssetsList)
        adapter?.notifyDataSetChanged()

    }


    override fun setupObserver() {

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                tokenViewModel.updateTokenResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            if (!isFromTokenImageUpdate) try {
                                getTotalBalance()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            else isFromTokenImageUpdate = false

                        }

                        is NetworkState.Loading -> {}
                        is NetworkState.Error -> {
                            hideLoader()
                        }

                        is NetworkState.SessionOut -> {
                            hideLoader()
                            CustomSnackbar.make(
                                requireActivity().window.decorView.rootView as ViewGroup,
                                it.message.toString()
                            ).show()
                        }

                        else -> {
                            hideLoader()
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                tokenViewModel.walletsTokensResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            CoroutineScope(Dispatchers.IO).launch {

                                viewDataBinding?.swipeRefreshLayout?.isRefreshing = false
                                if (it.data?.isNotEmpty() == true) {
                                    dataList.clear()
                                    dataList.addAll(it.data as MutableList<Tokens>)
                                    val assetFilter =
                                        dataList.map { it.tokenId }.toList().joinToString(",")
                                    dataList.map { it.tokenId }.toList().joinToString(",")
                                    graphDetailViewModel.executeGetMarketResponse("$COIN_GEKO_PLUTO_PE_SERVER_URL_NEW${preferenceHelper.getSelectedCurrency()?.code}&sparkline=false&locale=en&ids=${assetFilter}")

                                }
                            }
                        }

                        is NetworkState.Loading -> {
                            // requireContext().showLoader()
                        }

                        is NetworkState.Error -> {
                            hideLoader()
                        }

                        is NetworkState.SessionOut -> {
                            hideLoader()
                            CustomSnackbar.make(
                                requireActivity().window.decorView.rootView as ViewGroup,
                                it.message.toString()
                            ).show()
                        }

                        else -> {
                            hideLoader()
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            graphDetailViewModel.getGetMarketResponse.collect { networkState ->
                when (networkState) {
                    is NetworkState.Success -> {
                        val cryptoList = networkState.data
                        var countBalance = 0
                        val deferredList = dataList.map { chain ->
                            if (chain.t_name == "Base" && chain.t_symbol == "Base" && chain.t_address == "") {
                                chain.t_address = "0xd07379a755a8f11b57610154861d694b2a0f615a"
                            } else if (chain.t_name == "Base" && chain.t_symbol == "ETH") {
                                chain.t_address = ""
                            }
                            if (chain.t_name == "Arbitrum" && chain.t_symbol == "ARB" && chain.t_address == "") {
                                chain.t_address = "0x912ce59144191c1204e64559fe8253a0e49e6548"
                            } else if (chain.t_name == "Arbitrum" && chain.t_symbol == "ETH") {
                                chain.t_address = ""
                            }

                            async(Dispatchers.IO) {
                                // Find the matching coinMarket
                                val matchingCoinMarket = cryptoList?.find { coinMarket ->
                                    chain.t_symbol.lowercase(Locale.getDefault()) == coinMarket.symbol.lowercase(
                                        Locale.ROOT
                                    )
                                }

                                // Update the chain data if matching coinMarket is found
                                matchingCoinMarket?.let { coinMarket ->

                                    Chains.entries.forEach {
                                        if (it.symbol.lowercase() == coinMarket.symbol.lowercase()) {
                                            it.currentPrice = coinMarket.current_price ?: "0"
                                        }
                                    }

                                    chain.t_price = coinMarket.current_price ?: "0"
                                    chain.t_last_price_change_impact =
                                        coinMarket.price_change_percentage_24h ?: "0"
                                    chain.t_logouri =
                                        if (chain.t_name == "Arbitrum" && chain.t_symbol == "ETH") {
                                            "https://assets.coingecko.com/coins/images/16547/large/photo_2023-03-29_21.47.00.jpeg?1680097630"
                                        } else if (chain.t_name == "Base" && chain.t_symbol == "ETH") {
                                            "https://coin-images.coingecko.com/coins/images/31199/large/59302ba8-022e-45a4-8d00-e29fe2ee768c-removebg-preview.png?1696530026"
                                        } else {
                                            coinMarket.image
                                        }
                                    chain.t_balance = chain.t_balance
                                }


                                lifecycleScope.launch(Dispatchers.IO) {
                                    chain.callFunction.getBalance {
                                        countBalance++
                                        chain.t_balance =
                                            if (chain.chain?.coinType == CoinType.BITCOIN) btcBalance else it.toString()
                                        tokenViewModel.executeUpdateTokens(dataList)
                                        checkBalanceFinish(countBalance)
                                    }
                                }

                                chain
                            }
                        }

                        val updatedDataList = deferredList.awaitAll().toMutableList()
                        adapter?.list = updatedDataList
                        dashboardViewModel.addAllAssetsList(updatedDataList)
                        adapter?.notifyDataSetChanged()

                        enableSwipeToDeleteAndUndo()
                        delay(4000)

                        requireActivity().runOnUiThread {
                            viewDataBinding?.rvAssetsList?.visibility = View.VISIBLE
                            viewDataBinding?.groupSearchToken?.visibility = View.VISIBLE
                            hideLoader()
                            stopShimmerEffect()

                        }

                    }

                    is NetworkState.Loading -> {
                        // requireContext().showLoader()
                    }

                    is NetworkState.Error -> {
                        requireContext().showToast("Please check after sometimes.")
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


        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                tokenViewModel.updateWalletTokenResp.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            tokenViewModel.getWalletTokenOfSpecificWalletId(Wallet.walletObject.w_id)
                            delay(1000)
                            hideLoader()

                        }

                        is NetworkState.Loading -> {
                            // requireContext().showLoader()
                        }

                        is NetworkState.Error -> {
                            hideLoader()
                        }

                        is NetworkState.SessionOut -> {
                            hideLoader()
                            CustomSnackbar.make(
                                requireActivity().window.decorView.rootView as ViewGroup,
                                it.message.toString()
                            ).show()
                        }

                        else -> {
                            hideLoader()
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                tokenViewModel.getBitcoinBalance.collect {
                    when (it) {
                        is NetworkState.Success -> {/* tokenViewModel.getWalletTokenOfSpecificWalletId(Wallet.walletObject.w_id)
                             delay(1000)*/
                            btcBalance = it.data!!
                            dataList.map { chain ->
                                if (chain.chain?.coinType == CoinType.BITCOIN) {
                                    lifecycleScope.launch(Dispatchers.IO) {

                                        chain.t_balance =
                                            if (chain.chain?.coinType == CoinType.BITCOIN) btcBalance else it.toString()

                                        /* chain.callFunction.getBalance {
                                             chain.t_balance =
                                                 if (chain.chain?.coinType == CoinType.BITCOIN) btcBalance else it.toString()
                                         }*/
                                    }
                                }
                            }
                        }

                        is NetworkState.Loading -> {
                            // requireContext().showLoader()
                        }

                        is NetworkState.Error -> {
                            hideLoader()
                        }

                        is NetworkState.SessionOut -> {
                            hideLoader()
                            CustomSnackbar.make(
                                requireActivity().window.decorView.rootView as ViewGroup,
                                it.message.toString()
                            ).show()
                        }

                        else -> {
                            hideLoader()
                        }
                    }
                }
            }
        }


        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                tokenViewModel.getAllActiveTokenListResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            if (it.data!!.isNotEmpty()) {
                                loge("getAllActiveTokenListResponse", "here i am")
                                val tempList = mutableListOf<Tokens>()
                                tokenViewModel.getAllDisableTokens().forEach { allToken ->
                                    it.data.forEach { responseTokens ->
                                        if (allToken.t_symbol.lowercase() == responseTokens.symbol?.lowercase() && responseTokens.tokenAddress?.lowercase() == allToken.t_address.lowercase()) {
                                            if (responseTokens.tokenAddress != "0x0000000000000000000000000000000000001010") {
                                                tempList.add(allToken)
                                            }
                                        }
                                    }
                                }



                                tokenViewModel.getAllTokenList(
                                    tokenViewModel, tempList.distinct().toMutableList(), true
                                )

                            } else {
                                tokenViewModel.getAllTokenList(tokenViewModel)
                            }

                        }

                        is NetworkState.Loading -> {
                            startShimmerEffect()
                        }

                        is NetworkState.Error -> {
                            hideLoader()
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
                tokenViewModel.insertWalletTokenResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                                hideLoader()
                                adapter?.list?.clear()
                                adapter?.notifyDataSetChanged()

                                setupUI()
                            }

                        }

                        is NetworkState.Loading -> {

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


        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                dashboardViewModel.observeIsBalanceHidden.collect { isHidden ->
                    adapter?.notifyDataSetChanged()
                }
            }
        }

    }

    private fun checkBalanceFinish(countBalance: Int) {
        if (countBalance == adapter?.list?.size) {
            val list = adapter?.list

            val sortedList = list?.sortedWith(compareByDescending { item ->
                val balance = item.t_balance.toBigDecimalOrNull() ?: BigDecimal.ZERO
                val price = item.t_price.toBigDecimalOrNull() ?: BigDecimal.ZERO
                balance * price
            }) as MutableList<Tokens>
            tokenViewModel.executeUpdateTokens(sortedList)
            activity?.runOnUiThread {
                adapter?.sortListByPrice()
                enableSwipeToDeleteAndUndo()

                /*  loge("checkBalanceFinish","Now it's time to load list")
                  viewDataBinding?.rvAssetsList?.visibility = View.VISIBLE
                  viewDataBinding?.groupSearchToken?.visibility = View.VISIBLE
                  stopShimmerEffect()*/
            }

        }
    }


    private fun getTotalBalance() {
        val list = adapter?.list
        var totalBalance: BigDecimal = BigDecimal.ZERO

        list?.let {
            if (it.isNotEmpty()) {
                it.forEach {
                    if (it.t_price.isNotEmpty() == true) {
                        val rupees: BigDecimal =
                            (it.t_balance.toBigDecimal().times(it.t_price.toBigDecimal()))
                        totalBalance += rupees
                    }
                }

                adapter?.notifyDataSetChanged()

                dashboardViewModel.getBalance.value = totalBalance.toString()
                callback?.invoke(totalBalance.toString())
                // Notify adapter **after** all calculations are done
                adapter?.notifyDataSetChanged()
            } else {
                // Handle the case where the list is empty
                // You may want to notify the user or take appropriate action
                callback?.invoke("0.0")
            }
        }
    }

    private fun exploreNewToken() {
        tokenViewModel.getAllActiveTokenList(Wallet.getPublicWalletAddress(CoinType.ETHEREUM)!!)
    }

    override fun onDataLoaded() {
    }


}