package com.app.plutope.dialogs


import android.app.Dialog
import android.content.Context
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.WindowManager
import com.app.plutope.R
import com.app.plutope.databinding.DialogNftDetailBinding
import com.bumptech.glide.Glide


class NFTDetailDialog private constructor() {
    lateinit var binding: DialogNftDetailBinding

    companion object {
        var singleInstance: NFTDetailDialog? = null

        fun getInstance(): NFTDetailDialog {
            if (singleInstance == null) {
                singleInstance = NFTDetailDialog()
            }
            return singleInstance!!
        }
    }

    private var dialogNft: Dialog? = null

    fun show(
        context: Context,
        title:String,
        description:String,
        imageUrl:String
    ) {

        if (dialogNft == null) {
            dialogNft =
                Dialog(context, android.R.style.Theme_DeviceDefault_Dialog_NoActionBar_MinWidth)
        }


        binding = DialogNftDetailBinding.inflate(LayoutInflater.from(context))

        dialogNft!!.setContentView(binding.root)

        val layoutParams = dialogNft?.window!!.attributes
        dialogNft?.window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        dialogNft?.window?.setBackgroundDrawableResource(R.color.transparent)
        dialogNft?.window!!.attributes = layoutParams
        dialogNft?.setCancelable(false)

        binding.txtTitle.text = title
        binding.txtDesc.apply {
            text = Html.fromHtml(description, Html.FROM_HTML_MODE_LEGACY)
            movementMethod = LinkMovementMethod.getInstance()
        }
        Glide.with(context).load(imageUrl).into(binding.imgPhraseSecretDialog)


        binding.imgClose.setOnClickListener {
            dialogNft?.dismiss()
        }

        if (!dialogNft!!.isShowing) {
            try {
                dialogNft?.show()
            } catch (e: WindowManager.BadTokenException) {
                e.printStackTrace()
            }
        } else {
            dialogNft?.dismiss()
        }

        binding.executePendingBindings()
    }

    fun dismiss() {
        if (dialogNft!!.isShowing) {
            dialogNft?.dismiss()
        }
    }
}