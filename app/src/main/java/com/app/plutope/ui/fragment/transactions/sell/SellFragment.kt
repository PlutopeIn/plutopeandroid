package com.app.plutope.ui.fragment.transactions.sell

import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentSellBinding
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.utils.hideLoader
import com.app.plutope.utils.loge
import com.app.plutope.utils.network.NetworkState
import com.app.plutope.utils.safeNavigate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SellFragment : BaseFragment<FragmentSellBinding, SellViewModel>() {

    private val sellViewModel: SellViewModel by viewModels()

    private var adapter: SellProviderListAdapter? = null
    private var dataList: MutableList<SellProvider> = mutableListOf()

    override fun getViewModel(): SellViewModel {
        return sellViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.sellViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_sell
    }

    override fun setupToolbarText(): String {
        return ""
    }

    override fun setupUI() {

        sellViewModel.getSellProviderList()

        adapter = SellProviderListAdapter(providerClick = { model ->
            findNavController().safeNavigate(
                SellFragmentDirections.actionSellToBrowser(
                    model.url!!
                )
            )
        })

        viewDataBinding?.rvSendCoinList?.adapter = adapter

        viewDataBinding?.edtSearch?.doAfterTextChanged {
            if (dataList.isNotEmpty()) {
                filters(it.toString(), dataList.toMutableList())
            }
        }

        viewDataBinding?.imgBack?.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun setupObserver() {

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                sellViewModel.sellProviderResponse.collect {
                    when (it) {

                        is NetworkState.Error -> {
                            hideLoader()
                            showToast(it.message.toString())
                        }

                        is NetworkState.Loading -> {
                            showLoader()
                        }

                        is NetworkState.SessionOut -> {}
                        is NetworkState.Success -> {
                            hideLoader()
                            if (it.data?.providerList!!.isNotEmpty()) {
                                dataList.clear()
                                dataList.addAll(it.data.providerList)
                                adapter?.submitList(dataList)
                            }
                        }

                        else -> {}
                    }
                }
            }
        }


    }

    private fun filters(text: String, list: MutableList<SellProvider>) {
        loge("callList", "list : ${list.size}")
        val filterList = ArrayList<SellProvider>()
        val exactMatchList = ArrayList<SellProvider>()

        val searchText = text.lowercase()
        if (searchText != "") {
            for (i in list) {
                val symbol = i.name?.lowercase()
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
            adapter?.submitList(dataList)
            viewDataBinding!!.rvSendCoinList.smoothScrollToPosition(0)
        }
    }


}