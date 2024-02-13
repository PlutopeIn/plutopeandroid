package com.app.plutope.utils.walletConnection.compose_ui.connections

import com.walletconnect.web3.wallet.client.Wallet


data class ConnectionUI(
    val id: Int,
    val type: ConnectionType,
    val name: String,
    val uri: String,
    val icon: String?,
)

sealed class ConnectionType {
    data class Sign(
        val topic: String,
        val namespaces: Map<String, Wallet.Model.Namespace.Session>
    ) : ConnectionType()
}

