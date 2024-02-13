package com.app.plutope.ui.fragment.card.payment

import com.app.plutope.R
import com.app.plutope.databinding.RowCardTransactionBinding
import com.app.plutope.ui.base.BaseAdapter

class PaymentTransactionAdapter(list: List<PaymentTransactionModel>) :
    BaseAdapter<RowCardTransactionBinding, PaymentTransactionModel>(list) {
    override val layoutId: Int
        get() = R.layout.row_card_transaction

    override fun bind(binding: RowCardTransactionBinding, item: PaymentTransactionModel) {
        binding.model = item
    }
}