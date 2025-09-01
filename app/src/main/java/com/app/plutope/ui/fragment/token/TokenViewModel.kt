package com.app.plutope.ui.fragment.token

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.app.plutope.data.repository.TokensRepo
import com.app.plutope.model.ModelActiveWalletToken
import com.app.plutope.model.TokenListImageModel
import com.app.plutope.model.Tokens
import com.app.plutope.model.TransferTraceDetail
import com.app.plutope.model.Wallet
import com.app.plutope.model.WalletTokens
import com.app.plutope.ui.base.BaseViewModel
import com.app.plutope.ui.fragment.dashboard.GenerateTokenModel
import com.app.plutope.utils.common.CommonNavigator
import com.app.plutope.utils.constant.BASE_URL_PLUTO_PE
import com.app.plutope.utils.loge
import com.app.plutope.utils.network.NetworkState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TokenViewModel @Inject constructor(private val tokenRepo: TokensRepo) :
    BaseViewModel<CommonNavigator>() {

    //  val wallet: Wallet = Wallet

    //Insert Tokens
    private val _tagInsertTokens =
        MutableStateFlow<NetworkState<Tokens?>>(NetworkState.Empty())

    val insertTokenResponse: StateFlow<NetworkState<Tokens?>>
        get() = _tagInsertTokens

    fun executeInsertTokens(tokens: MutableList<Tokens>) {
        viewModelScope.launch {
            _tagInsertTokens.emit(NetworkState.Loading())
            _tagInsertTokens.collectStateFlow(tokenRepo.insertAllTokens(tokens))
        }
    }


    fun getEnableTokens(isEnable: Int): List<Tokens> {
        return tokenRepo.getAllIsEnableToken(isEnable)
    }

    fun getAllTokensList(): List<Tokens> {
        return tokenRepo.getAllTokenList()
    }

    fun getChainTokenList(): List<Tokens> {
        return tokenRepo.getChainTokenList()
    }

    private val _tokenList = MutableLiveData<List<Tokens>>()
    val tokenList: LiveData<List<Tokens>> get() = _tokenList

    fun fetchAllTokensList() {
        viewModelScope.launch(Dispatchers.IO) {
            val list = tokenRepo.getAllTokenList()
            _tokenList.postValue(list)
        }
    }

    /* private val _tokenList2 = MutableStateFlow<List<Tokens>>(emptyList())
     val tokenList2: StateFlow<List<Tokens>> get() = _tokenList2

     fun fetchAllTokensList2() {
         viewModelScope.launch(Dispatchers.IO) {
             val list = tokenRepo.getAllTokenList()
             _tokenList2.value = list // Use value instead of postValue
         }
     }*/

    fun getAllTokensWithBalance(): List<Tokens> {
        return tokenRepo.getAllTokensWithBalance()
    }

    fun getAllDisableTokens(): List<Tokens> {
        return tokenRepo.getAllDisableTokenList()
    }

    //update Tokens
    private val _tagUpdateTokens =
        MutableStateFlow<NetworkState<Tokens?>>(NetworkState.Empty())

    val updateTokenResponse: StateFlow<NetworkState<Tokens?>>
        get() = _tagUpdateTokens

    fun executeUpdateTokens(tokens: MutableList<Tokens>?) {
        viewModelScope.launch {
            _tagUpdateTokens.emit(NetworkState.Loading())
            _tagUpdateTokens.collectStateFlow(tokenRepo.updateAllTokens(tokens))
        }
    }

    //update Tokens
    private val _tagUpdateAndInsertTokens =
        MutableStateFlow<NetworkState<Tokens?>>(NetworkState.Empty())

    val updateAndInsertTokensResponse: StateFlow<NetworkState<Tokens?>>
        get() = _tagUpdateAndInsertTokens

    fun executeUpdateAndInsertTokens(tokens: MutableList<Tokens>?) {
        viewModelScope.launch {
            _tagUpdateAndInsertTokens.emit(NetworkState.Loading())
            _tagUpdateAndInsertTokens.collectStateFlow(tokenRepo.updateAndInsertTokens(tokens))
        }
    }


    //update Token
    private val _tagUpdateToken =
        MutableStateFlow<NetworkState<Tokens?>>(NetworkState.Empty())

    val updateTokenResp: StateFlow<NetworkState<Tokens?>>
        get() = _tagUpdateToken

    fun executeUpdateToken(tokens: Tokens) {
        viewModelScope.launch {
            _tagUpdateToken.emit(NetworkState.Loading())
            _tagUpdateToken.collectStateFlow(tokenRepo.updateToken(tokens))
        }
    }


    //update wallet Token for enable
    private val _tagUpdateWalletToken =
        MutableStateFlow<NetworkState<Tokens?>>(NetworkState.Empty())

    val updateWalletTokenResp: StateFlow<NetworkState<Tokens?>>
        get() = _tagUpdateWalletToken

    fun executeUpdateWalletToken(tokens: Tokens) {
        viewModelScope.launch {
            _tagUpdateWalletToken.emit(NetworkState.Loading())
            _tagUpdateWalletToken.collectStateFlow(tokenRepo.updateWalletToken(tokens))
        }
    }

    //update wallet Token for enable
    /*  private val _replaceWalletToken =
          MutableStateFlow<NetworkState<List<Tokens?>>>(NetworkState.Empty())

      val replaceWalletTokens: StateFlow<NetworkState<List<Tokens?>>>
          get() = _replaceWalletToken*/

    private val _replaceWalletToken =
        MutableStateFlow<NetworkState<Tokens?>>(NetworkState.Empty())
    val replaceWalletTokens: StateFlow<NetworkState<Tokens?>>
        get() = _replaceWalletToken

    fun replaceWalletToken(oldToken: Tokens, newToken: Tokens) {
        viewModelScope.launch {
            _replaceWalletToken.emit(NetworkState.Loading())
            _replaceWalletToken.collectStateFlow(
                tokenRepo.replaceWalletToken(
                    oldToken,
                    newToken
                )
            )
            /*  _replaceWalletToken.emit(NetworkState.Loading())
              tokenRepo.replaceWalletToken(oldToken,newToken).collect { networkState ->
                  _replaceWalletToken.value = networkState
              }*/

        }
    }


    //Insert Wallet Tokens
    private val _tagInsertWalletTokens =
        MutableStateFlow<NetworkState<WalletTokens?>>(NetworkState.Empty())

    val insertWalletTokenResponse: StateFlow<NetworkState<WalletTokens?>>
        get() = _tagInsertWalletTokens

    fun executeInsertWalletTokens(tokens: MutableList<WalletTokens>) {
        viewModelScope.launch {
            _tagInsertWalletTokens.emit(NetworkState.Loading())
            _tagInsertWalletTokens.collectStateFlow(tokenRepo.insertAllWalletTokens(tokens))
        }
    }

    //Update Wallet Tokens
    private val _tagUpdateWalletTokens =
        MutableStateFlow<NetworkState<WalletTokens?>>(NetworkState.Empty())

    val updateWalletTokenResponse: StateFlow<NetworkState<WalletTokens?>>
        get() = _tagUpdateWalletTokens

    fun executeUpdateWalletTokens(tokens: MutableList<WalletTokens>) {
        viewModelScope.launch {
            _tagInsertWalletTokens.emit(NetworkState.Loading())
            _tagInsertWalletTokens.collectStateFlow(tokenRepo.updateAllWalletTokens(tokens))
        }
    }

    //Update Wallet Tokens
    private val _tagUpdateAndInsertWalletTokens =
        MutableStateFlow<NetworkState<WalletTokens?>>(NetworkState.Empty())

    val updateAndInsertWalletTokensResponse: StateFlow<NetworkState<WalletTokens?>>
        get() = _tagUpdateAndInsertWalletTokens

    fun executeUpdateAndInsertWalletTokens(tokens: MutableList<WalletTokens>) {
        viewModelScope.launch {
            _tagUpdateAndInsertWalletTokens.emit(NetworkState.Loading())
            _tagUpdateAndInsertWalletTokens.collectStateFlow(
                tokenRepo.updateAndInsertWalletTokens(
                    tokens
                )
            )
        }
    }


    //get WalletTokens
    private val _getWalletTokensResponse =
        MutableStateFlow<NetworkState<List<Tokens?>>>(NetworkState.Empty())
    val walletsTokensResponse: MutableStateFlow<NetworkState<List<Tokens?>>>
        get() = _getWalletTokensResponse

    fun getWalletTokenOfSpecificWalletId(walletId: Int) {
        loge("getWalletTokenOfSpecificWalletId", "enter in call")
        viewModelScope.launch {
            _getWalletTokensResponse.emit(NetworkState.Loading())
            tokenRepo.getAllSpecificWalletTokens(walletId).collect { networkState ->
                _getWalletTokensResponse.value = networkState
            }
        }
    }

    fun getAllTokenList(
        tokenViewModel: TokenViewModel,
        tokenList: MutableList<Tokens> = mutableListOf(),
        isFromRefresh: Boolean = false
    ) {
        val list = getAllTokensList()
        if (list.isNotEmpty()) {
            insertInWalletTokens(tokenViewModel, tokenList, isFromRefresh)
        } else {
            setChains(tokenViewModel)
        }

        //  setChains(tokenViewModel)

    }


    private fun setChains(tokenViewModel: TokenViewModel) {
        if (Wallet.prefHelper.menomonicWallet.isNotEmpty() == true) {
            tokenViewModel.getCoinGeckoTokensList()
        }
    }

    fun insertInWalletTokens(
        tokenViewModel: TokenViewModel,
        tokenList: MutableList<Tokens> = mutableListOf(),
        isFromRefresh: Boolean = false

    ) {
        val listWalletTokens = mutableListOf<WalletTokens>()
        // val defaultTokenslist =getEnableTokens(1) + tokenList
        loge(
            "insertInWalletTokens",
            "condition => ${isFromRefresh}   :: ${getEnableTokens(1)} :: ${tokenList}"
        )
        val defaultTokenslist = if (isFromRefresh) tokenList else getEnableTokens(1) + tokenList

        defaultTokenslist.forEach {
            listWalletTokens.add(
                WalletTokens(
                    walletId = Wallet.walletObject.w_id,
                    it.t_pk,
                    isEnable = true
                )
            )
        }

        loge("executeInsertWalletTokens", "${defaultTokenslist}   ==>${listWalletTokens}")

        tokenViewModel.executeInsertWalletTokens(listWalletTokens.distinct().toMutableList())


    }


    //Insert New Token
    private val _tagInsertNewTokens =
        MutableStateFlow<NetworkState<Tokens?>>(NetworkState.Empty())

    val insertNewTokenResponse: StateFlow<NetworkState<Tokens?>>
        get() = _tagInsertNewTokens

    fun executeInsertNewTokens(tokens: Tokens) {
        viewModelScope.launch {
            _tagInsertNewTokens.emit(NetworkState.Loading())
            _tagInsertNewTokens.collectStateFlow(tokenRepo.insertNewToken(tokens))
        }
    }

    //get coin gecko token list
    private val _getTokenListResponse =
        MutableStateFlow<NetworkState<MutableList<Tokens>?>>(NetworkState.Empty())
    val coinGeckoTokensResponse: StateFlow<NetworkState<MutableList<Tokens>?>>
        get() = _getTokenListResponse

    fun getCoinGeckoTokensList() {

        viewModelScope.launch {
            _getTokenListResponse.emit(NetworkState.Loading())
            _getTokenListResponse.collectStateFlow(tokenRepo.getTokenFromCoinGecko())

        }
    }

    //get coin gecko token list
    private val _getTokenImageListResponse =
        MutableStateFlow<NetworkState<MutableList<TokenListImageModel>?>>(NetworkState.Empty())
    val tokenImageListResponse: StateFlow<NetworkState<MutableList<TokenListImageModel>?>>
        get() = _getTokenImageListResponse

    fun getTokenImageList() {

        viewModelScope.launch {

            _getTokenImageListResponse.emit(NetworkState.Loading())
            _getTokenImageListResponse.collectStateFlow(tokenRepo.executeTokenListImages())

        }
    }

    suspend fun updateTokenImages(tokenImage: TokenListImageModel) {
        return tokenRepo.updateTokenImages(tokenImage)
    }

    suspend fun deleteToken(tokenId: String) {
        return tokenRepo.deleteToken(tokenId)
    }

    fun getAllTokensImageList(): List<TokenListImageModel> {
        return tokenRepo.getAllTokenImageList()
    }


    fun getTokenListByContractAddress(contractAdd: List<String>): List<Tokens> {
        return tokenRepo.getTokenByContractAddress(contractAdd)
    }


    //updateAllTokenBalanceZero
    private val _tagUpdateAllTokenBalanceZero =
        MutableStateFlow<NetworkState<Tokens?>>(NetworkState.Empty())

    val updateAllTokenBalanceZero: StateFlow<NetworkState<Tokens?>>
        get() = _tagUpdateAllTokenBalanceZero

    fun executeUpdateAllTokenBalanceZero() {
        viewModelScope.launch {
            _tagUpdateAllTokenBalanceZero.emit(NetworkState.Loading())
            _tagUpdateAllTokenBalanceZero.collectStateFlow(tokenRepo.updateAllTokenBalanceZero())
        }
    }

    /**
     * Get Bitcoin balance
     * */

    //update Tokens
    private val _tagGetBitcoinBalance =
        MutableStateFlow<NetworkState<String?>>(NetworkState.Empty())

    val getBitcoinBalance: StateFlow<NetworkState<String?>>
        get() = _tagGetBitcoinBalance

    fun executeUpdateTokens(address: String) {
        viewModelScope.launch {
            _tagGetBitcoinBalance.emit(NetworkState.Loading())
            _tagGetBitcoinBalance.collectStateFlow(tokenRepo.getBitcoinBalance(address))
        }
    }

    /**
     * Get register wallet
     * */

    private val _tagRegisterWallet =
        MutableStateFlow<NetworkState<String?>>(NetworkState.Empty())

    val getRegisterWallet: StateFlow<NetworkState<String?>>
        get() = _tagRegisterWallet

    fun registerWalletCall(
        address: String,
        fcmToken: String,
        type: String,
        refferalCode: String,
    ) {
        viewModelScope.launch {
            _tagRegisterWallet.emit(NetworkState.Loading())
            _tagRegisterWallet.collectStateFlow(
                tokenRepo.registerWallet(
                    address,
                    fcmToken,
                    type,
                    refferalCode
                )
            )
        }
    }

    /*
    */
    /**
     * Get register wallet with referralCode
     * *//*

    private val _tagRegisterWalletMaster =
        MutableStateFlow<NetworkState<String?>>(NetworkState.Empty())

    val getRegisterWalletMaster: StateFlow<NetworkState<String?>>
        get() = _tagRegisterWalletMaster

    fun registerWalletCallMaster(deviceId: String, address: String, referralCode: String) {
        viewModelScope.launch {
            _tagRegisterWalletMaster.emit(NetworkState.Loading())
            _tagRegisterWalletMaster.collectStateFlow(
                tokenRepo.registerWalletMaster(
                    deviceId,
                    address,
                    referralCode
                )
            )
        }
    }*/


    /**
     *  Get all active token list api call
     */


    private val _getAllActiveTokenListResponse =
        MutableStateFlow<NetworkState<MutableList<ModelActiveWalletToken>?>>(NetworkState.Empty())
    val getAllActiveTokenListResponse: StateFlow<NetworkState<MutableList<ModelActiveWalletToken>?>>
        get() = _getAllActiveTokenListResponse

    fun getAllActiveTokenList(walletAddress: String) {
        viewModelScope.launch {
            _getAllActiveTokenListResponse.emit(NetworkState.Loading())
            _getAllActiveTokenListResponse.collectStateFlow(
                tokenRepo.getAllActiveTokenList(
                    BASE_URL_PLUTO_PE + "get-wallet-tokens/" + walletAddress
                )
            )
        }
    }


    /**
     *  Trace all transaction activity log
     */

    private val _traceActivityLog =
        MutableStateFlow<NetworkState<String?>>(NetworkState.Empty())
    val trackActivityLogResponse: StateFlow<NetworkState<String?>>
        get() = _traceActivityLog

    fun traceActivityLogCall(body: TransferTraceDetail) {
        viewModelScope.launch {
            _traceActivityLog.emit(NetworkState.Loading())
            _traceActivityLog.collectStateFlow(
                tokenRepo.transactionTrackActivityLog(body)
            )

        }
    }

    private val _tagGetGenerateToken =
        MutableStateFlow<NetworkState<GenerateTokenModel?>>(NetworkState.Empty())

    val getGenerateTokenResponse: StateFlow<NetworkState<GenerateTokenModel?>>
        get() = _tagGetGenerateToken

    fun executeGetGenerateToken() {
        viewModelScope.launch {
            _tagGetGenerateToken.emit(NetworkState.Loading())
            _tagGetGenerateToken.collectStateFlow(tokenRepo.getGenerateToken())
        }

    }
}