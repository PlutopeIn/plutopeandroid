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
import com.app.plutope.utils.coinTypeEnum.CoinType
import com.app.plutope.utils.constant.OK_LINK_TRANSACTION_DETAIL
import com.app.plutope.utils.convertAmountToCurrency
import com.app.plutope.utils.customSnackbar.CustomSnackbar
import com.app.plutope.utils.getDateFromTimeStamp
import com.app.plutope.utils.hideLoader
import com.app.plutope.utils.loge
import com.app.plutope.utils.network.NetworkState
import com.app.plutope.utils.setBalanceText
import com.app.plutope.utils.shareUrl
import com.app.plutope.utils.showLoader
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class Transfer : BaseFragment<FragmentTransferBinding, TransferViewModel>() {
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

        viewDataBinding!!.imgBack.setOnClickListener {
            findNavController().navigateUp()
        }

        viewDataBinding?.txtMoreDetails?.setOnClickListener {
            val url = getUrlDetail()
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }
        viewDataBinding?.imgNotification?.setOnClickListener {
            shareUrl(requireContext(),getUrlDetail())
        }
        transferViewModel.executeGetTransactionHistoryDetailOkLink(OK_LINK_TRANSACTION_DETAIL + "chainShortName=${args.tokenModel.chain?.chainName?.lowercase()}&txid=${args.transaction.txId}")

    }

    private fun getUrlDetail(): String {
        val urlToOpen: String = when (args.tokenModel.chain) {
            Chain.BinanceSmartChain -> "https://bscscan.com/tx/${args.transaction.txId ?: ""}"
            Chain.Ethereum -> "https://etherscan.io/tx/${args.transaction.txId ?: ""}"
            Chain.OKC -> "https://www.okx.com/explorer/oktc/tx/${args.transaction.txId?:""}"
            Chain.Polygon -> "https://polygonscan.com/tx/${args.transaction.txId ?: ""}"
            Chain.Bitcoin -> "https://btcscan.org/tx/${args.transaction.txId ?: ""}"
            else -> ""
        }
        return urlToOpen
    }

    @SuppressLint("SetTextI18n")
    private fun setDetail(transaction: TransactionDetail) {
        val tokenModel = args.tokenModel


        /*
                if(model.isSwap){
                    binding.txtTransactionName.text = "Swap"
                }else {
                    if (model.methodId == "" && model.isToContract) {
                        binding.txtTransactionName.text = "Transfer"

                    } else {
                        if (addressToken == "" && model.amount.toDouble()<=0.0 ) {
                            binding.txtTransactionName.text = "Smart Contract Call"
                            binding.txtPrice.text =
                                "-0.00 ${model.priceToShow?.split(" ")?.lastOrNull()}"

                        } else {
                            binding.txtTransactionName.text = "Transfer"
                        }
                    }
                }
        */



        if (transaction.tokenTransferDetails.isEmpty()) {
            if (tokenModel.t_address != "") {
                setTokenTransferDetails(transaction)
            } else {
                setCoinDetail(transaction)
            }
        } else {
            if (transaction.tokenTransferDetails.size > 1) {
                // setTokenTransferSwapDetails(transaction)
                if (args.transaction.isSwap) {
                    setSwapAmountDetail(transaction)
                } else {
                    setTokenTransferDetails(transaction/*,true*/)
                }

            }else{
                setTokenTransferDetails(transaction/*,true*/)
            }
        }

        val textColorStatus =
            if (transaction.state == "success") viewDataBinding?.txtBalance?.context?.resources?.getColor(
                R.color.green_099817,
                null
            ) else viewDataBinding?.txtBalance?.context?.resources?.getColor(R.color.red, null)

        viewDataBinding?.txtStatusValue?.setTextColor(textColorStatus!!)

        viewDataBinding?.txtDateValue?.text =
            getDateFromTimeStamp(transaction.transactionTime.toLong())


        viewDataBinding?.txtStatusValue?.text =
            if (transaction.state == "success") "Completed" else "Failed"
        viewDataBinding?.txtNonceValue?.text = transaction.nonce
        val convertedGasValue = transaction.txfee ?: ""
        val chainList = tokenViewModel.getAllTokensList()
            .filter { it.t_address == "" && it.t_type?.lowercase() == args.tokenModel.t_type?.lowercase() && it.t_symbol?.lowercase() == args.tokenModel.chain?.symbol?.lowercase() }
        var chainPrice = args.tokenModel.t_price?.toDoubleOrNull()
        if (chainList.isNotEmpty()) {
            chainPrice = chainList[0].t_price?.toDoubleOrNull()
        }
        val gasPrice = if (args.tokenModel.t_address != "") ((convertedGasValue.toDoubleOrNull()
            ?: 0.0) * (chainPrice ?: 0.0)) / 1 else ((convertedGasValue.toDoubleOrNull()
            ?: 0.0) * (args.tokenModel.t_price?.toDoubleOrNull() ?: 0.0)) / 1

        viewDataBinding?.txtNetworkFeeValue?.text = convertedGasValue + " " + transaction.transactionSymbol+" " + "(${preferenceHelper.getSelectedCurrency()?.symbol}${
                String.format(
                    "%.2f",
                    gasPrice
                ) 
            })"
    }

    /* @SuppressLint("SetTextI18n")
     private fun setTokenTransferDetails(transaction: TransactionDetail) {
         if (transaction.tokenTransferDetails[0].from.lowercase() == Wallet.getPublicWalletAddress(
                 args.tokenModel.chain?.coinType!!
             )?.lowercase()
         ) {
             viewDataBinding?.txtRecipientValue?.text = transaction.tokenTransferDetails[0].to

             setAmountLabel(
                 "-" + transaction.tokenTransferDetails[0].amount + " " + args.tokenModel.t_symbol,
                 viewDataBinding?.txtBalance?.context?.resources?.getColor(R.color.red, null)
             )
         } else {
             viewDataBinding?.txtRecipientValue?.text = transaction.tokenTransferDetails[0].from
             setAmountLabel(
                 "+" + transaction.tokenTransferDetails[0].amount + " " + args.tokenModel.t_symbol,
                 viewDataBinding?.txtBalance?.context?.resources?.getColor(
                     R.color.green_099817,
                     null
                 )
             )
         }

         if (args.tokenModel.t_address == "" && args.transaction.amount.toDouble() <= 0.0) {
             viewDataBinding?.txtToolbarTitle?.text = "Smart Contract Call"

             setAmountLabel(
                 "-" + transaction.amount + " " + args.tokenModel.t_symbol,
                 viewDataBinding?.txtBalance?.context?.resources?.getColor(R.color.red, null)
             )


         } else {
             viewDataBinding?.txtToolbarTitle?.text = "Transfer"
         }

         viewDataBinding?.groupSwap?.visibility = VISIBLE
         viewDataBinding?.constraintSwap?.visibility = GONE

     }*/


    private fun setTokenTransferDetails(
        firstTransaction: TransactionDetail,
        isFromSec: Boolean = false
    ) {


        if (args.transaction.methodId == "" && args.transaction.isToContract) {
            viewDataBinding?.txtToolbarTitle?.text =
                context?.getString(R.string.transfer) /*"Transfer"*/

        } else {

            if (args.tokenModel.t_address == "" && args.transaction.amount.toDouble() <= 0.0) {
                viewDataBinding?.txtToolbarTitle?.text = /*"Smart Contract Call" */
                    context?.getString(R.string.smart_contract_call)

            } else {
                viewDataBinding?.txtToolbarTitle?.text = context?.getString(R.string.transfer)
            }
        }


        val availableSender = firstTransaction.tokenTransferDetails.firstOrNull {
            it.from.lowercase() == Wallet.getPublicWalletAddress(args.tokenModel.chain?.coinType!!)
                ?.lowercase()
        }
        val availableReceiver = firstTransaction.tokenTransferDetails.firstOrNull {
            it.to.lowercase() == Wallet.getPublicWalletAddress(args.tokenModel.chain?.coinType!!)
                ?.lowercase()
        }


        val tokenTransferDetails = firstTransaction.tokenTransferDetails.firstOrNull()

        loge("transactionDetail", "$tokenTransferDetails")

        if (tokenTransferDetails != null) {

            if (args.tokenModel.t_symbol?.lowercase() == "btc") {
                val obj = firstTransaction.outputDetails.filter {
                    it.outputHash == Wallet.getPublicWalletAddress(CoinType.BITCOIN)
                }
                val amount = if (obj.isNotEmpty()) obj[0].amount else "0.0"

                loge("BTCamount=>", "$amount")
            }


            val amount =
                if (args.tokenModel.t_address == "" && args.transaction.amount.toDouble() <= 0.0) {
                    setBalanceText(
                        firstTransaction.txfee.toBigDecimal() ?: 0.toBigDecimal(),
                        "",
                        8
                    )
                } else {


                    setBalanceText(
                        args.transaction.amount.toBigDecimal() ?: 0.toBigDecimal(),
                        "",
                        8
                    )
                }

            viewDataBinding?.txtRecipientValue?.text = when (tokenTransferDetails.from) {
                args.tokenModel.chain?.walletAddress?.lowercase() -> tokenTransferDetails.to ?: ""
                else -> tokenTransferDetails.from ?: ""
            }


            val symbolValue =
                if (args.transaction.priceToShow?.startsWith("+") == true) "+" else "-"

            setAmountLabel(

                text = if (tokenTransferDetails.from == args.tokenModel.chain?.walletAddress?.lowercase()) {
                    "$symbolValue$amount"
                } else {
                    "$symbolValue$amount"
                },
                color = Color.WHITE
            )


            /*
                        setAmountLabel(

                            //firstTransaction.inputDetails[0].inputHash

                            text = if (firstTransaction.inputDetails[0].inputHash == args.tokenModel.chain?.walletAddress?.lowercase()) {
                                "-$amount"
                            } else {
                                "+$amount"
                            },
                            color = Color.WHITE
                        )
            */

        }
    }


    private fun setAmountLabel(text: String, color: Int?) {
        // viewDataBinding?.txtBalance?.setTextColor(color!!)
        viewDataBinding?.txtBalance?.text = ""
        val textStr = text.replace("-", "")
        val convertedValue = textStr.replace(args.tokenModel.t_symbol.toString(), "").toDouble()

        val coinPrice = args.tokenModel.t_price

        if (convertedValue != 0.0) {
            viewDataBinding?.txtBalance?.text = text
            /* viewDataBinding?.txtPrice?.text =
                 if (coinPrice?.isNotEmpty() == true && convertedValue.toDouble() > 0.0
                 ) "≈" + preferenceHelper.getSelectedCurrency()?.symbol + "" + convertAmountToCurrency(
                     convertedValue.toBigDecimal(),
                     args.tokenModel.t_price.toString().toDouble().toBigDecimal()
                 ).toString() else ""*/

        } else {
            viewDataBinding?.txtPrice?.text = ""
            viewDataBinding?.txtBalance?.text = "0.00 ${args.tokenModel?.t_symbol}"
        }

        val price = convertAmountToCurrency(
            convertedValue.toBigDecimal(),
            args.tokenModel.t_price.toString().toDouble().toBigDecimal()
        )

        val formatedPrice = String.format("%.2f", price)

        viewDataBinding?.txtPrice?.text =
            if (coinPrice?.isNotEmpty() == true)  preferenceHelper.getSelectedCurrency()?.symbol + "" + formatedPrice else ""


    }

    private fun setCoinDetail(transaction: TransactionDetail) {

        if (args.transaction.methodId == "" && args.transaction.isToContract) {
            viewDataBinding?.txtToolbarTitle?.text = context?.getString(R.string.transfer)

        } else {

            if (args.tokenModel.t_address == "" && args.transaction.amount.toDouble() <= 0.0) {
                viewDataBinding?.txtToolbarTitle?.text =
                    context?.getString(R.string.smart_contract_call)

            } else {
                viewDataBinding?.txtToolbarTitle?.text = context?.getString(R.string.transfer)
            }
        }


        // val formatedAmount = String.format("%.8f", transaction.amount.toDouble())
        var btcAmount = "0.0"
        if (args.tokenModel.t_symbol?.lowercase() == "btc") {
            val obj = transaction.outputDetails.filter {
                it.outputHash == Wallet.getPublicWalletAddress(CoinType.BITCOIN)
            }
            btcAmount = if (obj.isNotEmpty()) obj[0].amount else "0.0"

            loge("BTCamount=>", "$btcAmount")
        }


        val formatedAmount = setBalanceText(
            if (args.tokenModel.t_symbol?.lowercase() == "btc") btcAmount.toBigDecimal() else transaction.amount.toBigDecimal()
                ?: 0.toBigDecimal(),
            "",
            8
        )


        if (transaction.inputDetails.first().inputHash.lowercase() == Wallet.getPublicWalletAddress(
                args.tokenModel.chain?.coinType!!
            )?.lowercase()
        ) {
            viewDataBinding?.txtRecipientValue?.text =
                transaction.outputDetails.first().outputHash ?: ""
            setAmountLabel(
                text = "-${formatedAmount} ${args.tokenModel.t_symbol}",
                viewDataBinding?.txtBalance?.context?.resources?.getColor(R.color.red, null)
            )
        } else {
            viewDataBinding?.txtRecipientValue?.text =
                transaction.inputDetails.first().inputHash ?: ""

            setAmountLabel(
                text = "+${formatedAmount} ${args.tokenModel.t_symbol}",
                viewDataBinding?.txtBalance?.context?.resources?.getColor(
                    R.color.green_099817,
                    null
                )
            )
        }
    }

    override fun setupObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                transferViewModel.transactionHistoryOkLinkResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            hideLoader()
                            if (it.data?.isNotEmpty() == true) {
                                setDetail(it.data[0])
                            }
                        }

                        is NetworkState.Loading -> {
                            requireContext().showLoader()
                        }

                        is NetworkState.Error -> {
                            hideLoader()
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
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setTokenTransferSwapDetails(transaction: TransactionDetail) {
        if (transaction.tokenTransferDetails[0].from.lowercase() == Wallet.getPublicWalletAddress(args.tokenModel.chain?.coinType!!)?.lowercase()) {
            viewDataBinding?.txtRecipientValue?.text = transaction.tokenTransferDetails[0].to
            setAmountLabel("-"+transaction.tokenTransferDetails[0].amount +" "+ args.tokenModel.t_symbol, viewDataBinding?.txtBalance?.context?.resources?.getColor(R.color.red, null))
        } else {
            viewDataBinding?.txtRecipientValue?.text = transaction.tokenTransferDetails[0].from
            setAmountLabel(
                "+" + transaction.tokenTransferDetails[0].amount + " " + args.tokenModel.t_symbol,
                viewDataBinding?.txtBalance?.context?.resources?.getColor(
                    R.color.green_099817,
                    null
                )
            )
        }

        viewDataBinding?.groupSwap?.visibility = GONE
        viewDataBinding?.constraintSwap?.visibility = VISIBLE

        if (args.tokenModel.t_address == "" && args.transaction.amount.toDouble() <= 0.0) {
            viewDataBinding?.txtToolbarTitle?.text =/* "Smart Contract Call"*/
                context?.getString(R.string.smart_contract_call)
        } else {
            viewDataBinding?.txtToolbarTitle?.text = /*"Swap" */context?.getString(R.string.swap)
        }

        viewDataBinding?.txtRecipientValue?.text = args.transaction.addressShow


        if (transaction.tokenTransferDetails.size > 1) {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val contractAddressList = listOf(
                        transaction.tokenTransferDetails[0].tokenContractAddress,
                        transaction.tokenTransferDetails[transaction.tokenTransferDetails.size - 1].tokenContractAddress
                    )
                    val tokenListTransaction =
                        tokenViewModel.getTokenListByContractAddress(contractAddressList)


                    if (tokenListTransaction.size > 1) {
                        requireActivity().runOnUiThread {
                            tokenListTransaction.forEach {

                                if (it.t_address == transaction.tokenTransferDetails[0].tokenContractAddress) {


                                    Glide.with(requireContext()).load(it.t_logouri)
                                        .into(viewDataBinding?.imgFromSwap!!)
                                    viewDataBinding?.txtFromBalance?.text =
                                        transaction.tokenTransferDetails/*.distinctBy { it.tokenContractAddress }*/[0].amount + " " + transaction.tokenTransferDetails.distinctBy { it.tokenContractAddress }[0].symbol
                                    viewDataBinding?.txtFromType?.text = it.t_type
                                } else {
                                    Glide.with(requireContext()).load(it.t_logouri)
                                        .into(viewDataBinding?.imgToSwap!!)
                                    viewDataBinding?.txtToBalance?.text =
                                        transaction.tokenTransferDetails/*.distinctBy { it.tokenContractAddress }*/[1].amount + " " + transaction.tokenTransferDetails.distinctBy { it.tokenContractAddress }[1].symbol
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

        if (args.tokenModel.t_address == "" && args.transaction.amount.toDouble() <= 0.0) {
            viewDataBinding?.txtToolbarTitle?.text = /*"Smart Contract Call"*/
                context?.getString(R.string.smart_contract_call)
        } else {
            viewDataBinding?.txtToolbarTitle?.text = /*"Swap"*/ context?.getString(R.string.swap)
        }

        // val payTokenTransferDetails = firstTransaction.tokenTransferDetails.firstOrNull()

        val payTokenTransferDetails =
            firstTransaction.tokenTransferDetails.firstOrNull {
                it.from.lowercase() == Wallet.getPublicWalletAddress(
                    args.tokenModel.chain?.coinType!!
                )?.lowercase()
            }

        // val getTokenTransferDetails = if (firstTransaction.tokenTransferDetails.distinctBy { it.tokenContractAddress }.size > 1) firstTransaction.tokenTransferDetails.distinctBy { it.tokenContractAddress }[1] else firstTransaction.tokenTransferDetails.distinctBy { it.tokenContractAddress }[0]
        val getTokenTransferDetails =
            firstTransaction.tokenTransferDetails.firstOrNull {
                it.to.lowercase() == Wallet.getPublicWalletAddress(
                    args.tokenModel.chain?.coinType!!
                )?.lowercase()
            }


        if (payTokenTransferDetails != null && getTokenTransferDetails != null) {
            viewDataBinding?.txtFromBalance?.text =
                "${payTokenTransferDetails.amount} ${payTokenTransferDetails.symbol}"
            viewDataBinding?.txtToBalance?.text =
                "${getTokenTransferDetails.amount} ${getTokenTransferDetails.symbol}"

            val contractAddressList = listOf(
                firstTransaction.tokenTransferDetails[0].tokenContractAddress,
                if (firstTransaction.tokenTransferDetails.distinctBy { it.tokenContractAddress }.size > 1) firstTransaction.tokenTransferDetails.distinctBy { it.tokenContractAddress }[1].tokenContractAddress else firstTransaction.tokenTransferDetails.distinctBy { it.tokenContractAddress }[0].tokenContractAddress
            )

            val allToken = tokenViewModel.getTokenListByContractAddress(contractAddressList)

            if (allToken != null) {
                val payToken =
                    allToken.firstOrNull { it.t_address?.lowercase() == payTokenTransferDetails.tokenContractAddress.lowercase() }
                if (payToken != null) {
                    viewDataBinding?.imgFromSwap!!.loadImage(payToken.t_logouri)
                    viewDataBinding?.txtFromType?.text = payToken.t_type ?: ""
                }


                val getToken = allToken.firstOrNull {

                it.t_address?.lowercase() == getTokenTransferDetails.tokenContractAddress?.lowercase()
                }

                if (getToken != null) {
                    viewDataBinding?.imgToSwap!!.loadImage(getToken.t_logouri)
                    viewDataBinding?.txtToType?.text = getToken.t_type ?: ""
                }
            }
        }

        viewDataBinding?.txtRecipientValue?.text = args.transaction.addressShow
    }

    private fun ImageView.loadImage(url: String?) {
        Glide.with(this)
            .load(url)
            .into(this)
    }

    // Extension function to load an image into an ImageView


}