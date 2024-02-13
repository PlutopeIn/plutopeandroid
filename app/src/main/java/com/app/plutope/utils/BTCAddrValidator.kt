package com.app.plutope.utils

import org.bitcoinj.core.Address
import org.bitcoinj.core.AddressFormatException
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.params.MainNetParams


object BitcoinAddressValidator {

    fun validateBitcoinAddress(bitcoinAddress: String): Boolean {
        return if (isValidBtcAddress(bitcoinAddress)) {
            try {
                val networkParameters: NetworkParameters = MainNetParams.get()
                //  val networkParameters: NetworkParameters = TestNet3Params.get()
                Address.fromString(networkParameters, bitcoinAddress)
                true
            } catch (e: AddressFormatException) {
                false
            }
        } else {
            false
        }
    }

    private fun isValidBtcAddress(address: String): Boolean {
        val regexes = listOf(
            // P2PKH
            Regex("^(1|3)[a-km-zA-HJ-NP-Z1-9]{25,34}$"),
            // P2SH
            Regex("^3[a-km-zA-HJ-NP-Z1-9]{59,64}$"),
            // Bech32
            Regex("^(bc1|[tmb])[a-z0-9]{25,39}$"),
            // P2WPKH
            Regex("^(bc1|tb)[qpzry9x8gf2tvdw0s3jn54eh76km3c2]([qpzry9x8gf2tvdw0s3jn54eh76km3c2]{41}|[0-9]{13})+$"),
            // P2WSH
            Regex("^(bc1|tb)[qpzry9x8gf2tvdw0s3jn54eh76km3c2]{52}$"),
            // P2TR
            Regex("^(bc1|tb)[qza3mlkrs0-9]{42}$"),
        )

        return regexes.any { it.matches(address) }
    }
}