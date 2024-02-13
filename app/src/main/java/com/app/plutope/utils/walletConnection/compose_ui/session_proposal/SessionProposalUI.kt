package com.app.plutope.utils.walletConnection.compose_ui.session_proposal

import com.app.plutope.utils.walletConnection.ACCOUNTS_1_EIP155_ADDRESS
import com.app.plutope.utils.walletConnection.compose_ui.peer.PeerContextUI
import com.app.plutope.utils.walletConnection.compose_ui.peer.PeerUI
import com.walletconnect.web3.wallet.client.Wallet

data class SessionProposalUI(
    val peerUI: PeerUI,
    val namespaces: Map<String, Wallet.Model.Namespace.Proposal>,
    val optionalNamespaces: Map<String, Wallet.Model.Namespace.Proposal> = mapOf(),
    val peerContext: PeerContextUI,
    val redirect: String,
    val pubKey: String,
)

data class WalletMetaData(
    val peerUI: PeerUI,
    val namespaces: Map<String, Wallet.Model.Namespace.Session>,
)

val walletMetaData = WalletMetaData(
    peerUI = PeerUI(
        peerIcon = "https://raw.githubusercontent.com/WalletConnect/walletconnect-assets/master/Icon/Gradient/Icon.png",
        peerName = "plutope",
        peerUri = "com.app",
        peerDescription = ""
    ),
    namespaces = mapOf(
        "eip155" to Wallet.Model.Namespace.Session(
            chains = listOf("eip155:1", "eip155:137", "eip155:56", "eip155:66"),
            methods = listOf(
                "eth_sendTransaction",
                "personal_sign",
                "eth_accounts",
                "eth_requestAccounts",
                "eth_call",
                "eth_getBalance",
                "eth_sendRawTransaction",
                "eth_sign",
                "eth_signTransaction",
                "eth_signTypedData",
                "eth_signTypedData_v3",
                "eth_signTypedData_v4", "stellar_signXDR", "stellar_signAndSubmitXDR"
            ),
            events = listOf("chainChanged", "accountsChanged"),
            accounts = listOf(
                "eip155:1:$ACCOUNTS_1_EIP155_ADDRESS",
                "eip155:137:$ACCOUNTS_1_EIP155_ADDRESS",
                "eip155:56:$ACCOUNTS_1_EIP155_ADDRESS",
                "eip155:66:$ACCOUNTS_1_EIP155_ADDRESS"
            )
        ),
//        "cosmos" to Wallet.Model.Namespace.Session(
//            chains = listOf("cosmos:cosmoshub-4", "cosmos:cosmoshub-1"),
//            methods = listOf("accountsChanged", "personalSign"),
//            events = listOf("chainChanged", "chainChanged"),
//            accounts = listOf("cosmos:cosmoshub-4:cosmos1w605a5ejjlhp04eahjqxhjhmg8mj6nqhp8v6xc", "cosmos:cosmoshub-1:cosmos1w605a5ejjlhp04eahjqxhjhmg8mj6nqhp8v6xc")
//        )
    )
)