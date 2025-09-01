package com.app.plutope.ui.fragment.transactions.buy.buy_detail

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentBuyDetailsBinding
import com.app.plutope.model.Wallet
import com.app.plutope.networkConfig.Chain
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.utils.coinTypeEnum.CoinType
import com.app.plutope.utils.constant.defaultPLTTokenId
import com.app.plutope.utils.customSnackbar.CustomSnackbar
import com.app.plutope.utils.extras.PreferenceHelper
import com.app.plutope.utils.hideLoader
import com.app.plutope.utils.loge
import com.app.plutope.utils.network.NetworkState
import com.app.plutope.utils.pagination.PaginationScrollListener
import com.app.plutope.utils.safeNavigate
import com.app.plutope.utils.setBalanceText
import com.bumptech.glide.Glide
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


@AndroidEntryPoint
class BuyDetails : BaseFragment<FragmentBuyDetailsBinding, BuyDetailsViewModel>() {

    private val buyDetailsViewModel: BuyDetailsViewModel by viewModels()
    private lateinit var mAdapter: TransactionListAdapter
    val args: BuyDetailsArgs by navArgs()
    var currentPage: Int? = 0
    var totalPages = 0
    var isLastPage = false
    private var isLoading: Boolean = false

    var dataList: MutableList<TransferHistoryModel.Transactions> = mutableListOf()

    var lastCursor: String? = ""

    var protocolType = ""
    override fun getViewModel(): BuyDetailsViewModel {
        return buyDetailsViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.buyDetailsViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_buy_details
    }

    override fun setupToolbarText(): String {
        return ""
    }

    override fun setupUI() {
        loge("TokenModel", "${args.tokenModel}")
        /* CoroutineScope(Dispatchers.IO).launch {
             args.tokenModel.callFunction.getDecimal { decimal ->
                 args.tokenModel.t_decimal = decimal!!
             }
         }*/

        if (defaultPLTTokenId == args.tokenModel.tokenId) {
            viewDataBinding!!.imgAdd.visibility = GONE
            viewDataBinding!!.imgSwap.visibility = GONE
            viewDataBinding!!.imgSell.visibility = GONE
            viewDataBinding!!.txtBuy.visibility = GONE
            viewDataBinding!!.txtMore.visibility = GONE
            viewDataBinding!!.txtSell.visibility = GONE
            viewDataBinding!!.imgGraph.visibility = GONE
        } else {
            viewDataBinding!!.imgAdd.visibility = VISIBLE
            viewDataBinding!!.imgSwap.visibility = VISIBLE
            viewDataBinding!!.imgSell.visibility = VISIBLE
            viewDataBinding!!.txtBuy.visibility = VISIBLE
            viewDataBinding!!.txtMore.visibility = VISIBLE
            viewDataBinding!!.txtSell.visibility = VISIBLE
            viewDataBinding!!.imgGraph.visibility = VISIBLE
        }

        currentPage = 0

        setTokenDetails()
        setOnClickLisners()
        initMyOrderRecyclerView()
    }

