package com.app.plutope.ui.fragment.transactions.swap.previewSwap

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import androidx.core.os.postDelayed
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentPreviewSwapBinding
import com.app.plutope.dialogs.SwapProgressDialog
import com.app.plutope.model.CoinCode
import com.app.plutope.model.ExchangeRequestModel
import com.app.plutope.model.SwapExchangeStatus
import com.app.plutope.model.Wallet
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.ui.fragment.providers.ProviderModel
import com.app.plutope.ui.fragment.token.TokenViewModel
import com.app.plutope.ui.fragment.transactions.swap.SwapViewModel
import com.app.plutope.utils.Securities
import com.app.plutope.utils.coinTypeEnum.CoinType
import com.app.plutope.utils.common.OnBackPressedListener
import com.app.plutope.utils.constant.DEFAULT_CHAIN_ADDRESS
import com.app.plutope.utils.constant.EXCHANGE_API
import com.app.plutope.utils.constant.EXCHANGE_STATUS_API
import com.app.plutope.utils.convertWeiToEther
import com.app.plutope.utils.customSnackbar.CustomSnackbar
import com.app.plutope.utils.enableDisableButton
import com.app.plutope.utils.extras.setSafeOnClickListener
import com.app.plutope.utils.getNetworkForRangoExchange
import com.app.plutope.utils.getNetworkString
import com.app.plutope.utils.hideLoader
import com.app.plutope.utils.loge
import com.app.plutope.utils.network.NetworkState
import com.app.plutope.utils.safeNavigate
import com.app.plutope.utils.showLoader
import com.app.plutope.utils.showLoaderAnyHow
import com.app.plutope.utils.showToast
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.web3j.utils.Convert
import java.math.BigDecimal
import java.math.BigInteger

