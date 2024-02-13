package com.app.plutope.ui.fragment.wallet_connection.walletConnectionDetail

import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentWalletConnectionDetailBinding
import com.app.plutope.model.Wallets
import com.app.plutope.networkConfig.Chains
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.ui.fragment.wallet_connection.walletConnectionList.NetworkListAdapter
import com.app.plutope.utils.loge
import com.app.plutope.utils.walletConnection.compose_ui.connections.ConnectionType
import com.app.plutope.utils.walletConnection.compose_ui.connections.ConnectionUI
import com.app.plutope.utils.walletConnection.compose_ui.connections.ConnectionsViewModel
import com.bumptech.glide.Glide
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.walletconnect.web3.wallet.client.Wallet
import com.walletconnect.web3.wallet.client.Web3Wallet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WalletConnectionDetail :
    BaseFragment<FragmentWalletConnectionDetailBinding, WalletConnectionDetailViewModel>() {
    val args: WalletConnectionDetailArgs by navArgs()

    private var connectionUI: ConnectionUI? = null
    private val walletConnectionDetailViewModel: WalletConnectionDetailViewModel by viewModels()
    private val connectionsViewModel: ConnectionsViewModel by viewModels()


    override fun getViewModel(): WalletConnectionDetailViewModel {
        return walletConnectionDetailViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.walletConnectionDetailViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_wallet_connection_detail
    }

    override fun setupToolbarText(): String {
        return getString(R.string.connection_detail)
    }

    override fun setupUI() {
        connectionsViewModel.currentConnectionId = args.connectionId.toInt()
        // val connectionUI by remember { connectionsViewModel.currentConnectionUI }
        CoroutineScope(Dispatchers.Main).launch {
            connectionUI = connectionsViewModel.currentConnectionUI.value

            loge("Detail", "$connectionUI")

            setDetail()

        }


        viewDataBinding!!.btnRemoveConnection.setOnClickListener {

            loge("detail_connectionUI", "$connectionUI")

            if (connectionUI != null) {
                // Implement functionality for top buttons, connection details, and connection type
                // For example:
                when (connectionUI!!.type) {
                    is ConnectionType.Sign -> {

                        val topic = when (val connectionType = connectionUI?.type) {
                            is ConnectionType.Sign -> connectionType.topic
                            else -> null
                        }
                        if (topic != null) {
                            Web3Wallet.disconnectSession(
                                Wallet.Params.SessionDisconnect(topic)
                            ) { error ->
                                Firebase.crashlytics.recordException(error.throwable)
                            }
                            connectionsViewModel.refreshConnections()
                            findNavController().popBackStack()
                        } else {
                            loge(message = "Topic is null")
                        }
                    }
                }
            } else {
                // Something went wrong
                Toast.makeText(requireContext(), "Something went wrong :C", Toast.LENGTH_SHORT)
                    .show()
            }

        }

    }

    private fun setDetail() {
        viewDataBinding!!.layoutConnector.model = connectionUI
        viewDataBinding!!.layoutConnector.imgForword.visibility = View.GONE
        Glide.with(viewDataBinding!!.layoutConnector.imgConnection.context)
            .load(connectionUI?.icon).into(viewDataBinding!!.layoutConnector.imgConnection)
        viewDataBinding!!.executePendingBindings()

        val walletListType = object : TypeToken<MutableList<Wallets>>() {}.type
        val walletList =
            if (preferenceHelper.walletList != "") {
                Gson().fromJson<MutableList<Wallets>>(preferenceHelper.walletList, walletListType)
            } else {
                mutableListOf()
            }


        val adapter = NetworkListAdapter {}
        viewDataBinding!!.rvNetworkList.adapter = adapter

        val chains: List<String>? = when (val connectionType = connectionUI?.type) {
            is ConnectionType.Sign -> connectionType.namespaces["eip155"]?.chains
            else -> null
        }

        val account: List<String>? = when (val connectionType = connectionUI?.type) {
            is ConnectionType.Sign -> connectionType.namespaces["eip155"]?.accounts
            else -> null
        }

        viewDataBinding!!.txtWalletName.text =
            account?.get(0)?.split(":")?.lastOrNull() ?: ""

        val chainList = arrayListOf<Chains>()
        Chains.values().forEach {
            account?.forEach { chain ->
                val indexOfColon = chain.indexOf(":")
                val indexOfSecondColon = chain.indexOf(":", indexOfColon + 1)
                val extracted = chain.substring(0, indexOfSecondColon)

                if (it.chainId == extracted) {
                    it.walletAddress = chain.split(":").lastOrNull() ?: ""
                    chainList.add(it)
                }
            }
        }

        adapter.submitList(chainList.distinct())
        loge("WalletList=>", "$walletList")

    }


    override fun setupObserver() {

    }


}