package com.app.plutope.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.text.InputFilter
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import cash.z.ecc.android.bip39.Mnemonics
import com.app.plutope.BuildConfig
import com.app.plutope.R
import com.app.plutope.dialogs.DeviceLockFullScreenDialog
import com.app.plutope.networkConfig.Chain
import com.app.plutope.ui.base.App
import com.app.plutope.ui.base.BaseActivity
import com.app.plutope.ui.fragment.wallet.backup.PhraseBackupFragment
import com.app.plutope.utils.constant.isFullScreenLockDialogOpen
import com.app.plutope.utils.dialogs.LoadingDialog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.material.snackbar.Snackbar
import com.google.api.services.drive.DriveScopes
import io.michaelrocks.libphonenumber.android.NumberParseException
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.web3j.crypto.MnemonicUtils
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.response.TransactionReceipt
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.nio.charset.StandardCharsets
import java.text.DecimalFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.Optional
import java.util.TimeZone
import java.util.regex.Pattern
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.pow


fun NavController.safeNavigate(direction: NavDirections) {
    currentDestination?.getAction(direction.actionId)?.run {
        navigate(direction)
    }
}

fun NavController.safeNavigate(direction: Int) {
    currentDestination?.getAction(direction)?.run {
        navigate(direction)
    }
}

fun NavController.safeNavigate(direction: Int, bundle: Bundle) {
    currentDestination?.getAction(direction)?.run {
        navigate(direction, bundle)
    }
}

fun isPasswordValid(password: String): Boolean {
    val pattern =
        Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&\\#])[A-Za-z\\d@$!%*?&#]{8,}$")

    return pattern.matches(password)
}

fun isCardPasswordValid(password: String): Boolean {
    val digitRegex = ".*\\d.*"
    val letterRegex = ".*[a-zA-Z].*"

    return password.length >= 8 && password.matches(digitRegex.toRegex()) && password.matches(
        letterRegex.toRegex()
    )
}

fun String?.toNonNullString(): String {
    if (this == null || this == "null" || this == "") {
        return ""
    }
    return this
}

fun String?.toNonNullInt(): String {
    if (this == null || this == "null" || this == "") {
        return "0"
    }
    return this
}

fun EditText.validateEmpty(): Boolean {
    val temp = (TextUtils.isEmpty(this.text.trim().toString()))
    if (temp) this.requestFocus()
    return temp
}

fun String.validateEmpty(): Boolean {
    return TextUtils.isEmpty(trim())
}

fun validateBothPassword(password: String, confirmPassword: String): Boolean {
    return password.trim() != confirmPassword.trim()
}


data class PasswordValidation(
    val isLengthMatched: Boolean = false,
    val isCaseMatched: Boolean = false,
    val isNumberMatched: Boolean = false,
    val isSymbolMatched: Boolean = false,

    )

fun String.passwordValidation(): PasswordValidation {

    return PasswordValidation(
        isLengthMatched = validateLength(this),
        isCaseMatched = validateCase(this),
        isNumberMatched = validateNumber(this),
        isSymbolMatched = validateSymbol(this),
    )
}


fun validateLength(password: String): Boolean {
    return password.length in 8..32
}

// Validation for uppercase and lowercase Latin letters
fun validateCase(password: String): Boolean {
    val hasLowerCase = password.any { it.isLowerCase() }
    val hasUpperCase = password.any { it.isUpperCase() }
    return hasLowerCase && hasUpperCase
}

// Validation for at least one number
fun validateNumber(password: String): Boolean {
    return password.any { it.isDigit() }
}

// Validation for at least one symbol
fun validateSymbol(password: String): Boolean {
    return password.any { !it.isLetterOrDigit() }
}


fun EditText.attachPasswordToggle(imageView: ImageView) {
    this.transformationMethod = BiggerDotPasswordTransformationMethod
    imageView.setOnClickListener {
        if (this.transformationMethod == BiggerDotPasswordTransformationMethod) {
            this.transformationMethod = HideReturnsTransformationMethod.getInstance()
            imageView.setImageResource(R.drawable.ic_show_eye)
            this.setSelection(this.length())
        } else {
            this.transformationMethod = BiggerDotPasswordTransformationMethod
            imageView.setImageResource(R.drawable.ic_eye_hide)
            this.setSelection(this.length())
        }
    }
}

