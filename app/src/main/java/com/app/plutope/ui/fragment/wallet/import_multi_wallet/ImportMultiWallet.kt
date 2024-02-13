package com.app.plutope.ui.fragment.wallet.import_multi_wallet

import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentImportMultiWalletBinding
import com.app.plutope.model.Tokens
import com.app.plutope.model.Wallet
import com.app.plutope.model.Wallets
import com.app.plutope.ui.base.BaseActivity
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.ui.fragment.phrase.verify_phrase.VerifySecretPhraseViewModel
import com.app.plutope.ui.fragment.token.TokenViewModel
import com.app.plutope.utils.coinTypeEnum.CoinType
import com.app.plutope.utils.constant.WHAT_IS_SECRET_PHRASE_URL
import com.app.plutope.utils.constant.appUpdateVersion
import com.app.plutope.utils.extras.setSafeOnClickListener
import com.app.plutope.utils.hideLoader
import com.app.plutope.utils.network.NetworkState
import com.app.plutope.utils.safeNavigate
import com.app.plutope.utils.showLoader
import com.app.plutope.utils.showToast
import com.app.plutope.utils.validateMnemonic
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ImportMultiWallet :
    BaseFragment<FragmentImportMultiWalletBinding, ImportMultiWalletViewModel>() {

    private var words: String = ""
    private val importMultiWalletViewModel: ImportMultiWalletViewModel by viewModels()
    private val verifySecretPhraseViewModel: VerifySecretPhraseViewModel by viewModels()
    private val tokenViewModel: TokenViewModel by viewModels()

    private var walletList = mutableListOf<Wallets>()
    override fun getViewModel(): ImportMultiWalletViewModel {
        return importMultiWalletViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.importMultiWalletViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_import_multi_wallet
    }

    override fun setupToolbarText(): String {
        return ""
    }

    override fun setupUI() {
        viewDataBinding?.apply {
            if (!preferenceHelper.isTokenImageCalled)
                tokenViewModel.getTokenImageList()


            imgBack.setOnClickListener {
                findNavController().navigateUp()
            }

            txtPaste.setOnClickListener {
                val clipboard =
                    requireActivity().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val item = clipboard.primaryClip?.getItemAt(0)
                val pasteData = item?.text

                edtPhrases.setText(pasteData)
                edtPhrases.setSelection(viewDataBinding!!.edtPhrases.length())
            }

            btnImport.setOnClickListener {
                val walletListType = object : TypeToken<MutableList<Wallets>>() {}.type
                walletList =
                    if (preferenceHelper.walletList != "") {
                        Gson().fromJson(preferenceHelper.walletList, walletListType)
                    } else {
                        mutableListOf()
                    }

                when {
                    viewDataBinding?.edtWalletName?.text?.isEmpty() == true -> viewDataBinding?.edtWalletName?.error =
                        "Wallet name can't be empty"

                    viewDataBinding?.edtPhrases?.text?.isEmpty() == true -> viewDataBinding?.edtPhrases?.error =
                        "Phrases can't be empty"

                    else -> {

                        val mainWords = viewDataBinding?.edtPhrases?.text.toString()
                        val spLiteWord = mainWords.split("\\s+".toRegex())
                        words = spLiteWord.joinToString(" ")
                        if (validateMnemonic(words).first) {

                            val availableWalletList = walletList.let {
                                it.filter { list -> list.w_mnemonic == words }
                            }

                            if (availableWalletList.isEmpty()) {


                                verifySecretPhraseViewModel.executeInsertWallet(
                                    words,
                                    viewDataBinding?.edtWalletName?.text?.toString()!!,
                                    isManualBackup = true
                                )
                            } else {
                                requireContext().showToast("This wallet has already been imported.")
                            }
                        } else {
                            requireContext().showToast(validateMnemonic(words).second)
                        }
                    }
                }
            }

            txtSecretPhraseInstruction.setSafeOnClickListener {
                findNavController().safeNavigate(ImportMultiWalletDirections.actionImportMultiWalletToWebViewToolbar(
                    WHAT_IS_SECRET_PHRASE_URL,"What is Secret Phrase?"))
            }
        }

    }

    override fun setupObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                verifySecretPhraseViewModel.insertWalletResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            preferenceHelper.menomonicWallet = words
                            preferenceHelper.isWalletCreatedData = true
                            val data = it.data
                            (requireActivity() as BaseActivity).setWalletObject()
                            if (data != null) {
                                Wallet.setWalletObjectFromInstance(data)
                                Wallet.refreshWallet()
                            }
                            lifecycleScope.launch(Dispatchers.IO) {
                                tokenViewModel.executeUpdateAllTokenBalanceZero()
                            }
                            tokenViewModel.getAllTokenList(tokenViewModel)


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
                            requireContext().showToast("Wallet imported successfully.")

                            val walletAddress = Wallet.getPublicWalletAddress(CoinType.ETHEREUM)
                            tokenViewModel.registerWalletCall(
                                walletAddress!!,
                                preferenceHelper.firebaseToken!!
                            )

                            preferenceHelper.appUpdatedFlag = appUpdateVersion
                            if (!preferenceHelper.isFirstTime)
                                findNavController().safeNavigate(ImportMultiWalletDirections.actionImportMultiWalletToWelcome())
                            else
                                findNavController().safeNavigate(ImportMultiWalletDirections.actionImportMultiWalletToDashboard())
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

        CoroutineScope(Dispatchers.IO).launch() {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                tokenViewModel.getRegisterWallet.collect {
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
    }

}