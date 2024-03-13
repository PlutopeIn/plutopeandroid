package com.app.plutope.data.repository

import com.app.plutope.data.database.TokensDao
import com.app.plutope.model.ModelActiveWalletToken
import com.app.plutope.model.TokenListImageModel
import com.app.plutope.model.Tokens
import com.app.plutope.model.Wallet
import com.app.plutope.model.WalletTokens
import com.app.plutope.network.ApiHelper
import com.app.plutope.network.NoConnectivityException
import com.app.plutope.utils.constant.NO_INTERNET_CONNECTION
import com.app.plutope.utils.constant.responseServerError
import com.app.plutope.utils.constant.serverErrorMessage
import com.app.plutope.utils.extras.PreferenceHelper
import com.app.plutope.utils.loge
import com.app.plutope.utils.network.NetworkState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.net.UnknownHostException
import javax.inject.Inject

class TokensRepo @Inject constructor(
    private val tokensDao: TokensDao,
    private val apiHelper: ApiHelper
) {

    val activeTokenList = mutableListOf<ModelActiveWalletToken>()
    suspend fun insertAllTokens(
        tokenList: MutableList<Tokens>
    ): NetworkState<Tokens?> {
        return try {

            tokensDao.insertAll(tokenList)

            NetworkState.Success("", Tokens())

        } catch (e: Exception) {
            e.printStackTrace()
            if(e is NoConnectivityException || e is UnknownHostException) {
                NetworkState.SessionOut("", NO_INTERNET_CONNECTION)
            }else {
                NetworkState.Error(serverErrorMessage)
            }
        }

    }


    fun getAllIsEnableToken(isEnable: Int): List<Tokens> {
        return tokensDao.getTokenList(isEnable)
    }

    fun getAllDisableTokenList(): List<Tokens> {
        return tokensDao.getAllDisableTokens(Wallet.walletObject.w_id)
    }

    fun getAllTokenList(): List<Tokens> {
        return tokensDao.getAlleTokenList()
    }

    fun getAllTokensWithBalance(): List<Tokens> {
        return tokensDao.getTokensWithBalance(Wallet.walletObject.w_id)
    }

    suspend fun updateAllTokens(
        tokenList: MutableList<Tokens>?
    ): NetworkState<Tokens?> {
        return try {
            tokensDao.updateTokens(tokenList as List<Tokens>)
            NetworkState.Success("", Tokens())

        } catch (e: Exception) {
            e.printStackTrace()
            NetworkState.Error(serverErrorMessage)
        }

    }

    suspend fun updateAllTokenBalanceZero(
    ): NetworkState<Tokens?> {
        return try {
            tokensDao.updateAllTokenBalanceZero()
            NetworkState.Success("", Tokens())

        } catch (e: Exception) {
            e.printStackTrace()
            NetworkState.Error(serverErrorMessage)
        }

    }
    suspend fun updateToken(
        token: Tokens
    ): NetworkState<Tokens?> {
        return try {

            tokensDao.updateToken(token)
            val count=tokensDao.getCountTokenIdSpecificWallet(Wallet.walletObject.w_id,token.t_pk)
            if(count==0) {
                tokensDao.insertWalletToken(
                    WalletTokens(
                        Wallet.walletObject.w_id,
                        token.t_pk,
                        true
                    )
                )
            }
            NetworkState.Success(count.toString(), token)

        } catch (e: Exception) {
            e.printStackTrace()
            NetworkState.Error(serverErrorMessage)
        }

    }


    suspend fun updateWalletToken(
        token: Tokens
    ): NetworkState<Tokens?> {
        return try {
            if (token.isCustomTokens == true) {
                tokensDao.deleteCustomToken(token.t_pk)
            } else {
                tokensDao.updateWalletToken(token.t_pk, Wallet.walletObject.w_id)
            }
            NetworkState.Success("", token)

        } catch (e: Exception) {
            e.printStackTrace()
            NetworkState.Error(serverErrorMessage)
        }

    }


    suspend fun insertAllWalletTokens(
        walletTokenList: MutableList<WalletTokens>
    ): NetworkState<WalletTokens?> {
        return try {

            tokensDao.insertAllWalletTokens(walletTokenList)

            NetworkState.Success("", WalletTokens(0, 0))

        } catch (e: Exception) {
            e.printStackTrace()
            NetworkState.Error(serverErrorMessage)
        }

    }

    suspend fun getAllSpecificWalletTokens(walletId: Int): Flow<NetworkState<List<Tokens?>>> {
        return flow {
            val localData: MutableList<Tokens> = mutableListOf()
            localData.addAll(tokensDao.getAllSpecificWalletTokens(walletId).first())

            if (localData.isNotEmpty()) {
                emit(NetworkState.Success("", localData))
            } else {
                emit(NetworkState.Error("Failed to load"))
            }
        }
    }


    suspend fun insertNewToken(
        token: Tokens
    ): NetworkState<Tokens?> {
        return try {

            tokensDao.insert(token)
            val lastInsertedToken = tokensDao.getLastInsertedToken()
            tokensDao.insertWalletToken(
                WalletTokens(
                    Wallet.walletObject.w_id,
                    lastInsertedToken.t_pk,
                    true
                )
            )
            NetworkState.Success("", lastInsertedToken)

        } catch (e: Exception) {
            e.printStackTrace()
            NetworkState.Error(serverErrorMessage)
        }

    }


    suspend fun getTokenFromCoinGecko(): NetworkState<MutableList<Tokens>?> {
        return try {
            val response = apiHelper.executeCoinListApi()
            val result = response.body()
            if (response.code() == responseServerError) {
                NetworkState.Error(serverErrorMessage)
            } else {
                if (response.isSuccessful) {


                    loge(
                        "getTokenFromCoinGecko",
                        "getTokenFromCoinGecko: ${activeTokenList.map { it.symbol }}"
                    )

                    val tokens: MutableList<Tokens> = mutableListOf()
                    val jsonArray = JSONArray(result?.string())
                    val supportedChain = arrayListOf(
                        "binance-smart-chain",
                        "ethereum",
                        "polygon-pos",
                        "okex-chain",
                        "bitcoin"
                    )
                    val nativeCoins =
                        arrayListOf("OKT Chain", "Ethereum", "Polygon", "BNB", "Bitcoin")
                    repeat(jsonArray.length()) {
                        val jObj = jsonArray.getJSONObject(it)
                        val name = jObj.getString("name")
                        val symbol = jObj.getString("symbol")
                        val id = jObj.getString("id")

                        loge("jsonArraySymbol", symbol)


                        //  loge("MatchSymbole","${activeTokenBySymbol.contains(symbol)}")

                        if (nativeCoins.contains(name)) {
                            val tokenModel = Tokens()
                            tokenModel.apply {
                                tokenId = id
                                t_name = name
                                t_symbol = symbol.uppercase()
                                isEnable = true
                                t_address = ""
                                t_type = when (jObj.getString("name")) {
                                    "Ethereum" -> "ERC20"
                                    "Polygon" -> "POLYGON"
                                    "BNB" -> "BEP20"
                                    "OKT Chain" -> "KIP20"
                                    "Bitcoin" -> "BTC"
                                    else -> ""
                                }
                            }

                            tokens.add(tokenModel)
                           //return@repeat
                        }

                        val platformObj = jObj.getJSONObject("platforms")
                        val keys = platformObj.keys()

                        keys.forEach { key ->
                            val objChain = platformObj.getString(key)

                            if (supportedChain.contains(key)) {
                                if (objChain.isNotEmpty()) {
                                    val tokenModel = Tokens()

                                    tokenModel.apply {
                                        tokenId = jObj.getString("id")
                                        t_name = jObj.getString("name")
                                        t_symbol = jObj.getString("symbol").uppercase()
                                        isEnable = false
                                        t_address = objChain.toString()
                                        t_type = when (key) {
                                            "binance-smart-chain" -> "BEP20"
                                            "ethereum" -> "ERC20"
                                            "polygon-pos" -> "POLYGON"
                                            "okex-chain" -> "KIP20"
                                            "bitcoin" -> "BTC"
                                            else -> ""
                                        }

                                    }
                                    tokens.add(
                                        tokenModel
                                    )

                                }
                            }
                        }
                    }

                    // private val tokensDao: TokensDao

                    NetworkState.Success("", tokens)
                } else {
                    NetworkState.Error("")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if(e is NoConnectivityException || e is UnknownHostException) {
                NetworkState.SessionOut("", NO_INTERNET_CONNECTION)
            }else {
                NetworkState.Error(serverErrorMessage)
            }
        }

    }


    suspend fun executeTokenListImages(
    ): NetworkState<List<TokenListImageModel>?> {
        return try {
            val response = apiHelper.executeTokenImageListApi()
            val result = response.body()
            if (response.code() == responseServerError) {
                NetworkState.Error(serverErrorMessage)
            } else {
                if (response.isSuccessful && result != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        tokensDao.insertTokenImageTokens(result)
                    }
                    NetworkState.Success("", result)
                } else {
                    NetworkState.Error("")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if(e is NoConnectivityException || e is UnknownHostException) {
                NetworkState.SessionOut("", NO_INTERNET_CONNECTION)
            }else {
                NetworkState.Error(serverErrorMessage)
            }
        }

    }

    fun getAllTokenImageList(): List<TokenListImageModel> {
        return tokensDao.getAlleTokenImageList()
    }

    fun getTokenByContractAddress(contractAddress: List<String>): List<Tokens> {
        return tokensDao.getTokenByContractAddress(contractAddress)
    }


    suspend fun getBitcoinBalance(address: String): NetworkState<String?> {
        return try {
            val response = apiHelper.getBitcoinWalletBalance(address)
            val result = response.body()
            if (response.code() == responseServerError) {
                NetworkState.Error(serverErrorMessage)
            } else {
                if (response.isSuccessful) {
                    val balance = result?.string()?.let { JSONObject(it) }
                    NetworkState.Success("", balance?.getString("balance"))
                } else {
                    NetworkState.Error("")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is NoConnectivityException || e is UnknownHostException) {
                NetworkState.SessionOut("", NO_INTERNET_CONNECTION)
            } else {
                NetworkState.Error(serverErrorMessage)
            }
        }

    }


    suspend fun registerWallet(address: String, fcmToken: String): NetworkState<String?> {
        return try {
            val response = apiHelper.registerWallet(
                address,
                PreferenceHelper.getInstance().deviceId!!,
                fcmToken
            )
            val result = response.body()
            if (response.code() == responseServerError) {
                NetworkState.Error(serverErrorMessage)
            } else {
                if (response.isSuccessful) {
                  //  val data = result?.string()?.let { JSONObject(it) }

                    NetworkState.Success("", "")
                } else {
                    NetworkState.Error("")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is NoConnectivityException || e is UnknownHostException) {
                NetworkState.SessionOut("", NO_INTERNET_CONNECTION)
            } else {
                NetworkState.Error(serverErrorMessage)
            }
        }

    }

    suspend fun setWalletActive(address: String, receiverAddress: String): NetworkState<String?> {
        return try {
            val response = apiHelper.setWalletActive(address, receiverAddress)
            // val result = response.body()
            if (response.code() == responseServerError) {
                NetworkState.Error(serverErrorMessage)
            } else {
                if (response.isSuccessful) {
                    PreferenceHelper.getInstance().isActiveWallet = true
                    NetworkState.Success("", "")
                } else {
                    NetworkState.Error("")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is NoConnectivityException || e is UnknownHostException) {
                NetworkState.SessionOut("", NO_INTERNET_CONNECTION)
            } else {
                NetworkState.Error(serverErrorMessage)
            }
        }

    }


    suspend fun sendBTCTransaction(
        privateKey: String,
        value: String,
        toAddress: String,
        env: String,
        fromAddress: String
    ): NetworkState<String?> {
        return try {
            val response =
                apiHelper.sendBTCTransaction(privateKey, value, toAddress, env, fromAddress)
            val result = response.body()

            //  val data = JSONObject(result?.string())
            if (response.code() == responseServerError) {
                NetworkState.Error(serverErrorMessage)
            } else {
                if (response.isSuccessful) {
                    NetworkState.Success("", "Transaction Successful")
                } else {
                    val error = response.errorBody()?.charStream()?.buffered()?.readLine()
                    val jsonObject = JSONObject(error!!)
                    val statusCode = jsonObject.getInt("status")
                    val message = jsonObject.getString("message")
                    NetworkState.Error(message)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is NoConnectivityException || e is UnknownHostException) {
                NetworkState.SessionOut("", NO_INTERNET_CONNECTION)
            } else {
                NetworkState.Error(serverErrorMessage)
            }
        }

    }


    suspend fun registerWalletMaster(
        deviceId: String,
        address: String,
        referralCode: String
    ): NetworkState<String?> {
        return try {
            val response = apiHelper.registerWalletMaster(
                deviceId,
                address,
                referralCode
            )
            val result = response.body()
            if (response.code() == responseServerError) {
                NetworkState.Error(serverErrorMessage)
            } else {
                if (response.isSuccessful) {
                    val data = result?.string()?.let { JSONObject(it) }

                    NetworkState.Success("", "")
                } else {
                    NetworkState.Error("")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is NoConnectivityException || e is UnknownHostException) {
                NetworkState.SessionOut("", NO_INTERNET_CONNECTION)
            } else {
                NetworkState.Error(serverErrorMessage)
            }
        }

    }


    suspend fun getAllActiveTokenList(
        url: String
    ): NetworkState<MutableList<ModelActiveWalletToken>?> {
        return try {
            val response = apiHelper.getAllActiveTokenList(url)
            val result = response.body()
            if (response.code() == responseServerError) {
                NetworkState.Error(serverErrorMessage)
            } else {
                if (response.isSuccessful && result != null) {

                    activeTokenList.addAll(result)

                    NetworkState.Success("", result)
                } else {
                    NetworkState.Error(response.message().toString())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is NoConnectivityException || e is UnknownHostException) {
                NetworkState.SessionOut("", NO_INTERNET_CONNECTION)
            } else {
                NetworkState.Error(e.message.toString())
            }
        }

    }


}