package com.app.plutope.ui.fragment.transfer

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentTransferBinding
import com.app.plutope.model.TransactionDetail
import com.app.plutope.model.Wallet
import com.app.plutope.networkConfig.Chain
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.ui.fragment.token.TokenViewModel
import com.app.plutope.ui.fragment.transactions.buy.buy_detail.TransferHistoryModel
import com.app.plutope.utils.constant.isFromTransactionDetail
import com.app.plutope.utils.convertAmountToCurrency
import com.app.plutope.utils.customSnackbar.CustomSnackbar
import com.app.plutope.utils.date_formate.toCal
import com.app.plutope.utils.getDateFromTimeStamp
import com.app.plutope.utils.hideLoader
import com.app.plutope.utils.loge
import com.app.plutope.utils.network.NetworkState
import com.app.plutope.utils.shareUrl
import com.app.plutope.utils.showLoaderAnyHow
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class Transfer : BaseFragment<FragmentTransferBinding, TransferViewModel>() {
    private lateinit var transactionsModel: TransferHistoryModel.Transactions
    private val transferViewModel: TransferViewModel by viewModels()
    private val tokenViewModel: TokenViewModel by viewModels()
    val args: TransferArgs by navArgs()
    override fun getViewModel(): TransferViewModel {
        return transferViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.transferViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_transfer
    }

    override fun setupToolbarText(): String {
        return ""
    }

    override fun setupUI() {

        loge("GroupList", "transaaction=>${args.transaction}")
        loge("t_decimal", "transaaction=>${args.tokenModel.t_decimal}")
        isFromTransactionDetail = true
        transactionsModel = args.transaction

        viewDataBinding!!.imgBack.setOnClickListener {
            findNavController().navigateUp()
        }

        viewDataBinding?.txtMoreDetails?.setOnClickListener {
            val url = getUrlDetail()
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }
        viewDataBinding?.imgNotification?.setOnClickListener {
            shareUrl(requireContext(), getUrlDetail())
        }

        setDetail(transaction = transactionsModel)
    }

    private fun getUrlDetail(): String {
        val urlToOpen: String = when (args.tokenModel.chain) {
            Chain.BinanceSmartChain -> "https://bscscan.com/tx/${transactionsModel.hash}"
            Chain.Ethereum -> "https://etherscan.io/tx/${transactionsModel.hash}"
            Chain.OKC -> "https://www.okx.com/explorer/oktc/tx/${transactionsModel.hash}"
            Chain.Polygon -> "https://polygonscan.com/tx/${transactionsModel.hash}"
            Chain.Bitcoin -> "https://btcscan.org/tx/${transactionsModel.hash}"
            Chain.Optimism -> "https://optimistic.etherscan.io/tx/${transactionsModel.hash}"
            Chain.Arbitrum -> "https://arbiscan.io/tx/${transactionsModel.hash}"
            Chain.Avalanche -> "https://subnets.avax.network/c-chain/block/${transactionsModel.hash}"
            else -> ""
        }
        return urlToOpen
    }

    @SuppressLint("SetTextI18n")
    private fun setDetail(transaction: TransferHistoryModel.Transactions) {
        val tokenModel = args.tokenModel

        viewDataBinding?.txtToolbarTitle?.text = transaction.transactionTitle


        if (tokenModel.isCustomTokens) {
            val img = when (tokenModel.t_type.lowercase()) {
                "erc20" -> R.drawable.ic_erc
                "bep20" -> R.drawable.ic_bep
                "polygon" -> R.drawable.ic_polygon
                "kip20" -> R.drawable.ic_kip
                else -> {
                    R.drawable.ic_erc
                }
            }
            Glide.with(requireContext()).load(img).into(viewDataBinding?.imgCoin!!)

        } else {
            Glide.with(requireContext()).load(tokenModel.t_logouri).into(viewDataBinding?.imgCoin!!)
        }


        val textColorStatus =
            if (transaction.isTransactionFailed) viewDataBinding?.txtBalance?.context?.resources?.getColor(
                R.color.red, null
            )
            else viewDataBinding?.txtBalance?.context?.resources?.getColor(
                R.color.green_00A323, null
            )

        viewDataBinding?.txtStatusValue?.setTextColor(textColorStatus!!)
        // viewDataBinding?.txtDateValue?.text = transaction.transactionTime
        viewDataBinding?.txtDateValue?.text =
            getDateFromTimeStamp(transaction.timestamp?.toCal("yyyy-MM-dd'T'HH:mm:ss.SSSX")?.timeInMillis!!)

        viewDataBinding?.txtStatusValue?.text =
            if (transaction.isTransactionFailed) "Failed" else "Completed"
        val convertedGasValue = transaction.transactionFee?.toDouble() ?: 0.0
        val chainList = tokenViewModel.getAllTokensList()
            .filter { it.t_address == "" && it.t_type.lowercase() == args.tokenModel.t_type.lowercase() && it.t_symbol.lowercase() == args.tokenModel.chain?.symbol?.lowercase() }
        var chainPrice = args.tokenModel.t_price.toDoubleOrNull()
        if (chainList.isNotEmpty()) {
            chainPrice = chainList[0].t_price.toDoubleOrNull()
        }
        val gasPrice = if (args.tokenModel.t_address != "") (convertedGasValue * (chainPrice
            ?: 0.0)) / 1 else (convertedGasValue * (args.tokenModel.t_price.toDoubleOrNull()
            ?: 0.0)) / 1

        viewDataBinding?.txtNetworkFeeValue?.text = convertedGasValue.toBigDecimal()
            .toPlainString() + " " + tokenModel.chain?.symbol + " " + "(${preferenceHelper.getSelectedCurrency()?.symbol}${
            String.format(
                "%.2f", gasPrice
            )
        })"

        viewDataBinding?.txtNonceValue?.text = transaction.nonce
        setTokenTransferDetails()
    }


    private fun setTokenTransferDetails() {
        viewDataBinding?.txtToolbarTitle?.text = args.transaction.transactionTitle
        val amount =
            if (args.tokenModel.t_address == "") {
                transactionsModel.priceToShow
            } else {
                transactionsModel.formattedValue + " " + args.tokenModel.t_symbol
            }
        viewDataBinding?.txtRecipientValue?.text = transactionsModel.addressShow
        setAmountLabel(text = amount, color = Color.WHITE)

    }


    private fun setAmountLabel(text: String?, color: Int?) {
        loge("")
        viewDataBinding?.txtBalance?.text = ""
        val textStr = text?.replace("-", "")
        val convertedValue = textStr?.replace(args.tokenModel.t_symbol.toString(), "")?.toDouble()

        val coinPrice = args.tokenModel.t_price

        if (convertedValue != 0.0) {
            viewDataBinding?.txtBalance?.text = text

        } else {
            viewDataBinding?.txtPrice?.text = ""
            viewDataBinding?.txtBalance?.text = "0.00 ${args.tokenModel.t_symbol}"
        }

        val price = convertAmountToCurrency(
            convertedValue?.toBigDecimal() ?: 0.toBigDecimal(),
            args.tokenModel.t_price.toDouble().toBigDecimal()
        )

        val formatedPrice = String.format("%.5f", price)

        viewDataBinding?.txtPrice?.text =
            if (coinPrice.isNotEmpty() == true) preferenceHelper.getSelectedCurrency()?.symbol + "" + formatedPrice else ""


    }


    override fun setupObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                transferViewModel.transactionHistoryOkLinkResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            if (it.data?.isNotEmpty() == true) {
                                // setDetail(it.data[0])
                            }
                            hideLoader()
                        }

                        is NetworkState.Loading -> {
                            requireContext().showLoaderAnyHow()
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
                            hideLoader()

                        }
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setTokenTransferSwapDetails(transaction: TransferHistoryModel.Transactions) {

        viewDataBinding?.txtToolbarTitle?.text = transaction.transactionTitle
        viewDataBinding?.txtDateValue?.text =
            getDateFromTimeStamp(transaction.timestamp?.toCal("yyyy-MM-dd'T'HH:mm:ss.SSSX")?.timeInMillis!!)

        viewDataBinding?.txtStatusValue?.text =
            if (transaction.isTransactionFailed) "Failed" else "Completed"
        val convertedGasValue = transaction.transactionFee?.toDouble() ?: 0.0
        val chainList = tokenViewModel.getAllTokensList()
            .filter { it.t_address == "" && it.t_type.lowercase() == args.tokenModel.t_type.lowercase() && it.t_symbol.lowercase() == args.tokenModel.chain?.symbol?.lowercase() }
        var chainPrice = args.tokenModel.t_price.toDoubleOrNull()
        if (chainList.isNotEmpty()) {
            chainPrice = chainList[0].t_price.toDoubleOrNull()
        }
        val gasPrice = if (args.tokenModel.t_address != "") (convertedGasValue * (chainPrice
            ?: 0.0)) / 1 else (convertedGasValue * (args.tokenModel.t_price.toDoubleOrNull()
            ?: 0.0)) / 1

        viewDataBinding?.txtNetworkFeeValue?.text = convertedGasValue.toBigDecimal()
            .toPlainString() + " " + args.tokenModel.chain?.symbol + " " + "(${preferenceHelper.getSelectedCurrency()?.symbol}${
            String.format(
                "%.2f", gasPrice
            )
        })"

        viewDataBinding?.txtNonceValue?.text = transaction.nonce


        if (transaction.swapTranscation[0].from?.lowercase() == Wallet.getPublicWalletAddress(
                args.tokenModel.chain?.coinType!!
            )?.lowercase()
        ) {
            viewDataBinding?.txtRecipientValue?.text = transaction.swapTranscation[0].to
            setAmountLabel(
                "-" + transaction.swapTranscation[0].value + " " + args.tokenModel.t_symbol,
                viewDataBinding?.txtBalance?.context?.resources?.getColor(R.color.red, null)
            )
        } else {
            viewDataBinding?.txtRecipientValue?.text = transaction.swapTranscation[0].from
            setAmountLabel(
                "+" + transaction.swapTranscation[0].value + " " + args.tokenModel.t_symbol,
                viewDataBinding?.txtBalance?.context?.resources?.getColor(
                    R.color.green_00A323, null
                )
            )
        }

        viewDataBinding?.groupSwap?.visibility = GONE
        viewDataBinding?.constraintSwap?.visibility = VISIBLE


        viewDataBinding?.txtRecipientValue?.text = args.transaction.addressShow


        if (transaction.swapTranscation.size > 1) {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val contractAddressList = listOf(
                        transaction.swapTranscation[0].contract,
                        transaction.swapTranscation[transaction.swapTranscation.size - 1].contract
                    )
                    val tokenListTransaction =
                        tokenViewModel.getTokenListByContractAddress(contractAddressList)


                    if (tokenListTransaction.size > 1) {
                        requireActivity().runOnUiThread {
                            tokenListTransaction.forEach {

                                if (it.t_address == transaction.swapTranscation[0].contract) {

                                    Glide.with(requireContext()).load(it.t_logouri)
                                        .into(viewDataBinding?.imgFromSwap!!)
                                    viewDataBinding?.txtFromBalance?.text =
                                        transaction.swapTranscation/*.distinctBy { it.tokenContractAddress }*/[0].value + " " + transaction.swapTranscation.distinctBy { it.contract }[0].foundToken?.symbol
                                    viewDataBinding?.txtFromType?.text = it.t_type
                                } else {
                                    Glide.with(requireContext()).load(it.t_logouri)
                                        .into(viewDataBinding?.imgToSwap!!)
                                    viewDataBinding?.txtToBalance?.text =
                                        transaction.swapTranscation/*.distinctBy { it.tokenContractAddress }*/[1].value + " " + transaction.swapTranscation.distinctBy { it.contract }[1].foundToken?.symbol
                                    viewDataBinding?.txtToType?.text = it.t_type
                                }
                            }


                        }

                    }
                }
            }

        }

    }

    private fun setSwapAmountDetail(firstTransaction: TransactionDetail) {

        viewDataBinding?.groupSwap?.visibility = GONE
        viewDataBinding?.constraintSwap?.visibility = VISIBLE

        if (args.tokenModel.t_address == "" && args.transaction!!.value!!.toDouble() <= 0.0) {
            viewDataBinding?.txtToolbarTitle?.text =
                context?.getString(R.string.smart_contract_call)
        } else {
            viewDataBinding?.txtToolbarTitle?.text = context?.getString(R.string.swap)
        }

        // val payTokenTransferDetails = firstTransaction.tokenTransferDetails.firstOrNull()

        val payTokenTransferDetails = firstTransaction.tokenTransferDetails.firstOrNull {
            it.from.lowercase() == Wallet.getPublicWalletAddress(
                args.tokenModel.chain?.coinType!!
            )?.lowercase()
        }

        // val getTokenTransferDetails = if (firstTransaction.tokenTransferDetails.distinctBy { it.tokenContractAddress }.size > 1) firstTransaction.tokenTransferDetails.distinctBy { it.tokenContractAddress }[1] else firstTransaction.tokenTransferDetails.distinctBy { it.tokenContractAddress }[0]
        val getTokenTransferDetails = firstTransaction.tokenTransferDetails.firstOrNull {
            it.to.lowercase() == Wallet.getPublicWalletAddress(
                args.tokenModel.chain?.coinType!!
            )?.lowercase()
        }


        if (payTokenTransferDetails != null && getTokenTransferDetails != null) {
            viewDataBinding?.txtFromBalance?.text =
                "${payTokenTransferDetails.amount} ${payTokenTransferDetails.symbol}"
            viewDataBinding?.txtToBalance?.text =
                "${getTokenTransferDetails.amount} ${getTokenTransferDetails.symbol}"

            val contractAddressList =
                listOf(firstTransaction.tokenTransferDetails[0].tokenContractAddress,
                    if (firstTransaction.tokenTransferDetails.distinctBy { it.tokenContractAddress }.size > 1) firstTransaction.tokenTransferDetails.distinctBy { it.tokenContractAddress }[1].tokenContractAddress else firstTransaction.tokenTransferDetails.distinctBy { it.tokenContractAddress }[0].tokenContractAddress
                )

            val allToken = tokenViewModel.getTokenListByContractAddress(contractAddressList)

            if (allToken != null) {
                val payToken =
                    allToken.firstOrNull { it.t_address.lowercase() == payTokenTransferDetails.tokenContractAddress.lowercase() }
                if (payToken != null) {
                    viewDataBinding?.imgFromSwap!!.loadImage(payToken.t_logouri)
                    viewDataBinding?.txtFromType?.text = payToken.t_type
                }


                val getToken = allToken.firstOrNull {

                    it.t_address.lowercase() == getTokenTransferDetails.tokenContractAddress.lowercase()
                }

                if (getToken != null) {
                    viewDataBinding?.imgToSwap!!.loadImage(getToken.t_logouri)
                    viewDataBinding?.txtToType?.text = getToken.t_type
                }
            }
        }

        viewDataBinding?.txtRecipientValue?.text = args.transaction.addressShow
    }

    private fun ImageView.loadImage(url: String?) {
        Glide.with(this).load(url).into(this)
    }

    // Extension function to load an image into an ImageView


}