    private fun setOnClickLisners() {
        viewDataBinding!!.imgBack.setOnClickListener {
            findNavController().navigateUp()
        }

        viewDataBinding!!.imgGraph.setOnClickListener {
            findNavController().safeNavigate(BuyDetailsDirections.actionBuyDetailsToGraphDetail(args.tokenModel))
        }

        viewDataBinding!!.btnBuy.setOnClickListener {
            findNavController().safeNavigate(BuyDetailsDirections.actionBuyDetailsToBuyBTC(args.tokenModel))
        }

        viewDataBinding!!.imgAdd.setOnClickListener {
            findNavController().safeNavigate(BuyDetailsDirections.actionBuyDetailsToBuyBTC(args.tokenModel))

        }
        viewDataBinding?.imgReceive?.setOnClickListener {
            findNavController().safeNavigate(BuyDetailsDirections.actionBuyDetailsToReceiveCoin(args.tokenModel))
        }

        viewDataBinding?.btnCheckExplorer?.setOnClickListener {
            try {
                val url = getUrlDetail()
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        viewDataBinding?.layoutCheckExplorer?.setOnClickListener {
            try {
                val url = getUrlDetail()
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        viewDataBinding?.imgSwap?.setOnClickListener {
            findNavController().safeNavigate(
                BuyDetailsDirections.actionBuyDetailsToSwap(
                    args.tokenModel
                )
            )
        }

        viewDataBinding!!.imgSell.setOnClickListener {
            findNavController().safeNavigate(
                BuyDetailsDirections.actionBuyDetailsToSellFragment()
            )
        }

        viewDataBinding!!.imgSend.setOnClickListener {
            findNavController().safeNavigate(
                BuyDetailsDirections.actionBuyDetailsToSendCoin(
                    args.tokenModel
                )
            )
        }

    }


    private fun fetchTransactionListData(currentPage: Int, protocolType: String) {

        args.tokenModel.chain?.walletAddress =
            Wallet.getPublicWalletAddress(args.tokenModel.chain?.coinType!!)

        if (args.tokenModel.t_address != "") {
            buyDetailsViewModel.executeGetTransferHistory(
                "https://plutope.app/api/wallet-transcation?wallet_address=${
                    Wallet.getPublicWalletAddress(
                        args.tokenModel.chain?.coinType!!
                    )
                }&chain=${args.tokenModel.chain?.chainName?.lowercase()}&token_address=${args.tokenModel.t_address}&cursor=${lastCursor}",
                args.tokenModel, currentPage
            )


        } else {

            val shortname =
                if (args.tokenModel.chain?.chainName != null) args.tokenModel.chain?.chainName else args.tokenModel.t_symbol

            buyDetailsViewModel.executeGetTransferHistory(
                "https://plutope.app/api/wallet-transcation?wallet_address=${
                    Wallet.getPublicWalletAddress(
                        args.tokenModel.chain?.coinType!!
                    )
                }&chain=${shortname?.lowercase()}&token_address=&cursor=${lastCursor}",
                args.tokenModel,
                currentPage
            )
        }
    }


    @SuppressLint("SetTextI18n")
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
            if (defaultPLTTokenId != args.tokenModel.tokenId) {
                viewDataBinding?.groupShowSendReceiveIcon?.visibility = GONE
            }
        } else {
            Glide.with(requireContext()).load(tokenModel.t_logouri).into(viewDataBinding?.imgCoin!!)
            if (defaultPLTTokenId != args.tokenModel.tokenId) {
                viewDataBinding?.groupShowSendReceiveIcon?.visibility = VISIBLE
            }
        }

        viewDataBinding?.txtToolbarTitle?.text = tokenModel.t_name

        viewDataBinding?.txtBalance?.text = setBalanceText(
            tokenModel.t_balance.toBigDecimal(),
            tokenModel.t_symbol.toString(),
            7
        )
        viewDataBinding?.txtNetworkName?.text = tokenModel.t_type

        val priceDouble = tokenModel.t_price.toDoubleOrNull() ?: 0.0
        val priceText = String.format("%.6f", priceDouble)
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
            preferenceHelper.getSelectedCurrency()?.symbol + "" + priceText
        viewDataBinding?.txtCryptoDiffrencePercentage?.text = "$pricePercent%"
        viewDataBinding?.txtCryptoDiffrencePercentage?.setTextColor(color)


    }

