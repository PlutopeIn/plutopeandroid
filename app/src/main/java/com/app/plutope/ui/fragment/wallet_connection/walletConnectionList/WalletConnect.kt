package com.app.plutope.ui.fragment.wallet_connection.walletConnectionList

import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentWalletConnectBinding
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.utils.hideLoader
import com.app.plutope.utils.loge
import com.app.plutope.utils.safeNavigate
import com.app.plutope.utils.showLoaderAnyHow
import com.app.plutope.utils.walletConnection.Web3WalletViewModel
import com.app.plutope.utils.walletConnection.compose_ui.connections.ConnectionsViewModel
import com.walletconnect.sample.wallet.ui.state.PairingState
import dagger.hilt.android.AndroidEntryPoint
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanQRCode
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WalletConnect : BaseFragment<FragmentWalletConnectBinding, WalletConnectViewModel>() {

    private var adapter: WalletConnectionListAdapter? = null
    private val walletConnectViewModel: WalletConnectViewModel by viewModels()
    private val web3walletViewModel: Web3WalletViewModel by viewModels()
    private val connectionsViewModel: ConnectionsViewModel by viewModels()


    override fun getViewModel(): WalletConnectViewModel {
        return walletConnectViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.walletConnectViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_wallet_connect
    }

    override fun setupToolbarText(): String {
        return getString(R.string.wallet_connect)
    }

    override fun setupUI() {
        // connectionsViewModel.refreshConnections()

        adapter = WalletConnectionListAdapter {
            findNavController().safeNavigate(
                WalletConnectDirections.actionWalletConnectionToWalletConnectionDetail(
                    it.id.toString()
                )
            )
        }
        viewDataBinding!!.rvConnectionList.adapter = adapter
        viewDataBinding!!.btnAddConnection.setOnClickListener {
            scanQrCode.launch(null)
        }

    }

    override fun setupObserver() {

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                connectionsViewModel.refreshConnections()
                connectionsViewModel.connections.collect {
                    loge("connectionList =>", "$it")
                    adapter?.submitList(it)
                }

            }
        }

        // Observe pairingStateSharedFlow in the fragment
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                web3walletViewModel.pairingStateSharedFlow.collect { pairingState ->
                    when (pairingState) {
                        is PairingState.Loading -> {
                            requireContext().showLoaderAnyHow()

                        }

                        is PairingState.Success -> {
                            hideLoader()

                        }

                        is PairingState.Error -> {
                            hideLoader()

                        }

                        else -> {

                        }
                    }
                }
            }
        }

    }

    private val scanQrCode = registerForActivityResult(ScanQRCode()) { result: QRResult ->
        when (result) {
            is QRResult.QRSuccess -> {
                handleQRCodeResult(result.content.rawValue)
            }

            QRResult.QRUserCanceled -> Toast.makeText(
                requireContext(),
                "Cancelled",
                Toast.LENGTH_LONG
            ).show()

            QRResult.QRMissingPermission -> Toast.makeText(
                requireContext(),
                "Missing permission",
                Toast.LENGTH_LONG
            ).show()

            is QRResult.QRError -> "${result.exception.javaClass.simpleName}: ${result.exception.localizedMessage}"
        }
    }

    private fun handleQRCodeResult(result: String) {
        loge("Wallet connect :", result)

        web3walletViewModel.pair(result)
    }


    /*
        private fun generateSessionProposalUI(): SessionProposalUI? {

            loge("TAG", "generateSessionProposalUI: $sessionProposalEvent")

            return if (sessionProposalEvent != null) {
                val (proposal, context) = sessionProposalEvent!!
                SessionProposalUI(
                    peerUI = PeerUI(
                        peerIcon = proposal.icons.firstOrNull().toString(),
                        peerName = proposal.name,
                        peerDescription = proposal.description,
                        peerUri = proposal.url,
                    ),
                    namespaces = proposal.requiredNamespaces,
                    optionalNamespaces = proposal.optionalNamespaces,
                    peerContext = context.toPeerUI(),
                    redirect = proposal.redirect,
                    pubKey = proposal.proposerPublicKey
                )
            } else null
        }
    */


}