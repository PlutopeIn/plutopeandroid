package com.app.plutope.ui.fragment.providers

import android.os.Bundle
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentProvidersBinding
import com.app.plutope.ui.base.BaseFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Providers : BaseFragment<FragmentProvidersBinding, ProvidersViewModel>() {

    private val providersViewModel: ProvidersViewModel by viewModels()
    private var providerListAdapter: ProviderListAdapter? = null

    private val args: ProvidersArgs by navArgs()
    companion object {
        const val keyProvider = "Provider"
        const val keyBundleProvider = "BundleProvider"
    }

    override fun getViewModel(): ProvidersViewModel {
        return providersViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.providersViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_providers
    }

    override fun setupToolbarText(): String {
        return getString(R.string.providers)
    }

    override fun setupUI() {

        val providerListType = object : TypeToken<List<ProviderModel>>() {}.type
        val providerList: ArrayList<ProviderModel> =
            Gson().fromJson(args.providers, providerListType)

        providerListAdapter = ProviderListAdapter {

            val bundle = Bundle()
            bundle.putParcelable(keyProvider, it)
            setFragmentResult(keyBundleProvider, bundle)
            findNavController().navigateUp()


        }
        providerListAdapter?.submitList(providerList)
        viewDataBinding!!.rvProviderList.adapter = providerListAdapter

    }

    override fun setupObserver() {

    }


}