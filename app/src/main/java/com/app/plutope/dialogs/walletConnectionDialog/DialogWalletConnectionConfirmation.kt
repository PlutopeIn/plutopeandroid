package com.app.plutope.dialogs.walletConnectionDialog

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.core.net.toUri
import com.app.plutope.R
import com.app.plutope.databinding.DialogWalletConnectionConfirmationBinding
import com.app.plutope.model.TransactionModelDApp
import com.app.plutope.networkConfig.Chains
import com.app.plutope.ui.fragment.wallet_connection.walletConnectionList.NetworkListAdapter
import com.app.plutope.ui.fragment.wallet_connection.walletConnectionList.Permission
import com.app.plutope.ui.fragment.wallet_connection.walletConnectionList.PermissionListAdapter
import com.app.plutope.utils.loge
import com.app.plutope.utils.showToast
import com.app.plutope.utils.walletConnection.compose_ui.peer.Validation
import com.app.plutope.utils.walletConnection.compose_ui.peer.getDescriptionContent
import com.app.plutope.utils.walletConnection.compose_ui.peer.getDescriptionTitle
import com.app.plutope.utils.walletConnection.compose_ui.peer.getValidationIcon
import com.app.plutope.utils.walletConnection.compose_ui.session_proposal.SessionProposalUI
import com.app.plutope.utils.walletConnection.compose_ui.session_proposal.SessionProposalViewModel
import com.app.plutope.utils.walletConnection.sendResponseDeepLink
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class DialogWalletConnectionConfirmation private constructor() {

    private var binding: DialogWalletConnectionConfirmationBinding? = null

    companion object {
        private var singleInstence: DialogWalletConnectionConfirmation? = null
        fun getInstance(): DialogWalletConnectionConfirmation? {
            if (singleInstence == null) {
                singleInstence = DialogWalletConnectionConfirmation()
            }
            return singleInstence
        }

        fun clearDialogWalletConnectionConfirmationInstence() {
            singleInstence = null
        }
    }

    private var walletConnectionDialog: BottomSheetDialog? = null
    fun show(
        context: Context?,
        unit: (TransactionModelDApp, Boolean) -> Any
    ) {

        if (walletConnectionDialog == null) {
            walletConnectionDialog = BottomSheetDialog(
                context!!,
                android.R.style.Theme_DeviceDefault_Dialog_NoActionBar_MinWidth
            )
        }
        walletConnectionDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(walletConnectionDialog?.window?.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.BOTTOM
        lp.windowAnimations = R.style.DialogAnimation
        walletConnectionDialog?.window?.attributes = lp
        walletConnectionDialog?.behavior?.peekHeight = 2500
        walletConnectionDialog?.setCancelable(false)

        binding = DialogWalletConnectionConfirmationBinding.inflate(LayoutInflater.from(context))
        walletConnectionDialog?.setContentView(binding!!.root)
        val sessionProposalViewModel = SessionProposalViewModel()
        val adapter = NetworkListAdapter {}
        binding!!.rvNetworkList.adapter = adapter

        scammerScreenView(sessionProposalViewModel)

        val requiredChains = getRequiredChains(sessionProposalViewModel.sessionProposal!!)
        val optionalChains = getOptionalChains(sessionProposalViewModel.sessionProposal)
        val allChain = requiredChains + optionalChains

        val chainList = arrayListOf<Chains>()
        Chains.values().forEach {
            allChain.forEach { chain ->
                if (it.chainId == chain) {
                    chainList.add(it)
                }
            }
        }


        adapter.submitList(chainList.distinct())
        binding!!.apply {
            imgBack.setOnClickListener {
                walletConnectionDialog?.dismiss()
                clearDialogWalletConnectionConfirmationInstence()
            }

            Glide.with(imgConnection.context)
                .load(sessionProposalViewModel.sessionProposal.peerUI.peerIcon).into(imgConnection)
            txtConnectorName.text =
                context?.getString(
                    R.string.wants_to_connect_to_your_wallet,
                    sessionProposalViewModel.sessionProposal.peerUI.peerName
                )
            txtConnectionUrl.apply {
                text = Html.fromHtml(
                    sessionProposalViewModel.sessionProposal.peerUI.peerUri,
                    Html.FROM_HTML_MODE_LEGACY
                )
                movementMethod = LinkMovementMethod.getInstance();
            }

            btnConnect.setOnClickListener {
                CoroutineScope(Dispatchers.Main).launch {
                    try {

                        sessionProposalViewModel.approve(sessionProposalViewModel.sessionProposal.pubKey) { redirect ->
                            if (redirect.isNotEmpty()) {
                                context?.sendResponseDeepLink(redirect.toUri())
                            }
                            dismiss()
                            clearDialogWalletConnectionConfirmationInstence()
                        }
                    } catch (e: Throwable) {
                        loge("message=>", "${e.message}")
                        context?.showToast(e.message!!)
                        dismiss()
                        clearDialogWalletConnectionConfirmationInstence()
                    }
                }
            }

            btnCancel.setOnClickListener {
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        sessionProposalViewModel.reject(sessionProposalViewModel.sessionProposal.pubKey) { redirect ->
                            if (redirect.isNotEmpty()) {
                                context?.sendResponseDeepLink(redirect.toUri())
                            }
                        }
                        dismiss()
                        clearDialogWalletConnectionConfirmationInstence()
                    } catch (e: Throwable) {
                        context?.showToast(e.message!!)
                        dismiss()
                        clearDialogWalletConnectionConfirmationInstence()
                    }
                }
            }
        }

        if (walletConnectionDialog != null) {
            if (walletConnectionDialog!!.isShowing) {
                walletConnectionDialog?.dismiss()
                clearDialogWalletConnectionConfirmationInstence()
            } else {
                try {
                    walletConnectionDialog?.show()
                } catch (e: WindowManager.BadTokenException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun scammerScreenView(sessionProposalViewModel: SessionProposalViewModel) {

        val permissionListAdapter = PermissionListAdapter {}
        binding!!.rvPermissionList.adapter = permissionListAdapter

        if (sessionProposalViewModel.sessionProposal?.peerContext?.isScam == true) {
            binding!!.layoutScamUrl.visibility = View.VISIBLE
            binding!!.txtWebsiteHostText.text =
                Uri.parse(sessionProposalViewModel.sessionProposal.peerContext.origin).host
        } else {
            binding!!.layoutScamUrl.visibility = View.GONE
        }


        if (sessionProposalViewModel.sessionProposal?.peerContext?.validation != Validation.VALID) {
            binding!!.layoutPermission.visibility = View.VISIBLE
            binding!!.layoutPermissionList.visibility = View.INVISIBLE

            val icon = sessionProposalViewModel.sessionProposal?.peerContext?.isScam?.let {
                if (it) R.drawable.ic_scam else getValidationIcon(
                    sessionProposalViewModel.sessionProposal.peerContext.validation
                )
            }
                ?: getValidationIcon(sessionProposalViewModel.sessionProposal?.peerContext!!.validation)

            binding!!.imgValidationIcon.setImageResource(icon)
            binding!!.txtDescriptionTitle.text =
                getDescriptionTitle(sessionProposalViewModel.sessionProposal?.peerContext!!)
            binding!!.txtDescriptionContent.text =
                getDescriptionContent(sessionProposalViewModel.sessionProposal.peerContext)
        } else {
            binding!!.layoutPermission.visibility = View.GONE
            binding!!.layoutPermissionList.visibility = View.VISIBLE
            val permissions = arrayListOf(
                Permission("View your balance and activity"),
                Permission("Send approval requests"),
                Permission(
                    "Move funds without permissions",
                    icon = R.drawable.ic_close,
                    color = Color.parseColor("#44456E")
                )
            )
            permissionListAdapter.submitList(permissions)
        }
    }

    fun dismiss() {
        if (walletConnectionDialog != null) {
            walletConnectionDialog?.dismiss()
            clearDialogWalletConnectionConfirmationInstence()
        }
    }

    private fun getOptionalChains(sessionProposalUI: SessionProposalUI) =
        sessionProposalUI.optionalNamespaces.flatMap { (namespaceKey, proposal) ->
            proposal.chains ?: listOf(namespaceKey)
        }

    private fun getRequiredChains(sessionProposalUI: SessionProposalUI) =
        sessionProposalUI.namespaces.flatMap { (namespaceKey, proposal) ->
            proposal.chains ?: listOf(namespaceKey)
        }


}