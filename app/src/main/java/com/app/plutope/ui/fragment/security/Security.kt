package com.app.plutope.ui.fragment.security

import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentSecurityBinding
import com.app.plutope.dialogs.DialogOptionSecurity
import com.app.plutope.model.CommonOptionModel
import com.app.plutope.model.SecurityOption
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.utils.safeNavigate
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Security : BaseFragment<FragmentSecurityBinding, SecurityViewModel>() {
    private val securityViewModel: SecurityViewModel by viewModels()
    override fun getViewModel(): SecurityViewModel {
        return securityViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.securityViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_security
    }

    override fun setupToolbarText(): String {
        return ""
    }

    override fun onResume() {
        super.onResume()
        if (!preferenceHelper.isAppLock) {
            viewDataBinding?.switchApplock?.isChecked = false
            viewDataBinding?.groupApplockVisiblity?.visibility = GONE
        } else {
            viewDataBinding?.groupApplockVisiblity?.visibility = VISIBLE
            viewDataBinding?.switchApplock?.isChecked = true
        }
    }

    override fun setupUI() {

        if (!preferenceHelper.isAppLock) {
            viewDataBinding?.switchApplock?.isPressed = false
            viewDataBinding?.switchApplock?.isChecked = false
            viewDataBinding?.groupApplockVisiblity?.visibility = GONE
        } else {
            viewDataBinding?.groupApplockVisiblity?.visibility = VISIBLE
            viewDataBinding?.switchApplock?.isChecked = true
        }
        viewDataBinding!!.imgBack.setOnClickListener {
            findNavController().navigateUp()
        }

        setDetail()
        setOnClickListner()
    }

    private fun setDetail() {
        viewDataBinding?.txtLockMethodValue?.text = if(!preferenceHelper.isLockModePassword) SecurityOption.PASSCODEBIOMATRIC.value else SecurityOption.PASSCODE.value

        viewDataBinding?.switchApplock?.isChecked = preferenceHelper.isAppLock

        viewDataBinding?.switchTransSignin?.isChecked = preferenceHelper.isTransactionSignIn
    }

    private fun setOnClickListner() {
        viewDataBinding?.cardLockMethod?.setOnClickListener {
            openLockMethodDialog()
        }

        viewDataBinding?.cardSecurityScanner?.setOnClickListener {
            viewDataBinding?.switchSecurityScanner?.performClick()
        }

        viewDataBinding?.cardTransactionSigning?.setOnClickListener {
            viewDataBinding?.switchTransSignin?.performClick()
        }

        viewDataBinding?.cardAppLock?.setOnClickListener {
            viewDataBinding?.switchApplock?.performClick()
            if (viewDataBinding?.switchApplock!!.isChecked) {
                // preferenceHelper.isAppLock = true
                viewDataBinding?.groupApplockVisiblity?.visibility = VISIBLE
                findNavController().safeNavigate(SecurityDirections.actionSecurityToPasscode(true))
            } else {
                preferenceHelper.isAppLock = false
                viewDataBinding?.groupApplockVisiblity?.visibility = GONE
            }
        }

        viewDataBinding?.switchApplock?.setOnCheckedChangeListener { buttonView, isChecked ->
            // Perform actions based on the checked state
            if (buttonView.isPressed) {
                if (isChecked) {
                    //  preferenceHelper.isAppLock = true
                    viewDataBinding?.groupApplockVisiblity?.visibility = VISIBLE
                    findNavController().safeNavigate(
                        SecurityDirections.actionSecurityToPasscode(
                            true
                        )
                    )

                } else {
                    preferenceHelper.isAppLock = false
                    viewDataBinding?.groupApplockVisiblity?.visibility= GONE
                }
            }
        }

        viewDataBinding?.switchTransSignin?.setOnCheckedChangeListener { buttonView, isChecked ->
            if(buttonView.isPressed){
                preferenceHelper.isTransactionSignIn = isChecked
            }
        }
    }

    private fun openLockMethodDialog() {

        val selected = preferenceHelper.isLockModePassword
        val list = mutableListOf<CommonOptionModel>()
        list.add(CommonOptionModel(SecurityOption.PASSCODE.value, "1", selected))
        list.add(CommonOptionModel(SecurityOption.PASSCODEBIOMATRIC.value, "2", !selected))
        DialogOptionSecurity.getInstance().show(
            requireContext(),
            list,
            0,
            object : DialogOptionSecurity.DialogOnClickBtnListner {
                override fun onSelectedItemClicked(selected: String) {
                    when (selected) {
                        SecurityOption.PASSCODE.value -> {
                            preferenceHelper.isLockModePassword = true
                        }

                        SecurityOption.PASSCODEBIOMATRIC.value -> {
                            preferenceHelper.isLockModePassword=false
                        }

                    }
                    setDetail()
                }
            })
    }

    override fun setupObserver() {

    }


}