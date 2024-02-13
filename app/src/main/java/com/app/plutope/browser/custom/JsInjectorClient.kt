package com.app.plutope.browser.custom

import android.content.Context
import android.text.TextUtils
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Request
import okhttp3.Response
import java.util.Locale
import java.util.regex.Pattern

class JsInjectorClient(context: Context?) {
    @JvmField
    val chainId: Long = 0
    private val rpcUrl: String? = null
    fun initJs(context: Context?): String {
        return "" /*loadInitJs(context)*/
    }

    fun providerJs(context: Context?): String {
        return "" /*loadFile(context, com.app.plutope.R.raw.alphawallet_min)*/
    }

    fun injectJSAtEnd(view: String, newCode: String): String {
        val position = getEndInjectionPosition(view)
        if (position >= 0) {
            val beforeTag = view.substring(0, position)
            val afterTab = view.substring(position)
            return beforeTag + newCode + afterTab
        }
        return view
    }

    fun injectJS(html: String, js: String): String {
        if (TextUtils.isEmpty(html)) {
            return html
        }
        val position = getInjectionPosition(html)
        if (position >= 0) {
            val beforeTag = html.substring(0, position)
            val afterTab = html.substring(position)
            return beforeTag + js + afterTab
        }
        return html
    }

    private fun getInjectionPosition(body: String): Int {
        var body = body
        body = body.lowercase(Locale.getDefault())
        val ieDetectTagIndex = body.indexOf("<!--[if")
        val scriptTagIndex = body.indexOf("<script")
        var index: Int
        index = if (ieDetectTagIndex < 0) {
            scriptTagIndex
        } else {
            Math.min(scriptTagIndex, ieDetectTagIndex)
        }
        if (index < 0) {
            index = body.indexOf("</head")
        }
        if (index < 0) {
            index = 0 //just wrap whole view
        }
        return index
    }

    private fun getEndInjectionPosition(body: String): Int {
        var body = body
        body = body.lowercase(Locale.getDefault())
        val firstIndex = body.indexOf("<script")
        val nextIndex = body.indexOf("web3", firstIndex)
        return body.indexOf("</script", nextIndex)
    }

    /*private fun buildRequest(url: String, headers: Map<String, String>): Request? {
        val httpUrl = HttpUrl.parse(url) ?: return null
        val requestBuilder: Headers.Builder = Headers.Builder()[""]
            .url(httpUrl)
        val keys = headers.keys
        for (key in keys) {
            requestBuilder.addHeader(key, headers[key])
        }
        return requestBuilder.build()
    }*/

    fun buildRequest(url: String, headers: Map<String, String>): Request? {
        val httpUrl = url.toHttpUrlOrNull() ?: return null
        val requestBuilder: Request.Builder = Request.Builder()
            .get()
            .url(httpUrl)

        headers.forEach { (key, value) ->
            requestBuilder.addHeader(key, value)
        }

        return requestBuilder.build()
    }

    /* private String loadInitJs(Context context) {
        String initSrc = loadFile(context, R.raw.init);
        String address = walletAddress == null ? Address.EMPTY.toString() : Keys.toChecksumAddress(walletAddress.toString());
        return String.format(initSrc, address, rpcUrl, chainId);
    }*/
    fun injectStyleAndWrap(view: String, style: String?): String {
        var style = style
        if (style == null) style = ""
        //String injectHeader = "<head><meta name=\"viewport\" content=\"width=device-width, user-scalable=false\" /></head>";
        val injectHeader =
            "<head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1, maximum-scale=1, shrink-to-fit=no\" />" //iOS uses these header settings
        style = """
             <style type="text/css">
             $style.token-card {
             padding: 0pt;
             margin: 0pt;
             }</style></head><body>
             
             """.trimIndent()
        // the opening of the following </div> is in injectWeb3TokenInit();
        return "$injectHeader$style$view</div></body>"
    }

    private fun getMimeType(contentType: String): String {
        val regexResult = Pattern.compile("^.*(?=;)").matcher(contentType)
        return if (regexResult.find()) {
            regexResult.group()
        } else DEFAULT_MIME_TYPE
    }

    private fun getCharset(contentType: String): String {
        val regexResult = Pattern.compile("charset=([a-zA-Z0-9-]+)").matcher(contentType)
        if (regexResult.find()) {
            if (regexResult.groupCount() >= 2) {
                return regexResult.group(1)
            }
        }
        return DEFAULT_CHARSET
    }

    private fun getContentTypeHeader(response: Response): String? {
        val headers = response.headers
        var contentType: String?
        contentType = if (TextUtils.isEmpty(headers["Content-Type"])) {
            if (TextUtils.isEmpty(headers["content-Type"])) {
                "text/data; charset=utf-8"
            } else {
                headers["content-Type"]
            }
        } else {
            headers["Content-Type"]
        }
        if (contentType != null) {
            contentType = contentType.trim { it <= ' ' }
        }
        return contentType
    }

    companion object {
        private const val DEFAULT_CHARSET = "utf-8"
        private const val DEFAULT_MIME_TYPE = "text/html"
        private const val JS_TAG_TEMPLATE = "<script type=\"text/javascript\">%1\$s%2\$s</script>"
    }
}
