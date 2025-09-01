package com.app.plutope.utils.walletConnection

import android.app.Activity
import com.app.plutope.BuildConfig
import com.app.plutope.utils.constant.RELAY_URL
import com.app.plutope.utils.walletConnection.state.ConnectionState
import com.app.plutope.utils.walletConnection.state.connectionStateFlow
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.reown.android.Core
import com.reown.android.CoreClient
import com.reown.android.internal.common.scope
import com.reown.android.relay.ConnectionType
import com.reown.walletkit.client.Wallet
import com.reown.walletkit.client.WalletKit
import kotlinx.coroutines.launch
import org.web3j.crypto.Credentials
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.Hash
import org.web3j.crypto.Sign
import org.web3j.utils.Numeric
import java.nio.charset.StandardCharsets


object WalletConnectionUtils {
    fun initialWalletConnection(activity: Activity) {

        val serverUrl = "wss://$RELAY_URL?projectId=${BuildConfig.PROJECT_ID}"
        val appMetaData = Core.Model.AppMetaData(
            name = "PlutoPe Wallet",
            description = "PlutoPe Wallet Implementation",
            url = "https://www.plutope.io/",
            icons = listOf("https://plutope.app/api/images/applogo.png"),
            redirect = /*"https://presale.plutope.io/"*/ "plutope://request"
        )

        CoreClient.initialize(
            relayServerUrl = serverUrl,
            connectionType = ConnectionType.AUTOMATIC,
            application = activity.application,
            metaData = appMetaData
        ) { error ->
            Firebase.crashlytics.recordException(error.throwable)
            print(error.throwable.stackTraceToString())
            scope.launch {
                connectionStateFlow.emit(ConnectionState.Error(error.throwable.message ?: ""))
            }
        }

        val initParams = Wallet.Params.Init(core = CoreClient)
        WalletKit.initialize(initParams) { error ->
            print(error.throwable.stackTraceToString())
        }

        /*  NotifyClient.initialize(
              init = Notify.Params.Init(CoreClient)
          ) { error ->
              loge(tag = "NotifyClient", error.throwable.stackTraceToString())
          }

          handleNotifyMessages(activity)*/

    }

    /* private fun handleNotifyMessages(activity: Activity) {
         val scope = CoroutineScope(Dispatchers.Default)

         val notifyEventsJob = NotifyDelegate.notifyEvents
             .filterIsInstance<Notify.Event.Notification>()
             .onEach { notification -> NotificationHandler.addNotification(notification.notification.notification) }
             .launchIn(scope)


         val notificationDisplayingJob =
             NotificationHandler.startNotificationDisplayingJob(scope, activity)


         notifyEventsJob.invokeOnCompletion { cause ->
             onScopeCancelled(cause, "notifyEventsJob")
         }

         notificationDisplayingJob.invokeOnCompletion { cause ->
             onScopeCancelled(cause, "notificationDisplayingJob")
         }
     }
 */
    private fun onScopeCancelled(error: Throwable?, job: String) {

    }


    object WalletConnectionMethod {
        var personalSignIn = "personal_sign"
        var ethSignTransaction = "eth_signTransaction"
        var ethSendTransaction = "eth_sendTransaction"
        var walletSwitchEthereumChain = "wallet_switchEthereumChain"
        var ethSignTypedDataV4 = "eth_signTypedData_v4"
        var ethSign = "eth_sign"
    }
}

fun signPersonalMessage(message: String, privateKey: String): String {
    val credentials = Credentials.create(privateKey)
    val signedMessage = Sign.signPrefixedMessage(
        message.toByteArray(StandardCharsets.UTF_8),
        credentials.ecKeyPair
    )
    val r = Numeric.toHexStringNoPrefix(signedMessage.r)
    val s = Numeric.toHexStringNoPrefix(signedMessage.s)
    val v = Numeric.toHexStringNoPrefix(signedMessage.v)
    return "0x$r$s$v"
}


fun signTypedData(jsonMessage: String, privateKey: String): String {
    val credentials = Credentials.create(ECKeyPair.create(Numeric.toBigInt(privateKey)))
    val messageHash = Hash.sha3(jsonMessage.toByteArray(StandardCharsets.UTF_8))
    val signatureData = Sign.signMessage(messageHash, credentials.ecKeyPair)
    val r = Numeric.toHexStringNoPrefix(signatureData.r)
    val s = Numeric.toHexStringNoPrefix(signatureData.s)
    val v = Numeric.toHexStringNoPrefix(signatureData.v)
    return "0x$r$s$v"

}


















