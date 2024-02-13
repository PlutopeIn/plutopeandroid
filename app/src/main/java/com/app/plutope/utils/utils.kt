package com.app.plutope.utils

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.PictureDrawable
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.text.InputFilter
import android.text.Spannable
import android.text.SpannableString
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.text.style.UnderlineSpan
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.BindingAdapter
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
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.api.services.drive.DriveScopes
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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


fun setProgress(view: View, progress: Int, context: BaseActivity) {


    val imgCircle1: CircleImageView = view.findViewById(R.id.img_circle_1)
    val imgCircle2: CircleImageView = view.findViewById(R.id.img_circle_2)
    val imgCircle3: CircleImageView = view.findViewById(R.id.img_circle_3)

    val view1: View = view.findViewById(R.id.view_1)
    val view2: View = view.findViewById(R.id.view_2)
    val view3: View = view.findViewById(R.id.view_3)
    val view4: View = view.findViewById(R.id.view_4)

    val initiated: TextView = view.findViewById(R.id.txt_initiated)
    val swapping: TextView = view.findViewById(R.id.txt_swapping)
    val success: TextView = view.findViewById(R.id.txt_success)

    when (progress) {

        1 -> {
            imgCircle1.background = ResourcesCompat.getDrawable(
                context.resources,
                R.drawable.background_circle_progress,
                null
            )

            view1.setBackgroundColor(
                ResourcesCompat.getColor(
                    context.resources,
                    R.color.purple_25264D,
                    null
                )
            )
            initiated.setTextColor(ResourcesCompat.getColor(context.resources, R.color.white, null))


            imgCircle2.background = ResourcesCompat.getDrawable(
                context.resources,
                R.drawable.background_circle_non_progress,
                null
            )
            view2.setBackgroundColor(
                ResourcesCompat.getColor(
                    context.resources,
                    R.color.purple_25264D,
                    null
                )
            )
            view4.setBackgroundColor(
                ResourcesCompat.getColor(
                    context.resources,
                    R.color.purple_25264D,
                    null
                )
            )
            swapping.setTextColor(
                ResourcesCompat.getColor(
                    context.resources,
                    R.color.purple_7576D,
                    null
                )
            )



            imgCircle3.background = ResourcesCompat.getDrawable(
                context.resources,
                R.drawable.background_circle_non_progress,
                null
            )
            view3.setBackgroundColor(
                ResourcesCompat.getColor(
                    context.resources,
                    R.color.purple_25264D,
                    null
                )
            )
            success.setTextColor(
                ResourcesCompat.getColor(
                    context.resources,
                    R.color.purple_7576D,
                    null
                )
            )


        }

        2 -> {

            imgCircle1.background =
                ResourcesCompat.getDrawable(
                    context.resources,
                    R.drawable.background_circle_progress,
                    null
                )

            view1.setBackgroundColor(
                ResourcesCompat.getColor(
                    context.resources,
                    R.color.blue_00C6FB,
                    null
                )
            )
            initiated.setTextColor(ResourcesCompat.getColor(context.resources, R.color.white, null))

            imgCircle2.background =
                ResourcesCompat.getDrawable(
                    context.resources,
                    R.drawable.background_circle_progress,
                    null
                )
            view2.setBackgroundColor(
                ResourcesCompat.getColor(
                    context.resources,
                    R.color.blue_00C6FB,
                    null
                )
            )
            view4.setBackgroundColor(
                ResourcesCompat.getColor(
                    context.resources,
                    R.color.purple_25264D,
                    null
                )
            )
            swapping.setTextColor(ResourcesCompat.getColor(context.resources, R.color.white, null))

            imgCircle3.background =
                ResourcesCompat.getDrawable(
                    context.resources,
                    R.drawable.background_circle_non_progress,
                    null
                )

            view3.setBackgroundColor(
                ResourcesCompat.getColor(
                    context.resources,
                    R.color.purple_25264D,
                    null
                )
            )
            initiated.setTextColor(
                ResourcesCompat.getColor(
                    context.resources,
                    R.color.purple_7576D,
                    null
                )
            )

        }

        3 -> {
            imgCircle1.background =
                ResourcesCompat.getDrawable(
                    context.resources,
                    R.drawable.background_circle_progress,
                    null
                )

            view1.setBackgroundColor(
                ResourcesCompat.getColor(
                    context.resources,
                    R.color.blue_00C6FB,
                    null
                )
            )
            initiated.setTextColor(ResourcesCompat.getColor(context.resources, R.color.white, null))


            imgCircle2.background =
                ResourcesCompat.getDrawable(
                    context.resources,
                    R.drawable.background_circle_progress,
                    null
                )

            view2.setBackgroundColor(
                ResourcesCompat.getColor(
                    context.resources,
                    R.color.blue_00C6FB,
                    null
                )
            )
            view4.setBackgroundColor(
                ResourcesCompat.getColor(
                    context.resources,
                    R.color.blue_00C6FB,
                    null
                )
            )
            swapping.setTextColor(ResourcesCompat.getColor(context.resources, R.color.white, null))


            imgCircle3.background =
                ResourcesCompat.getDrawable(
                    context.resources,
                    R.drawable.background_circle_progress,
                    null
                )

            view3.setBackgroundColor(
                ResourcesCompat.getColor(
                    context.resources,
                    R.color.blue_00C6FB,
                    null
                )
            )
            success.setTextColor(ResourcesCompat.getColor(context.resources, R.color.white, null))


        }
    }

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

//haxadecimal string to long
fun String?.getHaxaDecimalToLong(): Long {
    return (this?.substring(2)?.toLong(16)) ?: 0L
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


fun Context.openCustomDeviceLock() {
    val dialog = DeviceLockFullScreenDialog.getInstance()

    dialog.show(this, object : DeviceLockFullScreenDialog.DialogOnClickBtnListner {
        override fun onSubmitClicked(selectedList: String) {
        }
    })

    // Store the reference to the dialog in your BaseActivity
    (this as? BaseActivity)?.setDeviceLockDialog(dialog)
}


val decimalInputFilter = InputFilter { source, _, _, dest, dstart, dend ->
    val text = dest.toString()
    val input = source.toString()
    val result = text.substring(0, dstart) + input + text.substring(dend)
    if (result.matches("^\\d*\\.\\d{0,2}$".toRegex())) {
        null // Accept the input as it is
    } else if (result.matches("^\\d*\\.$".toRegex())) {
        // Allow input ending with a dot
        input
    } else {
        "" // Remove the invalid input
    }
}

var decimalInputFilter2 =
    InputFilter { source, _, _, dest, _, _ ->
        val text = dest.toString()

            // If a dot already exists, prevent adding another dot
        if (text.contains(".") && (source == "." || text.substring(text.indexOf(".") + 1).length >= 2)) {
                return@InputFilter ""
        }

        null // Accept the input as it is
    }

fun findDecimalFromString(input: String): Boolean {
    val decimalRegex = "\\d*\\.\\d+".toRegex()
    val matchResult = decimalRegex.find(input)
    val decimalValue = matchResult?.value
    return decimalValue != null
}

fun formateLimitedDecimal(value: String): String {
    val decimalFormat = DecimalFormat("#.#####")
    return decimalFormat.format(value.toDouble())
}


fun convertAmountToCurrency(amount: BigDecimal, price: BigDecimal): BigDecimal {
    return price * amount
}

fun convertAmountToACurrency(amount: BigDecimal, price: BigDecimal): BigDecimal {
    return price / amount
}

fun weiToEther(wei: BigInteger): Double {
    val weiPerEther = BigInteger("1000000000000000000")
    val weiBigDecimal = wei.toBigDecimal()
    val etherBigDecimal = weiBigDecimal.divide(weiPerEther.toBigDecimal())
    return etherBigDecimal.toDouble()


}

fun convertWeiToEther(wei: String, decimal: Int): String {
    val weiBigDecimal = BigDecimal(wei)
    val divisor = BigDecimal.TEN.pow(decimal)
    val etherBigDecimal = weiBigDecimal.divide(divisor, decimal, RoundingMode.DOWN)
    return etherBigDecimal.toString()
}

fun convertToWei(amount: Double, decimal: Int): BigInteger {
    val weiAmount = BigDecimal(amount) * BigDecimal(10.0.pow(decimal))
    return weiAmount.toBigInteger()
}


fun getDateFromTimeStamp(timestamp: Long): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp

    /* val currentTime = System.currentTimeMillis()
     *//*val diff = currentTime - calendar.timeInMillis
    val hours = (diff / (1000 * 60 * 60)).toInt()*/
    val dateFormat = SimpleDateFormat("MMM dd yyyy, hh:mm aa", Locale.getDefault())
    return dateFormat.format(calendar.time)
}

fun getDateFromTimeStampShow(timestamp: Long): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp

    val currentTime = System.currentTimeMillis()
    val diff = currentTime - calendar.timeInMillis
    val hours = (diff / (1000 * 60 * 60)).toInt()

    return when {

        hours < 24 -> {
            "Today"
        }

        hours in 25..48->{
            "Yesterday"
        }

        else -> {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            dateFormat.format(calendar.time)
        }
    }
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
        else -> ""
    }
}

