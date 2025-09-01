package com.app.plutope.utils.constant

import androidx.core.text.HtmlCompat
import com.app.plutope.R
import com.app.plutope.model.Tokens
import com.app.plutope.ui.fragment.card.card_wallet_list.CardWalletListResponseModel
import com.app.plutope.ui.fragment.card.my_card.CardListResponseModel
import com.app.plutope.ui.fragment.card_beta.user_management.card_dashboard.model.AccountModel
import com.app.plutope.utils.extras.PreferenceHelper
import com.app.plutope.utils.isScientificNotation
import com.app.plutope.utils.loge

const val getNotificationType = "Notification_Type"
const val key_notification_type = "key_notification_type"
const val networkErrorMessage = "Network Error"
const val serverErrorMessage = "Server Error"

const val btcImplementationMessage = "BTC in not available now implemented coming soon"

enum class Language(
    val displayName: String,
    val code: String,
    val imageResId: Int,
    var isSelected: Boolean = false
) {
    HINDI("Hindi", "hi", R.drawable.ic_india_flag),
    ENGLISH("English", "en", R.drawable.ic_america_flag),
    THAI("Thai", "th", R.drawable.ic_thai_flag)
}


const val RELAY_URL = "relay.walletconnect.com"

//Page types

/***
 * If you need to see update screen in your app change needAppUpdateVersion and appUpdateVersion both count update currentVersion+1
 * **/

var needAppUpdateVersion = "5"  // last 4
var appUpdateVersion = "6"   //5

const val nftPageType = "1"
const val AddCustomTokenPageType = "2"

const val registerType = "1"
const val passwordType = "2"

var isForceDownLockScreen: Boolean = false

var isFromReceived: Boolean = false
var isPausedOnce: Boolean = false

var statusKYC1Process = ""
var statusKYC2Process = ""

var isChangePin: Boolean = false
var typeFromChangePin = 1
var isOpenAlreadyOtpDialog = false
var isCardOtpDialog = false


const val typeSell = "Sell"
const val typeBuy = "Buy"

const val pageTypeBuy = "Buy"
const val pageTypeSwap = "Swap"


const val pageTypePhoneNumber = "phone_number"
const val pageTypeWalletAddress = "wallet_address"

var isFullScreenLockDialogOpen = false

const val typeCountryList: Int = 1
const val typeCurrencyList: Int = 2
const val typeCountryCodeList: Int = 3

var lastSelectedSlippage = 1

var isFromList: Boolean = false

var defaultPLTTokenId = "plt"


//URL

const val cryptoCurrencyUrl = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/"

const val responseServerError = 500
const val responseBadRequest = 400 or 403
const val responseUnAuthorizedRequest = 401

//Validation messages
const val CANT_BE_EMPTY = "Can't Be Empty!"
const val ENTER_EMAIL_PHONE = "Please enter email or mobile no."

const val CURRENT_PASSWORD_EMPTY_VALIDATION = "Please enter current password"
const val NEW_PASSWORD_EMPTY_VALIDATION = "Please enter new password"
const val PASSWORD_EMPTY_VALIDATION_NEW = "Please enter confirm password"
const val PASSWORD_MATCH_VALIDATION = "Password and confirm password does not match"
const val ENTER_PASSWORD = "Please enter password"
const val backupnamecantempty = "Backup Name can't be empty!"
const val phrasesCanNotBeEmpty = "Phrases can't be empty"


//

const val BASE_URL_PLUTO_PE = "https://plutope.app/api/"
const val BASE_URL_PLUTO_PE_IMAGES = BASE_URL_PLUTO_PE + "images/"

enum class CardBetaEnv(val type: String) {
    LIVE("Live"),
    TEST("Test"),
}

var cardBetaENV = CardEnv.LIVE.type

const val liveCardBetaUrl = "https://api.sandbox-v2.vault.ist/"
const val testCardBetaUrl = "https://api.sandbox-v2.vault.ist/"
const val partnerID = "{your_card_partner_id}"
const val clientId = "{your_card_client_id}"
val CARD_BETA_BASE_URL = when (cardBetaENV) {
    CardEnv.LIVE.type -> liveCardBetaUrl
    else -> testCardBetaUrl
}


enum class CardEnv(val type: String) {
    LIVE("Live"),
    TEST("Test"),
}

/** @Pravin You can change card environment from here live(CardEnv.LIVE.type) or test(CardEnv.TEST.type) **/
var cardENV = CardEnv.LIVE.type

