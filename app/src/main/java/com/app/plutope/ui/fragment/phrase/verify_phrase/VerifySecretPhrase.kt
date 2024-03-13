package com.app.plutope.ui.fragment.phrase.verify_phrase

import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentVarifySecretPhraseBinding
import com.app.plutope.model.Tokens
import com.app.plutope.model.Wallet
import com.app.plutope.ui.base.BaseActivity
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.ui.fragment.phrase.recovery_phrase.PhraseGenerateListAdapter
import com.app.plutope.ui.fragment.token.TokenViewModel
import com.app.plutope.utils.Securities
import com.app.plutope.utils.coinTypeEnum.CoinType
import com.app.plutope.utils.constant.appUpdateVersion
import com.app.plutope.utils.extras.buttonClickedWithEffect
import com.app.plutope.utils.hideLoader
import com.app.plutope.utils.network.NetworkState
import com.app.plutope.utils.safeNavigate
import com.app.plutope.utils.showLoader
import com.app.plutope.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.random.Random

@AndroidEntryPoint
class VerifySecretPhrase :
    BaseFragment<FragmentVarifySecretPhraseBinding, VerifySecretPhraseViewModel>() {

    private var tagRandomWordList = mutableListOf<String>()
    private var words: String = ""
    private val args: VerifySecretPhraseArgs by navArgs()

    private val tagUserSelectWordList = mutableListOf<String>()

    private var phrasesGenraterAdapter: PhraseGenerateListAdapter? = null
    private var phrasesRandomPhrasesAdapter: RandomSecretPhraseListAdapter? = null


    private val verifySecretPhraseViewModel: VerifySecretPhraseViewModel by viewModels()
    private val tokenViewModel: TokenViewModel by viewModels()
    override fun getViewModel(): VerifySecretPhraseViewModel {
        return verifySecretPhraseViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.verifySecretPhraseViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_varify_secret_phrase
    }

    override fun setupToolbarText(): String {
        return getString(R.string.verify_secret_phrase)
    }

    override fun setupUI() {
        words = args.wordsString
        val wordsList = args.wordArrayList.toMutableList()

        tagRandomWordList = wordsList.toMutableList()
        tagRandomWordList.shuffle(Random)
        setEmptyPhrasesList()
        pickedListPhrasesList(tagRandomWordList)

        viewDataBinding?.btnDone?.buttonClickedWithEffect {
            createWalletAndInsertTable()
        }

        if (!preferenceHelper.isTokenImageCalled)
            tokenViewModel.getTokenImageList()
    }

    private fun createWalletAndInsertTable() {
        preferenceHelper.isWalletCreatedData = true
        requireContext().showLoader()
        if (args.walletModel.w_wallet_name != "") {
            verifySecretPhraseViewModel.updateWalletBackup(
                args.walletModel.w_is_cloud_backup,
                true,
                args.walletModel.w_id,args.walletModel.w_wallet_name.toString(),args.walletModel.folderId,args.walletModel.fileId
            )
        } else {
            verifySecretPhraseViewModel.executeInsertWallet(words, isManualBackup = true)
        }
    }

    private fun setEmptyPhrasesList() {

        phrasesGenraterAdapter = PhraseGenerateListAdapter { model, position ->
            tagRandomWordList.add(model)
            tagUserSelectWordList.removeAt(position)
            phrasesGenraterAdapter?.notifyDataSetChanged()
            phrasesRandomPhrasesAdapter?.notifyDataSetChanged()

            showErrorMessage()
        }
        phrasesGenraterAdapter?.submitList(tagUserSelectWordList)
        viewDataBinding!!.rvPhraseGeneratedList.adapter = phrasesGenraterAdapter

    }

    private fun pickedListPhrasesList(tagRandomWordList: MutableList<String>) {
        phrasesRandomPhrasesAdapter = RandomSecretPhraseListAdapter { model, position ->
            if (tagRandomWordList.isNotEmpty()) {
                //if (!tagUserSelectWordList.contains(model)) {
                    tagUserSelectWordList.add(model)
                    tagRandomWordList.removeAt(position)

                    phrasesRandomPhrasesAdapter?.notifyItemRemoved(position)
                    phrasesGenraterAdapter?.notifyDataSetChanged()

                    showErrorMessage()
                //}

            }
            /* phrasesRandomPhrasesAdapter?.notifyItemRemoved(position)
             phrasesGenraterAdapter?.notifyDataSetChanged()

             showErrorMessage()*/

        }

        phrasesRandomPhrasesAdapter?.submitList(tagRandomWordList)
        viewDataBinding?.rvPhrasePickList?.adapter = phrasesRandomPhrasesAdapter
    }

    private fun showErrorMessage() {

        if (tagUserSelectWordList.size == args.wordArrayList.toMutableList().size) {
            val equal = args.wordArrayList.toMutableList() == tagUserSelectWordList
            if (equal) {
                isEnableDisable(true)

            } else {
                isEnableDisable(false)
            }
        } else {

            viewDataBinding?.txtSuccessMsg?.visibility = GONE
            viewDataBinding?.btnDone?.alpha = 0.4F
            viewDataBinding?.btnDone?.isEnabled = false
            viewDataBinding?.btnDone?.isClickable = false

            val originalList = args.wordArrayList.toMutableList()
            val sublist2 = originalList.subList(
                0,
                tagUserSelectWordList.size
            )
            val orderingMatches = sublist2 == tagUserSelectWordList

            if (orderingMatches) {
                viewDataBinding?.txtInvalidOrder?.visibility = View.GONE

            } else {
                isEnableDisable(false)
            }


        }

    }

    override fun setupObserver() {

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                verifySecretPhraseViewModel.insertWalletResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            preferenceHelper.menomonicWallet = Securities.decrypt(words)
                            val data = it.data
                            (requireActivity() as BaseActivity).setWalletObject()
                            if (data != null) {
                                Wallet
                                    .setWalletObjectFromInstance(data)
                                Wallet.refreshWallet()
                            }
                            tokenViewModel.getAllTokenList(tokenViewModel)


                        }

                        is NetworkState.Loading -> {}

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
                tokenViewModel.insertTokenResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            tokenViewModel.insertInWalletTokens(tokenViewModel)
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
                tokenViewModel.insertWalletTokenResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            hideLoader()
                            requireContext().showToast("Wallet Created successfully")

                            val walletAddress = Wallet.getPublicWalletAddress(CoinType.ETHEREUM)
                            tokenViewModel.registerWalletCall(
                                walletAddress!!,
                                preferenceHelper.firebaseToken!!
                            )

                            tokenViewModel.registerWalletCallMaster(
                                preferenceHelper.deviceId,
                                walletAddress,
                                preferenceHelper.referralCode
                            )


                            preferenceHelper.appUpdatedFlag = appUpdateVersion
                            if (!preferenceHelper.isFirstTime)
                                findNavController().safeNavigate(VerifySecretPhraseDirections.actionVerifySecretPhraseToWelcomeScreen())
                            else
                                findNavController().safeNavigate(VerifySecretPhraseDirections.actionVerifySecretPhraseToDashboard())
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
                tokenViewModel.coinGeckoTokensResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            val listTokens = it.data as MutableList<Tokens>
                            if (listTokens.isNotEmpty()) {
                                tokenViewModel.executeInsertTokens(listTokens)
                            }

                        }

                        is NetworkState.Loading -> {

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

        GlobalScope.launch(Dispatchers.IO) {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                tokenViewModel.tokenImageListResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            hideLoader()
                            preferenceHelper.isTokenImageCalled = true

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

        GlobalScope.launch(Dispatchers.IO) {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                tokenViewModel.getRegisterWallet.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            hideLoader()

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

        CoroutineScope(Dispatchers.IO).launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                tokenViewModel.getRegisterWalletMaster.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            hideLoader()
                        }

                        is NetworkState.Loading -> {
                            //requireContext().showLoader()
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
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                verifySecretPhraseViewModel.updateWalletBackupResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            hideLoader()
                            if (args.walletModel.w_mnemonic != "") {
                                val walletModel = args.walletModel
                                walletModel.w_is_manual_backup = true
                                if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                                    findNavController().safeNavigate(
                                        VerifySecretPhraseDirections.actionVerifySecretPhraseToRecoveryWalletFragment(
                                            args.walletModel
                                        )
                                    )
                                }
                            }


                        }

                        is NetworkState.Loading -> {}

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

    private fun isEnableDisable(isEnable: Boolean) {
        if (isEnable) {
            viewDataBinding?.txtInvalidOrder?.visibility = View.GONE
            viewDataBinding?.btnDone?.alpha = 1.0F
            viewDataBinding?.btnDone?.isClickable = isEnable
            viewDataBinding?.btnDone?.isEnabled = isEnable
            viewDataBinding?.txtSuccessMsg?.visibility = VISIBLE
        } else {
            viewDataBinding?.txtInvalidOrder?.visibility = VISIBLE
            viewDataBinding?.btnDone?.alpha = 0.4F
            viewDataBinding?.btnDone?.isEnabled = isEnable
            viewDataBinding?.btnDone?.isClickable = isEnable
            viewDataBinding?.txtSuccessMsg?.visibility = GONE
        }
    }


}