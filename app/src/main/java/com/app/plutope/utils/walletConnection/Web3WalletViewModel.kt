package com.app.plutope.utils.walletConnection

import androidx.lifecycle.viewModelScope
import com.app.plutope.ui.base.BaseViewModel
import com.app.plutope.utils.common.CommonNavigator
import com.app.plutope.utils.logd
import com.app.plutope.utils.loge
import com.app.plutope.utils.walletConnection.state.ConnectionState
import com.app.plutope.utils.walletConnection.state.NoAction
import com.app.plutope.utils.walletConnection.state.PairingState
import com.app.plutope.utils.walletConnection.state.SignEvent
import com.reown.walletkit.client.Wallet
import com.reown.walletkit.client.WalletKit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class Web3WalletViewModel @Inject constructor() : BaseViewModel<CommonNavigator>() {

    private val connectivityStateFlow: MutableStateFlow<ConnectionState> =
        MutableStateFlow(ConnectionState.Idle)


    private val _pairingStateSharedFlow: MutableSharedFlow<PairingState> = MutableSharedFlow()
    val pairingStateSharedFlow = _pairingStateSharedFlow.asSharedFlow()


    val walletEvents = WCDelegate.walletEvents.map { wcEvent ->
        logd("Web3Wallet", "VM: $wcEvent")

        when (wcEvent) {
            is Wallet.Model.SessionRequest -> {
                loge("walletEvents", "SessionRequest")
                val topic = wcEvent.topic
                val icon = wcEvent.peerMetaData?.icons?.firstOrNull()
                val peerName = wcEvent.peerMetaData?.name
                val requestId = wcEvent.request.id.toString()
                val params = wcEvent.request.params
                val chain = wcEvent.chainId
                val method = wcEvent.request.method
                val arrayOfArgs: ArrayList<String?> =
                    arrayListOf(topic, icon, peerName, requestId, params, chain, method)

                SignEvent.SessionRequest(wcEvent, arrayOfArgs, arrayOfArgs.size)


            }

            /* is Wallet.Model.AuthRequest -> {
                 viewModelScope.launch {
                     _pairingStateSharedFlow.emit(PairingState.Success)
                 }

                 try {
                     val message = Web3Wallet.formatMessage(
                         Wallet.Params.FormatMessage(
                             wcEvent.payloadParams,
                             ISSUER
                         )
                     ) ?: "Error formatting message"
                     loge("Message", message)
                 } catch (e: Exception) {
                     e.printStackTrace()
                 }
                 AuthEvent.OnRequest(wcEvent.id, "")

             }*/

            is Wallet.Model.SessionDelete -> {
                SignEvent.Disconnect
            }

            is Wallet.Model.SessionProposal -> {
                viewModelScope.launch {
                    _pairingStateSharedFlow.emit(PairingState.Success)
                }
                SignEvent.SessionProposal
            }

            is Wallet.Model.ConnectionState -> {
                val connectionState = if (wcEvent.isAvailable) {
                    ConnectionState.Ok
                } else {
                    ConnectionState.Error("No Internet connection, please check your internet connection and try again")
                }
                connectivityStateFlow.emit(connectionState)
            }

            else -> {
                loge("walletEvents", "No Action")
                NoAction

            }
        }
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed())


    fun pair(pairingUri: String) {
        viewModelScope.launch {
            _pairingStateSharedFlow.emit(PairingState.Loading)
        }

        val pairingParams = Wallet.Params.Pair(pairingUri)
        WalletKit.pair(pairingParams) { error ->
            viewModelScope.launch {
                _pairingStateSharedFlow.emit(PairingState.Error(error.throwable.message ?: ""))
            }
        }
    }
}