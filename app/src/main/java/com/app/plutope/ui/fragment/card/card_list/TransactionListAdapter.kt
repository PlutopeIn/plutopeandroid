package com.app.plutope.ui.fragment.card.card_list

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.plutope.R
import com.app.plutope.databinding.RowTransactionListBinding
import com.app.plutope.model.TransactionLists


class TransactionListAdapter(
    var addressToken: String,
    var providerClick: ((TransactionLists)) -> Unit
) :
    ListAdapter<TransactionLists, TransactionListAdapter.ViewHolder>(DIFF_CALLBACK) {

    inner class ViewHolder(var binding: RowTransactionListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(model: TransactionLists) {
            binding.transactionModel = model


            binding.txtPrice.text = model.priceToShow

            if(model.isSwap){
                binding.txtTransactionName.text = "Swap"
            }else {
                if (model.methodId == "" && model.isToContract) {
                    binding.txtTransactionName.text = "Transfer"

                } else {
                    if (addressToken == "" && model.amount.toDouble()<=0.0 ) {
                        binding.txtTransactionName.text = "Smart Contract Call"
                        binding.txtPrice.text =
                            "-0.00 ${model.priceToShow?.split(" ")?.lastOrNull()}"

                    } else {
                        binding.txtTransactionName.text = "Transfer"
                    }
                }
            }

            val textColor =
                if (model.priceToShow?.startsWith("+") == true) binding.txtPrice.context?.resources?.getColor(
                    R.color.green_099817,
                    null
                ) else binding.txtPrice.context?.resources?.getColor(R.color.red, null)

            if (model.priceToShow?.startsWith("+") == true) binding.imgBag.setImageDrawable(
                ResourcesCompat.getDrawable(
                    binding.imgBag.resources,
                    R.drawable.ic_back_arrow_down,
                    null
                )
            )
            else binding.imgBag.setImageDrawable(
                ResourcesCompat.getDrawable(
                    binding.imgBag.resources,
                    R.drawable.ic_back_arrow_right_up,
                    null
                )
            )

            binding.txtPrice.setTextColor(textColor!!)
            binding.executePendingBindings()

            itemView.setOnClickListener {
                providerClick.invoke(model)
            }

        }

    }


    companion object {
        private val DIFF_CALLBACK = object :
            DiffUtil.ItemCallback<TransactionLists>() {
            override fun areItemsTheSame(
                oldModel: TransactionLists, newModel: TransactionLists
            ) = oldModel == newModel

            override fun areContentsTheSame(
                oldModel: TransactionLists,
                newModel: TransactionLists
            ) = oldModel == newModel
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            RowTransactionListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])

        /*  if (position == currentList.lastIndex) {
              val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
              params.bottomMargin = 300
              holder.itemView.layoutParams = params
          } else {
              val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
              params.bottomMargin = 15
              holder.itemView.layoutParams = params
          }*/

    }


}