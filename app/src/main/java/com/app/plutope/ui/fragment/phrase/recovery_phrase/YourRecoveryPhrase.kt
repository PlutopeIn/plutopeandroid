package com.app.plutope.ui.fragment.phrase.recovery_phrase

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.WindowManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentYourRecoveryPhraseBinding
import com.app.plutope.dialogs.ConfirmationAlertDialog
import com.app.plutope.model.Tokens
import com.app.plutope.model.Wallet
import com.app.plutope.ui.base.BaseActivity
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.ui.fragment.token.TokenViewModel
import com.app.plutope.ui.fragment.wallet.backup.PhraseBackupFragment
import com.app.plutope.utils.GoogleSignInListner
import com.app.plutope.utils.Securities
import com.app.plutope.utils.coinTypeEnum.CoinType
import com.app.plutope.utils.constant.appUpdateVersion
import com.app.plutope.utils.copyToClipboard
import com.app.plutope.utils.enableDisableButton
import com.app.plutope.utils.extras.DriveServiceHelper
import com.app.plutope.utils.extras.buttonClickedWithEffect
import com.app.plutope.utils.extras.setSafeOnClickListener
import com.app.plutope.utils.hideLoader
import com.app.plutope.utils.loge
import com.app.plutope.utils.network.NetworkState
import com.app.plutope.utils.requestGoogleSignIn
import com.app.plutope.utils.safeNavigate
import com.app.plutope.utils.showLoaderAnyHow
import com.app.plutope.utils.showToast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Integer.min


