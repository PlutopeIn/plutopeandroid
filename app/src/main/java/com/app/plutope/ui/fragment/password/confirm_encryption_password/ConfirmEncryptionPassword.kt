package com.app.plutope.ui.fragment.password.confirm_encryption_password

import android.app.Activity
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentConfirmEncryptionPasswordBinding
import com.app.plutope.model.Tokens
import com.app.plutope.model.Wallet
import com.app.plutope.ui.base.BaseActivity
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.ui.fragment.phrase.recovery_phrase.VerifySecretPhraseViewModel
import com.app.plutope.ui.fragment.token.TokenViewModel
import com.app.plutope.ui.fragment.wallet.backup.PhraseBackupFragment
import com.app.plutope.utils.Securities
import com.app.plutope.utils.attachPasswordToggle
import com.app.plutope.utils.constant.FOLDER_NAME
import com.app.plutope.utils.constant.PARENT_FOLDER_ID
import com.app.plutope.utils.extras.DriveServiceHelper
import com.app.plutope.utils.getMnemonics
import com.app.plutope.utils.getWordListFromWordCharArray
import com.app.plutope.utils.hideLoader
import com.app.plutope.utils.isPasswordValid
import com.app.plutope.utils.network.NetworkState
import com.app.plutope.utils.safeNavigate
import com.app.plutope.utils.showLoader
import com.app.plutope.utils.showToast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.DriveScopes
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ConfirmEncryptionPassword :
    BaseFragment<FragmentConfirmEncryptionPasswordBinding, ConfirmEncryptionPasswordViewModel>() {

    private var driveFileId: String? = ""
    private var folderId: String = ""
    private var mnemonic: String = ""
    private var isValid: Boolean? = false

    val args: ConfirmEncryptionPasswordArgs by navArgs()
    private lateinit var mDriveServiceHelper: DriveServiceHelper
    private val confirmEncryptionPasswordViewModel: ConfirmEncryptionPasswordViewModel by viewModels()
    private val tokenViewModel: TokenViewModel by viewModels()
    private val verifySecretPhraseViewModel: VerifySecretPhraseViewModel by viewModels()

    companion object {
        const val REQUEST_CODE_SIGN_IN = 1
    }

    override fun getViewModel(): ConfirmEncryptionPasswordViewModel {
        return confirmEncryptionPasswordViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.confirmEncryptionPasswordViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_confirm_encryption_password
    }

    override fun setupToolbarText(): String {
        return ""
    }

    override fun setupUI() {
        setUpListener()

        val mnemonicsWords = getMnemonics()
        val wordList = getWordListFromWordCharArray(mnemonicsWords)

        mnemonic = if (args.walletModel.w_wallet_name != "") {
            val wordList1 = args.walletModel.w_mnemonic?.split(" ")?.toMutableList()

            wordList1?.toTypedArray()!!.joinToString(separator = " ")
        } else {
            wordList.joinToString(separator = " ")
        }

    }

    private fun setUpListener() {
        viewDataBinding!!.imgBack.setOnClickListener {
            findNavController().navigateUp()
        }

        viewDataBinding!!.checkboxLoosFund.setOnCheckedChangeListener { _, _ ->
            enableButton()
        }

        viewDataBinding!!.checkboxExploreShare.setOnCheckedChangeListener { _, _ ->
            enableButton()
        }

        viewDataBinding!!.edtPassword.attachPasswordToggle(viewDataBinding!!.imgHideShow)


        viewDataBinding!!.edtPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                isValid = isPasswordValid(p0.toString())
                enableButton()

            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })


        viewDataBinding!!.btnConfirm.setOnClickListener {
            when {
                args.password != viewDataBinding!!.edtPassword.text.toString() -> {
                    requireContext().showToast(getString(R.string.confirm_password_did_not_match_with_password))
                }

                else -> {
                    requestSignIn()
                }
            }
        }
    }

    override fun setupObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                verifySecretPhraseViewModel.insertWalletResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            preferenceHelper.menomonicWallet = Securities.decrypt(mnemonic)
                            val data = it.data
                            (requireActivity() as BaseActivity).setWalletObject()
                            if (data != null) {
                                Wallet
                                    .setWalletObjectFromInstance(data)
                                Wallet.refreshWallet()
                            }
                            lifecycleScope.launch(Dispatchers.IO) {
                                tokenViewModel.executeUpdateAllTokenBalanceZero()
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
                            requireContext().showToast("Wallet Created successfully.")

                            findNavController().safeNavigate(ConfirmEncryptionPasswordDirections.actionConfirmEncryptionPasswordToDashboard())

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

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                verifySecretPhraseViewModel.updateWalletBackupResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            hideLoader()
                            if (args.walletModel.w_mnemonic != "") {
                                val walletModel = args.walletModel
                                walletModel.w_is_cloud_backup = true
                                if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                                    findNavController().safeNavigate(
                                        ConfirmEncryptionPasswordDirections.actionConfirmEncryptionPasswordToRecoveryWalletFragment(
                                            walletModel
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

    private fun enableButton() {
        if (isValid!! && viewDataBinding!!.checkboxLoosFund.isChecked && viewDataBinding!!.checkboxExploreShare.isChecked) {
            // viewDataBinding?.txtValidationMessage?.setTextColor(resources.getColor(R.color.purple_7576D,null))
            viewDataBinding!!.btnConfirm.apply {
                isEnabled = true
                background =
                    ResourcesCompat.getDrawable(resources, R.drawable.button_gradient_26, null)
                setTextColor(ResourcesCompat.getColor(resources, R.color.bg_white, null))
            }
        } else {
            // viewDataBinding?.txtValidationMessage?.setTextColor(resources.getColor(R.color.red,null))
            viewDataBinding!!.btnConfirm.apply {
                isEnabled = false
                background =
                    ResourcesCompat.getDrawable(resources, R.drawable.button_disable, null)
                setTextColor(ResourcesCompat.getColor(resources, R.color.green_02303B, null))

            }

        }
    }


    /**
     * Starts a sign-in activity using [.REQUEST_CODE_SIGN_IN].
     */
    private fun requestSignIn() {

        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .build()
        val client = GoogleSignIn.getClient(requireContext(), signInOptions)

        // The result of the sign-in Intent is handled in onActivityResult.
        startActivityForResult(client.signInIntent, PhraseBackupFragment.REQUEST_CODE_SIGN_IN)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_SIGN_IN -> if (resultCode === Activity.RESULT_OK && data != null) {
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
                val googleDriveService: com.google.api.services.drive.Drive =
                    com.google.api.services.drive.Drive.Builder(
                        AndroidHttp.newCompatibleTransport(),
                        JacksonFactory.getDefaultInstance(),
                        credential
                    )
                        .setApplicationName("Drive API Migration")
                        .build()


                mDriveServiceHelper = DriveServiceHelper(googleDriveService)

                uploadFile()
            }
            .addOnFailureListener {}
    }

    private fun uploadFile() {

        requireContext().showLoader()
        mDriveServiceHelper.createFolder(PARENT_FOLDER_ID, FOLDER_NAME)
            .continueWithTask { parentFolderTask ->

                val parentFolderId: String = parentFolderTask.result
                folderId = parentFolderId
                preferenceHelper.appFolderId = folderId

                val passcode = args.password

                // Encrypt the passphrase

                //  val encryptedPassphrase = EncryptionUtils.encryptPassphrase(mnemonic, passcode)

                //  val encryptedJson = EncryptionUtils.convertToJSON(encryptedPassphrase.toString())

                mDriveServiceHelper.createFile(
                    parentFolderId,
                    "${args.walletBackupName.trim()}.json",
                    "application/json", ""
                    /*"$encryptedJson"*/
                )
            }
            .addOnSuccessListener { fileId ->
                // File creation and content update successful
                driveFileId = fileId
                requireActivity().runOnUiThread {
                    requireContext().showToast("File Uploaded Successfully.")
                    createWalletAndInsertTable()
                }

            }
            .addOnFailureListener { exception ->
                // Folder or file creation or content update failed
                requireActivity().runOnUiThread {
                    hideLoader()
                    requireContext().showToast("Error creating folder or file:")
                }
            }
    }

    private fun createWalletAndInsertTable() {
        preferenceHelper.isWalletCreatedData = true
        if (args.walletModel.w_wallet_name != "") {
            verifySecretPhraseViewModel.updateWalletBackup(
                isCloudBackup = true,
                isManualBackup = args.walletModel.w_is_manual_backup,
                args.walletModel.w_id, args.walletBackupName, folderId, driveFileId ?: ""
            )
        } else {
            verifySecretPhraseViewModel.executeInsertWallet(
                mnemonic,
                isFromDrive = true,
                walletName = args.walletBackupName,
                folderId = folderId,
                fileId = driveFileId ?: ""
            )
        }
    }
}