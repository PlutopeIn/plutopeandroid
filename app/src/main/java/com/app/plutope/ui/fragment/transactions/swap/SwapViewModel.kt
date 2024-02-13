package com.app.plutope.ui.fragment.transactions.swap

import androidx.lifecycle.viewModelScope
import com.app.plutope.data.repository.SwapRepo
import com.app.plutope.data.repository.TokensRepo
import com.app.plutope.model.AvailablePairsResponseModel
import com.app.plutope.model.ExchangeRequestModel
import com.app.plutope.model.ExchangeResponseModel
import com.app.plutope.model.ExchangeStatusResponse
import com.app.plutope.model.OkxApproveResponse
import com.app.plutope.model.OkxSwapResponse
import com.app.plutope.model.RangSwapQuoteModel
import com.app.plutope.model.Tokens
import com.app.plutope.ui.base.BaseViewModel
import com.app.plutope.ui.fragment.transactions.swap.previewSwap.RangoSwapResponseModel
import com.app.plutope.utils.common.CommonNavigator
import com.app.plutope.utils.constant.CHANGE_NOW_AVAILABLE_PAIR
import com.app.plutope.utils.constant.OKX_APPROVE_API
import com.app.plutope.utils.constant.OKX_SECRETE_API_KEY
import com.app.plutope.utils.constant.OKX_SWAP_API
import com.app.plutope.utils.convertToWei
import com.app.plutope.utils.network.NetworkState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.Date
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

