package com.app.plutope.ui.fragment

import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentUpdateAnythingBinding
import com.app.plutope.model.TokenListImageModel
import com.app.plutope.model.Tokens
import com.app.plutope.model.Wallet
import com.app.plutope.model.WalletTokens
import com.app.plutope.model.Wallets
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.ui.fragment.phrase.recovery_phrase.VerifySecretPhraseViewModel
import com.app.plutope.ui.fragment.token.TokenViewModel
import com.app.plutope.utils.Securities
import com.app.plutope.utils.coinTypeEnum.CoinType
import com.app.plutope.utils.constant.appUpdateVersion
import com.app.plutope.utils.constant.defaultPLTTokenId
import com.app.plutope.utils.constant.needAppUpdateVersion
import com.app.plutope.utils.hideLoader
import com.app.plutope.utils.loge
import com.app.plutope.utils.network.NetworkState
import com.app.plutope.utils.safeNavigate
import com.app.plutope.utils.showLoader
import com.app.plutope.utils.showLoaderAnyHow
import com.app.plutope.utils.showSuccessToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UpdateAnything : BaseFragment<FragmentUpdateAnythingBinding, VerifySecretPhraseViewModel>() {

    private var walletList: MutableList<Wallets> = mutableListOf()
    private val updateViewModel: VerifySecretPhraseViewModel by viewModels()
    private val tokenViewModel: TokenViewModel by viewModels()
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

        CoroutineScope(Dispatchers.Main).launch {
            val walletId = Wallet.walletObject.w_id
            loge("walletId=>", "$walletId")
            tokenViewModel.getWalletTokenOfSpecificWalletId(walletId)
        }


        viewDataBinding!!.btnUpdate.setOnClickListener {

            if (preferenceHelper.appUpdatedFlag == "") {
                if (walletList.isNotEmpty()) {
                    walletList.forEach {
                        it.w_mnemonic = Securities.encrypt(it.w_mnemonic)
                    }
                    updateViewModel.updateWallets(walletList)
                } else {
                    findNavController().safeNavigate(UpdateAnythingDirections.actionUpdateAnythingToDashboard())
                }
            } else if (preferenceHelper.appUpdatedFlag == needAppUpdateVersion) {

                val default = TokenListImageModel(
                    id = 0,
                    coin_id = defaultPLTTokenId,
                    image = "https://plutope.app/api/images/applogo.png",
                    symbol = "plt",
                    name = "Plutope Token"

                )
                CoroutineScope(Dispatchers.IO).launch {
                    tokenViewModel.updateTokenImages(default)
                    tokenViewModel.deleteToken("binary-holdings")
                }

                tokenViewModel.getCoinGeckoTokensList()
            } else {
                tokenViewModel.executeGetGenerateToken()
            }
        }

    }

    override fun setupObserver() {

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                tokenViewModel.getGenerateTokenResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            loge(
                                "getGenerateTokenResponse",
                                "${it.data?.data?.isUpdate} :: ${it.data?.data?.tokenString}"
                            )
                            if (it.data?.data?.isUpdate == true) {
                                if (it.data.data.tokenString?.lowercase() != preferenceHelper.updateTokenText.lowercase()) {
                                    preferenceHelper.updateTokenText = it.data.data.tokenString!!
                                    val default = TokenListImageModel(
                                        id = 0,
                                        coin_id = defaultPLTTokenId,
                                        image = "https://plutope.app/api/images/applogo.png",
                                        symbol = "plt",
                                        name = "Plutope Token"

                                    )
                                    CoroutineScope(Dispatchers.IO).launch {
                                        tokenViewModel.updateTokenImages(default)
                                        tokenViewModel.deleteToken("binary-holdings")
                                    }

                                    tokenViewModel.getCoinGeckoTokensList()
                                }
                            }
                        }

                        is NetworkState.Loading -> {}
                        is NetworkState.Error -> {}
                        is NetworkState.SessionOut -> {}
                        else -> {

                        }
                    }
                }
            }
        }


        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                updateViewModel.walletsListResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            walletList = it.data as MutableList<Wallets>
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

                            preferenceHelper.appUpdatedFlag = needAppUpdateVersion
                            preferenceHelper.menomonicWallet = preferenceHelper.menomonicWallet
                            preferenceHelper.appPassword = preferenceHelper.appPassword


                            loge("UpdateWallet", "Updated")
                            if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                                if (preferenceHelper.appUpdatedFlag == needAppUpdateVersion) {
                                    tokenViewModel.getCoinGeckoTokensList()
                                } else {
                                    findNavController().safeNavigate(UpdateAnythingDirections.actionUpdateAnythingToDashboard())
                                }
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


        /** Start call update version 2 */

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                tokenViewModel.coinGeckoTokensResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {

                            val listTokens = it.data as MutableList<Tokens>
                            if (listTokens.isNotEmpty()) {
                                // tokenViewModel.executeInsertTokens(listTokens)

                                loge("EnableTokens", "${listTokens.filter { it.isEnable }}")

                                tokenViewModel.executeUpdateAndInsertTokens(listTokens)
                            }
                        }

                        is NetworkState.Loading -> {
                            requireContext().showLoaderAnyHow()
                        }

                        is NetworkState.Error -> {

                        }

                        is NetworkState.SessionOut -> {}

                        else -> {

                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                tokenViewModel.updateAndInsertTokensResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {

                            loge("Update", "Update and Inserted successfully")
                            tokenViewModel.getAllActiveTokenList(
                                Wallet.getPublicWalletAddress(
                                    CoinType.ETHEREUM
                                )!!
                            )

                        }

                        is NetworkState.Loading -> {

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


        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                tokenViewModel.getAllActiveTokenListResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            if (it.data!!.isNotEmpty()) {

                                // loge("MatchToken","${tokenViewModel.getAllTokensList().contains()}")
                                val tempList = mutableListOf<Tokens>()
                                tokenViewModel.getAllDisableTokens().forEach { allToken ->
                                    it.data.forEach {
                                        if (/*allToken.t_symbol?.lowercase() == it.symbol?.lowercase() &&*/ it.tokenAddress?.lowercase() == allToken.t_address.lowercase()) {
                                            if (it.tokenAddress != "0x0000000000000000000000000000000000001010") {
                                                tempList.add(allToken)
                                            }
                                            // tempList.add(allToken)
                                        }
                                    }
                                }


                                val listWalletTokens = mutableListOf<WalletTokens>()
                                val enableAllToken = tokenViewModel.getEnableTokens(1)

                                CoroutineScope(Dispatchers.IO).launch {
                                    var oldPolygon = Tokens()
                                    var newPolygon = Tokens()
                                    tokenViewModel.getChainTokenList().forEach {
                                        if (it.tokenId == "polygon-ecosystem-token" && it.t_address == "0x0000000000000000000000000000000000001010") {
                                            newPolygon = it
                                        } else if (it.tokenId == "matic-network" && it.t_address == "") {
                                            oldPolygon = it
                                        }
                                    }


                                    val defaultTokenList =
                                        enableAllToken + tempList.distinct().toMutableList()

                                    defaultTokenList.forEach {
                                        listWalletTokens.add(
                                            WalletTokens(
                                                walletId = Wallet.walletObject.w_id,
                                                it.t_pk,
                                                isEnable = true
                                            )
                                        )

                                        // if(it.t_name == "Base")

                                    }

                                    tokenViewModel.replaceWalletToken(oldPolygon, newPolygon)

                                    //  loge("listWalletTokens", "${listWalletTokens}")

                                    /* tokenViewModel.executeInsertWalletTokens(
                                         listWalletTokens.distinct().toMutableList()
                                     )*/

                                    tokenViewModel.executeUpdateAndInsertWalletTokens(
                                        listWalletTokens
                                    )

                                }


                            } else {
                                tokenViewModel.getAllTokenList(tokenViewModel)
                            }

                        }

                        is NetworkState.Loading -> {

                        }

                        is NetworkState.Error -> {
                            hideLoader()
                        }

                        is NetworkState.SessionOut -> {}

                        else -> {

                        }
                    }
                }
            }
        }


        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                tokenViewModel.updateAndInsertWalletTokensResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            hideLoader()
                            showSuccessToast("Updated successfully")
                            preferenceHelper.appUpdatedFlag = appUpdateVersion
                            findNavController().safeNavigate(UpdateAnythingDirections.actionUpdateAnythingToDashboard())
                        }

                        is NetworkState.Loading -> {

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