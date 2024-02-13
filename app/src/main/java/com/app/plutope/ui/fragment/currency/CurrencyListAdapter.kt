package com.app.plutope.ui.fragment.currency

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.plutope.databinding.RowCurrencyListBinding
import com.app.plutope.model.CurrencyModel


class CurrencyListAdapter(
    var providerClick: ((CurrencyModel)) -> Unit
) :
    ListAdapter<CurrencyModel, CurrencyListAdapter.ViewHolder>(DIFF_CALLBACK) {

    inner class ViewHolder(var binding: RowCurrencyListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: CurrencyModel) {
            binding.model = model

            binding.executePendingBindings()

            itemView.setOnClickListener {
                providerClick.invoke(model)
            }

        }

    }

    companion object {
        private val DIFF_CALLBACK = object :
            DiffUtil.ItemCallback<CurrencyModel>() {
            override fun areItemsTheSame(
                oldModel: CurrencyModel, newModel: CurrencyModel
            ) = oldModel == newModel

            override fun areContentsTheSame(
                oldModel: CurrencyModel,
                newModel: CurrencyModel
            ) = oldModel == newModel
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            RowCurrencyListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])

    }


}