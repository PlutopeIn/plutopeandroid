package com.app.plutope.ui.fragment.card.card_list

import com.app.plutope.R
import com.app.plutope.databinding.RowCardListBinding
import com.app.plutope.ui.base.BaseAdapter

class CardListAdapter(list: MutableList<CardListModel>) :
    BaseAdapter<RowCardListBinding, CardListModel>(list) {
    override val layoutId: Int
        get() = R.layout.row_card_list

    override fun bind(binding: RowCardListBinding, item: CardListModel) {
        binding.model = item
        binding.executePendingBindings()

    }
}