package com.app.plutope.ui.fragment.add_custom_token

import android.content.ClipboardManager
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentAddCustomTokanBinding
import com.app.plutope.model.Tokens
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.ui.fragment.token.TokenViewModel
import com.app.plutope.ui.fragment.transactions.receive.Receive
import com.app.plutope.utils.constant.AddCustomTokenPageType
import com.app.plutope.utils.constant.WHAT_IS_CUSTOM_TOKEN_URL
import com.app.plutope.utils.enableDisableButton
import com.app.plutope.utils.extractQRCodeScannerInfo
import com.app.plutope.utils.hideLoader
import com.app.plutope.utils.isValidTokenContractAddress
import com.app.plutope.utils.network.NetworkState
import com.app.plutope.utils.safeNavigate
import com.app.plutope.utils.showLoader
import com.app.plutope.utils.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanQRCode
import kotlinx.coroutines.launch
import org.web3j.crypto.WalletUtils

@AndroidEntryPoint
class AddCustomToken : BaseFragment<FragmentAddCustomTokanBinding, AddCustomTokenViewModel>() {
    private var tokenAlreadyModel: Tokens = Tokens()
    private var isAlreadyToken: Boolean = false
    private lateinit var tokenList: MutableList<Tokens>
    private var selectedValue: Tokens? = null
    private val addCustomTokenViewModel: AddCustomTokenViewModel by viewModels()
    private val tokenViewModel: TokenViewModel by viewModels()
    override fun getViewModel(): AddCustomTokenViewModel {
        return addCustomTokenViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.addCustomTokenViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_add_custom_tokan
    }

    override fun setupToolbarText(): String {
        return ""
    }

    override fun setupUI() {
        setDetail()
        setOnClickListeners()

        setFragmentResultListener(Receive.keyReceiveCustomToken) { _, bundle ->
            selectedValue = bundle.getParcelable(Receive.keyReceive) as? Tokens
            viewDataBinding?.txtNetwork?.text = selectedValue?.t_name
        }


    }


