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
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
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
import androidx.navigation.ui.setupWithNavController
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
import com.app.plutope.ui.fragment.phrase.verify_phrase.VerifySecretPhraseViewModel
import com.app.plutope.ui.fragment.transactions.send.send_coin.TransferNetworkDetail
import com.app.plutope.utils.ConnectivityReceiver
import com.app.plutope.utils.constant.isPausedOnce
import com.app.plutope.utils.constant.pageTypeSwap
import com.app.plutope.utils.extras.BiometricResult
import com.app.plutope.utils.extras.PreferenceHelper
import com.app.plutope.utils.extras.lock_request_code
import com.app.plutope.utils.extras.security_setting_request_code
import com.app.plutope.utils.hexStringToBigInteger
import com.app.plutope.utils.hideLoader
import com.app.plutope.utils.loge
import com.app.plutope.utils.network.NetworkState
import com.app.plutope.utils.openCustomDeviceLock
import com.app.plutope.utils.safeNavigate
import com.app.plutope.utils.showToast
import com.app.plutope.utils.walletConnection.WalletConnectionUtils
import com.app.plutope.utils.walletConnection.WalletConnectionUtils.initialWalletConnection
import com.app.plutope.utils.walletConnection.Web3WalletViewModel
import com.app.plutope.utils.walletConnection.compose_ui.connections.ConnectionsViewModel
import com.app.plutope.utils.walletConnection.sendResponseDeepLink
import com.app.plutope.utils.walletConnection.session_request.SessionRequestViewModel
import com.app.plutope.utils.walletConnection.state.AuthEvent
import com.app.plutope.utils.walletConnection.state.SignEvent
import com.github.amlcurran.showcaseview.ShowcaseView
import com.github.amlcurran.showcaseview.targets.ViewTarget
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
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
    private var value: Bundle? = null
    private var retryCount = 0
    private val maxRetries = 5
    var selectedCurrency: CurrencyModel? = null
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityBaseBinding
    var navController: NavController? = null
    var bioMetricDialog: CustomAlertDialog? = null
    var isShowCenterMenus: Boolean = false
    var openDefaultPass: Boolean = false
    private lateinit var fadeInAnimation: Animation
    private lateinit var fadeIn1: Animation
    private lateinit var fadeIn2: Animation
    private lateinit var fadeIn3: Animation
    private lateinit var fadeIn4: Animation
    private var deviceLockDialog: DeviceLockFullScreenDialog? = null

    private val verifyPhraseViewModel: VerifySecretPhraseViewModel by viewModels()
    private val web3WalletViewModel: Web3WalletViewModel by viewModels()
    private val connectionsViewModel: ConnectionsViewModel by viewModels()

    @Inject
    lateinit var preferenceHelper: PreferenceHelper

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBundle("key", value)
    }

    override fun onRestoreInstanceState(
        savedInstanceState: Bundle?,
        persistentState: PersistableBundle?
    ) {
        super.onRestoreInstanceState(savedInstanceState, persistentState)
        value = savedInstanceState
    }

    override fun onResume() {
        super.onResume()
        if (preferenceHelper.menomonicWallet != "") {
            if (preferenceHelper.appUpdatedFlag != "") {
                if (preferenceHelper.isAppLock) {
                    if (/*navController?.currentDestination?.id != R.id.sendCoin &&*/ navController?.currentDestination?.id != R.id.addCustomToken && navController?.currentDestination?.id != R.id.addContactFragment && navController?.currentDestination?.id != R.id.confirmEncryptionPassword && navController?.currentDestination?.id != R.id.selectWalletBackup && navController?.currentDestination?.id != R.id.walletConnect && navController?.currentDestination?.id != R.id.browser) {
                        if (navController?.currentDestination?.id == R.id.splash) {
                            Handler(Looper.getMainLooper()).postDelayed(1000) {
                                hideLoader()
                                if (!preferenceHelper.isLockModePassword) {
                                    setBioMetric(biometricListener)
                                } else {
                                    this@BaseActivity.openCustomDeviceLock()
                                }
                            }
                        } else {
                            if (!preferenceHelper.isLockModePassword) {
                                setBioMetric(biometricListener)
                            } else {
                                this@BaseActivity.openCustomDeviceLock()
                            }
                        }
                    }
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


    }

    override fun onPause() {
        super.onPause()
        isPausedOnce = true
    }

    override fun onStop() {
        super.onStop()
    }

    fun getAndroidId(): String {
        return Secure.getString(this.contentResolver, Secure.ANDROID_ID)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val saveInstState = if (value != null) value else savedInstanceState
        super.onCreate(saveInstState)
        binding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialWalletConnection(this)
        checkInternetConnection()


        /*   if (preferenceHelper.menomonicWallet != "") {
               if (preferenceHelper.appUpdatedFlag == "") {
                   preferenceHelper.clear()
                   AppDataBase.clearAllTable(this)
               }
           }*/

        setSupportActionBar(binding.toolbar)
        binding.toolbar.title = ""
        navController =
            (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController
        appBarConfiguration = AppBarConfiguration(navController!!.graph)

        NavigationUI.setupActionBarWithNavController(this, navController!!)
        binding.bottomNavigation.setupWithNavController(navController!!)
        binding.bottomNavigation.menu.findItem(R.id.dashboard).isChecked = true

        /* val navGraph = navController!!.graph
         navGraph.setStartDestination(R.id.dashboard)
         navController!!.setGraph(navGraph.id)*/

        // JJ().selectedLanguage = preferenceHelper.currentLanguage!!
        loge("SelectedLang", "::${preferenceHelper.currentLanguage}")

        changeLanguage(preferenceHelper.currentLanguage)

        navController!!.addOnDestinationChangedListener { _, destination, _ ->
            binding.toolbar.title = ""
            when (destination.id) {
                R.id.legal, R.id.yourRecoveryPhrase, R.id.verifySecretPhrase, /*R.id.send, R.id.buy, R.id.receive, R.id.swap,*/ R.id.notification, R.id.providers, R.id.currency/*, R.id.addCustomToken*/, R.id.security, R.id.selectWalletBackup, R.id.sendCoin, R.id.webViewToolbar, R.id.contactListFragment, R.id.addContactFragment, R.id.recoveryWalletFragment, R.id.walletConnect, R.id.addENS, R.id.walletConnectionDetail -> {
                    showToolbarTransparentBack()
                    showBottomNavigation(false)
                }

                R.id.setting -> {
                    showToolbarTransparentBack(true)
                    showBottomNavigation(true)
                }

                R.id.dashboard, R.id.card -> {
                    binding.toolbar.visibility = View.GONE
                    showBottomNavigation(true)
                }

                else -> {
                    binding.toolbar.visibility = View.GONE
                    setStatusBarGradiant(R.drawable.bg_statusbar_white)
                    showBottomNavigation(false)

                }
            }
        }

        fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_up_dialog)
        fadeIn1 = AnimationUtils.loadAnimation(this, R.anim.fade_in_1)
        fadeIn2 = AnimationUtils.loadAnimation(this, R.anim.fade_in_2)
        fadeIn3 = AnimationUtils.loadAnimation(this, R.anim.fade_in_3)
        fadeIn4 = AnimationUtils.loadAnimation(this, R.anim.fade_in_4)

        fadeInAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {}
            override fun onAnimationRepeat(animation: Animation?) {}
        })

        binding.imgCenterMenuOption.startAnimation(fadeInAnimation)

        binding.fabButton.setOnClickListener {
            /* isShowCenterMenus = !isShowCenterMenus
             isShowCenterMenuItem(isShowCenterMenus)*/
            if (navController?.currentDestination?.id == R.id.dashboard) {
                isShowCenterMenus = !isShowCenterMenus
                isShowCenterMenuItem(isShowCenterMenus)
            } else {
                navController!!.safeNavigate(R.id.action_global_to_dashboard)
            }

        }

        binding.fabButton.setOnLongClickListener {
            isShowCenterMenus = false
            isShowCenterMenuItem(false)
            if (navController?.currentDestination?.id != R.id.dashboard) {
                binding.bottomNavigation.menu.findItem(R.id.card).isChecked = false
                binding.bottomNavigation.menu.findItem(R.id.setting).isChecked = false
                navController!!.safeNavigate(R.id.action_global_to_dashboard)
            }
            true
        }

        binding.imgCloseMenuOption.setOnClickListener {
            isShowCenterMenus = false
            isShowCenterMenuItem(false)
            if (navController?.currentDestination?.id != R.id.dashboard) {
                binding.bottomNavigation.menu.findItem(R.id.card).isChecked = false
                binding.bottomNavigation.menu.findItem(R.id.setting).isChecked = false
                navController!!.safeNavigate(R.id.action_global_to_dashboard)
            }
        }

        binding.imgSend.setOnClickListener {
            isShowCenterMenus = false
            binding.imgCloseMenuOption.visibility = View.GONE
            binding.viewBlur.visibility = View.GONE
            navController!!.safeNavigate(R.id.action_global_to_send)
        }

        binding.imgReceive.setOnClickListener {
            isShowCenterMenus = false
            binding.imgCloseMenuOption.visibility = View.GONE
            binding.viewBlur.visibility = View.GONE
            navController!!.safeNavigate(R.id.action_global_to_receive)
        }

        binding.imgAdd.setOnClickListener {
            isShowCenterMenus = false
            binding.imgCloseMenuOption.visibility = View.GONE
            binding.viewBlur.visibility = View.GONE
            navController!!.safeNavigate(R.id.action_global_to_buy)
        }

        binding.imgSwap.setOnClickListener {
            isShowCenterMenus = false
            binding.imgCloseMenuOption.visibility = View.GONE
            binding.viewBlur.visibility = View.GONE

            val result = Bundle()
            result.putString("page_type", pageTypeSwap)
            navController!!.safeNavigate(R.id.action_global_to_buy, result)
        }

        loge("DeviceIDDDD ::", getAndroidId())

        preferenceHelper.deviceId = getAndroidId()

        setupObserver()
        setWalletObject()
        setCurrency()
        getFCMTokenWithRetry()

        /*Firebase.dynamicLinks.dynamicLink {
            loge("Link","${this.link}")
        }
  */
        intent?.handle()
    }

    private fun checkInternetConnection() {
        val connectivityReceiver = ConnectivityReceiver()
        connectivityReceiver.setConnectivityReceiverListener(this)
        registerReceiver(
            connectivityReceiver,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
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
            binding.bottomNavigationCard.visibility = View.GONE
            binding.bottomNavigation.visibility = View.GONE
            binding.fabButton.visibility = View.VISIBLE
            binding.imgCenterMenuOption.visibility = View.VISIBLE
            binding.imgAdd.visibility = View.VISIBLE
            binding.imgReceive.visibility = View.VISIBLE
            binding.imgSend.visibility = View.VISIBLE
            binding.imgSwap.visibility = View.VISIBLE
            binding.viewBlur.visibility = View.VISIBLE
            binding.imgCloseMenuOption.visibility = View.VISIBLE
            binding.txtSend.visibility = View.VISIBLE
            binding.txtReceive.visibility = View.VISIBLE
            binding.txtAdd.visibility = View.VISIBLE
            binding.txtSwap.visibility = View.VISIBLE
            binding.txtSend.setText(R.string.send)
            binding.txtReceive.setText(R.string.receive)
            binding.txtAdd.setText(R.string.buy)
            binding.txtSwap.setText(R.string.swap)

        } else {
            binding.bottomNavigationCard.visibility = View.VISIBLE
            binding.bottomNavigation.visibility = View.VISIBLE
            binding.fabButton.visibility = View.VISIBLE
            binding.imgCenterMenuOption.visibility = View.GONE
            binding.imgAdd.visibility = View.GONE
            binding.imgReceive.visibility = View.GONE
            binding.imgSend.visibility = View.GONE
            binding.imgSwap.visibility = View.GONE
            binding.viewBlur.visibility = View.GONE
            binding.imgCloseMenuOption.visibility = View.GONE
            binding.txtSend.visibility = View.GONE
            binding.txtReceive.visibility = View.GONE
            binding.txtAdd.visibility = View.GONE
            binding.txtSwap.visibility = View.GONE
        }
    }
    private fun showBottomNavigation(isShow: Boolean = false) {
        if (isShow) {
            val button = Button(this)
            button.text = ""
            button.isEnabled = false
            button.visibility = View.GONE
            val viewTarget = ViewTarget(binding.fabButton)
            ShowcaseView.Builder(this)
                .setTarget(viewTarget)
                .setContentTitle("Home")
                .hideOnTouchOutside()
                .replaceEndButton(button)
                .setStyle(R.style.CustomShowcaseTheme)
                .setContentText("Single tap to open menu & Hold and press to go dashboard")
                .singleShot(44)
                .build()

            binding.bottomNavigationCard.visibility = View.VISIBLE
            binding.bottomNavigation.visibility = View.VISIBLE
            binding.fabButton.visibility = View.VISIBLE
            isShowCenterMenus = false
            isShowCenterMenuItem(false)

        } else {
            binding.imgCloseMenuOption.visibility = View.GONE
            binding.bottomNavigation.visibility = View.GONE
            binding.fabButton.visibility = View.GONE
            binding.bottomNavigationCard.visibility = View.GONE
            binding.imgCenterMenuOption.visibility = View.GONE
            binding.imgAdd.visibility = View.GONE
            binding.imgReceive.visibility = View.GONE
            binding.imgSend.visibility = View.GONE
            binding.imgSwap.visibility = View.GONE
        }
    }

    fun showToolbarTransparentBack(isHideBackButton: Boolean = false) { // binding.drawerLayout[0].findViewById<Toolbar>(R.id.toolbar).visibility = View.VISIBLE
        binding.toolbar.visibility = View.VISIBLE
        binding.toolbar.background =
            ResourcesCompat.getDrawable(resources, R.drawable.background_toolbar_transparent, null)
        binding.toolbarTitle.setTextColor(ResourcesCompat.getColor(resources, R.color.white, null))
        if (isHideBackButton) {
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            supportActionBar?.setDisplayShowHomeEnabled(false)
        } else {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
        }
    }
    private fun setStatusBarGradiant(drawable: Int) {
        val window: Window = window
        val background = ContextCompat.getDrawable(this, drawable)
        window.statusBarColor = ContextCompat.getColor(this, android.R.color.transparent)
        window.setBackgroundDrawable(background)
    }

    fun showToolBarTitle(title: String) {
        binding.toolbarTitle.text = title
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
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                verifyPhraseViewModel.walletsPrimaryResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            if (it.data != null) {
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

        web3WalletViewModel.walletEvents
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
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
                        // val arrayOfArgs = event.arrayOfArgs
                        // navController!!.navigate(Route.AuthRequest.path)
                       // DialogRequestSignApproval.getInstance()?.show(this@BaseActivity) { _, _ -> }
                    }
                    else -> Unit
                }
            }
            .launchIn(lifecycleScope)
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
                            if (transactionModelDAPP.transactionDetails[0].gas == "null") 0.toBigInteger() else hexStringToBigInteger(
                                transactionModelDAPP.transactionDetails[0].gas
                            )

                        when (transactionModelDAPP.transactionType) {

                            WalletConnectionUtils.WalletConnectionMethod.personalSignIn -> {
                                CoroutineScope(Dispatchers.Main).launch {
                                    try {
                                        SessionRequestViewModel().approve(
                                            transactionModelDAPP,
                                            transactionHash!!
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
                            /*
                                                        WalletConnectionUtils.WalletConnectionMethod.ethSendTransaction -> {
                                                            CoroutineScope(Dispatchers.Main).launch {
                                                                token.callFunction.getTransactionHash(
                                                                    isGettingTransactionHash = true,
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
                                                                                    transactionModelDAPP,
                                                                                    transactionHash!!
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
                            */
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
                                                        transactionModelDAPP,
                                                        transactionHash!!
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
                                loge("jannat", "dsfhg")
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
            // moveToHome(0)
            //showToast("Success Biometric!!")
        }
        override fun failure(errorCode: Int, errorMessage: String) {
            when (errorCode) {
                BiometricPrompt.ERROR_LOCKOUT -> continueWithoutBiometric(
                    "Maximum number of attempts exceeds! Try again later",
                    useDevicePassword = true
                )

                BiometricPrompt.ERROR_USER_CANCELED, BiometricPrompt.ERROR_NEGATIVE_BUTTON, BiometricPrompt.ERROR_CANCELED -> {
                    /* continueWithoutBiometric(
                         "Unlock with Face ID/ Touch ID or password",
                         true
                     )*/
                }

                else -> continueWithoutBiometric(errorMessage, tryAgain = true)
            }
        }

        override fun successCustomPasscode() {}
    }
    private fun setBioMetric(listener: BiometricResult) {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                this@BaseActivity.openCustomDeviceLock()
                instanceOfBiometricPrompt(listener).authenticate(getPromptInfo())
                //this@BaseActivity.openCustomDeviceLock()
            }
            else -> /*openDeviceLock()*/ this@BaseActivity.openCustomDeviceLock()
        }
    }

    private fun instanceOfBiometricPrompt(listener: BiometricResult): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(this)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                listener.failure(errorCode, errString.toString())
            }
            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                //   "Authentication failed for an unknown reason".showToast(this@SplashActivity2)
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
        val keyguardManager =
            getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager?
        val i = keyguardManager!!.createConfirmDeviceCredentialIntent(
            "PlutoPe locked",
            "Use device password to unlock"
        )
        try {
            this.openCustomDeviceLock()
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
        val keyguardManager =
            getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager?
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
        message: String,
        tryAgain: Boolean = false,
        useDevicePassword: Boolean = false
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
            this@BaseActivity.openCustomDeviceLock()
        }
        if (tryAgain)
            bioMetricDialog?.setNegativeButton("Try again") {
                bioMetricDialog?.dismiss()
                //instanceOfBiometricPrompt(biometricListener).authenticate(getPromptInfo())
                if (preferenceHelper.isAppLock) {
                    if (!preferenceHelper.isLockModePassword)
                        setBioMetric(biometricListener)
                    else
                        openDeviceLock()
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
                if (!isDeviceSecure())
                    continueWithoutBiometric("Can not use app without device credentials", true)
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
        dialog.message = "Are you sure you want to exit from PlutoPe?"
        dialog.positiveButtonText = "Yes"
        dialog.negativeButtonText = "No"
        dialog.setPositiveButton("Yes") {
            dialog.dismiss()
            finish()
        }
        dialog.setNegativeButton("No") {
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
                            { getFCMTokenWithRetry() },
                            delay
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

        FirebaseInstallations.getInstance().id.addOnSuccessListener {
            // preferenceHelper.deviceId = it.toString()

            // loge("DeviceID","deviceID => ${preferenceHelper.deviceId}")
        }
    }

    fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        when {
            isGranted -> {
                // FCM SDK (and your app) can post notifications.
            }
            else -> {
                //not granted permission have you try with rational permission
            }
        }
    }

    private fun sendTransaction(
        it: TransactionModelDApp,
        token: Tokens = Tokens()
    ) {
        val txValue =
            if (it.transactionDetails[0].value == "null") "0" else it.transactionDetails[0].value
        val txGasLimit =
            if (it.transactionDetails[0].gas == "null") 0.toBigInteger() else hexStringToBigInteger(
                it.transactionDetails[0].gas
            )


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
        SwapProgressDialog.getInstance().show(
            this,
            title,
            subtitle,
            listener = object : SwapProgressDialog.DialogOnClickBtnListner {
                override fun onOkClick() {
                    //  findNavController().safeNavigate(PreviewSwapFragmentDirections.actionPreviewSwapFragmentToDashboard())
                }
            })
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        loge("intent", "data" + Gson().toJson(intent?.data))
        loge("intent", "scheme" + Gson().toJson(intent?.scheme))
        loge("intent", "dataString" + Gson().toJson(intent?.dataString))
        loge("intent", "extras" + Gson().toJson(intent?.extras))
        loge("intent", "action" + Gson().toJson(intent?.action))
        loge("intent", "categories" + Gson().toJson(intent?.categories))
        loge("intent", "action" + Gson().toJson(intent?.clipData))
        loge("intent", "action" + Gson().toJson(intent?.component))
        loge("intent", "action" + Gson().toJson(intent?.type))
        intent?.handle()

        //  handleDynamicLink(intent)

    }

    private fun Intent.handle() {
        // val data = intent.data

        preferenceHelper.deviceId = getAndroidId()

        loge("DeviceIDDDD ::", getAndroidId())
        loge("TAG", "showDeepLinkOffer: ${this.dataString}")

        when (this.scheme) {
            "wc" -> {
                val uri = dataString.toString()
                web3WalletViewModel.pair(uri)
            }

            "http", "https" -> {
                val appLinkAction: String? = this.action
                val appLinkData: Uri? = this.data

                showDeepLinkOffer(appLinkAction, appLinkData)

            }
        }

    }

    /*
        private fun Intent.handle() {

            loge("intent", "data" + Gson().toJson(this.data))
            loge("intent", "scheme" + Gson().toJson(this.scheme))
            loge("intent", "dataString" + Gson().toJson(this.dataString))
            loge("intent", "extras" + Gson().toJson(this.extras))
            loge("intent", "action" + Gson().toJson(this.action))
            loge("intent", "categories" + Gson().toJson(this.categories))
            loge("intent", "clipData" + Gson().toJson(this.clipData))
            loge("intent", "component" + Gson().toJson(this.component))
            loge("intent", "type" + Gson().toJson(this.type))
            loge("intent", "referral" + this.extras)



            Firebase.dynamicLinks.getDynamicLink(this)
                .addOnSuccessListener(this@BaseActivity) { pendingDynamicLinkData ->

                 // Get deep link from result (may be null if no link is found)
                    var deepLink: Uri? = null
                    if (pendingDynamicLinkData != null) {
                        loge("TAG", "Referral code from dynamic link: ${Gson().toJson(pendingDynamicLinkData.utmParameters)}")

                        deepLink = pendingDynamicLinkData.link
                    }

                    loge("TAG", "deepLink: ${deepLink}")

                    if (deepLink != null) {
                        val referralCode = deepLink.getQueryParameter("referral")
                        loge("TAG", "Referral code from dynamic link: $referralCode")
                        showToast("Your referral code is: $referralCode")
                        // Process the referral code as needed
                    }
                }
                .addOnFailureListener(this@BaseActivity) { e ->
                    loge("TAG", "getDynamicLink:onFailure $e")
                }
        }
    */
    private fun showDeepLinkOffer(appLinkAction: String?, appLinkData: Uri?) {
        loge("TAG", "http  : $appLinkAction  :: $appLinkData")
        if (Intent.ACTION_VIEW == appLinkAction && appLinkData != null) {
            val referralCode: String? = appLinkData.getQueryParameter("referral")

            preferenceHelper.referralCode = referralCode!!

            loge("showDeepLinkOffer", "referralCode:  $referralCode")

            //  showToast("Your referralCode is : $referralCode")

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
                        R.color.red,
                        null
                    )
                )
            }

        } else {

            runOnUiThread {
                val mColors = arrayOf(
                    ColorDrawable(resources.getColor(R.color.green_099817, null)),
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