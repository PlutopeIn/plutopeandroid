package com.app.plutope.ui.fragment.wallet_connection.walletConnectionList

import com.app.plutope.ui.base.BaseViewModel
import com.app.plutope.utils.common.CommonNavigator
import com.app.plutope.utils.loge
import com.app.plutope.utils.walletConnection.compose_ui.peer.PeerUI
import com.app.plutope.utils.walletConnection.compose_ui.peer.toPeerUI
import com.app.plutope.utils.walletConnection.compose_ui.session_proposal.SessionProposalUI
import com.app.plutope.utils.walletConnection.compose_ui.session_proposal.walletMetaData
import com.app.plutope.utils.walletConnection.sessionProposalEvent
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.walletconnect.web3.wallet.client.Wallet
import com.walletconnect.web3.wallet.client.Web3Wallet
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@HiltViewModel
class WalletConnectViewModel @Inject constructor() : BaseViewModel<CommonNavigator>() {

    val sessionProposal: SessionProposalUI? = generateSessionProposalUI()


    suspend fun approve(proposalPublicKey: String, onRedirect: (String) -> Unit = {}) {
        return suspendCoroutine { continuation ->
            if (Web3Wallet.getSessionProposals().isNotEmpty()) {
                val sessionProposal: Wallet.Model.SessionProposal = requireNotNull(
                    Web3Wallet.getSessionProposals()
                        .find { it.proposerPublicKey == proposalPublicKey })
                val sessionNamespaces = Web3Wallet.generateApprovedNamespaces(
                    sessionProposal = sessionProposal,
                    supportedNamespaces = walletMetaData.namespaces
                )
                val approveProposal = Wallet.Params.SessionApprove(
                    proposerPublicKey = sessionProposal.proposerPublicKey,
                    namespaces = sessionNamespaces
                )

                Web3Wallet.approveSession(approveProposal,
                    onError = { error ->
                        continuation.resumeWithException(error.throwable)
                        Firebase.crashlytics.recordException(error.throwable)
                        sessionProposalEvent = null
                        onRedirect(sessionProposal.redirect)
                    },
                    onSuccess = {
                        continuation.resume(Unit)
                        sessionProposalEvent = null
                        onRedirect(sessionProposal.redirect)
                    })
            }
        }
    }

    suspend fun reject(proposalPublicKey: String, onRedirect: (String) -> Unit = {}) {
        return suspendCoroutine { continuation ->
            if (Web3Wallet.getSessionProposals().isNotEmpty()) {
                val sessionProposal: Wallet.Model.SessionProposal = requireNotNull(
                    Web3Wallet.getSessionProposals()
                        .find { it.proposerPublicKey == proposalPublicKey })
                val rejectionReason = "Reject Session"
                val reject = Wallet.Params.SessionReject(
                    proposerPublicKey = sessionProposal.proposerPublicKey,
                    reason = rejectionReason
                )

                Web3Wallet.rejectSession(reject,
                    onSuccess = {
                        continuation.resume(Unit)
                        sessionProposalEvent = null
                        onRedirect(sessionProposal.redirect)
                    },
                    onError = { error ->
                        continuation.resumeWithException(error.throwable)
                        Firebase.crashlytics.recordException(error.throwable)
                        sessionProposalEvent = null
                        onRedirect(sessionProposal.redirect)
                    })
            }
        }
    }


    private fun generateSessionProposalUI(): SessionProposalUI? {

        loge("TAG", "generateSessionProposalUI: ${sessionProposalEvent}")

        return if (sessionProposalEvent != null) {
            val (proposal, context) = sessionProposalEvent!!
            SessionProposalUI(
                peerUI = PeerUI(
                    peerIcon = proposal.icons.firstOrNull().toString(),
                    peerName = proposal.name,
                    peerDescription = proposal.description,
                    peerUri = proposal.url,
                ),
                namespaces = proposal.requiredNamespaces,
                optionalNamespaces = proposal.optionalNamespaces,
                peerContext = context.toPeerUI(),
                redirect = proposal.redirect,
                pubKey = proposal.proposerPublicKey
            )
        } else null
    }


}