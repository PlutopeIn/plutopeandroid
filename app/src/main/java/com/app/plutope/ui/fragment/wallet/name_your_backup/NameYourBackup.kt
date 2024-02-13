package com.app.plutope.ui.fragment.wallet.name_your_backup

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentNameYourBackupBinding
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.utils.constant.backupnamecantempty
import com.app.plutope.utils.safeNavigate
import com.app.plutope.utils.showToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NameYourBackup : BaseFragment<FragmentNameYourBackupBinding, NameYourBackupViewModel>() {

    private val nameYourBackupViewModel: NameYourBackupViewModel by viewModels()
    val args:NameYourBackupArgs by navArgs()
    override fun getViewModel(): NameYourBackupViewModel {
        return nameYourBackupViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.nameYourBackupViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_name_your_backup
    }

    override fun setupToolbarText(): String {
        return ""
    }

    override fun setupUI() {
        if(args.walletModel.w_mnemonic!=""){
            viewDataBinding?.edtBackUpName?.setText(args.walletModel.w_wallet_name.toString())
        }
        viewDataBinding!!.imgBack.setOnClickListener {
            findNavController().navigateUp()
        }

        viewDataBinding!!.btnContinue.setOnClickListener {
            when {
                viewDataBinding?.edtBackUpName?.text?.toString()
                    ?.isEmpty() == true -> viewDataBinding?.edtBackUpName?.error =
                    backupnamecantempty

                else -> {
                    findNavController().safeNavigate(
                        NameYourBackupDirections.actionNameYourBackupToSetEncryptionPassword(
                            viewDataBinding?.edtBackUpName?.text?.toString()!!,
                            "",
                            args.walletModel
                        )
                    )
                }
            }

        }

        viewDataBinding!!.imgRemoveBackupName.setOnClickListener {
            viewDataBinding!!.edtBackUpName.setText("")
        }

    }

    override fun setupObserver() {

    }


}