const val liveCardUrl = "https://api.crypterium.com/"
const val testCardUrl = "https://api.vault.sandbox.testessential.net/"
val VAULT_SAND_BOX_URL = when (cardENV) {
    CardEnv.LIVE.type -> liveCardUrl
    else -> testCardUrl
}
val VAULT_X_MERCHANT_ID =
    if (cardENV == CardEnv.LIVE.type) "{live_card_merchant_id}" else "{test_card_merchant_id}"

val VAULT_SEND_BOX_URL_VERSION_V1 = VAULT_SAND_BOX_URL + "v1/"
val VAULT_SEND_BOX_URL_VERSION_V2 = VAULT_SAND_BOX_URL + "v2/"
val VAULT_SEND_BOX_URL_VERSION_V3 = VAULT_SAND_BOX_URL + "v3/"
val VAULT_SEND_BOX_URL_VERSION_V4 = VAULT_SAND_BOX_URL + "v4/"
const val VAULT_X_VERSION = "1.2"
const val VaultCardProgram = "CP_2"


//change now api
const val CHANGE_NOW_BASE_URL = "https://api.changenow.io"
const val CHANGE_API_URL = "$CHANGE_NOW_BASE_URL/v2/"
const val EXCHANGE_API = CHANGE_API_URL + "exchange"
const val EXCHANGE_STATUS_API = "$EXCHANGE_API/by-id"
const val CHANGE_NOW_AVAILABLE_PAIR = "$EXCHANGE_API/available-pairs?"



const val CHANGE_NOW_API_KEY = "{CHANGE_NOW_API_KEY}"


//Transaction history api
const val BASE_URL_ETHER_SCAN = "https://api.etherscan.io/"
const val API_URL_ETHER_SCAN = BASE_URL_ETHER_SCAN + "api?"
const val ETHER_SCAN_API_KEY = "{ETHER_SCAN_API_KEY}"

const val BASE_URL_BSC_SCAN = "https://api.bscscan.com/"
const val API_URL_BSC_SCAN = BASE_URL_BSC_SCAN + "api?"
const val BSC_SCAN_API_KEY = "{BSC_SCAN_API_KEY}"

//polygon
const val BASE_URL_POLY_GONE = "https://api.polygonscan.com/"
const val API_URL_POLY_SCAN = BASE_URL_POLY_GONE + "api?"
const val POLY_API_KEY = "{POLY_API_KEY}"

//onMeta APi Key
const val ON_META_API_KEY = "{ON_META_API_KEY}"

//okx api
const val OKX_SECRETE_API_KEY = "{OKX_SECRETE_API_KEY}"
const val OKX_API_KEY = "{OKX_API_KEY}"
const val OKX_PASSPHRASE = "{OKX_PASSPHRASE}"
const val OKX_HEADER_SOURCE = "{OKX_HEADER_SOURCE}"


var isImportWallet: Boolean = false


//CoinGeko MarketPrice
const val COIN_GEKO_BASE_URL = "https://api.coingecko.com/"
const val COIN_GEKO_PRO_BASE_URL = "https://pro-api.coingecko.com/"
const val COIN_GEKO_API_URL = "${COIN_GEKO_BASE_URL}api/v3/"
const val COIN_GEKO_MARKETPRICE = "${COIN_GEKO_API_URL}coins/"
const val COIN_GEKO_PRO_API_URL = "${COIN_GEKO_PRO_BASE_URL}api/v3/"
const val COIN_GEKO_MARKET_API = "${COIN_GEKO_API_URL}coins/markets"
const val COIN_GEKO_PRO_MARKET_API = "${COIN_GEKO_PRO_API_URL}coins/markets"


/*
const val COIN_GEKO_PLUTO_PE_SERVER_URL = "https://plutope.app/api/markets-price?currency="
const val COIN_GEKO_PLUTO_PE_SERVER_URL_NEW = "https://plutope.app/api/markets-price-new?currency="
*/

const val COIN_GEKO_PLUTO_PE_SERVER_URL =
    "https://plutope.app/api/markets-price-v2-filter?currency="
const val COIN_GEKO_PLUTO_PE_SERVER_URL_NEW =
    "https://plutope.app/api/markets-price-v2-filter?currency="


const val COIN_GEKO_COIN_DETAIL = "$COIN_GEKO_MARKETPRICE"

const val COIN_GEKO_COIN_LIST_API = "https://plutope.app/api/get-all-tokens"

const val TOKEN_IMAGE_LIST_API = "https://plutope.app/api/get-all-images"


//nftMoralysis
const val NFT_MORALIS_API_KEY =
    "{NFT_MORALIS_API_KEY}"


