package com.app.plutope.ui.fragment.dashboard.assets

import android.view.View
import android.view.ViewGroup
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
import com.app.plutope.model.Tokens
import com.app.plutope.model.Wallet
import com.app.plutope.networkConfig.Chains
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.ui.fragment.dashboard.DashboardDirections
import com.app.plutope.ui.fragment.token.TokenViewModel
import com.app.plutope.ui.fragment.transactions.buy.graph.GraphDetailViewModel
import com.app.plutope.utils.coinTypeEnum.CoinType
import com.app.plutope.utils.constant.COIN_GEKO_MARKET_API
import com.app.plutope.utils.constant.isFromReceived
import com.app.plutope.utils.customSnackbar.CustomSnackbar
import com.app.plutope.utils.extras.SwipeToDeleteCallback
import com.app.plutope.utils.hideLoader
import com.app.plutope.utils.loge
import com.app.plutope.utils.network.NetworkState
import com.app.plutope.utils.safeNavigate
import com.app.plutope.utils.showLoader
import com.app.plutope.utils.showLoaderAnyHow
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.util.Locale

@AndroidEntryPoint
class Assets(val callback: (data: String) -> Unit) :
    BaseFragment<FragmentAssetsBinding, AssetsViewModel>() {

    private val assetsViewModel: AssetsViewModel by viewModels()
    private var adapter: AssetsAdapter? = null
    private val tokenViewModel: TokenViewModel by viewModels()
    private val graphDetailViewModel: GraphDetailViewModel by viewModels()
    private var dataList: MutableList<Tokens> = mutableListOf()
    private var isFirstTimeCall: Boolean = false
    private var isFromTokenImageUpdate: Boolean = false

    private var btcBalance = "0"
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
        isFromReceived = false
        adapter = AssetsAdapter(dataList, listener = { model ->
            if (isAdded) {
                requireActivity().runOnUiThread {
                    findNavController().safeNavigate(
                        DashboardDirections.actionDashboardToBuyDetails(model)
                    )
                }
            }
        })


        viewDataBinding?.rvAssetsList?.adapter = adapter
        tokenViewModel.executeUpdateTokens(Wallet.getPublicWalletAddress(CoinType.BITCOIN)!!/*"bc1q4ce2w4z6q8m6v7fehau640dpp0fpqqzjqzpgsk"*/)
        // tokenViewModel.executeUpdateTokens("bc1q4ce2w4z6q8m6v7fehau640dpp0fpqqzjqzpgsk")

        tokenViewModel.getWalletTokenOfSpecificWalletId(Wallet.walletObject.w_id)
        viewDataBinding?.swipeRefreshLayout?.setOnRefreshListener {
            tokenViewModel.executeUpdateTokens(Wallet.getPublicWalletAddress(CoinType.BITCOIN)!!)
            tokenViewModel.getWalletTokenOfSpecificWalletId(Wallet.walletObject.w_id)
        }

        viewDataBinding!!.edtSearch.setOnClickListener {
            CoinSearchBottomSheetDialog(
                isFromGet = false,
                isFromSwap = false, dialogDismissListner = { _, _ ->
                    tokenViewModel.getWalletTokenOfSpecificWalletId(Wallet.walletObject.w_id)
                }).show(childFragmentManager, "")

        }


        if (!preferenceHelper.isTokenImageUpdateCalled) {
            requireContext().showLoaderAnyHow()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val tokenlist = tokenViewModel.getAllTokensList()
                    val tokenImageList = tokenViewModel.getAllTokensImageList()
                    val filteredList = tokenlist.filter { item1 ->
                        tokenImageList.any { item2Update ->
                            item1.tokenId == item2Update.coin_id &&
                                    item1.t_symbol?.lowercase() == item2Update.symbol.lowercase()
                        }
                    }

                    filteredList.forEach { item1 ->
                        val item2Update = tokenImageList.find { it.coin_id == item1.tokenId }
                        item1.t_logouri = item2Update?.image ?: ""
                    }

                    withContext(Dispatchers.Main) {
                        tokenViewModel.executeUpdateTokens(filteredList as MutableList<Tokens>)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            isFromTokenImageUpdate = true
        }


    }

    private fun enableSwipeToDeleteAndUndo() {
        val swipeToDeleteCallback: SwipeToDeleteCallback =
            object : SwipeToDeleteCallback(requireContext(), adapter!!.filteredList) {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, i: Int) {

                    val position = viewHolder.adapterPosition
                    val item = adapter!!.filteredList[position]
                    item.isEnable = false
                    tokenViewModel.executeUpdateWalletToken(item)
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
                            if (!isFromTokenImageUpdate)
                                getTotalBalance()
                            else
                                isFromTokenImageUpdate = false

                        }

                        is NetworkState.Loading -> {}

                        is NetworkState.Error -> {
                            hideLoader()
                        }

                        is NetworkState.SessionOut -> {
                            hideLoader()
                            CustomSnackbar.make(requireActivity().window.decorView.rootView as ViewGroup, it.message.toString())
                                .show()
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
                            viewDataBinding?.swipeRefreshLayout?.isRefreshing = false
                            if (it.data?.isNotEmpty() == true) {
                                dataList.clear()

                                dataList.addAll(it.data as MutableList<Tokens>)

                                val assetFilter =
                                    dataList.map { it.tokenId }.toList().joinToString(",")

                                //dashboardViewModel.executeGetAssets(cryptoCurrencyUrl + "quotes/latest?" + "symbol=" + assetFilter + "&convert=${preferenceHelper.getSelectedCurrency()?.code}")

                                graphDetailViewModel.executeGetMarketResponse("$COIN_GEKO_MARKET_API?vs_currency=${preferenceHelper.getSelectedCurrency()?.code}&sparkline=false&locale=en&ids=${assetFilter},bitcoin")

                            }
                        }

                        is NetworkState.Loading -> {
                            requireContext().showLoader()
                        }

                        is NetworkState.Error -> {
                            hideLoader()
                        }

                        is NetworkState.SessionOut -> {
                            hideLoader()
                            CustomSnackbar.make(requireActivity().window.decorView.rootView as ViewGroup, it.message.toString())
                                .show()
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
                        var countBalance=0
                        val deferredList = dataList.map { chain ->
                            async(Dispatchers.IO) {
                                // Find the matching coinMarket
                                val matchingCoinMarket = cryptoList?.find { coinMarket ->
                                    chain.t_symbol?.lowercase(Locale.getDefault()) == coinMarket.symbol.lowercase(
                                        Locale.ROOT
                                    )
                                }

                                // Update the chain data if matching coinMarket is found
                                matchingCoinMarket?.let { coinMarket ->

                                    loge(
                                        "Prices",
                                        "${coinMarket.name} symbole :  ${coinMarket.symbol} :: ${coinMarket.current_price}"
                                    )
                                    Chains.values().forEach {
                                        if (it.symbol.lowercase() == coinMarket.symbol.lowercase()) {
                                            it.currentPrice = coinMarket.current_price
                                        }
                                    }

                                    chain.t_price = coinMarket.current_price
                                    chain.t_last_price_change_impact =
                                        coinMarket.price_change_percentage_24h
                                    chain.t_logouri = coinMarket.image
                                }

                                /* if (chain.chain?.coinType == CoinType.BITCOIN) {
                                     countBalance++
                                     chain.t_balance = btcBalance
                                     tokenViewModel.executeUpdateTokens(dataList)
                                     checkBalanceFinish(countBalance)

                                     //  tokenViewModel.executeUpdateTokens("bc1q4ce2w4z6q8m6v7fehau640dpp0fpqqzjqzpgsk")


                                 } else {

                                     lifecycleScope.launch(Dispatchers.IO) {
                                         chain.callFunction.getBalance { // Assuming getBalance is a suspend function
                                             countBalance++
                                             chain.t_balance =
                                                 if (chain.chain?.coinType != CoinType.BITCOIN) btcBalance else it.toString()
                                             tokenViewModel.executeUpdateTokens(dataList)
                                             checkBalanceFinish(countBalance)
                                         }
                                     }
                                 }*/

                                lifecycleScope.launch(Dispatchers.IO) {
                                    chain.callFunction.getBalance { // Assuming getBalance is a suspend function
                                        countBalance++
                                        chain.t_balance =
                                            if (chain.chain?.coinType == CoinType.BITCOIN) btcBalance else it.toString()
                                        tokenViewModel.executeUpdateTokens(dataList)
                                        checkBalanceFinish(countBalance)
                                    }
                                }


                                chain // Return the updated Chain object
                            }
                        }

                        val updatedDataList = deferredList.awaitAll()

                        requireActivity().runOnUiThread {

                            adapter?.list = updatedDataList as MutableList<Tokens>
                            adapter?.notifyDataSetChanged()
                            enableSwipeToDeleteAndUndo()

                            viewDataBinding?.rvAssetsList?.visibility = View.VISIBLE
                            viewDataBinding?.groupSearchToken?.visibility = View.VISIBLE
                        }

                        delay(1000)
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
                        CustomSnackbar.make(requireActivity().window.decorView.rootView as ViewGroup, networkState.message.toString())
                            .show()
                    }

                    else -> {
                        hideLoader()
                    }
                }
            }
        }


        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                tokenViewModel.updateWalletTokenResp.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            tokenViewModel.getWalletTokenOfSpecificWalletId(Wallet.walletObject.w_id)
                            delay(1000)
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
                            CustomSnackbar.make(requireActivity().window.decorView.rootView as ViewGroup, it.message.toString())
                                .show()
                        }

                        else -> {
                            hideLoader()
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                tokenViewModel.getBitcoinBalance.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            /* tokenViewModel.getWalletTokenOfSpecificWalletId(Wallet.walletObject.w_id)
                             delay(1000)*/
                            btcBalance = it.data!!

                            dataList.map { chain ->
                                if (chain.chain?.coinType == CoinType.BITCOIN) {
                                    lifecycleScope.launch(Dispatchers.IO) {
                                        chain.callFunction.getBalance { // Assuming getBalance is a suspend function
                                            // countBalance++
                                            chain.t_balance =
                                                if (chain.chain?.coinType == CoinType.BITCOIN) btcBalance else it.toString()
                                            /* tokenViewModel.executeUpdateTokens(dataList)
                                        checkBalanceFinish(countBalance)*/
                                        }
                                    }
                                }
                            }
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
                                it.message.toString()
                            )
                                .show()
                        }

                        else -> {
                            hideLoader()
                        }
                    }
                }
            }
        }


    }

    private fun checkBalanceFinish(countBalance: Int) {

        if(countBalance==adapter?.list?.size){
            val list = adapter?.list

            val sortedList = list?.sortedWith(compareByDescending { item ->
                val balance = item.t_balance.toBigDecimalOrNull() ?: BigDecimal.ZERO
                val price = item.t_price?.toBigDecimalOrNull() ?: BigDecimal.ZERO
                balance * price
            }) as MutableList<Tokens>
            tokenViewModel.executeUpdateTokens(sortedList)
            requireActivity().runOnUiThread {
                adapter?.sortListByPrice()
                enableSwipeToDeleteAndUndo()
            }

        }
    }

    private fun getTotalBalance() {
        val list = adapter?.list
        var totalBalance: BigDecimal = BigDecimal.ZERO
        list?.onEach {
            if (it.t_price?.isNotEmpty() == true) {
                val rupees: BigDecimal =
                    (it.t_balance.toBigDecimal()
                        .times(it.t_price?.toBigDecimal() ?: 0.0.toBigDecimal()))

                totalBalance += rupees
            }
        }?.last()

        adapter?.notifyDataSetChanged()
        // enableSwipeToDeleteAndUndo()
        callback.invoke(totalBalance.toString())
    }


}