package com.app.plutope.utils

import com.app.plutope.BuildConfig
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object Securities {

    fun encrypt(privateKey: String?): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val secretKey = SecretKeySpec(BuildConfig.ENCRYPTION_KEY.toByteArray(), "AES")
        val ivParameterSpec = IvParameterSpec(BuildConfig.INITIAL_VACTOR.toByteArray())
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec)
        val encrypted = cipher.doFinal(privateKey?.toByteArray())
        return Base64.getEncoder().encodeToString(encrypted)
    }

    fun decrypt(encryptedPrivateKey: String?): String {
        return try {
            val encryptedData = Base64.getDecoder().decode(encryptedPrivateKey)
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val secretKey = SecretKeySpec(BuildConfig.ENCRYPTION_KEY.toByteArray(), "AES")
            val ivParameterSpec = IvParameterSpec(BuildConfig.INITIAL_VACTOR.toByteArray())
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec)
            val decrypted = cipher.doFinal(encryptedData)
            String(decrypted)
        } catch (e: Exception) {
            encryptedPrivateKey!!
        }
    }

}