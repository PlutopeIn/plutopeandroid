package com.app.plutope.ui.fragment.wallet.select_wallet_backup

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.plutope.databinding.RowWalletBackUpListItemBinding
import com.app.plutope.model.GoogleDriveBackupModel
import com.app.plutope.model.Wallets


class WalletBackupListAdapter(
    var providerClick: ((GoogleDriveBackupModel)) -> Unit
) :
    ListAdapter<GoogleDriveBackupModel, WalletBackupListAdapter.ViewHolder>(DIFF_CALLBACK) {

    inner class ViewHolder(var binding: RowWalletBackUpListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: GoogleDriveBackupModel) {
            binding.model = model
            binding.txtTransactionDateTime.text = model.createdTime
            binding.executePendingBindings()

            itemView.setOnClickListener {
                providerClick.invoke(model)
            }

        }

    }

    companion object {
        private val DIFF_CALLBACK = object :
            DiffUtil.ItemCallback<GoogleDriveBackupModel>() {
            override fun areItemsTheSame(
                oldModel: GoogleDriveBackupModel, newModel: GoogleDriveBackupModel
            ) = oldModel == newModel

            override fun areContentsTheSame(
                oldModel: GoogleDriveBackupModel,
                newModel: GoogleDriveBackupModel
            ) = oldModel == newModel
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            RowWalletBackUpListItemBinding.inflate(
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