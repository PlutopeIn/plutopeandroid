package com.app.plutope.ui.fragment.transactions.buy

import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentBuyBinding
import com.app.plutope.model.Tokens
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.ui.fragment.token.TokenViewModel
import com.app.plutope.ui.fragment.transactions.send.CoinListAdapter
import com.app.plutope.utils.constant.defaultPLTTokenId
import com.app.plutope.utils.constant.pageTypeBuy
import com.app.plutope.utils.constant.pageTypeSwap
import com.app.plutope.utils.safeNavigate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class Buy : BaseFragment<FragmentBuyBinding, TokenViewModel>() {

    private val buyViewModel: TokenViewModel by viewModels()
    private var coinAdapter: CoinListAdapter? = null
    private var dataList: MutableList<Tokens> = mutableListOf()

    val args: BuyArgs by navArgs()

    override fun getViewModel(): TokenViewModel {
        return buyViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.buyViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_buy
    }

    override fun setupToolbarText(): String {
        return ""
    }

    override fun setupUI() {
        viewDataBinding!!.imgBack.setOnClickListener {
            findNavController().popBackStack()
        }
        viewDataBinding!!.txtToolbarTitle.text =
            if (args.pageType == pageTypeSwap) getString(R.string.swap) else getString(R.string.buy)
        coinAdapter = CoinListAdapter(providerClick = { model ->
            if (args.pageType == pageTypeSwap) {
                findNavController().safeNavigate(BuyDirections.actionBuyToSwap(model))
            } else if (args.pageType == pageTypeBuy) {
                findNavController().safeNavigate(BuyDirections.actionBuyToBuyBTC(model))
            } else {
                findNavController().safeNavigate(BuyDirections.actionBuyToBuyDetails(model))
            }
        })
        viewDataBinding!!.rvSendCoinList.adapter = coinAdapter

        viewDataBinding!!.edtSearch.doAfterTextChanged {
            filters(it.toString(), dataList)
        }

        if (buyViewModel.tokenList.value == null) {
            viewDataBinding?.shimmerLayout?.visibility = View.VISIBLE
            buyViewModel.fetchAllTokensList() // Fetch initially if null
        }

    }


    override fun setupObserver() {
        buyViewModel.tokenList.observe(viewLifecycleOwner) { tokens ->
            updateUI(tokens.toMutableList())
        }
    }

    private fun filters(text: String, list: MutableList<Tokens>) {
        val filterList = ArrayList<Tokens>()
        if (text != "") {
            for (i in list)
                if (i.t_name.lowercase().contains(text.lowercase()) || i.t_symbol.lowercase()
                        .contains(text.lowercase())
                ) {
                    filterList.add(i)
                }
            coinAdapter?.submitList(filterList)
        } else {
            coinAdapter?.submitList(list.filter { it.isEnable == true || it.t_balance.toDouble() > 0.0 })
        }
    }


    private fun updateUI(tokens: MutableList<Tokens>) {
        CoroutineScope(Dispatchers.IO).launch {

            val pltToken = tokens.find { it.tokenId == defaultPLTTokenId }
            if (pltToken != null) {
                tokens.remove(pltToken)
                // lastList.add(0, pltToken)
            }

            val sortedList = tokens.sortedByDescending { it.t_balance.toBigDecimalOrNull() }
                .distinctBy { it.t_pk }

            val filteredList =
                sortedList.filter { it.isEnable == true || it.t_balance.toDouble() > 0.0 }

            dataList.clear()
            dataList.addAll(sortedList)
            CoroutineScope(Dispatchers.Main).launch {
                viewDataBinding?.shimmerLayout?.visibility = View.GONE
                viewDataBinding?.rvSendCoinList?.visibility = View.VISIBLE
                coinAdapter?.submitList(filteredList)
                viewDataBinding!!.rvSendCoinList.smoothScrollToPosition(0)
            }
        }
    }


}