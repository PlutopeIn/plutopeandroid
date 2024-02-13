package com.app.plutope.ui.fragment.ens

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.plutope.R
import com.app.plutope.databinding.RowEnsListItemBinding


class ENSListAdapter(
    var providerClick: ((ENSListModel)) -> Unit
) :
    ListAdapter<ENSListModel, ENSListAdapter.ViewHolder>(DIFF_CALLBACK) {

    inner class ViewHolder(var binding: RowEnsListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: ENSListModel) {
            binding.ensListModel = model

            val price = model.data?.availability?.price?.subTotal?.usdCents?.div(100)
            binding.txtDomainPrice.apply {
                text = context.getString(R.string.price, "$price")
            }

            binding.executePendingBindings()

            binding.btnBuy.setOnClickListener {
                providerClick.invoke(model)
            }

        }

    }

    companion object {
        private val DIFF_CALLBACK = object :
            DiffUtil.ItemCallback<ENSListModel>() {
            override fun areItemsTheSame(
                oldModel: ENSListModel, newModel: ENSListModel
            ) = oldModel == newModel

            override fun areContentsTheSame(
                oldModel: ENSListModel,
                newModel: ENSListModel
            ) = oldModel == newModel
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            RowEnsListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])

        /*
                if (position == currentList.lastIndex) {
                    val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
                    params.bottomMargin = 300
                    holder.itemView.layoutParams = params
                } else {
                    val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
                    params.bottomMargin = 15
                    holder.itemView.layoutParams = params
                }
        */

    }


}