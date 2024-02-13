package com.app.plutope.utils.walletConnection.session_request

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.app.plutope.utils.walletConnection.compose_ui.Buttons
import com.app.plutope.utils.walletConnection.compose_ui.SemiTransparentDialog
import com.app.plutope.utils.walletConnection.compose_ui.common.Content
import com.app.plutope.utils.walletConnection.compose_ui.common.InnerContent
import com.app.plutope.utils.walletConnection.compose_ui.common.blue.BlueLabelRow
import com.app.plutope.utils.walletConnection.compose_ui.peer.Peer
import com.app.plutope.utils.walletConnection.compose_ui.peer.PeerUI
import com.app.plutope.utils.walletConnection.compose_ui.peer.getColor
import com.app.plutope.utils.walletConnection.compose_ui.peer.verified_color
import com.app.plutope.utils.walletConnection.compose_ui.themedColor
import com.app.plutope.utils.walletConnection.showSnackbar
import kotlinx.coroutines.launch


@Composable
fun SessionRequestRoute(
    navController: NavHostController,
    sessionRequestViewModel: SessionRequestViewModel = viewModel()
) {
    val sessionRequestUI = sessionRequestViewModel.sessionRequest
    val composableScope = rememberCoroutineScope()
    val context = LocalContext.current

    when (sessionRequestUI) {
        is SessionRequestUI.Content -> {
            val allowButtonColor = getColor(sessionRequestUI.peerContextUI)

            SemiTransparentDialog {
                Spacer(modifier = Modifier.height(24.dp))
                Peer(
                    peerUI = sessionRequestUI.peerUI,
                    "sends a request",
                    sessionRequestUI.peerContextUI
                )
                Spacer(modifier = Modifier.height(16.dp))
                Request(sessionRequestUI = sessionRequestUI)
                Spacer(modifier = Modifier.height(16.dp))
                Buttons(allowButtonColor, onDecline = {
                    composableScope.launch {
                        try {
                           // sessionRequestViewModel.reject { uri -> context.sendResponseDeepLink(uri) }
                            navController.popBackStack()
                        } catch (e: Throwable) {
                            closeAndShowError(navController, e.message)
                        }
                    }
                }, onAllow = {
                    composableScope.launch {
                        try {
                            /*
                                                        sessionRequestViewModel.approve("") { uri ->
                                                            loge("SessionRequestRoute ==>", "$uri")

                                                            context.sendResponseDeepLink(uri)
                                                        }
                            */
                            navController.popBackStack()
                        } catch (e: Throwable) {
                            closeAndShowError(navController, e.message)
                        }
                    }
                })
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        SessionRequestUI.Initial -> {
            SemiTransparentDialog {
                Spacer(modifier = Modifier.height(24.dp))
                Peer(peerUI = PeerUI.Empty, null)
                Spacer(modifier = Modifier.height(200.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        strokeWidth = 8.dp,
                        modifier = Modifier.size(100.dp),
                        color = Color(0xFFB8F53D)
                    )
                }
                Spacer(modifier = Modifier.height(200.dp))
                Buttons(
                    verified_color,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .blur(4.dp)
                        .padding(vertical = 8.dp)

                )
            }
        }
    }

}

private fun closeAndShowError(navController: NavHostController, message: String?) {
    navController.popBackStack()
    navController.showSnackbar(
        message ?: "Session request error, please check your Internet connection"
    )
}

@Composable
fun Request(sessionRequestUI: SessionRequestUI.Content) {
    Column(modifier = Modifier.height(400.dp)) {
        Content(title = "Request") {
            InnerContent {
                Text(
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 13.dp),
                    text = "Params",
                    style = TextStyle(
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = themedColor(
                            darkColor = Color(0xFF9ea9a9),
                            lightColor = Color(0xFF788686)
                        )
                    )
                )
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 5.dp, top = 0.dp, end = 5.dp, bottom = 10.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(
                            themedColor(
                                darkColor = Color(0xFFE4E4E7).copy(alpha = .12f),
                                lightColor = Color(0xFF505059).copy(.1f)
                            )
                        )
                        .padding(start = 8.dp, top = 5.dp, end = 8.dp, bottom = 5.dp),
                    text = sessionRequestUI.param,
                    style = TextStyle(
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = themedColor(
                            darkColor = Color(0xFF9ea9a9),
                            lightColor = Color(0xFF788686)
                        )
                    )
                )
            }
            Spacer(modifier = Modifier.height(5.dp))
            sessionRequestUI.chain?.let { chain ->
                InnerContent {
                    Text(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 13.dp),
                        text = "Chain",
                        style = TextStyle(
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = themedColor(
                                darkColor = Color(0xFF9ea9a9),
                                lightColor = Color(0xFF788686)
                            )
                        )
                    )
                    BlueLabelRow(listOf(sessionRequestUI.chain))
                }
            }
            Spacer(modifier = Modifier.height(5.dp))
            InnerContent {
                Text(
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 13.dp),
                    text = "Method",
                    style = TextStyle(
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = themedColor(
                            darkColor = Color(0xFF9ea9a9),
                            lightColor = Color(0xFF788686)
                        )
                    )
                )
                BlueLabelRow(listOf(sessionRequestUI.method))
            }
        }
    }
}

