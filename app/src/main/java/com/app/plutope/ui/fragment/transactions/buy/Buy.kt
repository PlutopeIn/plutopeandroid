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
import com.app.plutope.ui.base.BaseActivity
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.ui.fragment.token.TokenViewModel
import com.app.plutope.ui.fragment.transactions.send.CoinListAdapter
import com.app.plutope.utils.constant.pageTypeSwap
import com.app.plutope.utils.safeNavigate
import dagger.hilt.android.AndroidEntryPoint

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
        return if (args.pageType == pageTypeSwap) getString(R.string.swap) else getString(R.string.buy)
    }

    override fun setupUI() {
        (activity as BaseActivity).showToolbarTransparentBack()
        coinAdapter = CoinListAdapter(providerClick = { model ->
            if (args.pageType == pageTypeSwap) {
                findNavController().safeNavigate(BuyDirections.actionBuyToSwap(model))
            } else {
                findNavController().safeNavigate(BuyDirections.actionBuyToBuyDetails(model))
            }
        })

        getAllTokenList()
        viewDataBinding!!.edtSearch.doAfterTextChanged {
            filters(it.toString(), dataList)
        }

    }


    override fun setupObserver() {

    }

    private fun filters(text: String, list: MutableList<Tokens>) {
        val filterList = ArrayList<Tokens>()
        if (text != "") {
            for (i in list)
                if (i.t_name?.lowercase()!!.contains(text.lowercase()) || i.t_symbol?.lowercase()!!
                        .contains(text.lowercase())
                ) {
                    filterList.add(i)
                }
            coinAdapter?.submitList(filterList)
        } else {
            coinAdapter?.submitList(list.filter { it.isEnable == true || it.t_balance.toDouble() > 0.0 })
        }
    }

    private fun getAllTokenList() {
        val list = buyViewModel.getAllTokensList()
        val sortList=list.sortedByDescending { it.t_balance.toBigDecimalOrNull() }.distinctBy { it.t_pk }

        if (sortList.isNotEmpty()) {
            dataList.clear()
            dataList.addAll(sortList)

            viewDataBinding?.rvSendCoinList?.visibility = View.VISIBLE

            // dataList.filter { it.isEnable == true }

            coinAdapter?.submitList(dataList.filter { it.isEnable == true || it.t_balance.toDouble() > 0.0 })

            viewDataBinding?.rvSendCoinList?.adapter = coinAdapter
            coinAdapter?.notifyDataSetChanged()

        }
    }


}