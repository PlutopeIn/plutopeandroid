package com.app.plutope.ui.base

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModel
import androidx.viewbinding.ViewBinding

abstract class BaseDialogFragment<VB : ViewBinding, VM : ViewModel> :
    DialogFragment() {

    private lateinit var _vb: VB
    private lateinit var _vm: VM
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _vb = DataBindingUtil.inflate(inflater, getLayoutID(), container, false)
        _vm = getBindedViewModel()
        setDataBinding(_vb)

        setBindingVariables()
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return _vb.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setUpUI()
    }

    abstract fun getLayoutID(): Int

    abstract fun setDataBinding(vb: VB)

    abstract fun setBindingVariables()

    abstract fun getBindedViewModel(): VM

    abstract fun setupObservers()

    abstract fun setUpUI()
    abstract fun showToolbar(): Boolean
    // abstract fun showTitleView(): Boolean

}
