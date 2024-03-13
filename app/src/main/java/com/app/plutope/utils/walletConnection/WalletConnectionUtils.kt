package com.app.plutope.utils.walletConnection

import android.app.Activity
import com.app.plutope.BuildConfig
import com.app.plutope.ui.fragment.wallet_connection.SignTypedDataParams
import com.app.plutope.ui.fragment.wallet_connection.SignTypedDataV4Request
import com.app.plutope.utils.coinTypeEnum.CoinType
import com.app.plutope.utils.constant.RELAY_URL
import com.app.plutope.utils.loge
import com.app.plutope.utils.walletConnection.compose_ui.domain.NotificationHandler
import com.app.plutope.utils.walletConnection.state.ConnectionState
import com.app.plutope.utils.walletConnection.state.connectionStateFlow
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.android.cacao.signature.SignatureType
import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.android.relay.ConnectionType
import com.walletconnect.android.utils.cacao.sign
import com.walletconnect.foundation.util.Logger
import com.walletconnect.notify.client.Notify
import com.walletconnect.notify.client.NotifyClient
import com.walletconnect.notify.client.cacao.CacaoSigner
import com.walletconnect.web3.wallet.client.Wallet
import com.walletconnect.web3.wallet.client.Web3Wallet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import org.koin.core.qualifier.named
import org.web3j.crypto.Credentials
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.Hash
import org.web3j.crypto.Sign
import org.web3j.utils.Numeric
import java.lang.reflect.Type
import java.nio.charset.StandardCharsets