    private val scanQrCode = registerForActivityResult(ScanQRCode()) { result: QRResult ->
        when (result) {
            is QRResult.QRSuccess -> {

                val qrResult = extractQRCodeScannerInfo(result.content.rawValue)

                if (qrResult?.first != null) {
                    // viewDataBinding!!.edtContractAddress.setText(cleanEthereumAddress(qrResult.first))
                    //  viewDataBinding!!.edtContractAddress.setText(qrResult.first)

                    if (!WalletUtils.isValidAddress(qrResult.first)) {
                        viewDataBinding?.constRoot?.showSnackBar(getString(R.string.invalid_address))
                    } else {
                        viewDataBinding?.edtContractAddress?.setText("")
                        viewDataBinding!!.edtContractAddress.setText(qrResult.first)

                        setAddressWithDetail(qrResult.first)


                        /* val resultModel =
                             tokenList.filter {
                                 it.t_address?.lowercase() == qrResult.first
                                     .lowercase() && it.t_type?.lowercase() == selectedValue?.t_type?.lowercase()
                             }
                         if (resultModel.isNotEmpty()) {
                             isAlreadyToken = true
                             tokenAlreadyModel = resultModel[0]
                             setTokenDetail(resultModel[0])
                             enableDisableInput(false)
                         } else {
                             isAlreadyToken = false
                             enableDisableInput(true)
                         }*/
                    }


                } else {
                    //  viewDataBinding!!.edtContractAddress.setText(cleanEthereumAddress(result))
                    viewDataBinding!!.edtContractAddress.setText(result.content.rawValue)
                    setAddressWithDetail(result.content.rawValue)

                }


                /*
                                if (!isValidTokenContractAddress(qrResult?.first)) {
                                    viewDataBinding?.constRoot?.showSnackBar(getString(R.string.invalid_address))
                                } else {
                                    viewDataBinding?.edtContractAddress?.setText("")
                                    viewDataBinding!!.edtContractAddress.setText(qrResult.first)
                                    val resultModel =
                                        tokenList.filter {
                                            it.t_address?.lowercase() == qrResult.first!!
                                                .lowercase() && it.t_type?.lowercase() == selectedValue?.t_type?.lowercase()
                                        }
                                    if (resultModel.isNotEmpty()) {
                                        isAlreadyToken = true
                                        tokenAlreadyModel = resultModel[0]
                                        setTokenDetail(resultModel[0])
                                        enableDisableInput(false)
                                    } else {
                                        isAlreadyToken = false
                                        enableDisableInput(true)
                                    }
                                }
                */
                /*
                                if (!isValidTokenContractAddress(result.content.rawValue)) {
                                    viewDataBinding?.constRoot?.showSnackBar(getString(R.string.invalid_address))
                                } else {
                                    viewDataBinding?.edtContractAddress?.setText("")
                                    viewDataBinding!!.edtContractAddress.setText(result.content.rawValue)
                                    val resultModel =
                                        tokenList.filter {
                                            it.t_address?.lowercase() == result.content.rawValue
                                                .lowercase() && it.t_type?.lowercase() == selectedValue?.t_type?.lowercase()
                                        }
                                    if (resultModel.isNotEmpty()) {
                                        isAlreadyToken = true
                                        tokenAlreadyModel = resultModel[0]
                                        setTokenDetail(resultModel[0])
                                        enableDisableInput(false)
                                    } else {
                                        isAlreadyToken = false
                                        enableDisableInput(true)
                                    }
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

    private fun setAddressWithDetail(address: String) {
        val resultModel =
            tokenList.filter {
                it.t_address?.lowercase() == address
                    .lowercase() && it.t_type?.lowercase() == selectedValue?.t_type?.lowercase()
            }
        if (resultModel.isNotEmpty()) {
            isAlreadyToken = true
            tokenAlreadyModel = resultModel[0]
            setTokenDetail(resultModel[0])
            enableDisableInput(false)
        } else {
            isAlreadyToken = false
            enableDisableInput(true)
        }
    }

    private fun setOnClickListeners() {
        viewDataBinding?.apply {
            imgBack.setOnClickListener {
                findNavController().navigateUp()
            }

            layoutNetwork.setOnClickListener {
                findNavController().safeNavigate(
                    AddCustomTokenDirections.actionGlobalToReceive(
                        AddCustomTokenPageType
                    )
                )
            }

            imgScanner.setOnClickListener {
                scanQrCode.launch(null)
            }

            btnCancel.setOnClickListener {
                findNavController().navigateUp()
            }

            txtPaste.setOnClickListener {
                val clipboard =
                    requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val item = clipboard.primaryClip?.getItemAt(0)
                val pasteData = item?.text
                if (!isValidTokenContractAddress(pasteData.toString())) {
                    viewDataBinding?.constRoot?.showSnackBar("Invalid Address")
                } else {
                    viewDataBinding?.edtContractAddress?.setText("")
                    viewDataBinding?.edtContractAddress?.setText(pasteData)
                    viewDataBinding?.edtContractAddress?.setSelection(viewDataBinding!!.edtContractAddress.length())

                    val resultModel = tokenList.filter {
                        it.t_address?.lowercase() == pasteData.toString()
                            .lowercase() && it.t_type?.lowercase() == selectedValue?.t_type?.lowercase()
                    }
                    if (resultModel.isNotEmpty()) {
                        isAlreadyToken = true
                        tokenAlreadyModel = resultModel[0]
                        setTokenDetail(resultModel[0])
                        enableDisableInput(false)
                    } else {
                        isAlreadyToken = false
                        enableDisableInput(true)
                    }
                }
            }

            btnSave.setOnClickListener {
                when {
                    !isValidTokenContractAddress(
                        viewDataBinding?.edtContractAddress?.text.toString().replace("...", "")
                    ) -> viewDataBinding?.constRoot?.showSnackBar(
                        "Invalid Address"
                    )

                    else -> {

                        if (isAlreadyToken) {
                            tokenViewModel.executeUpdateToken(tokenAlreadyModel)
                        } else {
                            tokenViewModel.executeInsertNewTokens(
                                Tokens(
                                    t_decimal = viewDataBinding?.edtDecimal?.text.toString()
                                        .toInt(),
                                    t_address = viewDataBinding?.edtContractAddress?.text.toString()
                                        .replace("...", ""),
                                    t_name = viewDataBinding?.edtName?.text.toString(),
                                    t_symbol = viewDataBinding?.edtSymbol?.text.toString()
                                        .uppercase(),
                                    t_type = selectedValue?.t_type.toString(),
                                    isCustomTokens = true
                                )
                            )
                        }
                    }
                }
            }

            edtContractAddress.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    checkRequiredFields()
                }

                override fun afterTextChanged(s: Editable?) {

                }

            })

            edtName.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    checkRequiredFields()
                }

                override fun afterTextChanged(s: Editable?) {

                }

            })

            edtSymbol.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    checkRequiredFields()
                }

                override fun afterTextChanged(s: Editable?) {

                }

            })

            edtDecimal.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    checkRequiredFields()
                }

                override fun afterTextChanged(s: Editable?) {

                }

            })

            txtTokenInstruction.setOnClickListener {
                findNavController().safeNavigate(
                    AddCustomTokenDirections.actionAddCustomTokenToWebViewToolbar(
                        WHAT_IS_CUSTOM_TOKEN_URL,
                        "What is Custom Tokens?"
                    )
                )
            }
        }


    }

    private fun enableDisableInput(b: Boolean) {
        if (!b) {
            viewDataBinding?.apply {
                edtName.isEnabled = false
                edtSymbol.isEnabled = false
                edtDecimal.isEnabled = false
                edtContractAddress.isEnabled = false
                layoutNetwork.isEnabled = false
            }
        } else {
            viewDataBinding?.apply {
                edtName.isEnabled = true
                edtSymbol.isEnabled = true
                edtDecimal.isEnabled = true
                edtContractAddress.isEnabled = true
                layoutNetwork.isEnabled = true
            }
        }
    }

    private fun setTokenDetail(resultModel: Tokens) {
        viewDataBinding?.edtName?.setText(resultModel.t_name.toString())
        viewDataBinding?.edtSymbol?.setText(resultModel.t_symbol.toString())
        viewDataBinding?.edtDecimal?.setText(resultModel.t_decimal.toString())
    }

    private fun checkRequiredFields() {
        when {
            viewDataBinding?.edtContractAddress?.text.toString().isEmpty() -> {
                viewDataBinding?.btnSave?.enableDisableButton(false)
            }

            viewDataBinding?.edtName?.text.toString().isEmpty() -> {
                viewDataBinding?.btnSave?.enableDisableButton(false)
            }

            viewDataBinding?.edtSymbol?.text.toString().isEmpty() -> {
                viewDataBinding?.btnSave?.enableDisableButton(false)
            }

            viewDataBinding?.edtDecimal?.text.toString().isEmpty() -> {
                viewDataBinding?.btnSave?.enableDisableButton(false)
            }

            else -> {
                viewDataBinding?.btnSave?.enableDisableButton(true)
            }
        }
    }

    private fun setDetail() {
        viewDataBinding?.btnSave?.enableDisableButton(false)
        tokenViewModel.fetchAllTokensList()

        /*  tokenList = tokenViewModel.getAllTokensList() as MutableList<Tokens>
         val ethModel = tokenList.filter { it.t_symbol == "ETH" }
         selectedValue = ethModel[0]*/


    }

    override fun setupObserver() {

        tokenViewModel.tokenList.observe(viewLifecycleOwner) { tokens ->
            tokenList = tokens as MutableList<Tokens>
            val ethModel = tokenList.filter { it.t_symbol == "ETH" }
            selectedValue = ethModel[0]
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                tokenViewModel.updateTokenResp.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            hideLoader()
                            if (it.status.toString() == "0")
                                findNavController().safeNavigate(AddCustomTokenDirections.actionAddCustomTokenToDashboard())
                            else
                                findNavController().safeNavigate(
                                    AddCustomTokenDirections.actionAddCustomTokenToBuyDetails(
                                        it.data!!
                                    )
                                )

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
                tokenViewModel.insertNewTokenResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            hideLoader()
                            if (it.data != null && viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                                findNavController().safeNavigate(
                                    AddCustomTokenDirections.actionAddCustomTokenToBuyDetails(
                                        it.data
                                    )
                                )
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

    }


}