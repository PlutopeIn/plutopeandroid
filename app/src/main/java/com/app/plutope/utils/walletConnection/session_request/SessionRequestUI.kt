package com.app.plutope.utils.walletConnection.session_request

import com.app.plutope.utils.walletConnection.compose_ui.peer.PeerContextUI
import com.app.plutope.utils.walletConnection.compose_ui.peer.PeerUI

sealed class SessionRequestUI {
    object Initial : SessionRequestUI()

    data class Content(
        val peerUI: PeerUI,
        val topic: String,
        val requestId: Long,
        val param: String,
        val chain: String?,
        val method: String,
        val peerContextUI: PeerContextUI
    ) : SessionRequestUI()
}