object BiggerDotPasswordTransformationMethod : PasswordTransformationMethod() {
    val DOT = '\u2022'
    val BIGGER_DOT = '*'

    override fun getTransformation(source: CharSequence, view: View): CharSequence {
        return PasswordCharSequence(super.getTransformation(source, view))
    }

    class PasswordCharSequence(val transformation: CharSequence) : CharSequence by transformation {
        override fun get(index: Int): Char = if (transformation[index] == DOT) {
            BIGGER_DOT
        } else {
            transformation[index]
        }
    }

}

fun Context.showToast(message: String) {
    if (message.isNotEmpty())
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun Fragment.showToast(message: String) {
    if (message.isNotEmpty())
        Toast.makeText(this.context, message, Toast.LENGTH_LONG).show()
}


fun ByteArray.toHexString(): String {
    return joinToString("") { byte ->
        "%02x".format(byte)
    }
}

fun loadJSONFromRaw(resourceId: Int): String {
    return try {
        val inputStream = App.getContext().resources.openRawResource(resourceId)
        val buffer = ByteArray(inputStream.available())
        inputStream.read(buffer)
        inputStream.close()
        String(buffer)
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}


//generate 12 words from HDWallet
fun getMnemonics(): List<CharArray> {
    val mnemonicCode: Mnemonics.MnemonicCode =
        Mnemonics.MnemonicCode(Mnemonics.WordCount.COUNT_12)
    return mnemonicCode.words
}

//get word list from list char array
fun getWordListFromWordCharArray(mnemonicsWords: List<CharArray>): MutableList<String> {
    val wordList = mutableListOf<String>()
    for (row in mnemonicsWords) {
        val word = row.joinToString(separator = "")
        wordList.add(word)
    }
    return wordList
}

//copy clipboard
fun Context.copyToClipboard(text: String) {
    val clipboard = ContextCompat.getSystemService(this, ClipboardManager::class.java)
    clipboard?.setPrimaryClip(ClipData.newPlainText("", text))
}

fun Context.showLoader() {
    if (!isFullScreenLockDialogOpen) CoroutineScope(Dispatchers.Main).launch {
        LoadingDialog.getInstance()?.show(this@showLoader)
    }
}

fun Context.showLoaderAnyHow() {
    LoadingDialog.getInstance()?.show(this@showLoaderAnyHow)
}

fun hideLoader() {
    LoadingDialog.getInstance()?.dismiss()
}

fun Context.openCustomDeviceLock(listener: DeviceLockOpenSuccess) {
    val dialog = DeviceLockFullScreenDialog.getInstance()
    dialog.show(this, object : DeviceLockFullScreenDialog.DialogOnClickBtnListner {
        override fun onSubmitClicked(selectedList: String) {
            listener.onSuccessOpenDeviceLock()
        }
    })

    (this as? BaseActivity)?.setDeviceLockDialog(dialog)

    /*DeviceLockDialogFragment2.show(
        (this as BaseActivity).supportFragmentManager,
        object : DeviceLockDialogFragment2.DialogOnClickBtnListener {
            override fun onSubmitClicked(selectedList: String) {
                // Handle success
                listener.onSuccessOpenDeviceLock()
            }
        })*/


}

interface DeviceLockOpenSuccess {
    fun onSuccessOpenDeviceLock()
}

var decimalInputFilter2 =
    InputFilter { source, _, _, dest, _, _ ->
        val text = dest.toString()
        if (text.contains(".") && (source == "." || text.substring(text.indexOf(".") + 1).length >= 2)) {
            return@InputFilter ""
        }
        null
    }

fun findDecimalFromString(input: String): Boolean {
    val decimalRegex = "\\d*\\.\\d+".toRegex()
    val matchResult = decimalRegex.find(input)
    val decimalValue = matchResult?.value
    return decimalValue != null
}

fun convertAmountToCurrency(amount: BigDecimal, price: BigDecimal): BigDecimal {
    return price * amount
}

fun weiToEther(wei: BigInteger): Double {
    val weiPerEther = BigInteger("1000000000000000000")
    val weiBigDecimal = wei.toBigDecimal()
    val etherBigDecimal = weiBigDecimal.divide(weiPerEther.toBigDecimal())
    return etherBigDecimal.toDouble()
}

fun convertWeiToEther(wei: String, decimal: Int): String {
    loge("convertWeiToEther", "wei =>${wei} :: decimal => ${decimal}")
    val weiBigDecimal = BigDecimal(wei)
    val divisor = BigDecimal.TEN.pow(decimal)
    val etherBigDecimal = weiBigDecimal.divide(divisor, decimal, RoundingMode.DOWN)
    return etherBigDecimal.toPlainString()
}

fun convertToWei(amount: Double, decimal: Int): BigInteger {
    val weiAmount = BigDecimal(amount) * BigDecimal(10.0.pow(decimal))
    return weiAmount.toBigInteger()
}


fun getDateFromTimeStamp(timestamp: Long): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp
    val dateFormat = SimpleDateFormat("MMM dd yyyy, hh:mm aa", Locale.getDefault())
    return dateFormat.format(calendar.time)
}


fun decryptTransferInput(input: String): Pair<String, BigInteger>? {
    if (input.length < 138) return null
    val recipientAddress = "0x" + input.substring(34, 74)
    val transferValue = BigInteger(input.substring(74), 16)

    return Pair(recipientAddress, transferValue)
}

fun validateMnemonic(mnemonic: String): Pair<Boolean, String> {
    val trimmedMnemonic = mnemonic.trim()
    val words = trimmedMnemonic.split("\\s+".toRegex())
    if (words.size != 12) {
        return Pair(false, "Phrase key is invalid. Please enter valid phrase key.")
    }
    val reconstructedMnemonic = words.joinToString(" ")
    val isValid = MnemonicUtils.validateMnemonic(reconstructedMnemonic)
    val errorMessage = if (isValid) "" else "Phrase key is invalid. Please enter valid phrase key."

    return Pair(isValid, errorMessage)
}

fun getNetworkString(chain: Chain?): String {
    return when (chain) {
        Chain.Ethereum -> "eth"
        Chain.BinanceSmartChain -> "bsc"
        Chain.OKC -> "okt"
        Chain.Polygon -> "matic"
        Chain.Bitcoin -> "btc"
        Chain.Optimism -> "optimism"
        Chain.Arbitrum -> "arbitrum"
        Chain.Avalanche -> "avalanche"
        Chain.BaseMainnet -> "base"
        Chain.Tron -> "tron"
        Chain.Solana -> "solana"
        else -> chain?.chainName.toString()
    }
}

fun getNetworkForRangoExchange(chain: Chain?): String {
    return when (chain) {
        Chain.Ethereum -> "eth"
        Chain.BinanceSmartChain -> "bsc"
        Chain.OKC -> "okt"
        Chain.Polygon -> "polygon"
        Chain.Bitcoin -> "btc"
        Chain.Optimism -> "optimism"
        Chain.Arbitrum -> "arbitrum"
        Chain.Avalanche -> "avalanche"
        Chain.BaseMainnet -> "base"
        Chain.Tron -> "tron"
        Chain.Solana -> "solana"
        else -> chain?.chainName.toString()
    }
}

fun isValidTokenContractAddress(address: String): Boolean {
    val addressRegex = "^0x([A-Fa-f0-9]{40})$".toRegex()
    return addressRegex.matches(address)
}


fun View.showSnackBar(message: String) {
    Snackbar.make(this, message, Snackbar.LENGTH_LONG).show()
}

fun Button.enableDisableButton(isValid: Boolean) {
    if (isValid) {
        this.apply {
            isEnabled = true
            background = ResourcesCompat.getDrawable(resources, R.drawable.button_gradient_26, null)
            setTextColor(ResourcesCompat.getColor(resources, R.color.white_text, null))

        }
    } else {
        this.apply {
            isEnabled = false
            background =
                ResourcesCompat.getDrawable(resources, R.drawable.button_disable, null)
            setTextColor(ResourcesCompat.getColor(resources, R.color.text_gray, null))

        }

    }

}

fun Date.toISOString(): String {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    calendar.time = this
    return String.format("%tFT%tT.%tLZ", calendar, calendar, calendar)
}

fun generateSignature(
    timestamp: String,
    method: String,
    endpoint: String,
    body: String,
    secretKey: String
): String {
    val dataToSign = "$timestamp$method$endpoint$body"
    val hmacSha256 = Mac.getInstance("HmacSHA256")
    val keySpec = SecretKeySpec(secretKey.toByteArray(StandardCharsets.UTF_8), "HmacSHA256")
    hmacSha256.init(keySpec)
    val signatureBytes = hmacSha256.doFinal(dataToSign.toByteArray(StandardCharsets.UTF_8))
    return android.util.Base64.encodeToString(signatureBytes, android.util.Base64.NO_WRAP)
}

//onRamp Type enum
enum class OnRampType(val value: Int) {
    onRamp(1),
    offRamp(2)
}


//google signIn
fun requestGoogleSignIn(context: Context, listener: GoogleSignInListner) {
    val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestScopes(Scope(DriveScopes.DRIVE_FILE))
        .build()
    val client = GoogleSignIn.getClient(context, signInOptions)
    listener.requestUserGoogleSingIn(client.signInIntent, PhraseBackupFragment.REQUEST_CODE_SIGN_IN)
}

interface GoogleSignInListner {
    fun requestUserGoogleSingIn(recoveryIntent: Intent?, requestCode: Int)
}

fun convertDateTimeToDDMMMYYYY(createdTime: String): String {
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm a", Locale.ENGLISH)
    val zonedDateTime = ZonedDateTime.parse(createdTime)
    return zonedDateTime.withZoneSameInstant(ZoneId.systemDefault()).format(formatter)
}


fun getReceipt(
    web3: Web3j,
    transactionHash: String,
    count: Int,
    completion: (Boolean, String?, Optional<TransactionReceipt>?) -> Unit,

    ) {

    web3.ethGetTransactionReceipt(transactionHash).send().apply {
        when {
            this.result != null -> {
                completion(true, this.transactionReceipt.toString(), this.transactionReceipt)
            }

            else -> {
                Thread.sleep(2000)
                getReceipt(
                    web3,
                    transactionHash,
                    count,
                    completion = { success, errorMessage, transaction ->
                        if (success) {
                            completion(true, errorMessage, transaction)
                        } else {
                            completion(false, errorMessage, transaction)
                        }
                    })
            }
        }
    }
}


fun shareUrl(context: Context, urlToShare: String) {
    val shareIntent = Intent(Intent.ACTION_SEND)
    shareIntent.type = "text/plain"
    shareIntent.putExtra(Intent.EXTRA_TEXT, urlToShare)
    startActivity(context, Intent.createChooser(shareIntent, "Share URL"), Bundle())
}

//get Resource id
fun getImageResource(type: String?): Int {
    return when (type?.lowercase()) {
        "erc20" -> R.drawable.ic_erc
        "bep20" -> R.drawable.ic_bep
        "polygon" -> R.drawable.ic_polygon
        "okschain" -> R.drawable.ic_kip
        else -> R.drawable.ic_erc
    }
}

fun setBalanceText(balance: Double, symbol: String, decimalPoints: Int = 6): String {
    val formattedBalance = if (balance.toBigDecimal() <= BigDecimal.ZERO) "0" else
        balance.toBigDecimal().setScale(decimalPoints, RoundingMode.DOWN).stripTrailingZeros()
            .toPlainString()
    return "$formattedBalance $symbol"
}

fun setBalanceText(balance: BigDecimal, symbol: String, decimal: Int = 6): String {
    val formattedBalance = if (balance <= BigDecimal.ZERO) "0" else
        balance.setScale(decimal, RoundingMode.DOWN).stripTrailingZeros().toPlainString()
    return if (symbol != "") "$formattedBalance $symbol" else formattedBalance

}

fun setBalanceDoubleText(balance: Double, symbol: String, decimal: Int = 6): String {
    val formattedBalance = if (balance <= 0.0) "0" else {
        BigDecimal(balance).setScale(decimal, RoundingMode.DOWN).stripTrailingZeros()
            .toPlainString()
    }
    return if (symbol.isNotEmpty()) {
        "$formattedBalance $symbol"
    } else {
        formattedBalance
    }
}


fun extractQRCodeScannerInfo(input: String): Triple<String, String, String>? {
    val regex1 = Regex("^(.+):(?!@1)(.+?)/transfer\\?address=([^&]+)&([^&]+)=([^&]+)$")
    val regex2 = Regex("^(.+):(?!@1)(.+?)(?:@1)?(?:\\?(.+))?$")
    val regex3 =
        Regex("(0x.+?)\\?(amount|@1?value)=(.+)|(0x.+?)\\?(amount|@137?value)=(.+)|(0x.+?)\\?(amount|@56?value)=(.+)|(0x.+?)\\?(amount|@66?value)=(.+)")
    val regexBitcoin = Regex("(bitcoin:(.+?)(?:\\?(.+))?)|(ethereum:(.+?)(?:\\?(.+))?)")
    val matchResult1 = regex1.matchEntire(input)
    val matchResult2 = regex2.matchEntire(input)
    val matchResult3 = regex3.find(input)
    val matchResultBitcoin = regexBitcoin.matchEntire(input)
    var valueAmount = ""
    loge("matchResult1", "${matchResult1?.groupValues}")
    loge("matchResult2", "${matchResult2?.groupValues}")
    loge("matchResult3", "${matchResult3?.groupValues}")
    loge("matchResultBitcoin", "${matchResultBitcoin?.groupValues}")

    if (matchResult1 != null) {
        val prefix = matchResult1.groups[2]?.value?.replace("@1", "") ?: ""
        val address = matchResult1.groups[3]?.value?.replace("@1", "") ?: ""
        val dynamicValues = matchResult1.groupValues.drop(4).chunked(2).associate { it[0] to it[1] }
        if (dynamicValues.isNotEmpty()) {
            dynamicValues.forEach { (name, value) ->
                valueAmount = value
                return@forEach
            }
        }
        val parts = address.substringBefore('@')
        return Triple(parts, valueAmount, prefix)
    } else if (matchResult2 != null) {
        val prefix = matchResult2.groups[1]?.value ?: ""
        val address = matchResult2.groups[2]?.value
        val dynamicValues = parseParameters(matchResult2.groups[3]?.value ?: "")

        if (dynamicValues.isNotEmpty()) {
            dynamicValues.forEach { (name, value) ->
                val amount = value.split("=")
                valueAmount = amount[amount.lastIndex]
                return@forEach
            }
        }
        val parts = address?.substringBefore('@') ?: ""
        return Triple(parts, valueAmount, prefix)
    } else if (matchResult3 != null) {
        val amount = matchResult3.groupValues[3]
        val parts = matchResult3.groupValues[1].substringBefore('@')
        return Triple(parts, amount, "")
    } else if (matchResultBitcoin != null) {
        val address = matchResultBitcoin.groups[1]?.value ?: ""
        val dynamicValues = parseParameters(matchResultBitcoin.groups[2]?.value ?: "")
        if (dynamicValues.isNotEmpty()) {
            dynamicValues.forEach { (name, value) ->
                valueAmount = value
                return@forEach
            }
        }
        val parts = address.substringBefore('@')
        return Triple(parts, valueAmount, "")
    }

    return null
}

fun parseParameters(parameters: String): Map<String, String> {
    return parameters.split("&")
        .map { it.split("=") }
        .filter { it.size == 2 }
        .associate { it[0] to it[1] }
}

fun hexToDecimalChainId(hex: String?): Long {
    return hex?.removePrefix("0x")!!.toLong(16)
}


/**
 * format decimal values remove trailing zero
 * **/
fun formatDecimal(input: String): String {
    val decimalValue = try {
        BigDecimal(input).setScale(18, RoundingMode.DOWN).stripTrailingZeros()
    } catch (e: Exception) {
        BigDecimal.ZERO
    }
    val formattedString = decimalValue.toPlainString()
    val result =
        if (formattedString.contains(".") && formattedString.indexOf('.') < formattedString.length - 1) {
            formattedString.replace("0*$".toRegex(), "")
        } else {
            formattedString
        }
    return result
}

//isScientificNotation
fun isScientificNotation(input: String): Boolean {
    val scientificNotationRegex = Regex("""[-+]?[0-9]*\.?[0-9]+([eE][-+]?[0-9]+)""")
    return scientificNotationRegex.matches(input)
}


//open chrome from url
fun redirectToChrome(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = Uri.parse(url)
    intent.`package` = "com.android.chrome"
    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
        intent.`package` = null
        context.startActivity(intent)
    }
}

