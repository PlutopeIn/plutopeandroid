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
import com.app.plutope.R
import com.app.plutope.databinding.DialogCoinSearchListBinding
import com.app.plutope.model.Tokens
import com.app.plutope.ui.adapter.SearchCoinListAdapter
import com.app.plutope.ui.base.BaseActivity
import com.app.plutope.ui.base.BaseBottomSheetDialog
import com.app.plutope.ui.fragment.token.TokenViewModel
import com.app.plutope.ui.fragment.transactions.buy.graph.GraphDetailViewModel
import com.app.plutope.utils.constant.COIN_GEKO_PLUTO_PE_SERVER_URL
import com.app.plutope.utils.constant.defaultPLTTokenId
import com.app.plutope.utils.constant.storedTokenList
import com.app.plutope.utils.date_formate.toAny
import com.app.plutope.utils.date_formate.ymd
import com.app.plutope.utils.extras.PreferenceHelper
import com.app.plutope.utils.hideLoader
import com.app.plutope.utils.loge
import com.app.plutope.utils.network.NetworkState
import com.app.plutope.utils.showLoader
import com.app.plutope.utils.showSnackBar
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject


@AndroidEntryPoint
class CoinSearchBottomSheetDialog : BaseBottomSheetDialog() {

    private var isFromGet: Boolean = false
    var isFromSwap: Boolean = false
    var fromNetWork: String? = ""
    var fromCurrency: String = ""
    var toNetworkPair: String = ""
    var payObj: Tokens = Tokens()
    var getObj: Tokens = Tokens()
    var dialogDismissListner: ((Tokens, Boolean) -> Unit)? = null


    private var allTokenList: MutableList<Tokens> = arrayListOf()
    private var trendingList: MutableList<Tokens> = mutableListOf()
    private var originalList: List<Tokens> = mutableListOf()
    private var selectedModel: Tokens? = Tokens()
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


    companion object {
        const val TOKEN_MODEL = "token_model"
        const val IS_FROM_GET = "is_from_get"

        const val IS_FROM_SWAP = "is_from_swap"
        const val FROM_NETWORK = "from_network"
        const val FROM_CURRENCY = "from_currency"
        const val TO_NETWORK_PAIR = "to_network_pair"
        const val PAY_OBJ = "pay_obj"
        const val GET_OBJ = "get_obj"

        fun newInstance(
            isFromGet: Boolean,
            isFromSwap: Boolean,
            fromNetWork: String? = "",
            fromCurrency: String = "",
            toNetworkPair: String = "",
            payObj: Tokens = Tokens(),
            getObj: Tokens = Tokens(),
            dialogDismissListner: ((Tokens, Boolean) -> Unit)
        ): CoinSearchBottomSheetDialog {
            val fragment = CoinSearchBottomSheetDialog()

            loge("CBSINIT", "newInstance: $isFromGet")

            val args = Bundle().apply {
                putBoolean(IS_FROM_GET, isFromGet)
                putBoolean(IS_FROM_SWAP, isFromSwap)
                putString(FROM_NETWORK, fromNetWork)
                putString(FROM_CURRENCY, fromCurrency)
                putString(TO_NETWORK_PAIR, toNetworkPair)
                putParcelable(PAY_OBJ, payObj) // Ensure Tokens implements Parcelable
                putParcelable(GET_OBJ, getObj) // Ensure Tokens implements Parcelable
            }
            fragment.arguments = args
            fragment.dialogDismissListner = dialogDismissListner // Set listener
            return fragment
        }

    }

