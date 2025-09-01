package com.app.plutope.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.plutope.databinding.RowCountryListBinding
import com.app.plutope.model.CountryListModel
import com.app.plutope.utils.constant.typeCountryCodeList
import com.app.plutope.utils.constant.typeCountryList
import com.app.plutope.utils.constant.typeCurrencyList


class CountryListAdapter(
    private val listType: Int,
    private val providerClick: ((CountryListModel)) -> Unit
) : ListAdapter<CountryListModel, CountryListAdapter.ViewHolder>(DIFF_CALLBACK) {

    inner class ViewHolder(var binding: RowCountryListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(model: CountryListModel) {
            binding.model = model

            when (listType) {
                typeCountryList -> {
                    binding.txtCountryCode.visibility = View.GONE
                    binding.txtLockMethodValue.text = model.countryName
                }

                typeCurrencyList -> {
                    binding.txtCountryCode.visibility = View.GONE
                    binding.txtLockMethodValue.text = model.currencyCode
                }

                typeCountryCodeList -> {
                    binding.txtCountryCode.visibility = View.VISIBLE
                    binding.txtCountryCode.text = model.code
                    binding.txtLockMethodValue.text = model.countryName
                }
            }

            binding.executePendingBindings()

            itemView.setOnClickListener {
                // Update selection state in the model
                val updatedModel = model.copy(isSelected = true)

                // Update list to reflect selection change
                val newList = currentList.map {
                    if (it == model) updatedModel else it.copy(isSelected = false)
                }

                submitList(newList)
                providerClick.invoke(updatedModel)
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CountryListModel>() {
            override fun areItemsTheSame(
                oldModel: CountryListModel, newModel: CountryListModel
            ) = oldModel.code == newModel.code

            override fun areContentsTheSame(
                oldModel: CountryListModel,
                newModel: CountryListModel
            ) = oldModel == newModel
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            RowCountryListBinding.inflate(
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