fun bigIntegerToString(bigInteger: BigInteger): String {
    return bigInteger.toString()
}

fun stringToBigInteger(str: String): BigInteger {
    return BigInteger(str)
}

fun stringToBigDecimal(str: String): BigDecimal {
    return BigDecimal(str)
}

/*fun weiToGwei(wei: BigInteger): BigInteger {
    val gweiFactor = BigInteger.TEN.pow(9)
    return wei.divide(gweiFactor)
}*/

fun weiToGwei(wei: BigInteger): BigDecimal {
    val weiInOneGwei = BigDecimal("1000000000") // 1 Gwei = 10^9 Wei
    return BigDecimal(wei).divide(weiInOneGwei)
}

fun weiToGwei(wei: BigDecimal): BigDecimal {
    val weiInOneGwei = BigDecimal("1000000000") // 1 Gwei = 10^9 Wei
    return wei.divide(weiInOneGwei)
}

fun gweiToWei(gwei: BigInteger): BigInteger {
    val gweiFactor = BigInteger.TEN.pow(9)
    return gwei.multiply(gweiFactor)
}

fun gweiToWei(gwei: BigDecimal): BigDecimal {
    val gweiFactor = BigDecimal("1000000000") // 1 Gwei = 10^9 Wei
    return gwei.multiply(gweiFactor)
}

