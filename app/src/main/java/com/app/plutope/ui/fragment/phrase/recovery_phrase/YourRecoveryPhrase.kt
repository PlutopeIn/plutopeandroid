package com.app.plutope.ui.fragment.phrase.recovery_phrase

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.MotionEvent
import android.view.View
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
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.ui.fragment.phrase.verify_phrase.VerifySecretPhraseViewModel
import com.app.plutope.ui.fragment.wallet.backup.PhraseBackupFragment
import com.app.plutope.utils.GoogleSignInListner
import com.app.plutope.utils.copyToClipboard
import com.app.plutope.utils.extras.DriveServiceHelper
import com.app.plutope.utils.extras.setSafeOnClickListener
import com.app.plutope.utils.hideLoader
import com.app.plutope.utils.network.NetworkState
import com.app.plutope.utils.requestGoogleSignIn
import com.app.plutope.utils.safeNavigate
import com.app.plutope.utils.showToast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import dagger.hilt.android.AndroidEntryPoint
import eightbitlab.com.blurview.RenderScriptBlur
import kotlinx.coroutines.launch
import java.lang.Integer.min


@AndroidEntryPoint
class YourRecoveryPhrase :
    BaseFragment<FragmentYourRecoveryPhraseBinding, YourRecoveryPhraseViewModel>() {

    private var wordsList: Array<String> = arrayOf()
    private var words: String? = ""
    var isShowPhrases: Boolean = false
    private val yourRecoveryPhraseViewModel: YourRecoveryPhraseViewModel by viewModels()
    private val verifySecretPhraseViewModel: VerifySecretPhraseViewModel by viewModels()
    private val args: YourRecoveryPhraseArgs by navArgs()
    val spanCount = 6
    private lateinit var mDriveServiceHelper: DriveServiceHelper
    override fun getViewModel(): YourRecoveryPhraseViewModel {
        return yourRecoveryPhraseViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.backUpWalletCheckViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_your_recovery_phrase
    }

    override fun setupToolbarText(): String {
        return getString(R.string.your_recovery_phrase)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun setupUI() {
        // Set the secure flag to prevent screenshots
        requireActivity().window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

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
                if(args.walletModel.w_is_cloud_backup && args.isFromDriveBackup){
                    viewDataBinding?.btnContinue?.visibility = GONE
                    viewDataBinding?.txtCopy?.visibility = GONE
                }else {
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

        val radius = 12f
        val decorView: View = requireActivity().window.decorView
        val windowBackground = decorView.background
        viewDataBinding!!.blurView.setupWith(
            viewDataBinding!!.rvPhraseGeneratedList,
            RenderScriptBlur(requireContext())
        ) // or RenderEffectBlur
            .setFrameClearDrawable(windowBackground) // Optional
            .setBlurRadius(radius)


        /*  viewDataBinding!!.cardViewBlur.setOnClickListener {
              viewDataBinding!!.txtHideShowPhrase.performClick()
          }

          viewDataBinding!!.cardView.setOnClickListener{
              viewDataBinding!!.txtHideShowPhrase.performClick()
          }*/

        viewDataBinding!!.cardViewBlur.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    viewDataBinding!!.cardViewBlur.visibility = GONE
                    viewDataBinding!!.txtTapToView.visibility = GONE
                    true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    viewDataBinding!!.cardViewBlur.visibility = VISIBLE
                    viewDataBinding!!.txtTapToView.visibility = VISIBLE
                    true
                }

                else -> false
            }
        }

        /*
                viewDataBinding!!.txtHideShowPhrase.setOnClickListener {
                    isShowPhrases = !isShowPhrases
                    if (isShowPhrases) {
                        viewDataBinding!!.cardViewBlur.visibility = GONE
                        viewDataBinding!!.txtTapToView.visibility = GONE
                        viewDataBinding!!.txtHideShowPhrase.text = getString(R.string.hide_recovery_phrase)
                    } else {
                        viewDataBinding!!.cardViewBlur.visibility = VISIBLE
                        viewDataBinding!!.txtTapToView.visibility = VISIBLE
                        viewDataBinding!!.txtHideShowPhrase.text = getString(R.string.show_recovery_phrase)
                    }
                }
        */


        val adapter = PhraseGenerateListAdapter { _: String, _: Int -> }
        adapter.submitList(wordsList.toMutableList())

        val layoutManager =
            GridLayoutManager(requireContext(), spanCount, GridLayoutManager.HORIZONTAL, false)
        viewDataBinding?.rvPhraseGeneratedList?.layoutManager = layoutManager

        viewDataBinding?.rvPhraseGeneratedList?.adapter = adapter

        setListener()
    }

    override fun setupObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                verifySecretPhraseViewModel.updateWalletBackupResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                                val wallet = args.walletModel
                                wallet.w_is_cloud_backup=false
                                findNavController().navigate(YourRecoveryPhraseDirections.actionYourRecoveryPhraseToRecoveryWalletFragment(wallet))
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
        viewDataBinding?.btnContinue?.setOnClickListener {
            findNavController().safeNavigate(
                YourRecoveryPhraseDirections.actionYourRecoveryPhraseToVerifySecretPhrase(
                    words.toString(),
                    wordsList,
                    args.walletModel
                )
            )
        }

        viewDataBinding?.txtCopy?.setOnClickListener {
            requireContext().copyToClipboard("$words")
            requireContext().showToast("Copied $words")
        }

        viewDataBinding?.btnDelete?.setSafeOnClickListener {
            if (!args.walletModel.w_is_manual_backup) {
                openConfirmationDialog(getString(R.string.manual_backup_required),getString(R.string.before_deleting_your_icloud_backup_ensure_a_manual_backup_is_in_place_for_your_data_s_security),true)
            } else {
                openConfirmationDialog(getString(R.string.delete_backup),
                    getString(R.string.are_you_sure_you_want_to_delete_cloud_backup),false)
            }
        }
    }

    private fun openConfirmationDialog(title:String,subtitle:String,isFromRecovery: Boolean) {
        ConfirmationAlertDialog.getInstance().show(
            requireContext(),
            title,
            subtitle,
            isFromRecovery,
            listener = object : ConfirmationAlertDialog.DialogOnClickBtnListner {
                override fun onDeleteClick() {
                    if(!isFromRecovery){
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
}



