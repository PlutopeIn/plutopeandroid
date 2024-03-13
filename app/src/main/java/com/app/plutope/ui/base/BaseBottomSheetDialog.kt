package com.app.plutope.ui.base

import android.app.Dialog
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.view.ContextThemeWrapper
import com.app.plutope.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class BaseBottomSheetDialog : BottomSheetDialogFragment() {
     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
         super.onViewCreated(view, savedInstanceState)
         setUpObservers()
         setUpUI()
     }

     abstract fun setUpObservers()

     abstract fun setUpUI()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog: Dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            setupFullHeight(bottomSheetDialog)
        }
        return dialog
    }

    private fun setupFullHeight(bottomSheetDialog: BottomSheetDialog) {
        setAsBottomSheet(bottomSheetDialog)
    }

    private fun setAsBottomSheet(dialog: BottomSheetDialog) {
        val bottomSheet =
            dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        val behavior = BottomSheetBehavior.from(bottomSheet!!)
        behavior.isHideable = false
        bottomSheet.setBackgroundColor(Color.TRANSPARENT)
        val displayMetrics: DisplayMetrics = requireContext().resources.displayMetrics
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            val display = requireActivity().display
            display?.getRealMetrics(displayMetrics)
        } else {
            @Suppress("DEPRECATION")
            val display = requireActivity().windowManager?.defaultDisplay
            @Suppress("DEPRECATION")
            display?.getMetrics(displayMetrics)
        }

        behavior.isHideable = true
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (slideOffset <= -0.3) {
                    dialog.dismiss()
                }
            }
        })
        dialog.setCancelable(true)
//        bottomSheet.layoutParams.height = Resources.getSystem().displayMetrics.heightPixels - getStatusBarHeight(requireActivity().window!!)
        bottomSheet.layoutParams.width = (Resources.getSystem().displayMetrics.widthPixels)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.peekHeight = 4000

    }

     override fun getTheme(): Int {
         return R.style.BottomSheetDialogThemeNoAnimation
     }

     override fun onGetLayoutInflater(savedInstanceState: Bundle?): LayoutInflater {
         val inflater = super.onGetLayoutInflater(savedInstanceState)
         val wrappedContext = ContextThemeWrapper(requireContext(), R.style.BottomSheetTheme)
         return inflater.cloneInContext(wrappedContext)
     }

 }