fun hexStringToBigInteger(hexString: String): BigInteger {
    val cleanHexString = if (hexString.startsWith("0x", ignoreCase = true)) {
        hexString.substring(2)
    } else {
        hexString
    }
    return BigInteger(cleanHexString, 16)
}

fun hexToMatic(hexValue: String): Double {
    val decimalValue = BigInteger(hexValue.substring(2), 16)
    return decimalValue.toDouble() / 1e18
}

fun hexToMaticWithDecimal(hexValue: String): BigDecimal {
    val decimalValue = BigInteger(hexValue.substring(2), 16)
    return BigDecimal(decimalValue).divide(BigDecimal.TEN.pow(18))
}

fun Context.hideKeyboard(view: View, editText: EditText) {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
    editText.clearFocus()
}

fun Context.hideKeyboard(windowToken: IBinder) {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}


fun getActualDigits(value: Any): String {
    return if (value is String && "e" in value.lowercase()) {
        try {
            BigDecimal(value).toPlainString()
        } catch (e: Exception) {
            value.toString()
        }
    } else {
        value.toString()
    }
}

fun convertScientificToBigDecimal(scientificValue: String): BigDecimal {
    return BigDecimal(scientificValue)
}

fun TextView.underlineText(text: String) {
    val spannableString = SpannableString(text)
    spannableString.setSpan(UnderlineSpan(), 0, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    this.text = spannableString
}

fun ImageView.defaultCountryCodeSelected() {
    val defaultFlag = "https://flagcdn.com/w320/in.png"
    loadBannerImage(this, defaultFlag)
}


fun loge(tag: String = "TAG", message: String = "") {
    when {
        BuildConfig.DEBUG -> {
            Log.e(tag, message)
        }
    }
}

fun logd(tag: String = "TAG", message: String = "") {
    when {
        BuildConfig.DEBUG -> {
            Log.d(tag, message)
        }
    }
}


fun Fragment.showSuccessToast(message: String, showTime: Int = Toast.LENGTH_SHORT) {
    val inflater = LayoutInflater.from(context)
    val layout = inflater.inflate(R.layout.custom_success_toast, null)
    val textView = layout.findViewById<TextView>(R.id.txt_toast_message)
    textView.text = message
    val toast = Toast(context)
    toast.duration = showTime
    toast.view = layout
    toast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 100)
    toast.show()
}

