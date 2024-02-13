package com.app.plutope.ui.fragment.passcode_screen

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.app.plutope.R
import com.app.plutope.custom_views.CustomKeyboardView
import com.app.plutope.databinding.FragmentConformPasscodeBinding
import com.app.plutope.passcode.pass2.PassCodeView
import com.app.plutope.utils.constant.isImportWallet
import com.app.plutope.utils.extras.PreferenceHelper
import com.app.plutope.utils.safeNavigate

class ConfirmPasscode : Fragment() {
    lateinit var binding: FragmentConformPasscodeBinding
    private var input: String = ""
    val args: ConfirmPasscodeArgs by navArgs()
    private val shakeDelay: Long = 300
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentConformPasscodeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imgBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.keyboardView.listner = object : CustomKeyboardView.NotifyKeyListener {
            override fun getValue(value: String) {
                appendInputText(value)
                binding.passwordView.setPassCode(input)
            }

            override fun removeValue() {
                removeInputText()
                binding.passwordView.setPassCode(input)
            }

            override fun removeAllValue() {
                input = ""
                binding.passwordView.setPassCode(input)

            }
        }

        binding.passwordView.setOnTextChangeListener(object : PassCodeView.TextChangeListener {
            override fun onTextChanged(text: String?) {
                if (text?.length == 6) {
                    if (text != args.passcode) {
                        binding.passwordView.setFilledDrawable(R.drawable.ic_asterisk_error)
                        handler.postDelayed({
                            input = ""
                            binding.passwordView.startShakeAnimation()
                            binding.passwordView.reset()
                        }, shakeDelay)

                    } else {
                        PreferenceHelper.getInstance().appPassword = text
                        input = ""
                        PreferenceHelper.getInstance().isAppLock = true
                        if (args.isFromSecurity) {
                            findNavController().popBackStack(R.id.security, false)
                        } else {
                            if (isImportWallet) {
                                findNavController().safeNavigate(ConfirmPasscodeDirections.actionConfirmPasscodeToRestoreWallet())
                            } else {
                                findNavController().safeNavigate(ConfirmPasscodeDirections.actionConfirmPasscodeToPhraseBackupFragment())

                            }

                        }
                    }

                }
            }
        })


    }

    fun appendInputText(text: String) {
        if (text.length + input.length > 6) {
            return
        }
        input += text
    }

    /**
     * remove last characters from input values and run not input animation
     */
    fun removeInputText() {
        if (input.isEmpty()) {
            return
        }

        input = input.dropLast(1)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}