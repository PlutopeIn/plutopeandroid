package com.app.plutope.utils.extras

import android.text.InputFilter
import android.text.Spanned


class InputFilterMinMax : InputFilter {
    private val DOT = "."

    private var mMaxIntegerDigitsLength = 0
    private var mMaxDigitsAfterLength = 0
    private var mMax = 0.0

    fun digitsInputFilter(maxDigitsBeforeDot: Int, maxDigitsAfterDot: Int, maxValue: Double) {
        mMaxIntegerDigitsLength = maxDigitsBeforeDot
        mMaxDigitsAfterLength = maxDigitsAfterDot
        mMax = maxValue
    }

    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int,
    ): CharSequence? {
        val allText = getAllText(source, dest, dstart)
        val onlyDigitsText = getOnlyDigitsPart(allText)
        return if (allText.isEmpty()) {
            null
        } else {
            val enteredValue: Double = try {
                onlyDigitsText.toDouble()
            } catch (e: NumberFormatException) {
                return ""
            }
            checkMaxValueRule(enteredValue, onlyDigitsText)
        }
    }


    private fun checkMaxValueRule(enteredValue: Double, onlyDigitsText: String): CharSequence? {
        return if (enteredValue > mMax) {
            ""
        } else {
            handleInputRules(onlyDigitsText)
        }
    }

    private fun handleInputRules(onlyDigitsText: String): CharSequence? {
        return if (isDecimalDigit(onlyDigitsText)) {
            checkRuleForDecimalDigits(onlyDigitsText)
        } else {
            checkRuleForIntegerDigits(onlyDigitsText.length)
        }
    }

    private fun isDecimalDigit(onlyDigitsText: String): Boolean {
        return onlyDigitsText.contains(DOT)
    }

    private fun checkRuleForDecimalDigits(onlyDigitsPart: String): CharSequence? {
        val afterDotPart =
            onlyDigitsPart.substring(onlyDigitsPart.indexOf(DOT), onlyDigitsPart.length - 1)
        return if (afterDotPart.length > mMaxDigitsAfterLength) {
            ""
        } else null
    }

    private fun checkRuleForIntegerDigits(allTextLength: Int): CharSequence? {
        return if (allTextLength > mMaxIntegerDigitsLength) {
            ""
        } else null
    }

    private fun getOnlyDigitsPart(text: String): String {
        return text.replace("[^0-9?!\\.]".toRegex(), "")
    }

    private fun getAllText(source: CharSequence, dest: Spanned, dstart: Int): String {
        var allText = ""
        if (dest.toString().isNotEmpty()) {
            allText = if (source.toString().isEmpty()) {
                deleteCharAtIndex(dest, dstart)
            } else {
                StringBuilder(dest).insert(dstart, source).toString()
            }
        }
        return allText
    }

    private fun deleteCharAtIndex(dest: Spanned, dstart: Int): String {
        val builder = StringBuilder(dest)
        builder.deleteCharAt(dstart)
        return builder.toString()
    }
}