//CoinMarketCap
const val COIN_MARKET_CAP_API_KEY =
    "{COIN_MARKET_CAP_API_KEY}"

//okLink APi
const val OK_LINK_ACCESS_KEY = "{OK_LINK_ACCESS_KEY}"

//Moralis APi
const val moralis_access_key =
    "{moralis_access_key}"

//onRamp api
const val ON_RAMP_API_KEY = "{ON_RAMP_API_KEY}"

//onMeld api
const val ON_MELD_KEY = "{ON_MELD_KEY}"

//About us url
const val ABOUT_US_URL = "https://plutope.io"


//Alchemy Pay
const val ALCHEMY_PAY_APP_ID = "{ALCHEMY_PAY_APP_ID}"

//Drive
const val PARENT_FOLDER_ID = "root"
const val FOLDER_NAME = "PlutoPeApp"

//default   chain address
const val DEFAULT_CHAIN_ADDRESS = "0xeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee"

const val NO_INTERNET_CONNECTION = "No Internet Connection"
const val UN_AUTHORIZED_ACCESS = "Access token expired"


//kip20
const val KIP_20 = "KIP20"
const val PERCENTAGE_6F = "%.6f"

//send validation
const val ENTER_SENDER_ADDRESS = "Enter Sender Address!"
const val ENTER_AMOUNT = "Enter Amount!"
const val PLEASE_ENTER_SOME_AMOUNT = "Please enter some amount"
const val INVALID_BALANCE = "You haven't enough balance to send entered amount"
const val INVALID_SENDER_ADDRESS = "Invalid receiver address"

const val ENTER_GAS_PRICE_EMPTY = "Enter gas price can't be empty!"
const val ENTER_GAS_PRICE = "Enter some gas price!"
const val ENTER_GAS_LIMIT = "Enter gas limit!"
const val ENTER_NONCE = "Enter nonce!"

//What is Custom Token URL
const val WHAT_IS_CUSTOM_TOKEN_URL = "https://blogplutope.com/custom-tokens/"
const val WHAT_IS_SECRET_PHRASE_URL =
    "https://blogplutope.com/all-you-need-to-know-about-secret-phrase/"

// Unlimit crypto api urls

var UNLIMIT = "UNLIMIT"

const val UNLIMITE_GATEFI_BASE_URL = "https://api-sandbox.gatefi.com/"
const val UNLIMITE_GATEFI_API_URL = "onramp/v1/"

enum class CardDesign(val colorName: String, val colorCode: Int, val cardBackground: Int) {
    BLUE("BLUE", R.color.blue, R.drawable.img_card_color_blue),
    ORANGE("ORANGE", R.color.orange, R.drawable.img_card_color_blue), // Orange color
    BLACK("BLACK", R.color.black, R.drawable.img_card_color_blue),
    GOLD("GOLD", R.color.gold, R.drawable.img_card_color_gold), // Gold color
    PURPLE("PURPLE", R.color.purple, R.drawable.img_card_color_blue) // Purple color
}


var storedTokenList = listOf<Tokens>()
var supportedVaultCurrency = mutableListOf(
    "BAT",
    "BTC",
    "CHO",
    "CRPT",
    "DAI",
    "DAO",
    "ETH",
    "GALA",
    "LINK",
    "LTC",
    "MANA",
    "MAPS",
    "MATIC",
    "MKR",
    "OMG",
    "QASH",
    "REP",
    "SAND",
    "SHIB",
    "UNI",
    "USDC",
    "USDT",
    "XRP",
    "ZRX"
)

var walletTokenList: MutableList<CardWalletListResponseModel.Wallet> = arrayListOf()
var cardStoredList: MutableList<CardListResponseModel.Card> = arrayListOf()
var lastSelectedWallet = CardWalletListResponseModel.Wallet()

var lastSelectedContactNumber = ""

var selectedCardColor = 0

var isFromTransactionDetail = false


fun scientificNotationToNormalNumber(value: String): String {
    return if (isScientificNotation(value)) {
        val fullNumberString = "%.10f".format(value.toDouble())
        fullNumberString
    } else {
        HtmlCompat.fromHtml(value, 0).toString()

    }
}

/**Start card Beta form here **/

fun getBarrierToken(): String {
    return "Bearer " + PreferenceHelper.getInstance().betaCardAccessToken
}

var accountList: MutableList<AccountModel> = mutableListOf()

const val reasonTypeExchange = "Exchange"
const val reasonTypeDeposit = "Deposit"
const val reasonTypeWithdrawal = "Withdrawal"

