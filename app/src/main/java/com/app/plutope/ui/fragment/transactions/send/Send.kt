package com.app.plutope.ui.fragment.transactions.send

import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentSendBinding
import com.app.plutope.model.Tokens
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.ui.fragment.token.TokenViewModel
import com.app.plutope.utils.constant.defaultPLTTokenId
import com.app.plutope.utils.safeNavigate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class Send : BaseFragment<FragmentSendBinding, TokenViewModel>() {

    private val sendViewModel: TokenViewModel by viewModels()
    private var adapter: CoinListAdapter? = null
    private var dataList: MutableList<Tokens> = mutableListOf()
    val args: SendArgs by navArgs()


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
        return ""
    }

    override fun setupUI() {

        adapter = CoinListAdapter(providerClick = { model ->
            findNavController().safeNavigate(
                SendDirections.actionSendToSendCoin(
                    model, args.result
                )
            )
        })

        viewDataBinding?.rvSendCoinList?.adapter = adapter

        viewDataBinding?.edtSearch?.doAfterTextChanged {
            filters(it.toString(), dataList)
        }

        viewDataBinding?.imgBack?.setOnClickListener {
            findNavController().popBackStack()
        }

        //  loge("SendList", "setupUI:${sendViewModel.tokenList.value}")
        if (sendViewModel.tokenList.value == null) {
            viewDataBinding?.shimmerLayout?.visibility = View.VISIBLE
            sendViewModel.fetchAllTokensList() // Fetch initially if null
        } /*else {
            updateUI(sendViewModel.tokenList.value!!)
        }*/

    }

    override fun setupObserver() {
        // getAllTokenList()

        sendViewModel.tokenList.observe(viewLifecycleOwner) { tokens ->
            updateUI(tokens)
        }

        /* viewLifecycleOwner.lifecycleScope.launch {
             repeatOnLifecycle(Lifecycle.State.RESUMED) {
                 sendViewModel.tokenList2.collect { tokens ->
                     updateUI(tokens)

                 }
             }
         }*/


    }

    private fun filters(text: String, list: MutableList<Tokens>) {
        val filterList = ArrayList<Tokens>()
        val exactMatchList = ArrayList<Tokens>()

        val searchText = text.lowercase()
        if (searchText != "") {
            for (i in list) {
                val symbol = i.t_symbol.lowercase()
                if (symbol.contains(searchText) == true) {
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

            val lastList =
                list.filter { it.isEnable == true || it.t_balance.toDouble() > 0.0 }.toMutableList()
            val pltToken = lastList.find { it.tokenId == defaultPLTTokenId }
            if (pltToken != null) {
                lastList.remove(pltToken)
                lastList.add(0, pltToken)
            }

            adapter?.submitList(lastList/*list.filter { it.isEnable == true || it.t_balance.toDouble() > 0.0 }*/)
            viewDataBinding!!.rvSendCoinList.smoothScrollToPosition(0)
        }
    }

    private fun updateUI(tokens: List<Tokens>) {
        CoroutineScope(Dispatchers.IO).launch {
            val sortedList = tokens.sortedByDescending { it.t_balance.toBigDecimalOrNull() }
                .distinctBy { it.t_pk }

            val filteredList =
                sortedList.filter { it.isEnable == true || it.t_balance.toDouble() > 0.0 }
                    .toMutableList()

            dataList.clear()
            dataList.addAll(sortedList)

            CoroutineScope(Dispatchers.Main).launch {
                viewDataBinding?.shimmerLayout?.visibility = View.GONE
                viewDataBinding?.rvSendCoinList?.visibility = View.VISIBLE

                val pltToken = filteredList.find { it.tokenId == defaultPLTTokenId }
                if (pltToken != null) {
                    filteredList.remove(pltToken)
                    filteredList.add(0, pltToken)
                }

                adapter?.submitList(filteredList)
                viewDataBinding!!.rvSendCoinList.smoothScrollToPosition(0)

            }
        }
    }


}