@AndroidEntryPoint
class PreviewSwapFragment : BaseFragment<FragmentPreviewSwapBinding,PreviewSwapViewModel>(),
    OnBackPressedListener {
    private var URL_BY_ID: String = ""
    private val previewSwapViewModel: PreviewSwapViewModel by viewModels()
    val args : PreviewSwapFragmentArgs by navArgs()
    private val swapViewModel: SwapViewModel by viewModels()
    private val tokenViewModel: TokenViewModel by viewModels()
    private var dexCotractAddress: String? = ""


    companion object{
        const val KEY_BUNDLE_PREVIEWSWAP:String="key_bundle_previewswap"
        const val KEY_PREVIEW_SWAP:String="key_preview_swap"
    }

    override fun getViewModel(): PreviewSwapViewModel {
        return previewSwapViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.transferViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_preview_swap
    }


    override fun setupToolbarText(): String {
        return ""
    }

    override fun setupUI() {
        if (args.providerModel.coinCode.name == CoinCode.RANGO.name) {
           // viewDataBinding?.btnSwap?.enableDisableButton(false)
            requireContext().showLoaderAnyHow()
            rangoExchangeSubmit(false)
        }
        setDetail()
        setOnClickListner()
    }

    private fun setOnClickListner() {
        viewDataBinding?.apply {
            imgBack.setSafeOnClickListener {
                val bundle = Bundle()
                bundle.putBoolean(KEY_PREVIEW_SWAP, true)
                setFragmentResult(KEY_BUNDLE_PREVIEWSWAP, bundle)

                findNavController().navigateUp()
            }

            btnSwap.setSafeOnClickListener {
                viewDataBinding?.btnSwap?.enableDisableButton(false)
                requireContext().showLoader()
                swapApiExicution(args.providerModel)

            }
        }

    }

    private fun swapApiExicution(it: ProviderModel) {
        when (it.coinCode.name) {
            CoinCode.CHANGENOW.name -> {
                executeExchangeCall()
            }

            CoinCode.OKX.name -> {
                if (args.previewSwapDetail.payObject.t_address == "") {
                    //Change now
                    CoroutineScope(Dispatchers.IO).launch {
                        var decimal: Int? = 18
                        args.previewSwapDetail.payObject.callFunction.getDecimal {
                            decimal = it
                            swapViewModel.executeSwapOkx(
                                args.previewSwapDetail.payAmount,
                                args.previewSwapDetail.payObject.chain?.chainIdHex.toString(),
                                args.previewSwapDetail.getObject.t_address.toString()
                                    .ifEmpty { DEFAULT_CHAIN_ADDRESS },
                                args.previewSwapDetail.payObject.t_address.toString()
                                    .ifEmpty { DEFAULT_CHAIN_ADDRESS },
                                Wallet.getPublicWalletAddress(args.previewSwapDetail.payObject.chain?.coinType!!)
                                    .toString(), decimal
                            )
                        }
                    }
                } else {

                    approveOkxApiCall()


                }

            }

            CoinCode.RANGO.name -> {
                rangoExchangeSubmit(true)
            }


        }


    }

    private fun rangoExchangeSubmit(isFromButtonCliked: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            args.previewSwapDetail.payObject.callFunction.getDecimal {

                val fromNetwork =
                    getNetworkForRangoExchange(args.previewSwapDetail.payObject.chain)
                val toNetwork =
                    getNetworkForRangoExchange(args.previewSwapDetail.getObject.chain)

                swapViewModel.executeRangoSubmitCall(
                    fromBlockchain = fromNetwork,
                    fromTokenSymbol = args.previewSwapDetail.payObject.t_symbol.toString(),
                    fromTokenAddress = args.previewSwapDetail.payObject.t_address.toString()
                        .ifEmpty { DEFAULT_CHAIN_ADDRESS },
                    toBlockchain = toNetwork,
                    toTokenSymbol = args.previewSwapDetail.getObject.t_symbol.toString()
                        .lowercase(),
                    toTokenAddress = args.previewSwapDetail.getObject.t_address.toString()
                        .ifEmpty { DEFAULT_CHAIN_ADDRESS },
                    walletAddress = Wallet.getPublicWalletAddress(args.previewSwapDetail.payObject.chain?.coinType!!)
                        .toString(),
                    args.previewSwapDetail.payAmount,
                    decimal = it,
                    fromWalletAddress = Wallet.getPublicWalletAddress(args.previewSwapDetail.payObject.chain?.coinType!!)!!,
                    toWalletAddress = Wallet.getPublicWalletAddress(args.previewSwapDetail.getObject.chain?.coinType!!)!!,
                    isFromButtonCliked = isFromButtonCliked

                )
            }
        }

    }

    private fun approveOkxApiCall() {
        CoroutineScope(Dispatchers.IO).launch {
            var decimal: Int? = 18
            args.previewSwapDetail.payObject.callFunction.getDecimal {
                decimal = it
                swapViewModel.executeApproveOkx(
                    args.previewSwapDetail.payAmount,
                    args.previewSwapDetail.payObject.chain?.chainIdHex.toString(),
                    if (args.previewSwapDetail.payObject.t_address?.isEmpty() == true) DEFAULT_CHAIN_ADDRESS else args.previewSwapDetail.payObject.t_address.toString(),
                    decimal
                )
            }


        }

    }

    private fun executeExchangeCall() {
        val fromNetwork = getNetworkString(args.previewSwapDetail.payObject.chain)
        val toNetwork = getNetworkString(args.previewSwapDetail.getObject.chain)

        swapViewModel.executeExchange(
            EXCHANGE_API, ExchangeRequestModel(
                args.previewSwapDetail.payObject.t_symbol.toString().lowercase(),
                args.previewSwapDetail.getObject.t_symbol.toString().lowercase(),
                fromNetwork.toString(),
                toNetwork.toString(),
                args.previewSwapDetail.payAmount.ifEmpty { "0.0" },
                Wallet.getPublicWalletAddress(
                    args.previewSwapDetail.getObject.chain?.coinType ?: CoinType.ETHEREUM
                ).toString()
            )
        )
    }


    @SuppressLint("SetTextI18n")
    private fun setDetail() {
        loge("Preview", "model => ${args.previewSwapDetail}")
        val model = args.previewSwapDetail
        viewDataBinding?.apply {

            if (model.payObject.chain?.coinType == CoinType.BITCOIN) {
                layoutFinanceDetail.visibility = View.GONE
            } else {
                layoutFinanceDetail.visibility = View.VISIBLE
            }


            val payAmount =
                (model.payAmount.toDouble() * (args.previewSwapDetail.payObject.t_price?.toDoubleOrNull()
                    ?: 0.0)) / 1

            Glide.with(requireContext()).load(model.payObject.t_logouri).into(imgFromSwap)
            txtFromBalance.text = model.payAmount + " " + model.payObject.t_symbol
            txtFromType.text =
                model.payObject.t_type + "" + " (${preferenceHelper.getSelectedCurrency()?.symbol}${
                    String.format(
                        "%.2f",
                        payAmount
                    )
                })"

            Glide.with(requireContext()).load(model.getObject.t_logouri).into(imgToSwap)
            txtToBalance.text = model.getAmount
            // txtToType.text = model.getObject.t_type

            val getAmountConverted =
                (args.providerModel.bestPrice.toDouble() * (args.previewSwapDetail.getObject.t_price?.toDoubleOrNull()
                    ?: 0.0)) / 1

            viewDataBinding!!.txtToType.text =
                args.previewSwapDetail.getObject.t_type + " (${preferenceHelper.getSelectedCurrency()?.symbol}${
                    String.format(
                        "%.2f",
                        getAmountConverted
                    )
                })"



            txtFromValue.text =
                Wallet.getPublicWalletAddress(model.payObject.chain?.coinType!!).toString()
            txtQuoteValue.text = model.quote
            val fee: BigInteger?
            var convertedGasValue: String? = ""


            if (model.routerResult.toString() != "null" || model.routerResult != null) {
                fee =
                    model.routerResult?.tx?.gas!!.toBigInteger() * model.routerResult?.tx?.gasPrice?.toBigInteger()!!
                convertedGasValue =
                    convertWeiToEther(fee.toString(), model.payObject.chain!!.decimals)
                val chainList = tokenViewModel.getAllTokensList()
                    .filter { it.t_address == "" && it.t_type?.lowercase() == args.previewSwapDetail.payObject.t_type?.lowercase() && it.t_symbol?.lowercase() == args.previewSwapDetail.payObject.chain?.symbol?.lowercase() }
                var chainPrice = args.previewSwapDetail.payObject.t_price?.toDoubleOrNull()
                if (chainList.isNotEmpty()) {
                    chainPrice = chainList[0].t_price?.toDoubleOrNull()
                }
                val gasPrice =
                    if (args.previewSwapDetail.payObject.t_address != "") (convertedGasValue.toDouble() * (chainPrice
                        ?: 0.0)) / 1 else (convertedGasValue.toDouble() * (args.previewSwapDetail.payObject.t_price?.toDoubleOrNull()
                        ?: 0.0)) / 1

                txtNetworkFeeValue.text =
                    convertedGasValue + " " + model.payObject.chain?.symbol + "\n(${preferenceHelper.getSelectedCurrency()?.symbol}${
                        String.format(
                            "%.2f",
                            gasPrice
                        )
                    })"

                val swapperFeesConverted =
                    if (args.previewSwapDetail.payObject.t_address != "") (args.providerModel.swapperFees.toDouble() * (chainPrice
                        ?: 0.0)) / 1 else (args.providerModel.swapperFees.toDouble() * (args.previewSwapDetail.payObject.t_price?.toDoubleOrNull()
                        ?: 0.0)) / 1
                txtSwapperFeeValue.text =
                    args.providerModel.swapperFees + " " + model.payObject.chain?.symbol + "(${preferenceHelper.getSelectedCurrency()?.symbol}${
                        String.format(
                            "%.2f",
                            swapperFeesConverted
                        )
                    })"

            } else {
                loge("Preview", "here1")

                lifecycleScope.launch(Dispatchers.IO) {
                    model.payObject.callFunction.getGasFee { it, _, _, _ ->
                        previewSwapViewModel?.gasFee?.value = it
                        val convertedGasValue2 = convertWeiToEther(
                            it.toString(), model.payObject.chain!!.decimals
                        )

                        val chainList = tokenViewModel.getAllTokensList()
                            .filter { it.t_address == "" && it.t_type?.lowercase() == args.previewSwapDetail.payObject.t_type?.lowercase() && it.t_symbol?.lowercase() == args.previewSwapDetail.payObject.chain?.symbol?.lowercase() }
                        var chainPrice = args.previewSwapDetail.payObject.t_price?.toDoubleOrNull()
                        if (chainList.isNotEmpty()) {
                            chainPrice = chainList[0].t_price?.toDoubleOrNull()
                        }
                        val gasPrice =
                            if (args.previewSwapDetail.payObject.t_address != "") (convertedGasValue2.toDouble() * (chainPrice
                                ?: 0.0)) / 1 else (convertedGasValue2.toDouble() * (args.previewSwapDetail.payObject.t_price?.toDoubleOrNull()
                                ?: 0.0)) / 1
                        val swapperFeesConverted =
                            if (args.previewSwapDetail.payObject.t_address != "") (args.providerModel.swapperFees.toDouble() * (chainPrice
                                ?: 0.0)) / 1 else (args.providerModel.swapperFees.toDouble() * (args.previewSwapDetail.payObject.t_price?.toDoubleOrNull()
                                ?: 0.0)) / 1

                        requireActivity().runOnUiThread {
                            val formatedValue = String.format("%.7f", convertedGasValue2.toDouble())
                            viewDataBinding?.apply {
                                txtNetworkFeeValue.text =
                                    formatedValue + " " + model.payObject.chain?.symbol + "(${preferenceHelper.getSelectedCurrency()?.symbol}${
                                        String.format(
                                            "%.2f",
                                            gasPrice
                                        )
                                    })"

                                if (args.providerModel.swapperFees != "0.0") {
                                    viewDataBinding!!.txtSwapperFeeTitle.visibility =
                                        View.VISIBLE
                                    viewDataBinding!!.txtSwapperFeeValue.visibility =
                                        View.VISIBLE
                                } else {
                                    viewDataBinding!!.txtSwapperFeeTitle.visibility =
                                        View.GONE
                                    viewDataBinding!!.txtSwapperFeeValue.visibility =
                                        View.GONE
                                }

                                txtSwapperFeeValue.text =
                                    args.providerModel.swapperFees + " " + model.payObject.chain?.symbol + "(${preferenceHelper.getSelectedCurrency()?.symbol}${
                                        String.format(
                                            "%.2f", swapperFeesConverted
                                        )
                                    })"
                                                    }


                                                }


                    }


                }


            }


        }
    }

    override fun setupObserver() {

        GlobalScope.launch(Dispatchers.IO) {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                swapViewModel.setWalletActive.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            // hideLoader()

                        }

                        is NetworkState.Loading -> {

                        }

                        is NetworkState.Error -> {
                            // hideLoader()
                        }

                        is NetworkState.SessionOut -> {}

                        else -> {
                            //  hideLoader()
                        }
                    }
                }
            }
        }


        lifecycleScope.launch {
            swapViewModel.executeSwapUsingOkxResponse.collect {
                when (it) {
                    is NetworkState.Success -> {
                        if (it.data?.data1?.isNotEmpty() == true) {
                            val response = it.data.data1[0].tx

                            val amountSend: BigInteger =
                                Convert.toWei(
                                    args.previewSwapDetail.payAmount.toString(),
                                    Convert.Unit.ETHER
                                ).toBigInteger()


                            args.previewSwapDetail.payObject.callFunction.swapTokenOrCoinOkx(
                                response.to,
                                response.data,
                                response.gasPrice,
                                response.gas,
                                amountSend,
                                dexCotractAddress, { success, _, _ ->
                                    if (success) {
                                        /*
                                       if (!PreferenceHelper.getInstance().isActiveWallet) {
                                           swapViewModel.setWalletActiveCall(
                                               Wallet.getPublicWalletAddress(
                                                   CoinType.ETHEREUM
                                               )!!,response.to
                                           )
                                       }
                                        */

                                        swapViewModel.setWalletActiveCall(
                                            Wallet.getPublicWalletAddress(
                                                CoinType.ETHEREUM
                                            )!!, response.to
                                        )


                                        SwapProgressDialog.getInstance().dismiss()
                                        requireActivity().runOnUiThread {
                                            findNavController().safeNavigate(
                                                PreviewSwapFragmentDirections.actionPreviewSwapFragmentToDashboard()
                                            )
                                        }

                                    } else {
                                        SwapProgressDialog.getInstance().dismiss()
                                        if (isAdded) {
                                            requireActivity().runOnUiThread {
                                                hideLoader()
                                                // requireContext().showToast(errorMessage.toString())
                                                findNavController().safeNavigate(
                                                    PreviewSwapFragmentDirections.actionGlobalToDashboard()
                                                )

                                            }
                                        }
                                    }

                                }, { success, _, _ ->
                                    if (success) {
                                        if (isAdded) {
                                            requireActivity().runOnUiThread {
                                                requireContext().showToast("Success")
                                            }
                                        }

                                    } else {
                                        if (isAdded) {
                                            requireActivity().runOnUiThread {
                                                hideLoader()
                                            }
                                        }
                                    }
                                })

                        }

                    }

                    is NetworkState.Loading -> {
                        if (isAdded) {
                            requireActivity().runOnUiThread {
                                openSwapProgressDialog(
                                    "Processing....",
                                    "It might take a few minutes."
                                )

                            }
                        }

                    }

                    is NetworkState.Error -> {
                        hideLoader()
                        requireContext().showToast(it.message.toString())
                    }

                    is NetworkState.SessionOut -> {
                        hideLoader()
                        CustomSnackbar.make(requireActivity().window.decorView.rootView as ViewGroup, it.message.toString())
                            .show()
                    }

                    else -> {
                        hideLoader()
                    }
                }
            }
        }

        lifecycleScope.launch {
            swapViewModel.executeApproveUsingOkxResponse.collect {
                when (it) {
                    is NetworkState.Success -> {
                        hideLoader()
                        if (it.data?.data1?.isNotEmpty() == true) {

                            requireActivity().runOnUiThread {
                                openSwapProgressDialog(
                                    "Processing....",
                                    "It might take a few minutes."
                                )

                            }


                            dexCotractAddress = it.data.data1[0].dexContractAddress

                            CoroutineScope(Dispatchers.IO).launch {
                                var decimal: Int? = 18
                                args.previewSwapDetail.payObject.callFunction.getDecimal {
                                    decimal = it
                                    swapViewModel.executeSwapOkx(
                                        args.previewSwapDetail.payAmount,
                                        args.previewSwapDetail.payObject.chain?.chainIdHex.toString(),
                                        args.previewSwapDetail.getObject.t_address.toString()
                                            .ifEmpty { DEFAULT_CHAIN_ADDRESS },
                                        args.previewSwapDetail.payObject.t_address.toString()
                                            .ifEmpty { DEFAULT_CHAIN_ADDRESS },
                                        Wallet.getPublicWalletAddress(args.previewSwapDetail.payObject.chain?.coinType!!)
                                            .toString(), decimal
                                    )
                                }
                            }


                        }


                    }

                    is NetworkState.Loading -> {
                        requireContext().showLoader()
                    }

                    is NetworkState.Error -> {
                        hideLoader()
                        requireContext().showToast(it.message.toString())

                    }

                    is NetworkState.SessionOut -> {
                        hideLoader()
                        CustomSnackbar.make(
                            requireActivity().window.decorView.rootView as ViewGroup,
                            it.message.toString()
                        )
                            .show()
                    }

                    else -> {
                        hideLoader()
                    }
                }
            }
        }


        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                swapViewModel.executeExchangeResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            val response = it.data
                            // viewDataBinding?.edtYouGet?.setText(String.format("%.7f", response?.toAmount) + " ${youGetObj.t_symbol}")

                            val payingAddress = response?.payinAddress
                            val transactionID = response?.id

                            val tokenList = tokenViewModel.getAllTokensList()
                            URL_BY_ID = "$EXCHANGE_STATUS_API?id=$transactionID"

                            if (args.previewSwapDetail.payObject.chain?.coinType == CoinType.BITCOIN) {

                                swapViewModel.sendBTCTransactionCall(
                                    Securities.encrypt(Wallet.getPrivateKeyData(CoinType.BITCOIN)) /*"XV53A5ytqeVMfbN7cXNKYn4YGWZJWbnFbPswI0xoJXKCuPlzmW74SXwAI+jPi3fUt3sSyTnHMj7bQf7zSSSIRw=="*/,
                                    args.previewSwapDetail.payAmount,
                                    payingAddress!! /*"mnfE6ySXEuaA3bJRTikUk9j454T3cmSKvz"*/,
                                    "mainnet",
                                    /*"mnfE6ySXEuaA3bJRTikUk9j454T3cmSKvz"*/
                                    Wallet.getPublicWalletAddress(CoinType.BITCOIN)!!
                                )

                            } else {
                                args.previewSwapDetail.payObject.callFunction.sendTokenOrCoin(
                                    payingAddress,
                                    /*viewDataBinding?.edtYouPay?.text.toString().toDouble()*/
                                    args.previewSwapDetail.payAmount.toDouble(),
                                    tokenList
                                ) { success, errorMessage, _ ->
                                    if (success) {
                                        //URL_BY_ID = "$EXCHANGE_STATUS_API?id=$transactionID"
                                        requireActivity().runOnUiThread {
                                            Handler(Looper.getMainLooper()).postDelayed(5000) {
                                                hideLoader()
                                                openSwapProgressDialog(
                                                    "Processing....",
                                                    "It might take a few minutes."
                                                )
                                            }
                                        }

                                        swapViewModel.executeExchangeStatus(URL_BY_ID)

                                    } else {
                                        if (isResumed) {
                                            requireActivity().runOnUiThread {
                                                hideLoader()
                                                viewDataBinding?.btnSwap?.enableDisableButton(true)
                                                requireContext().showToast(errorMessage.toString())
                                            }
                                        }
                                    }

                                }
                            }
                        }

                        is NetworkState.Loading -> {
                            requireContext().showLoader()
                        }

                        is NetworkState.Error -> {
                            viewDataBinding?.btnSwap?.enableDisableButton(true)
                            requireContext().showToast(it.message.toString())
                            hideLoader()
                        }

                        is NetworkState.SessionOut -> {
                            hideLoader()
                            CustomSnackbar.make(
                                requireActivity().window.decorView.rootView as ViewGroup,
                                it.message.toString()
                            ).show()
                        }

                        else -> {
                            // hideLoader()
                        }
                    }
                }
            }
        }


        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                swapViewModel.executeExchangeStatusResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            when {
                                it.data?.status?.lowercase() == SwapExchangeStatus.finished.name.lowercase() -> {
                                    requireActivity().runOnUiThread {

                                        swapViewModel.setWalletActiveCall(
                                            Wallet.getPublicWalletAddress(
                                                CoinType.ETHEREUM
                                            )!!, ""
                                        )




                                        hideLoader()
                                        viewDataBinding?.btnSwap?.enableDisableButton(true)
                                        SwapProgressDialog.getInstance().dismiss()

                                        /* setProgress(
                                             viewDataBinding!!.root,
                                             3,
                                             (requireActivity() as BaseActivity)
                                         )*/
                                        findNavController().safeNavigate(
                                            PreviewSwapFragmentDirections.actionPreviewSwapFragmentToDashboard()
                                        )

                                    }
                                }

                                it.data?.status?.lowercase() == SwapExchangeStatus.failed.name -> {
                                    viewDataBinding?.btnSwap?.enableDisableButton(true)
                                    requireContext().showToast("Failed Transaction")
                                    hideLoader()
                                    return@collect
                                }

                                else -> {
                                    if (it.data?.status?.lowercase() == SwapExchangeStatus.confirming.name || it.data?.status?.lowercase() == SwapExchangeStatus.exchanging.name) {
                                        /* setProgress(
                                             viewDataBinding!!.root,
                                             2,
                                             (requireActivity() as BaseActivity)
                                         )*/
                                    } else if (it.data?.status?.lowercase() == SwapExchangeStatus.waiting.name) {
                                        /*  setProgress(
                                              viewDataBinding!!.root,
                                              1,
                                              (requireActivity() as BaseActivity)
                                          )*/
                                    }

                                    CoroutineScope(Dispatchers.IO).launch {
                                        delay(2000)
                                        swapViewModel.executeExchangeStatus(URL_BY_ID)
                                    }

                                }
                            }
                        }

                        is NetworkState.Loading -> {
                            // requireContext().showLoader()
                        }

                        is NetworkState.Error -> {
                            hideLoader()
                        }

                        is NetworkState.SessionOut -> {
                            hideLoader()
                            CustomSnackbar.make(
                                requireActivity().window.decorView.rootView as ViewGroup,
                                it.message.toString()
                            ).show()
                        }

                        else -> {
                            //  hideLoader()
                        }
                    }
                }
            }
        }


        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                swapViewModel.responseRengoSwapSubmitResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {

                            if (it.data!!.isFromButtonCliked) {
                                if (it.data?.tx != null) {
                                    if (it.data.tx?.approveData != null) {

                                        openSwapProgressDialog(
                                            "Processing....",
                                            "It might take a few minutes."
                                        )
                                        val txValue =
                                            if (it.data.tx?.value == "null" || it.data.tx?.value == null) "0" else it.data.tx?.value!!

                                        CoroutineScope(Dispatchers.IO).launch {
                                            args.previewSwapDetail.payObject.callFunction.signAndSendTranscation(
                                                toAddress = it.data.tx?.approveTo,
                                                gasLimit = it.data.tx?.gasLimit!!,
                                                gasPrice = if (it.data.tx?.gasPrice == "null" || it.data.tx?.gasPrice == null) "0" else it.data.tx?.gasPrice!!,
                                                data = it.data.tx?.approveData!!,
                                                value = txValue

                                            ) { success, errorMessage, _ ->
                                                if (success) {
                                                    requireActivity().runOnUiThread {
                                                        //  SwapProgressDialog.getInstance().dismiss()
                                                        requireContext().showToast("Approved success. Swapping tokens.. ")

                                                        CoroutineScope(Dispatchers.IO).launch {
                                                            args.previewSwapDetail.payObject.callFunction.signAndSendTranscation(
                                                                toAddress = it.data.tx?.txTo,
                                                                gasLimit = it.data.tx?.gasLimit!!,
                                                                gasPrice = if (it.data.tx?.gasPrice == "null" || it.data.tx?.gasPrice == null) "0" else it.data.tx?.gasPrice!!,
                                                                data = it.data.tx?.txData!!,
                                                                value = txValue

                                                            ) { success, errorMessage, _ ->
                                                                if (success) {
                                                                    requireActivity().runOnUiThread {
                                                                        viewDataBinding?.btnSwap?.enableDisableButton(
                                                                            true
                                                                        )

                                                                        swapViewModel.setWalletActiveCall(
                                                                            Wallet.getPublicWalletAddress(
                                                                                CoinType.ETHEREUM
                                                                            )!!,
                                                                            it.data.tx?.txTo!!
                                                                        )


                                                                        SwapProgressDialog.getInstance()
                                                                            .dismiss()
                                                                        requireContext().showToast(
                                                                            "Success"
                                                                        )
                                                                        findNavController().safeNavigate(
                                                                            PreviewSwapFragmentDirections.actionPreviewSwapFragmentToDashboard()
                                                                        )

                                                                    }

                                                                } else {
                                                                    requireActivity().runOnUiThread {
                                                                        viewDataBinding?.btnSwap?.enableDisableButton(
                                                                            true
                                                                        )
                                                                        SwapProgressDialog.getInstance()
                                                                            .dismiss()
                                                                        hideLoader()
                                                                        requireContext().showToast(
                                                                            "$errorMessage"
                                                                        )
                                                                    }
                                                                }

                                                            }
                                                        }


                                                    }

                                                } else {
                                                    requireActivity().runOnUiThread {
                                                        viewDataBinding?.btnSwap?.enableDisableButton(
                                                            true
                                                        )
                                                        SwapProgressDialog.getInstance()
                                                            .dismiss()
                                                        hideLoader()
                                                        requireContext().showToast("$errorMessage")
                                                    }
                                                }


                                            }
                                        }

                                    } else {

                                        openSwapProgressDialog(
                                            "Processing....",
                                            "It might take a few minutes."
                                        )
                                        val txValue =
                                            if (it.data.tx?.value == "null" || it.data.tx?.value == null) "0" else it.data.tx?.value!!

                                        CoroutineScope(Dispatchers.IO).launch {
                                            args.previewSwapDetail.payObject.callFunction.signAndSendTranscation(
                                                toAddress = it.data.tx?.txTo,
                                                gasLimit = it.data.tx?.gasLimit,
                                                gasPrice = if (it.data.tx?.gasPrice == "null" || it.data.tx?.gasPrice == null) "0" else it.data.tx?.gasPrice!!,
                                                data = it.data.tx?.txData,
                                                value = txValue

                                            ) { success, errorMessage, _ ->
                                                if (success) {
                                                    requireActivity().runOnUiThread {
                                                        /*if (!PreferenceHelper.getInstance().isActiveWallet) {
                                                    swapViewModel.setWalletActiveCall(
                                                        Wallet.getPublicWalletAddress(
                                                            CoinType.ETHEREUM
                                                        )!!,it.data.tx?.txTo!!
                                                    )
                                                }*/

                                                        viewDataBinding?.btnSwap?.enableDisableButton(
                                                            true
                                                        )

                                                        swapViewModel.setWalletActiveCall(
                                                            Wallet.getPublicWalletAddress(
                                                                CoinType.ETHEREUM
                                                            )!!, it.data.tx?.txTo!!
                                                        )

                                                        SwapProgressDialog.getInstance()
                                                            .dismiss()
                                                        requireContext().showToast("Success")
                                                        findNavController().safeNavigate(
                                                            PreviewSwapFragmentDirections.actionPreviewSwapFragmentToDashboard()
                                                        )

                                                    }

                                                } else {
                                                    requireActivity().runOnUiThread {
                                                        viewDataBinding?.btnSwap?.enableDisableButton(
                                                            true
                                                        )
                                                        SwapProgressDialog.getInstance()
                                                            .dismiss()
                                                        hideLoader()
                                                        requireContext().showToast("$errorMessage")
                                                    }
                                                }


                                            }
                                        }

                                    }
                                } else {
                                    if (it.data!!.isFromButtonCliked) {
                                        requireContext().showToast("" + it.data?.error)
                                    }
                                    viewDataBinding?.btnSwap?.enableDisableButton(true)
                                }


                            } else {

                                val amt = convertWeiToEther(
                                    it.data.route?.outputAmount!!,
                                    it.data.route?.to?.decimals!!
                                )
                                viewDataBinding!!.txtToBalance.text = amt
                                viewDataBinding!!.txtToType.text =
                                    args.previewSwapDetail.getObject.t_type
                                var feesAmount: BigDecimal = 0.toBigDecimal()
                                var networkAmount: BigDecimal = 0.toBigDecimal()
                                it.data.route?.fee?.forEach { swapperFees ->
                                    feesAmount += if (swapperFees?.expenseType == "FROM_SOURCE_WALLET" && swapperFees.name == "Swapper Fee") {
                                        convertWeiToEther(
                                            swapperFees.amount!!,
                                            swapperFees.token?.decimals!!
                                        ).toBigDecimal()
                                    } else 0.toBigDecimal()

                                    networkAmount += if (swapperFees?.expenseType == "FROM_SOURCE_WALLET" && swapperFees.name == "Network Fee") {
                                        convertWeiToEther(
                                            swapperFees.amount!!,
                                            swapperFees.token?.decimals!!
                                        ).toBigDecimal()
                                    } else 0.toBigDecimal()

                                }

                                val chainListPay = tokenViewModel.getAllTokensList()
                                    .filter { it.t_address == "" && it.t_type?.lowercase() == args.previewSwapDetail.payObject.t_type?.lowercase() && it.t_symbol?.lowercase() == args.previewSwapDetail.payObject.chain?.symbol?.lowercase() }
                                val chainListGet = tokenViewModel.getAllTokensList()
                                    .filter { it.t_address == "" && it.t_type?.lowercase() == args.previewSwapDetail.getObject.t_type?.lowercase() && it.t_symbol?.lowercase() == args.previewSwapDetail.getObject.chain?.symbol?.lowercase() }
                                var chainPricePay =
                                    args.previewSwapDetail.payObject.t_price?.toDoubleOrNull()
                                var chainPriceGet =
                                    args.previewSwapDetail.getObject.t_price?.toDoubleOrNull()
                                if (chainListPay.isNotEmpty()) {
                                    chainPricePay = chainListPay[0].t_price?.toDoubleOrNull()
                                }
                                if (chainListGet.isNotEmpty()) {
                                    chainPriceGet = chainListGet[0].t_price?.toDoubleOrNull()
                                }

                                val gasPrice =
                                    if (args.previewSwapDetail.payObject.t_address != "") (networkAmount.toDouble() * (chainPricePay
                                        ?: 0.0)) / 1 else (networkAmount.toDouble() * (args.previewSwapDetail.payObject.t_price?.toDoubleOrNull()
                                        ?: 0.0)) / 1


                                loge(
                                    "Objectc",
                                    "Amount = ${amt.toDouble()}  : getObject ${args.previewSwapDetail.getObject}  ::  payObject : ${args.previewSwapDetail.payObject}"
                                )

                                val getAmountConverted =
                                    /* if (args.previewSwapDetail.getObject.t_address != "") (amt.toDouble() * (chainPriceGet
                                         ?: 0.0)) / 1 else */
                                    (amt.toDouble() * (args.previewSwapDetail.getObject.t_price?.toDoubleOrNull()
                                        ?: 0.0)) / 1


                                viewDataBinding!!.txtToType.text =
                                    args.previewSwapDetail.getObject.t_type + " (${preferenceHelper.getSelectedCurrency()?.symbol}${
                                        String.format(
                                            "%.2f",
                                            getAmountConverted
                                        )
                                    })"


                                loge(
                                    "Preview",
                                    "NetworkAmount => $networkAmount  :: fees =>$feesAmount"
                                )

                                if (networkAmount.toDouble() > 0.0) {
                                    viewDataBinding!!.txtNetworkFeeValue.text =
                                        networkAmount.toString() + " " + args.previewSwapDetail.payObject.chain?.symbol + "(${preferenceHelper.getSelectedCurrency()?.symbol}${
                                            String.format(
                                                "%.2f",
                                                gasPrice
                                            )
                                        })"

                                } else {
                                    viewDataBinding!!.txtNetworkFeeValue.text = "-"
                                }

                                if (feesAmount.toString().trim() != "0") {
                                    loge(
                                        "Preview",
                                        "here i am feesAmount = ${
                                            feesAmount.toString().trim()
                                        }|"
                                    )
                                    viewDataBinding!!.txtSwapperFeeTitle.visibility =
                                        View.VISIBLE
                                    viewDataBinding!!.txtSwapperFeeValue.visibility =
                                        View.VISIBLE

                                    val swapperFeesConverted =
                                        if (args.previewSwapDetail.payObject.t_address != "") (feesAmount.toDouble() * (chainPricePay
                                            ?: 0.0)) / 1 else (feesAmount.toDouble() * (args.previewSwapDetail.payObject.t_price?.toDoubleOrNull()
                                            ?: 0.0)) / 1

                                    val convertedSwapper =
                                        "(${preferenceHelper.getSelectedCurrency()?.symbol}${
                                            String.format(
                                                "%.2f",
                                                swapperFeesConverted
                                            )
                                        })"

                                    loge(
                                        "ConvertedSwapper",
                                        "${swapperFeesConverted} :::  $convertedSwapper"
                                    )

                                    viewDataBinding!!.txtSwapperFeeValue.text =
                                        getString(
                                            R.string.networkfees,
                                            feesAmount,
                                            args.previewSwapDetail.payObject.chain?.symbol,
                                            convertedSwapper
                                        )

                                } else {
                                    loge(
                                        "Preview",
                                        "here i am feesAmount = ${
                                            feesAmount.toString().trim()
                                        }|"
                                    )
                                    viewDataBinding!!.txtSwapperFeeTitle.visibility = View.GONE
                                    viewDataBinding!!.txtSwapperFeeValue.visibility = View.GONE
                                    viewDataBinding!!.txtSwapperFeeValue.text = "-"
                                }

                            }

                            hideLoader()
                        }

                        is NetworkState.Loading -> {
                            requireContext().showLoaderAnyHow()
                        }

                        is NetworkState.Error -> {
                            hideLoader()
                            // requireContext().showToast(it.message.toString())

                        }

                        is NetworkState.SessionOut -> {
                            hideLoader()
                            CustomSnackbar.make(
                                requireActivity().window.decorView.rootView as ViewGroup,
                                it.message.toString()
                            )
                                .show()
                        }

                        else -> {
                            //   hideLoader()
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                swapViewModel.sendBTCTransactionResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {

                            requireActivity().runOnUiThread {
                                Handler(Looper.getMainLooper()).postDelayed(5000) {
                                    hideLoader()
                                    openSwapProgressDialog(
                                        "Processing....",
                                        "It might take a few minutes."
                                    )
                                }
                            }


                            /* if (!PreferenceHelper.getInstance().isActiveWallet) {
                                 swapViewModel.setWalletActiveCall(
                                     Wallet.getPublicWalletAddress(
                                         CoinType.BITCOIN
                                     )!!,""
                                 )
                             }*/

                            swapViewModel.setWalletActiveCall(
                                Wallet.getPublicWalletAddress(
                                    CoinType.BITCOIN
                                )!!, ""
                            )


                            swapViewModel.executeExchangeStatus(URL_BY_ID)

                            /*
                                                        Handler(Looper.getMainLooper()).postDelayed(5000) {
                                                            requireActivity().runOnUiThread {
                                                                SwapProgressDialog.getInstance().dismiss()
                                                                hideLoader()
                                                                requireContext().showToast(it.toString())
                                                            }

                                                            findNavController().safeNavigate(PreviewSwapFragmentDirections.actionPreviewSwapFragmentToDashboard())

                                                        }
                            */

                        }

                        is NetworkState.Loading -> {
                            //  requireContext().showLoaderAnyHow()

                            openSwapProgressDialog(
                                "Processing....",
                                "It might take a few minutes."
                            )

                        }

                        is NetworkState.Error -> {


                            requireActivity().runOnUiThread {
                                SwapProgressDialog.getInstance().dismiss()
                                hideLoader()
                                requireContext().showToast(it.message.toString())
                            }

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

    override fun onBackPressed() {
        val bundle = Bundle()
        bundle.putBoolean(KEY_PREVIEW_SWAP, true)
        setFragmentResult(KEY_BUNDLE_PREVIEWSWAP, bundle)

        findNavController().navigateUp()
    }

    private fun openSwapProgressDialog(title: String, subtitle: String) {
        SwapProgressDialog.getInstance().show(
            requireContext(),
            title,
            subtitle,
            listener = object : SwapProgressDialog.DialogOnClickBtnListner {
                override fun onOkClick() {

                    findNavController().safeNavigate(PreviewSwapFragmentDirections.actionPreviewSwapFragmentToDashboard())
                }
            })
    }


}