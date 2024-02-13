package com.app.plutope.ui.fragment.wallet.restore_wallet


import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentRestoreWalletBinding
import com.app.plutope.model.GoogleDriveBackupModel
import com.app.plutope.model.Wallets
import com.app.plutope.ui.base.BaseActivity
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.ui.fragment.wallet.select_wallet_backup.SelectWalletBackup
import com.app.plutope.utils.extras.DriveServiceHelper
import com.app.plutope.utils.safeNavigate
import com.app.plutope.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

@AndroidEntryPoint
class RestoreWallet : BaseFragment<FragmentRestoreWalletBinding, RestoreWalletViewModel>(),
    DriveServiceHelper.UserRecoverableAuthListener {

    private val restoreWalletViewModel: RestoreWalletViewModel by viewModels()
    private lateinit var fileSelectionLauncher: ActivityResultLauncher<String>

    override fun getViewModel(): RestoreWalletViewModel {
        return restoreWalletViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.restoreWalletViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_restore_wallet
    }

    override fun setupToolbarText(): String {
        return ""
    }

    override fun setupUI() {
        fileSelectionLauncher = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { result ->
            result?.let { selectedFileUri ->
                handleSelectedFile(selectedFileUri)
            }
        }
        setOnClickListner()
        setFragmentResultListner()
    }

    private fun setFragmentResultListner() {

        setFragmentResultListener(SelectWalletBackup.keyBundleSelectWallet) { _, bundle ->
            val selectedFile =
                bundle.getParcelable(SelectWalletBackup.keySelectWallet) as? GoogleDriveBackupModel
            val fileContent = selectedFile?.fileContent

            if (fileContent?.isNotEmpty() == true) {
                val jsonObj = JSONObject(fileContent)
                if (jsonObj.has("encryptedData")) {
                    findNavController().safeNavigate(
                        RestoreWalletDirections.actionRestoreWalletToSetEncryptionPassword(
                            selectedFile.fileName.substringBefore("."),
                            fileContent,
                            Wallets()
                        )
                    )
                } else {
                    requireContext().showToast("Invalid File")
                }
            } else {
                requireContext().showToast("File is Blank!")
            }
        }

    }

    private fun setOnClickListner() {
        viewDataBinding!!.imgBack.setOnClickListener {
            findNavController().navigateUp()
        }


        viewDataBinding!!.btnRestoreWithSecretPhrase.setOnClickListener {
            (activity as BaseActivity).askNotificationPermission()
            findNavController().safeNavigate(RestoreWalletDirections.actionRestoreWalletToImportMultiWallet())
        }

        viewDataBinding!!.btnRestoreWithGoogleDrive.setOnClickListener {
            // openFileManager()
            findNavController().safeNavigate(RestoreWalletDirections.actionRestoreWalletToSelectWalletBackup())
        }
    }

    override fun setupObserver() {

    }

    private fun handleSelectedFile(selectedFileUri: Uri) {
        requireActivity().contentResolver.openInputStream(selectedFileUri)?.use { inputStream ->
            try {
                val fileContent = readTextFromInputStream(inputStream)
                if (fileContent.isNotEmpty()) {
                    val jsonObj = JSONObject(fileContent)
                    if (jsonObj.has("encryptedData")) {
                        findNavController().safeNavigate(
                            RestoreWalletDirections.actionRestoreWalletToSetEncryptionPassword(
                                "",
                                fileContent,
                                Wallets()
                            )
                        )
                    } else {
                        requireContext().showToast("Invalid File")
                    }
                } else {
                    requireContext().showToast("File is Blank!")
                }

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun readTextFromInputStream(inputStream: InputStream): String {
        val stringBuilder = StringBuilder()
        BufferedReader(InputStreamReader(inputStream)).use { reader ->
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                stringBuilder.append(line)
            }
        }
        return stringBuilder.toString()
    }

    private fun openFileManager() {
        fileSelectionLauncher.launch("application/json")
    }

    override fun requestUserRecoverableAuth(recoveryIntent: Intent?, requestCode: Int) {
        recoveryIntent?.let { startActivityForResult(it, requestCode) }
    }
}