fun generateRequestBody(params: Map<String, Any>): RequestBody {
    val mediaType = "application/json; charset=utf-8".toMediaType()
    val jsonObject = JSONObject()
    for ((key, value) in params) {
        when (value) {
            is String -> jsonObject.put(key, value)
            is Boolean -> jsonObject.put(key, value)
            is Int -> jsonObject.put(key, value)
            // Add more cases for other data types as needed
            else -> throw IllegalArgumentException("Unsupported data type for parameter $key: ${value::class.simpleName}")
        }
    }
    val jsonString = jsonObject.toString()
    return jsonString.toRequestBody(mediaType)
}

fun String.checkPatternMatch(patternRegex: String?): Boolean {
    val pattern = patternRegex?.let { Pattern.compile(it) }
    return pattern?.matcher(this)!!.matches()
}


data class CountryInfo(
    val isoCode: String,
    val flagEmoji: String,
    val countryCode: Int,
    val fullNumber: String,
    val isValidNumber: Boolean = false
)

fun getCountryInfo(number: String, phoneNumberUtil: PhoneNumberUtil): CountryInfo? {
    val validatedNumber = if (number.startsWith("+")) number else "+$number"

    return try {
        val phone = if (validatedNumber.length < 5) {
            validatedNumber + "00"
        } else {
            validatedNumber
        }
        val phoneNumber = phoneNumberUtil.parse(phone, "IN")
        val countryCode = phoneNumber.countryCode
        val isoCode = phoneNumberUtil.getRegionCodeForCountryCode(countryCode)
        val flagEmoji = getFlagEmoji(isoCode)
        val isValidNumber = phoneNumberUtil.isValidNumber(phoneNumberUtil.parse(phone, isoCode))

        CountryInfo(isoCode, flagEmoji, countryCode, validatedNumber, isValidNumber)
    } catch (e: NumberParseException) {
        CountryInfo("", "", 0, validatedNumber, false)
    }
}

