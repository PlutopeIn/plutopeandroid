package com.app.plutope.ui.fragment

import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentUpdateAnythingBinding
import com.app.plutope.model.Wallets
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.ui.fragment.phrase.verify_phrase.VerifySecretPhraseViewModel
import com.app.plutope.utils.Securities
import com.app.plutope.utils.hideLoader
import com.app.plutope.utils.loge
import com.app.plutope.utils.network.NetworkState
import com.app.plutope.utils.safeNavigate
import com.app.plutope.utils.showLoader
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UpdateAnything : BaseFragment<FragmentUpdateAnythingBinding, VerifySecretPhraseViewModel>() {

    private var walletList: MutableList<Wallets?> = mutableListOf()
    private val updateViewModel: VerifySecretPhraseViewModel by viewModels()
    override fun getViewModel(): VerifySecretPhraseViewModel {
        return updateViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.updateViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_update_anything
    }

    override fun setupToolbarText(): String {
        return ""
    }

    override fun setupUI() {
        updateViewModel.getWalletsList()

        viewDataBinding!!.btnUpdate.setOnClickListener {
            if (walletList.isNotEmpty()) {
                walletList.forEach {
                    it?.w_mnemonic = Securities.encrypt(it?.w_mnemonic)
                }
                updateViewModel.updateWallets(walletList)
            } else {
                findNavController().safeNavigate(UpdateAnythingDirections.actionUpdateAnythingToDashboard())
            }

        }

    }

    override fun setupObserver() {

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                updateViewModel.walletsListResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            /*  if (it.data?.isNotEmpty() == true && it.data.size > 1) {

                              } else {

                              }*/

                            walletList = it.data as MutableList<Wallets?>
                            loge("UpdateWallet", "$walletList")

                            hideLoader()
                        }

                        is NetworkState.Loading -> {
                            requireContext().showLoader()
                        }

                        is NetworkState.Error -> {
                            loge("UpdateWallet", "$walletList")
                            hideLoader()
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
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                updateViewModel.updateWalletsResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            /*  if (it.data?.isNotEmpty() == true && it.data.size > 1) {

                              } else {

                              }*/

                            preferenceHelper.appUpdatedFlag = "1"
                            preferenceHelper.menomonicWallet = preferenceHelper.menomonicWallet
                            preferenceHelper.appPassword = preferenceHelper.appPassword


                            loge("UpdateWallet", "Updated")
                            if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                                findNavController().safeNavigate(UpdateAnythingDirections.actionUpdateAnythingToDashboard())
                            }


                            hideLoader()
                        }

                        is NetworkState.Loading -> {
                            requireContext().showLoader()
                        }

                        is NetworkState.Error -> {
                            hideLoader()
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

}