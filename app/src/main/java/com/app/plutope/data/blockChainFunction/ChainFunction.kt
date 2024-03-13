package com.app.plutope.data.blockChainFunction

import android.util.Log
import com.app.plutope.model.Tokens
import com.app.plutope.model.Wallet
import com.app.plutope.network.NoConnectivityException
import com.app.plutope.networkConfig.UserTokenData
import com.app.plutope.ui.fragment.transactions.send.send_coin.TransferNetworkDetail
import com.app.plutope.utils.bigIntegerToString
import com.app.plutope.utils.coinTypeEnum.CoinType
import com.app.plutope.utils.constant.DEFAULT_CHAIN_ADDRESS
import com.app.plutope.utils.constant.NO_INTERNET_CONNECTION
import com.app.plutope.utils.convertWeiToEther
import com.app.plutope.utils.getReceipt
import com.app.plutope.utils.hexStringToBigInteger
import com.app.plutope.utils.loge
import com.app.plutope.utils.stringToBigInteger
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.web3j.crypto.Credentials
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Convert
import org.web3j.utils.Numeric
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.net.ConnectException
import java.net.UnknownHostException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ChainFunctions(private val tokenDetails: Tokens) : BlockchainFunctions {

    override suspend fun getGasFee(completion: (BigInteger?, String, BigInteger, BigInteger) -> Unit) {
        val chain = tokenDetails.chain
        val web3 = Web3j.build(HttpService(chain?.rpcURL))

        val receiverAddress: String = Wallet.getPublicWalletAddress(coinType = CoinType.ETHEREUM)!!
        val tokenAmount: Double = 0.0

        val toWalletAddress = try {
            receiverAddress
        } catch (e: Exception) {
            return
        }
        val myWalletAddress = try {
            Wallet.getPublicWalletAddress(
                tokenDetails.chain?.coinType ?: CoinType.ETHEREUM
            )
        } catch (e: Exception) {
            return
        }

        val privateKeyData = chain?.privateKey
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val privateKeyHex: String? = privateKeyData
                val credentials = Credentials.create(privateKeyHex)
                web3.ethGetTransactionCount(
                    credentials.address,
                    DefaultBlockParameterName.LATEST
                ).sendAsync().get().apply {
                    val nonce: BigInteger = this.transactionCount
                    val value: BigInteger =
                        Convert.toWei(tokenAmount.toString(), Convert.Unit.ETHER).toBigInteger()
                    web3.ethGasPrice().sendAsync().get().apply {
                        val gasPrice = this.gasPrice
                        web3.ethEstimateGas(
                            org.web3j.protocol.core.methods.request.Transaction(
                                myWalletAddress, nonce, gasPrice,
                                BigInteger.ONE, toWalletAddress, value, ""
                            )
                        ).sendAsync().get().apply {
                            val initialGasLimit =
                                if (result.isNullOrBlank()) BigInteger.valueOf(21000) else BigInteger(
                                    this.result.substring(2),
                                    16
                                )
                            val increaseFactor: BigDecimal = BigDecimal.valueOf(1.1)
                            val gaslimit =
                                (initialGasLimit.toBigDecimal() * increaseFactor).toBigInteger()

                            val fee = gasPrice * gaslimit
                            val gasAmount =
                                convertWeiToEther(fee.toString(), tokenDetails.t_decimal!!.toInt())

                            // completion(fee, "$nonce")
                            completion(fee, "$nonce", gaslimit, gasPrice)

                        }
                    }


                }


            } catch (e: Exception) {

                when (e) {
                    is NoConnectivityException, is UnknownHostException, is ConnectException -> {
                        // completion(0.toBigInteger())
                    }

                    else -> {
                        // completion(0.toBigInteger())
                    }
                }
                e.printStackTrace()
            }

        }

    }

    override suspend fun getDecimal(completion: (Int?) -> Unit) {
        completion(18)
    }

    override fun sendTokenOrCoin(
        receiverAddress: String?,
        tokenAmount: Double,
        tokenList: List<Tokens>,
        completion: (Boolean, String?, String?) -> Unit
    ) {
        val chain = tokenDetails.chain

        val web3 = Web3j.build(HttpService(chain?.rpcURL))
        val toWalletAddress = try {
            receiverAddress
        } catch (e: Exception) {
            return
        }
        val myWalletAddress = try {
            Wallet.getPublicWalletAddress(
                tokenDetails.chain?.coinType ?: CoinType.ETHEREUM
            )
        } catch (e: Exception) {
            return
        }

        val privateKeyData = chain?.privateKey

        CoroutineScope(Dispatchers.Default).launch {
            try {
                val privateKeyHex: String? = privateKeyData
                val credentials = Credentials.create(privateKeyHex)
                web3.ethGetTransactionCount(
                    credentials.address,
                    DefaultBlockParameterName.LATEST
                ).sendAsync().get().apply {
                    val nonce: BigInteger = this.transactionCount
                    val value: BigInteger =
                        Convert.toWei(tokenAmount.toString(), Convert.Unit.ETHER).toBigInteger()

                    web3.ethGasPrice().sendAsync().get().apply {
                        val gasPrice = this.gasPrice
                        web3.ethEstimateGas(
                            org.web3j.protocol.core.methods.request.Transaction(
                                myWalletAddress, nonce, gasPrice,
                                BigInteger.ONE, toWalletAddress, value, ""
                            )
                        ).sendAsync().get().apply {
                            val initialGasLimit =
                                if (result.isNullOrBlank()) BigInteger.valueOf(21000) else BigInteger(
                                    this.result.substring(2),
                                    16
                                )
                            val increaseFactor: BigDecimal = BigDecimal.valueOf(10.1)
                            val gaslimit =
                                (initialGasLimit.toBigDecimal() * increaseFactor).toBigInteger()

                            val fee = gasPrice * gaslimit
                            val gasAmount =
                                convertWeiToEther(fee.toString(), tokenDetails.t_decimal!!.toInt())

                            loge("condition", "==> ${tokenDetails.t_balance} > $gasAmount")

                            if (tokenDetails.t_balance.toBigDecimal() > gasAmount.toBigDecimal()) {

                                val rawTransaction: RawTransaction =
                                    RawTransaction.createEtherTransaction(
                                        nonce,
                                        gasPrice,
                                        gaslimit,
                                        toWalletAddress,
                                        value
                                    )

                                val signedMessage: ByteArray =
                                    TransactionEncoder.signMessage(
                                        rawTransaction,
                                        chain?.chainIdHex?.toLong()!!,
                                        credentials
                                    )
                                val hexValue: String = Numeric.toHexString(signedMessage)

                                web3.ethSendRawTransaction(hexValue).sendAsync().get().apply {

                                    if (this.error != null) {
                                        completion(false, this.error.message, "")

                                    } else {
                                        val count = 0
                                        getReceipt(
                                            web3,
                                            transactionHash,
                                            count
                                        ) { success, errorMessage, transaction ->
                                            if (transaction?.get()?.status == "0x1") {
                                                completion(
                                                    true,
                                                    null,
                                                    transactionHash
                                                )
                                            } else {
                                                completion(
                                                    false,
                                                    "Send failed please check after sometime.",
                                                    ""
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                completion(
                                    false,
                                    "You don't have enough ${tokenDetails.t_name} (${tokenDetails.t_symbol}) to cover network fees.",
                                    null
                                )
                            }
                        }

                    }

                }


            } catch (e: Exception) {

                when (e) {
                    is NoConnectivityException, is UnknownHostException, is ConnectException -> {
                        completion(false, NO_INTERNET_CONNECTION, "")
                    }

                    else -> {
                        completion(false, e.localizedMessage, "")
                    }
                }
                e.printStackTrace()
            }

        }
    }

    /*
        suspend fun getBalance(): String {
            return suspendCoroutine { continuation ->
                val chain = tokenDetails.chain ?: run {
                    continuation.resume("0")
                    return@suspendCoroutine
                }

                val walletAddress =
                    Wallet.getPublicWalletAddress(chain.coinType ?: CoinType.ETHEREUM) ?: run {
                        continuation.resume("0")
                        return@suspendCoroutine
                    }

                if (chain.coinType != CoinType.BITCOIN) {
                    val httpClientBuilder = OkHttpClient.Builder()
                    httpClientBuilder.connectTimeout(20000, TimeUnit.SECONDS)
                    val httpClient = httpClientBuilder.build()
                    val web3 = Web3j.build(HttpService(chain.rpcURL, httpClient))

                    val ethGetBalanceFuture =
                        web3?.ethGetBalance(walletAddress, DefaultBlockParameterName.LATEST)
                            ?.sendAsync()

                    ethGetBalanceFuture?.whenComplete { ethGetBalance, exception ->
                        GlobalScope.launch {
                            try {
                                if (exception == null) {
                                    val weiValue = BigDecimal(ethGetBalance?.balance?.toString() ?: "0")
                                    val ethValue = weiValue.divide(BigDecimal.TEN.pow(18))

                                    UserTokenData.update(
                                        symbol = tokenDetails.t_symbol ?: "",
                                        balance = ethValue.setScale(18, RoundingMode.DOWN).toString()
                                    )

                                    continuation.resume(
                                        ethValue.setScale(18, RoundingMode.DOWN).toString()
                                    )
                                } else {
                                    UserTokenData.update(
                                        symbol = tokenDetails.t_symbol ?: "",
                                        balance = "0"
                                    )
                                    Log.e(
                                        "TAG",
                                        "Failed to Fetch Balance: ${exception.printStackTrace()}"
                                    )
                                    continuation.resume("0.0")
                                }
                            } catch (e: Exception) {
                                continuation.resume("0.0")
                            }
                        }
                    }
                } else {
                    continuation.resume(tokenDetails.t_balance ?: "0")
                }
            }
        }
    */


    override suspend fun getBalance(completion: (String?) -> Unit) {
        val chain = tokenDetails.chain ?: return
        val walletAddress =
            Wallet.getPublicWalletAddress(tokenDetails.chain?.coinType ?: CoinType.ETHEREUM)
                ?: return
        if (chain.coinType != CoinType.BITCOIN) {
            val httpClientBuilder = OkHttpClient.Builder()
            httpClientBuilder.connectTimeout(
                20000,
                TimeUnit.SECONDS
            )
            val httpClient = httpClientBuilder.build()
            val web3 = Web3j.build(HttpService(chain.rpcURL, httpClient))


            val latch = CountDownLatch(1)
            val executor: ExecutorService = Executors.newSingleThreadExecutor()

            executor.execute {
                try {
                    web3?.ethGetBalance(walletAddress, DefaultBlockParameterName.LATEST)
                        ?.sendAsync()
                        ?.apply {

                            // this.get(20000, TimeUnit.SECONDS)
                            // val weiValue = BigDecimal(this?.balance.toString())
                            val weiValue =
                                BigDecimal(this.get(30, TimeUnit.SECONDS).balance.toString())

                            val ethValue = weiValue.divide(BigDecimal.TEN.pow(18))
                            UserTokenData.update(
                                symbol = tokenDetails.t_symbol.toString(),
                                balance = ethValue?.toString()
                            )


                            completion(ethValue.setScale(18, RoundingMode.DOWN).toString())
                        }

                } catch (e: Exception) {
                    UserTokenData.update(
                        symbol = tokenDetails.t_symbol.toString(),
                        balance = "0"
                    )
                    completion("0.0")
                    e.printStackTrace()
                } finally {
                    latch.countDown() // Decrease the latch count
                }

            }

            // Wait for the latch count to reach zero
            try {
                withContext(Dispatchers.IO) {
                    latch.await()
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            // Shutdown the executor service after the task is completed
            executor.shutdown()
        } else {
            completion(tokenDetails.t_balance)
        }
    }

    override fun swapTokenOrCoinOkx(
        toAddress: String?,
        data: String,
        gasPrice: String,
        gasLimit: String,
        amountSend: BigInteger,
        dexCotractAddress: String?,
        completion: (Boolean, String?, String?) -> Unit,
        approveCompletion: (Boolean, String?, String?) -> Unit
    ) {
        val chain = tokenDetails.chain
        val web3 = Web3j.build(HttpService(chain?.rpcURL))

        val myWalletAddress = Wallet.getPublicWalletAddress(
            tokenDetails.chain?.coinType ?: CoinType.ETHEREUM
        )
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val credentials = Credentials.create(chain?.privateKey)
                val contractAddress =
                    tokenDetails.t_address.toString().ifEmpty { DEFAULT_CHAIN_ADDRESS }
                web3.ethGetTransactionCount(
                    myWalletAddress,
                    DefaultBlockParameterName.LATEST
                ).sendAsync().get().apply {
                    val nonce: BigInteger = this.transactionCount

                    val value: BigInteger =
                        (amountSend.toString()).toBigInteger()

                    web3.ethGasPrice().sendAsync().get().apply {
                        val gasPrice = this.gasPrice
                        web3.ethEstimateGas(
                            org.web3j.protocol.core.methods.request.Transaction(
                                myWalletAddress, nonce, gasPrice,
                                BigInteger.ONE, toAddress, value, ""
                            )
                        ).sendAsync().get().apply {
                            val initialGasLimit =
                                if (result.isNullOrBlank()) BigInteger.valueOf(150000) else BigInteger(
                                    this.result.substring(2),
                                    16
                                )

                            val increaseFactor: BigDecimal = BigDecimal.valueOf(1.1)
                            val gaslimit =
                                (initialGasLimit.toBigDecimal() * increaseFactor).toBigInteger()

                            val fee = gasPrice * gaslimit
                            val gasAmount =
                                convertWeiToEther(fee.toString(), tokenDetails.t_decimal!!.toInt())
                            /*val contract = MyContract.load(
                                contractAddress,
                                web3,
                                credentials,
                                gasPrice,
                                gaslimit//BigInteger.valueOf(150000)
                            )*/
                            if (tokenDetails.t_balance > gasAmount) {
                                val rawTransaction: RawTransaction? =
                                    RawTransaction.createTransaction(
                                        nonce,
                                        gasPrice,
                                        gaslimit,
                                        toAddress,
                                        value,
                                        data
                                    )


                                val signedTransaction =
                                    TransactionEncoder.signMessage(
                                        rawTransaction,
                                        chain?.chainIdHex!!.toLong(),
                                        credentials
                                    )
                                val hexValue =
                                    Numeric.toHexString(signedTransaction)

                                //transactionReceipt
                                web3.ethSendRawTransaction(hexValue).sendAsync()
                                    .get()
                                    .apply {
                                        if (this.hasError()) {

                                            completion(
                                                false,
                                                this.error.message,
                                                ""
                                            )

                                        } else {
                                            val transactionHash =
                                                this.transactionHash
                                            val count = 0
                                            getReceipt(
                                                web3,
                                                transactionHash,
                                                count
                                            ) { success, errorMessage, transaction ->
                                                if (transaction?.get()?.status == "0x1") {
                                                    completion(
                                                        true,
                                                        null,
                                                        transactionHash
                                                    )
                                                } else {
                                                    completion(
                                                        false,
                                                        "Swap Failed",
                                                        ""
                                                    )
                                                }
                                            }
                                        }
                                    }
                            } else {
                                completion(
                                    false,
                                    "You don't have enough ${tokenDetails.t_name} (${tokenDetails.t_symbol}) to cover network fees.",
                                    null
                                )
                            }

                        }
                    }


                }

            } catch (e: java.lang.Exception) {

                when (e) {
                    is NoConnectivityException, is UnknownHostException, is ConnectException -> {
                        completion(false, NO_INTERNET_CONNECTION, null)
                    }

                    else -> {
                        completion(false, e.localizedMessage, null)
                    }
                }
                e.printStackTrace()
            }
        }
    }

    override suspend fun getTokenOrCoinNetworkDetailBeforeSend(
        receiverAddress: String?,
        tokenAmount: Double,
        tokenList: List<Tokens>,
        completion: (Boolean, TransferNetworkDetail?, String?) -> Unit
    ) {
        val chain = tokenDetails.chain
        val web3 = Web3j.build(HttpService(chain?.rpcURL))
        val toWalletAddress = try {
            receiverAddress
        } catch (e: Exception) {
            return
        }
        val myWalletAddress = try {
            Wallet.getPublicWalletAddress(
                tokenDetails.chain?.coinType ?: CoinType.ETHEREUM
            )
        } catch (e: Exception) {
            return
        }

        val privateKeyData = chain?.privateKey
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val privateKeyHex: String? = privateKeyData
                val credentials = Credentials.create(privateKeyHex)
                web3.ethGetTransactionCount(
                    credentials.address,
                    DefaultBlockParameterName.LATEST
                ).sendAsync().get().apply {
                    val nonce: BigInteger = this.transactionCount

                    val value: BigInteger =
                        Convert.toWei(tokenAmount.toString(), Convert.Unit.ETHER).toBigInteger()


                    web3.ethGasPrice().sendAsync().get().apply {
                        val gasPrice = this.gasPrice
                        web3.ethEstimateGas(
                            org.web3j.protocol.core.methods.request.Transaction(
                                myWalletAddress, nonce, gasPrice,
                                BigInteger.ONE, toWalletAddress, value, ""
                            )
                        ).sendAsync().get().apply {

                        val initialGasLimit =
                                if (result.isNullOrBlank()) BigInteger.valueOf(21000) else BigInteger(
                                    this.result.substring(2), 16
                                )

                            val increasedGasLimit =
                                if (chain?.chainName == "polygon" && chain.minGasLimit > initialGasLimit) chain.minGasLimit else initialGasLimit


                            val fee = gasPrice * increasedGasLimit
                            val gasAmount =
                                convertWeiToEther(fee.toString(), tokenDetails.t_decimal!!.toInt())

                            if (tokenDetails.t_balance > gasAmount) {
                                completion(
                                    true,
                                    TransferNetworkDetail(
                                        /*initialGasLimit*/increasedGasLimit,
                                        nonce.toInt(),
                                        fee,
                                        gasAmount,
                                        bigIntegerToString(gasPrice),
                                        tokenDetails.t_decimal!!.toInt()
                                    ),
                                    "transactionHash"
                                )
                            } else {
                                completion(
                                    false,
                                    TransferNetworkDetail(
                                        /*initialGasLimit*/increasedGasLimit,
                                        nonce.toInt(),
                                        fee,
                                        gasAmount,
                                        bigIntegerToString(gasPrice),
                                        tokenDetails.t_decimal!!.toInt()
                                    ),
                                    "You don't have enough ${tokenDetails.t_name} (${tokenDetails.t_symbol}) to cover network fees.",
                                )
                            }
                        }


                    }


                }


            } catch (e: Exception) {
                when (e) {
                    is NoConnectivityException, is UnknownHostException, is ConnectException -> {
                        completion(false,null , NO_INTERNET_CONNECTION)
                    }

                    else -> {
                        completion(false, null, e.localizedMessage)
                    }
                }
                e.printStackTrace()
            }

        }
    }

    override suspend fun swapTokenOrCoin(
        toAddress: String?,
        gas: String,
        gasPrice: String,
        data: String,
        tokenAmount: String,
        completion: (Boolean, String?, String?) -> Unit
    ) {
        val chain = tokenDetails.chain ?: return
        val web3 = Web3j.build(HttpService(chain.rpcURL))


        val myWalletAddress = Wallet.getPublicWalletAddress(
            tokenDetails.chain?.coinType ?: CoinType.ETHEREUM
        )


        web3.ethGetTransactionCount(
            myWalletAddress,
            DefaultBlockParameterName.LATEST
        )
            .sendAsync().get().apply {
                val nonce = this.transactionCount
                web3.ethGasPrice().sendAsync().get().apply {
                    val gasPrice = this.gasPrice
                    web3.ethEstimateGas(
                        org.web3j.protocol.core.methods.request.Transaction(
                            myWalletAddress, nonce, gasPrice,
                            BigInteger.ONE, toAddress, BigInteger.ZERO, data
                        )
                    ).sendAsync().get().apply {
                        val initialGasLimit =
                            if (this.result.isNullOrBlank()) BigInteger.valueOf(50000) else BigInteger(
                                this.result.substring(2),
                                16
                            )
                        val increaseFactor: BigDecimal = BigDecimal.valueOf(10.1)
                        val gasLimit: BigInteger =
                            (initialGasLimit.toBigDecimal() * increaseFactor).toBigInteger()
                        try {
                            val rawTransaction = RawTransaction.createTransaction(
                                nonce,
                                gasPrice,
                                gasLimit,
                                toAddress,
                                data
                            )


                            val signedTransaction = TransactionEncoder.signMessage(
                                rawTransaction,
                                chain?.chainIdHex?.toLong()!!,
                                /*credentials*/Credentials.create(chain.privateKey)
                            )
                            val hexValue = Numeric.toHexString(signedTransaction)

                            web3.ethSendRawTransaction(hexValue).sendAsync().get().apply {
                                if (this.hasError()) {
                                    val errorMessage = this.error.message
                                    completion(false, errorMessage, "")
                                } else {
                                    val transactionHash = this.transactionHash

                                    val count = 0
                                    getReceipt(
                                        web3,
                                        transactionHash,
                                        count
                                    ) { success, errorMessage, transaction ->
                                        if (transaction?.get()?.status == "0x1") {
                                            completion(
                                                true,
                                                null, ""
                                            )
                                        } else {
                                            completion(
                                                false,
                                                "Swap Failed", ""
                                            )
                                        }
                                    }
                                }
                            }


                        } catch (e: Exception) {
                            when (e) {
                                is NoConnectivityException, is UnknownHostException, is ConnectException -> {
                                    completion(false, NO_INTERNET_CONNECTION, "")
                                }

                                else -> {
                                    completion(false, e.localizedMessage, "")
                                }
                            }
                            e.printStackTrace()
                        }
                    }
                }
            }
    }


    override suspend fun sendTokenOrCoinWithLavrageFee(
        receiverAddress: String?,
        tokenAmount: Double,
        nonce: BigInteger,
        gasAmount: String,
        gasLimit: BigInteger,
        decimal: Int?,
        tokenList: List<Tokens>,
        completion: (Boolean, String?, String?) -> Unit
    ) {

        val chain = tokenDetails.chain
        val web3 = Web3j.build(HttpService(chain?.rpcURL))

        val contractAddress = tokenDetails.t_address
        val myWalletAddress = Wallet.getPublicWalletAddress(
            tokenDetails.chain?.coinType ?: CoinType.ETHEREUM
        )
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val credentials = Credentials.create(chain?.privateKey)
                web3.ethGetTransactionCount(
                    myWalletAddress,
                    DefaultBlockParameterName.LATEST
                ).sendAsync().get().apply {
                    // val nonce: BigInteger = this.transactionCount


                    val filterTokenChain =
                        (tokenList as MutableList<Tokens>).filter { it.t_address == "" && it.t_type?.lowercase() == tokenDetails.t_type?.lowercase() && it.t_symbol?.lowercase() == tokenDetails.chain?.symbol?.lowercase() }

                    if (filterTokenChain.isNotEmpty()) {
                        val chainBalance = filterTokenChain[0].t_balance
                        val totalGasFee = stringToBigInteger(gasAmount)
                        /** gasLimit*/
                        val gasAmt = convertWeiToEther(totalGasFee.toString(), 18)

                        // val gasAm: BigInteger = Convert.toWei(gasAmount, Convert.Unit.ETHER).toBigInteger()
                        if (chainBalance.toDouble() < gasAmt.toDouble()) {
                            completion(
                                false,
                                "You don't have enough ${filterTokenChain[0].t_name} (${filterTokenChain[0].t_symbol}) to cover network fees.",
                                null
                            )
                        } else {


                            val conversionUnit: Convert.Unit = when (decimal) {
                                18 -> Convert.Unit.ETHER
                                0 -> Convert.Unit.WEI
                                6 -> Convert.Unit.MWEI
                                9 -> Convert.Unit.GWEI
                                12 -> Convert.Unit.SZABO
                                15 -> Convert.Unit.FINNEY
                                21 -> Convert.Unit.KETHER
                                24 -> Convert.Unit.METHER
                                27 -> Convert.Unit.GETHER
                                else -> Convert.Unit.ETHER
                            }


                            val tokenAmount = Convert.toWei(
                                tokenAmount.toString(),
                                conversionUnit// Convert.Unit.ETHER//Convert.Unit.valueOf(/*decimals.toString()Convert.Unit.ETHER.toString())
                            ).toBigInteger()

                            val increaseFactor: BigDecimal = BigDecimal.valueOf(1.1)
                            val gaslimitIncrese =
                                (gasLimit.toBigDecimal() * increaseFactor).toBigInteger()

                            val rawTransaction: RawTransaction =
                                RawTransaction.createEtherTransaction(
                                    nonce,
                                    totalGasFee,
                                    gaslimitIncrese,
                                    receiverAddress,
                                    tokenAmount
                                )

                            val signedMessage: ByteArray =
                                TransactionEncoder.signMessage(
                                    rawTransaction,
                                    chain?.chainIdHex?.toLong()!!,
                                    credentials
                                )
                            val hexValue: String = Numeric.toHexString(signedMessage)
                            web3.ethSendRawTransaction(hexValue).sendAsync().get().apply {
                                if (this.error != null) {
                                    completion(false, this.error.message, "")

                                } else {
                                    val count = 0
                                    getReceipt(
                                        web3,
                                        transactionHash,
                                        count
                                    ) { success, errorMessage, transaction ->
                                        if (transaction?.get()?.status == "0x1") {
                                            completion(
                                                true,
                                                null,
                                                transactionHash
                                            )
                                        } else {
                                            completion(
                                                false,
                                                "Send failed please check after sometime.",
                                                ""
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        completion(false, "no chain found", null)
                    }

                }

            } catch (e: java.lang.Exception) {
                completion(false, e.localizedMessage, "")
                e.printStackTrace()
            }
        }

    }


    override suspend fun signAndSendTranscation(
        toAddress: String?,
        gasLimit: String?,
        gasPrice: String,
        data: String?,
        value: String,
        completion: (Boolean, String?, String?) -> Unit
    ) {
        val txGasPrice = gasPrice
        val chain = tokenDetails.chain ?: return
        val web3 = Web3j.build(HttpService(chain.rpcURL))

        Log.e("TAG", "signAndSendTranscation: here i am")

        val myWalletAddress = Wallet.getPublicWalletAddress(
            tokenDetails.chain?.coinType ?: CoinType.ETHEREUM
        )


        web3.ethGetTransactionCount(
            myWalletAddress,
            DefaultBlockParameterName.LATEST
        ).sendAsync().get().apply {
            val nonce = this.transactionCount
            web3.ethGasPrice().sendAsync().get().apply {
                val gasPrice = this.gasPrice
                web3.ethEstimateGas(
                    org.web3j.protocol.core.methods.request.Transaction(
                        myWalletAddress, nonce, gasPrice,
                        BigInteger.ONE, toAddress, BigInteger.ZERO, data
                    )
                ).sendAsync().get().apply {
                    val initialGasLimit =
                        if (this.result.isNullOrBlank()) BigInteger.valueOf(50000) else BigInteger(
                            this.result.substring(2),
                            16
                        )
                    val increaseFactor: BigDecimal = BigDecimal.valueOf(1.1)
                    val gasLimitDefault: BigInteger =
                        (initialGasLimit.toBigDecimal() * increaseFactor).toBigInteger()

                    try {
                        val newGasLimit: BigInteger =
                            if (gasLimit != "0") hexStringToBigInteger(gasLimit!!) else gasLimitDefault
                        val txValue = hexStringToBigInteger(value)

                        val rawTransaction = RawTransaction.createTransaction(
                            nonce,
                            if (txGasPrice != "0") txGasPrice.toBigInteger() else gasPrice,
                            newGasLimit,
                            toAddress,
                                txValue,
                                data
                        )

                        loge("RawTransaction", Gson().toJson(rawTransaction))

                        val signedTransaction = TransactionEncoder.signMessage(
                            rawTransaction,
                            chain.chainIdHex.toLong(),
                            /*credentials*/Credentials.create(chain.privateKey)
                        )
                        val hexValue = Numeric.toHexString(signedTransaction)

                        loge("signedTransaction", Gson().toJson(hexValue))

                        web3.ethSendRawTransaction(hexValue).sendAsync().get().apply {
                            if (this.hasError()) {
                                val errorMessage = this.error.message
                                completion(false, errorMessage, "")
                            } else {
                                val transactionHash = this.transactionHash

                                loge("transactionHash", transactionHash)

                                val count = 0
                                getReceipt(
                                    web3,
                                    transactionHash,
                                    count
                                ) { success, errorMessage, transaction ->
                                    if (transaction?.get()?.status == "0x1") {
                                        completion(
                                            true,
                                                null, ""
                                            )
                                        } else {
                                            completion(
                                                false,
                                                "Swap Failed", ""
                                            )
                                        }
                                    }
                                }
                            }


                        } catch (e: Exception) {
                            when (e) {
                                is NoConnectivityException, is UnknownHostException, is ConnectException -> {
                                    completion(false, NO_INTERNET_CONNECTION, "")
                                }

                                else -> {
                                    completion(false, e.localizedMessage, "")
                                }
                            }
                        e.printStackTrace()
                    }
                }
            }
        }
    }


    override suspend fun getTransactionHash(
        toAddress: String?,
        gasLimit: String,
        gasPrice: String,
        data: String,
        value: String,
        isGettingTransactionHash: Boolean,
        completion: (Boolean, String?, TransferNetworkDetail?) -> Unit
    ) {
        val txGasPrice = gasPrice
        val chain = tokenDetails.chain ?: return
        val web3 = Web3j.build(HttpService(chain.rpcURL))
        val myWalletAddress = Wallet.getPublicWalletAddress(
            tokenDetails.chain?.coinType ?: CoinType.ETHEREUM
        )

        web3.ethGetTransactionCount(
            myWalletAddress,
            DefaultBlockParameterName.LATEST
        ).sendAsync().get().apply {
            val nonce = this.transactionCount
            web3.ethGasPrice().sendAsync().get().apply {
                val gasPrice = this.gasPrice
                web3.ethEstimateGas(
                    org.web3j.protocol.core.methods.request.Transaction(
                        myWalletAddress, nonce, gasPrice,
                        BigInteger.ONE, toAddress, BigInteger.ZERO, data
                    )
                ).sendAsync().get().apply {
                    val initialGasLimit =
                        if (this.result.isNullOrBlank()) BigInteger.valueOf(50000) else BigInteger(
                            this.result.substring(2),
                            16
                        )
                    val increaseFactor: BigDecimal = BigDecimal.valueOf(1.1)
                    val gasLimitDefault: BigInteger =
                        (initialGasLimit.toBigDecimal() * increaseFactor).toBigInteger()

                    try {
                        val newGasLimit: BigInteger =
                            if (gasLimit != "0") hexStringToBigInteger(gasLimit) else gasLimitDefault
                        val txValue = hexStringToBigInteger(value)

                        val rawTransaction = RawTransaction.createTransaction(
                            nonce,
                            if (txGasPrice != "0") txGasPrice.toBigInteger() else gasPrice,
                            newGasLimit,
                            toAddress,
                            txValue,
                            data
                        )



                        loge("RawTransaction", Gson().toJson(rawTransaction))

                        val signedTransaction = TransactionEncoder.signMessage(
                            rawTransaction,
                            chain.chainIdHex.toLong(),
                            /*credentials*/Credentials.create(chain.privateKey)
                        )

                        // TransactionEncoder.signMessage()

                        val signedMessage = Numeric.toHexString(signedTransaction)

                        loge("signedTransaction", Gson().toJson(signedMessage))

                        if (isGettingTransactionHash) {

                            web3.ethSendRawTransaction(signedMessage).sendAsync().get().apply {
                                if (this.hasError()) {
                                    val errorMessage = this.error.message
                                    loge("Transaction Error :", errorMessage)
                                    completion(
                                        false,
                                        errorMessage,
                                        TransferNetworkDetail(
                                            newGasLimit,
                                            nonce.toInt(),
                                            gasLimitDefault,
                                            txValue.toString(),
                                            bigIntegerToString(gasPrice),
                                            tokenDetails.t_decimal!!.toInt()
                                        )
                                    )
                                } else {

                                    val transactionHash = this.transactionHash
                                    completion(
                                        true,
                                        transactionHash,
                                        TransferNetworkDetail(
                                            newGasLimit,
                                            nonce.toInt(),
                                            gasLimitDefault,
                                            txValue.toString(),
                                            bigIntegerToString(gasPrice),
                                            tokenDetails.t_decimal!!.toInt()
                                        )
                                    )
                                }

                            }

                        } else {
                            /**
                             * This part haxValue return the signing message
                             * */

                            completion(
                                true,
                                signedMessage,
                                TransferNetworkDetail(
                                    newGasLimit,
                                    nonce.toInt(),
                                    gasLimitDefault,
                                    txValue.toString(),
                                    bigIntegerToString(gasPrice),
                                    tokenDetails.t_decimal!!.toInt()
                                )
                            )
                        }
                    } catch (e: Exception) {
                        when (e) {
                            is NoConnectivityException, is UnknownHostException, is ConnectException -> {
                                completion(false, NO_INTERNET_CONNECTION, null)
                            }
                            else -> {
                                completion(false, e.localizedMessage, null)
                            }
                        }
                        e.printStackTrace()
                    }
                }
            }
        }
    }


}
