package com.app.plutope.utils.walletConnection.compose_ui.session_proposal

import com.app.plutope.ui.base.BaseViewModel
import com.app.plutope.utils.common.CommonNavigator
import com.app.plutope.utils.date_formate.toAny
import com.app.plutope.utils.date_formate.ymdHMS
import com.app.plutope.utils.loge
import com.app.plutope.utils.walletConnection.compose_ui.peer.PeerUI
import com.app.plutope.utils.walletConnection.compose_ui.peer.toPeerUI
import com.app.plutope.utils.walletConnection.sessionProposalEvent
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.walletconnect.web3.wallet.client.Wallet
import com.walletconnect.web3.wallet.client.Web3Wallet
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@HiltViewModel
class SessionProposalViewModel @Inject constructor() : BaseViewModel<CommonNavigator>() {
    val sessionProposal: SessionProposalUI? = generateSessionProposalUI()
    suspend fun approve(proposalPublicKey: String, onRedirect: (String) -> Unit = {}) {
        return suspendCoroutine { continuation ->
            if (Web3Wallet.getSessionProposals().isNotEmpty()) {
                val sessionProposal: Wallet.Model.SessionProposal = requireNotNull(
                    Web3Wallet.getSessionProposals()
                        .find { it.proposerPublicKey == proposalPublicKey })

                loge("Propose_1", "sessionProposal => $sessionProposal")
                loge("Propose_1", " namespaces => ${walletMetaData.namespaces}")

                val sessionNamespaces = Web3Wallet.generateApprovedNamespaces(
                    sessionProposal = sessionProposal,
                    supportedNamespaces = walletMetaData.namespaces
                )

                loge("Propose_2", "$sessionNamespaces")

                val approveProposal = Wallet.Params.SessionApprove(
                    proposerPublicKey = sessionProposal.proposerPublicKey,
                    namespaces = sessionNamespaces
                )

                loge("Propose_3", "$approveProposal")

                Web3Wallet.approveSession(
                    approveProposal,
                    onError = { error ->
                        loge("Propose_1_error", "$error")


                        continuation.resumeWithException(error.throwable)
                        Firebase.crashlytics.recordException(error.throwable)
                        sessionProposalEvent = null
                        onRedirect(sessionProposal.redirect)
                    },
                    onSuccess = {
                        loge("Propose_4", "$approveProposal")
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

                loge("rejectSession =>", "$reject")

                Web3Wallet.rejectSession(
                    reject,
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
        loge("sessionProposalEvent", "==> $sessionProposalEvent")
        return if (sessionProposalEvent != null) {
            val (proposal, context) = sessionProposalEvent!!
            SessionProposalUI(
                peerUI = PeerUI(
                    peerIcon = proposal.icons.firstOrNull().toString(),
                    peerName = proposal.name,
                    peerDescription = proposal.description,
                    peerUri = proposal.url,
                    connectionDate = Calendar.getInstance().toAny(ymdHMS)
                ),
                namespaces = proposal.requiredNamespaces,
                optionalNamespaces = proposal.optionalNamespaces,
                peerContext = context.toPeerUI(),
                redirect = proposal.redirect,
                pubKey = proposal.proposerPublicKey
            )
        } else null
    }

    /*
        private fun buildNamespaces(
            sessionProposal: Wallet.Model.SessionProposal,
            selectedAccounts: List<String>
        ): Map<out String?, Wallet.Model.Namespace.Session?> {
            val supportedNamespaces: Map<String, Wallet.Model.Namespace.Session> = Collections.singletonMap<String, Wallet.Model.Namespace.Session>(
                "eip155", Wallet.Model.Namespace.Session(
                    getSupportedChains(),
                    toCAIP10(getSupportedChains(), selectedAccounts),
                    getSupportedMethods(),
                    getSupportedEvents()
                )
            )
            try {
                return generateApprovedNamespaces(sessionProposal, supportedNamespaces)
            } catch (e: Exception) {
               // Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                Timber.tag("PlutoPE").e(e)
            }
            return HashMap<String?, Wallet.Model.Namespace.Session?>()
        }
    */


}