package com.app.plutope.browser.utils

import android.content.Context
import android.preference.PreferenceManager
import android.text.TextUtils
import android.util.Patterns
import androidx.annotation.RawRes
import com.app.plutope.browser.C
import com.app.plutope.browser.browserModel.DApp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import timber.log.Timber
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.regex.Pattern

private const val IPFS_PREFIX = "ipfs://"
private const val IPFS_DESIGNATOR = "/ipfs/"
private const val DAPPS_HISTORY_FILE = "dappshistory"
private const val DAPPS_LIST_FILENAME = "dapps_list.json"

private const val DEFAULT_HOMEPAGE = "https://alphawallet.com/browser/"
private const val POLYGON_HOMEPAGE = "https://alphawallet.com/browser-item-category/polygon/"

private var POLYGON_ID: Long = 137

fun loadFile(context: Context, @RawRes rawRes: Int): String? {
    var buffer = ByteArray(0)
    try {
        val `in` = context.resources.openRawResource(rawRes)
        buffer = ByteArray(`in`.available())
        val len = `in`.read(buffer)
        if (len < 1) {
            throw IOException("Nothing is read.")
        }
    } catch (ex: Exception) {
        Timber.tag("READ_JS_TAG").d(ex, "Ex")
    }
    try {
        Timber.tag("READ_JS_TAG").d("HeapSize:%s", Runtime.getRuntime().freeMemory())
        return String(buffer)
    } catch (e: Exception) {
        Timber.tag("READ_JS_TAG").d(e, "Ex")
    }
    return ""
}

fun isValidUrl(url: String): Boolean {
    if (TextUtils.isEmpty(url)) return false
    val p: Pattern = Patterns.WEB_URL
    val m = p.matcher(url.lowercase(Locale.getDefault()))
    return m.matches() || isIPFS(url)
}

fun isIPFS(url: String): Boolean {
    return url.contains(IPFS_DESIGNATOR) || url.startsWith(IPFS_PREFIX) || shouldBeIPFS(
        url
    )
}

private fun shouldBeIPFS(url: String): Boolean {
    return url.startsWith("Qm") && url.length == 46 && !url.contains(".") && !url.contains("/")
}

fun removeFromHistory(context: Context, url: String) {
    val history = getBrowserHistory(context) as MutableList<DApp?> // Ensure MutableList

    val removeIndices = mutableListOf<Int>()
    for ((index, dApp) in history.withIndex()) {
        if (dApp?.url == url) {
            removeIndices.add(index)
        }
    }
    removeIndices.sortDescending()
    for (index in removeIndices) {
        history.removeAt(index)
    }
    saveHistory(context, history)
}

/*fun removeFromHistory(context: Context?, url: String?) {
    val history: List<DApp> = getBrowserHistory(context)
    val removeList: ArrayList<Int> = ArrayList()
    for (i in history.indices) {
        if (history[i]?.url.equals(url)) {
            removeList.add(i)
        }
    }
    removeList.sortWith { d1: Int?, d2: Int? ->
        d2!!.compareTo(d1!!)
    }

    //remove in reverse order
    for (i in removeList) {
        history.removeAt(i)
    }

    saveHistory(context, history)
}*/

private fun saveHistory(context: Context?, history: List<DApp?>) {
    if (context != null) {
        val dAppHistory = Gson().toJson(history)
        storeJsonData(
            DAPPS_HISTORY_FILE,
            dAppHistory,
            context
        )
    }
}

fun getBrowserHistory(context: Context?): List<DApp?> {
    if (context == null) return java.util.ArrayList()
    val historyJson: String = loadJsonData(DAPPS_HISTORY_FILE, context)!!
    if (TextUtils.isEmpty(historyJson)) blankPrefEntry(
        context,
        C.DAPP_BROWSER_HISTORY
    ) //blank legacy
    var history: List<DApp?> = if (historyJson.isEmpty()) {
        java.util.ArrayList()
    } else {
        Gson().fromJson(historyJson, object : TypeToken<java.util.ArrayList<DApp?>?>() {}.type)
    }
    val historyLen = history.size
    history = parseDappHistory(history)!!
    if (historyLen != history.size) {
        val dappHistory = Gson().toJson(history)
        storeJsonData(
            DAPPS_HISTORY_FILE,
            dappHistory,
            context
        )
    }
    return history
}

