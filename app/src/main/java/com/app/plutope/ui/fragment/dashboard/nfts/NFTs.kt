package com.app.plutope.ui.fragment.dashboard.nfts

import android.view.View
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
import com.app.plutope.model.NFTListModel
import com.app.plutope.model.Wallet
import com.app.plutope.networkConfig.Chain
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.ui.fragment.dashboard.DashboardDirections
import com.app.plutope.utils.coinTypeEnum.CoinType
import com.app.plutope.utils.constant.BASE_URL_PLUTO_PE
import com.app.plutope.utils.constant.nftPageType
import com.app.plutope.utils.customSnackbar.CustomSnackbar
import com.app.plutope.utils.network.NetworkState
import com.app.plutope.utils.safeNavigate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NFTs : BaseFragment<FragmentNFTsBinding, NFTsViewModel>() {
    private val nFTsViewModel: NFTsViewModel by viewModels()
    private var adapter: NftsListAdapter? = null
    var dataList: MutableList<NFTListModel> = mutableListOf()
    val arrayChain = arrayListOf(
        Chain.Ethereum.chainName,
        Chain.BinanceSmartChain.chainName,
        Chain.Polygon.chainName,
        "Avalanche",
        Chain.Arbitrum.chainName,
        Chain.BaseMainnet.chainName,
        Chain.OKC.chainName
    )

    var apiCount = 0
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
            // showNftDetailDialog(it)
            if (isAdded) {
                findNavController().safeNavigate(
                    DashboardDirections.actionDashboardToNftDetails(it)
                )
            }

        }
        viewDataBinding?.rvNftsList?.adapter = adapter

        arrayChain.forEach { it ->
            nFTsViewModel.executeGetNFTList(
                BASE_URL_PLUTO_PE + "get-all-nft" + "?&walletAddress=${
                    Wallet.getPublicWalletAddress(
                        CoinType.ETHEREUM
                    )
                }"
            )
        }


        viewDataBinding?.btnReceive?.setOnClickListener {
            openReceiveList()
        }
        viewDataBinding?.btnReceiveBottom?.setOnClickListener {
            openReceiveList()
        }
    }

    private fun showNftDetailDialog(nftModel: NFTListModel) {
        NFTDetailDialog.getInstance().show(
            requireContext(),
            nftModel.metadata?.name.toString(),
            nftModel.metadata?.description.toString(),
            nftModel.metadata?.image.toString()
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
                            // apiCount += 1
                            //hideLoader()
                            val list = it.data
                            if (!list.isNullOrEmpty()) {
                                dataList.clear()
                                dataList.addAll(list)
                                submitData(dataList)
                            }

                            stopShimmerEffect()
                            if (dataList.isEmpty()) {
                                viewDataBinding?.rvNftsList?.visibility = GONE
                                viewDataBinding?.btnReceiveBottom?.visibility = GONE
                                viewDataBinding?.layoutNoFound?.visibility = VISIBLE
                            }

                            /* if (apiCount >= arrayChain.size) {


                             }*/


                        }

                        is NetworkState.Loading -> {
                            // requireContext().showLoader()
                            startShimmerEffect()
                        }

                        is NetworkState.Error -> {
                            apiCount += 1
                            // hideLoader()
                            stopShimmerEffect()
                        }

                        is NetworkState.SessionOut -> {
                            // hideLoader()
                            stopShimmerEffect()
                            CustomSnackbar.make(
                                requireActivity().window.decorView.rootView as ViewGroup,
                                it.message.toString()
                            )
                                .show()
                        }

                        else -> {
                            // hideLoader()
                            stopShimmerEffect()
                        }
                    }
                }
            }
        }

    }

    private fun submitData(dataList: MutableList<NFTListModel>) {
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


    private fun startShimmerEffect() {
        viewDataBinding!!.shimmerLayout.startShimmer()
        viewDataBinding!!.shimmerLayout.visibility = VISIBLE
        viewDataBinding!!.rvNftsList.visibility = GONE

    }

    private fun stopShimmerEffect() {
        viewDataBinding!!.shimmerLayout.stopShimmer()
        viewDataBinding?.shimmerLayout?.visibility = View.INVISIBLE
        viewDataBinding!!.rvNftsList.visibility = VISIBLE
    }

}