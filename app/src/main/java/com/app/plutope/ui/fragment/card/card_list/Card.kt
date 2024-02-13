package com.app.plutope.ui.fragment.card.card_list

import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.custom_views.CustomDotsIndicatorDecoration
import com.app.plutope.databinding.FragmentCardBinding
import com.app.plutope.model.Result
import com.app.plutope.model.TransactionHistoryModel
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.utils.safeNavigate
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Card : BaseFragment<FragmentCardBinding, CardViewModel>() {

    private val cardViewModel: CardViewModel by viewModels()
    var transactionListAdapter: TransactionListAdapter? = null
    override fun getViewModel(): CardViewModel {
        return cardViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.cardViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_card
    }

    override fun setupToolbarText(): String {
        return getString(R.string.card)
    }

    override fun setupUI() {

        val transactionList = TransactionHistoryModel()
        repeat(10) {
            transactionList.result.add(Result())
        }

        transactionListAdapter = TransactionListAdapter(addressToken = "") { }

        viewDataBinding!!.rvTransactionList.adapter = transactionListAdapter
        viewDataBinding!!.cardListAdapter = CardListAdapter(arrayListOf())

        val cardList = mutableListOf<CardListModel>()
        cardList.add(CardListModel())
        cardList.add(CardListModel())
        cardList.add(CardListModel())

        cardViewModel.setCardList(cardList)

        viewDataBinding!!.rvCardList.addItemDecoration(
            CustomDotsIndicatorDecoration(
                colorInactive = ContextCompat.getColor(requireContext(), R.color.white),
                colorActive = ContextCompat.getColor(requireContext(), R.color.blue_00C6FB)
            )
        )

        viewDataBinding!!.imgNotification.setOnClickListener {
            findNavController().safeNavigate(CardDirections.actionCardToNotification())
        }

        viewDataBinding!!.btnGetCard.setOnClickListener {
            findNavController().safeNavigate(CardDirections.actionCardToSignUpLogin())
        }

    }

    override fun setupObserver() {

    }


}