fun getFlagEmoji(isoCode: String?): String {
    if (isoCode == null || isoCode.length != 2) {
        return ""
    }
    val countryCode = isoCode.uppercase(Locale.US)
    val firstLetter = Character.codePointAt(countryCode, 0) - 0x41 + 0x1F1E6
    val secondLetter = Character.codePointAt(countryCode, 1) - 0x41 + 0x1F1E6

    return String(Character.toChars(firstLetter)) + String(Character.toChars(secondLetter))
}

fun isEmailValid(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

fun Context.supportNavigationRedirection() {
    val url = "https://cards.plutope.io/faq"
    val intent = CustomTabsIntent.Builder().build()
    intent.launchUrl(this, Uri.parse(url))
}

fun ScrollView?.adjustResizeScroll() {
    this?.viewTreeObserver?.addOnGlobalLayoutListener {
        val screenHeight = this.rootView?.height
        val rect = android.graphics.Rect()
        this.getWindowVisibleDisplayFrame(rect)
        val keypadHeight = screenHeight?.minus(rect.bottom)
        if (keypadHeight!! > screenHeight * 0.15) {
            this.setPadding(0, 0, 0, keypadHeight)
        } else {
            this.setPadding(0, 0, 0, 0)
        }
    }
}

fun convertToMillions(amount: Double): String {
    val million = 1_000_000
    return if (amount >= million) {
        val formattedAmount = amount / million
        val decimalFormat = DecimalFormat("#,##,##0.00")
        "${decimalFormat.format(formattedAmount)}M"
    } else {
        amount.toString()
    }
}

fun convertToBillions(amount: Double): String {
    val divisor = BigDecimal("1000000000") // No underscores
    val billions = BigDecimal(amount).divide(divisor, 10, RoundingMode.HALF_UP)
    return "${billions.setScale(2, RoundingMode.HALF_UP)}B"
}

fun imageViewToBitmap(imageView: ImageView): Bitmap {
    // Create a Bitmap with the same dimensions as the ImageView
    val bitmap = Bitmap.createBitmap(
        imageView.width,
        imageView.height,
        Bitmap.Config.ARGB_8888
    )

    // Draw the ImageView's content onto the Bitmap using a Canvas
    val canvas = Canvas(bitmap)
    imageView.draw(canvas)

    return bitmap
}

@Composable
fun ShowToastExample(message: String) {
    val context = LocalContext.current

    // This will show the toast as soon as the composable is composed
    LaunchedEffect(message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}

/*fun main() {


}*/