@HiltViewModel
class SwapViewModel @Inject constructor(val swapRepo: SwapRepo, val tokensRepo: TokensRepo) :
    BaseViewModel<CommonNavigator>() {


    //execute Exchange api
    private val _tagExecuteExchange =
        MutableStateFlow<NetworkState<ExchangeResponseModel?>>(NetworkState.Empty())

    val executeExchangeResponse: StateFlow<NetworkState<ExchangeResponseModel?>>
        get() = _tagExecuteExchange

    fun executeExchange(url: String, body: ExchangeRequestModel) {
        viewModelScope.launch {
            _tagExecuteExchange.emit(NetworkState.Loading())
            _tagExecuteExchange.collectStateFlow(swapRepo.executeExchange(url, body))
        }
    }


    //execute Exchange Status Api
    private val _tagExecuteExchangeStatus =
        MutableStateFlow<NetworkState<ExchangeStatusResponse?>>(NetworkState.Empty())

    val executeExchangeStatusResponse: StateFlow<NetworkState<ExchangeStatusResponse?>>
        get() = _tagExecuteExchangeStatus

    fun executeExchangeStatus(url: String) {
        viewModelScope.launch {
            _tagExecuteExchangeStatus.emit(NetworkState.Loading())
            _tagExecuteExchangeStatus.collectStateFlow(swapRepo.executeExchangeStatus(url = url))
        }
    }


    //execute Swap using okx api
    private val _tagSwapUsingOkx =
        MutableStateFlow<NetworkState<OkxSwapResponse?>>(NetworkState.Empty())

    val executeSwapUsingOkxResponse: StateFlow<NetworkState<OkxSwapResponse?>>
        get() = _tagSwapUsingOkx

    fun executeSwapOkx(
        amount: String,
        chainId: String,
        toTokenAddress: String,
        fromTokenAddress: String,
        userWalletAddress: String, decimal: Int? = 18
    ) {
        // val amountSend: BigInteger = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger()

        val amountSend: BigInteger = convertToWei(amount.toDouble(), decimal!!)
        val timestamp = Date().toInstant().toString()
        val requestPath = "/api/v5/dex/aggregator/swap?"
        val queryParams =
            "amount=${amountSend}&chainId=${chainId}&toTokenAddress=${toTokenAddress}&fromTokenAddress=${fromTokenAddress}&slippage=0.1&userWalletAddress=${userWalletAddress}"
        val httpMethod = "GET"
        val preSignData = "$timestamp$httpMethod$requestPath$queryParams"
        val secretKey = OKX_SECRETE_API_KEY

        val sign = generateHmacSHA256Signature(preSignData, secretKey)

        /* val headers = mutableMapOf<String, String>()
         headers["OK-ACCESS-TIMESTAMP"] = timestamp
         headers["OK-ACCESS-SIGN"] = sign
         headers["Content-Type"] = "application/json"*/


        val url =
            OKX_SWAP_API + "amount=${amountSend}&chainId=${chainId}&toTokenAddress=${toTokenAddress}&fromTokenAddress=${fromTokenAddress}&slippage=0.1&userWalletAddress=${userWalletAddress}"
        viewModelScope.launch {
            _tagSwapUsingOkx.emit(NetworkState.Loading())
            _tagSwapUsingOkx.collectStateFlow(swapRepo.executeSwapUsingOkx(url, sign, timestamp))
        }
    }


    //execute Swap pair check api
    private val _tagSwapPairs =
        MutableStateFlow<NetworkState<MutableList<AvailablePairsResponseModel>?>>(NetworkState.Empty())

    val executeSwapUsingSwapPairResponse: StateFlow<NetworkState<MutableList<AvailablePairsResponseModel>?>>
        get() = _tagSwapPairs


    fun executeSwapPairResponse(fromCurrency: String, fromNetwork: String, toNetwork: String) {

        val url =
            CHANGE_NOW_AVAILABLE_PAIR + "fromCurrency=${fromCurrency}&fromNetwork=${fromNetwork}&toNetwork=${toNetwork}"
        viewModelScope.launch {
            _tagSwapPairs.emit(NetworkState.Loading())
            _tagSwapPairs.collectStateFlow(swapRepo.executeSwapAvailablePairs(url))
        }
    }

    fun filterTokensFromPair(
        pairTokenList: MutableList<AvailablePairsResponseModel>,
        tokenList: MutableList<Tokens>
    ): MutableList<Tokens> {
        val filterTokensPairList = mutableListOf<Tokens>()
        pairTokenList.forEach { pair ->
            tokenList.forEach { token ->
                if (pair.toCurrency.lowercase() == token.t_symbol?.lowercase() && (token.t_type == "ERC20" || token.t_type == "POLYGON" || token.t_type == "BEP20")) {
                    filterTokensPairList.add(token)
                }
            }
        }

        return filterTokensPairList
    }

    //execute Approve using okx api
    private val _tagApproveUsingOkx =
        MutableStateFlow<NetworkState<OkxApproveResponse?>>(NetworkState.Empty())

    val executeApproveUsingOkxResponse: StateFlow<NetworkState<OkxApproveResponse?>>
        get() = _tagApproveUsingOkx

    fun executeApproveOkx(
        amount: String,
        chainId: String,
        tokenContractAddress: String, decimal: Int? = 18
    ) {
        // val amountSend: BigInteger = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger()
        val amountSend: BigInteger = convertToWei(amount.toDouble(), decimal!!)
        val timestamp = Date().toInstant().toString()
        val requestPath = "/api/v5/dex/aggregator/approve-transaction?"
        val queryParams =
            "amount=${amountSend}&chainId=${chainId}&tokenContractAddress=${tokenContractAddress}&approveAmount=${amountSend}"
        val httpMethod = "GET"
        val preSignData = "$timestamp$httpMethod$requestPath$queryParams"
        val secretKey = OKX_SECRETE_API_KEY

        val sign = generateHmacSHA256Signature(preSignData, secretKey)

        /* val headers = mutableMapOf<String, String>()
         headers["OK-ACCESS-TIMESTAMP"] = timestamp
         headers["OK-ACCESS-SIGN"] = sign
         headers["Content-Type"] = "application/json"*/

        val url =
            OKX_APPROVE_API + "amount=${amountSend}&chainId=${chainId}&tokenContractAddress=${tokenContractAddress}&approveAmount=${amountSend}"
        viewModelScope.launch {
            _tagApproveUsingOkx.emit(NetworkState.Loading())
            _tagApproveUsingOkx.collectStateFlow(
                swapRepo.executeApproveUsingOkx(
                    url,
                    sign,
                    timestamp
                )
            )
        }
    }

    private fun generateHmacSHA256Signature(data: String, key: String): String {
        val keyBytes = key.toByteArray(StandardCharsets.UTF_8)
        val secretKeySpec = SecretKeySpec(keyBytes, "HmacSHA256")
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(secretKeySpec)
        val signatureBytes = mac.doFinal(data.toByteArray(StandardCharsets.UTF_8))
        return Base64.getEncoder().encodeToString(signatureBytes)
    }

    //okx swap to get estimate price
    private val _tagSwapUsingOkxEstimate =
        MutableStateFlow<NetworkState<OkxSwapResponse?>>(NetworkState.Empty())

    val executeSwapUsingOkxEstimatResponse: StateFlow<NetworkState<OkxSwapResponse?>>
        get() = _tagSwapUsingOkxEstimate

    fun executeSwapOkxEstimat(
        amount: String,
        chainId: String,
        toTokenAddress: String,
        fromTokenAddress: String,
        userWalletAddress: String,
        decimal: Int? = 18
    ) {
        // val amountSend: BigInteger = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger()

        val amountSend: BigInteger = convertToWei(amount.toDouble(), decimal!!)

        val timestamp = Date().toInstant().toString()
        val requestPath = "/api/v5/dex/aggregator/swap?"
        val queryParams =
            "amount=${amountSend}&chainId=${chainId}&toTokenAddress=${toTokenAddress}&fromTokenAddress=${fromTokenAddress}&slippage=0.1&userWalletAddress=${userWalletAddress}"
        val httpMethod = "GET"
        val preSignData = "$timestamp$httpMethod$requestPath$queryParams"
        val secretKey = OKX_SECRETE_API_KEY

        val sign = generateHmacSHA256Signature(preSignData, secretKey)
        val url =
            OKX_SWAP_API + "amount=${amountSend}&chainId=${chainId}&toTokenAddress=${toTokenAddress}&fromTokenAddress=${fromTokenAddress}&slippage=0.1&userWalletAddress=${userWalletAddress}"
        viewModelScope.launch {
            _tagSwapUsingOkxEstimate.emit(NetworkState.Loading())
            _tagSwapUsingOkxEstimate.collectStateFlow(swapRepo.executeSwapUsingOkx(url, sign, timestamp))
        }
    }


    //execute Exchange api estimate
    private val _tagExecuteEstimateExchange =
        MutableStateFlow<NetworkState<ExchangeResponseModel?>>(NetworkState.Empty())

    val executeEstimateExchangeResponse: StateFlow<NetworkState<ExchangeResponseModel?>>
        get() = _tagExecuteEstimateExchange

    fun executeEstimateExchange(url: String, body: ExchangeRequestModel) {
        viewModelScope.launch {
            _tagExecuteEstimateExchange.emit(NetworkState.Loading())
            _tagExecuteEstimateExchange.collectStateFlow(swapRepo.executeExchange(url, body))
        }
    }


    //execute Exchange api estimate
    private val _tagRangoEstimationQuote =
        MutableStateFlow<NetworkState<RangSwapQuoteModel?>>(NetworkState.Empty())

    val responseRengoExcQuoteResponse: StateFlow<NetworkState<RangSwapQuoteModel?>>
        get() = _tagRangoEstimationQuote

    fun executeRangoExchangeQuote(
        fromBlockchain: String,
        fromTokenSymbol: String,
        fromTokenAddress: String,
        toBlockchain: String,
        toTokenSymbol: String,
        toTokenAddress: String,
        walletAddress: String,
        price: String,
        decimal: Int? = 18,
        fromWalletAddress: String,
        toWalletAddress: String,
    ) {
        viewModelScope.launch {
            _tagRangoEstimationQuote.emit(NetworkState.Loading())

            val amountSend: BigInteger = convertToWei(price.toDouble(), decimal!!)

            _tagRangoEstimationQuote.collectStateFlow(
                swapRepo.executeRangoExchangeQuote(
                    fromBlockchain.uppercase(),
                    fromTokenSymbol.uppercase(),
                    fromTokenAddress,
                    toBlockchain.uppercase(),
                    toTokenSymbol.uppercase(),
                    toTokenAddress,
                    walletAddress,
                    amountSend.toString(),
                    fromWalletAddress,
                    toWalletAddress,
                )
            )
        }
    }

    //execute Exchange api estimate
    private val _tagRangoSwapSubmit =
        MutableStateFlow<NetworkState<RangoSwapResponseModel?>>(NetworkState.Empty())

    val responseRengoSwapSubmitResponse: StateFlow<NetworkState<RangoSwapResponseModel?>>
        get() = _tagRangoSwapSubmit

    fun executeRangoSubmitCall(
        fromBlockchain: String,
        fromTokenSymbol: String,
        fromTokenAddress: String,
        toBlockchain: String,
        toTokenSymbol: String,
        toTokenAddress: String,
        walletAddress: String,
        price: String, decimal: Int? = 18,
        fromWalletAddress: String,
        toWalletAddress: String,
    ) {
        viewModelScope.launch {
            _tagRangoSwapSubmit.emit(NetworkState.Loading())

            val amountSend: BigInteger = convertToWei(price.toDouble(), decimal!!)
            _tagRangoSwapSubmit.collectStateFlow(
                swapRepo.rangSwapSubmitCall(
                    fromBlockchain.uppercase(),
                    fromTokenSymbol.uppercase(),
                    fromTokenAddress,
                    toBlockchain.uppercase(),
                    toTokenSymbol.uppercase(),
                    toTokenAddress,
                    walletAddress,
                    amountSend.toString(),
                    fromWalletAddress,
                    toWalletAddress,
                )
            )
        }
    }

    /**
     * Set Active wallet
     * */

    private val _tagSetWalletActive =
        MutableStateFlow<NetworkState<String?>>(NetworkState.Empty())

    val setWalletActive: StateFlow<NetworkState<String?>>
        get() = _tagSetWalletActive

    fun setWalletActiveCall(address: String) {
        viewModelScope.launch {
            _tagSetWalletActive.emit(NetworkState.Loading())
            _tagSetWalletActive.collectStateFlow(tokensRepo.setWalletActive(address))
        }
    }

    /**
     * Send Bitcoin transaction
     * */

    private val _tagSendBTCTransaction =
        MutableStateFlow<NetworkState<String?>>(NetworkState.Empty())

    val sendBTCTransactionResponse: StateFlow<NetworkState<String?>>
        get() = _tagSendBTCTransaction

    fun sendBTCTransactionCall(
        privateKey: String,
        value: String,
        toAddress: String,
        env: String,
        fromAddress: String
    ) {
        viewModelScope.launch {
            _tagSendBTCTransaction.emit(NetworkState.Loading())
            _tagSendBTCTransaction.collectStateFlow(
                tokensRepo.sendBTCTransaction(
                    privateKey,
                    value,
                    toAddress,
                    env,
                    fromAddress
                )
            )
        }
    }


}