package com.app.plutope.utils

import com.app.plutope.BuildConfig
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec
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


    fun rsaDecrypt(encryptedText: String, privateKeyString: String): String {
        // Convert the private key from string to PrivateKey object
        return try {
            val privateKeyBytes = Base64.getDecoder().decode(privateKeyString)
            val keySpec = PKCS8EncodedKeySpec(privateKeyBytes)
            val keyFactory = KeyFactory.getInstance("RSA")
            val privateKey = keyFactory.generatePrivate(keySpec)

            // Decrypt the text
            val cleanedEncryptedText = encryptedText.replace("\\s".toRegex(), "")
            val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
            cipher.init(Cipher.DECRYPT_MODE, privateKey)
            val decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(cleanedEncryptedText))
            String(decryptedBytes)
        } catch (e: Exception) {
            e.printStackTrace()
            encryptedText
        }

    }


}