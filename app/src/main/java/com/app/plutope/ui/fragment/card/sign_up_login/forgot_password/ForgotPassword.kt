package com.app.plutope.ui.fragment.card.sign_up_login.forgot_password

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentForgotPasswordBinding
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.ui.fragment.card.sign_up_login.SignUpDirections
import com.app.plutope.utils.safeNavigate
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ForgotPassword : BaseFragment<FragmentForgotPasswordBinding, ForgotPasswordViewModel>() {
    private val forgotPasswordViewModel: ForgotPasswordViewModel by viewModels()
    override fun getViewModel(): ForgotPasswordViewModel {
        return forgotPasswordViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.forgotPasswordViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_forgot_password
    }

    override fun setupToolbarText(): String {
        return ""
    }

    override fun setupUI() {
        viewDataBinding!!.imgBack.setOnClickListener {
            findNavController().navigateUp()
        }

        viewDataBinding!!.btnContinue.setOnClickListener {
            findNavController().safeNavigate(SignUpDirections.actionSignUpToPersonalDetail())
        }
    }

    override fun setupObserver() {

    }

}