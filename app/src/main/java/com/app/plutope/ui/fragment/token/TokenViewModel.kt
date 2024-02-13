package com.app.plutope.ui.fragment.token

import androidx.lifecycle.viewModelScope
import com.app.plutope.data.repository.TokensRepo
import com.app.plutope.model.TokenListImageModel
import com.app.plutope.model.Tokens
import com.app.plutope.model.Wallet
import com.app.plutope.model.WalletTokens
import com.app.plutope.ui.base.BaseViewModel
import com.app.plutope.utils.common.CommonNavigator
import com.app.plutope.utils.network.NetworkState
import dagger.hilt.android.lifecycle.HiltViewModel
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


    private fun getEnableTokens(isEnable: Int): List<Tokens> {
        return tokenRepo.getAllIsEnableToken(isEnable)
    }

    fun getAllTokensList(): List<Tokens> {
        return tokenRepo.getAllTokenList()
    }

    fun getAllTokensWithBalance() : List<Tokens>{
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


    //Insert Wallet Tokens
    private val _tagInsertWalletTokens =
        MutableStateFlow<NetworkState<WalletTokens?>>(NetworkState.Empty())

    val insertWalletTokenResponse: StateFlow<NetworkState<WalletTokens?>>
        get() = _tagInsertWalletTokens

    private fun executeInsertWalletTokens(tokens: MutableList<WalletTokens>) {
        viewModelScope.launch {
            _tagInsertWalletTokens.emit(NetworkState.Loading())
            _tagInsertWalletTokens.collectStateFlow(tokenRepo.insertAllWalletTokens(tokens))
        }
    }

    //get WalletTokens
    private val _getWalletTokensResponse =
        MutableStateFlow<NetworkState<List<Tokens?>>>(NetworkState.Empty())
    val walletsTokensResponse: MutableStateFlow<NetworkState<List<Tokens?>>>
        get() = _getWalletTokensResponse

    fun getWalletTokenOfSpecificWalletId(walletId: Int) {

        viewModelScope.launch {
            _getWalletTokensResponse.emit(NetworkState.Loading())
            tokenRepo.getAllSpecificWalletTokens(walletId).collect { networkState ->
                _getWalletTokensResponse.value = networkState
            }
        }
    }

    fun getAllTokenList(tokenViewModel:TokenViewModel) {
        val list = getAllTokensList()
        if (list.isNotEmpty()) {
           insertInWalletTokens(tokenViewModel)
        } else {
            setChains(tokenViewModel)
        }
    }


    private fun setChains(tokenViewModel:TokenViewModel) {
        if(Wallet.prefHelper.menomonicWallet?.isNotEmpty()==true){
            tokenViewModel.getCoinGeckoTokensList()
        }

    }

    fun insertInWalletTokens(tokenViewModel: TokenViewModel){
        val listWalletTokens= mutableListOf<WalletTokens>()
        val defaultTokenslist = getEnableTokens(1)
        defaultTokenslist.forEach {
            listWalletTokens.add(WalletTokens(walletId = Wallet.walletObject.w_id ,it.t_pk,isEnable = true))
        }
        tokenViewModel.executeInsertWalletTokens(listWalletTokens)
    }


    //Insert New Token
    private val _tagInsertNewTokens =
        MutableStateFlow<NetworkState<Tokens?>>(NetworkState.Empty())

    val insertNewTokenResponse: StateFlow<NetworkState<Tokens?>>
        get() = _tagInsertNewTokens

    fun executeInsertNewTokens(tokens:Tokens) {
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

    private fun getCoinGeckoTokensList() {

        viewModelScope.launch {
            _getTokenListResponse.emit(NetworkState.Loading())
            _getTokenListResponse.collectStateFlow(tokenRepo.getTokenFromCoinGecko())

        }
    }

    //get coin gecko token list
    private val _getTokenImageListResponse =
        MutableStateFlow<NetworkState<List<TokenListImageModel>?>>(NetworkState.Empty())
    val tokenImageListResponse: StateFlow<NetworkState<List<TokenListImageModel>?>>
        get() = _getTokenImageListResponse

    fun getTokenImageList() {

        viewModelScope.launch {

            _getTokenImageListResponse.emit(NetworkState.Loading())
            _getTokenImageListResponse.collectStateFlow(tokenRepo.executeTokenListImages())

        }
    }

    fun getAllTokensImageList(): List<TokenListImageModel> {
        return tokenRepo.getAllTokenImageList()
    }

    fun getTokenListByContractAddress(contractAdd:List<String>): List<Tokens> {
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

    fun registerWalletCall(address: String, fcmToken: String) {
        viewModelScope.launch {
            _tagRegisterWallet.emit(NetworkState.Loading())
            _tagRegisterWallet.collectStateFlow(tokenRepo.registerWallet(address, fcmToken))
        }
    }


}