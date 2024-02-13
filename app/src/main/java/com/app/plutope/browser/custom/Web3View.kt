package com.app.plutope.browser.custom

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.app.plutope.utils.loge
import org.jetbrains.annotations.Contract
import timber.log.Timber

class Web3View : WebView {
    private val webViewClient: Web3ViewClient
    private var loadInterface: URLLoadInterface? = null


    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        webViewClient = Web3ViewClient(getContext())
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        webViewClient = Web3ViewClient(getContext())
        init()
    }

    override fun setWebChromeClient(client: WebChromeClient?) {
        super.setWebChromeClient(client)
    }

    override fun setWebViewClient(client: WebViewClient) {
        loge("setWebViewClient", "here i am")
        super.setWebViewClient(WrapWebViewClient(webViewClient, client))
    }

    override fun loadUrl(url: String, additionalHttpHeaders: Map<String?, String?>) {
        super.loadUrl(url, additionalHttpHeaders)
    }

    override fun loadUrl(url: String) {
        loadUrl(url, web3Headers)
    }

    @get:Contract(" -> new")
    private val web3Headers: Map<String?, String?>
        /* Required for CORS requests */ private get() =//headers
            object : HashMap<String?, String?>() {
                init {
                    put("Connection", "close")
                    put("Content-Type", "text/plain")
                    put("Access-Control-Allow-Origin", "*")
                    put("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
                    put("Access-Control-Max-Age", "600")
                    put("Access-Control-Allow-Credentials", "true")
                    put("Access-Control-Allow-Headers", "accept, authorization, Content-Type")
                }
            }

    @SuppressLint("SetJavaScriptEnabled")
    fun init() {
        settings.javaScriptEnabled = true
        settings.cacheMode = WebSettings.LOAD_DEFAULT
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.domStorageEnabled = true
        settings.javaScriptCanOpenWindowsAutomatically = true
        settings.userAgentString =
            settings.userAgentString + "PlutoPe(Platform=Android&AppVersion=" + 1 + ")"
        setWebContentsDebuggingEnabled(true) //so devs can debug their scripts/pages
        setInitialScale(0)
        settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
        settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK

        /*
        addJavascriptInterface(new SignCallbackJSInterface(
                this,
                innerOnSignTransactionListener,
                innerOnSignMessageListener,
                innerOnSignPersonalMessageListener,
                innerOnSignTypedMessageListener,
                innerOnEthCallListener,
                innerAddChainListener,
                innerOnWalletActionListener), "alpha");
*/if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
            WebSettingsCompat.setAlgorithmicDarkeningAllowed(settings, true)
        }
    }

    private fun callbackToJS(callbackId: Long, function: String, param: String) {
        val callback = String.format(function, callbackId, param)
        post { evaluateJavascript(callback) { value: String? -> Timber.tag("WEB_VIEW").d(value) } }
    }

    fun onWalletActionSuccessful(callbackId: Long, expression: String?) {
        val callback = String.format(JS_PROTOCOL_EXPR_ON_SUCCESSFUL, callbackId, expression)
        post { evaluateJavascript(callback) { message: String? -> Timber.d(message) } }
    }

    fun setWebLoadCallback(iFace: URLLoadInterface) {
        loadInterface = iFace
    }

    fun resetView() {
        webViewClient.resetInject()
    }

    private inner class WrapWebViewClient(
        private val internalClient: Web3ViewClient,
        private val externalClient: WebViewClient?
    ) : WebViewClient() {
        private var loadingError = false
        private var redirect = false
        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)

            loge("onPageStarted", "url => $url")



            clearCache(true)
            if (!redirect) {
                view.evaluateJavascript(internalClient.getProviderString(view), null)
                view.evaluateJavascript(internalClient.getInitString(view), null)
                internalClient.resetInject()
            }
            redirect = false
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            if (!redirect && !loadingError) {
                loadInterface?.onWebpageLoaded(url, view.title)
            } else if (!loadingError && loadInterface != null) {
                loadInterface!!.onWebpageLoadComplete()
            }
            redirect = false
            loadingError = false
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            redirect = true
            return (externalClient!!.shouldOverrideUrlLoading(view, request)
                    || internalClient.shouldOverrideUrlLoading(view, request))
        }

        override fun onReceivedError(
            view: WebView,
            request: WebResourceRequest,
            error: WebResourceError
        ) {
            loadingError = true
            externalClient?.onReceivedError(view, request, error)
        }
    }

    companion object {
        private const val JS_PROTOCOL_CANCELLED = "cancelled"
        private const val JS_PROTOCOL_ON_SUCCESSFUL =
            "AlphaWallet.executeCallback(%1\$s, null, \"%2\$s\")"
        private const val JS_PROTOCOL_EXPR_ON_SUCCESSFUL =
            "AlphaWallet.executeCallback(%1\$s, null, %2\$s)"
        private const val JS_PROTOCOL_ON_FAILURE =
            "AlphaWallet.executeCallback(%1\$s, \"%2\$s\", null)"
    }
}
