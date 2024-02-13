package com.app.plutope.ui.fragment.wallet_connection.auth_request

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import com.app.plutope.R
import com.app.plutope.databinding.DialogAuthRequestSignApprovalBinding
import com.app.plutope.model.Tokens
import com.app.plutope.model.TransactionModelDApp
import com.app.plutope.ui.fragment.transactions.send.send_coin.TransferNetworkDetail
import com.app.plutope.utils.showToast
import com.app.plutope.utils.walletConnection.compose_ui.session_proposal.SessionProposalUI
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class DialogRequestSignApproval private constructor() {

    private var binding: DialogAuthRequestSignApprovalBinding? = null

    companion object {
        private var singleInstence: DialogRequestSignApproval? = null
        fun getInstance(): DialogRequestSignApproval? {
            if (singleInstence == null) {
                singleInstence = DialogRequestSignApproval()
            }
            return singleInstence
        }

        fun clearDialogRequestSignApprovalInstence() {
            singleInstence = null
        }
    }

    private var walletConnectionDialog: BottomSheetDialog? = null
    fun show(
        context: Context?,
        transactionModelDAPP: TransactionModelDApp,
        token: Tokens,
        transactionHash: String?,
        wrapData: TransferNetworkDetail?,
        unit: (AuthRequestUI, Boolean) -> Any
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
        binding = DialogAuthRequestSignApprovalBinding.inflate(LayoutInflater.from(context))
        walletConnectionDialog?.setContentView(binding!!.root)

        val authRequestViewModel = AuthRequestViewModel()

        binding!!.btnConnect.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    authRequestViewModel.approve()
                } catch (e: Throwable) {
                    context?.showToast(e.message.toString())
                }
            }

        }

        binding!!.btnCancel.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    authRequestViewModel.reject()
                } catch (e: Throwable) {
                    context?.showToast(e.message.toString())
                }
                dismiss()
            }

        }




        if (walletConnectionDialog != null) {
            if (walletConnectionDialog!!.isShowing) {
                walletConnectionDialog?.dismiss()
                clearDialogRequestSignApprovalInstence()
            } else {
                try {
                    walletConnectionDialog?.show()
                } catch (e: WindowManager.BadTokenException) {
                    e.printStackTrace()
                }
            }
        }
    }


    fun dismiss() {
        if (walletConnectionDialog != null) {
            walletConnectionDialog?.dismiss()
            clearDialogRequestSignApprovalInstence()
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