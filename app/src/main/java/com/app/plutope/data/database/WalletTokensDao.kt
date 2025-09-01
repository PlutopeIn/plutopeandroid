package com.app.plutope.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.app.plutope.model.WalletTokens
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletTokensDao {


    @Query("SELECT * from walletTokens")
    fun getAllWalletsTokens(): Flow<List<WalletTokens>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(walletTokens: WalletTokens)

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<WalletTokens>)
}