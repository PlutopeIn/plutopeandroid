package com.app.plutope.ui.fragment.transactions.send.send_coin

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.app.plutope.data.repository.TokensRepo
import com.app.plutope.model.Tokens
import com.app.plutope.ui.base.BaseViewModel
import com.app.plutope.utils.common.CommonNavigator
import com.app.plutope.utils.network.NetworkState
import com.app.plutope.utils.stringToBigDecimal
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject

@HiltViewModel
class SendCoinViewModel @Inject constructor(private val tokensRepo: TokensRepo) :
    BaseViewModel<CommonNavigator>() {
    var coinDetail = MutableLiveData<SendCoinDetail>()

    fun setCoinDetail(
        walletAddress: String,
        amount: BigDecimal,
        tokenModel: Tokens,
        tokenList: List<Tokens> = listOf(),
        convertedPrice: String
    ) {
        coinDetail.value =
            SendCoinDetail(walletAddress, amount, tokenModel, tokenList, convertedPrice)
    }

    fun getCoinDetail(): LiveData<SendCoinDetail> {
        return coinDetail
    }


    var isFromLaverageChange = MutableLiveData<Boolean>(false)
    var customGasPrice = MutableLiveData<BigDecimal>(stringToBigDecimal("0"))
    var customGasLimit = MutableLiveData<String>("0")
    var customNonce = MutableLiveData<String>("0")
    var customTransactionData = MutableLiveData<String>("")
    var decimal = MutableLiveData<Int>(18)


    /**
     * Set Active wallet
     * */

    private val _tagSetWalletActive =
        MutableStateFlow<NetworkState<String?>>(NetworkState.Empty())

    val setWalletActive: StateFlow<NetworkState<String?>>
        get() = _tagSetWalletActive

    fun setWalletActiveCall(address: String, receiverAddress: String) {
        viewModelScope.launch {
            _tagSetWalletActive.emit(NetworkState.Loading())
            _tagSetWalletActive.collectStateFlow(
                tokensRepo.setWalletActive(
                    address,
                    receiverAddress
                )
            )
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

@Parcelize
data class SendCoinDetail(
    val address: String,
    val amount: BigDecimal,
    val tokenModel: Tokens,
    val tokenList: List<Tokens> = listOf(),
    val convertedPrice: String
) : Parcelable

@Parcelize
data class TransferNetworkDetail(
    val gasLimit: BigInteger,
    val nonce: Int,
    val gasFee: BigInteger,
    val gasAmount: String,
    var gasPrice: String = "",
    var decimal: Int? = 18
) : Parcelable
