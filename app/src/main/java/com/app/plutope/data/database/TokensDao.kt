package com.app.plutope.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.app.plutope.model.TokenListImageModel
import com.app.plutope.model.Tokens
import com.app.plutope.model.WalletTokens
import kotlinx.coroutines.flow.Flow

@Dao
interface TokensDao {

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<Tokens>?)

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(token: Tokens)


    @Query("SELECT * FROM Tokens where t_address != '0x0000000000000000000000000000000000001010' and isEnable=:isEnable order by t_symbol asc")
    fun getTokenList(isEnable: Int): List<Tokens>

    @Query("SELECT * FROM Tokens where isEnable=0 order by t_symbol asc")
    fun getAllNotEnableTokenList(): List<Tokens>

    @Query("SELECT * FROM Tokens where t_address != '0x0000000000000000000000000000000000001010' order by t_symbol asc ")
    fun getAlleTokenList(): List<Tokens>

    @Update
    suspend fun updateTokens(tokens: List<Tokens>): Int

    @Update
    suspend fun updateToken(tokens: Tokens): Int

    @Query("Delete from Tokens where t_pk=:tokenId and isCustomTokens=1")
    suspend fun deleteCustomToken(tokenId: Int)


    //@Query("Update walletTokens set isEnable=:isEnable where t_pk=:tokenId and walletId=:walletId")
    @Query("Delete from walletTokens where t_pk=:tokenId and walletId=:walletId")
    suspend fun updateWalletToken(tokenId: Int, walletId: Int)


    @Transaction
    @Insert
    suspend fun insertAllWalletTokens(list: List<WalletTokens?>)

    @Transaction
    @Insert
    suspend fun insertWalletToken(walletToken: WalletTokens?)


    //@Query("SELECT token.* FROM walletTokens AS walletToken INNER JOIN tokens AS token ON walletToken.t_pk = token.t_pk and walletId=:walletId")
    @Query("SELECT token.* FROM walletTokens AS walletToken INNER JOIN tokens AS token ON walletToken.t_pk = token.t_pk and walletToken.walletId=:walletId and walletToken.isEnable=1")
    fun getAllSpecificWalletTokens(walletId: Int): Flow<List<Tokens>>


    //@Query("SELECT * FROM Tokens WHERE isEnable = 0  AND t_pk NOT IN ( SELECT t_pk  FROM walletTokens WHERE walletId = :walletId )")
    @Query("SELECT * FROM Tokens where t_pk NOT IN ( SELECT t_pk  FROM walletTokens WHERE walletId = :walletId ) order by t_symbol asc")
    fun getAllDisableTokens(walletId: Int): List<Tokens>

    @Query("SELECT * FROM Tokens ORDER BY t_pk DESC LIMIT 1")
    suspend fun getLastInsertedToken(): Tokens

    @Transaction
    @Insert
    suspend fun insertTokenImageTokens(list: List<TokenListImageModel?>)


    @Query("SELECT * FROM TokenImageModel")
    fun getAlleTokenImageList(): List<TokenListImageModel>

    @Query("SELECT count(*)  FROM walletTokens WHERE walletId = :walletId and t_pk=:tokenid")
    fun getCountTokenIdSpecificWallet(walletId: Int, tokenid: Int): Int


    @Query("SELECT * FROM Tokens where t_address in (:contractAddress)")
    fun getTokenByContractAddress(contractAddress:List<String>): List<Tokens>

    @Query("UPDATE walletTokens SET balance =:newBalance WHERE t_pk =:id and walletId=:walletId")
    suspend fun updateBalanceWalletTokens(id: Int, newBalance: String , walletId: Int)

    @Query("SELECT t.t_pk,t.t_decimal,t.t_address, wt.balance as t_balance,t.t_last_price_change_impact,t.t_logouri,t.t_name,t.t_price,t.t_symbol,t.t_type,t.isEnable,t.isCustomTokens,t.tokenId,t.tokenPerPrice  FROM Tokens t LEFT JOIN walletTokens wt ON t.t_pk = wt.t_pk WHERE wt.walletId = :walletId ORDER BY t.t_symbol ASC")
    fun getTokensWithBalance(walletId: Int): List<Tokens>


    @Query("UPDATE Tokens SET t_balance = 0.0")
    suspend fun updateAllTokenBalanceZero()
}