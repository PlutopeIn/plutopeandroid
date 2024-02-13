package com.app.plutope.ui.adapter

import com.app.plutope.R
import com.app.plutope.databinding.LayoutWalletCurrencyListBinding
import com.app.plutope.model.CurrencyModel
import com.app.plutope.ui.base.BaseAdapter


class CurrencyListAdapter(
    list: List<CurrencyModel>, var listen: OnActionClickListener
) : BaseAdapter<LayoutWalletCurrencyListBinding, CurrencyModel>(list) {

    override val layoutId: Int = R.layout.layout_wallet_currency_list

    override fun bind(binding: LayoutWalletCurrencyListBinding, item: CurrencyModel) {
        binding.apply {
            // imgCountryFlag = ImageCategory.PRODUCT
            model = item
            listener = listen
            // txtCurrencyName.m = item.name
            executePendingBindings()
        }
    }

    interface OnActionClickListener {
        fun onEditCLick(model: CurrencyModel)
        fun onDeleteClick(model: CurrencyModel)
    }
}