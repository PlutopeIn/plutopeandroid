package com.app.plutope.ui.base

import com.app.plutope.model.Tokens
import com.app.plutope.ui.fragment.transactions.send.send_coin.TransferNetworkDetail
import com.app.plutope.utils.bigIntegerToString
import com.app.plutope.utils.loge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.web3j.crypto.Credentials
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.core.methods.response.EthEstimateGas
import org.web3j.protocol.core.methods.response.EthSendTransaction
import org.web3j.utils.Numeric
import java.math.BigInteger

suspend fun getTransactionCount(web3: Web3j, address: String): BigInteger {
    withContext(Dispatchers.IO) {
        web3.ethGetTransactionCount(
            address,
            DefaultBlockParameterName.LATEST
        ).sendAsync().get()
    }.apply {
        return this.transactionCount
    }
}

suspend fun getGasPrice(web3: Web3j): BigInteger {
    withContext(Dispatchers.IO) {
        web3.ethGasPrice().sendAsync().get()
    }.apply {
        return this.gasPrice
    }
}

suspend fun estimateGas(
    web3: Web3j,
    address: String,
    nonce: BigInteger,
    gasPrice: BigInteger,
    toAddress: String?,
    data: String
): EthEstimateGas {
    withContext(Dispatchers.IO) {
        web3.ethEstimateGas(
            Transaction(
                address, nonce, gasPrice,
                BigInteger.ONE, toAddress, BigInteger.ZERO, data
            )
        ).sendAsync().get()
    }.apply {
        return this
    }
}

fun calculateGasLimit(gasEstimationResult: EthEstimateGas): BigInteger {
    return if (gasEstimationResult.result.isNullOrBlank()) BigInteger.valueOf(50000) else BigInteger(
        gasEstimationResult.result.substring(2),
        16
    )
}

fun buildRawTransaction(
    nonce: BigInteger,
    gasPrice: String,
    gasLimit: BigInteger,
    toAddress: String?,
    value: BigInteger,
    data: String
): RawTransaction {
    return RawTransaction.createTransaction(
        nonce,
        gasPrice.toBigInteger(),
        gasLimit,
        toAddress,
        value,
        data
    )
}

fun signTransaction(rawTransaction: RawTransaction, chainId: String, privateKey: String): String {
    val signedTransaction = TransactionEncoder.signMessage(
        rawTransaction,
        chainId.toLong(),
        Credentials.create(privateKey)
    )
    return Numeric.toHexString(signedTransaction)
}

suspend fun sendRawTransaction(web3: Web3j, signedTransaction: String): EthSendTransaction {
    withContext(Dispatchers.IO) {
        web3.ethSendRawTransaction(signedTransaction).sendAsync().get()
    }.apply {
        return this
    }

}

fun handleTransactionResult(
    result: EthSendTransaction,
    gasLimit: BigInteger,
    nonce: BigInteger,
    gasPrice: BigInteger,
    txValue: BigInteger,
    tokenDetails: Tokens,
    completion: (Boolean, String?, TransferNetworkDetail?) -> Unit
) {
    return if (result.hasError()) {
        val errorMessage = result.error.message
        loge("Transaction Error :", errorMessage)
        completion(
            false,
            errorMessage,
            TransferNetworkDetail(
                gasLimit,
                nonce.toInt(),
                gasLimit,
                txValue.toString(),
                bigIntegerToString(gasPrice),
                tokenDetails.t_decimal
            )
        )
    } else {

        val transactionHash = result.transactionHash
        completion(
            true,
            transactionHash,
            TransferNetworkDetail(
                gasLimit,
                nonce.toInt(),
                gasLimit,
                txValue.toString(),
                bigIntegerToString(gasPrice),
                tokenDetails.t_decimal
            )
        )
    }

}

fun handleException(e: Exception, completion: (Boolean, String?, TransferNetworkDetail?) -> Unit) {
    // Implementation for handling exceptions
}


