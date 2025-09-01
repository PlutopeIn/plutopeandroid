package com.app.plutope.ui.fragment.transactions.buy.buy_detail

import androidx.lifecycle.viewModelScope
import com.app.plutope.data.repository.TokensRepo
import com.app.plutope.data.repository.TransactionHistoryRepo
import com.app.plutope.model.Tokens
import com.app.plutope.model.Transaction
import com.app.plutope.model.TransactionHistoryModel
import com.app.plutope.networkConfig.Chain
import com.app.plutope.ui.base.BaseViewModel
import com.app.plutope.utils.common.CommonNavigator
import com.app.plutope.utils.constant.API_URL_BSC_SCAN
import com.app.plutope.utils.constant.API_URL_ETHER_SCAN
import com.app.plutope.utils.constant.API_URL_POLY_SCAN
import com.app.plutope.utils.constant.BSC_SCAN_API_KEY
import com.app.plutope.utils.constant.ETHER_SCAN_API_KEY
import com.app.plutope.utils.constant.POLY_API_KEY
import com.app.plutope.utils.network.NetworkState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BuyDetailsViewModel @Inject constructor(
    val transactionRepo: TransactionHistoryRepo,
    val tokenRepo: TokensRepo
) : BaseViewModel<CommonNavigator>() {


    //transaction History
    private val _tagGetTransactionHistory =
        MutableStateFlow<NetworkState<TransactionHistoryModel?>>(NetworkState.Empty())

    val transactionHistoryResponse: StateFlow<NetworkState<TransactionHistoryModel?>>
        get() = _tagGetTransactionHistory

    fun executeGetTransactionHistory(url: String, tokens: Tokens) {

        viewModelScope.launch {
            _tagGetTransactionHistory.emit(NetworkState.Loading())
            _tagGetTransactionHistory.collectStateFlow(
                transactionRepo.getTransactionHistory(
                    url,
                    tokens
                )
            )
        }
    }

    fun getTransactionURL(tokens: Tokens): String {
        return when (tokens.chain) {
            Chain.Ethereum -> API_URL_ETHER_SCAN
            Chain.BinanceSmartChain -> API_URL_BSC_SCAN
            Chain.OKC -> ""
            Chain.Polygon -> API_URL_POLY_SCAN
            else -> ""
        }
    }

    fun getTransactionApiKey(tokens: Tokens): String {
        return when (tokens.chain) {
            Chain.Ethereum -> ETHER_SCAN_API_KEY
            Chain.BinanceSmartChain -> BSC_SCAN_API_KEY
            Chain.OKC -> ""
            Chain.Polygon -> POLY_API_KEY
            else -> ""
        }
    }

    //transaction History
    private val _tagGetTransactionOkLinkHistory =
        MutableStateFlow<NetworkState<Transaction?>>(NetworkState.Empty())

    val transactionHistoryOkLinkResponse: StateFlow<NetworkState<Transaction?>>
        get() = _tagGetTransactionOkLinkHistory

    fun executeGetTransactionHistoryOkLink(url: String, tokens: Tokens) {
        viewModelScope.launch {
            _tagGetTransactionOkLinkHistory.emit(NetworkState.Loading())
            _tagGetTransactionOkLinkHistory.collectStateFlow(
                transactionRepo.getTransactionHistoryNew(
                    url,
                    tokens
                )
            )
        }
    }

    //transaction History moralis
    private val _tagGetTransactionMoralisHistory =
        MutableStateFlow<NetworkState<TransactionMoralisResponse?>>(NetworkState.Empty())

    val transactionHistoryMoralisResponse: StateFlow<NetworkState<TransactionMoralisResponse?>>
        get() = _tagGetTransactionMoralisHistory

    fun executeGetTransactionHistoryMoralis(url: String, tokens: Tokens) {
        viewModelScope.launch {
            _tagGetTransactionMoralisHistory.emit(NetworkState.Loading())
            _tagGetTransactionMoralisHistory.collectStateFlow(
                transactionRepo.executeMoralisTransactionList(
                    url,
                    tokens
                )
            )
        }
    }

    //transaction History new
    private val _tagGetTransferHistory =
        MutableStateFlow<NetworkState<TransferHistoryModel?>>(NetworkState.Empty())

    val transferHistoryResponse: StateFlow<NetworkState<TransferHistoryModel?>>
        get() = _tagGetTransferHistory

    fun executeGetTransferHistory(url: String, tokens: Tokens, page: Int? = 0) {
        viewModelScope.launch {
            _tagGetTransferHistory.emit(NetworkState.Loading())
            _tagGetTransferHistory.collectStateFlow(
                transactionRepo.executeTransactionHistoryList(
                    url,
                    tokens,
                    page
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

    fun setWalletActiveCall(address: String, receiverAddress: String) {
        viewModelScope.launch {
            _tagSetWalletActive.emit(NetworkState.Loading())
            _tagSetWalletActive.collectStateFlow(
                tokenRepo.setWalletActive(
                    address,
                    receiverAddress
                )
            )
        }
    }


}