package com.app.plutope.data.blockChainFunction

import com.app.plutope.model.Tokens
import com.app.plutope.model.Wallet
import com.app.plutope.network.NoConnectivityException
import com.app.plutope.networkConfig.Chain
import com.app.plutope.networkConfig.UserTokenData
import com.app.plutope.ui.fragment.transactions.send.send_coin.TransferNetworkDetail
import com.app.plutope.utils.bigIntegerToString
import com.app.plutope.utils.coinTypeEnum.CoinType
import com.app.plutope.utils.constant.NO_INTERNET_CONNECTION
import com.app.plutope.utils.contractWrapperClass.MyContract
import com.app.plutope.utils.convertWeiToEther
import com.app.plutope.utils.getReceipt
import com.app.plutope.utils.hexStringToBigInteger
import com.app.plutope.utils.loge
import com.app.plutope.utils.stringToBigInteger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
import java.math.MathContext
import java.math.RoundingMode
import java.net.ConnectException
import java.net.UnknownHostException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class TokenFunctions(private val tokenDetails: Tokens) : BlockchainFunctions {

    override suspend fun getGasFee(completion: (BigInteger?, String, BigInteger, BigInteger) -> Unit) {
        val chain = tokenDetails.chain
        val web3 = Web3j.build(HttpService(chain?.rpcURL))
        val receiverAddress: String = "0x37Ec14eF9C13C2a07c2cD1ed3f5869D42d9a6596"
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


        /*  web3.ethEstimateGas(
              org.web3j.protocol.core.methods.request.Transaction(
                  myWalletAddress, nonce, gasPrice,
                  BigInteger.ONE, toWalletAddress, value, ""
              )
          ).sendAsync().get().apply {
              web3.ethGasPrice().sendAsync().get().apply {
                  val gasPrice = this.gasPrice
                  completion(gasPrice.toString())

              }
          }*/



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
                            // Log.e("TAG", "result initialGasLimit token functione: ${this.result.substring(2)}")
                            val initialGasLimit =
                                if (result.isNullOrBlank()) BigInteger.valueOf(21000) else BigInteger(
                                    this.result.substring(2),
                                    16
                                )

                            //  val increaseFactor: BigDecimal = BigDecimal.valueOf(1.1)
                            val increaseFactor: BigDecimal = BigDecimal.valueOf(1.1)
                            val gaslimit =
                                (initialGasLimit.toBigDecimal() * increaseFactor).toBigInteger()

                            val fee = gasPrice * gaslimit
                            val gasAmount =
                                convertWeiToEther(fee.toString(), tokenDetails.t_decimal!!.toInt())
                            completion(fee, "$nonce", gaslimit, gasPrice)

                        }


                    }


                }


            } catch (e: Exception) {

                when (e) {
                    is NoConnectivityException, is UnknownHostException, is ConnectException -> {
                        //       completion(0.toBigInteger())
                    }

                    else -> {
                        //        completion(0.toBigInteger())
                    }
                }
                e.printStackTrace()
            }

        }

    }


    override fun sendTokenOrCoin(
        receiverAddress: String?,
        tokenAmount: Double,
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
                    val nonce: BigInteger = this.transactionCount
                    val value: BigInteger =
                        Convert.toWei(tokenAmount.toString(), Convert.Unit.ETHER).toBigInteger()
                    web3.ethGasPrice().sendAsync().get().apply {
                        val gasPrice = this.gasPrice
                        web3.ethEstimateGas(
                            org.web3j.protocol.core.methods.request.Transaction(
                                myWalletAddress,
                                nonce,
                                gasPrice,
                                BigInteger.ONE,
                                receiverAddress,
                                value,
                                ""
                            )
                        ).sendAsync().get().apply {
                            val initialGasLimit =
                                if (result.isNullOrBlank()) BigInteger.valueOf(150000) else BigInteger(
                                    this.result.substring(2),
                                    16
                                )
                            val increaseFactor: BigDecimal = BigDecimal.valueOf(10.1)
                            val gaslimit =
                                (initialGasLimit.toBigDecimal() * increaseFactor).toBigInteger()

                            val fee = gasPrice * gaslimit
                            val contract = MyContract.load(
                                contractAddress,
                                web3,
                                credentials,
                                gasPrice,
                                gaslimit
                            )


                            contract.decimals().sendAsync().get().apply {


                                val decimals = this.toInt()
                                val gasAmount = convertWeiToEther(fee.toString(), 18)

                                val filterTokenChain =
                                    (tokenList as MutableList<Tokens>).filter { it.t_address == "" && it.t_type?.lowercase() == tokenDetails.t_type?.lowercase() && it.t_symbol?.lowercase() == tokenDetails.chain?.symbol?.lowercase() }
                                loge("filterTokenChain", "==> $filterTokenChain")
                                if (filterTokenChain.isNotEmpty()) {
                                    val chainBalance = filterTokenChain[0].t_balance

                                    loge(
                                        "condition",
                                        "==> ${tokenDetails.t_balance}  :: ${chainBalance} > $gasAmount"
                                    )




                                    if (tokenDetails.t_balance.toBigDecimal() < gasAmount.toBigDecimal()) {
                                        completion(
                                            false,
                                            "You don't have enough ${filterTokenChain[0].t_name} (${filterTokenChain[0].t_symbol}) to cover network fees.",
                                            null
                                        )
                                    } else {

                                        val conversionUnit: Convert.Unit = when (decimals) {
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

                                        contract.transfer(receiverAddress, tokenAmount)
                                            .encodeFunctionCall().apply {

                                                val rawTransaction: RawTransaction? =
                                                    RawTransaction.createTransaction(
                                                        nonce,
                                                        gasPrice,
                                                        gaslimit,//BigInteger.valueOf(150000),
                                                        contractAddress, BigInteger.ZERO,
                                                        this
                                                    )

                                                val signedTransaction =
                                                    TransactionEncoder.signMessage(
                                                        rawTransaction,
                                                        chain?.chainIdHex?.toLong()!!,
                                                        credentials
                                                    )
                                                val hexValue =
                                                    Numeric.toHexString(signedTransaction)

                                                //transactionReceipt
                                                web3.ethSendRawTransaction(hexValue).sendAsync()
                                                    .get()
                                                    .apply {
                                                        if (this.hasError()) {
                                                            val errorMessage = this.error.message
                                                            completion(
                                                                false,
                                                                this.error.message,
                                                                ""
                                                            )
                                                            // Handle the error
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
                                                                        "Send failed please check after sometime.",
                                                                        ""
                                                                    )
                                                                }
                                                            }
                                                            //completion(true, null, transactionHash)
                                                            // Transaction successful
                                                        }
                                                    }


                                            }
                                    }
                                } else {
                                    completion(false, "no chain found", null)
                                }


                            }
                        }

                    }
                }

            } catch (e: java.lang.Exception) {
                completion(false, e.localizedMessage, "")
                e.printStackTrace()
            }
        }

    }

    private var storedDecimal: Int? = 18
    override suspend fun getDecimal(completion: (Int?) -> Unit) {
        val chain = tokenDetails.chain
        val walletAddress =
            Wallet.getPublicWalletAddress(tokenDetails.chain?.coinType ?: CoinType.ETHEREUM)

        val httpClientBuilder = OkHttpClient.Builder()
        httpClientBuilder.connectTimeout(
            5,
            TimeUnit.MINUTES
        )
        httpClientBuilder.readTimeout(5, TimeUnit.MINUTES)
        httpClientBuilder.writeTimeout(5, TimeUnit.MINUTES)

        val httpClient = httpClientBuilder.build()
        // val web3 = Web3j.build(HttpService(chain?.rpcURL))
        val web3 = Web3j.build(HttpService(chain?.rpcURL, httpClient))
        val contractAddress = tokenDetails.t_address
        val executorService: ExecutorService =
            Executors.newSingleThreadExecutor() // Create a single-threaded executor

        executorService.execute {
            try {
                val credentials = Credentials.create(chain?.privateKey)



                web3.ethGetTransactionCount(
                    walletAddress,
                    DefaultBlockParameterName.LATEST
                ).sendAsync().apply {
                    // val nonce: BigInteger = this.transactionCount


                    web3.ethGasPrice().sendAsync().get().apply {
                        val gasPrice = this.gasPrice
                        val contract = MyContract.load(
                            contractAddress,
                            web3,
                            credentials,
                            gasPrice,
                            BigInteger.valueOf(50000)
                            //BigInteger.valueOf(150000)
                        )
                        try {
                            contract.decimals().sendAsync().get().apply {
                                val decimals = this.toInt()
                                storedDecimal = decimals
                                completion(decimals)

                            }
                        } catch (e: Exception) {
                            completion(storedDecimal)
                            e.printStackTrace()

                        }

                    }
                }

            } catch (e: java.lang.Exception) {
                completion(storedDecimal)
                e.printStackTrace()
            }
        }

        executorService.shutdown()

    }



    override suspend fun getBalance(completion: (String?) -> Unit) {
        val chain = tokenDetails.chain
        val walletAddress =
            Wallet.getPublicWalletAddress(tokenDetails.chain?.coinType ?: CoinType.ETHEREUM)
        val web3 = Web3j.build(HttpService(chain?.rpcURL))
        val contractAddress = tokenDetails.t_address

        val executorService: ExecutorService =
            Executors.newSingleThreadExecutor() // Create a single-threaded executor

        executorService.execute {
            try {
                val credentials = Credentials.create(chain?.privateKey)

                web3.ethGetTransactionCount(
                    walletAddress,
                    DefaultBlockParameterName.LATEST
                ).sendAsync().get().apply {

                    web3.ethGasPrice().sendAsync().get().apply {
                        val gasPrice = this.gasPrice
                        val contract = MyContract.load(
                            contractAddress,
                            web3,
                            credentials,
                            gasPrice,
                            BigInteger.valueOf(50000)
                            //BigInteger.valueOf(150000)
                        )
                        try {
                            contract.decimals().sendAsync().get().apply {
                                val decimals = this.toInt()

                                contract.balanceOf(walletAddress).sendAsync().get().apply {

                                    val bal = this
                                    val etherValue = BigDecimal(bal).divide(
                                        BigDecimal(10).pow(decimals),
                                        MathContext.DECIMAL128
                                    )

                                    UserTokenData.update(
                                        symbol = tokenDetails.t_symbol ?: "",
                                        balance = etherValue.toString()
                                    )

                                    completion(
                                        etherValue.setScale(decimals, RoundingMode.DOWN).toString()
                                    )


                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            completion("0.0")
                        }
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                completion("0.0")
            }
        }
        executorService.shutdown()
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
                val contractAddress = tokenDetails.t_address
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
                                    BigInteger.ONE, toAddress, amountSend, ""
                                )
                            ).sendAsync().get().apply {
                                val initialGasLimit =
                                    if (this.result.isNullOrBlank()) BigInteger.valueOf(150000) else BigInteger(
                                        this.result.substring(2),
                                        16
                                    )
                                val contract = MyContract.load(
                                    contractAddress,
                                    web3,
                                    credentials,
                                    gasPrice,
                                    initialGasLimit//BigInteger.valueOf(150000)
                                )

                                approveToken(
                                    contract,
                                    dexCotractAddress,
                                    amountSend,
                                    gasPrice,
                                    web3,
                                    myWalletAddress,
                                    contractAddress,
                                    chain,
                                    credentials,
                                    initialGasLimit, toAddress.toString(),
                                    data,
                                ) { success, errorMessage ->
                                    if (success) {
                                        completion(true, null, "")
                                    } else {
                                        completion(false, errorMessage, "")
                                    }
                                }

                            }
                        }
                    }

            } catch (e: Exception) {
                when (e) {
                    is NoConnectivityException, is UnknownHostException , is ConnectException-> {
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
                                    this.result.substring(2),
                                    16
                                )
                            /* val increaseFactor: BigDecimal = BigDecimal.valueOf(1.1)
                             val gaslimit =
                                 (initialGasLimit.toBigDecimal() * increaseFactor).toBigInteger()*/


                            val increasedGasLimit =
                                if (chain?.chainName == "polygon" && chain.minGasLimit > initialGasLimit) chain.minGasLimit else initialGasLimit


                            // val fee = gasPrice * initialGasLimit
                            val fee = gasPrice * increasedGasLimit
                            val gasAmount =
                                convertWeiToEther(fee.toString(), tokenDetails.t_decimal!!.toInt())

                            if (tokenDetails.t_balance > gasAmount) {
                                completion(
                                    true,
                                    TransferNetworkDetail(
                                        /*initialGasLimit,*/increasedGasLimit,
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
                                        /* initialGasLimit,*/increasedGasLimit,
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
                        completion(false, null, NO_INTERNET_CONNECTION)
                    }

                    else -> {
                        completion(false, null, e.localizedMessage)
                    }
                }
                e.printStackTrace()
            }

        }
    }


    private fun approveToken(
        contract: MyContract,
        dexContractAddress: String?,
        amount: BigInteger,
        gasPrice: BigInteger,
        web3: Web3j,
        myWalletAddress: String?,
        contractAddress: String?,
        chain: Chain?,
        credentials: Credentials,
        gasLimit: BigInteger,
        toAddress: String,
        data: String,
        completion: (Boolean, String?) -> Unit
    ) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val nonce =
                    web3.ethGetTransactionCount(myWalletAddress, DefaultBlockParameterName.LATEST)
                        .sendAsync().get().transactionCount


                val approveFunction =
                    contract.approve(dexContractAddress, amount).encodeFunctionCall()

                val rawApproveTransaction = RawTransaction.createTransaction(
                    nonce,
                    gasPrice,
                    gasLimit,//BigInteger.valueOf(50000),
                    contractAddress,
                    BigInteger.ZERO,
                    approveFunction
                )

                val signedApproveTransaction = TransactionEncoder.signMessage(
                    rawApproveTransaction,
                    chain?.chainIdHex?.toLong()!!,
                    credentials
                )
                val hexApproveValue = Numeric.toHexString(signedApproveTransaction)

                web3.ethSendRawTransaction(hexApproveValue).sendAsync().get().apply {
                    if (this.hasError()) {
                        val errorMessage = this.error.message
                        completion(false, errorMessage)
                    } else {
                        val count = 0
                        getReceipt(
                            web3,
                            this.transactionHash,
                            count
                        ) { success, errorMessage, transaction ->
                            if (transaction?.get()?.status == "0x1") {
                                swapContractToken(web3, myWalletAddress, chain, credentials,
                                    toAddress, data, completion = { sucess, errormsg ->
                                        if (sucess) {
                                            completion(
                                                true,
                                                null
                                            )
                                        } else {
                                            completion(false, errorMessage)
                                        }
                                    })

                            } else {
                                completion(
                                    false,
                                    "Swap Failed"
                                )
                            }
                        }

                    }
                }
            } catch (e: Exception) {
                when (e) {
                    is NoConnectivityException, is UnknownHostException , is ConnectException-> {
                        completion(false, NO_INTERNET_CONNECTION)
                    }

                    else -> {
                        completion(false, e.localizedMessage)
                    }
                }
                e.printStackTrace()
            }
        }
    }


    private fun swapContractToken(
        web3: Web3j,
        myWalletAddress: String?,
        chain: Chain?,
        credentials: Credentials,
        toAddress: String,
        data: String,
        completion: (Boolean, String?) -> Unit
    ) {

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
                        val increaseFactor: BigDecimal = BigDecimal.valueOf(1.1)
                        val gasLimit: BigInteger = (initialGasLimit.toBigDecimal() * increaseFactor).toBigInteger()
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
                                credentials
                            )
                            val hexValue = Numeric.toHexString(signedTransaction)

                            web3.ethSendRawTransaction(hexValue).sendAsync().get().apply {
                                if (this.hasError()) {
                                    val errorMessage = this.error.message
                                    completion(false, errorMessage)
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
                                                null
                                            )
                                        } else {
                                            completion(
                                                false,
                                                "Swap Failed"
                                            )
                                        }
                                    }
                                }
                            }


                        } catch (e: Exception) {
                            when (e) {
                                is NoConnectivityException, is UnknownHostException , is ConnectException-> {
                                    completion(false, NO_INTERNET_CONNECTION)
                                }

                                else -> {
                                    completion(false, e.localizedMessage)
                                }
                            }
                            e.printStackTrace()
                        }
                    }
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
                                chain.chainIdHex.toLong(),
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


    override suspend fun signAndSendTranscation(
        toAddress: String?,
        gasLimit: String?,
        gasPrice: String,
        data: String?,
        value: String,
        completion: (Boolean, String?, String?) -> Unit
    ) {
        val chain = tokenDetails.chain ?: return
        val web3 = Web3j.build(HttpService(chain.rpcURL))


        val myWalletAddress = Wallet.getPublicWalletAddress(
            tokenDetails.chain?.coinType ?: CoinType.ETHEREUM
        )

        loge("signAndSendTranscation", "$chain")

        web3.ethGetTransactionCount(
            myWalletAddress,
            DefaultBlockParameterName.LATEST
        )
            .sendAsync().get().apply {
                val nonce = this.transactionCount
                web3.ethGasPrice().sendAsync().get().apply {
//                    val gasPrice = this.gasPrice
                    var txGasPrice: BigInteger = gasPrice.toBigInteger();

                    if (txGasPrice == null || txGasPrice == 0.toBigInteger()) {
                        txGasPrice = this.gasPrice
                    }

                    web3.ethEstimateGas(
                        org.web3j.protocol.core.methods.request.Transaction(
                            myWalletAddress, nonce, txGasPrice,
                            BigInteger.ONE, toAddress, BigInteger.ZERO, data
                        )
                    ).sendAsync().get().apply {
//                        val initialGasLimit =
//                            if (this.result.isNullOrBlank()) BigInteger.valueOf(50000) else BigInteger(
//                                this.result.substring(2),
//                                16
//                            )


                        val txGasLimit: BigInteger = hexStringToBigInteger(gasLimit!!)

                        /*  val increaseFactor: BigDecimal = BigDecimal.valueOf(1.1)
                          val increaseGasLimit: BigInteger = (txGasLimit.toBigDecimal() * increaseFactor).toBigInteger()

  */
                        val txValue = hexStringToBigInteger(value)

                        try {
                            val rawTransaction = RawTransaction.createTransaction(
                                nonce,
                                txGasPrice,
                                txGasLimit,
                                toAddress,
                                txValue,
                                data
                            )


                            val signedTransaction = TransactionEncoder.signMessage(
                                rawTransaction,
                                chain.chainIdHex.toLong(),
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
                                    ) { _, _, transaction ->
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
                    val value: BigInteger =
                        Convert.toWei(tokenAmount.toString(), Convert.Unit.ETHER).toBigInteger()

                    val totalGasFee = stringToBigInteger(gasAmount)

                    val gasAmt = convertWeiToEther(totalGasFee.toString(), 18)
                    // val gasPrice = this.gasPrice

                    val initialGasLimit =
                        if (result.isNullOrBlank()) BigInteger.valueOf(150000) else BigInteger(
                            this.result.substring(
                                2
                            ), 16
                        )


                    //  val increaseFactor: BigDecimal = BigDecimal.valueOf(10.1)
                    //   val gaslimit = (initialGasLimit.toBigDecimal() * increaseFactor).toBigInteger()

                    //  val fee = gasPrice * gaslimit

                    val increaseFactor: BigDecimal = BigDecimal.valueOf(1.1)
                    val gaslimitIncrese = (gasLimit.toBigDecimal() * increaseFactor).toBigInteger()

                    val contract = MyContract.load(
                        contractAddress,
                        web3,
                        credentials,
                        totalGasFee,
                        gaslimitIncrese
                    )

                    contract.decimals().sendAsync().get().apply {
                        val decimals = this.toInt()
                        // val gasAmount = convertWeiToEther(fee.toString(), 18)

                        val filterTokenChain =
                            (tokenList as MutableList<Tokens>).filter { it.t_address == "" && it.t_type?.lowercase() == tokenDetails.t_type?.lowercase() && it.t_symbol?.lowercase() == tokenDetails.chain?.symbol?.lowercase() }

                        if (filterTokenChain.isNotEmpty()) {
                            val chainBalance = filterTokenChain[0].t_balance
                            if (chainBalance.toDouble() < gasAmt.toDouble()) {
                                completion(
                                    false,
                                    "You don't have enough ${filterTokenChain[0].t_name} (${filterTokenChain[0].t_symbol}) to cover network fees.",
                                    null
                                )
                            } else {

                                val conversionUnit: Convert.Unit = when (decimals) {
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

                                contract.transfer(receiverAddress, tokenAmount)
                                    .encodeFunctionCall().apply {

                                        val rawTransaction: RawTransaction? =
                                            RawTransaction.createTransaction(
                                                nonce,
                                                totalGasFee,
                                                gaslimitIncrese,//BigInteger.valueOf(150000),
                                                contractAddress,
                                                BigInteger.ZERO,
                                                this
                                            )

                                        val signedTransaction =
                                            TransactionEncoder.signMessage(
                                                rawTransaction,
                                                chain?.chainIdHex?.toLong()!!,
                                                credentials
                                            )
                                        val hexValue =
                                            Numeric.toHexString(signedTransaction)

                                        //transactionReceipt
                                        web3.ethSendRawTransaction(hexValue).sendAsync()
                                            .get()
                                            .apply {
                                                if (this.hasError()) {
                                                    val errorMessage = this.error.message
                                                    completion(
                                                        false,
                                                        this.error.message,
                                                        ""
                                                    )
                                                    // Handle the error
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
                                                                "Send failed please check after sometime.",
                                                                ""
                                                            )
                                                        }
                                                    }
                                                    //completion(true, null, transactionHash)
                                                    // Transaction successful
                                                }
                                            }


                                    }
                            }
                        } else {
                            completion(false, "no chain found", null)
                        }


                    }


                }

            } catch (e: java.lang.Exception) {
                completion(false, e.localizedMessage, "")
                e.printStackTrace()
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
        completion(false, "", null)
    }


}

