package com.app.plutope.ui.fragment.card.sign_up_login

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentSignUpBinding
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.utils.safeNavigate
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUp : BaseFragment<FragmentSignUpBinding, SignUpViewModel>() {

    private val signUpViewModel: SignUpViewModel by viewModels()
    override fun getViewModel(): SignUpViewModel {
        return signUpViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.signUpViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_sign_up
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

        viewDataBinding!!.txtForgotPassword.setOnClickListener {
            findNavController().safeNavigate(SignUpDirections.actionSignUpToForgotPassword())
        }

    }

    override fun setupObserver() {

    }


}