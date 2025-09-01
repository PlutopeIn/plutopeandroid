package com.app.plutope.ui.fragment.contact

import android.content.ClipboardManager
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentAddContactBinding
import com.app.plutope.model.ContactModel
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.utils.constant.serverErrorMessage
import com.app.plutope.utils.enableDisableButton
import com.app.plutope.utils.extractQRCodeScannerInfo
import com.app.plutope.utils.hideLoader
import com.app.plutope.utils.isValidTokenContractAddress
import com.app.plutope.utils.network.NetworkState
import com.app.plutope.utils.showLoader
import com.app.plutope.utils.showSnackBar
import com.app.plutope.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanQRCode
import kotlinx.coroutines.launch
import org.web3j.crypto.WalletUtils

@AndroidEntryPoint
class AddContactFragment : BaseFragment<FragmentAddContactBinding, ContactListViewModel>() {
    private val args: AddContactFragmentArgs by navArgs()
    private val contactViewModel :ContactListViewModel by viewModels()
    /*
        private val barcodeLauncher = registerForActivityResult<ScanOptions, ScanIntentResult>(
            ScanContract()
        ) { result: ScanIntentResult ->
            if (result.contents == null) {
                Toast.makeText(requireContext(), "Cancelled", Toast.LENGTH_LONG).show()
            } else {
                if (!isValidTokenContractAddress(result.contents.toString())) {
                    viewDataBinding?.constRoot?.showSnackBar(getString(R.string.invalid_address))
                } else {
                    viewDataBinding?.edtContractAddress?.setText("")
                    viewDataBinding!!.edtContractAddress.setText(result.contents.toString())

                }
            }
        }
    */

    private val scanQrCode = registerForActivityResult(ScanQRCode()) { result: QRResult ->
        when (result) {
            is QRResult.QRSuccess -> {

                val qrResult = extractQRCodeScannerInfo(result.content.rawValue)

                if (qrResult?.first != null) {
                    // viewDataBinding!!.edtContractAddress.setText(cleanEthereumAddress(qrResult.first))
                    // viewDataBinding!!.edtContractAddress.setText(qrResult.first)

                    if (!WalletUtils.isValidAddress(qrResult.first)) {
                        requireContext().showToast(getString(R.string.invalid_address))
                    } else {
                        viewDataBinding?.edtContractAddress?.setText("")
                        viewDataBinding!!.edtContractAddress.setText(qrResult.first)

                    }


                } else {
                    //  viewDataBinding!!.edtContractAddress.setText(cleanEthereumAddress(result))
                    viewDataBinding!!.edtContractAddress.setText(result.content.rawValue)
                }

                /*
                                if (!isValidTokenContractAddress(qrResult?.first!!)) {
                                    viewDataBinding?.constRoot?.showSnackBar(getString(R.string.invalid_address))
                                } else {
                                    viewDataBinding?.edtContractAddress?.setText("")
                                    viewDataBinding!!.edtContractAddress.setText(qrResult.first)

                                }
                */

            }

            QRResult.QRUserCanceled -> Toast.makeText(
                requireContext(),
                "Cancelled",
                Toast.LENGTH_LONG
            ).show()

            QRResult.QRMissingPermission -> Toast.makeText(
                requireContext(),
                "Missing permission",
                Toast.LENGTH_LONG
            ).show()

            is QRResult.QRError -> "${result.exception.javaClass.simpleName}: ${result.exception.localizedMessage}"
        }


    }


    override fun getViewModel(): ContactListViewModel {
        return contactViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.contact
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_add_contact
    }

    override fun setupToolbarText(): String {
        return ""
    }

    override fun setupUI() {
        if (args.isEditContact){
            viewDataBinding!!.btnAddContact.visibility = View.GONE
            viewDataBinding!!.btnSave.visibility = View.VISIBLE
            viewDataBinding!!.btnDelete.visibility = View.VISIBLE
            viewDataBinding!!.txtProfileTitle.text = getString(R.string.edit_contacts)
            viewDataBinding!!.edtContractAddress.setText(args.contactModel!!.address)
            viewDataBinding!!.edtName.setText(args.contactModel!!.name)
        }else{
            viewDataBinding!!.btnAddContact.visibility = View.VISIBLE
            viewDataBinding!!.btnSave.visibility = View.GONE
            viewDataBinding!!.btnDelete.visibility = View.GONE
            viewDataBinding!!.txtProfileTitle.text = getString(R.string.add_contacts)
        }
        viewDataBinding?.btnAddContact?.enableDisableButton(false)
        viewDataBinding?.btnSave?.enableDisableButton(false)
        viewDataBinding!!.txtProfileTitle.setOnClickListener {
            findNavController().navigateUp()
        }
        setOnClickListner()
    }

