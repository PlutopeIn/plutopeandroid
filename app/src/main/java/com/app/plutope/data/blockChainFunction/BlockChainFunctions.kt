package com.app.plutope.data.blockChainFunction

import com.app.plutope.model.Tokens
import com.app.plutope.ui.fragment.transactions.send.send_coin.TransferNetworkDetail
import java.math.BigInteger

interface BlockchainFunctions {
    fun sendTokenOrCoin(
        receiverAddress: String?,
        tokenAmount: Double,
        tokenList:List<Tokens>,
        completion: (Boolean, String?, String?) -> Unit
    )

    //  fun getBalance(completion: (String?) -> Unit)
    suspend fun getBalance(completion: (String?) -> Unit)
    fun swapTokenOrCoinOkx(
        toAddress: String?,
        data: String,
        gasPrice: String,
        gasLimit: String,
        amountSend: BigInteger,
        dexCotractAddress: String?,
        completion: (Boolean, String?, String?) -> Unit,
        approveCompletion: (Boolean, String?, String?) -> Unit
    )

    suspend fun getTokenOrCoinNetworkDetailBeforeSend(
        receiverAddress: String?,
        tokenAmount: Double,
        tokenList: List<Tokens>,
        completion: (Boolean, TransferNetworkDetail?, String?) -> Unit
    )


    suspend fun getGasFee(completion: (BigInteger?, String, BigInteger, BigInteger) -> Unit)

    suspend fun getDecimal(completion: (Int?) -> Unit)

    suspend fun swapTokenOrCoin(
        toAddress: String?,
        gas: String,
        gasPrice: String,
        data: String,
        tokenAmount: String,
        completion: (Boolean, String?, String?) -> Unit
    )

    suspend fun signAndSendTranscation(
        toAddress: String?,
        gasLimit: String?,
        gasPrice: String,
        data: String?,
        value: String,
        completion: (Boolean, String?, String?) -> Unit
    )

    suspend fun sendTokenOrCoinWithLavrageFee(
        receiverAddress: String?,
        tokenAmount: Double,
        nonce: BigInteger,
        gasAmount: String,
        gasLimit: BigInteger,
        decimal: Int? = 18,
        tokenList: List<Tokens>,
        completion: (Boolean, String?, String?) -> Unit
    )

    suspend fun getTransactionHash(
        toAddress: String?,
        gasLimit: String,
        gasPrice: String,
        data: String,
        value: String,
        isGettingTransactionHash: Boolean = false,
        completion: (Boolean, String?, TransferNetworkDetail?) -> Unit
    )


}