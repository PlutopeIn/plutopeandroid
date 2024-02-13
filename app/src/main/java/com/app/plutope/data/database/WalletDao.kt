package com.app.plutope.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.app.plutope.model.Wallets
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {

    @Query("SELECT * from wallets")
    fun getAllWallets(): Flow<List<Wallets>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(wallet: Wallets)

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<Wallets>?)


    @Query("SELECT * FROM Wallets WHERE w_id = :wId")
    fun getUserById(wId: Int): Wallets

    @Query("SELECT *  FROM Wallets ORDER BY w_id DESC LIMIT 1")
    fun getLastInsertedWallet(): Wallets?


    @Query("SELECT MAX(w_id) FROM Wallets")
    fun getWalletMaxId(): Int?

    @Query("Update Wallets set w_isprimary=(CASE WHEN w_id =:w_id THEN 1 ELSE 0 END)")
    suspend fun updateWalletPrimary(w_id:Int):Int

    @Query("SELECT * FROM Wallets where w_isprimary=1 LIMIT 1")
    fun getPrimaryWallet(): Flow<Wallets>?

    @Query("Update Wallets set w_is_cloud_backup=:isCloudBackup, w_is_manual_backup=:isManualBackup, w_wallet_name=:walletName,folderId=:folderId,fileId=:fileId where w_id=:walletId")
    suspend fun updateWalletBackupSet(
        isCloudBackup: Boolean,
        isManualBackup: Boolean,
        walletId: Int,
        walletName: String,
        folderId: String,
        fileId: String
    ): Int

    @Query("Delete from Wallets where w_id=:walletId")
    suspend fun deleteWallet(walletId: Int): Int

    @Update
    suspend fun updateWallets(wallets: List<Wallets?>): Int


}