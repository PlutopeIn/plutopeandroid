package com.app.plutope.utils.constant

import com.app.plutope.model.Tokens

const val getNotificationType = "Notification_Type"
const val key_notification_type = "key_notification_type"
const val networkErrorMessage = "Network Error"
const val serverErrorMessage = "Server Error"

const val btcImplementationMessage = "BTC in not available now implemented coming soon"

enum class Language(val displayName: String, val code: String) {
    ENGLISH("English", "en"),
    THAI("Thai", "th"),
    HINDI("Hindi", "hi"),
    ARABIC("Arabic", "ar")
}

class JJ {
    var selectedLanguage: String = "en"
}


const val RELAY_URL = "relay.walletconnect.com"

//Page types

var appUpdateVersion = "1"

const val nftPageType = "1"
const val AddCustomTokenPageType = "2"

var isFromReceived: Boolean = false
var isPausedOnce: Boolean = false

const val buttonSwap = "Swap"
const val buttonSell = "Sell"
const val buttonRampable = "Rampable"

const val buttonMetaMask = "Meta mask"
const val buttonTrustWallet = "Trust wallet"
const val buttonPlutoPe = "PlutoPe"

const val typeSell = "Sell"
const val typeBuy = "Buy"

const val pageTypeBuy = "Buy"
const val pageTypeSwap = "Swap"

var isFullScreenLockDialogOpen = false

const val typeCountryList: Int = 1
const val typeCurrencyList: Int = 2
const val typeCountryCodeList: Int = 3

var lastSelectedSlippage = 2


//URL
const val BASE_URL = "https://sc.chimpareusa.com"
const val API_URL = "$BASE_URL/api/v2/"
const val cryptoCurrencyUrl = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/"
const val cryptoCurrencyMarketUrl = "https://api.coingecko.com/api/v3/coins/markets"
//Network messages

const val responseServerError = 500
const val responseBadRequest = 400

//Validation messages
const val CANT_BE_EMPTY = "Can't Be Empty!"
const val ENTER_EMAIL_PHONE = "Please enter email or mobile no."
const val ENTER_PASSWORD = "Please enter password"
const val backupnamecantempty = "Backup Name can't be empty!"

//INFURA API KEY
const val INFURA_KEY = "3c01e09390db4731bfebd48d42c7dde0"


//

const val BASE_URL_PLUTO_PE = "https://plutope.app/api/"
const val BASE_URL_PLUTO_PE_LOCAL = "http://192.168.29.77:3011/api/"


//change now api
const val CHANGE_NOW_BASE_URL = "https://api.changenow.io"
const val CHANGE_API_URL = "$CHANGE_NOW_BASE_URL/v2/"
const val EXCHANGE_API = CHANGE_API_URL + "exchange"
const val EXCHANGE_STATUS_API = "$EXCHANGE_API/by-id"
const val CHANGE_NOW_AVAILABLE_PAIR = "$EXCHANGE_API/available-pairs?"

const val CHANGE_NOW_BEST_PRICE = CHANGE_API_URL + "markets/estimate?"

const val CHANGE_NOW_API_KEY = "53600a5f3f67bc771bef9f3b0336c740d9c10d9db83e8df1491add59a09b6ccb"


const val CHANGE_NOW_ESTIMATION_BASE_URL = "https://vip-api.changenow.io"
const val CHANGE_MIN_API_URL = "$CHANGE_NOW_ESTIMATION_BASE_URL/v1.3/"
const val EXCHANGE_MIN_API = CHANGE_MIN_API_URL + "exchange/"
const val ESTIMATE_MIN_PRICE = EXCHANGE_MIN_API + "estimate?"


//Transaction history api
const val BASE_URL_ETHER_SCAN = "https://api.etherscan.io/"
const val API_URL_ETHER_SCAN = BASE_URL_ETHER_SCAN + "api?"
const val ETHER_SCAN_API_KEY = "1IT9WXZ9X2AVMUFJHRBP7E8I6W6TXIMHEJ"

const val BASE_URL_BSC_SCAN = "https://api.bscscan.com/"
const val API_URL_BSC_SCAN = BASE_URL_BSC_SCAN + "api?"
const val BSC_SCAN_API_KEY = "G5NXUANXH7RE8ZQXGXRVRJQDZ8RBNMJZ4S"

//polygon
const val BASE_URL_POLY_GONE = "https://api.polygonscan.com/"
const val API_URL_POLY_SCAN = BASE_URL_POLY_GONE + "api?"
const val POLY_API_KEY = "2QQ6FI7RA8G8R6IBJA52UA97TD9BH3SFG8"

//onMeta APi Key
const val ON_META_API_KEY = "31e2fd7c-0081-435e-ab7f-e6436e68cd52"
const val ON_META_BASE_URL = "https://api.onmeta.in/"
const val ON_META_API_URL = ON_META_BASE_URL + "v1"
const val ON_META_BEST_PRICE_API = "$ON_META_API_URL/quote/buy"
const val ON_META_BEST_PRICE_SELL_API = "$ON_META_API_URL/quote/sell"

