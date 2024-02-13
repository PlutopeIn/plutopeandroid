package com.app.plutope.utils.walletConnection

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.walletconnect.web3.wallet.client.Wallet


var sessionProposalEvent: Pair<Wallet.Model.SessionProposal, Wallet.Model.VerifyContext>? = null
var sessionRequestEvent: Pair<Wallet.Model.SessionRequest, Wallet.Model.VerifyContext>? = null

fun Context.sendResponseDeepLink(sessionRequestDeeplinkUri: Uri) {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, sessionRequestDeeplinkUri))
    } catch (exception: ActivityNotFoundException) {
        exception.printStackTrace()
    }
}