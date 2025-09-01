package com.app.plutope.utils.walletConnection

import com.app.plutope.utils.loge
import com.reown.android.Core
import com.reown.android.CoreClient
import com.reown.walletkit.client.Wallet
import com.reown.walletkit.client.WalletKit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

object WCDelegate : WalletKit.WalletDelegate, CoreClient.CoreDelegate {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _coreEvents: MutableSharedFlow<Core.Model> = MutableSharedFlow()
    val coreEvents: SharedFlow<Core.Model> = _coreEvents.asSharedFlow()

    private val _walletEvents: MutableSharedFlow<Wallet.Model> = MutableSharedFlow()
    val walletEvents: SharedFlow<Wallet.Model> = _walletEvents.asSharedFlow()
    // var authRequestEvent: Pair<Wallet.Model.AuthRequest, Wallet.Model.VerifyContext>? = null

    //  var sessionProposalEvent: Pair<Wallet.Model.SessionProposal, Wallet.Model.VerifyContext>? = null
    //var sessionRequestEvent: Pair<Wallet.Model.SessionRequest, Wallet.Model.VerifyContext>? = null

    init {
        CoreClient.setDelegate(this)
        WalletKit.setWalletDelegate(this)
    }

    /*  override fun onAuthRequest(
          authRequest: Wallet.Model.AuthRequest,
          verifyContext: Wallet.Model.VerifyContext
      ) {
          authRequestEvent = Pair(authRequest, verifyContext)

          scope.launch {
              _walletEvents.emit(authRequest)
          }
      }*/

    override fun onConnectionStateChange(state: Wallet.Model.ConnectionState) {
        scope.launch {
            _walletEvents.emit(state)
        }
    }


    override fun onError(error: Wallet.Model.Error) {
        scope.launch {
            _walletEvents.emit(error)
        }
    }

    override fun onProposalExpired(proposal: Wallet.Model.ExpiredProposal) {

    }

    override fun onRequestExpired(request: Wallet.Model.ExpiredRequest) {

    }

    override fun onSessionDelete(sessionDelete: Wallet.Model.SessionDelete) {
        scope.launch {
            _walletEvents.emit(sessionDelete)
        }
    }

    override fun onSessionExtend(session: Wallet.Model.Session) {
        loge("onSessionExtend", "${session.expiry}")
    }

    override fun onSessionProposal(
        sessionProposal: Wallet.Model.SessionProposal,
        verifyContext: Wallet.Model.VerifyContext
    ) {
        sessionProposalEvent = Pair(sessionProposal, verifyContext)
        scope.launch {
            _walletEvents.emit(sessionProposal)
        }
    }

    override fun onSessionRequest(
        sessionRequest: Wallet.Model.SessionRequest,
        verifyContext: Wallet.Model.VerifyContext
    ) {
        sessionRequestEvent = Pair(sessionRequest, verifyContext)
        scope.launch {
            _walletEvents.emit(sessionRequest)
        }
    }

    override fun onSessionSettleResponse(settleSessionResponse: Wallet.Model.SettledSessionResponse) {
        scope.launch {
            _walletEvents.emit(settleSessionResponse)
        }
    }

    override fun onSessionUpdateResponse(sessionUpdateResponse: Wallet.Model.SessionUpdateResponse) {
        scope.launch {
            _walletEvents.emit(sessionUpdateResponse)
        }
    }


    override fun onPairingDelete(deletedPairing: Core.Model.DeletedPairing) {
        scope.launch {
            _coreEvents.emit(deletedPairing)
        }
    }

    override fun onPairingExpired(expiredPairing: Core.Model.ExpiredPairing) {
        scope.launch {
            _coreEvents.emit(expiredPairing)
        }
    }

    override fun onPairingState(pairingState: Core.Model.PairingState) {
        scope.launch {
            _coreEvents.emit(pairingState)
        }
    }
}