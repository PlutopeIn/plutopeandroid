package com.app.plutope.data.repository

import com.app.plutope.data.database.WalletDao
import com.app.plutope.model.Wallets
import com.app.plutope.network.NoConnectivityException
import com.app.plutope.utils.Securities
import com.app.plutope.utils.constant.NO_INTERNET_CONNECTION
import com.app.plutope.utils.constant.serverErrorMessage
import com.app.plutope.utils.network.NetworkState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import java.net.UnknownHostException
import javax.inject.Inject

class WalletRepo @Inject constructor(private val walletDao: WalletDao) {
    suspend fun insertWallet(
        mnemonics: String,
        passWalletname: String = "",
        isFromDrive:Boolean=false,
        isManualBackup:Boolean=false,
        folderId:String="",fileId:String=""
    ): NetworkState<Wallets?> {
        return try {
            var walletName = passWalletname
            if (passWalletname.isEmpty()) {
                val maxId = walletDao.getWalletMaxId() ?: 0
                walletName = "Main Wallet ${maxId + 1}"
            }
            val newWallet =
                Wallets(
                    w_isprimary = 1,
                    w_mnemonic = Securities.encrypt(mnemonics),
                    w_wallet_name = walletName,
                    w_is_cloud_backup = isFromDrive,
                    w_is_manual_backup = isManualBackup,
                    folderId = folderId,
                    fileId = fileId
                )
            walletDao.insert(newWallet)

            //lastadded wallet
            val lastInsertedWallet = walletDao.getLastInsertedWallet()

            //update primary wallet
            walletDao.updateWalletPrimary(lastInsertedWallet?.w_id.toString().toInt())

            NetworkState.Success("", lastInsertedWallet)
        } catch (e: Exception) {
            e.printStackTrace()
            if(e is NoConnectivityException || e is UnknownHostException) {
                NetworkState.SessionOut("", NO_INTERNET_CONNECTION)
            }else {
                NetworkState.Error(serverErrorMessage)
            }
        }

    }

    suspend fun getAllWalletList(): Flow<NetworkState<List<Wallets?>>> {
        return flow {
            val localData: MutableList<Wallets> = mutableListOf()
            localData.addAll(walletDao.getAllWallets().first())

            if (localData.isNotEmpty()) {
                // If the local data is not empty, return it immediately
                emit(NetworkState.Success("", localData))
            } else {
                emit(NetworkState.Error("Failed to load"))
            }
        }
    }


    suspend fun getPrimaryWallet(): Flow<NetworkState<Wallets?>> {
        return flow {
            var localData: Wallets? = null

            localData = walletDao.getPrimaryWallet()?.first()
            if (localData != null) {
                // If the local data is not empty, return it immediately
                emit(NetworkState.Success("", localData))
            } else {
                emit(NetworkState.Error("Failed to load"))
            }
        }
    }


    suspend fun updatePrimaryWallet(walletId: Int): Flow<NetworkState<Wallets?>> {
        return flow {
            walletDao.updateWalletPrimary(walletId)
                var walletdata: Wallets? = null
                emit(NetworkState.Success("", walletdata))
                walletDao.getPrimaryWallet()?.collect { networkState ->
                    walletdata = networkState
                    if (walletdata!=null) {
                        // If the local data is not empty, return it immediately
                        emit(NetworkState.Success("", walletdata))
                    } else {
                        emit(NetworkState.Error("Failed to load"))
                    }
                }

        }
    }
    suspend fun updateWalletBackupSet(isCloudBackup:Boolean,isManualBackup:Boolean,walletId:Int,walletName:String,folderId:String,fileId:String): Flow<NetworkState<Wallets?>> {
        return flow {
            var localData: Int? = null

            localData = walletDao.updateWalletBackupSet(isCloudBackup,isManualBackup,walletId,walletName,folderId,fileId)
            var walletdata: Wallets? = null
            walletDao.getPrimaryWallet()?.collect { networkState ->
                walletdata = networkState

                emit(NetworkState.Success("", walletdata))
            }
           if(walletdata==null){
               emit(NetworkState.Error("Failed to load"))
           }

        }
    }

    suspend fun deleteWalletByWalletId(walletId: Int): Flow<NetworkState<Int?>> {
        return flow {
            var localData: Int? = null

            localData = walletDao.deleteWallet(walletId)
            emit(NetworkState.Success("", localData))

        }
    }


    suspend fun updateAllWalletList(wallet: MutableList<Wallets?>): Flow<NetworkState<Int?>> {
        return flow {
            var localData: Int? = null
            localData = walletDao.updateWallets(wallet)
            emit(NetworkState.Success("", localData))

        }
    }

}