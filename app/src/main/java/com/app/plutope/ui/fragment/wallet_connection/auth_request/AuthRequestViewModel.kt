package com.app.plutope.ui.fragment.wallet_connection.auth_request

import androidx.lifecycle.ViewModel
import com.app.plutope.utils.loge
import com.app.plutope.utils.walletConnection.ISSUER
import com.app.plutope.utils.walletConnection.PRIVATE_KEY_1
import com.app.plutope.utils.walletConnection.WCDelegate
import com.app.plutope.utils.walletConnection.compose_ui.peer.PeerUI
import com.app.plutope.utils.walletConnection.compose_ui.peer.toPeerUI
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.walletconnect.android.cacao.signature.SignatureType
import com.walletconnect.android.utils.cacao.signHex
import com.walletconnect.web3.wallet.client.Wallet
import com.walletconnect.web3.wallet.client.Web3Wallet
import com.walletconnect.web3.wallet.utils.CacaoSigner
import org.web3j.utils.Numeric.toHexString
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class AuthRequestViewModel : ViewModel() {
    val authRequest: AuthRequestUI?
        get() = generateAuthRequestUI()

    suspend fun approve() {
        return suspendCoroutine { continuation ->
            if (WCDelegate.authRequestEvent != null) {

                loge("Msg", authRequest!!.message)
                val request = requireNotNull(WCDelegate.authRequestEvent!!.first)
                var formate = Wallet.Params.FormatMessage(request.payloadParams, ISSUER)
                loge(
                    "approve",
                    "request: $request  \n payloadParams => ${request.payloadParams}  \n ISSUER:: $ISSUER  $formate"
                )


                val message = Web3Wallet.formatMessage(
                    Wallet.Params.FormatMessage(
                        request.payloadParams,
                        ISSUER
                    )
                ) ?: throw Exception("Error formatting message")


//                val credentials: Credentials = Credentials.create(token.chain?.privateKey)
//                var signature = Sign.signPrefixedMessage(message.toByteArray(), credentials.ecKeyPair)


                Web3Wallet.respondAuthRequest(
                    Wallet.Params.AuthRequestResponse.Result(
                        id = request.id,
                        signature = CacaoSigner.signHex(
                            toHexString(message.toByteArray()),
                            PRIVATE_KEY_1,
                            SignatureType.EIP191
                        ),
                        issuer = ISSUER
                    ),
                    onSuccess = {
                        WCDelegate.authRequestEvent = null
                        AuthRequestStore.addActiveSession(request)
                        continuation.resume(Unit)
                    },
                    onError = { error ->
                        //  Firebase.crashlytics.recordException(error.throwable)
                        AuthRequestStore.removeActiveSession(request)
                        WCDelegate.authRequestEvent = null
                        continuation.resumeWithException(error.throwable)
                    })
            }
        }
    }

    suspend fun reject() {
        return suspendCoroutine { continuation ->
            if (WCDelegate.authRequestEvent != null) {
                val request = requireNotNull(WCDelegate.authRequestEvent!!.first)
                //todo: Define Error Codes
                Web3Wallet.respondAuthRequest(
                    Wallet.Params.AuthRequestResponse.Error(
                        request.id, 12001, "User Rejected Request"
                    ),
                    onSuccess = {
                        WCDelegate.authRequestEvent = null
                        continuation.resume(Unit)
                    },
                    onError = { error ->
                        Firebase.crashlytics.recordException(error.throwable)
                        WCDelegate.authRequestEvent = null
                        continuation.resumeWithException(error.throwable)
                    })
            }
        }
    }

    private fun generateAuthRequestUI(): AuthRequestUI? {
        return if (WCDelegate.authRequestEvent != null) {
            val (authRequest, authContext) = WCDelegate.authRequestEvent!!
            val message = Web3Wallet.formatMessage(
                Wallet.Params.FormatMessage(
                    authRequest.payloadParams,
                    ISSUER
                )
            ) ?: throw Exception("Error formatting message")

            loge("generateAuthRequestUI", "  ${message}")

            AuthRequestUI(
                peerUI = PeerUI(
                    peerIcon = "https://raw.githubusercontent.com/WalletConnect/walletconnect-assets/master/Icon/Gradient/Icon.png",
                    peerName = "WalletConnect",
                    peerUri = "https://walletconnect.com/",
                    peerDescription = "The communications protocol for web3.",
                ),
                message = message,
                peerContextUI = authContext.toPeerUI()
            )
        } else null
    }
}