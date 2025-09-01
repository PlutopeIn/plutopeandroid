package com.app.plutope.ui.fragment.currency

import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentCurrencyBinding
import com.app.plutope.model.CurrencyModel
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.utils.extras.FirebaseAnalyticsHelper
import com.app.plutope.utils.extras.FirebaseAnalyticsHelper.logEvent
import com.app.plutope.utils.hideLoader
import com.app.plutope.utils.loge
import com.app.plutope.utils.network.NetworkState
import com.app.plutope.utils.safeNavigate
import com.app.plutope.utils.showLoader
import com.app.plutope.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class Currency : BaseFragment<FragmentCurrencyBinding, CurrencyViewModel>() {

    private val currencyViewModel: CurrencyViewModel by viewModels()
    private var currencyListAdapter: CurrencyListAdapter? = null
    val currencyList = arrayListOf<CurrencyModel>()
    val args: CurrencyArgs by navArgs()

    var supportedCurrencyList = mutableListOf(
        "btc",
        "eth",
        "ltc",
        "bch",
        "bnb",
        "eos",
        "xrp",
        "xlm",
        "link",
        "dot",
        "yfi",
        "usd",
        "aed",
        "ars",
        "aud",
        "bdt",
        /* "bhd",*/
        "bmd",
        "brl",
        "cad",
        "chf",
        "clp",
        "cny",
        "czk",
        "dkk",
        "eur",
        "gbp",
        "gel",
        "hkd",
        "huf",
        "idr",
        "ils",
        "inr",
        "jpy",
        "krw",
        "kwd",
        "lkr",
        "mmk",
        "mxn",
        "myr",
        "ngn",
        "nok",
        "nzd",
        "php",
        "pkr",
        "pln",
        "rub",
        "sar",
        "sek",
        "sgd",
        "thb",
        "try",
        "twd",
        "uah",
        "vef",
        "vnd",
        "zar",
        "xdr",
        "xag",
        "xau",
        "bits",
        "sats"
    )

    companion object {
        const val keyCurrency = "Currency"
        const val keyBundleCurrency = "BundleCurrency"
    }

    override fun getViewModel(): CurrencyViewModel {
        return currencyViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.currencyViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_currency
    }

    override fun setupToolbarText(): String {
        return getString(R.string.currency)
    }

    override fun setupUI() {
        this.logEvent()
        viewDataBinding!!.imgBack.setOnClickListener {
            findNavController().navigateUp()
        }

        currencyListAdapter = CurrencyListAdapter {
            val model = it
            if (!args.isFromSetting) {
                val bundle = Bundle()
                bundle.putParcelable(keyCurrency, it)
                setFragmentResult(keyBundleCurrency, bundle)
                findNavController().navigateUp()
            } else {
                preferenceHelper.setSelectedCurrency(model)
                findNavController().safeNavigate(CurrencyDirections.actionCurrencyToDashboard())
            }


        }

        currencyListAdapter?.submitList(currencyList)
        viewDataBinding!!.rvCurrencyList.adapter = currencyListAdapter

        currencyViewModel.getCurrencyFromTable()

        viewDataBinding?.edtSearch?.doAfterTextChanged {
            filters(it.toString(), currencyList.toMutableList())
        }


    }

    override fun setupObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                currencyViewModel.currencyListResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            hideLoader()
                            if (it.data?.isNotEmpty() == true) {
                                viewDataBinding?.rvCurrencyList?.visibility = View.VISIBLE
                                loge("ˇˇ", "==> $it")
                                currencyList.clear()
                                val tempList = mutableListOf<CurrencyModel>()
                                it.data.forEach {
                                    supportedCurrencyList.forEach { supportedCode ->
                                        if (supportedCode.lowercase() == it?.code?.lowercase()) {
                                            tempList.add(it)
                                        }
                                    }

                                }

                                // currencyList.addAll(it.data as MutableList<CurrencyModel>)
                                currencyList.addAll(tempList)
                                currencyList.filter { it.code.lowercase() == preferenceHelper.getSelectedCurrency()?.code?.lowercase() }
                                    .forEach { it.isSelected = true }
                                currencyListAdapter?.submitList(currencyList)
                                currencyListAdapter?.notifyDataSetChanged()
                            }

                        }

                        is NetworkState.Loading -> {
                            requireContext().showLoader()
                        }

                        is NetworkState.Error -> {
                            hideLoader()
                            requireContext().showToast(it.message.toString())
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

    private fun filters(text: String, list: MutableList<CurrencyModel>) {
        val filterList = ArrayList<CurrencyModel>()
        for (i in list)
            if (i.code.lowercase().contains(text.lowercase()) || i.name.lowercase()
                    .contains(text.lowercase())
            ) {
                filterList.add(i)
            }
        currencyListAdapter?.submitList(filterList)

    }

}