package com.app.plutope.ui.fragment.transactions.receive

import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentReceiveBinding
import com.app.plutope.model.Tokens
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.ui.fragment.token.TokenViewModel
import com.app.plutope.ui.fragment.transactions.send.CoinListAdapter
import com.app.plutope.utils.constant.AddCustomTokenPageType
import com.app.plutope.utils.constant.defaultPLTTokenId
import com.app.plutope.utils.constant.nftPageType
import com.app.plutope.utils.loge
import com.app.plutope.utils.safeNavigate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class Receive : BaseFragment<FragmentReceiveBinding, TokenViewModel>() {

    private val receiveViewModel: TokenViewModel by viewModels()
    private var adapter: CoinListAdapter? = null
    private var dataList: MutableList<Tokens> = mutableListOf()

    val args: ReceiveArgs by navArgs()

    companion object {
        const val keyReceiveCustomToken = "keyReceiveCustomToken"
        const val keyReceive = "keyReceive"

    }

    override fun getViewModel(): TokenViewModel {
        return receiveViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.receiveViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_receive
    }

    override fun setupToolbarText(): String {
        return if (args.pageType == nftPageType) "Receive NFT" else if (args.pageType == AddCustomTokenPageType) "Network" else getString(
            R.string.receive
        )
    }

    override fun setupUI() {
        //  (activity as BaseActivity).showToolbarTransparentBack()

        viewDataBinding?.txtToolbarTitle?.text =
            if (args.pageType == nftPageType) "Receive NFT" else if (args.pageType == AddCustomTokenPageType) "Network" else getString(
                R.string.receive
            )

        adapter = CoinListAdapter(providerClick = { model ->
            if (args.pageType == AddCustomTokenPageType) {
                val bundle = Bundle()
                bundle.putParcelable(keyReceive, model)
                setFragmentResult(keyReceiveCustomToken, bundle)
                findNavController().popBackStack()
            } else {
                findNavController().safeNavigate(ReceiveDirections.actionReceiveToReceiveCoin(model))
            }
        })

        viewDataBinding!!.rvReceiveCoinList.adapter = adapter
        // getAllTokenList()

        viewDataBinding!!.edtSearch.doAfterTextChanged {
            filters(it.toString(), dataList)
        }
        viewDataBinding!!.imgBack.setOnClickListener {
            findNavController().navigateUp()
        }

        if (receiveViewModel.tokenList.value == null) {
            viewDataBinding?.shimmerLayout?.visibility = View.VISIBLE
            receiveViewModel.fetchAllTokensList() // Fetch initially if null
        }

    }

    override fun setupObserver() {
        receiveViewModel.tokenList.observe(viewLifecycleOwner) { tokens ->
            updateUI(tokens)
        }
    }

    //    private fun filters(text: String, list: MutableList<Tokens>) {
//        val filterList = ArrayList<Tokens>()
//        val exactMatchList = ArrayList<Tokens>()
//
//        val searchText = text.lowercase()
//        if (searchText != "") {
//            for (i in list) {
//                val symbol = i.t_symbol.lowercase()
//                if (symbol.contains(searchText) == true) {
//                    if (symbol == searchText) {
//                        exactMatchList.add(i)
//                    } else {
//                        filterList.add(i)
//                    }
//                }
//            }
//            val sortedList = exactMatchList + filterList
//            adapter?.submitList(sortedList)
//        } else {
//
//            val lastList =
//                list.filter { it.isEnable == true || it.t_balance.toDouble() > 0.0 }.toMutableList()
//            val pltToken = lastList.find { it.tokenId == defaultPLTTokenId }
//            if (pltToken != null) {
//                lastList.remove(pltToken)
//                lastList.add(0, pltToken)
//            }
//
//            adapter?.submitList(lastList /*list.filter { it.isEnable == true || it.t_balance.toDouble() > 0.0 }*/)
//        }
//    }
    private fun filters(text: String, list: MutableList<Tokens>) {
        val filterList = ArrayList<Tokens>()
        val exactMatchList = ArrayList<Tokens>()

        val searchText = text.lowercase()
        val allowedSymbols = setOf("ETH", "POL", "BNB", "OKT", "OP", "ARB", "AVAX", "BASE")

        if (searchText.isNotEmpty()) {
            for (i in list) {
                val symbol = i.t_symbol.lowercase()
                if (symbol.contains(searchText)) {
                    if (symbol == searchText) {
                        exactMatchList.add(i)
                    } else {
                        filterList.add(i)
                    }
                }
            }
            val sortedList = exactMatchList + filterList
            adapter?.submitList(sortedList)
        } else {
            // Apply the new filtering condition
            val lastList = list.filter {
                (it.isEnable == true || it.t_balance.toDouble() > 0.0) &&
                        (it.t_address.isEmpty() && allowedSymbols.contains(it.t_symbol.uppercase()))
            }.toMutableList()

            val pltToken = lastList.find { it.tokenId == defaultPLTTokenId }
            if (pltToken != null) {
                lastList.remove(pltToken)
                lastList.add(0, pltToken)
            }

            adapter?.submitList(lastList)
        }
    }


    private fun updateUI(tokens: List<Tokens>) {
        CoroutineScope(Dispatchers.IO).launch {
            val sortedList = tokens.sortedByDescending { it.t_balance.toBigDecimalOrNull() }
                .distinctBy { it.t_pk }

            val filteredList =
                sortedList.filter { it.isEnable == true || it.t_balance.toDouble() > 0.0 }
                    .toMutableList()

            if (tokens.isNotEmpty()) {
                dataList.clear()
                if (args.pageType == nftPageType || args.pageType == AddCustomTokenPageType) {
                    if (args.pageType == AddCustomTokenPageType) {
                        dataList.addAll(sortedList.filter { it.t_address == "" })
                        loge("AddCustomTokenPageType", "updateUI: ${dataList}")
                    } else {
                        dataList.addAll(sortedList.filter {
                            it.t_address == "" && it.t_name.lowercase() != getString(
                                R.string.okt_chain
                            ).lowercase()
                        })
                    }
                } else {
                    dataList.addAll(sortedList)
                }

                CoroutineScope(Dispatchers.Main).launch {
                    viewDataBinding?.shimmerLayout?.visibility = View.GONE
                    viewDataBinding?.rvReceiveCoinList?.visibility = View.VISIBLE

                    val pltToken = filteredList.find { it.tokenId == defaultPLTTokenId }
                    if (pltToken != null) {
                        filteredList.remove(pltToken)
                        filteredList.add(0, pltToken)
                    }
                    if (args.pageType == nftPageType || args.pageType == AddCustomTokenPageType) {
                        adapter?.submitList(dataList)
                    } else {

                        loge(
                            "filteredList",
                            "${filteredList.filter { it.t_symbol.lowercase() == "arb" }}"
                        )

                        adapter?.submitList(filteredList.distinct())
                    }
                    viewDataBinding!!.rvReceiveCoinList.smoothScrollToPosition(0)
                }


            }
        }

    }


}