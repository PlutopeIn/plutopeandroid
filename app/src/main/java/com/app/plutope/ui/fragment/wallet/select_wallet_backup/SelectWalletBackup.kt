package com.app.plutope.ui.fragment.wallet.select_wallet_backup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentSelectWalletBackupBinding
import com.app.plutope.model.GoogleDriveBackupModel
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.ui.fragment.wallet.backup.PhraseBackupFragment
import com.app.plutope.utils.constant.FOLDER_NAME
import com.app.plutope.utils.constant.PARENT_FOLDER_ID
import com.app.plutope.utils.convertDateTimeToDDMMMYYYY
import com.app.plutope.utils.extras.DriveServiceHelper
import com.app.plutope.utils.extras.DriveServiceHelper.ContentCallback
import com.app.plutope.utils.hideLoader
import com.app.plutope.utils.showLoader
import com.app.plutope.utils.showSnackBar
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class SelectWalletBackup :
    BaseFragment<FragmentSelectWalletBackupBinding, SelectWalletBackupViewModel>() {

    private lateinit var googleAcct: GoogleSignInAccount
    private val selectWalletBackupViewModel: SelectWalletBackupViewModel by viewModels()
    private var walletBackupListAdapter: WalletBackupListAdapter? = null
    private lateinit var mDriveServiceHelper: DriveServiceHelper
    private val googleDriveBackupList: MutableList<GoogleDriveBackupModel> = mutableListOf()

    companion object {
        const val keySelectWallet = "SelectWalletBackUp"
        const val keyBundleSelectWallet = "BundleSelectWallet"
    }

    override fun getViewModel(): SelectWalletBackupViewModel {
        return selectWalletBackupViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.selectWalletBackupViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_select_wallet_backup
    }

    override fun setupToolbarText(): String {
        return getString(R.string.select_wallet_backup)
    }

    override fun setupUI() {
        walletBackupListAdapter = WalletBackupListAdapter {
            getFileContent(it)


        }

        viewDataBinding!!.rvWalletList.adapter = walletBackupListAdapter

        viewDataBinding!!.layoutWarning.setOnClickListener {
            signIn()
        }

        signIn()
    }

    override fun setupObserver() {

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

            googleAcct = googleAccount
                val credential = GoogleAccountCredential.usingOAuth2(
                    requireContext(), setOf(DriveScopes.DRIVE_FILE)
                )
                credential.selectedAccount = googleAccount.account
                val googleDriveService: Drive = Drive.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    JacksonFactory.getDefaultInstance(),
                    credential
                ).setApplicationName("Drive API Migration").build()




                mDriveServiceHelper = DriveServiceHelper(googleDriveService)
                requireContext().showLoader()
                val folderIdTask: Task<String> = mDriveServiceHelper.getFolderIdFromFolderName(PARENT_FOLDER_ID, FOLDER_NAME)

                folderIdTask.addOnSuccessListener { folderId ->
                    if (folderId.isNotEmpty()) {
                        mDriveServiceHelper.getFileListFromFolder(
                            requireContext(),
                            googleAccount,
                            folderId
                        ) { recoveryIntent, requestCode ->
                            startActivityForResult(
                                recoveryIntent, requestCode
                            )
                        }.addOnSuccessListener { fileList ->

                            if (fileList != null) {
                                val files = fileList.files
                                googleDriveBackupList.clear()
                                var pendingFileContentRequests = files.size
                                for (file in files) {
                                    val fileId = file.id
                                    val fileName = file.name
                                    googleDriveBackupList.add(
                                        GoogleDriveBackupModel(
                                            fileId,
                                            fileName,
                                            convertDateTimeToDDMMMYYYY(file.createdTime.toString()),
                                            ""
                                        )
                                    )

                                    if (--pendingFileContentRequests == 0) {
                                        hideLoader()
                                        walletBackupListAdapter?.submitList(
                                            googleDriveBackupList
                                        )
                                    }

                                }

                            } else {
                                hideLoader()
                            }
                        }.addOnFailureListener { e ->
                            hideLoader()
                            e.printStackTrace()
                        }
                    } else {
                        hideLoader()
                        viewDataBinding?.constRoot?.showSnackBar("Folder not found in Your Drive")

                    }
                }.addOnFailureListener { exception ->
                    hideLoader()
                    viewDataBinding?.constRoot?.showSnackBar("Failed to get folder ID")

                }



            }.addOnFailureListener { exception: Exception? ->
                hideLoader()

            }
    }

    private fun getFileContent(
        model: GoogleDriveBackupModel
    ) {
        requireContext().showLoader()
        lifecycleScope.launch {
            mDriveServiceHelper.getFileContent(
                requireContext(),
                googleAcct,
                model.fileId,
                object : ContentCallback {
                    override fun onContentAvailable(content: String?) {
                        hideLoader()
                        model.fileContent = content.toString()
                        val bundle = Bundle()
                        bundle.putParcelable(keySelectWallet, model)
                        setFragmentResult(keyBundleSelectWallet, bundle)
                        findNavController().navigateUp()
                    }

                    override fun onContentFailed() {
                        hideLoader()
                        viewDataBinding?.constRoot?.showSnackBar("File content is blank!")
                    }

                }
            )
        }

    }
}