package com.app.plutope.ui.fragment.transactions.buy.buy_detail

import android.annotation.SuppressLint
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentBuyDetailsBinding
import com.app.plutope.dialogs.DialogSelectButtonList
import com.app.plutope.model.ButtonModel
import com.app.plutope.model.TransactionLists
import com.app.plutope.model.Wallet
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.ui.fragment.card.card_list.TransactionListAdapter
import com.app.plutope.utils.coinTypeEnum.CoinType
import com.app.plutope.utils.constant.OK_LINK_TRANSACTION_LIST
import com.app.plutope.utils.constant.buttonRampable
import com.app.plutope.utils.constant.buttonSell
import com.app.plutope.utils.constant.buttonSwap
import com.app.plutope.utils.customSnackbar.CustomSnackbar
import com.app.plutope.utils.extras.PreferenceHelper
import com.app.plutope.utils.hideLoader
import com.app.plutope.utils.loge
import com.app.plutope.utils.network.NetworkState
import com.app.plutope.utils.safeNavigate
import com.app.plutope.utils.setBalanceText
import com.app.plutope.utils.showLoader
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BuyDetails : BaseFragment<FragmentBuyDetailsBinding, BuyDetailsViewModel>() {

    private val buyDetailsViewModel: BuyDetailsViewModel by viewModels()
    private var transactionListAdapter: TransactionListAdapter? = null
    val args: BuyDetailsArgs by navArgs()
    var currentPage = 1
    var totalPages = 0
    var isLastPage = false
    var dataList: MutableList<TransactionLists> = mutableListOf()
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

        loge("Detail", "${args.tokenModel}")

        setTokenDetails()
        setOnClickLisners()
        transactionListAdapter = TransactionListAdapter(args.tokenModel.t_address.toString()) {
            currentPage = 1
            findNavController().safeNavigate(
                BuyDetailsDirections.actionBuyDetailsToTransfer(
                    args.tokenModel,
                    it
                )
            )
        }

        fetchTransactionListData(currentPage)

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

        viewDataBinding?.imgSwap?.setOnClickListener {

            val list = mutableListOf<ButtonModel>()
            /**
             * Add sell option later
             *
            list.add(ButtonModel(2, buttonSell, R.drawable.ic_bank))
             */
            list.add(ButtonModel(1, buttonSwap, R.drawable.ic_swap_with_bg))
            list.add(ButtonModel(2, buttonSell, R.drawable.ic_bank))
            // list.add(ButtonModel(3, buttonRampable, R.drawable.img_logo_circle_black))

            DialogSelectButtonList.getInstance()?.show(requireContext(), list) {

                when (it.buttonName) {
                    buttonSwap -> {
                        findNavController().safeNavigate(
                            BuyDetailsDirections.actionBuyDetailsToSwap(
                                args.tokenModel
                            )
                        )

                    }

                    buttonSell -> {
                        /*findNavController().safeNavigate(
                            BuyDetailsDirections.actionBuyDetailsToSellFragment(args.tokenModel)
                        )*/

                        val url =
                            "https://webview.rampable.co/?clientSecret=KfoET5E31jh7iikwBwGfHNqB78mbmUmGEpzgVSOj2ovD4AKzuPmdRnX0Up4miXyx&useWalletConnect=true"

                        //  val url2 = "https://webview-api.rampable.co/?clientSecret=KfoET5E31jh7iikwBwGfHNqB78mbmUmGEpzgVSOj2ovD4AKzuPmdRnX0Up4miXyx&useWalletConnect=true"


                        findNavController().safeNavigate(
                            BuyDetailsDirections.actionBuyDetailsToBrowser(
                                url
                            )
                        )

                        // findNavController().safeNavigate(BuyDetailsDirections.actionBuyDetailsToSellWebView(url))


                    }

                    buttonRampable -> {
                        val url =
                            "https://webview-dev.rampable.co/?clientSecret=wpyYO6EyVSwx3QGY50d0VHCICTjiBHTTRGo7zbL6G6bxBtCSaGBrEbRB70ZhzdvP&useWalletConnect=true"

                        //  val url2 = "https://webview-api.rampable.co/?clientSecret=KfoET5E31jh7iikwBwGfHNqB78mbmUmGEpzgVSOj2ovD4AKzuPmdRnX0Up4miXyx&useWalletConnect=true"

                        /*  val url3 = "https://webview-KfoET5E31jh7iikwBwGfHNqB78mbmUmGEpzgVSOj2ovD4AKzuPmdRnX0Up4miXyx/?clientSecret=wpyYO6EyVSwx3QGY50d0VHCICTjiBHTTRGo7zbL6G6bxBtCSaGBrEbRB70ZhzdvP&useWalletConnect=true"
                          val url4 = "https://webview-dev.rampable.co/?clientSecret=KfoET5E31jh7iikwBwGfHNqB78mbmUmGEpzgVSOj2ovD4AKzuPmdRnX0Up4miXyx&useWalletConnect=true"
                        */

                        findNavController().safeNavigate(
                            BuyDetailsDirections.actionBuyDetailsToBrowser(
                                url
                            )
                        )

                    }
                }


            }

        }

        viewDataBinding!!.imgSend.setOnClickListener {
            findNavController().safeNavigate(
                BuyDetailsDirections.actionBuyDetailsToSendCoin(
                    args.tokenModel
                )
            )
        }

        val layoutManager = LinearLayoutManager(requireContext())
        viewDataBinding?.rvTransactionList?.layoutManager = layoutManager

        viewDataBinding?.rvTransactionList?.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount
                if (!recyclerView.canScrollVertically(1) && newState==RecyclerView.SCROLL_STATE_IDLE) {

                    if (!isLastPage && lastVisibleItemPosition == totalItemCount - 1 && currentPage < totalPages) {
                        currentPage += 1
                        fetchTransactionListData(currentPage)
                    }
                }
            }
        })


    }

    private fun fetchTransactionListData(currentPage: Int) {
        if (currentPage >= 2) {
            viewDataBinding?.progressPage?.visibility = VISIBLE
        }
        args.tokenModel.chain?.walletAddress =
            Wallet.getPublicWalletAddress(args.tokenModel.chain?.coinType!!)
        if (args.tokenModel.t_address != "") {

            buyDetailsViewModel.executeGetTransactionHistoryOkLink(
                "${OK_LINK_TRANSACTION_LIST}chainShortName=${args.tokenModel.chain?.chainName?.lowercase()}" +
                        "&address=${Wallet.getPublicWalletAddress(args.tokenModel.chain?.coinType!!)}&page=$currentPage&limit=50&protocolType=token_20",
                args.tokenModel
            )

/*
            buyDetailsViewModel.executeGetTransactionHistoryOkLink(
                "${OK_LINK_TRANSACTION_LIST}chainShortName=${args.tokenModel.chain?.chainName?.lowercase()}" +
                        "&address=1N52wHoVR79PMDishab2XmRHsbekCdGquK&page=$currentPage&limit=50&protocolType=token_20",
                args.tokenModel
            )
*/

        }else {
                        buyDetailsViewModel.executeGetTransactionHistoryOkLink(
                            "${OK_LINK_TRANSACTION_LIST}chainShortName=${args.tokenModel.chain?.chainName?.lowercase()}&address=${
                                Wallet.getPublicWalletAddress(
                                    args.tokenModel.chain?.coinType!!
                                )
                            }&page=$currentPage&limit=50&protocolType=", args.tokenModel
                        )

            /*
                        buyDetailsViewModel.executeGetTransactionHistoryOkLink(
                            "${OK_LINK_TRANSACTION_LIST}chainShortName=${args.tokenModel.chain?.chainName?.lowercase()}&address=1N52wHoVR79PMDishab2XmRHsbekCdGquK&page=$currentPage&limit=50&protocolType=",
                            args.tokenModel
                        )
            */
        }

    }


    @SuppressLint("SetTextI18n")
    private fun setTokenDetails() {
        val tokenModel = args.tokenModel

        PreferenceHelper.getInstance().getSelectedCurrency()?.symbol ?: ""
        if (tokenModel.isCustomTokens == true) {
            val img = when (tokenModel.t_type?.lowercase()) {
                "erc20" -> R.drawable.ic_erc
                "bep20" -> R.drawable.ic_bep
                "polygon" -> R.drawable.ic_polygon
                "kip20" -> R.drawable.ic_kip
                else -> {
                    R.drawable.ic_erc
                }
            }
            Glide.with(requireContext()).load(img).into(viewDataBinding?.imgCoin!!)
            viewDataBinding?.groupShowSendReceiveIcon?.visibility = GONE
        } else {
            Glide.with(requireContext()).load(tokenModel.t_logouri).into(viewDataBinding?.imgCoin!!)
            viewDataBinding?.groupShowSendReceiveIcon?.visibility = VISIBLE
        }

        viewDataBinding?.txtToolbarTitle?.text = tokenModel.t_name

        viewDataBinding?.txtBalance?.text = setBalanceText(
            tokenModel.t_balance.toBigDecimal() ?: 0.toBigDecimal(),
            tokenModel.t_symbol.toString(),
            7
        )
        viewDataBinding?.txtNetworkName?.text = tokenModel.t_type


        val priceDouble = tokenModel.t_price?.toDoubleOrNull() ?: 0.0
        val priceText = String.format("%.2f", priceDouble)
        val percentChange = tokenModel.t_last_price_change_impact?.toDoubleOrNull() ?: 0.0
        val color = if (percentChange < 0.0) context?.resources!!.getColor(
            R.color.red,
            null
        ) else context?.resources!!.getColor(R.color.green_099817, null)

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
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                buyDetailsViewModel.transactionHistoryOkLinkResponse.collect { it ->
                    when (it) {
                        is NetworkState.Success -> {
                            hideLoader()
                            if (it.data?.transactionLists?.isNotEmpty() == true) {
                                if (it.data.transactionLists.isNotEmpty()) {

                                    if (!PreferenceHelper.getInstance().isActiveWallet) {
                                        buyDetailsViewModel.setWalletActiveCall(
                                            Wallet.getPublicWalletAddress(
                                                CoinType.ETHEREUM
                                            )!!
                                        )
                                    }
                                    currentPage = it.data.page.toInt()
                                    totalPages = it.data.totalPage.toInt()
                                    if (currentPage == totalPages) {
                                        isLastPage = true
                                    }
                                    viewDataBinding?.rvTransactionList?.visibility = VISIBLE
                                    viewDataBinding?.layoutNoFound?.visibility = GONE
                                    val respList = it.data.transactionLists
                                    dataList.addAll(respList)
                                    if (args.tokenModel.t_address != "") {
                                        dataList = dataList.filter {
                                            it.methodId == "" && args.tokenModel.t_symbol == it.transactionSymbol
                                        } as MutableList<TransactionLists>
                                    }
                                    if (dataList.isEmpty()) {
                                        viewDataBinding?.rvTransactionList?.visibility = GONE
                                        viewDataBinding?.layoutNoFound?.visibility = VISIBLE
                                    }

                                    transactionListAdapter?.submitList(dataList.distinctBy { it.txId }
                                        .distinctBy { it.transactionTimeInMillis })
                                    if (transactionListAdapter?.currentList!!.size < 10) {
                                        currentPage += 1
                                        fetchTransactionListData(currentPage)
                                    }

                                    viewDataBinding!!.rvTransactionList.adapter =
                                        transactionListAdapter
                                } else {
                                    viewDataBinding?.rvTransactionList?.visibility = GONE
                                    viewDataBinding?.layoutNoFound?.visibility = VISIBLE
                                }
                            }
                            viewDataBinding?.progressPage?.visibility = GONE

                            if(transactionListAdapter?.currentList?.size==0){
                                viewDataBinding?.rvTransactionList?.visibility = GONE
                                viewDataBinding?.layoutNoFound?.visibility = VISIBLE
                            }
                        }
                        is NetworkState.Loading -> {
                            if (currentPage == 1)
                                requireContext().showLoader()
                        }
                        is NetworkState.Error -> {
                            viewDataBinding?.progressPage?.visibility = GONE
                            hideLoader()
                        }
                        is NetworkState.SessionOut -> {
                            hideLoader()
                            CustomSnackbar.make(requireActivity().window.decorView.rootView as ViewGroup, it.message.toString())
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

        GlobalScope.launch(Dispatchers.IO) {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                buyDetailsViewModel.setWalletActive.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            hideLoader()

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


    }


}