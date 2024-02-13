package com.app.plutope.ui.fragment.contact

import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.plutope.databinding.RowContactItemBinding
import com.app.plutope.databinding.RowNotificationListBinding
import com.app.plutope.model.ContactModel
import com.app.plutope.model.ContactType


class ContactListAdapter(
    var providerClick: ((ContactModel)) -> Unit
) :
    ListAdapter<ContactModel, ContactListAdapter.ViewHolder>(DIFF_CALLBACK) {

    inner class ViewHolder(var binding: RowContactItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: ContactModel) {
            binding.contactModel = model
            if(model.type==ContactType.HEADER){
                binding.txtHeader.visibility=VISIBLE
                binding.cardItem.visibility= GONE
            }else{
                binding.txtHeader.visibility=GONE
                binding.cardItem.visibility= VISIBLE
            }
            binding.executePendingBindings()

            itemView.setOnClickListener {
                providerClick.invoke(model)
            }

        }

    }

    companion object {
        private val DIFF_CALLBACK = object :
            DiffUtil.ItemCallback<ContactModel>() {
            override fun areItemsTheSame(
                oldModel: ContactModel, newModel: ContactModel
            ) = oldModel == newModel

            override fun areContentsTheSame(
                oldModel: ContactModel,
                newModel: ContactModel
            ) = oldModel == newModel
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            RowContactItemBinding.inflate(
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