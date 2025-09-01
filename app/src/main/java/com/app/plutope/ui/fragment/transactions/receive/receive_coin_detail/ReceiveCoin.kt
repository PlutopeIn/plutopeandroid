package com.app.plutope.ui.fragment.transactions.receive.receive_coin_detail


import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentReceiveCoinBinding
import com.app.plutope.dialogs.RequestPaymentDialogFragment
import com.app.plutope.model.Wallet
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.utils.coinTypeEnum.CoinType
import com.app.plutope.utils.constant.isFromReceived
import com.app.plutope.utils.extras.setSafeOnClickListener
import com.app.plutope.utils.hideLoader
import com.app.plutope.utils.imageViewToBitmap
import com.app.plutope.utils.loge
import com.app.plutope.utils.showToast
import com.github.alexzhirkevich.customqrgenerator.style.Color
import com.github.alexzhirkevich.customqrgenerator.vector.QrCodeDrawable
import com.github.alexzhirkevich.customqrgenerator.vector.createQrVectorOptions
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorBallShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorColor
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorFrameShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorLogoPadding
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorLogoShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorPixelShape
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
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
        qrCodeUrl = /*"${args.tokenModel.chain?.chainForTrustWallet}:" +*/ addressWallet!!

        viewDataBinding?.imgBack?.setOnClickListener {
            findNavController().navigateUp()
        }

        viewDataBinding?.txtWalletKey?.text = addressWallet
        viewDataBinding!!.imgCopy.setOnClickListener {
            onClickCopy()
        }




        viewDataBinding!!.imgShare.setSafeOnClickListener {
            shareQRCode(qrCode)
        }

        viewDataBinding!!.imgDoller.setOnClickListener {
            // openAlertDialog()

            val dialog = RequestPaymentDialogFragment.newInstance(args.tokenModel)
            dialog.setDialogListener(object : RequestPaymentDialogFragment.DialogOnClickBtnListner {
                override fun onSubmitClicked(amount: String) {
                    loge("DialogAmount", "$amount")
                    val calculateTotal =
                        amount.toDouble() * args.tokenModel.t_price.toDouble()
                    val formattedValue = String.format("%.2f", calculateTotal)

                    viewDataBinding!!.txtInstruction.text =
                        amount + "  " + args.tokenModel.t_symbol + " ≈ " + "${preferenceHelper.getSelectedCurrency()?.symbol}$formattedValue"

                    qrCodeUrl =
                        "${args.tokenModel.chain?.chainForTrustWallet}:" + addressWallet.toString() + "?amount=$amount"
                    // renderQRCode()

                    renderLatestQRCode(qrCodeUrl)
                }
            })
            dialog.show(childFragmentManager, "RequestPaymentDialog")
        }

        // renderQRCode()

        renderLatestQRCode(qrCodeUrl)
    }

    /* private fun renderQRCode() {

         qrCodeGenerationJob?.cancel()


         qrCodeGenerationJob = CoroutineScope(Dispatchers.Default).launch {
             val logoBitmap =
                 BitmapFactory.decodeResource(resources, R.drawable.img_logo_white_blue)
             val logo = Logo()
             logo.bitmap = logoBitmap
             logo.borderRadius = 50
             logo.borderWidth = 1
             logo.scale = 0.25f
             logo.clippingRect = RectF(0f, 0f, 50f, 50f)

             val color = Color()
             color.light = 0xFFFFFFFF.toInt()
             color.dark = 0xFF000000.toInt()
             color.background =
                 0xFFFFFFFF.toInt()
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

     }*/

    private fun renderLatestQRCode(url: String) {
        val options = createQrVectorOptions {
            padding = .125f
            background {
                drawable = ContextCompat
                    .getDrawable(requireActivity(), R.drawable.frame)
            }

            logo {
                drawable = ContextCompat
                    .getDrawable(requireActivity(), R.drawable.img_logo_white_blue)
                size = .25f
                padding = QrVectorLogoPadding.Natural(.2f)
                shape = QrVectorLogoShape
                    .Circle
            }
            colors {

                dark = QrVectorColor.Solid(Color(0xFF000000))
                light = QrVectorColor.Solid(Color(0xFFFFFFFF))

                ball = QrVectorColor.Solid(
                    ContextCompat.getColor(requireActivity(), R.color.black_both_themed)
                )
                frame = QrVectorColor.LinearGradient(
                    colors = listOf(
                        0f to android.graphics.Color.RED,
                        1f to android.graphics.Color.BLUE,
                    ),
                    orientation = QrVectorColor.LinearGradient
                        .Orientation.LeftDiagonal
                )
            }
            shapes {
                darkPixel = QrVectorPixelShape
                    .RoundCorners(.5f)
                lightPixel = QrVectorPixelShape
                    .RoundCorners(.5f)
                ball = QrVectorBallShape
                    .RoundCorners(.25f)
                frame = QrVectorFrameShape
                    .RoundCorners(.25f)
            }
        }
        val generator = QrCodeDrawable(
            data = { url },
            options = options
        )
        viewDataBinding!!.imgQrCode.setImageDrawable(generator)
        viewDataBinding?.imgQrCode?.viewTreeObserver?.addOnGlobalLayoutListener {
            qrCode = imageViewToBitmap(viewDataBinding!!.imgQrCode)
        }

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

    private var isSharingInProgress = false

    private fun shareQRCode(qrCode: Bitmap?) {
        // Check if sharing is already in progress
        if (isSharingInProgress) return

        isSharingInProgress = true // Set the flag

        val share = Intent(Intent.ACTION_SEND)
        share.type = "image/jpeg"
        val bytes = ByteArrayOutputStream()
        qrCode?.compress(Bitmap.CompressFormat.JPEG, 100, bytes)

        val file = File(requireContext().cacheDir, "${Calendar.getInstance().timeInMillis}.jpeg")
        val fos = FileOutputStream(file)
        fos.write(bytes.toByteArray())
        fos.close()

        val uri = FileProvider.getUriForFile(
            requireContext(), requireContext().packageName + ".provider", file
        )
        share.putExtra(Intent.EXTRA_STREAM, uri)

        // Reset the flag after the chooser is opened
        try {
            startActivity(Intent.createChooser(share, "QR code Image"))
        } catch (e: ActivityNotFoundException) {
            // Handle the case where no app can handle the intent
            loge("ShareQRCode", "No app available to share the QR code $e")
        } finally {
            isSharingInProgress = false // Reset the flag
        }
    }


    override fun onPause() {
        super.onPause()
        // Cancel the ongoing QR code generation when the fragment is paused
        qrCodeGenerationJob?.cancel()
    }


    /* fun generateQRAccordingToPlatform(amount: String, platform: String) {
         val calculateTotal =
             amount.toDouble() * args.tokenModel.t_price.toDouble()
         val formattedValue = String.format(getString(R.string._2f), calculateTotal)
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

 */
}