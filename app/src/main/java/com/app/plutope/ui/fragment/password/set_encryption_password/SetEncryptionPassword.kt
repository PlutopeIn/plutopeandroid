package com.app.plutope.ui.fragment.password.set_encryption_password

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
import com.app.plutope.databinding.FragmentSetEncryptionPasswordBinding
import com.app.plutope.dialogs.DialogNeverShareSecretPhrase
import com.app.plutope.model.PointModel
import com.app.plutope.model.Tokens
import com.app.plutope.model.Wallet
import com.app.plutope.ui.base.BaseActivity
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.ui.fragment.phrase.recovery_phrase.VerifySecretPhraseViewModel
import com.app.plutope.ui.fragment.token.TokenViewModel
import com.app.plutope.utils.Securities
import com.app.plutope.utils.attachPasswordToggle
import com.app.plutope.utils.hideLoader
import com.app.plutope.utils.isPasswordValid
import com.app.plutope.utils.network.NetworkState
import com.app.plutope.utils.safeNavigate
import com.app.plutope.utils.showLoader
import com.app.plutope.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SetEncryptionPassword :
    BaseFragment<FragmentSetEncryptionPasswordBinding, SetEncryptionPasswordViewModel>() {
    private var words: String? = ""
    var pointList = arrayListOf<PointModel>()
    private val setEncryptionPasswordViewModel: SetEncryptionPasswordViewModel by viewModels()
    val args: SetEncryptionPasswordArgs by navArgs()

    private val verifySecretPhraseViewModel: VerifySecretPhraseViewModel by viewModels()
    private val tokenViewModel: TokenViewModel by viewModels()
    override fun getViewModel(): SetEncryptionPasswordViewModel {
        return setEncryptionPasswordViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.setEncryptionPasswordViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_set_encryption_password
    }

    override fun setupToolbarText(): String {
        return ""
    }

    override fun setupUI() {
        if (args.backupDriveContent != "") {
            viewDataBinding?.txtBackupTitle?.text =
                resources.getString(R.string.enter_encryption_password)
            viewDataBinding?.btnSetEncryptionPassword?.text = getString(R.string.restore_wallet)


            viewDataBinding?.txtBackupDes?.text =
                getString(R.string.enter_the_encryption_password_you_created_for_your_google_drive_secret_phrase_backup)

        } else {
            viewDataBinding?.txtBackupTitle?.text =
                resources.getString(R.string.set_encryption_password)

            viewDataBinding?.txtBackupDes?.text =
                getString(R.string.this_password_is_used_to_protect_your_wallet_and_provide_access_to_the_browser_extension_it_cannot_be_reset_and_is_separate_from_your_mobile_wallet)

        }

        pointList.add(
            PointModel(
                0,
                "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod."
            )
        )
        pointList.add(
            PointModel(
                1,
                "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod."
            )
        )
        pointList.add(
            PointModel(
                2,
                "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna."
            )
        )
        viewDataBinding!!.imgBack.setOnClickListener {
            findNavController().navigateUp()
        }

        viewDataBinding!!.edtPassword.attachPasswordToggle(viewDataBinding!!.imgHideShow)


        viewDataBinding!!.edtPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val isValid = isPasswordValid(p0.toString())
                enableButton(isValid)

            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })

        viewDataBinding!!.btnSetEncryptionPassword.setOnClickListener {
            if (args.backupDriveContent != "") {
                val str =
                    ""/*EncryptionUtils.decryptPassphrase(jsonObj.getString("encryptedData"))*/
                val password = str.split("|").last()
                val menomonics = str.split("|").first()
                if (password.toString() == viewDataBinding!!.edtPassword.text.toString()) {
                    words = menomonics
                    requireContext().showLoader()
                    preferenceHelper.isWalletCreatedData = true
                    verifySecretPhraseViewModel.executeInsertWallet(
                        words!!,
                        isFromDrive = true,
                        walletName = args.backupWalletName
                    )
                } else {
                    words = menomonics
                    requireContext().showToast("Please enter correct password")
                }

            } else {

                DialogNeverShareSecretPhrase.getInstance().show(
                    requireContext(),
                    pointList,
                    object : DialogNeverShareSecretPhrase.DialogOnClickBtnListner {
                        override fun onSubmitClicked(selectedList: String) {
                            findNavController().safeNavigate(
                                SetEncryptionPasswordDirections.actionSetEncryptionPasswordToConfirmEncryptionPassword(
                                    viewDataBinding!!.edtPassword.text.toString(),
                                    args.backupWalletName, args.walletModel
                                )
                            )

                        }
                    })
            }
        }


        if (!preferenceHelper.isTokenImageCalled)
            tokenViewModel.getTokenImageList()
    }

    override fun setupObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                verifySecretPhraseViewModel.insertWalletResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            preferenceHelper.menomonicWallet = Securities.decrypt(words!!)
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

                            if (!preferenceHelper.isFirstTime) findNavController().safeNavigate(
                                SetEncryptionPasswordDirections.actionSetEncryptionPasswordToWelcomeScreen()
                            )
                            else
                                findNavController().safeNavigate(SetEncryptionPasswordDirections.actionSetEncryptionPasswordToDashboard())
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
    }

    private fun enableButton(isValid: Boolean) {
        if (isValid) {
            viewDataBinding?.txtValidationMessage?.setTextColor(
                resources.getColor(
                    R.color.purple_7576D,
                    null
                )
            )
            viewDataBinding!!.btnSetEncryptionPassword.apply {
                isEnabled = true
                background =
                    ResourcesCompat.getDrawable(resources, R.drawable.button_gradient_26, null)
                setTextColor(ResourcesCompat.getColor(resources, R.color.bg_white, null))
            }
        } else {
            viewDataBinding?.txtValidationMessage?.setTextColor(
                resources.getColor(
                    R.color.red,
                    null
                )
            )
            viewDataBinding!!.btnSetEncryptionPassword.apply {
                isEnabled = false
                background =
                    ResourcesCompat.getDrawable(resources, R.drawable.button_disable, null)
                setTextColor(ResourcesCompat.getColor(resources, R.color.green_02303B, null))

            }

        }

    }


}