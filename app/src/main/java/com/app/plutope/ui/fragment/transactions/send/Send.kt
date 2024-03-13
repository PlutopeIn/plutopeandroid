package com.app.plutope.ui.fragment.transactions.send

import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentSendBinding
import com.app.plutope.model.Tokens
import com.app.plutope.ui.base.BaseActivity
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.ui.fragment.token.TokenViewModel
import com.app.plutope.utils.safeNavigate
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Send : BaseFragment<FragmentSendBinding, TokenViewModel>() {

    private val sendViewModel: TokenViewModel by viewModels()
    private var adapter: CoinListAdapter? = null
    private var dataList: MutableList<Tokens> = mutableListOf()


    override fun getViewModel(): TokenViewModel {
        return sendViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.sendViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_send
    }

    override fun setupToolbarText(): String {
        return getString(R.string.send)
    }

    override fun setupUI() {
        (activity as BaseActivity).showToolbarTransparentBack()
        adapter = CoinListAdapter(providerClick = { model ->
            findNavController().safeNavigate(SendDirections.actionSendToSendCoin(model))
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
       val exactMatchList = ArrayList<Tokens>()

       val searchText = text.lowercase()
       if (searchText != "") {
           for (i in list) {
               val symbol = i.t_symbol?.lowercase()
               if (symbol?.contains(searchText) == true) {
                   if (symbol == searchText) {
                       exactMatchList.add(i)
                   } else {
                       filterList.add(i)
                   }
               }
           }

           // Add the exact matches first, then add the filtered results
           val sortedList = exactMatchList + filterList
           adapter?.submitList(sortedList)
           viewDataBinding!!.rvSendCoinList.smoothScrollToPosition(0)
       } else {

           adapter?.submitList(list.filter { it.isEnable == true || it.t_balance.toDouble() > 0.0 })
           viewDataBinding!!.rvSendCoinList.smoothScrollToPosition(0)
       }
   }

    private fun getAllTokenList() {
        val list = sendViewModel.getAllTokensList()
        val sortList=list.sortedByDescending { it.t_balance.toBigDecimalOrNull() }.distinctBy { it.t_pk }

        if (sortList.isNotEmpty()) {
            dataList.clear()
            dataList.addAll(sortList)

            viewDataBinding?.rvSendCoinList?.visibility = View.VISIBLE
            adapter?.submitList(dataList.filter { it.isEnable == true || it.t_balance.toDouble() > 0.0 })

            viewDataBinding?.rvSendCoinList?.adapter = adapter
            viewDataBinding!!.rvSendCoinList.smoothScrollToPosition(0)
            adapter?.notifyDataSetChanged()

        }
    }


}