package com.app.plutope.ui.base

import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.TransitionDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.LocaleList
import android.os.Looper
import android.os.PersistableBundle
import android.provider.Settings.ACTION_SECURITY_SETTINGS
import android.provider.Settings.Secure
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.postDelayed
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.android.installreferrer.api.InstallReferrerClient
import com.app.plutope.R
import com.app.plutope.custom_views.CustomAlertDialog
import com.app.plutope.databinding.ActivityBaseBinding
import com.app.plutope.dialogs.DeviceLockFullScreenDialog
import com.app.plutope.dialogs.SwapProgressDialog
import com.app.plutope.dialogs.walletConnectionDialog.DialogWalletApproveTransaction
import com.app.plutope.dialogs.walletConnectionDialog.DialogWalletConnectionConfirmation
import com.app.plutope.model.CurrencyModel
import com.app.plutope.model.Tokens
import com.app.plutope.model.TransactionModelDApp
import com.app.plutope.model.Wallet
import com.app.plutope.model.Wallet.refreshWallet
import com.app.plutope.model.parseData
import com.app.plutope.networkConfig.Chains
import com.app.plutope.ui.fragment.phrase.recovery_phrase.VerifySecretPhraseViewModel
import com.app.plutope.ui.fragment.transactions.send.send_coin.TransferNetworkDetail
import com.app.plutope.utils.ConnectivityReceiver
import com.app.plutope.utils.DeviceLockOpenSuccess
import com.app.plutope.utils.constant.isForceDownLockScreen
import com.app.plutope.utils.constant.isPausedOnce
import com.app.plutope.utils.extras.BiometricResult
import com.app.plutope.utils.extras.FirebaseAnalyticsHelper.logEvent
import com.app.plutope.utils.extras.PreferenceHelper
import com.app.plutope.utils.extras.lock_request_code
import com.app.plutope.utils.extras.security_setting_request_code
import com.app.plutope.utils.hideLoader
import com.app.plutope.utils.loge
import com.app.plutope.utils.network.NetworkState
import com.app.plutope.utils.openCustomDeviceLock
import com.app.plutope.utils.safeNavigate
import com.app.plutope.utils.showToast
import com.app.plutope.utils.walletConnection.WalletConnectionUtils
import com.app.plutope.utils.walletConnection.WalletConnectionUtils.WalletConnectionMethod.walletSwitchEthereumChain
import com.app.plutope.utils.walletConnection.WalletConnectionUtils.initialWalletConnection
import com.app.plutope.utils.walletConnection.Web3WalletViewModel
import com.app.plutope.utils.walletConnection.compose_ui.connections.ConnectionsViewModel
import com.app.plutope.utils.walletConnection.sendResponseDeepLink
import com.app.plutope.utils.walletConnection.session_request.SessionRequestViewModel
import com.app.plutope.utils.walletConnection.state.AuthEvent
import com.app.plutope.utils.walletConnection.state.SignEvent
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.james.tronwallet.TronWeb
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.web3j.utils.Numeric
import java.io.IOException
import java.util.Locale
import java.util.concurrent.ExecutionException
import javax.inject.Inject


@AndroidEntryPoint
open class BaseActivity : AppCompatActivity(), ConnectivityReceiver.ConnectivityReceiverListener {
    private var referrerClient: InstallReferrerClient? = null
    private var value: Bundle? = null
    private var retryCount = 0
    private val maxRetries = 5
    var selectedCurrency: CurrencyModel? = null
    private lateinit var appBarConfiguration: AppBarConfiguration
    lateinit var binding: ActivityBaseBinding
    var navController: NavController? = null
    var bioMetricDialog: CustomAlertDialog? = null
    var isShowCenterMenus: Boolean = false
    var openDefaultPass: Boolean = false


    private var deviceLockDialog: DeviceLockFullScreenDialog? = null

    private val verifyPhraseViewModel: VerifySecretPhraseViewModel by viewModels()

    private val web3WalletViewModel: Web3WalletViewModel by viewModels()
    private val connectionsViewModel: ConnectionsViewModel by viewModels()

    var isDashboardFromCard: Boolean = false

    var isFromDeepLink: Boolean = false
    var walletConnection: Boolean = false
    var walletPairingUrl: String = ""

    @Inject
    lateinit var preferenceHelper: PreferenceHelper

