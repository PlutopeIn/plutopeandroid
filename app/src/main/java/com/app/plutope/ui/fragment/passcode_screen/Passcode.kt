package com.app.plutope.ui.fragment.passcode_screen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.app.plutope.custom_views.CustomKeyboardView
import com.app.plutope.databinding.FragmentPasscodeBinding
import com.app.plutope.passcode.pass2.PassCodeView
import com.app.plutope.utils.safeNavigate

class Passcode : Fragment() {
    private var input: String = ""
    lateinit var binding: FragmentPasscodeBinding
    val args:PasscodeArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPasscodeBinding.inflate(layoutInflater)
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
                    input = ""

                    findNavController().safeNavigate(
                        PasscodeDirections.actionPasscodeToConfirmPasscode(
                            text,args.isFromSecurity
                        )
                    )
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


}