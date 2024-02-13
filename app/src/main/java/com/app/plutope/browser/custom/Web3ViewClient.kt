package com.app.plutope.browser.custom

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.app.plutope.R

class Web3ViewClient(context: Context) : WebViewClient() {
    @JvmField
    val jsInjectorClient: JsInjectorClient
    private val context: Context

    init {
        jsInjectorClient = JsInjectorClient(context)
        this.context = context
    }

    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        return handleTrustedApps(url)
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        if (request == null || view == null) {
            return false
        }
        val url = request.url.toString()
        return handleTrustedApps(url)
    }

    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        return if (request == null) {
            null
        } else super.shouldInterceptRequest(view, request)
    }

    fun getInitString(view: WebView): String {
        return jsInjectorClient.initJs(view.context)
    }

    fun getProviderString(view: WebView): String {
        return jsInjectorClient.providerJs(view.context)
    }

    override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
        /* AWalletAlertDialog aDialog = new AWalletAlertDialog(context);
        aDialog.setTitle(R.string.title_dialog_error);
        aDialog.setIcon(AWalletAlertDialog.ERROR);
        aDialog.setMessage(R.string.ssl_cert_invalid);
        aDialog.setButtonText(R.string.dialog_approve);
        aDialog.setButtonListener(v -> {
            handler.proceed();
            aDialog.dismiss();
        });
        aDialog.setSecondaryButtonText(R.string.action_cancel);
        aDialog.setButtonListener(v -> {
            handler.cancel();
            aDialog.dismiss();
        });
        aDialog.show();*/
    }

    //Handling of trusted apps
    private fun handleTrustedApps(url: String): Boolean {
        //get list
        val strArray = context.resources.getStringArray(R.array.TrustedApps)
        for (item in strArray) {
            val split = item.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (url.startsWith(split[1])) {
                intentTryApp(split[0], url)
                return true
            }
        }
        return false
    }

    private fun intentTryApp(appId: String, msg: String) {
        val isAppInstalled = isAppAvailable(appId)
        if (isAppInstalled) {
            val myIntent = Intent(Intent.ACTION_VIEW)
            myIntent.setPackage(appId)
            myIntent.setData(Uri.parse(msg))
            myIntent.putExtra(Intent.EXTRA_TEXT, msg)
            myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(myIntent)
        } else {
            Toast.makeText(context, "Required App not Installed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isAppAvailable(appName: String): Boolean {
        val pm = context.packageManager
        return try {
            pm.getPackageInfo(appName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun resetInject() {}
}