package com.app.plutope.utils.extras

import android.content.Context
import android.content.SharedPreferences
import com.app.plutope.model.CurrencyModel
import com.app.plutope.ui.base.App
import com.app.plutope.utils.Securities
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceHelper @Inject constructor(@ApplicationContext context: Context) {
    private val _prefName = "TRUST_WALLET"

    private val KEY_CRYPTO_DATA = "key_crypto_data"
    private val KEY_IS_WALLET_CREATED = "KEY_IS_WALLET_CREATED"
    private val KEY_MENEMONIC_WALLET = "key_menemonic_wallet"
    private val IS_BIOMETRIC_ENABLE = "IS_BIOMETRIC_ENABLE"
    private val IS_ACTIVE_WALLET = "IS_ACTIVE_WALLET"
    private val KEY_SELECTED_CURRENCY = "KEY_SELECTED_CURRENCY"
    private val KEY_WALLET_LIST = "WalletList"
    private val KEY_GRANTED_PUSH_NOTIFICATION = "KEY_GRANTED_PUSH_NOTIFICATION"
    private val KEY_IS_LOCK_ONLY_PASSCODE = "KEY_IS_LOCK_ONLY_PASSCODE"
    private val KEY_IS_APP_LOCK = "KEY_IS_APP_LOCK"
    private val KEY_IS_TRANSACTION_SIGNING = "KEY_IS_TRANSACTION_SIGNING"
    private val KEY_IS_TOKEN_IMAGE_API_CALL = "KEY_IS_TOKEN_IMAGE_API_CALL"
    private val KEY_IS_TOKEN_IMAGE_UPDATE_CALL = "KEY_IS_TOKEN_IMAGE_UPDATE_CALL"
    private val KEY_IS_FIRST_TIME = "KEY_IS_FIRST_TIME"

    private val KEY_APP_PASSWORD = "KEY_APP_PASSWORD"
    private val KEY_DRIVE_FOLDER_ID = "KEY_DRIVE_FOLDER_ID"
    private val KEY_IS_SPLASH_SCREEN_SHOW = "KEY_IS_SPLASH_SCREEN_SHOW"
    private val KEY_LANGUAGE = "KEY_LANGUAGE"
    private val KEY_FIREBASE_TOKEN = "KEY_FIREBASE_TOKEN"
    private val KEY_DEVICE_ID = "KEY_DEVICE_ID"
    private val KEY_REFERRAL_CODE = "KEY_REFERRAL_CODE"

    private val KEY_UPDATE_APP = "KEY_UPDATE_APP"


    private var preferences: SharedPreferences =
        context.getSharedPreferences(_prefName, Context.MODE_PRIVATE)


    var menomonicWallet
        get() = Securities.decrypt(preferences.getString(KEY_MENEMONIC_WALLET, ""))
        set(menomonicWallet) = preferences.edit()
            .putString(KEY_MENEMONIC_WALLET, Securities.encrypt(menomonicWallet))
            .apply()
    var firebaseToken
        get() = Securities.decrypt(preferences.getString(KEY_FIREBASE_TOKEN, ""))
        set(firebaseToken) = preferences.edit()
            .putString(KEY_FIREBASE_TOKEN, Securities.encrypt(firebaseToken))
            .apply()
    var deviceId
        get() = Securities.decrypt(preferences.getString(KEY_DEVICE_ID, ""))
        set(deviceId) = preferences.edit().putString(KEY_DEVICE_ID, Securities.encrypt(deviceId))
            .apply()
    var referralCode
        get() = Securities.decrypt(preferences.getString(KEY_REFERRAL_CODE, ""))
        set(referralCode) = preferences.edit()
            .putString(KEY_REFERRAL_CODE, Securities.encrypt(referralCode))
            .apply()

    var appUpdatedFlag
        get() = preferences.getString(KEY_UPDATE_APP, "")
        set(appUpdatedFlag) = preferences.edit().putString(KEY_UPDATE_APP, appUpdatedFlag)
            .apply()


    var currentLanguage
        get() = Securities.decrypt(
            preferences.getString(
                KEY_LANGUAGE,
                Securities.encrypt("en")
            )
        )
        set(value) = preferences.edit().putString(KEY_LANGUAGE, Securities.encrypt(value))
            .apply()
    var appPassword
        get() = Securities.decrypt(preferences.getString(KEY_APP_PASSWORD, ""))
        set(appPassword) = preferences.edit()
            .putString(KEY_APP_PASSWORD, Securities.encrypt(appPassword))
            .apply()

    var walletList
        get() = Securities.decrypt(preferences.getString(KEY_WALLET_LIST, ""))
        set(walletList) = preferences.edit()
            .putString(KEY_WALLET_LIST, Securities.encrypt(walletList))
            .apply()


    var cryptoData
        get() = preferences.getString(KEY_CRYPTO_DATA, "")
        set(cryptoData) = preferences.edit().putString(KEY_CRYPTO_DATA, cryptoData).apply()

    var isWalletCreatedData
        get() = preferences.getBoolean(KEY_IS_WALLET_CREATED, false)
        set(walletCreated) = preferences.edit().putBoolean(KEY_IS_WALLET_CREATED, walletCreated)
            .apply()

    var isGrantedPushNotification
        get() = preferences.getBoolean(KEY_GRANTED_PUSH_NOTIFICATION, false)
        set(pushNotification) = preferences.edit()
            .putBoolean(KEY_GRANTED_PUSH_NOTIFICATION, pushNotification)
            .apply()

    companion object {
        fun getInstance(): PreferenceHelper {
            return PreferenceHelper(App.getContext())
        }
    }


    var isBiometricAllow: Boolean
        get() = preferences.getBoolean(IS_BIOMETRIC_ENABLE, false)
        set(value) = preferences.edit().putBoolean(KEY_IS_WALLET_CREATED, value).apply()
    var isActiveWallet: Boolean
        get() = preferences.getBoolean(IS_ACTIVE_WALLET, false)
        set(value) = preferences.edit().putBoolean(IS_ACTIVE_WALLET, value).apply()


    fun clear() {
        preferences.edit().clear().apply()
    }


    fun setSelectedCurrency(currency: CurrencyModel) {
        val json = Gson().toJson(currency)
        preferences.edit().putString(KEY_SELECTED_CURRENCY, Securities.encrypt(json)).apply()
    }

    fun getSelectedCurrency(): CurrencyModel? {
        val json = preferences.getString(KEY_SELECTED_CURRENCY, null)
        val decrypted =
            if (json.equals(null) or json.equals("null")) json else Securities.decrypt(json)
        return Gson().fromJson(decrypted, CurrencyModel::class.java)
    }


    var isLockModePassword
        get() = preferences.getBoolean(KEY_IS_LOCK_ONLY_PASSCODE, false)
        set(lockMode) = preferences.edit()
            .putBoolean(KEY_IS_LOCK_ONLY_PASSCODE, lockMode)
            .apply()

    var isAppLock
        get() = preferences.getBoolean(KEY_IS_APP_LOCK, true)
        set(appLock) = preferences.edit()
            .putBoolean(KEY_IS_APP_LOCK, appLock)
            .apply()

    var isTransactionSignIn
        get() = preferences.getBoolean(KEY_IS_TRANSACTION_SIGNING, true)
        set(transactionSignIn) = preferences.edit()
            .putBoolean(KEY_IS_TRANSACTION_SIGNING, transactionSignIn)
            .apply()

    var isTokenImageCalled
        get() = preferences.getBoolean(KEY_IS_TOKEN_IMAGE_API_CALL, false)
        set(transactionSignIn) = preferences.edit()
            .putBoolean(KEY_IS_TOKEN_IMAGE_API_CALL, transactionSignIn)
            .apply()

    var isTokenImageUpdateCalled
        get() = preferences.getBoolean(KEY_IS_TOKEN_IMAGE_UPDATE_CALL, false)
        set(transactionSignIn) = preferences.edit()
            .putBoolean(KEY_IS_TOKEN_IMAGE_UPDATE_CALL, transactionSignIn)
            .apply()

    var isFirstTime
        get() = preferences.getBoolean(KEY_IS_FIRST_TIME, false)
        set(isFirst) = preferences.edit()
            .putBoolean(KEY_IS_FIRST_TIME, isFirst)
            .apply()


    var appFolderId
        get() = preferences.getString(KEY_DRIVE_FOLDER_ID, "")
        set(driveFolder) = preferences.edit().putString(KEY_DRIVE_FOLDER_ID, driveFolder)
            .apply()


    var isSplashScreenShow
        get() = preferences.getBoolean(KEY_IS_SPLASH_SCREEN_SHOW, false)
        set(isSplashScreenShow) = preferences.edit()
            .putBoolean(KEY_IS_SPLASH_SCREEN_SHOW, isSplashScreenShow)
            .apply()

}