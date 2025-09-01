package com.app.plutope.utils.walletConnection.session_request

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import com.app.plutope.model.TransactionModelDApp
import com.app.plutope.utils.coinTypeEnum.CoinType
import com.app.plutope.utils.loge
import com.app.plutope.utils.walletConnection.compose_ui.peer.PeerUI
import com.app.plutope.utils.walletConnection.compose_ui.peer.toPeerUI
import com.app.plutope.utils.walletConnection.sessionProposalEvent
import com.app.plutope.utils.walletConnection.sessionRequestEvent
import com.app.plutope.utils.walletConnection.signPersonalMessage
import com.app.plutope.utils.walletConnection.signTypedData
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.reown.walletkit.client.Wallet
import com.reown.walletkit.client.WalletKit
import org.json.JSONArray
import org.web3j.utils.Numeric.hexStringToByteArray
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class SessionRequestViewModel : ViewModel() {
    var sessionRequest: SessionRequestUI = generateSessionRequestUI()
    private fun clearSessionRequest() {
        //WCDelegate.sessionProposalEvent = null
        loge("clearSessionRequest", "Now clear session")
        sessionProposalEvent = null
        sessionRequest = SessionRequestUI.Initial
    }

    suspend fun reject(sendSessionRequestResponseDeepLink: (Uri) -> Unit) {
        return suspendCoroutine { continuation ->
            val sessionRequest = sessionRequest as? SessionRequestUI.Content
            loge(
                tag = "Reject",
                "sessionRequestEvent => ${sessionRequestEvent}  ::: sessionRequest=> " + Gson().toJson(
                    sessionRequest
                )
            )
            if (sessionRequest != null) {
                val result = Wallet.Params.SessionRequestResponse(
                    sessionTopic = sessionRequest.topic,
                    jsonRpcResponse = Wallet.Model.JsonRpcResponse.JsonRpcError(
                        id = sessionRequest.requestId,
                        code = 500,
                        message = "PlutoPe : Transaction Confirmation rejected."
                    )
                )

                WalletKit.respondSessionRequest(result,
                    onSuccess = {
                        continuation.resume(Unit)
                        sessionRequestEvent = null
                        sendResponseDeepLink(sessionRequest, sendSessionRequestResponseDeepLink)
                        clearSessionRequest()
                    },
                    onError = { error ->
                        continuation.resumeWithException(error.throwable)
                        sessionRequestEvent = null
                        sendResponseDeepLink(sessionRequest, sendSessionRequestResponseDeepLink)
                        clearSessionRequest()
                    })
            }
        }
    }

    private fun extractMessageParamFromPersonalSign(input: String): String {
        val jsonArray = JSONArray(input)
        return if (jsonArray.length() > 0) {
            String(hexStringToByteArray(jsonArray.getString(0)))
        } else {
            throw IllegalArgumentException()
        }
    }

    suspend fun approve(
        transactionModelDApp: TransactionModelDApp,
        transactionHash: String,
        sendSessionRequestResponseDeepLink: (Uri) -> Unit
    ) {
        return suspendCoroutine { continuation ->
            val sessionRequest = sessionRequest as? SessionRequestUI.Content
            if (sessionRequest != null) {
                val signature = signPersonalMessage(
                    sessionRequest.param,
                    com.app.plutope.model.Wallet.getPrivateKeyData(CoinType.ETHEREUM)
                )
                val result: String = when {
                    sessionRequest.method == PERSONAL_SIGN_METHOD -> {
                        signature
                    }

                    /*sessionRequest.method == SWITCH_WALLET_CHAIN -> {

                    }*/

                    sessionRequest.method == ETH_SIGN_DATA -> {
                        signTypedData(
                            sessionRequest.param,
                            com.app.plutope.model.Wallet.getPrivateKeyData(CoinType.ETHEREUM)
                        )
                    }

                    sessionRequest.chain?.contains(
                        transactionModelDApp.chainId,
                        true
                    ) == true -> transactionHash

                    else -> throw Exception("Unsupported Chain")

                }


                val response = Wallet.Params.SessionRequestResponse(
                    sessionTopic = sessionRequest.topic,
                    jsonRpcResponse = Wallet.Model.JsonRpcResponse.JsonRpcResult(
                        sessionRequest.requestId,
                        result
                    )
                )

                loge("response", "result : $result \n\n Response = >$response")

                WalletKit.respondSessionRequest(response,
                    onSuccess = {
                        loge("respondSessionRequest", "onSuccess => $it")
                        continuation.resume(Unit)
                        sessionRequestEvent = null
                        sendResponseDeepLink(sessionRequest, sendSessionRequestResponseDeepLink)
                        clearSessionRequest()
                    },
                    onError = { error ->
                        loge("respondSessionRequest", "onError => $error")
                        continuation.resumeWithException(error.throwable)
                        sessionRequestEvent = null
                        Firebase.crashlytics.recordException(error.throwable)
                        sendResponseDeepLink(sessionRequest, sendSessionRequestResponseDeepLink)
                        clearSessionRequest()
                    })
            }
        }
    }

    private fun sendResponseDeepLink(
        sessionRequest: SessionRequestUI.Content,
        sendSessionRequestResponseDeepLink: (Uri) -> Unit,
    ) {
        loge("TAG", "sendResponseDeepLink ==> $sessionRequest")
        WalletKit.getActiveSessionByTopic(sessionRequest.topic)?.redirect?.toUri()
            ?.let { deepLinkUri ->
                loge("getActiveSessionByTopic", "$deepLinkUri")
                sendSessionRequestResponseDeepLink(deepLinkUri)
            }
    }

    fun generateSessionRequestUI(): SessionRequestUI {
        return if (sessionRequestEvent != null) {
            val (sessionRequest, context) = sessionRequestEvent!!
            SessionRequestUI.Content(
                peerUI = PeerUI(
                    peerName = sessionRequest.peerMetaData?.name ?: "",
                    peerIcon = sessionRequest.peerMetaData?.icons?.firstOrNull() ?: "",
                    peerUri = sessionRequest.peerMetaData?.url ?: "",
                    peerDescription = sessionRequest.peerMetaData?.description ?: "",
                ),
                topic = sessionRequest.topic,
                requestId = sessionRequest.request.id,
                param = if (sessionRequest.request.method == PERSONAL_SIGN_METHOD) extractMessageParamFromPersonalSign(
                    sessionRequest.request.params
                ) else sessionRequest.request.params,
                chain = sessionRequest.chainId,
                method = sessionRequest.request.method,
                peerContextUI = context.toPeerUI()
            )
        } else SessionRequestUI.Initial
    }

    /*  internal fun createSwitchChainParams(chain: WalletModel.Model.Chain): String {
          val chainHex = chain.chainReference.toInt().toString(radix = 16)

          @Language("JSON")
          val param = """
              [
                {
                  "chainId": "0x$chainHex"
                }
              ]
          """
          return param.formatParams()
      }*/
}


private const val PERSONAL_SIGN_METHOD = "personal_sign"
private const val SWITCH_WALLET_CHAIN = "wallet_switchEthereumChain"
private const val ETH_SIGN_DATA = "eth_signTypedData_v4"
