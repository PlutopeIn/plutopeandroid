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
import com.app.plutope.ui.base.BaseActivity
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.ui.fragment.token.TokenViewModel
import com.app.plutope.ui.fragment.transactions.send.CoinListAdapter
import com.app.plutope.utils.constant.AddCustomTokenPageType
import com.app.plutope.utils.constant.nftPageType
import com.app.plutope.utils.safeNavigate
import dagger.hilt.android.AndroidEntryPoint

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
        return  if (args.pageType == nftPageType) "Receive NFT" else if(args.pageType == AddCustomTokenPageType) "Network" else getString(R.string.receive)
    }

    override fun setupUI() {
        (activity as BaseActivity).showToolbarTransparentBack()
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
            val sortedList = exactMatchList + filterList
            adapter?.submitList(sortedList)
        } else {

            adapter?.submitList(list.filter { it.isEnable == true || it.t_balance.toDouble() > 0.0 })
        }
    }

    private fun getAllTokenList() {

        val list = receiveViewModel.getAllTokensList()


        val sortList=list.sortedByDescending { it.t_balance.toBigDecimalOrNull() }.distinctBy { it.t_pk }

        if (sortList.isNotEmpty()) {
            dataList.clear()
            if (args.pageType == nftPageType || args.pageType == AddCustomTokenPageType) {
                if (args.pageType == AddCustomTokenPageType) {
                    dataList.addAll(sortList.filter { it.t_address == "" })
                } else {
                    dataList.addAll(sortList.filter {
                        it.t_address == "" && it.t_name?.lowercase() != getString(
                            R.string.okt_chain
                        ).lowercase()
                    })
                }
            } else {
                dataList.addAll(sortList)
            }
            viewDataBinding?.rvReceiveCoinList?.visibility = View.VISIBLE
            adapter?.submitList(dataList.filter { it.isEnable == true || it.t_balance.toDouble() > 0.0 })
            viewDataBinding?.rvReceiveCoinList?.adapter = adapter
            adapter?.notifyDataSetChanged()

        }
    }


}