//okx api
const val OKX_BASE_URL = "https://www.okx.com/"
const val OKX_API_URL = OKX_BASE_URL + "api/v5/dex/aggregator/"
const val OKX_SWAP_API = OKX_API_URL + "swap?"
const val OKX_APPROVE_API = OKX_API_URL + "approve-transaction?"
const val OKX_SECRETE_API_KEY = "98B9B815F07D1A67F243FDBF7066EE1E"
const val OKX_API_KEY = "f062cbc4-5228-47a8-a02c-8c9989e2244e"
const val OKX_PASSPHRASE = "Plutope@ApiKey1"
const val OKX_HEADER_SOURCE = "plutope"


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

//const val COIN_GEKO_COIN_LIST_API = "${COIN_GEKO_MARKETPRICE}list?include_platform=true"
const val COIN_GEKO_COIN_DETAIL = "$COIN_GEKO_MARKETPRICE"

const val COIN_GEKO_COIN_LIST_API = "https://plutope.app/api/get-all-tokens"

//const val TOKEN_IMAGE_LIST_API = "https://paloilapp.com/Laravel/public/list-coin"
const val TOKEN_IMAGE_LIST_API = "https://plutope.app/api/get-all-images"

const val COIN_GEKO_API_KEY = "CG-VfiNDCtBYqjguUNc7ijB6iok"

//nftMoralysis
const val NFT_MORALIS_API_KEY =
    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJub25jZSI6IjE4YTViOTIwLThmOWUtNDlmZi1hOGE4LTMzZTEyMWJjOWNkZCIsIm9yZ0lkIjoiMzI5NDkzIiwidXNlcklkIjoiMzM4NzgzIiwidHlwZUlkIjoiZjU2YmIyYzYtMGEzZi00NWNmLTgzNDAtMGE4NjAyZjQ0NDNlIiwidHlwZSI6IlBST0pFQ1QiLCJpYXQiOjE2ODMxODIzNjQsImV4cCI6NDgzODk0MjM2NH0.I2os4dXf1BJsQLWqpN9oGXmwEC0gSeV8mP4nfy9DY58"
const val NFT_BASE_URL = "https://deep-index.moralis.io/api/v2/"


//CoinMarketCap
const val COIN_MARKET_CAP_API_KEY =
    "7d6014cf-9a4e-41bf-8034-bd971c214ee7"//"7d00fe84-d1ea-47da-b2f6-53287366a15c"
const val COIN_MARKET_BASE_URL = "https://pro-api.coinmarketcap.com/v1"
const val COIN_MARKET_CURRENCY_URL = "$COIN_MARKET_BASE_URL/fiat/map"


//okLink APi
const val OK_LINK_ACCESS_KEY = "91db5ffb-a488-4cc6-863b-e81227f96038"
const val OK_LINK_BASE_URL = "https://www.oklink.com"
const val OK_LINK_API_URL = "$OK_LINK_BASE_URL/api/v5/"
const val OK_LINK_TRANSACTION_LIST = "${OK_LINK_API_URL}explorer/address/transaction-list?"
const val OK_LINK_TRANSACTION_DETAIL = "${OK_LINK_API_URL}explorer/transaction/transaction-fills?"


//onRamp api
const val ON_RAMP_API_KEY = "ezJrTUkLwuLOPEtQ278qYreMaRUq7n"
const val ON_RAMP_BASE_URL = "https://api.onramp.money"
const val ON_RAMP_API_URL = "${ON_RAMP_BASE_URL}/onramp/api/v2/"
const val ON_RAMP_BEST_PRICE_URL = "${ON_RAMP_API_URL}common/transaction/quotes"

//onMeld api
const val ON_MELD_BASE_URL = "https://api.meld.io"
const val ON_MELD_BEST_PRICE = "$ON_MELD_BASE_URL/payments/crypto/quote"
const val ON_MELD_KEY = "WQ5G9zvsK1cKC22iGZ8KXb:2mSZKY8pSiqYckZgDvnH9UNzphpDgosDyi73m"

//About us url
const val ABOUT_US_URL = "https://plutope.io"
//const val ABOUT_US_URL = "https://www.plutope.io"

//Alchemy Pay
const val ALCHEMY_PAY_APP_ID = "f83Is2y7L425rxl8"
const val ALCHEMY_PAY_SECRET_KEY = "4Yn8RkxDXN71Q3p0"
const val ALCHEMY_PAY_BASE_URL =
    /* "https://openapi-test.alchemypay.org"*/ "https://openapi.alchemypay.org"
const val ALCHEMY_PAY_API_URL = "${ALCHEMY_PAY_BASE_URL}/open/api/v3/"
const val ALCHEMY_PAY_BEST_PRICE_URL = "${ALCHEMY_PAY_API_URL}merchant/order/quote"

//Rampable
const val RAMPABLE_SECRET_KEY = "wpyYO6EyVSwx3QGY50d0VHCICTjiBHTTRGo7zbL6G6bxBtCSaGBrEbRB70ZhzdvP"

//Drive
const val PARENT_FOLDER_ID = "root"
const val FOLDER_NAME = "PlutoPeApp"

//default   chain address
const val DEFAULT_CHAIN_ADDRESS = "0xeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee"

const val NO_INTERNET_CONNECTION = "No Internet Connection"


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

var storedTokenList = listOf<Tokens>()