object WalletConnectionUtils {
    // lateinit var mixPanel: MixpanelAPI
    fun initialWalletConnection(activity: Activity) {

        val serverUrl = "wss://$RELAY_URL?projectId=${BuildConfig.PROJECT_ID}"
        val appMetaData = Core.Model.AppMetaData(
            name = "PlutoPe Wallet",
            description = "PlutoPe Wallet Implementation",
            url = "https://www.plutope.io/",
            icons = listOf("https://plutope.app/api/images/applogo.png"),
            redirect = "kotlin-web3wallet:/request"
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


        Web3Wallet.initialize(initParams) { error ->
            print(error.throwable.stackTraceToString())
        }

        NotifyClient.initialize(
            init = Notify.Params.Init(CoreClient)
        ) { error ->
            //  Firebase.crashlytics.recordException(error.throwable)
            loge(tag = "NotifyClient", error.throwable.stackTraceToString())
        }

        //  registerAccount()
        // initializeBeagle()
        // For testing purposes onl
        handleNotifyMessages(activity)

    }

    private fun handleNotifyMessages(activity: Activity) {
        val scope = CoroutineScope(Dispatchers.Default)

        val notifyEventsJob = NotifyDelegate.notifyEvents
            .filterIsInstance<Notify.Event.Notification>()
            .onEach { notification -> NotificationHandler.addNotification(notification.notification.message) }
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

    private fun onScopeCancelled(error: Throwable?, job: String) {
        wcKoinApp.koin.get<Logger>(named(AndroidCommonDITags.LOGGER))
            .error("onScopeCancelled($job): $error")
    }

    private fun registerAccount() {
        val account = com.app.plutope.model.Wallet.getPublicWalletAddress(CoinType.ETHEREUM)
        val domain = BuildConfig.APPLICATION_ID
        val allApps = true

        val isRegistered = NotifyClient.isRegistered(
            params = Notify.Params.IsRegistered(
                account = account!!,
                domain = domain,
                allApps = allApps
            )
        )

        if (!isRegistered) {
            NotifyClient.prepareRegistration(
                params = Notify.Params.PrepareRegistration(
                    account = account!!,
                    domain = domain,
                    allApps = allApps
                ),
                onSuccess = { cacaoPayloadWithIdentityPrivateKey, message ->
                    loge(message = "PrepareRegistration Success")

                    val signature = CacaoSigner.sign(
                        message,
                        com.app.plutope.model.Wallet.getPrivateKeyData(CoinType.ETHEREUM)
                            .hexToBytes(),
                        SignatureType.EIP191
                    )

                    NotifyClient.register(
                        params = Notify.Params.Register(
                            cacaoPayloadWithIdentityPrivateKey = cacaoPayloadWithIdentityPrivateKey,
                            signature = signature
                        ),
                        onSuccess = { loge(message = "Register Success") },
                        onError = {
                            loge(
                                tag = "Error",
                                message = it.throwable.stackTraceToString()
                            )
                        }
                    )

                },
                onError = { loge(tag = "OnError", message = it.throwable.stackTraceToString()) }
            )
        } else {
            loge(message = "$account is already registered")
        }
    }

    fun String.hexToBytes(): ByteArray {
        val len = this.length
        val data = ByteArray(len / 2)
        var i = 0

        while (i < len) {
            data[i / 2] = ((Character.digit(this[i], 16) shl 4)
                    + Character.digit(this[i + 1], 16)).toByte()
            i += 2
        }

        return data
    }

    object WalletConnectionMethod {
        var personalSignIn = "personal_sign"
        var ethSignTransaction = "eth_signTransaction"
        var ethSendTransaction = "eth_sendTransaction"
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
    /* val msg = getTypesFromMessage(jsonMessage)
     loge("msg","msg=> ${msg}")*/

    val credentials = Credentials.create(ECKeyPair.create(Numeric.toBigInt(privateKey)))
    val messageHash = Hash.sha3(jsonMessage.toByteArray(StandardCharsets.UTF_8))
    val signatureData = Sign.signMessage(messageHash, credentials.ecKeyPair)

    /*  val r = Numeric.toHexStringNoPrefix(signatureData.r).padStart(64, '0')
      val s = Numeric.toHexStringNoPrefix(signatureData.s).padStart(64, '0')
      val v = Numeric.toHexStringNoPrefix(signatureData.v)
    */

    val r = Numeric.toHexStringNoPrefix(signatureData.r)
    val s = Numeric.toHexStringNoPrefix(signatureData.s)
    val v = Numeric.toHexStringNoPrefix(signatureData.v)

    // Concatenate the components in the correct order
    return "0x$r$s$v"

}

fun getTypesFromMessage(message: String): String {
    val jsonArray = JSONArray(message)
    loge("jsonArray", "${jsonArray.length()} :: jsonArray $jsonArray")
    return if (jsonArray.length() >= 1) {
        val typesObject = jsonArray.optJSONObject(1)
        loge("typesObject", "$typesObject")
        typesObject.toString()
    } else {
        message
    }
}

fun getSignTypedDataParamsData(params: String): Any {
    //  val data = params.firstOrNull { !isValidAddress(it) }

    return try {

        loge("params", "=>$params")

        val type: Type = object : TypeToken<List<Any>>() {}.type
        val jsonArray: List<Any> = Gson().fromJson(params, type)

        if (jsonArray.size == 2) {
            // val address = jsonArray[0] as String
            val signTypedDataV4Request =
                Gson().fromJson("$params", SignTypedDataV4Request::class.java)

            // val signTypedDataV4Request = Gson().fromJson(jsonArray[1].toString(), SignTypedDataV4Request::class.java)

            // Access the parsed data
            val address = signTypedDataV4Request.address
            val params = Gson().fromJson(
                signTypedDataV4Request.params.toString(),
                SignTypedDataParams::class.java
            )


            //  val types = params.types
            loge("params", "=>$params")
            // Access other properties as needed

        } else {
            // Handle the case where the array does not have the expected structure
        }


        /*   val signTypedDataArrayWrapper = Gson().fromJson(params, SignTypedDataArrayWrapper::class.java)
          val address = signTypedDataArrayWrapper.firstElement
          val signTypedDataV4Request = signTypedDataArrayWrapper.secondElement
          val types = signTypedDataV4Request.params.params["types"]
          loge("types","=>$types")*/


    } catch (e: JSONException) {
        params // Return as string if parsing fails
    }

}
