@AndroidEntryPoint
class YourRecoveryPhrase :
    BaseFragment<FragmentYourRecoveryPhraseBinding, VerifySecretPhraseViewModel>() {
    private var walletReferCode: String? = null
    private var wordsList: Array<String> = arrayOf()
    private var words: String? = ""
    var isShowPhrases: Boolean = false
    private val verifySecretPhraseViewModel: VerifySecretPhraseViewModel by viewModels()
    private val tokenViewModel: TokenViewModel by viewModels()
    private val args: YourRecoveryPhraseArgs by navArgs()
    val spanCount = 2
    private lateinit var mDriveServiceHelper: DriveServiceHelper
    override fun getViewModel(): VerifySecretPhraseViewModel {
        return verifySecretPhraseViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.backUpWalletCheckViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_your_recovery_phrase
    }

    override fun setupToolbarText(): String {
        return ""
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun setupUI() {
        /*requireActivity().window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )*/

        viewDataBinding?.imgBack?.setOnClickListener {
            findNavController().popBackStack()
        }

        words = args.wordsString
        wordsList = args.wordArrayList

        if (args.walletModel.w_wallet_name != "") {
            if (args.isFromDriveBackup) {
                viewDataBinding?.txtHideShowPhrase?.visibility = GONE
                viewDataBinding?.btnDelete?.visibility = VISIBLE
                viewDataBinding?.btnContinue?.visibility = GONE
                viewDataBinding?.txtCopy?.visibility = GONE
            }
            if (!args.walletModel.w_is_manual_backup) {
                if (args.walletModel.w_is_cloud_backup && args.isFromDriveBackup) {
                    viewDataBinding?.btnContinue?.visibility = GONE
                    viewDataBinding?.txtCopy?.visibility = GONE
                } else {
                    viewDataBinding?.btnContinue?.visibility = VISIBLE
                    viewDataBinding?.txtCopy?.visibility = VISIBLE
                }
            } else {
                viewDataBinding?.btnContinue?.visibility = GONE
                viewDataBinding?.txtCopy?.visibility = GONE
            }
        } else {
            viewDataBinding?.btnContinue?.visibility = VISIBLE
        }


        val adapter = PhraseGenerateListAdapter { _: String, _: Int -> }
        adapter.submitList(wordsList.toMutableList())

        val layoutManager =
            GridLayoutManager(requireContext(), spanCount, GridLayoutManager.VERTICAL, false)
        viewDataBinding?.rvPhraseGeneratedList?.layoutManager = layoutManager

        viewDataBinding?.rvPhraseGeneratedList?.adapter = adapter
        if (!preferenceHelper.isTokenImageCalled) {
            tokenViewModel.getTokenImageList()
        }

        if (preferenceHelper.referralCode.isNotEmpty()) {
            viewDataBinding?.lytReferral?.visibility = VISIBLE
            viewDataBinding?.txtReferralCode?.text = preferenceHelper.referralCode
        } else {
            viewDataBinding?.lytReferral?.visibility = GONE
        }

        setListener()
    }


    inner class ColumnGridLayoutManager(context: Context, spanCount: Int) :
        GridLayoutManager(context, spanCount) {

        override fun onLayoutChildren(
            recycler: RecyclerView.Recycler?,
            state: RecyclerView.State?
        ) {
            super.onLayoutChildren(recycler, state)
            setSpanOrder()
        }

        private fun setSpanOrder() {
            val spanCount = spanCount
            val totalItemCount = itemCount
            val column = totalItemCount / spanCount
            val columnRemainder = totalItemCount % spanCount

            val spanOrder = IntArray(totalItemCount) { 0 }
            for (i in 0 until spanCount) {
                var start = i * column + min(i, columnRemainder)
                val end = start + column + if (i < columnRemainder) 1 else 0

                while (start < end) {
                    spanOrder[start] = i
                    start++
                }
            }

            //setSpanOrderIndexes(spanOrder)


        }
    }

    private fun setListener() {
        viewDataBinding?.btnContinue?.setSafeOnClickListener {
            viewDataBinding?.btnContinue?.enableDisableButton(false)
            // findNavController().safeNavigate(YourRecoveryPhraseDirections.actionYourRecoveryPhraseToWelcomeScreen())

            createWalletAndInsertTable()
        }

        viewDataBinding?.txtCopy?.setOnClickListener {
            requireContext().copyToClipboard("$words")
            requireContext().showToast("Copied $words")
        }

        viewDataBinding?.btnDelete?.buttonClickedWithEffect {
            if (!args.walletModel.w_is_manual_backup) {
                openConfirmationDialog(
                    getString(R.string.manual_backup_required),
                    getString(R.string.before_deleting_your_icloud_backup_ensure_a_manual_backup_is_in_place_for_your_data_s_security),
                    true
                )
            } else {
                openConfirmationDialog(
                    getString(R.string.delete_backup),
                    getString(R.string.are_you_sure_you_want_to_delete_cloud_backup), false
                )
            }
        }
    }


    private fun createWalletAndInsertTable() {
        preferenceHelper.isWalletCreatedData = true
        requireContext().showLoaderAnyHow()
        viewDataBinding?.btnContinue?.enableDisableButton(true)
        if (args.walletModel.w_wallet_name != "") {
            verifySecretPhraseViewModel.updateWalletBackup(
                args.walletModel.w_is_cloud_backup,
                true,
                args.walletModel.w_id,
                args.walletModel.w_wallet_name.toString(),
                args.walletModel.folderId,
                args.walletModel.fileId
            )
        } else {
            verifySecretPhraseViewModel.executeInsertWallet(words!!, isManualBackup = true)
        }
    }

    private fun openConfirmationDialog(title: String, subtitle: String, isFromRecovery: Boolean) {
        ConfirmationAlertDialog.getInstance().show(
            requireContext(),
            title,
            subtitle,
            isFromRecovery,
            listener = object : ConfirmationAlertDialog.DialogOnClickBtnListner {
                override fun onDeleteClick() {
                    if (!isFromRecovery) {
                        requestGoogleSignIn(requireContext(), object : GoogleSignInListner {
                            override fun requestUserGoogleSingIn(
                                recoveryIntent: Intent?,
                                requestCode: Int
                            ) {
                                recoveryIntent?.let {
                                    startActivityForResult(
                                        it,
                                        requestCode
                                    )
                                }
                            }

                        })
                    }
                }
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PhraseBackupFragment.REQUEST_CODE_SIGN_IN -> if (resultCode === Activity.RESULT_OK && data != null) {
                handleSignInResult(data)
            }

            DriveServiceHelper.REQUEST_AUTHORIZATION -> if (resultCode === Activity.RESULT_OK && data != null) {
                handleSignInResult(data)
            }

        }
    }

    private fun handleSignInResult(result: Intent) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
            .addOnSuccessListener { googleAccount: GoogleSignInAccount ->

                // Use the authenticated account to sign in to the Drive service.
                val credential = GoogleAccountCredential.usingOAuth2(
                    requireContext(), setOf(DriveScopes.DRIVE_FILE)
                )
                credential.selectedAccount = googleAccount.account
                val googleDriveService: Drive =
                    Drive.Builder(
                        AndroidHttp.newCompatibleTransport(),
                        JacksonFactory.getDefaultInstance(),
                        credential
                    )
                        .setApplicationName("Drive API Migration")
                        .build()


                mDriveServiceHelper = DriveServiceHelper(googleDriveService)
                mDriveServiceHelper.deleteFileById(
                    requireContext(),
                    googleAccount,
                    args.walletModel.fileId
                ) { recoveryIntent, requestCode ->
                    startActivityForResult(
                        recoveryIntent,
                        requestCode
                    )
                }
                    .addOnSuccessListener { aVoid ->

                        // requireContext().showToast("Success Delete")
                        verifySecretPhraseViewModel.updateWalletBackup(
                            false,
                            args.walletModel.w_is_manual_backup,
                            args.walletModel.w_id,
                            args.walletModel.w_wallet_name.toString(),
                            args.walletModel.folderId,
                            args.walletModel.fileId
                        )

                    }
                    .addOnFailureListener { e ->
                        // Failed to delete the file
                        e.printStackTrace()
                    }
            }
    }


    override fun onDestroyView() {
        // Clear the secure flag when the fragment is destroyed
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        super.onDestroyView()
    }

    override fun setupObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                verifySecretPhraseViewModel.updateWalletBackupResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                                val wallet = args.walletModel
                                wallet.w_is_cloud_backup = false
                                findNavController().navigate(
                                    YourRecoveryPhraseDirections.actionYourRecoveryPhraseToRecoveryWalletFragment(
                                        wallet
                                    )
                                )
                            }

                        }

                        is NetworkState.Loading -> {

                        }

                        is NetworkState.Error -> {

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
                verifySecretPhraseViewModel.insertWalletResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            preferenceHelper.menomonicWallet = Securities.decrypt(words)
                            val data = it.data
                            (requireActivity() as BaseActivity).setWalletObject()
                            if (data != null) {
                                Wallet.setWalletObjectFromInstance(data)
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
                                preferenceHelper.firebaseToken, type = "create",
                                preferenceHelper.referralCode
                            )

                            /*  tokenViewModel.registerWalletCallMaster(
                                  preferenceHelper.deviceId,
                                  walletAddress,
                                  preferenceHelper.referralCode,
                              )*/


                            preferenceHelper.appUpdatedFlag = appUpdateVersion
                            if (!preferenceHelper.isFirstTime)
                                tokenViewModel.executeGetGenerateToken()
                            // findNavController().safeNavigate(YourRecoveryPhraseDirections.actionYourRecoveryPhraseToWelcomeScreen())
                            else
                            // findNavController().safeNavigate(VerifySecretPhraseDirections.actionVerifySecretPhraseToDashboard())
                                findNavController().safeNavigate(YourRecoveryPhraseDirections.actionYourRecoveryPhraseToDashboard())
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
                            walletReferCode = ""
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
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                verifySecretPhraseViewModel.updateWalletBackupResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            hideLoader()
                            if (args.walletModel.w_mnemonic != "") {
                                val walletModel = args.walletModel
                                walletModel.w_is_manual_backup = true
                                if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                                    /*  findNavController().safeNavigate(
                                          VerifySecretPhraseDirections.actionVerifySecretPhraseToRecoveryWalletFragment(args.walletModel)
                                      )*/

                                    findNavController().safeNavigate(
                                        YourRecoveryPhraseDirections.actionYourRecoveryPhraseToRecoveryWalletFragment(
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
                                }
                            }

                            findNavController().safeNavigate(YourRecoveryPhraseDirections.actionYourRecoveryPhraseToWelcomeScreen())
                        }

                        is NetworkState.Loading -> {}
                        is NetworkState.Error -> {
                            findNavController().safeNavigate(YourRecoveryPhraseDirections.actionYourRecoveryPhraseToWelcomeScreen())
                        }

                        is NetworkState.SessionOut -> {}
                        else -> {

                        }
                    }
                }
            }
        }


    }
}



