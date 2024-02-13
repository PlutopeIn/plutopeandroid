package com.app.plutope.dialogs

import android.content.Context
import android.view.Gravity
import android.view.WindowManager
import androidx.recyclerview.widget.RecyclerView
import com.app.plutope.R
import com.app.plutope.model.ButtonModel
import com.google.android.material.bottomsheet.BottomSheetDialog


class DialogSelectButtonList private constructor() {
    companion object {
        var singleInstence: DialogSelectButtonList? = null
        fun getInstance(): DialogSelectButtonList? {
            if (singleInstence == null) {
                singleInstence = DialogSelectButtonList()
            }
            return singleInstence
        }
    }

    private var alertDialogLocation: BottomSheetDialog? = null
    fun show(
        context: Context?,
        list: MutableList<ButtonModel>,
        unit: (ButtonModel) -> Any
    ) {

        if (alertDialogLocation == null) {
            alertDialogLocation = BottomSheetDialog(
                context!!,
                android.R.style.Theme_DeviceDefault_Dialog_NoActionBar_MinWidth
            )
        }
        alertDialogLocation?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(alertDialogLocation?.window?.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.BOTTOM
        lp.windowAnimations = R.style.DialogAnimation
        alertDialogLocation?.window?.attributes = lp
        alertDialogLocation?.behavior?.peekHeight = 2000
        alertDialogLocation?.setCancelable(true)
        alertDialogLocation?.setContentView(R.layout.dialog_option_button_selction)


        val rvDialogButtonList =
            alertDialogLocation?.findViewById<RecyclerView>(R.id.rv_dialog_button_list)
        rvDialogButtonList?.adapter = BottomSheetButtonListAdapter(list) {
            unit.invoke(it)
            alertDialogLocation?.dismiss()

        }



        if (alertDialogLocation != null) {
            if (alertDialogLocation!!.isShowing) {
                alertDialogLocation?.dismiss()
            } else {
                try {
                    alertDialogLocation?.show()
                } catch (e: WindowManager.BadTokenException) {
                    e.printStackTrace()
                }

            }
        }
    }

    fun dismiss() {
        if (alertDialogLocation != null) {
            alertDialogLocation?.dismiss()
        }
    }

}