    private fun setOnClickListner() {
        viewDataBinding?.imgScanner?.setOnClickListener {

            scanQrCode.launch(null)

            /* val options = ScanOptions()
             barcodeLauncher.launch(
                 options.setOrientationLocked(false)
                     .setBarcodeImageEnabled(true)
             )*/
        }

        viewDataBinding?.txtPaste?.setOnClickListener {
            viewDataBinding?.edtContractAddress?.setText("")
            val clipboard =
                requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val item = clipboard.primaryClip?.getItemAt(0)
            val pasteData = item?.text
            if (!isValidTokenContractAddress(pasteData.toString())) {
                viewDataBinding?.constRoot?.showSnackBar(getString(R.string.invalid_address))
            } else {
                viewDataBinding?.edtContractAddress?.setText(pasteData)
                viewDataBinding?.edtContractAddress?.setSelection(viewDataBinding!!.edtContractAddress.length())

            }
        }

        viewDataBinding?.btnAddContact?.setOnClickListener {
            when {
                !isValidTokenContractAddress(
                    viewDataBinding?.edtContractAddress?.text.toString().replace("...", "")
                ) -> viewDataBinding?.constRoot?.showSnackBar(
                    getString(R.string.invalid_address)
                )

                else -> {

                    contactViewModel.executeInsertTokens(ContactModel(name = viewDataBinding?.edtName?.text.toString(), address = viewDataBinding?.edtContractAddress?.text.toString().trim()))
                }
            }
        }
        viewDataBinding?.btnSave?.setOnClickListener {
            when {
                !isValidTokenContractAddress(
                    viewDataBinding?.edtContractAddress?.text.toString().replace("...", "")
                ) -> viewDataBinding?.constRoot?.showSnackBar(
                    getString(R.string.invalid_address)
                )

                else -> {

                    contactViewModel.executeInsertTokens(ContactModel(name = viewDataBinding?.edtName?.text.toString(), address = viewDataBinding?.edtContractAddress?.text.toString().trim()))
                }
            }
        }

        viewDataBinding!!.btnDelete.setOnClickListener {
            contactViewModel.executeDeleteTokens(args.contactModel!!.id)
        }

        viewDataBinding?.edtContractAddress?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                checkRequiredFields()
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
        viewDataBinding?.edtName?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                checkRequiredFields()
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

    }

    override fun setupObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                contactViewModel.insertContactResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            hideLoader()
                            //detail page
                            if (it.data!=null) {
                                Toast.makeText(
                                    requireContext(),
                                    if (!args.isEditContact) "Contact successfully added" else "Contact successfully updated",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                                findNavController().navigateUp()
                            }
                        }

                        is NetworkState.Loading -> {
                            requireContext().showLoader()
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
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                contactViewModel.deleteContactResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            hideLoader()
                            findNavController().navigateUp()
                            requireContext().showToast("Contact successfully deleted")
                        }

                        is NetworkState.Loading -> {
                            requireContext().showLoader()
                        }

                        is NetworkState.Error -> {
                            hideLoader()
                            requireContext().showToast(serverErrorMessage)
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

    private fun checkRequiredFields() {
        when {
            viewDataBinding?.edtContractAddress?.text.toString().isEmpty() -> {
                viewDataBinding?.btnAddContact?.enableDisableButton(false)
            }

            viewDataBinding?.edtName?.text.toString().isEmpty() -> {
                viewDataBinding?.btnAddContact?.enableDisableButton(false)
            }


            else -> {
                viewDataBinding?.btnAddContact?.enableDisableButton(true)
                viewDataBinding?.btnSave?.enableDisableButton(true)
            }
        }
    }
}