private fun storeJsonData(fName: String, json: String, context: Context) {
    val file = File(context.filesDir, fName)
    try {
        FileOutputStream(file).use { fos ->
            val writer =
                BufferedWriter(OutputStreamWriter(fos))
            writer.write(json)
            writer.flush()
            writer.close()
        }
    } catch (e: java.lang.Exception) {
        //
    }
}


private fun parseDappHistory(history: List<DApp?>): List<DApp?>? {
    var requireRefresh = false
    val recodeHistory: MutableList<DApp> = java.util.ArrayList()
    for (dapp in history) {
        if (TextUtils.isEmpty(dapp?.url)) {
            continue
        }
        if (isValidUrl(dapp?.url!!)) {
            recodeHistory.add(dapp)
        } else {
            requireRefresh = true
        }
    }
    return if (requireRefresh) {
        recodeHistory
    } else {
        history
    }
}


fun loadJsonData(fName: String?, context: Context): String? {
    val sb = StringBuilder()
    val file = File(context.filesDir, fName)
    try {
        FileInputStream(file).use { fis ->
            val reader =
                BufferedReader(InputStreamReader(fis))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                sb.append(line).append("\n")
            }
            reader.close()
        }
    } catch (e: java.lang.Exception) {
        //
    }
    return sb.toString()
}

private fun blankPrefEntry(context: Context, key: String) {
    PreferenceManager
        .getDefaultSharedPreferences(context)
        .edit()
        .putString(key, "")
        .apply()
}

fun isDefaultDapp(url: String): Boolean {
    return DEFAULT_HOMEPAGE == url || POLYGON_HOMEPAGE == url
}

fun getDomainName(url: String?): String? {
    return try {
        val uri = URI(url)
        val domain = uri.host
        if (domain.startsWith("www.")) domain.substring(4) else domain
    } catch (e: java.lang.Exception) {
        url ?: ""
    }
}

fun getIconUrl(url: String?): String {
    return "https://www.google.com/s2/favicons?sz=128&domain=$url"
}

fun getDappsList(context: Context?): MutableList<DApp>? {
    return Gson().fromJson<ArrayList<DApp>?>(
        loadJSONFromAsset(
            context!!,
            DAPPS_LIST_FILENAME
        ),
        object : TypeToken<List<DApp?>?>() {}.type
    )
}

fun loadJSONFromAsset(context: Context, fileName: String?): String? {
    var json: String? = null
    json = try {
        val `is` = context.assets.open(fileName!!)
        val size = `is`.available()
        val buffer = ByteArray(size)
        `is`.read(buffer)
        `is`.close()
        String(buffer, StandardCharsets.UTF_8)
    } catch (ex: IOException) {
        ex.printStackTrace()
        return null
    }
    return json
}

fun defaultDapp(chainId: Long): String? {
    return if (chainId == POLYGON_ID) POLYGON_HOMEPAGE else DEFAULT_HOMEPAGE
}

fun addToHistory(context: Context?, dapp: DApp?) {
    if (dapp == null || isWithinHomePage(dapp.url)) return
    val history: MutableList<DApp> = ArrayList()
    history.addAll(getBrowserHistory(context) as MutableList<DApp>)
    if (history.size > 1) {
        for (i in 1 until history.size) {
            if (history[i].url.equals(dapp.url)) {
                history.removeAt(i) //remove older item
                break
            }
        }
    }
    saveHistory(context, history)
}

fun isWithinHomePage(url: String?): Boolean {
    val homePageRoot: String =
        DEFAULT_HOMEPAGE.substring(0, DEFAULT_HOMEPAGE.length - 1) //remove final slash
    return url != null && url.startsWith(homePageRoot)
}