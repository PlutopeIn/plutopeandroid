package com.app.plutope.ui.fragment.wallet.wallets

import android.annotation.SuppressLint
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentWalletsBinding
import com.app.plutope.model.Wallet
import com.app.plutope.model.Wallets
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.ui.fragment.phrase.recovery_phrase.VerifySecretPhraseViewModel
import com.app.plutope.ui.fragment.token.TokenViewModel
import com.app.plutope.utils.Securities
import com.app.plutope.utils.coinTypeEnum.CoinType
import com.app.plutope.utils.hideLoader
import com.app.plutope.utils.network.NetworkState
import com.app.plutope.utils.safeNavigate
import com.app.plutope.utils.showLoader
import com.app.plutope.utils.showToast
import com.app.plutope.utils.walletConnection.ACCOUNTS_1_EIP155_ADDRESS
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WalletsList : BaseFragment<FragmentWalletsBinding, WalletsViewModel>() {
    private var walletList: ArrayList<Wallets?>? = null
    private val walletsViewModel: WalletsViewModel by viewModels()
    private val tokenViewModel: TokenViewModel by viewModels()
    private var walletListAdapter: WalletListAdapter? = null
    private val verifyViewModel: VerifySecretPhraseViewModel by viewModels()
    override fun getViewModel(): WalletsViewModel {
        return walletsViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.walletsViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_wallets
    }

    override fun setupToolbarText(): String {
        return ""
    }

    @SuppressLint("LogNotTimber")
    override fun setupUI() {
        viewDataBinding!!.imgBack.setOnClickListener {
            findNavController().navigateUp()
        }

        walletListAdapter = WalletListAdapter(providerClick = { data ->
            preferenceHelper.menomonicWallet = Securities.decrypt(data.w_mnemonic!!)

            verifyViewModel.updatePrimaryWallet(data.w_id)

        }, menuClick = { data ->
            findNavController().navigate(
                WalletsListDirections.actionWalletsToRecoveryWalletFragment(
                    data
                )
            )
        })

        walletsViewModel.getWalletsList()
        viewDataBinding!!.imgAddWallet.setOnClickListener {
            findNavController().safeNavigate(WalletsListDirections.actionWalletsToWalletSetup())
        }
    }

    override fun setupObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                walletsViewModel.walletsListResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            hideLoader()
                            walletList = it.data?.toCollection(arrayListOf())
                            preferenceHelper.walletList = Gson().toJson(walletList)
                            if (walletList != null && walletList!!.isNotEmpty()) {
                                walletListAdapter?.submitList(walletList)

                                viewDataBinding?.rvWalletList?.adapter = walletListAdapter
                                walletListAdapter?.notifyDataSetChanged()
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


        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                verifyViewModel.walletsPrimaryWalletUpdateResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            if (it.data != null) {
                                Wallet.setWalletObjectFromInstance(it.data)
                                lifecycleScope.launch(Dispatchers.IO) {
                                    tokenViewModel.executeUpdateAllTokenBalanceZero()
                                }
                                Wallet.refreshWallet()
                                ACCOUNTS_1_EIP155_ADDRESS =
                                    Wallet.getPublicWalletAddress(CoinType.ETHEREUM)
                                findNavController().safeNavigate(WalletsListDirections.actionGlobalToDashboard())
                            }
                        }

                        is NetworkState.Loading -> {}
                        is NetworkState.Error -> {}
                        is NetworkState.SessionOut -> {}
                        else -> {}

                    }
                }
            }
        }
    }

}