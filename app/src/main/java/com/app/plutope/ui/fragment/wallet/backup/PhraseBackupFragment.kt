package com.app.plutope.ui.fragment.wallet.backup

import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentPhraseBackupBinding
import com.app.plutope.model.Wallets
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.utils.safeNavigate
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class PhraseBackupFragment : BaseFragment<FragmentPhraseBackupBinding, PhraseBackupViewModel>() {

    private val phraseBackupViewModel by viewModels<PhraseBackupViewModel>()

    companion object {
        const val REQUEST_CODE_SIGN_IN = 1
    }

    override fun getViewModel(): PhraseBackupViewModel {
        return phraseBackupViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.phraseBackupViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_phrase_backup
    }

    override fun setupToolbarText(): String {
        return ""
    }

    override fun setupUI() {
        viewDataBinding!!.imgBack.setOnClickListener {
            findNavController().navigateUp()
        }

        viewDataBinding!!.btnBackUpManually.setOnClickListener {
            if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                findNavController().safeNavigate(
                    PhraseBackupFragmentDirections.actionPhraseBackupFragmentToBackUpWalletCheck(
                        Wallets()
                    )
                )
            }
        }

        viewDataBinding!!.btnBackUpToGoogleDrive.setOnClickListener {
            if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {

                findNavController().safeNavigate(
                    PhraseBackupFragmentDirections.actionPhraseBackupFragmentToNameYourBackup(
                        Wallets()
                    )
                )
            }
        }

    }

    override fun setupObserver() {

    }


}