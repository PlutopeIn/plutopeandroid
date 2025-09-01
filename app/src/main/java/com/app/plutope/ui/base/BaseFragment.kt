package com.app.plutope.ui.base

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.compose.runtime.Composable
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.app.plutope.utils.constant.getNotificationType
import com.app.plutope.utils.constant.key_notification_type
import com.app.plutope.utils.constant.networkErrorMessage
import com.app.plutope.utils.extras.PreferenceHelper
import com.app.plutope.utils.hideLoader
import com.app.plutope.utils.network.NetworkState
import com.app.plutope.utils.showLoaderAnyHow
import com.app.plutope.utils.showToast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject


abstract class BaseFragment<T : ViewDataBinding, V : BaseViewModel<*>> : Fragment() {

    internal var viewDataBinding: T? = null
    private var rootView: View? = null
    var mViewModel: V? = null
    private var googleSignInClient: GoogleSignInClient? = null

    @Inject
    lateinit var preferenceHelper: PreferenceHelper

    companion object {
        const val REQUEST_CODE_SIGN_IN = 1
    }

    /**
     *  Override below method for set view model instance
     * */
    abstract fun getViewModel(): V

    /**
     *  Override below method for set binding variable
     * */
    abstract fun getBindingVariable(): Int

    /**
     * Override below method for set layout id
     * */
    @LayoutRes
    abstract fun getLayoutId(): Int

    /**
     * Override below method for toolbar middle text
     *//*
    abstract fun setUpToolbarMiddleText(): String

    */
    /**
     * Override below method when back button beside text
     *//*
    abstract fun setUpToolbarText(): String*/
    /**
     * setupToolbarText() when toolbar text show
     */
    abstract fun setupToolbarText(): String
    abstract fun setupUI()
    abstract fun setupObserver()

    /**
     * refreshNetwork () is for refresh api call or page when show network error you should use this
     */
    open fun refreshNetwork() {}

    open fun handleSignInResultGet(data: Intent) {}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false)
        rootView = viewDataBinding?.root
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(
            view,
            savedInstanceState
        )
        setGoogleSignInIntialization()
        setupUI()
        setupToolbarText()
        setupObserver()
        mViewModel = getViewModel()
        viewDataBinding?.setVariable(getBindingVariable(), mViewModel)
        viewDataBinding?.lifecycleOwner = viewLifecycleOwner
        viewDataBinding?.executePendingBindings()

        (requireActivity() as BaseActivity).showToolBarTitle(setupToolbarText())

    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewDataBinding?.lifecycleOwner = null // mViewModel = null

    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(mMessageReceiver, IntentFilter(getNotificationType))

    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(mMessageReceiver)
    }


    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            try {
                val type = intent.getStringExtra(key_notification_type)
                if (type == networkErrorMessage) {
                    setupUI()
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setGoogleSignInIntialization() {
        // Initialize GoogleSignInClient
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireContext(), signInOptions)

    }

    protected fun signIn() {
        if (googleSignInClient != null) {
            setGoogleSignInIntialization().apply {
                googleSignInClient?.signInIntent?.let {
                    startActivityForResult(
                        it,
                        REQUEST_CODE_SIGN_IN
                    )
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                handleSignInResultGet(data)
            } else {
                Log.e("TAG", "Sign-in failed.")
            }
        }
    }

    open fun backPressed() {

    }


    fun <T> apiObserveState(
        flow: Flow<NetworkState<T>>,
        onSuccess: (T?) -> Unit,
        onLoading: () -> Unit = { showLoader() },
        onError: (String?) -> Unit = { showToast(it ?: "An error occurred") },
        onSessionOut: (String?) -> Unit = { handleSessionOut() }
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                flow.collect { state ->
                    when (state) {
                        is NetworkState.Success -> onSuccess(state.data)
                        is NetworkState.Loading -> onLoading()
                        is NetworkState.Error -> onError(state.message.toString())
                        is NetworkState.SessionOut -> {
                            onSessionOut(state.message.toString())

                        }

                        else -> {}
                    }
                }
            }
        }
    }

    @Composable
    fun <T> ApiObserveComposeState(
        flow: NetworkState<T>,
        onLoading: @Composable () -> Unit,
        onError: @Composable (String) -> Unit,
        onSuccess: @Composable (T?) -> Unit
    ) {
        when (flow) {
            is NetworkState.Error -> {
                hideLoader()
                onError(
                    flow.message.toString()
                )

            }

            is NetworkState.Loading -> onLoading()
            is NetworkState.SessionOut -> {
                handleSessionOut()
                hideLoader()
            }

            is NetworkState.Success -> {
                hideLoader()
                onSuccess(flow.data)
            }

            else -> {

            }
        }
    }

    // Provide common behavior for loaders, toasts, etc.
    open fun showLoader() {
        requireContext().showLoaderAnyHow()
    }


    open fun showToast(message: String?) {
        requireContext().showToast(message!!)
    }

    open fun handleSessionOut() {
        hideLoader()
        showToast("Un Auth")
        (activity as BaseActivity).logoutCardBetaUser("")

    }


}