    var tronweb: TronWeb? = null


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBundle("key", value)
    }

    override fun onRestoreInstanceState(
        savedInstanceState: Bundle?, persistentState: PersistableBundle?
    ) {
        super.onRestoreInstanceState(savedInstanceState, persistentState)
        value = savedInstanceState
    }

    override fun onResume() {
        super.onResume()
        loge("isForceDownLockScreen", "isForceDownLockScreen :$isForceDownLockScreen")


        this.let {
            if (!it.isFinishing && !it.isDestroyed) {
                if (preferenceHelper.menomonicWallet != "" && !isForceDownLockScreen) {
                    if (preferenceHelper.appUpdatedFlag != "") {
                        if (preferenceHelper.isAppLock) {
                            if (/*navController?.currentDestination?.id != R.id.sendCoin &&*/ navController?.currentDestination?.id != R.id.addCustomToken && navController?.currentDestination?.id != R.id.addContactFragment && navController?.currentDestination?.id != R.id.confirmEncryptionPassword && navController?.currentDestination?.id != R.id.selectWalletBackup && navController?.currentDestination?.id != R.id.walletConnect && navController?.currentDestination?.id != R.id.browser && navController?.currentDestination?.id != R.id.dashboard) {
                                isPausedOnce = false
                                if (navController?.currentDestination?.id == R.id.splash) {
                                    // Handler(Looper.getMainLooper()).postDelayed(1000) {
                                    hideLoader()

                                    if (!preferenceHelper.isLockModePassword) {
                                        setBioMetric(biometricListener)
                                    } else {
                                        this@BaseActivity.openCustomDeviceLock(object :
                                            DeviceLockOpenSuccess {
                                            override fun onSuccessOpenDeviceLock() {
                                                openAndPairWalletConnection()
                                            }

                                        })
                                    }


                                    // }
                                } else {
                                    if (!preferenceHelper.isLockModePassword) {
                                        setBioMetric(biometricListener)
                                    } else {
                                        this@BaseActivity.openCustomDeviceLock(object :
                                            DeviceLockOpenSuccess {
                                            override fun onSuccessOpenDeviceLock() {
                                                openAndPairWalletConnection()
                                            }

                                        })
                                    }
                                }
                            }
                        }
                    }
                } else {
                    isForceDownLockScreen = false
                }
            }
        }



        if (intent != null) {
            if (intent.resolveActivity(packageManager) != null) {
                val myIntent = intent
                myIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                myIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                myIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                // handleDynamicLink(myIntent)
                myIntent?.handle()

                intent = null
            }
        }

        this.logEvent()
        checkInternetConnection()
    }

    override fun onPause() {
        super.onPause()
        isPausedOnce = true
    }

    private fun getAndroidId(): String {
        return Secure.getString(this.contentResolver, Secure.ANDROID_ID)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        val saveInstState = if (value != null) value else savedInstanceState
        super.onCreate(saveInstState)
        binding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setStatusBarGradiant()
        tronweb = TronWeb(this, _webView = binding.webviewTron)

        initialWalletConnection(this)
        checkInternetConnection()


        setSupportActionBar(binding.toolbar)
        binding.toolbar.title = ""
        navController =
            (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController
        appBarConfiguration = AppBarConfiguration(navController!!.graph)

        NavigationUI.setupActionBarWithNavController(this, navController!!)
        binding.switchBottom.setOnClickCallback {
            if (binding.switchBottom.nowChoose == 0) {
                if (!preferenceHelper.isCardLogin) {
                    if (navController?.currentDestination?.id != R.id.startCardFlow) {
                        navController!!.safeNavigate(R.id.action_global_to_start_card_flow)
                    }
                } else {
                    if (navController?.currentDestination?.id != R.id.cardHome) {
                        navController!!.safeNavigate(R.id.action_global_to_card_home)
                    }

                    /* if (navController?.currentDestination?.id != R.id.cardDashboardV2Fragment) {
                         navController!!.safeNavigate(R.id.action_global_to_cardDashboardV2Fragment)
                     }*/

                }
            } else {
                isDashboardFromCard = true
                navController!!.safeNavigate(R.id.action_global_to_dashboard)
            }
        }
        binding.switchBottom.setOnDragCallback { direction ->
            if (direction == 0) {
                if (!preferenceHelper.isCardLogin) {
                    if (navController?.currentDestination?.id != R.id.startCardFlow) {
                        navController!!.safeNavigate(R.id.action_global_to_start_card_flow)
                    }
                } else {
                    if (navController?.currentDestination?.id != R.id.cardHome) {
                        navController!!.safeNavigate(R.id.action_global_to_card_home)
                    }
                }
            } else {
                isDashboardFromCard = true
                navController!!.safeNavigate(R.id.action_global_to_dashboard)
            }
        }


        changeLanguage(preferenceHelper.currentLanguage)

        navController!!.addOnDestinationChangedListener { _, destination, _ ->
            binding.toolbar.title = ""
            when (destination.id) {
                R.id.notification, R.id.selectWalletBackup, R.id.addENS -> {
                    showToolbarTransparentBack()
                    showBottomNavigation(false)
                }

                R.id.dashboard, R.id.card, R.id.cardHome, R.id.cardDashboardV2Fragment -> {
                    binding.toolbar.visibility = View.GONE
                    showBottomNavigation(true)
                    binding.switchBottom.setSelectionDynamically(
                        if (destination.id == R.id.dashboard) 1 else 0, true
                    )

                }


                else -> {
                    binding.toolbar.visibility = View.GONE
                    showBottomNavigation(false)

                }
            }
        }

        preferenceHelper.deviceId = getAndroidId()



        setupObserver()
        setWalletObject()
        setCurrency()
        getFCMTokenWithRetry()
        intent?.handle()
    }

    fun checkInternetConnection() {
        val connectivityReceiver = ConnectivityReceiver()
        connectivityReceiver.setConnectivityReceiverListener(this)
        registerReceiver(
            connectivityReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )

    }

    fun changeLanguage(languageName: String, isRestart: Boolean = false) {
        loge("languageName", "::> $languageName  :: $isRestart")
        val locale = Locale(languageName)
        Locale.setDefault(locale)

        val resources = this.resources
        val configuration = Configuration(this.applicationContext.resources.configuration)

        val localeList = LocaleList(locale)
        LocaleList.setDefault(localeList)
        configuration.setLocales(localeList)


        resources.updateConfiguration(configuration, resources.displayMetrics)

        if (isRestart) {
            val id = navController?.currentDestination?.id
            navController?.popBackStack(id!!, true)
            navController?.navigate(id!!)

        }
    }


    private fun setCurrency() {
        selectedCurrency = preferenceHelper.getSelectedCurrency()
        val defaultCurrency = CurrencyModel(code = "INR", name = "Indian Rupee", symbol = "₹")
        if (selectedCurrency == null) {
            preferenceHelper.setSelectedCurrency(defaultCurrency)
        }
    }

    fun setWalletObject() {
        verifyPhraseViewModel.getPrimaryWallet()
    }

    private fun isShowCenterMenuItem(isShow: Boolean) {
        if (isShow) {
            binding.bottomNavigation.visibility = View.GONE
        } else {
            binding.bottomNavigation.visibility = View.VISIBLE
        }
    }

    fun showBottomNavigation(isShow: Boolean = false) {
        if (isShow) {
            val button = Button(this)
            button.text = ""
            button.isEnabled = false
            button.visibility = View.GONE
            binding.bottomNavigation.visibility = View.VISIBLE
            isShowCenterMenus = false
            isShowCenterMenuItem(false)

        } else {
            binding.bottomNavigation.visibility = View.GONE

        }
    }

    fun showToolbarTransparentBack(
        isHideBackButton: Boolean = false, hideToolBar: Boolean = false
    ) { // binding.drawerLayout[0].findViewById<Toolbar>(R.id.toolbar).visibility = View.VISIBLE
        binding.toolbar.visibility = if (hideToolBar) View.GONE else View.VISIBLE
        binding.toolbar.background =
            ResourcesCompat.getDrawable(resources, R.drawable.background_toolbar_transparent, null)
        binding.toolbarTitle.setTextColor(ResourcesCompat.getColor(resources, R.color.black, null))
        if (isHideBackButton) {
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            supportActionBar?.setDisplayShowHomeEnabled(false)
        } else {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
        }
    }

    private fun setStatusBarGradiant() {
//                val background = ContextCompat.getDrawable(this, drawable)
//        window.setBackgroundDrawable(background)
        val window: Window = window
        window.statusBarColor = ContextCompat.getColor(this, R.color.bg_white)
        if ((resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) window.decorView.systemUiVisibility =
            0
        else window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }

    fun showToolBarTitle(title: String) {
        binding.toolbarTitle.text = title
    }

    fun logoutCardUser(message: String) {
        // showToast("Token Expired : Un-authorization")
        preferenceHelper.clearCardPreference()
        navController?.safeNavigate(R.id.action_global_to_card_login)

    }

    fun logoutCardBetaUser(message: String) {
        // showToast("Token Expired : Un-authorization")
        preferenceHelper.clearCardBetaPreference()
        navController?.safeNavigate(R.id.action_global_to_card_sign_in)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                navController?.popBackStack()
            }
        }
        return super.onOptionsItemSelected(item)

    }

    fun setupObserver() {

        /* lifecycleScope.launch {
             repeatOnLifecycle(Lifecycle.State.RESUMED) {
                 if (!preferenceHelper.isLockModePassword) {
                     setBioMetric(biometricListener)
                 } else {
                     this@BaseActivity.openCustomDeviceLock(object :
                         DeviceLockOpenSuccess {
                         override fun onSuccessOpenDeviceLock() {
                             openAndPairWalletConnection()
                         }

                     })
                 }
             }
         }*/



        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                verifyPhraseViewModel.walletsPrimaryResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            if (it.data != null && it.data.w_mnemonic != "") {
                                Wallet.setWalletObjectFromInstance(it.data)
                                refreshWallet()
                            }
                        }

                        is NetworkState.Loading -> {}
                        is NetworkState.Error -> {}
                        is NetworkState.SessionOut -> {}
                        else -> {}
                    }
                }
            }
        }

        web3WalletViewModel.walletEvents.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach { event ->
                when (event) {
                    is SignEvent.SessionProposal -> {
                        CoroutineScope(Dispatchers.Main).launch {
                            loge("Proposal", "here i am $event")
                            DialogWalletConnectionConfirmation.getInstance()
                                ?.show(this@BaseActivity) { _, _ ->
                                    this@BaseActivity.intent.takeIf { intent -> intent?.action == Intent.ACTION_VIEW && !intent.dataString.isNullOrBlank() }
                                        ?.let { intent ->
                                            web3WalletViewModel.pair(intent.dataString.toString())
                                            intent.data = null
                                        }
                                    connectionsViewModel.refreshConnections()
                                    // val connections = connectionsViewModel.connections
                                }
                        }
                    }

                    is SignEvent.SessionRequest -> {
                        loge("SessionRequest", Gson().toJson(event))
                        val arrayOfArgs = event.arrayOfArgs
                        sessionRequestEventCall(arrayOfArgs)
                    }

                    is SignEvent.Disconnect -> {
                        loge("Disconnect", "disconnected session")
                        connectionsViewModel.refreshConnections()
                    }

                    is AuthEvent.OnRequest -> {
                        loge("AuthEvent", Gson().toJson(event))
                    }

                    else -> Unit
                }
            }.launchIn(lifecycleScope)
    }

    private fun sessionRequestEventCall(arrayOfArgs: ArrayList<String?>) {
        val transactionModelDAPP = parseData(arrayOfArgs)
        val token = Tokens()
        Chains.values().forEach {
            if (it.chainId == transactionModelDAPP.chainId) {
                token.chain?.coinType = it.coinType
                token.t_address = ""
                token.t_name = it.chainName
                token.t_symbol = it.symbol
                token.t_type = it.type
                token.t_price = it.currentPrice
            }
        }
        loge("Params :: > ", "${arrayOfArgs[4]}")


        // handleMessage(arrayOfArgs[4]!!)
        //  handleMessage(Gson().toJson(arrayOfArgs))


        sendTransaction(transactionModelDAPP, token)
    }

    private fun openApprovalWalletConnectionDialog(
        transactionModelDAPP: TransactionModelDApp,
        token: Tokens,
        transactionHash: String?,
        wrapData: TransferNetworkDetail?,
    ) {
        runOnUiThread {
            try {
                DialogWalletApproveTransaction.getInstance()!!.show(
                    this@BaseActivity,
                    transactionModelDAPP,
                    token, wrapData,
                ) { bottomSheetDialog, mdl, isApprove ->
                    if (isApprove) {
                        // sendTransaction(bottomSheetDialog,mdl, token)
                        val txValue =
                            if (transactionModelDAPP.transactionDetails[0].value == "null") "0" else transactionModelDAPP.transactionDetails[0].value
                        val txGasLimit =
                            if (transactionModelDAPP.transactionDetails[0].gas == "null") 0.toBigInteger() else {

                                transactionModelDAPP.transactionDetails[0].gas/* hexStringToBigInteger(
                                     transactionModelDAPP.transactionDetails[0].gas
                                 )*/

                                /*  loge(
                                      "haxGas",
                                      "$hexgas :: wei ${gweiToWei(hexgas)} :: ether : ${
                                          weiToEther(gweiToWei(hexgas))
                                      }"
                                  )

                                  gweiToWei(hexgas)*/


                            }

                        when (transactionModelDAPP.transactionType) {

                            WalletConnectionUtils.WalletConnectionMethod.personalSignIn -> {
                                CoroutineScope(Dispatchers.Main).launch {
                                    try {
                                        SessionRequestViewModel().approve(
                                            transactionModelDAPP, transactionHash!!
                                        ) { uri ->
                                            sendResponseDeepLink(uri)
                                        }
                                        bottomSheetDialog.dismiss()
                                    } catch (e: Throwable) {
                                        e.printStackTrace()
                                        bottomSheetDialog.dismiss()
                                    }
                                }
                            }

                            walletSwitchEthereumChain -> {
                                CoroutineScope(Dispatchers.Main).launch {
                                    try {
                                        SessionRequestViewModel().approve(
                                            transactionModelDAPP, transactionHash!!
                                        ) { uri ->
                                            sendResponseDeepLink(uri)
                                        }
                                        bottomSheetDialog.dismiss()
                                    } catch (e: Throwable) {
                                        e.printStackTrace()
                                        bottomSheetDialog.dismiss()
                                    }
                                }
                            }

                            else -> {

                                val isGettingHase =
                                    transactionModelDAPP.transactionType == WalletConnectionUtils.WalletConnectionMethod.ethSendTransaction
                                CoroutineScope(Dispatchers.Main).launch {
                                    token.callFunction.getTransactionHash(
                                        isGettingTransactionHash = isGettingHase,
                                        toAddress = transactionModelDAPP.transactionDetails[0].to,
                                        gasLimit = "$txGasLimit",
                                        gasPrice = "0",
                                        data = transactionModelDAPP.transactionDetails[0].data,
                                        value = txValue

                                    ) { success, transactionHash, wrapData ->
                                        if (success) {
                                            CoroutineScope(Dispatchers.Main).launch {
                                                try {
                                                    SessionRequestViewModel().approve(
                                                        transactionModelDAPP, transactionHash!!
                                                    ) { uri ->
                                                        sendResponseDeepLink(uri)
                                                    }
                                                    bottomSheetDialog.dismiss()
                                                } catch (e: Throwable) {
                                                    e.printStackTrace()
                                                    bottomSheetDialog.dismiss()
                                                }
                                            }
                                        } else {
                                            loge("BaseActivity", "transactionHash : failed")
                                            bottomSheetDialog.dismiss()
                                            showToast(transactionHash!!)
                                        }
                                    }
                                }

                            }

                        }


                    } else {
                        CoroutineScope(Dispatchers.Main).launch {
                            try {
                                SessionRequestViewModel().reject { uri ->
                                    sendResponseDeepLink(uri)
                                }
                                bottomSheetDialog.dismiss()
                            } catch (e: Throwable) {
                                e.printStackTrace()
                                bottomSheetDialog.dismiss()
                            }
                        }
                    }
                }
            } catch (e: Throwable) {
                // closeAndShowError(navController, e.message)
                e.printStackTrace()
            }
        }
    }

    private fun extractMessageParamFromPersonalSign(input: String): String {
        val jsonArray = JSONArray(input)
        return if (jsonArray.length() > 0) {
            String(Numeric.hexStringToByteArray(jsonArray.getString(0)))
        } else {
            throw IllegalArgumentException()
        }
    }

    private var biometricListener = object : BiometricResult {
        override fun success() {
            PreferenceHelper.getInstance().isBiometricAllow = true
            dismissDeviceLockDialog()


            openAndPairWalletConnection()


            // moveToHome(0)
            //showToast("Success Biometric!!")
        }

        override fun failure(errorCode: Int, errorMessage: String) {
            when (errorCode) {
                BiometricPrompt.ERROR_LOCKOUT -> continueWithoutBiometric(
                    "Maximum number of attempts exceeds! Try again later", useDevicePassword = true
                )

                BiometricPrompt.ERROR_USER_CANCELED, BiometricPrompt.ERROR_NEGATIVE_BUTTON, BiometricPrompt.ERROR_CANCELED -> {/* continueWithoutBiometric(
                         "Unlock with Face ID/ Touch ID or password",
                         true
                     )*/
                }

                else -> continueWithoutBiometric(errorMessage, tryAgain = true)
            }
        }

        override fun successCustomPasscode() {

            if (isFromDeepLink && walletConnection) {
                isFromDeepLink = false
                walletConnection = false
                if (walletPairingUrl != "") {
                    web3WalletViewModel.pair(walletPairingUrl)
                }

            }


        }
    }

    private fun openAndPairWalletConnection() {
        if (isFromDeepLink && walletConnection) {
            isFromDeepLink = false
            walletConnection = false
            if (walletPairingUrl != "") {
                web3WalletViewModel.pair(walletPairingUrl)
            }
        }
    }

    private fun setBioMetric(listener: BiometricResult) {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                this@BaseActivity.openCustomDeviceLock(object : DeviceLockOpenSuccess {
                    override fun onSuccessOpenDeviceLock() {
                        openAndPairWalletConnection()
                    }

                })
                instanceOfBiometricPrompt(listener).authenticate(getPromptInfo())
                //this@BaseActivity.openCustomDeviceLock()
            }

            else -> this@BaseActivity.openCustomDeviceLock(object : DeviceLockOpenSuccess {
                override fun onSuccessOpenDeviceLock() {
                    openAndPairWalletConnection()
                }

            })
        }
    }

    private fun instanceOfBiometricPrompt(listener: BiometricResult): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(this)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                listener.failure(errorCode, errString.toString())
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                PreferenceHelper.getInstance().isBiometricAllow = true
                listener.success()
            }
        }
        return BiometricPrompt(this, executor, callback)
    }

    private fun openDeviceLock() {
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager?
        val i = keyguardManager!!.createConfirmDeviceCredentialIntent(
            "PlutoPe locked", "Use device password to unlock"
        )
        try {
            this@BaseActivity.openCustomDeviceLock(object : DeviceLockOpenSuccess {
                override fun onSuccessOpenDeviceLock() {
                    openAndPairWalletConnection()
                }

            })
        } catch (e: Exception) {
            e.printStackTrace()
            openDialog(
                "Please set device lock screen to set up secure login.", Intent(
                    DevicePolicyManager.ACTION_SET_NEW_PASSWORD
                ), security_setting_request_code
            )
        }
    }

    private fun isDeviceSecure(): Boolean {
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager?
        return keyguardManager!!.isKeyguardSecure
    }

    private fun getPromptInfo(): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder().apply {
            setTitle("Biometric login for PlutoPe")
            setConfirmationRequired(false)
            setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)
            setNegativeButtonText("Cancel")
        }.build()
    }

    private fun openDialog(message: String, intent: Intent, reqCode: Int) {
        var dialog: CustomAlertDialog? = null
        if (dialog == null) dialog = CustomAlertDialog(this)
        dialog.dismiss()
        dialog.setCancelable(false)
        dialog.message = message
        dialog.setPositiveButton("Ok") {
            dialog.dismiss()
            try {
                startActivityForResult(intent, reqCode)
            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
                startActivity(Intent(ACTION_SECURITY_SETTINGS))
            }
        }
        dialog.show()
    }

    var count = 0
    fun continueWithoutBiometric(
        message: String, tryAgain: Boolean = false, useDevicePassword: Boolean = false
    ) {
        count += 1
        PreferenceHelper.getInstance().isBiometricAllow = false
        instanceOfBiometricPrompt(biometricListener).cancelAuthentication()
        bioMetricDialog = if (bioMetricDialog == null) {
            CustomAlertDialog(this)
        } else {
            bioMetricDialog?.dismiss()
            CustomAlertDialog(this)
        }
        bioMetricDialog?.dismiss()
        bioMetricDialog?.title = "PlutoPe Locked"
        bioMetricDialog?.message = message
        bioMetricDialog?.setPositiveButton("Use Pin") {
            bioMetricDialog?.dismiss()
            this@BaseActivity.openCustomDeviceLock(object : DeviceLockOpenSuccess {
                override fun onSuccessOpenDeviceLock() {
                    openAndPairWalletConnection()
                }

            })
        }
        if (tryAgain) bioMetricDialog?.setNegativeButton("Try again") {
            bioMetricDialog?.dismiss()
            if (preferenceHelper.isAppLock) {
                if (!preferenceHelper.isLockModePassword) setBioMetric(biometricListener)
                else openDeviceLock()
            }
        }
        bioMetricDialog?.show()
    }

    private val rcBiometricEnroll = 1001
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            lock_request_code -> if (resultCode == RESULT_OK && isDeviceSecure()) {
                bioMetricDialog?.dismiss()
                Handler(Looper.getMainLooper()).postDelayed(3000) {
                    openDefaultPass = false
                }
            } else {
                if (!isDeviceSecure()) continueWithoutBiometric(
                    "Can not use app without device credentials",
                    true
                )
                else continueWithoutBiometric("Failed to authenticate user.", true)
            }

            security_setting_request_code -> {
                if (resultCode == RESULT_OK && isDeviceSecure()) {
                    openDeviceLock()
                } else {
                    continueWithoutBiometric("Can not use app without device credentials", true)
                }
            }

            rcBiometricEnroll -> {
                instanceOfBiometricPrompt(biometricListener).authenticate(getPromptInfo())
            }

            else -> {}
        }
    }

    fun setDeviceLockDialog(dialog: DeviceLockFullScreenDialog?) {
        deviceLockDialog = dialog
    }

    fun dismissDeviceLockDialog() {
        deviceLockDialog?.dismiss()
    }

    fun openExitDialog() {
        val dialog = CustomAlertDialog(this)
        dialog.title = ""
        dialog.message = getString(R.string.are_you_sure_you_want_to_exit_from_plutope)
        dialog.positiveButtonText = getString(R.string.yes)
        dialog.negativeButtonText = getString(R.string.no)
        dialog.setPositiveButton(getString(R.string.yes)) {
            dialog.dismiss()
            finish()
        }
        dialog.setNegativeButton(getString(R.string.no)) {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun getFCMTokenWithRetry() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                preferenceHelper.firebaseToken = token.toString()
                loge("Token", "FirebaseToken==>" + preferenceHelper.firebaseToken)
                retryCount = 0
            } else {
                val exception = task.exception
                if (exception is ExecutionException && exception.cause is IOException) {
                    if (retryCount < maxRetries) {
                        val delay: Long = 2 * retryCount * 1000L // Increase delay with each retry
                        retryCount++
                        Handler(Looper.getMainLooper()).postDelayed(
                            { getFCMTokenWithRetry() }, delay
                        )
                    } else {
                        loge(
                            "FirebaseMessaging",
                            "Token retrieval failed after $maxRetries attempts $exception"
                        )
                    }
                }

                loge("Token", "FirebaseToken==>" + preferenceHelper.firebaseToken)

            }
        }

        FirebaseInstallations.getInstance().id.addOnSuccessListener {}
    }

    fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when (PackageManager.PERMISSION_GRANTED) {
                ContextCompat.checkSelfPermission(
                    this, android.Manifest.permission.POST_NOTIFICATIONS
                ) -> {
                }

                else -> {
                    requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _: Boolean -> }

    private fun sendTransaction(
        it: TransactionModelDApp, token: Tokens = Tokens()
    ) {
        val txValue =
            if (it.transactionDetails[0].value == "null") "0" else it.transactionDetails[0].value
        val txGasLimit = if (it.transactionDetails[0].gas == "null") 0.toBigInteger() else {
            it.transactionDetails[0].gas
        }
        CoroutineScope(Dispatchers.Main).launch {
            token.callFunction.getTransactionHash(
                isGettingTransactionHash = false,
                toAddress = it.transactionDetails[0].to,
                gasLimit = "$txGasLimit",
                gasPrice = "0",
                data = it.transactionDetails[0].data,
                value = txValue
            ) { success, transactionHash, wrapData ->
                if (success) {
                    loge("BaseActivity", "wrapData : $wrapData")
                    CoroutineScope(Dispatchers.Main).launch {
                        openApprovalWalletConnectionDialog(it, token, transactionHash, wrapData)
                    }
                } else {
                    loge("BaseActivity", "transactionHash : failed")
                    showToast(transactionHash!!)
                }
            }
        }
    }

    private fun openSwapProgressDialog(title: String, subtitle: String) {
        SwapProgressDialog.getInstance().show(this,
            title,
            subtitle,
            listener = object : SwapProgressDialog.DialogOnClickBtnListner {
                override fun onOkClick() {
                    //  findNavController().safeNavigate(PreviewSwapFragmentDirections.actionPreviewSwapFragmentToDashboard())
                }
            })
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        loge("intent", "data" + Gson().toJson(intent.data))
        loge("intent", "scheme" + Gson().toJson(intent.scheme))
        loge("intent", "dataString" + Gson().toJson(intent.dataString))
        loge("intent", "extras" + Gson().toJson(intent.extras))
        loge("intent", "action" + Gson().toJson(intent.action))
        loge("intent", "categories" + Gson().toJson(intent.categories))
        loge("intent", "action" + Gson().toJson(intent.clipData))
        loge("intent", "action" + Gson().toJson(intent.component))
        loge("intent", "action" + Gson().toJson(intent.type))
        intent.handle()

        //  handleDynamicLink(intent)

    }

    private fun Intent.handle() {
        preferenceHelper.deviceId = getAndroidId()
        loge("DeviceIDDDD ::", getAndroidId())
        loge("showDeepLinkOffer", "showDeepLinkOffer: scheme ${this.scheme} :: ${this.dataString}")

        FirebaseDynamicLinks.getInstance().getDynamicLink(this)
            .addOnSuccessListener { pendingDynamicLinkData ->
                val deepLink: Uri? = pendingDynamicLinkData?.link
                if (deepLink != null) {
                    loge("FirebaseDynamicLinks", "Deep Link received: $deepLink")
                    showDeepLinkOffer(action, deepLink)
                    return@addOnSuccessListener
                } else {
                    loge("getPlayStoreReferrer", "getPlayStoreReferrer else:")
                    //getPlayStoreReferrer()
                }
            }

        when (this.scheme) {
            "wc" -> {
                isFromDeepLink = true
                walletConnection = true

                val uri = dataString.toString()
                walletPairingUrl = uri
                loge("showDeepLinkOffer ::", "WC_URL" + uri)
                // web3WalletViewModel.pair(uri)
            }

            "plutope" -> {
                isFromDeepLink = true
                walletConnection = true
                val uri = dataString.toString()
                walletPairingUrl = uri
                loge("showDeepLinkOffer ::", "plutope_URL=>" + uri)
                // web3WalletViewModel.pair(uri)
            }

            "http", "https" -> {
                val appLinkAction: String? = this.action
                val appLinkData: Uri? = this.data
                showDeepLinkOffer(appLinkAction, appLinkData)

            }
        }

    }


    private fun showDeepLinkOffer(appLinkAction: String?, appLinkData: Uri?) {
        loge("TAG", "http  : $appLinkAction  :: $appLinkData")
        try {
            val referralCode: String? = appLinkData?.getQueryParameter("referral")

            preferenceHelper.referralCode =
                if (!referralCode.isNullOrEmpty()) referralCode else preferenceHelper.referralCode

            loge("showDeepLinkOffer", "referralCode:  $referralCode")

            //  showToast("Your referralCode is : $referralCode")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        loge("InternetConnection", "$isConnected")
        if (!isConnected) {
            runOnUiThread {
                binding.layoutNoInternetConnection.visibility = View.VISIBLE
                binding.txtNoInternet.text = getString(R.string.no_internet_connection)
                binding.layoutNoInternetConnection.setBackgroundColor(
                    resources.getColor(
                        R.color.red, null
                    )
                )

                /*  CoroutineScope(Dispatchers.Main).launch {
                      delay(10000)
                      binding.layoutNoInternetConnection.visibility = View.GONE
                  }*/

            }

        } else {

            runOnUiThread {
                val mColors = arrayOf(
                    ColorDrawable(resources.getColor(R.color.green_00A323, null)),
                    ColorDrawable(resources.getColor(R.color.green_4DCC59, null)),
                    ColorDrawable(resources.getColor(R.color.light_green_22d1ee, null))
                )
                val mTransition = TransitionDrawable(mColors)
                mTransition.startTransition(5000)
                binding.txtNoInternet.text = getString(R.string.now_you_are_online)
                binding.layoutNoInternetConnection.background = mTransition
                CoroutineScope(Dispatchers.Main).launch {
                    delay(5000)
                    binding.layoutNoInternetConnection.visibility = View.GONE
                }
            }


        }

    }


}