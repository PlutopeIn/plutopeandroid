package com.app.plutope.ui.fragment.dashboard.nfts

import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentNFTsBinding
import com.app.plutope.dialogs.NFTDetailDialog
import com.app.plutope.model.NFTModel
import com.app.plutope.model.Wallet
import com.app.plutope.networkConfig.Chain
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.ui.fragment.dashboard.DashboardDirections
import com.app.plutope.utils.coinTypeEnum.CoinType
import com.app.plutope.utils.constant.NFT_BASE_URL
import com.app.plutope.utils.constant.nftPageType
import com.app.plutope.utils.customSnackbar.CustomSnackbar
import com.app.plutope.utils.hideLoader
import com.app.plutope.utils.network.NetworkState
import com.app.plutope.utils.safeNavigate
import com.app.plutope.utils.showLoader
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NFTs : BaseFragment<FragmentNFTsBinding, NFTsViewModel>() {
    private val nFTsViewModel: NFTsViewModel by viewModels()
    private var adapter: NftsListAdapter? = null
    var dataList: MutableList<NFTModel> = mutableListOf()
    override fun getViewModel(): NFTsViewModel {
        return nFTsViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.nFTsViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_n_f_ts
    }

    override fun setupToolbarText(): String {
        return ""
    }

    override fun setupUI() {
        adapter = NftsListAdapter {
            showNftDetailDialog(it)
        }
        viewDataBinding?.rvNftsList?.adapter = adapter

        val arrayChain = arrayListOf(
            Chain.Ethereum.chainName,
            Chain.BinanceSmartChain.chainName,
            Chain.Polygon.chainName
        )
        arrayChain.forEach { it ->
            nFTsViewModel.executeGetNFTList(NFT_BASE_URL + "${Wallet.getPublicWalletAddress(CoinType.ETHEREUM)}/nft?chain=${it}")
        }


        viewDataBinding?.btnReceive?.setOnClickListener {
            openReceiveList()
        }
        viewDataBinding?.btnReceiveBottom?.setOnClickListener {
            openReceiveList()
        }
    }

    private fun showNftDetailDialog(nftModel: NFTModel) {
        NFTDetailDialog.getInstance().show(
            requireContext(),
            nftModel.parsedMetadata?.name.toString(),
            nftModel.parsedMetadata?.description.toString(),
            nftModel.parsedMetadata?.image.toString()
        )
    }

    private fun openReceiveList() {
        findNavController().safeNavigate(DashboardDirections.actionDashboardToReceive(pageType = nftPageType))
    }

    override fun setupObserver() {

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                nFTsViewModel.getNFTListResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            hideLoader()
                            val list = it.data?.result
                            if (!list.isNullOrEmpty()) {
                                dataList.clear()
                                dataList.addAll(list)
                                submitData(dataList)
                            }
                            if (dataList.isEmpty()) {
                                viewDataBinding?.rvNftsList?.visibility = GONE
                                viewDataBinding?.btnReceiveBottom?.visibility = GONE
                                viewDataBinding?.layoutNoFound?.visibility = VISIBLE
                            }

                        }

                        is NetworkState.Loading -> {
                            requireContext().showLoader()
                        }

                        is NetworkState.Error -> {
                            hideLoader()
                        }

                        is NetworkState.SessionOut -> {
                            hideLoader()
                            CustomSnackbar.make(requireActivity().window.decorView.rootView as ViewGroup, it.message.toString())
                                .show()
                        }

                        else -> {
                            hideLoader()
                        }
                    }
                }
            }
        }

    }

    private fun submitData(dataList: MutableList<NFTModel>) {
        if (dataList.isNotEmpty()) {
            adapter?.submitList(dataList)
            viewDataBinding?.rvNftsList?.visibility = VISIBLE
            viewDataBinding?.btnReceiveBottom?.visibility = VISIBLE
            viewDataBinding?.layoutNoFound?.visibility = GONE
            adapter?.notifyDataSetChanged()
        } else {
            viewDataBinding?.rvNftsList?.visibility = GONE
            viewDataBinding?.btnReceiveBottom?.visibility = GONE
            viewDataBinding?.layoutNoFound?.visibility = VISIBLE
        }
    }


}