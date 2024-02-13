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
import com.walletconnect.web3.wallet.client.Wallet
import com.walletconnect.web3.wallet.client.Web3Wallet
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

                Web3Wallet.respondSessionRequest(result,
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

            val params = sessionRequest?.param
            val id = sessionRequest?.requestId
            val chainId = sessionRequest?.chain
            val request = sessionRequest?.method

            loge(
                "Approve",
                ":: Session :: ${transactionModelDApp.chainId}  address : ${
                    com.app.plutope.model.Wallet.getPrivateKeyData(
                        CoinType.ETHEREUM
                    )
                }"
            )

            if (sessionRequest != null) {


                val signature = signPersonalMessage(
                    sessionRequest.param,
                    com.app.plutope.model.Wallet.getPrivateKeyData(CoinType.ETHEREUM)
                )
                val result: String = when {
                    sessionRequest.method == PERSONAL_SIGN_METHOD -> {
                        signature
                    }

                    sessionRequest.method == ETH_SIGN_DATA -> {
                        // signature

                        // val x = getSignTypedDataParamsData(sessionRequest.param)

                        // loge("getSignTypedDataParamsData","$x")

                        /*val (domain, types, data) = getSignTypedDataParamsData(Gson().toJson(sessionRequest.param) as List<String>)
                        types.remove("EIP712Domain")
                        val signedData = wallet._signTypedData(domain, types, data)
                        val jsonRpcResult = formatJsonRpcResult(id, signedData)*/



                        signTypedData(
                            sessionRequest.param,
                            com.app.plutope.model.Wallet.getPrivateKeyData(CoinType.ETHEREUM)
                        )


                    }

                    sessionRequest.chain?.contains(
                        transactionModelDApp.chainId, true
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

                Web3Wallet.respondSessionRequest(response,
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
        Web3Wallet.getActiveSessionByTopic(sessionRequest.topic)?.redirect?.toUri()
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
}


private const val PERSONAL_SIGN_METHOD = "personal_sign"
private const val ETH_SIGN_DATA = "eth_signTypedData_v4"