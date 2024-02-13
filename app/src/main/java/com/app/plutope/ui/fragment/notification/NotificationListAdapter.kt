package com.app.plutope.ui.fragment.notification

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.plutope.databinding.RowNotificationListBinding


class NotificationListAdapter(
    var providerClick: ((NotificationModel)) -> Unit
) :
    ListAdapter<NotificationModel, NotificationListAdapter.ViewHolder>(DIFF_CALLBACK) {

    inner class ViewHolder(var binding: RowNotificationListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: NotificationModel) {
            binding.notificationModel = model
            binding.executePendingBindings()

            itemView.setOnClickListener {
                providerClick.invoke(model)
            }

        }

    }

    companion object {
        private val DIFF_CALLBACK = object :
            DiffUtil.ItemCallback<NotificationModel>() {
            override fun areItemsTheSame(
                oldModel: NotificationModel, newModel: NotificationModel
            ) = oldModel == newModel

            override fun areContentsTheSame(
                oldModel: NotificationModel,
                newModel: NotificationModel
            ) = oldModel == newModel
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            RowNotificationListBinding.inflate(
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