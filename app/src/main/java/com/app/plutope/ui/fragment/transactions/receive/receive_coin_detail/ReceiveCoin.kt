package com.app.plutope.ui.fragment.transactions.receive.receive_coin_detail


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.RectF
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.content.FileProvider
import androidx.core.view.setMargins
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentReceiveCoinBinding
import com.app.plutope.dialogs.DialogSelectButtonList
import com.app.plutope.dialogs.RequestPaymentDialog
import com.app.plutope.model.ButtonModel
import com.app.plutope.model.Wallet
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.utils.coinTypeEnum.CoinType
import com.app.plutope.utils.constant.buttonMetaMask
import com.app.plutope.utils.constant.buttonPlutoPe
import com.app.plutope.utils.constant.buttonTrustWallet
import com.app.plutope.utils.constant.isFromReceived
import com.app.plutope.utils.hideLoader
import com.app.plutope.utils.showToast
import com.github.sumimakito.awesomeqr.AwesomeQrRenderer
import com.github.sumimakito.awesomeqr.option.RenderOption
import com.github.sumimakito.awesomeqr.option.color.Color
import com.github.sumimakito.awesomeqr.option.logo.Logo
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar


@AndroidEntryPoint
class ReceiveCoin : BaseFragment<FragmentReceiveCoinBinding, ReceiveCoinViewModel>() {

    private var qrCode: Bitmap? = null
    val args: ReceiveCoinArgs by navArgs()
    private val keyAddress = "key_address"
    private var addressWallet: String? = ""
    private var qrCodeUrl = ""
    private var qrCodeGenerationJob: Job? = null
    private val receiveCoinViewModel: ReceiveCoinViewModel by viewModels()
    override fun getViewModel(): ReceiveCoinViewModel {
        return receiveCoinViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.receiveCoinViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_receive_coin
    }

    override fun setupToolbarText(): String {
        return getString(R.string.receiving_coin, args.tokenModel.t_symbol)
    }


    override fun setupUI() {
        hideLoader()
        isFromReceived = true
        viewDataBinding?.txtToolbarTitle?.text =
            getString(R.string.receive_coin, args.tokenModel.t_symbol)


        addressWallet =
            Wallet.getPublicWalletAddress(args.tokenModel.chain?.coinType ?: CoinType.ETHEREUM)
        qrCodeUrl = addressWallet!!

        viewDataBinding!!.imgBack.setOnClickListener {
            findNavController().navigateUp()
        }

        viewDataBinding?.txtWalletKey?.text = addressWallet
        viewDataBinding!!.imgCopy.setOnClickListener {
            onClickCopy()
        }

        viewDataBinding!!.imgShare.setOnClickListener {
            shareQRCode(qrCode)
        }

        viewDataBinding!!.imgDoller.setOnClickListener {
            // openAlertDialog()

            RequestPaymentDialog.getInstance().show(
                requireContext(),
                args.tokenModel,
                object : RequestPaymentDialog.DialogOnClickBtnListner {
                    @SuppressLint("SetTextI18n")
                    override fun onSubmitClicked(amount: String) {

                        // openOptionPlatform(amount)

                        val calculateTotal =
                            amount.toDouble() * args.tokenModel.t_price!!.toDouble()
                        val formattedValue = String.format("%.2f", calculateTotal)

                        viewDataBinding!!.txtInstruction.text =
                            amount + "  " + args.tokenModel.t_symbol + " ≈ " + "${preferenceHelper.getSelectedCurrency()?.symbol}$formattedValue"

                        qrCodeUrl = addressWallet.toString() + "?amount=$amount"
                        renderQRCode()

                    }
                })

        }

        renderQRCode()
    }

