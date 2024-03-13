package com.app.plutope.utils.walletConnection

import com.app.plutope.model.Wallet
import com.app.plutope.networkConfig.Chain
import com.app.plutope.utils.coinTypeEnum.CoinType

import io.ipfs.multibase.Base16

var ACCOUNTS_1_EIP155_ADDRESS = Wallet.getPublicWalletAddress(CoinType.ETHEREUM)

val accounts: List<Pair<Chain, String?>> by lazy {
    listOf(
        Chain.Ethereum to ACCOUNTS_1_EIP155_ADDRESS,
        Chain.Polygon to ACCOUNTS_1_EIP155_ADDRESS,
        Chain.BinanceSmartChain to ACCOUNTS_1_EIP155_ADDRESS,
        Chain.OKC to ACCOUNTS_1_EIP155_ADDRESS
    )
}

val PRIVATE_KEY_1: ByteArray by lazy { Wallet.getPrivateKeyData(CoinType.ETHEREUM).hexToBytes() }

const val ISS_DID_PREFIX = "did:pkh:"

val ISSUER by lazy { accounts.map { it.toIssuer() }.first() }

fun Pair<Chain, String?>.toIssuer(): String = "$ISS_DID_PREFIX${first.chainIdHex}:$second"

fun ByteArray.bytesToHex(): String = Base16.encode(this)

fun String.hexToBytes(): ByteArray = Base16.decode(this.lowercase())