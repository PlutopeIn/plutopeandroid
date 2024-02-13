package com.app.plutope.ui.fragment.wallet_connection.auth_request

import com.app.plutope.utils.walletConnection.compose_ui.peer.PeerContextUI
import com.app.plutope.utils.walletConnection.compose_ui.peer.PeerUI

data class AuthRequestUI(
    val peerUI: PeerUI,
    val message: String,
    val peerContextUI: PeerContextUI,
)