    private fun renderQRCode() {
        // Cancel any ongoing QR code generation if it exists
        qrCodeGenerationJob?.cancel()

        // Create a new coroutine for QR code generation
        qrCodeGenerationJob = CoroutineScope(Dispatchers.Default).launch {
            val logoBitmap =
                BitmapFactory.decodeResource(resources, R.drawable.img_logo_circle_black)
            val logo = Logo()
            logo.bitmap = logoBitmap
            logo.borderRadius = 50 // radius for logo's corners
            logo.borderWidth = 1 // width of the border to be added around the logo
            logo.scale = 0.25f // scale for the logo in the QR code
            logo.clippingRect = RectF(0f, 0f, 50f, 50f)

            val color = Color()
            color.light = 0xFFFFFFFF.toInt() // for blank spaces
            color.dark = 0xFF000000.toInt() // for non-blank spaces
            color.background =
                0xFFFFFFFF.toInt() // for the background (will be overriden by background images, if set)
            color.auto = false

            val renderOption = RenderOption()
            renderOption.content = qrCodeUrl
            renderOption.size = 800
            renderOption.borderWidth = 20
            renderOption.ecl = ErrorCorrectionLevel.M
            renderOption.patternScale = 1f
            renderOption.roundedPatterns = false
            renderOption.clearBorder = false
            renderOption.color = color
            renderOption.logo = logo

            try {
                val result = AwesomeQrRenderer.render(renderOption)
                withContext(Dispatchers.Main) {
                    if (result.bitmap != null) {
                        qrCode = result.bitmap
                        viewDataBinding!!.imgQrCode.setImageBitmap(qrCode)
                    }
                }
            } catch (e: CancellationException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun openAlertDialog() {
        val context = requireContext()

        // Create the parent layout (LinearLayout)
        val parentLayout = LinearLayout(context)
        val parentLayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        parentLayoutParams.setMargins(40, 30, 40, 20)
        parentLayout.layoutParams = parentLayoutParams

        // Create the input EditText
        val inputEditText = EditText(context)
        val inputLayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        inputLayoutParams.setMargins(50)
        inputEditText.layoutParams = inputLayoutParams
        inputEditText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL

        parentLayout.addView(inputEditText)

        val selectedCurrency = preferenceHelper.getSelectedCurrency()

        // Create the AlertDialog
        val dialog = AlertDialog.Builder(context)
            .setTitle("Enter Amount")
            .setView(parentLayout)
            .setPositiveButton("Confirm", null)
            .setNegativeButton("Cancel", null)
            .create()

        // Set an empty OnClickListener for the Confirm button to prevent auto-dismiss
        dialog.setOnShowListener { dialogInterface ->
            val positiveButton = (dialogInterface as AlertDialog).getButton(DialogInterface.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val editTextInput = inputEditText.text.toString()
                if (editTextInput.isNotEmpty()) {
                    val calculateTotal = editTextInput.toDouble() * args.tokenModel.t_price!!.toDouble()
                    val formattedValue = String.format("%.2f", calculateTotal)

                    viewDataBinding!!.txtInstruction.text =
                        editTextInput + "  " + args.tokenModel.t_symbol + " ≈ " + "${
                            selectedCurrency?.symbol
                        }$formattedValue"

                    qrCodeUrl = addressWallet.toString() + "?amount=$editTextInput"
                    renderQRCode()
                    dialog.dismiss() // Dismiss the dialog when Confirm is clicked
                } else {
                    inputEditText.error = "Amount required"
                }
            }
        }

        // Show the AlertDialog
        dialog.show()

        // Customize button text appearance
        val positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
        val negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        positiveButton.isAllCaps = false
        negativeButton.isAllCaps = false
    }



    override fun setupObserver() {}


    private fun onClickCopy() {
        val clipboard =
            requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        val clip = ClipData.newPlainText(
            keyAddress, addressWallet//qrCodeUrl
        )
        clipboard?.setPrimaryClip(clip)
        requireContext().showToast("Copied: $addressWallet")
    }

    private fun shareQRCode(qrCode: Bitmap?) {

        val share = Intent(Intent.ACTION_SEND)
        share.type = "image/jpeg"
        val bytes = ByteArrayOutputStream()
        qrCode?.compress(Bitmap.CompressFormat.JPEG, 100, bytes)


        val file = File(
            requireContext().cacheDir, "${Calendar.getInstance().timeInMillis}.jpeg"
        )
        val fos = FileOutputStream(file)
        fos.write(bytes.toByteArray())
        fos.close()

        // Open the PDF using a PDF viewer or webview
        val uri = FileProvider.getUriForFile(
            requireContext(), requireContext().packageName + ".provider", file
        )

        share.putExtra(Intent.EXTRA_STREAM, uri)
        startActivity(Intent.createChooser(share, "QR code Image"))
    }

    override fun onPause() {
        super.onPause()
        // Cancel the ongoing QR code generation when the fragment is paused
        qrCodeGenerationJob?.cancel()
    }

    fun openOptionPlatform(amount: String) {
        val list = mutableListOf<ButtonModel>()
        list.add(ButtonModel(1, buttonMetaMask, R.drawable.ic_meta_mask))
        list.add(ButtonModel(2, buttonTrustWallet, R.drawable.img_logo_circle_black))
        list.add(ButtonModel(3, buttonPlutoPe, R.drawable.img_logo_circle_black))

        DialogSelectButtonList.getInstance()?.show(requireContext(), list) {

            when (it.buttonName) {
                buttonMetaMask -> {
                    generateQRAccordingToPlatform(amount, buttonMetaMask)
                }

                buttonTrustWallet -> {
                    generateQRAccordingToPlatform(amount, buttonTrustWallet)
                }

                else -> {
                    generateQRAccordingToPlatform(amount, buttonPlutoPe)
                }
            }


        }

    }

    fun generateQRAccordingToPlatform(amount: String, platform: String) {
        val calculateTotal =
            amount.toDouble() * args.tokenModel.t_price!!.toDouble()
        val formattedValue = String.format("%.2f", calculateTotal)
        viewDataBinding!!.txtInstruction.text =
            getString(
                R.string.instruction_text,
                amount,
                args.tokenModel.t_symbol,
                preferenceHelper.getSelectedCurrency()?.symbol,
                formattedValue
            )

        when (platform) {
            buttonMetaMask -> {
                qrCodeUrl =
                    "ethereum:" + addressWallet.toString() + "@${args.tokenModel.chain?.chainIdHex}?value=$amount"
                renderQRCode()
            }

            buttonTrustWallet -> {
                qrCodeUrl =
                    "${args.tokenModel.chain?.chainForTrustWallet}:" + addressWallet.toString() + "?amount=$amount"
                renderQRCode()
            }

            else -> {
                qrCodeUrl = addressWallet.toString() + "?amount=$amount"
                renderQRCode()
            }
        }


    }


}