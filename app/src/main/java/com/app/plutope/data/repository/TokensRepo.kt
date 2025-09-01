package com.app.plutope.data.repository

import com.app.plutope.data.database.TokensDao
import com.app.plutope.model.ModelActiveWalletToken
import com.app.plutope.model.ReferralCodesWrapperModel
import com.app.plutope.model.TokenListImageModel
import com.app.plutope.model.Tokens
import com.app.plutope.model.TransferTraceDetail
import com.app.plutope.model.Wallet
import com.app.plutope.model.WalletTokens
import com.app.plutope.network.ApiHelper
import com.app.plutope.network.NoConnectivityException
import com.app.plutope.ui.fragment.dashboard.GenerateTokenModel
import com.app.plutope.ui.fragment.my_referrals.MyReferralWrapperModel
import com.app.plutope.utils.constant.BASE_URL_PLUTO_PE
import com.app.plutope.utils.constant.NO_INTERNET_CONNECTION
import com.app.plutope.utils.constant.defaultPLTTokenId
import com.app.plutope.utils.constant.responseServerError
import com.app.plutope.utils.constant.serverErrorMessage
import com.app.plutope.utils.extras.PreferenceHelper
import com.app.plutope.utils.loge
import com.app.plutope.utils.network.NetworkState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
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
            val lastToken =
                tokenList.filter { it.t_symbol.lowercase() != "bnry" || it.tokenId.lowercase() != "binary-holdings" }

            tokensDao.insertAll(lastToken)

            NetworkState.Success("", Tokens())

        } catch (e: Exception) {
            e.printStackTrace()
            if (e is NoConnectivityException || e is UnknownHostException) {
                NetworkState.SessionOut("", NO_INTERNET_CONNECTION)
            } else {
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
        return tokensDao.getAllTokenList()
    }

    fun getChainTokenList(): List<Tokens> {
        return tokensDao.getChainTokenList()
    }

    fun getAllTokensWithBalance(): List<Tokens> {
        return tokensDao.getTokensWithBalance(Wallet.walletObject.w_id)
    }

    suspend fun updateAllTokens(
        tokenList: MutableList<Tokens>?
    ): NetworkState<Tokens?> {
        return try {
            val lastToken =
                tokenList?.filter { it.t_symbol.lowercase() != "bnry" || it.tokenId.lowercase() != "binary-holdings" }
            tokensDao.updateTokens(lastToken as List<Tokens>)
            NetworkState.Success("", Tokens())

        } catch (e: Exception) {
            e.printStackTrace()
            NetworkState.Error(serverErrorMessage)
        }
    }

    suspend fun updateAndInsertTokens(
        tokenList: MutableList<Tokens>?
    ): NetworkState<Tokens?> {
        return try {
            tokensDao.updateAndInsertTokens(tokenList as List<Tokens>)
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
            val count =
                tokensDao.getCountTokenIdSpecificWallet(Wallet.walletObject.w_id, token.t_pk)
            if (count == 0) {
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

    suspend fun upsertWalletToken(
        walletToken: MutableList<WalletTokens>
    ): NetworkState<String?> {
        return try {

            //  var pltToken = getAllTokenList()

            val pltToken = getAllTokenList().find { it.tokenId == defaultPLTTokenId }


            val list = tokensDao.getAllWalletTokens()
            val walletWiseList = list.groupBy { it.walletId }

            list.forEach {
                loge("MYWalletsToken", "${it.walletId} :: ${it.t_pk}")
            }
            if (pltToken != null) {
                val pltTokenPk = pltToken.t_pk
                for ((walletId, tokens) in walletWiseList) {
                    val tokenExists =
                        tokens.any { it.t_pk == pltTokenPk } // Check if token exists

                    if (tokenExists) {
                        loge("TokenCheck", "Token $pltTokenPk exists in Wallet $walletId")
                        // You can update it here if needed
                    } else {
                        loge(
                            "TokenCheck",
                            "Token $pltTokenPk NOT found in Wallet $walletId, inserting..."
                        )
                        // Insert the token since it is missing in this wallet
                        val newWalletToken = WalletTokens(
                            walletId = walletId,
                            t_pk = pltTokenPk,
                            isEnable = true,  // Default behavior
                            balance = "0.0"  // Default balance
                        )
                        tokensDao.insertWalletToken(newWalletToken)
                    }
                }
            }


            // tokensDao.upsertWalletToken(walletToken)


            NetworkState.Success("", "")

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

    suspend fun deleteToken(
        tokenId: String
    ) {
        return try {
            tokensDao.deleteToken(tokenId)
        } catch (e: Exception) {
            e.printStackTrace()

        }

    }

    // NetworkState<MutableList<ModelActiveWalletToken>?>

    suspend fun replaceWalletToken(
        oldToken: Tokens,
        newToken: Tokens
    ): NetworkState<Tokens?> {
        return try {

            // tokensDao.deleteCustomToken(oldToken.t_pk)
            // tokensDao.updateWalletToken(newToken.t_pk, Wallet.walletObject.w_id)

            tokensDao.updateWalletTokenId(oldToken = oldToken, newToken = newToken)

            NetworkState.Success("", newToken)

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

    suspend fun deleteAllWalletTokens(
        walletTokenList: MutableList<WalletTokens>
    ): NetworkState<WalletTokens?> {
        return try {
            tokensDao.deleteAllWalletTokens(walletTokenList)
            NetworkState.Success("", WalletTokens(0, 0))
        } catch (e: Exception) {
            e.printStackTrace()
            NetworkState.Error(serverErrorMessage)
        }

    }

    suspend fun updateAllWalletTokens(
        walletTokenList: MutableList<WalletTokens>
    ): NetworkState<WalletTokens?> {
        return try {
            tokensDao.updateAllWalletTokens(walletTokenList)
            NetworkState.Success("", WalletTokens(0, 0))

        } catch (e: Exception) {
            e.printStackTrace()
            NetworkState.Error(serverErrorMessage)
        }

    }

    suspend fun updateAndInsertWalletTokens(
        walletTokenList: MutableList<WalletTokens>
    ): NetworkState<WalletTokens?> {
        return try {
            tokensDao.updateAndInsertWalletTokens(walletTokenList)

            upsertWalletToken(walletTokenList)

            NetworkState.Success("", WalletTokens(0, 0))

        } catch (e: Exception) {
            e.printStackTrace()
            NetworkState.Error(serverErrorMessage)
        }

    }

    /* suspend fun getAllSpecificWalletTokens(walletId: Int): Flow<NetworkState<List<Tokens?>>> {
         return flow {
             val localData: MutableList<Tokens> = mutableListOf()
             localData.addAll(tokensDao.getAllSpecificWalletTokens(walletId).first())

             if (localData.isNotEmpty()) {
                 loge("getWalletTokenOfSpecificWalletId", "getLocalData")
                 emit(NetworkState.Success("", localData))
             } else {
                 loge("getWalletTokenOfSpecificWalletId", "error while getting data")
                 emit(NetworkState.Error("Failed to load"))
             }
         }
     }*/

    suspend fun getAllSpecificWalletTokens(walletId: Int): Flow<NetworkState<List<Tokens?>>> {
        return flow {
            var localData: MutableList<Tokens>
            var wId = walletId
            do {


                localData = tokensDao.getAllSpecificWalletTokens(wId).first().toMutableList()

                /* localData.forEach {
                     if (it.tokenId != pltToken[0].tokenId) {
                         tokensDao.insertWalletToken(
                             WalletTokens(
                                 Wallet.walletObject.w_id,
                                 pltToken[0].t_pk,
                                 true
                             )
                         )
                     }
                 }*/

                if (localData.isNotEmpty()) {
                    loge("getWalletTokenOfSpecificWalletId", "${localData.map { it.t_pk }} ::")
                    emit(NetworkState.Success("", localData))
                    break // Exit the loop on success
                } else {
                    loge(
                        "getWalletTokenOfSpecificWalletId",
                        "$walletId ::No data found, retrying..."
                    )

                    wId = Wallet.walletObject.w_id

                    emit(NetworkState.Error("Failed to load, retrying..."))
                    delay(3000)
                }
            } while (localData.isEmpty())
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


    /* suspend fun getTokenFromCoinGecko(): NetworkState<MutableList<Tokens>?> {
         return try {
             val response = apiHelper.executeCoinListApi()
             val result = response.body()
             if (response.code() == responseServerError) {
                 NetworkState.Error(serverErrorMessage)
             } else {
                 if (response.isSuccessful) {


                     val tokens: MutableList<Tokens> = mutableListOf()
                     val jsonArray = JSONArray(result?.string())
                     val supportedChain = arrayListOf(
                         "binance-smart-chain",
                         "ethereum",
                         "polygon-pos",
                         "okex-chain",
                         "bitcoin",
                         "optimistic-ethereum",
                         "avalanche",
                         "arbitrum-one",
                         *//* "tron",
                         "solana",*//*
                        "base"
                    )
                    val nativeCoins =
                        arrayListOf(
                            "OKT Chain",
                            "Ethereum",
                            "POL (ex-MATIC)",
                            "BNB",
                            "Bitcoin",
                            "Optimism",
                            "Avalanche",
                            "Arbitrum",
                            "Base"*//*,"Tron","Solana"*//*
                        )
                    repeat(jsonArray.length()) {
                        val jObj = jsonArray.getJSONObject(it)
                        val name = jObj.getString("name")
                        val symbol = jObj.getString("symbol")
                        val id = jObj.getString("id")

                        val platformObj = jObj.getJSONObject("platforms")
                        val keys = platformObj.keys()


                        //  loge("MatchSymbole","${activeTokenBySymbol.contains(symbol)}")

                        if (nativeCoins.contains(name)) {

                            loge("token_name", "name -> $name  :: symbole -> $symbol")

                            val tokenModel = Tokens()
                            tokenModel.apply {
                                tokenId = id
                                t_name = name
                                t_symbol =
                                    if (jObj.getString("name") == "Optimism") "ETH" else symbol.uppercase()
                                isEnable = true
                                t_address = ""
                                t_type = when (jObj.getString("name")) {
                                    "Ethereum" -> "ERC20"
                                    "POL (ex-MATIC)" -> "POLYGON"
                                    "BNB" -> "BEP20"
                                    "OKT Chain" -> "KIP20"
                                    "Bitcoin" -> "BTC"
                                    "Optimism" -> "OP Mainnet"
                                    "Avalanche" -> "Avalanche"
                                    "Arbitrum" -> "Arbitrum"
                                    "Base" -> "Base"
                                    *//*"TRON" -> "TRC20"
                                    "Solana" -> "Solana"*//*
                                    else -> ""
                                }
                            }


                            loge(
                                "ADDNewToken",
                                "${tokenModel.t_type}  ${tokenModel.chain?.chainName}  :: ${tokenModel.chain?.name}"
                            )

                            tokens.add(tokenModel)
                            //return@repeat
                        }



                        keys.forEach { key ->
                            val objChain = platformObj.getString(key)

                            if (supportedChain.contains(key)) {
                                if (objChain.isNotEmpty()) {
                                    val tokenModel = Tokens()
                                    if (jObj.getString("id") != "polygon-ecosystem-token") {
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
                                                "optimistic-ethereum" -> "OP Mainnet"
                                                "avalanche" -> "Avalanche"
                                                "arbitrum-one" -> "Arbitrum"
                                                "base" -> "Base"
                                                *//* "tron" -> "TRC20"
                                             "solana" -> "Solana"*//*
                                                else -> ""
                                            }

                                        }
                                    }
                                    // if (jObj.getString("name").lowercase() != "optimism") {
                                    tokens.add(
                                        tokenModel
                                    )
                                    //  }

                                }
                            }
                        }
                    }

                    // private val tokensDao: TokensDao

                    //  Log.e("TAG", "getTokenFromCoinGecko: ${tokens.filter { it.chain == "" }}")

                    NetworkState.Success("", tokens)
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

    }*/


    /* suspend fun getTokenFromCoinGecko(): NetworkState<MutableList<Tokens>?> {
         return try {
             val response = apiHelper.executeCoinListApi()

             if (!response.isSuccessful) {
                 return NetworkState.Error(serverErrorMessage)
             }

             val responseBody =
                 response.body()?.string() ?: return NetworkState.Error("Empty response")
             val jsonArray = JSONArray(responseBody)

             val supportedChains = setOf(
                 "binance-smart-chain", "ethereum", "polygon-pos", "okex-chain", "bitcoin",
                 "optimistic-ethereum", "avalanche", "arbitrum-one", "base"
             )

             val nativeCoins = mapOf(
                 "OKT Chain" to "KIP20", "Ethereum" to "ERC20", "POL (ex-MATIC)" to "POLYGON",
                 "BNB" to "BEP20", "Bitcoin" to "BTC", "Optimism" to "OP Mainnet",
                 "Avalanche" to "Avalanche", "Arbitrum" to "Arbitrum", "Base" to "Base"
             )

             val tokens = getDefaultTokens() // Add predefined tokens first

             for (i in 0 until jsonArray.length()) {
                 val tokenJson = jsonArray.getJSONObject(i)
                 parseTokenFromJson(tokenJson, supportedChains, nativeCoins)?.let { tokens.add(it) }
             }


             NetworkState.Success("", tokens)
         } catch (e: Exception) {
             e.printStackTrace()
             when (e) {
                 is NoConnectivityException, is UnknownHostException -> NetworkState.SessionOut(
                     "",
                     NO_INTERNET_CONNECTION
                 )

                 else -> NetworkState.Error(serverErrorMessage)
             }
         }
     }

     private fun parseTokenFromJson(
         jObj: JSONObject,
         supportedChains: Set<String>,
         nativeCoins: Map<String, String>
     ): Tokens? {
         val name = jObj.optString("name")
         val symbol = jObj.optString("symbol").uppercase()
         val id = jObj.optString("id")

         val tokenModel = Tokens().apply {
             tokenId = id
             t_name = name
             t_symbol = if (name == "Optimism") "ETH" else symbol
             isEnable = nativeCoins.containsKey(name)
             t_address = ""
             t_type = nativeCoins[name] ?: ""
         }

         if (tokenModel.isEnable) {
             loge("Token Added", "Name: $name, Symbol: $symbol")
             return tokenModel
         }

         val platformObj = jObj.optJSONObject("platforms") ?: return null
         for (key in platformObj.keys()) {
             if (supportedChains.contains(key)) {
                 val address = platformObj.optString(key)
                 if (address.isNotEmpty()) {
                     return Tokens().apply {
                         tokenId = id
                         t_name = name
                         t_symbol = symbol
                         isEnable = false
                         t_address = address
                         t_type = when (key) {
                             "binance-smart-chain" -> "BEP20"
                             "ethereum" -> "ERC20"
                             "polygon-pos" -> "POLYGON"
                             "okex-chain" -> "KIP20"
                             "bitcoin" -> "BTC"
                             "optimistic-ethereum" -> "OP Mainnet"
                             "avalanche" -> "Avalanche"
                             "arbitrum-one" -> "Arbitrum"
                             "base" -> "Base"
                             else -> ""
                         }
                     }
                 }
             }
         }
         return null
     }

     private fun getDefaultTokens(): MutableList<Tokens> {
         return mutableListOf(
             Tokens().apply {
                 tokenId = defaultPLTTokenId
                 t_name = "Plutope Token"
                 t_symbol = "PLT"
                 isEnable = true
                 t_address =
                     "0x1E3B5Ac35B153BB0A6F6C6d46F05712E102FE42E"  // No address needed for native tokens
                 t_type = "BEP20"
             }


         )
     }*/


    suspend fun getTokenFromCoinGecko(): NetworkState<MutableList<Tokens>?> {
        return try {
            val response = apiHelper.executeCoinListApi()

            if (!response.isSuccessful) {
                return NetworkState.Error(serverErrorMessage)
            }

            val responseBody =
                response.body()?.string() ?: return NetworkState.Error("Empty response")
            val jsonArray = JSONArray(responseBody)

            val supportedChains = setOf(
                "binance-smart-chain", "ethereum", "polygon-pos", "okex-chain", "bitcoin",
                "optimistic-ethereum", "avalanche", "arbitrum-one", "base"
            )

            val nativeCoins = mapOf(
                "OKT Chain" to "KIP20",
                "Ethereum" to "ERC20",
                "POL (ex-MATIC)" to "POLYGON",
                "BNB" to "BEP20",
                "Bitcoin" to "BTC",
                "Optimism" to "OP Mainnet",
                "Avalanche" to "Avalanche",
                "Arbitrum" to "Arbitrum",
                "Base" to "Base"
            )

            val tokens = getDefaultTokens() // Add predefined tokens first

            for (i in 0 until jsonArray.length()) {
                val tokenJson = jsonArray.getJSONObject(i)
                val tokenList = parseTokenFromJson(tokenJson, supportedChains, nativeCoins)
                tokens.addAll(tokenList)
            }

            // Optional debug logs
            /*val platformCounts = mutableMapOf<String, Int>()
            tokens.forEach {
                platformCounts[it.t_type] = (platformCounts[it.t_type] ?: 0) + 1
            }
            platformCounts.forEach { (platform, count) ->
                loge("TokenPlatform", "$platform: $count tokens")
            }*/

            NetworkState.Success("", tokens)
        } catch (e: Exception) {
            e.printStackTrace()
            when (e) {
                is NoConnectivityException, is UnknownHostException -> NetworkState.SessionOut(
                    "",
                    NO_INTERNET_CONNECTION
                )

                else -> NetworkState.Error(serverErrorMessage)
            }
        }
    }

    private fun parseTokenFromJson(
        jObj: JSONObject,
        supportedChains: Set<String>,
        nativeCoins: Map<String, String>
    ): List<Tokens> {
        val name = jObj.optString("name")
        val symbol = jObj.optString("symbol").uppercase()
        val id = jObj.optString("id")

        val tokensList = mutableListOf<Tokens>()

        // Add native coin if applicable
        if (nativeCoins.containsKey(name)) {
            tokensList.add(
                Tokens().apply {
                    tokenId = id
                    t_name = name
                    t_symbol =
                        if (name == "Optimism" || name == "Arbitrum" || name == "Base") "ETH" else symbol
                    isEnable = true
                    t_address = ""
                    t_type = nativeCoins[name] ?: ""
                }
            )
        }

        // Parse all supported platform tokens
        val platformObj = jObj.optJSONObject("platforms") ?: return tokensList

        for (key in platformObj.keys()) {
            if (supportedChains.contains(key)) {
                val address = platformObj.optString(key)
                if (address.isNotEmpty()) {
                    /*if (name == "Base" || name == "Arbitrum") {
                         tokensList.add(
                             Tokens().apply {
                                 tokenId = id
                                 t_name = name
                                 t_symbol = symbol
                                 isEnable = true
                                 t_address = address
                                 t_type = when (key) {
                                     "binance-smart-chain" -> "BEP20"
                                     "ethereum" -> "ERC20"
                                     "polygon-pos" -> "POLYGON"
                                     "okex-chain" -> "KIP20"
                                     "bitcoin" -> "BTC"
                                     "optimistic-ethereum" -> "OP Mainnet"
                                     "avalanche" -> "Avalanche"
                                     "arbitrum-one" -> "Arbitrum"
                                     "base" -> "Base"
                                     else -> ""
                                 }
                             }
                         )
                    } else {
                        tokensList.add(
                            Tokens().apply {
                                tokenId = id
                                t_name = name
                                t_symbol = symbol
                                isEnable = false
                                t_address = address
                                t_type = when (key) {
                                    "binance-smart-chain" -> "BEP20"
                                    "ethereum" -> "ERC20"
                                    "polygon-pos" -> "POLYGON"
                                    "okex-chain" -> "KIP20"
                                    "bitcoin" -> "BTC"
                                    "optimistic-ethereum" -> "OP Mainnet"
                                    "avalanche" -> "Avalanche"
                                    "arbitrum-one" -> "Arbitrum"
                                    "base" -> "Base"
                                    else -> ""
                                }
                            }
                        )
                    }*/

                    tokensList.add(
                        Tokens().apply {
                            tokenId = id
                            t_name = name
                            t_symbol = symbol
                            isEnable = false
                            t_address = address
                            t_type = when (key) {
                                "binance-smart-chain" -> "BEP20"
                                "ethereum" -> "ERC20"
                                "polygon-pos" -> "POLYGON"
                                "okex-chain" -> "KIP20"
                                "bitcoin" -> "BTC"
                                "optimistic-ethereum" -> "OP Mainnet"
                                "avalanche" -> "Avalanche"
                                "arbitrum-one" -> "Arbitrum"
                                "base" -> "Base"
                                else -> ""
                            }
                        }
                    )

                }
            }
        }

        return tokensList
    }

    private fun getDefaultTokens(): MutableList<Tokens> {
        return mutableListOf(
            Tokens().apply {
                tokenId = defaultPLTTokenId
                t_name = "Plutope Token"
                t_symbol = "PLT"
                isEnable = true
                t_address = "0x1E3B5Ac35B153BB0A6F6C6d46F05712E102FE42E"
                t_type = "BEP20"
            }
        )
    }


    suspend fun executeTokenListImages(
    ): NetworkState<MutableList<TokenListImageModel>?> {
        return try {
            val response = apiHelper.executeTokenImageListApi()
            val result = response.body()
            if (response.code() == responseServerError) {
                NetworkState.Error(serverErrorMessage)
            } else {
                if (response.isSuccessful && result != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val default = TokenListImageModel(
                            id = 0,
                            coin_id = defaultPLTTokenId,
                            image = "https://plutope.app/api/images/applogo.png",
                            symbol = "plt",
                            name = "Plutope Token"

                        )
                        result.add(default)
                        tokensDao.insertTokenImageTokens(result)
                    }
                    NetworkState.Success("", result)
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

    suspend fun updateTokenImages(tokenImage: TokenListImageModel) {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                tokensDao.updateTokenImage(tokenImage)
                loge("updateTokenImages", "Inserted token image")
            }
        } catch (e: Exception) {
            e.printStackTrace()
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


    suspend fun registerWallet(
        address: String, fcmToken: String, type: String, referralCode: String
    ): NetworkState<String?> {
        return try {
            val response = apiHelper.registerWallet(
                address,
                PreferenceHelper.getInstance().deviceId,
                fcmToken, type, referralCode
            )
            if (response.code() == responseServerError) {
                NetworkState.Error(serverErrorMessage)
            } else {
                if (response.isSuccessful) {
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

    suspend fun getMyReferrals(address: String): NetworkState<MyReferralWrapperModel?> {
        return try {
            val response = apiHelper.getMyReferrals(address)
            if (response.code() == responseServerError) {
                NetworkState.Error(serverErrorMessage)
            } else {
                if (response.isSuccessful) {
                    NetworkState.Success("", response.body())
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

    suspend fun updateNonClaimedTokens(walletAddress: String): NetworkState<ResponseBody?> {
        return try {
            val response = apiHelper.updateNonClaimedTokens(walletAddress)
            if (response.code() == responseServerError) {
                NetworkState.Error(serverErrorMessage)
            } else {
                if (response.isSuccessful) {
                    NetworkState.Success("", response.body())
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

    suspend fun getMyUserCode(walletAddress: String): NetworkState<ReferralCodesWrapperModel?> {
        return try {
            val response = apiHelper.getMyUserCode(walletAddress)
            if (response.code() == responseServerError) {
                NetworkState.Error(serverErrorMessage)
            } else {
                if (response.isSuccessful) {
                    NetworkState.Success("", response.body())
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

            if (response.code() == responseServerError) {
                NetworkState.Error(serverErrorMessage)
            } else {
                if (response.isSuccessful) {
                    NetworkState.Success("", "Transaction Successful")
                } else {
                    val error = response.errorBody()?.charStream()?.buffered()?.readLine()
                    val jsonObject = JSONObject(error!!)
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


    /*suspend fun registerWalletMaster(
        deviceId: String,
        address: String,
        referralCode: String,
    ): NetworkState<String?> {
        return try {
            val response = apiHelper.registerWalletMaster(
                deviceId,
                address,
                referralCode,
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

    }*/


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

    suspend fun transactionTrackActivityLog(
        body: TransferTraceDetail
    ): NetworkState<String?> {
        return try {
            val response = apiHelper.transactionTrackActivityLog(body)
            val result = response.body()
            if (response.code() == responseServerError) {
                NetworkState.Error(serverErrorMessage)
            } else {
                if (response.isSuccessful && result != null) {
                    NetworkState.Success("", "OK")
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

    suspend fun getGenerateToken(): NetworkState<GenerateTokenModel?> {
        return try {
            val url = BASE_URL_PLUTO_PE + "get-generate-token"
            val response = apiHelper.getGenerateToken(url)
            val result = response.body()
            if (response.code() == responseServerError) {
                NetworkState.Error(serverErrorMessage)
            } else {
                if (response.isSuccessful) {
                    NetworkState.Success("", result)
                } else {
                    NetworkState.Error("")
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