    private var binding: DialogCoinSearchListBinding? = null
    val args: CoinSearchBottomSheetDialogArgs by navArgs()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context as BaseActivity
        arguments?.let {
            isFromGet = it.getBoolean(IS_FROM_GET)
            isFromSwap = it.getBoolean(IS_FROM_SWAP)
            fromNetWork = it.getString(FROM_NETWORK)
            fromCurrency = it.getString(FROM_CURRENCY) ?: ""
            toNetworkPair = it.getString(TO_NETWORK_PAIR) ?: ""
            payObj = it.getParcelable(PAY_OBJ) ?: Tokens()
            getObj = it.getParcelable(GET_OBJ) ?: Tokens()
        }
    }

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

        tokenViewModel.tokenList.observe(viewLifecycleOwner) { tokens ->
            allTokenList = tokens as MutableList<Tokens>
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                tokenViewModel.updateTokenResp.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            hideLoader()
                            loge("ForAssets", "_3_${isFromGet} :: $isFromSwap")

                            if (!isFromSwap) {
                                loge("ForAssets", "_4_${isFromGet} :: $isFromSwap :: ${it.data!!}")
                                dialogDismissListner?.invoke(it.data, false)
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

                            CoroutineScope(Dispatchers.IO).launch {

                                val cryptoList = networkState.data
                                if (cryptoList?.isNotEmpty() == true) {
                                    currentPage += 1
                                    val commonIds = cryptoList.map { it.id }

                                    preferenceHelper.lastTrendingTokenList =
                                        Gson().toJson(commonIds)
                                    val commonData = commonIds.flatMap { id ->
                                        originalList.filter {
                                            it.tokenId == id && it.t_address != "0x0000000000000000000000000000000000001010"
                                        }
                                    }
                                    if (!isFromGet) {
                                        loge("Dashboard_Search", "size : ${cryptoList.size}")
                                        preferenceHelper.lastLoadTrendingTokenDate =
                                            Calendar.getInstance().toAny(ymd)
                                        storedTokenList = commonData
                                        trendingList.addAll(commonData)
                                        setDataListRecyclerView(commonData)
                                    }


                                } else {
                                    isLastPage = true
                                }
                                // binding?.progressToken?.visibility = GONE
                                CoroutineScope(Dispatchers.Main).launch {
                                    stopShimmerEffect()
                                }
                            }
                        }

                        is NetworkState.Loading -> {
                            if (currentPage == 1)
                                startShimmerEffect()
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
        adapter =
            SearchCoinListAdapter(arrayListOf(), isFromSwap = isFromSwap, providerClick = { model ->
                lifecycleScope.launch(Dispatchers.Main) {
                    selectedModel = model
                    val bundle = Bundle()
                    bundle.putParcelable(TOKEN_MODEL, model)
                    bundle.putBoolean(IS_FROM_GET, isFromGet)
                    if (!isFromSwap) {
                        tokenViewModel.executeUpdateToken(model)
                    } else {
                        dialogDismissListner?.invoke(model, isFromGet)
                        dialog?.dismiss()
                    }
                }
            })

        tokenViewModel.fetchAllTokensList()


        lifecycleScope.launch(Dispatchers.IO) {
            originalList = tokenViewModel.getAllDisableTokens()
            loge("originalList : ${originalList.filter { it.t_pk == 1 }}")
            lifecycleScope.launch(Dispatchers.Main) {
                if (isFromSwap) {
                    getAllTokenList()
                } else {
                    if (storedTokenList.isEmpty()) {
                        CoroutineScope(Dispatchers.IO).launch {
                            if (originalList.isNotEmpty() && preferenceHelper.lastTrendingTokenList != "" && preferenceHelper.lastLoadTrendingTokenDate == Calendar.getInstance()
                                    .toAny(ymd)
                            ) {
                                val type = object : TypeToken<List<String>>() {}.type
                                val commonIds: List<String> =
                                    Gson().fromJson(preferenceHelper.lastTrendingTokenList, type)
                                val commonData = commonIds.flatMap { id ->
                                    originalList.filter {
                                        it.tokenId == id && it.t_address != "0x0000000000000000000000000000000000001010"
                                    }
                                }
                                if (!isFromGet) {
                                    storedTokenList = commonData
                                    trendingList.addAll(commonData)
                                    setDataListRecyclerView(commonData)
                                }
                            } else {
                                fetchTrendingTokens(currentPage)
                            }
                        }
                    } else {
                        startShimmerEffect()
                        trendingList.addAll(storedTokenList)
                        setDataListRecyclerView(storedTokenList)
                    }
                }
            }
        }
        binding?.edtSearch?.doAfterTextChanged {
            if (trendingList.isEmpty()) {
                trendingList.addAll(storedTokenList)
            }
            if (dataList.isNotEmpty()) {
                filters(it.toString(), dataList.toMutableList())
            }
        }
        val layoutManager = LinearLayoutManager(requireContext())
        binding?.rvShiftTypeList?.layoutManager = layoutManager
        binding?.rvShiftTypeList?.adapter = adapter
    }

    private fun filters(text: String, list: MutableList<Tokens>) {
        // val filterList = mutableListOf<Tokens>()
        // val exactMatchList = mutableListOf<Tokens>()

        val nameMatchList = mutableListOf<Tokens>()
        val symbolMatchList = mutableListOf<Tokens>()
        val typeMatchList = mutableListOf<Tokens>()
        val exactMatchList = mutableListOf<Tokens>()

        val searchText = text.lowercase()
        val listSearch =
            if (isFromSwap) list.filter { it.t_address != "0x0000000000000000000000000000000000001010" } else originalList.filter { it.t_address != "0x0000000000000000000000000000000000001010" }
        /* for (i in listSearch) {
             val symbol = i.t_symbol.lowercase()
             if (symbol != "" && symbol.contains(searchText) || i.t_type.lowercase()
                     .contains(searchText) || i.t_name.replace("-", "").lowercase()
                     .contains(searchText)
             ) {
                 if (symbol != "" && symbol == searchText) {
                     exactMatchList.add(i)
                 } else {
                     filterList.add(i)
                 }
             }
         }*/

        for (i in listSearch) {
            val name = i.t_name.lowercase()
            val symbol = i.t_symbol.replace("-", "").lowercase()
            val type = i.t_type.lowercase()

            when {
                name == searchText -> {
                    exactMatchList.add(i)
                }

                name.contains(searchText) -> {
                    nameMatchList.add(i)
                }

                symbol == searchText -> {
                    exactMatchList.add(i)
                }

                symbol.contains(searchText) -> {
                    symbolMatchList.add(i)
                }

                type.contains(searchText) -> {
                    typeMatchList.add(i)
                }
            }
        }

        val filterList = mutableListOf<Tokens>().apply {
            addAll(symbolMatchList)
            addAll(exactMatchList)
            addAll(nameMatchList)
            addAll(typeMatchList)
        }

        val sortedList =
            if (!isFromSwap && searchText.isEmpty()) (exactMatchList + trendingList).distinct() else (exactMatchList + filterList).distinct()

        var sortList = sortedList.sortedByDescending { it.t_balance.toBigDecimalOrNull() }
            .distinctBy { it.t_pk }.toMutableList()
        sortList = if (isFromSwap && isFromGet) {
            sortList.filter { it.t_pk != payObj.t_pk }.toMutableList()
        } else {
            sortList.filter { it != getObj }.toMutableList()
        }

        if (sortedList.isEmpty()) {
            binding?.noDataAvailable?.visibility = VISIBLE
        } else {
            binding?.noDataAvailable?.visibility = GONE
        }

        /* val pltToken = sortList.find { it.tokenId == defaultPLTTokenId }
         if (pltToken != null) {
              sortList.remove(pltToken)
         }*/

        adapter?.updateData(
            //sortList.distinct().filter { (it.t_name != "Base" && it.t_address != "") }.toMutableList()
            sortList.distinct().toMutableList()
        )
    }

    private fun getAllTokenList() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                if (allTokenList.isEmpty()) {
                    tokenViewModel.fetchAllTokensList()
                    while (allTokenList.isEmpty()) {
                        delay(100)
                    }
                }
                val list = if (isFromSwap) {
                    allTokenList
                } else {
                    tokenViewModel.getAllDisableTokens()
                }.sortedByDescending { it.t_balance }

                if (list.isNotEmpty()) {
                    if (!isFromGet) {
                        setDataListRecyclerView(list)
                    } else {
                        handleDataForGet(list)
                    }
                }
            }
        }
    }

    private suspend fun handleDataForGet(list: List<Tokens>) {
        if (fromNetWork == "okt") {
            val tokenList = allTokenList
            val newTokenList = tokenList.filter { it.t_type == "KIP20" }
            withContext(Dispatchers.Main) {
                if (newTokenList.isNotEmpty()) {
                    setDataListRecyclerView(newTokenList)
                }
            }
        } else {
            withContext(Dispatchers.Main) {
                setDataListRecyclerView(list)
            }
        }
    }


    private suspend fun setDataListRecyclerView(list: List<Tokens>) {
        withContext(Dispatchers.Main) {
            dataList.addAll(list)
            binding?.rvShiftTypeList?.visibility = VISIBLE
            var listSort = dataList.sortedByDescending { it.t_balance.toBigDecimalOrNull() }
                .distinctBy { it.t_pk }.toMutableList()

            if (isFromSwap && isFromGet) {
                listSort = listSort.filter { it.t_pk != payObj.t_pk }.toMutableList()

                val pltToken = listSort.find { it.tokenId == defaultPLTTokenId }
                loge("Mon", "Get ${pltToken}")
                if (pltToken != null) {
                    listSort.remove(pltToken)
                    // lastList.add(0, pltToken)
                }

            } else if (isFromSwap && !isFromGet) {

                listSort = listSort.filter { it != getObj }.toMutableList()
                val pltToken = listSort.find { it.tokenId == defaultPLTTokenId }
                loge("Mon", "NotGet ${pltToken}")
                if (pltToken != null) {
                    listSort.remove(pltToken)
                }
            } else {
                // listSort = listSort.filter { it != getObj }.toMutableList()

                val pltToken = listSort.find { it.tokenId == defaultPLTTokenId }
                loge("Mon", "JJJJ ${pltToken}")
                if (pltToken != null) {
                    listSort.remove(pltToken)
                    listSort.add(0, pltToken)
                }
            }
            loge("LoadRecycler", "$isFromSwap :: $isFromGet  :: $getObj :: ${listSort.size}")

            adapter?.list?.addAll(
                listSort.distinct().filter { (it.t_name != "Base" && it.t_address != "") })
            //adapter?.list?.addAll(listSort.distinct().distinctBy { it.t_name == "Base" /*&& it.t_address != "")*/ })
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
        graphDetailViewModel.executeGetMarketResponse("$COIN_GEKO_PLUTO_PE_SERVER_URL${preferenceHelper.getSelectedCurrency()?.code}&sparkline=false&locale=en&ids=&page=$currentPage&per_page=250")
    }

    private fun startShimmerEffect() {
        CoroutineScope(Dispatchers.Main).launch {
            binding?.shimmerLayout?.startShimmer()
            binding?.shimmerLayout?.visibility = VISIBLE
            binding?.rvShiftTypeList?.visibility = GONE
        }

    }

    private fun stopShimmerEffect() {
        CoroutineScope(Dispatchers.Main).launch {
            binding?.shimmerLayout?.stopShimmer()
            binding?.shimmerLayout?.visibility = View.INVISIBLE
            binding?.rvShiftTypeList?.visibility = VISIBLE

            loge("lastCheck", "${adapter?.list?.size}")
        }
    }
}