package com.app.plutope.utils.extras
/*
import android.util.Base64
import com.app.plutope.BuildConfig
import org.json.JSONObject
import java.security.SecureRandom
import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec


object EncryptionUtils {
    private const val ENCRYPTION_ALGORITHM = "AES"
    private const val SECRET_KEY = "passphrase1234567"

    private const val ITERATION_COUNT = 10000
    private const val KEY_LENGTH = 256
    private const val SALT_LENGTH_BYTES = 16

    fun encryptPassphrase(mnemonic: String, passcode: String): String? {
        try {
            val salt = generateRandomSalt()
            val keySpec: KeySpec =
                PBEKeySpec(SECRET_KEY.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH)
            val secretKeyFactory: SecretKeyFactory =
                SecretKeyFactory.getInstance(BuildConfig.ALGORITHM_KEY)
            val secretKeyBytes: ByteArray = secretKeyFactory.generateSecret(keySpec).encoded
            val secretKey: SecretKey = SecretKeySpec(secretKeyBytes, ENCRYPTION_ALGORITHM)
            val cipher: Cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val encryptedBytes: ByteArray =
                cipher.doFinal((mnemonic + "|" + passcode).toByteArray(Charsets.UTF_8))
            val encodedBytes: String = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
            return encodedBytes
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun generateRandomSalt(): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(SALT_LENGTH_BYTES)
        random.nextBytes(salt)
        return salt
    }

    fun decryptPassphrase(encryptedPassphrase: String): String? {
        try {
            val salt = generateRandomSalt()
            val keySpec: KeySpec =
                PBEKeySpec(SECRET_KEY.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH)
            val secretKeyFactory: SecretKeyFactory =
                SecretKeyFactory.getInstance(BuildConfig.ALGORITHM_KEY)
            val secretKeyBytes: ByteArray = secretKeyFactory.generateSecret(keySpec).encoded
            val secretKey: SecretKey = SecretKeySpec(secretKeyBytes, ENCRYPTION_ALGORITHM)
            val cipher: Cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, secretKey)
            val decodedBytes: ByteArray = Base64.decode(encryptedPassphrase, Base64.NO_WRAP)
            val decryptedBytes: ByteArray = cipher.doFinal(decodedBytes)
            return String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }


    fun convertToJSON(encryptedPassphrase: String): String {
        val jsonObject = JSONObject()
        jsonObject.put("encryptedData", encryptedPassphrase)
        return jsonObject.toString()
    }
}
*/
