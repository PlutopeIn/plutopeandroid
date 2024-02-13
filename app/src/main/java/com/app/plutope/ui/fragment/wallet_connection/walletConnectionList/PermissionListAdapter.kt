package com.app.plutope.ui.fragment.wallet_connection.walletConnectionList

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.plutope.R
import com.app.plutope.databinding.RowPermissionListItemBinding

class PermissionListAdapter(
    var providerClick: ((Permission)) -> Unit
) :
    ListAdapter<Permission, PermissionListAdapter.ViewHolder>(DIFF_CALLBACK) {

    inner class ViewHolder(var binding: RowPermissionListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: Permission) {
            binding.model = model
            binding.imgPermissionIcon.setImageResource(model.icon)
            binding.txtPermissionTitle.text = model.title
            binding.txtPermissionTitle.setTextColor(model.color)
            binding.executePendingBindings()

        }

    }

    companion object {
        private val DIFF_CALLBACK = object :
            DiffUtil.ItemCallback<Permission>() {
            override fun areItemsTheSame(
                oldModel: Permission, newModel: Permission
            ) = oldModel == newModel

            override fun areContentsTheSame(
                oldModel: Permission,
                newModel: Permission
            ) = oldModel == newModel
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            RowPermissionListItemBinding.inflate(
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

data class Permission(
    val title: String,
    val icon: Int = R.drawable.ic_check_2,
    val color: Int = Color.WHITE
)