fun getNetworkForRangoExchange(chain: Chain?): String {
    return when (chain) {
        Chain.Ethereum -> "eth"
        Chain.BinanceSmartChain -> "bsc"
        Chain.OKC -> "okt"
        Chain.Polygon -> "polygon"
        Chain.Bitcoin -> "btc"
        else -> ""
    }
}


fun getNetworkExceptPass(chain: Chain?): Pair<String, String> {
    return when (chain) {
        Chain.Ethereum -> {
            Pair("bsc", "matic")
        }

        Chain.BinanceSmartChain -> {
            Pair("eth", "matic")
        }

        Chain.Polygon -> {
            Pair("eth", "bsc")
        }

        else -> Pair("", "")
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
            background =
                ResourcesCompat.getDrawable(resources, R.drawable.button_gradient_26, null)
            setTextColor(ResourcesCompat.getColor(resources, R.color.white, null))
        }
    } else {
        this.apply {
            isEnabled = false
            background =
                ResourcesCompat.getDrawable(resources, R.drawable.button_disable, null)
            setTextColor(ResourcesCompat.getColor(resources, R.color.green_02303B, null))

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

// Extension function to round a Double to a specific number of decimal places
fun Double.roundTo(places: Int): Double {
    require(places >= 0)
    val factor = 10.0.pow(places)
    return (this * factor).toLong() / factor
}

//google signin

fun requestGoogleSignIn(context: Context, listner: GoogleSignInListner) {
    val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestScopes(Scope(DriveScopes.DRIVE_FILE))
        .build()
    val client = GoogleSignIn.getClient(context, signInOptions)

    // The result of the sign-in Intent is handled in onActivityResult.
    listner.requestUserGoogleSingIn(client.signInIntent, PhraseBackupFragment.REQUEST_CODE_SIGN_IN)
}
interface GoogleSignInListner {
    fun requestUserGoogleSingIn(recoveryIntent: Intent?, requestCode: Int)
}

fun convertDateTimeToDDMMMYYYY(createdTime: String): String {
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm a", Locale.ENGLISH)
    val zonedDateTime = ZonedDateTime.parse(createdTime)
    return zonedDateTime.withZoneSameInstant(ZoneId.systemDefault()).format(formatter)
}

@BindingAdapter("svgImageUrl")
fun loadSvgImage(view: ImageView, imageUrl: String?) {
    imageUrl?.let {
        val requestBuilder: RequestBuilder<PictureDrawable> = Glide.with(view)
            .`as`(PictureDrawable::class.java)
            .listener(SvgSoftwareLayerSetter()) // For SVG support
            .load(it)

        requestBuilder.into(view)
    }
}

fun Context.showAlertDialog(
    title: String,
    message: String,
    positiveButtonText: String = "OK",
    onPositiveClick: () -> Unit = {}
) {
    val alertDialogBuilder = AlertDialog.Builder(this)

    alertDialogBuilder.setTitle(title)
    alertDialogBuilder.setMessage(message)
    alertDialogBuilder.setCancelable(false)

    alertDialogBuilder.setPositiveButton(positiveButtonText) { _, _ ->
        onPositiveClick()
    }

    val alertDialog: AlertDialog = alertDialogBuilder.create()
    alertDialog.show()
}

fun getReceipt(
    web3: Web3j,
    transactionHash: String,
    count: Int,
    completion: (Boolean, String?, Optional<TransactionReceipt>?) -> Unit,

    ) {
    web3.ethGetTransactionReceipt(transactionHash).send().apply {
        if (this.result != null /*&& this.transactionReceipt.get().status != "0x0"*/) {
            completion(true, this.transactionReceipt.toString(), this.transactionReceipt)
        } else {
            Thread.sleep(2000)
            getReceipt(
                web3,
                transactionHash,
                count,
                completion = { success, errorMessage, transaction ->
                    if (success) {
                        completion(true, null, transaction)
                    } else {
                        completion(false, errorMessage, transaction)
                    }
                })
        }
    }
}


fun shareUrl(context:Context,urlToShare:String) {

    val shareText = "$urlToShare"

    val shareIntent = Intent(Intent.ACTION_SEND)
    shareIntent.type = "text/plain"
    shareIntent.putExtra(Intent.EXTRA_TEXT, shareText)

    // Show the share chooser dialog
    startActivity(context,Intent.createChooser(shareIntent, "Share URL"),Bundle())
}

//get Resource id
fun getImageResource(type:String?): Int {
    return when (type?.lowercase()) {
        "erc20" -> R.drawable.ic_erc
        "bep20" -> R.drawable.ic_bep
        "polygon" -> R.drawable.ic_polygon
        "okschain" -> R.drawable.ic_kip
        else -> R.drawable.ic_erc
    }
}

//balance Text set
fun setBalanceText(balance: Double, symbol: String, decimalPoints: Int = 6): String {
    /* val formattedBalance = if (balance <= 0.0) "0" else {
         val formatted = String.format(decimalPoints, balance.toBigDecimal().stripTrailingZeros())
         formatted.trimEnd('0', '.') // Trim trailing zeros and the decimal point
     }*/

    val formattedBalance = if (balance.toBigDecimal() <= BigDecimal.ZERO) "0" else {
        balance.toBigDecimal().setScale(decimalPoints, RoundingMode.DOWN).stripTrailingZeros()
            .toPlainString()
    }

    return "$formattedBalance $symbol"
}

fun setBalanceText(balance: BigDecimal, symbol: String, decimal: Int = 6): String {
    val formattedBalance = if (balance.toDouble() <= 0.0) "0" else {
        balance.setScale(decimal, RoundingMode.DOWN).stripTrailingZeros().toPlainString()
    }


    println("setBalanceText BigDecimal: $balance  :: formattedBalance = $formattedBalance")

    return if (symbol != "") {
        "$formattedBalance $symbol"
    } else {
        formattedBalance
    }

}

fun setBalanceDoubleText(balance: Double, symbol: String, decimal: Int = 6): String {
    val formattedBalance = if (balance <= 0.0) "0" else {
        BigDecimal(balance).setScale(decimal, RoundingMode.DOWN).stripTrailingZeros()
            .toPlainString()
    }

    println("setBalanceText Double: $balance  :: formattedBalance = $formattedBalance")

    return if (symbol.isNotEmpty()) {
        "$formattedBalance $symbol"
    } else {
        formattedBalance
    }
}


//wallet address validator
object EthereumAddressValidator {

    // Ethereum address regular expression
    private val ETHEREUM_ADDRESS_REGEX =
        "^0x[0-9a-fA-F]{40}$"

    private val ETHEREUM_ADDRESS_PATTERN = Pattern.compile(ETHEREUM_ADDRESS_REGEX)

    fun isValidEthereumAddress(address: String): Boolean {
        return ETHEREUM_ADDRESS_PATTERN.matcher(address).matches()
    }
}

//bottom sheet full height
fun setupFullHeight(bottomSheetDialog: BottomSheetDialog,context:Context) {
    val bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?
    val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(bottomSheet!!)
    BottomSheetBehavior.from(bottomSheet).peekHeight = 250
    val layoutParams = bottomSheet.layoutParams

    val windowHeight: Int = getWindowHeight(context)
    if (layoutParams != null) {
        layoutParams.height = windowHeight
    }
    bottomSheet.layoutParams = layoutParams
    behavior.state = BottomSheetBehavior.STATE_EXPANDED
}

//Calculate window height
fun getWindowHeight(context:Context): Int {
    // Calculate window height for fullscreen use
    val displayMetrics = DisplayMetrics()
    (context as Activity?)!!.windowManager.defaultDisplay.getMetrics(displayMetrics)
    return displayMetrics.heightPixels
}

//
/*
fun extractQRCodeScannerInfo(input: String): Triple<String, String, String>? {
    val regex1 = Regex("^(.+):(?!@1)(.+?)/transfer\\?address=([^&]+)&([^&]+)=([^&]+)$")
    val regex2 = Regex("^(.+):(?!@1)(.+?)(?:@1)?(?:\\?(.+))?$")


    // val regex3 = Regex("(0x.+?)\\?amount=(.+)")
    val regex4 = Regex("(0x.+?)\\?(amount|value)=(.+)")


    val matchResult1 = regex1.matchEntire(input)
    val matchResult2 = regex2.matchEntire(input)
    val matchResult3 = regex4.find(input)

    var valueAmount = ""

    if (matchResult1 != null) {
        val prefix = matchResult1.groups[2]?.value?.replace("@1", "") ?: ""
        val address = matchResult1.groups[3]?.value?.replace("@1", "") ?: ""
        val dynamicValues = matchResult1.groupValues.drop(4).chunked(2).associate { it[0] to it[1] }
            ?: emptyMap()//parseParameters(matchResult1.groups[4]?.value ?: "")

        if (dynamicValues.isNotEmpty()) {
            dynamicValues.forEach { (name, value) ->
                valueAmount = value
                return@forEach
            }
        }

        val parts = address.substringBefore('@')

        Log.e("TAG", "extractQRCodeScannerInfo 1 : $parts")

        return Triple(parts, valueAmount, prefix)
    } else if (matchResult2 != null) {
        val prefix = matchResult2.groups[1]?.value ?: ""
        val address = matchResult2.groups[2]?.value*/
/*?.replace("@1", "") ?: ""*//*





        // If "@" symbol is found, keep only the characters before it, otherwise, use the entire string


        val dynamicValues = parseParameters(matchResult2.groups[3]?.value ?: "")

        if (dynamicValues.isNotEmpty()) {
            dynamicValues.forEach { (name, value) ->
                valueAmount = value
                return@forEach
            }
        }

        val parts = address!!.substringBefore('@')



        return Triple(parts, valueAmount, prefix)
    }else if(matchResult3 != null) {
        val amount = matchResult3.groupValues[2] ?: ""

        //val parts = address.split('@')
        val parts = matchResult3.groupValues[1].substringBefore('@')
        return Triple(parts, amount, "")
    }

    return null
}

fun parseParameters(parametersString: String): Map<String, String> {
    val dynamicValues = mutableMapOf<String, String>()

    if (parametersString.isNotEmpty()) {
        val parameterPairs = parametersString.split('&')
        for (pair in parameterPairs) {
            val keyValue = pair.split('=')
            if (keyValue.size == 2) {
                dynamicValues[keyValue[0]] = keyValue[1]
            }
        }
    }

    return dynamicValues
}
*/




fun extractQRCodeScannerInfo(input: String): Triple<String, String, String>? {
    val regex1 = Regex("^(.+):(?!@1)(.+?)/transfer\\?address=([^&]+)&([^&]+)=([^&]+)$")
    val regex2 = Regex("^(.+):(?!@1)(.+?)(?:@1)?(?:\\?(.+))?$")
    val regex3 =
        Regex("(0x.+?)\\?(amount|@1?value)=(.+)|(0x.+?)\\?(amount|@137?value)=(.+)|(0x.+?)\\?(amount|@56?value)=(.+)|(0x.+?)\\?(amount|@66?value)=(.+)")
    // val regexBitcoin = Regex("bitcoin:(.+?)(?:\\?(.+))?")
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
        loge("matchResult1", "${matchResult1.groups}")
        val prefix = matchResult1.groups[2]?.value?.replace("@1", "") ?: ""
        val address = matchResult1.groups[3]?.value?.replace("@1", "") ?: ""
        val dynamicValues = matchResult1.groupValues.drop(4).chunked(2).associate { it[0] to it[1] }
            ?: emptyMap()

        if (dynamicValues.isNotEmpty()) {
            dynamicValues.forEach { (name, value) ->
                valueAmount = value
                return@forEach
            }
        }
        val parts = address.substringBefore('@')
        return Triple(parts, valueAmount, prefix)
    } else if (matchResult2 != null) {
        loge("matchResult2", "${matchResult2.groups}")
        val prefix = matchResult2.groups[1]?.value ?: ""
        val address = matchResult2.groups[2]?.value
        val dynamicValues = parseParameters(matchResult2.groups[3]?.value ?: "")

        if (dynamicValues.isNotEmpty()) {
            dynamicValues.forEach { (name, value) ->
                val amount = value.split("=")

                valueAmount = /*value*/amount[amount.lastIndex]
                return@forEach
            }
        }

        val parts = address?.substringBefore('@') ?: ""
        return Triple(parts, valueAmount, prefix)
    } else if (matchResult3 != null) {
        loge("matchResult3", "${matchResult3.groupValues}")
        val amount = matchResult3.groupValues[3] ?: ""
        val parts = matchResult3.groupValues[1].substringBefore('@')
        return Triple(parts, amount, "")
    } else if (matchResultBitcoin != null) {
        loge("matchResultBitcoin", "${matchResultBitcoin.groups}")
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


//contain words
fun containsWord(input: String, word: String): Boolean {
    val regex = "\\b$word\\b".toRegex()
    return regex.find(input) != null
}

//format decimal values remove trailing zero
fun formatDecimal(input: String): String {
    val decimalValue = try {
        BigDecimal(input).setScale(18, RoundingMode.DOWN).stripTrailingZeros()
    } catch (e: Exception) {
        BigDecimal.ZERO // If invalid input, use 0
    }

    val formattedString = decimalValue.toPlainString()

    // Remove trailing zeros after the decimal point
    val result = if (formattedString.contains(".") && formattedString.indexOf('.') < formattedString.length - 1) {
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
    // Create an Intent with the ACTION_VIEW action
    val intent = Intent(Intent.ACTION_VIEW)

    // Set the data (URL) for the Intent
    intent.data = Uri.parse(url)

    // Set the package to force opening in Chrome
    intent.`package` = "com.android.chrome"

    // Check if there's an activity that can handle the Intent
    if (intent.resolveActivity(context.packageManager) != null) {
        // Start the activity
        context.startActivity(intent)
    } else {
        // Chrome is not installed; open the URL in the default browser
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

fun weiToGwei(wei: BigInteger): BigInteger {
    val gweiFactor = BigInteger.TEN.pow(9)
    return wei.divide(gweiFactor)
}

fun gweiToWei(gwei: BigInteger): BigInteger {
    val gweiFactor = BigInteger.TEN.pow(9)
    return gwei.multiply(gweiFactor)
}

fun hexStringToBigInteger(hexString: String): BigInteger {
    // Remove the "0x" prefix if present
    val cleanHexString = if (hexString.startsWith("0x", ignoreCase = true)) {
        hexString.substring(2)
    } else {
        hexString
    }

    // Parse the hexadecimal string into a BigInteger
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

fun cleanEthereumAddress(inputAddress: String): String {
    // Remove leading and trailing whitespaces
    val trimmedAddress = inputAddress.trim()

    // Check if the address starts with "0x" and remove it if present
    val addressWithoutPrefix = if (trimmedAddress.startsWith("0x", ignoreCase = true)) {
        trimmedAddress.substring(2)
    } else {
        trimmedAddress
    }

    // Ensure that the cleaned address is exactly 40 characters long
    return if (addressWithoutPrefix.length == 40) {
        addressWithoutPrefix
    } else {
        // Handle error or return an indicator that the address is invalid
        // For example, you can return an empty string or throw an exception
        throw IllegalArgumentException("Invalid Receiver address: $inputAddress")
    }
}

fun getActualDigits(value: Any): String {
    if (value is String && "e" in value.lowercase()) {
        try {
            return BigDecimal(value).toPlainString()
        } catch (e: Exception) {
            return value.toString()
        }
    } else {
        return value.toString()
    }
}

fun convertScientificToBigDecimal(scientificValue: String): BigDecimal {
    return BigDecimal(scientificValue)
}

fun TextView.underlineText(text: String) {
    val spannableString = SpannableString(text)

    // Apply UnderlineSpan to the entire text
    spannableString.setSpan(UnderlineSpan(), 0, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

    // Apply ForegroundColorSpan (optional, for demonstration purposes)
    /*  val color = Color.BLUE
      spannableString.setSpan(ForegroundColorSpan(color), 0, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)*/

    // Set the SpannableString to the TextView
    this.text = spannableString
}

fun loge(tag: String = "TAG", message: String = "") {
    when {
        BuildConfig.DEBUG -> {
            Log.e(tag, message)
        }
    }
}






