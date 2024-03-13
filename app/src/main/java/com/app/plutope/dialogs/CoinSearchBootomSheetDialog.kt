package com.app.plutope.dialogs

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.plutope.R
import com.app.plutope.databinding.DialogCoinSearchListBinding
import com.app.plutope.model.Tokens
import com.app.plutope.ui.adapter.SearchCoinListAdapter
import com.app.plutope.ui.base.BaseActivity
import com.app.plutope.ui.base.BaseBottomSheetDialog
import com.app.plutope.ui.fragment.token.TokenViewModel
import com.app.plutope.ui.fragment.transactions.buy.graph.GraphDetailViewModel
import com.app.plutope.utils.constant.COIN_GEKO_PLUTO_PE_SERVER_URL
import com.app.plutope.utils.constant.storedTokenList
import com.app.plutope.utils.extras.PreferenceHelper
import com.app.plutope.utils.hideLoader
import com.app.plutope.utils.loge
import com.app.plutope.utils.network.NetworkState
import com.app.plutope.utils.showLoader
import com.app.plutope.utils.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@AndroidEntryPoint
class CoinSearchBottomSheetDialog(
    var isFromGet: Boolean = false,
    var isFromSwap: Boolean = false,
    var fromNetWork: String? = "",
    var fromCurrency: String = "",
    var toNetworkPair: String = "",
    var payObj: Tokens = Tokens(),
    var getObj: Tokens = Tokens(),
    var dialogDismissListner: ((Tokens, Boolean) -> Unit)
) : BaseBottomSheetDialog() {
    private var trendingList: MutableList<Tokens> = mutableListOf()
    private lateinit var originalList: List<Tokens>
    private lateinit var selectedModel: Tokens
    private var adapter: SearchCoinListAdapter? = null
    lateinit var mContext: BaseActivity

    private val tokenViewModel: TokenViewModel by viewModels()
    private val graphDetailViewModel: GraphDetailViewModel by viewModels()
    private var dataList: MutableList<Tokens> = mutableListOf()
    private var paireExchangeTokens: MutableList<Tokens> = mutableListOf()

    @Inject
    lateinit var preferenceHelper: PreferenceHelper
    var currentPage = 1
    var isLastPage = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context as BaseActivity
    }

    companion object {
        const val TOKEN_MODEL = "token_model"
        const val IS_FROM_GET = "is_from_get"
    }

    private var binding: DialogCoinSearchListBinding? = null
    val args: CoinSearchBottomSheetDialogArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.dialog_coin_search_list, container, false)
        binding!!.root.setBackgroundColor(Color.TRANSPARENT)

        return binding!!.root
    }

    fun setSwapPairTokenList(list: MutableList<Tokens>) {
        paireExchangeTokens.clear()
        paireExchangeTokens.addAll(list)
    }

    override fun setUpObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                tokenViewModel.updateTokenResp.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            hideLoader()
                            if (!isFromSwap) {
                                dialogDismissListner.invoke(it.data!!, false)
                                dialog?.dismiss()
                            }

                        }

                        is NetworkState.Loading -> {
                            dialog?.context?.showLoader()
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
                                currentPage += 1
                                val commonIds = cryptoList.map { it.id }
                                val commonData = commonIds.flatMap { id ->
                                    originalList.filter {
                                        it.tokenId == id && it.t_address != "0x0000000000000000000000000000000000001010"
                                    }
                                }
                                if (!isFromGet) {
                                    loge("Dashboard_Search", "size : ${cryptoList.size}")
                                    storedTokenList = commonData
                                    trendingList.addAll(commonData)
                                    setDataListRecyclerView(commonData)
                                }
                            } else {
                                isLastPage = true
                            }
                            // binding?.progressToken?.visibility = GONE
                            stopShimmerEffect()
                        }

                        is NetworkState.Loading -> {
                            if (currentPage == 1)
                                startShimmerEffect()
                            //binding?.progressToken?.visibility = VISIBLE

                        }

                        is NetworkState.Error -> {
                            binding?.root?.showSnackBar(networkState.message.toString())
                            //binding?.progressToken?.visibility = GONE
                            stopShimmerEffect()
                        }

                        is NetworkState.SessionOut -> {}

                        else -> {
                            // binding?.progressToken?.visibility = GONE
                            stopShimmerEffect()
                        }
                    }
                }
            }
        }
    }

    override fun setUpUI() {
        startShimmerEffect()
        adapter = SearchCoinListAdapter(isFromSwap = isFromSwap, providerClick = { model ->
            lifecycleScope.launch(Dispatchers.Main) {
                selectedModel = model
                val bundle = Bundle()
                bundle.putParcelable(TOKEN_MODEL, model)
                bundle.putBoolean(IS_FROM_GET, isFromGet)

                if (!isFromSwap) {
                    tokenViewModel.executeUpdateToken(model)
                } else {
                    dialogDismissListner.invoke(model, isFromGet)
                    dialog?.dismiss()
                }
            }
        })

        lifecycleScope.launch(Dispatchers.IO) {
            originalList = tokenViewModel.getAllDisableTokens()
            requireActivity().runOnUiThread {
                if (isFromSwap) {
                    getAllTokenList()
                } else {
                    if (storedTokenList.isEmpty()) {
                        fetchTrendingTokens(currentPage)
                    } else {
                        startShimmerEffect()

                        loge("Enable", "Tokens=>${tokenViewModel.getEnableTokens(1)}")

                        setDataListRecyclerView(storedTokenList)
                    }


                }
            }

        }
        binding?.edtSearch?.doAfterTextChanged {
            filters(it.toString(), dataList.toMutableList())
        }

        val layoutManager = LinearLayoutManager(requireContext())
        binding?.rvShiftTypeList?.layoutManager = layoutManager
        binding?.rvShiftTypeList?.adapter = adapter

        binding?.rvShiftTypeList?.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {

                    /*
                                        if (!isLastPage && lastVisibleItemPosition == totalItemCount - 1 && !isLastPage) {
                                           // currentPage += 1
                                            if (isFromSwap) {
                                               // getAllTokenList()
                                            } else {
                                                fetchTrendingTokens(currentPage)
                                            }
                                        }
                    */
                }
            }
        })

    }

    private fun filters(text: String, list: MutableList<Tokens>) {
        val filterList = mutableListOf<Tokens>()
        val exactMatchList = mutableListOf<Tokens>()

        val searchText = text.lowercase()
        val listSearch =
            if (isFromSwap) list.filter { it.t_address != "0x0000000000000000000000000000000000001010" } else originalList.filter { it.t_address != "0x0000000000000000000000000000000000001010" }

        for (i in listSearch) {
            val symbol = i.t_symbol?.lowercase()
            if (symbol != null && symbol.contains(searchText)) {
                if (symbol == searchText) {
                    exactMatchList.add(i)
                } else {
                    filterList.add(i)
                }
            }
        }

        val sortedList =
            if (!isFromSwap && searchText.isEmpty()) trendingList else (exactMatchList + filterList).distinct()
        var sortList = sortedList.sortedByDescending { it.t_balance.toBigDecimalOrNull() }
            .distinctBy { it.t_pk }
        if (isFromSwap && isFromGet) {
            sortList = sortList.filter { it.t_pk != payObj.t_pk }
        }
        adapter?.submitList(sortList.distinct())
        if (sortedList.isNotEmpty()) {
            // binding?.rvShiftTypeList?.smoothScrollBy((adapter?.itemCount!! - 1), 0)
            // binding?.rvShiftTypeList?.smoothScrollToPosition(0)
            //  binding?.rvShiftTypeList?.scrollToPosition(0)

            binding?.rvShiftTypeList?.layoutManager?.scrollToPosition(0)
            adapter?.notifyDataSetChanged()
            //  binding?.rvShiftTypeList?.layoutManager?.scrollToPosition(0)
        }
    }

    private fun getAllTokenList() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val list =
                    if (isFromSwap) tokenViewModel.getAllTokensList() else tokenViewModel.getAllDisableTokens()
                list.sortedByDescending { it.t_balance }
                if (list.isNotEmpty()) {
                    if (!isFromGet) {
                        setDataListRecyclerView(list)

                    } else {
                        if (fromNetWork == "okt") {
                            val tokenList = tokenViewModel.getAllTokensList() as MutableList<Tokens>
                            val newTokenList = tokenList.filter { it.t_type == "KIP20" }
                            requireActivity().runOnUiThread {
                                if (newTokenList.isNotEmpty()) {
                                    setDataListRecyclerView(newTokenList)
                                }
                            }

                        } else {
                            val tokenList = tokenViewModel.getAllTokensList() as MutableList<Tokens>
                            val newTokenList = tokenList.filter { it.t_type == "KIP20" }

                            requireActivity().runOnUiThread {

                                // setDataListRecyclerView(paireExchangeTokens)
                                setDataListRecyclerView(list)
                            }
                        }
                    }
                }
            }
        }


    }

    private fun setDataListRecyclerView(list: List<Tokens>) {
        requireActivity().runOnUiThread {
            dataList.addAll(list)
            binding?.rvShiftTypeList?.visibility = VISIBLE
            var listSort = dataList.sortedByDescending { it.t_balance.toBigDecimalOrNull() }
                .distinctBy { it.t_pk }
            if (isFromSwap && isFromGet) {
                listSort = listSort.filter { it.t_pk != payObj.t_pk }
            }
            adapter?.submitList(listSort.distinct())
            adapter?.notifyDataSetChanged()
            stopShimmerEffect()

        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        binding?.edtSearch?.setText("")
        dialog.dismiss()
    }

    private fun fetchTrendingTokens(currentPage: Int) {
        //graphDetailViewModel.executeGetMarketResponse("$COIN_GEKO_MARKET_API?vs_currency=${preferenceHelper.getSelectedCurrency()?.code}&sparkline=false&locale=en&page=$currentPage&per_page=250")
        graphDetailViewModel.executeGetMarketResponse("$COIN_GEKO_PLUTO_PE_SERVER_URL${preferenceHelper.getSelectedCurrency()?.code}&sparkline=false&locale=en&ids=&page=$currentPage&per_page=250")
    }

    private fun startShimmerEffect() {
        binding?.shimmerLayout?.startShimmer()
        binding?.shimmerLayout?.visibility = VISIBLE
        binding?.rvShiftTypeList?.visibility = GONE

    }

    private fun stopShimmerEffect() {
        binding?.shimmerLayout?.stopShimmer()
        binding?.shimmerLayout?.visibility = View.INVISIBLE
        binding?.rvShiftTypeList?.visibility = VISIBLE
    }
}