    override fun setupObserver() {
        GlobalScope.launch(Dispatchers.IO) {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                buyDetailsViewModel.setWalletActive.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            hideLoader()
                        }

                        is NetworkState.Loading -> {}
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
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                buyDetailsViewModel.transferHistoryResponse.collect { it ->
                    when (it) {
                        is NetworkState.Success -> {
                            hideLoader()
                            lastCursor = it.data?.cursor
                            totalPages = it.data?.totalPage!!
                            currentPage = it.data.page
                            if (it.data.transactions?.isNotEmpty() == true) {
                                if (it.data.transactions.isNotEmpty()) {

                                    if (!PreferenceHelper.getInstance().isActiveWallet) {
                                        buyDetailsViewModel.setWalletActiveCall(
                                            Wallet.getPublicWalletAddress(
                                                CoinType.ETHEREUM
                                            )!!, ""
                                        )
                                    }

                                    if (currentPage == totalPages) {
                                        isLastPage = true
                                        stopShimmerEffect()
                                    }
                                    viewDataBinding?.rvTransactionList?.visibility = VISIBLE
                                    viewDataBinding?.layoutNoFound?.visibility = GONE
                                    val respList = it.data.transactions

                                    dataList.addAll(respList)
                                    if (dataList.isEmpty()) {
                                        stopShimmerEffect()
                                        viewDataBinding?.rvTransactionList?.visibility = GONE
                                        viewDataBinding?.layoutNoFound?.visibility = VISIBLE
                                    }

                                    mAdapter.removeLoadingFooter()
                                    isLoading = false

                                    mAdapter.addAll(respList.distinctBy { it.hash } as MutableList<TransferHistoryModel.Transactions>)

                                    if (currentPage != totalPages && lastCursor != null) mAdapter.addLoadingFooter() else isLastPage =
                                        true

                                } else {
                                    stopShimmerEffect()
                                    viewDataBinding?.rvTransactionList?.visibility = GONE
                                    viewDataBinding?.layoutNoFound?.visibility = VISIBLE
                                }
                            } else {

                                if (currentPage == totalPages) {
                                    isLastPage = true
                                    stopShimmerEffect()
                                }

                            }


                            viewDataBinding?.progressPage?.visibility = GONE
                            if (mAdapter.transferList.size == 0) {
                                viewDataBinding?.rvTransactionList?.visibility = GONE
                                viewDataBinding?.layoutNoFound?.visibility = VISIBLE
                            }

                            stopShimmerEffect()
                        }

                        is NetworkState.Loading -> {
                            if (currentPage == 0)
                                startShimmerEffect()
                            // requireContext().showLoader()
                        }

                        is NetworkState.Error -> {
                            viewDataBinding?.progressPage?.visibility = GONE
                            // hideLoader()
                            //stopShimmerEffect()
                            if (dataList.isEmpty()) {
                                stopShimmerEffect()
                                viewDataBinding?.rvTransactionList?.visibility = GONE
                                viewDataBinding?.layoutNoFound?.visibility = VISIBLE
                            }
                        }

                        is NetworkState.SessionOut -> {
                            // hideLoader()
                            stopShimmerEffect()
                            CustomSnackbar.make(
                                requireActivity().window.decorView.rootView as ViewGroup,
                                it.message.toString()
                            )
                                .show()
                        }

                        else -> {
                            viewDataBinding?.progressPage?.visibility = GONE
                            hideLoader()
                        }
                    }
                }
            }
        }


    }

    private fun startShimmerEffect() {
        viewDataBinding?.shimmerLayout?.startShimmer()
        viewDataBinding?.shimmerLayout?.visibility = VISIBLE
        viewDataBinding?.rvTransactionList?.visibility = GONE

    }

    private fun stopShimmerEffect() {
        viewDataBinding?.shimmerLayout?.stopShimmer()
        viewDataBinding?.rvTransactionList?.visibility = VISIBLE
        viewDataBinding?.shimmerLayout?.visibility = View.INVISIBLE


    }


    /***
     *  Pagination code start
     * **/

    private fun initMyOrderRecyclerView() {
        //attach adapter to  recycler
        mAdapter =
            TransactionListAdapter(this@BuyDetails, args.tokenModel.t_address.toString()) {
                loge("model", "${Gson().toJson(it)}")
                // currentPage = 1
                /*lastCursor = ""
                findNavController().safeNavigate(
                    BuyDetailsDirections.actionBuyDetailsToTransfer(
                        args.tokenModel,
                        it
                    )
                )*/

                val url = getUrlDetail(it)
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)

            }
        viewDataBinding!!.adapter = mAdapter
        viewDataBinding!!.rvTransactionList.setHasFixedSize(true)
        viewDataBinding!!.rvTransactionList.itemAnimator = DefaultItemAnimator()

        loadFirstPage()

        viewDataBinding!!.rvTransactionList.addOnScrollListener(object :
            PaginationScrollListener(viewDataBinding!!.rvTransactionList.layoutManager as LinearLayoutManager) {
            override fun loadMoreItems() {
                loge("loadMoreItems :: $currentPage ::  ${dataList.size}")
                if (currentPage != 0 && lastCursor != null) {
                    isLoading = true
                    // currentPage += 1
                    //loge("loadMoreItems :: $currentPage")
                    Handler(Looper.myLooper()!!).postDelayed({
                        loadNextPage()
                    }, 1000)
                }
            }

            override fun getTotalPageCount(): Int {
                loge("totalPages_return : ${totalPages}")
                return totalPages
            }

            override fun isLastPage(): Boolean {
                loge("isLastPage_return : ${isLastPage}")
                return isLastPage
            }

            override fun isLoading(): Boolean {
                loge("isLoading_return : ${isLoading}")
                return isLoading
            }

        })

    }

    fun loadNextPage() {
        if (lastCursor != null) {
            fetchTransactionListData(currentPage!!, protocolType)
        }
    }

    private fun loadFirstPage() {
        fetchTransactionListData(currentPage!!, protocolType)
    }


    private fun getUrlDetail(transactionsModel: TransferHistoryModel.Transactions): String {
        val urlToOpen: String = when (args.tokenModel.chain) {
            Chain.BinanceSmartChain -> "https://bscscan.com/tx/${transactionsModel.hash}"
            Chain.Ethereum -> "https://etherscan.io/tx/${transactionsModel.hash}"
            Chain.OKC -> "https://web3.okx.com/explorer/x-layer/tx/${transactionsModel.hash}"
            Chain.Polygon -> "https://polygonscan.com/tx/${transactionsModel.hash}"
            Chain.Bitcoin -> "https://btcscan.org/tx/${transactionsModel.hash}"
            Chain.Optimism -> "https://optimistic.etherscan.io/tx/${transactionsModel.hash}"
            Chain.Arbitrum -> "https://arbiscan.io/tx/${transactionsModel.hash}"
            Chain.Avalanche -> "https://subnets.avax.network/c-chain/block/${transactionsModel.hash}"
            Chain.BaseMainnet -> "https://basescan.org/tx/${transactionsModel.hash}"
            else -> ""
        }
        return urlToOpen
    }

    private fun getUrlDetail(): String {
        val addressWallet =
            Wallet.getPublicWalletAddress(args.tokenModel.chain?.coinType ?: CoinType.ETHEREUM)
        val urlToOpen: String = when (args.tokenModel.chain) {
            Chain.BinanceSmartChain -> "https://bscscan.com/address/${addressWallet}"
            Chain.Ethereum -> "https://etherscan.io/address/${addressWallet}"
            Chain.OKC -> "https://web3.okx.com/explorer/x-layer/address/${addressWallet}"
            Chain.Polygon -> "https://polygonscan.com/address/${addressWallet}"
            Chain.Bitcoin -> "https://btcscan.org/address/${addressWallet}"
            Chain.Optimism -> "https://optimistic.etherscan.io/address/${addressWallet}"
            Chain.Arbitrum -> "https://arbiscan.io/address/${addressWallet}"
            Chain.Avalanche -> "https://subnets.avax.network/c-chain/address/${addressWallet}"
            Chain.BaseMainnet -> "https://basescan.org/address/${addressWallet}"
            else -> ""
        }
        return urlToOpen
    }

    override fun onStop() {
        loge("ONFINISH", "Here i am onStop")
        super.onStop()
    }

    override fun onDestroyView() {
        loge("ONFINISH", "Here i am onDestroyView")
        dataList.clear()
        mAdapter.transferList.clear()
        super.onDestroyView()
    }

}