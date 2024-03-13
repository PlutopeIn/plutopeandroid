package com.app.plutope.utils.walletConnection.compose_ui.session_proposal

import com.app.plutope.utils.coinTypeEnum.CoinType
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
        peerIcon = "https://plutope.app/api/images/applogo.png",
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
                "eth_signTypedData_v4",
                "stellar_signXDR",
                "stellar_signAndSubmitXDR",
                "solana_signTransaction",
                "solana_signMessage",
                "wallet_switchEthereumChain",
                "wallet_addEthereumChain",
                "eth_chainId"

            ),
            events = listOf("chainChanged", "accountsChanged"),
            accounts = listOf(
                "eip155:1:${com.app.plutope.model.Wallet.getPublicWalletAddress(CoinType.ETHEREUM)}",
                "eip155:137:${com.app.plutope.model.Wallet.getPublicWalletAddress(CoinType.ETHEREUM)}",
                "eip155:56:${com.app.plutope.model.Wallet.getPublicWalletAddress(CoinType.ETHEREUM)}",
                "eip155:66:${com.app.plutope.model.Wallet.getPublicWalletAddress(CoinType.ETHEREUM)}"
            )
        ),

        )
)