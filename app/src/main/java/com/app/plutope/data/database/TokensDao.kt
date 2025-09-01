package com.app.plutope.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.app.plutope.model.TokenListImageModel
import com.app.plutope.model.Tokens
import com.app.plutope.model.WalletTokens
import com.app.plutope.utils.loge
import kotlinx.coroutines.flow.Flow

@Dao
interface TokensDao {

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<Tokens>)

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(token: Tokens)


    @Query("SELECT * FROM Tokens where t_address != '0x0000000000000000000000000000000000001010' and isEnable=:isEnable order by t_symbol asc")
    fun getTokenList(isEnable: Int): List<Tokens>

    @Query("SELECT * FROM Tokens where isEnable=0 order by t_symbol asc")
    fun getAllNotEnableTokenList(): List<Tokens>

    @Query("SELECT * FROM Tokens where t_address != '0x0000000000000000000000000000000000001010' order by t_symbol asc ")
    fun getAllTokenList(): List<Tokens>

    @Query("SELECT * FROM Tokens where t_address == '0x0000000000000000000000000000000000001010' or t_address =='' order by t_symbol asc ")
    fun getChainTokenList(): List<Tokens>

    @Update
    suspend fun updateTokens(tokens: List<Tokens>): Int


    @Update
    suspend fun updateToken(tokens: Tokens): Int

    @Query("Delete from Tokens where t_pk=:tokenId and isCustomTokens=1")
    suspend fun deleteCustomToken(tokenId: Int)

    @Query("Delete from Tokens where tokenId=:tokenId")
    suspend fun deleteToken(tokenId: String)


    //@Query("Update walletTokens set isEnable=:isEnable where t_pk=:tokenId and walletId=:walletId")
    @Query("Delete from walletTokens where t_pk=:tokenId and walletId=:walletId")
    suspend fun updateWalletToken(tokenId: Int, walletId: Int)


    @Transaction
    @Insert
    suspend fun insertAllWalletTokens(list: List<WalletTokens>)


    @Delete
    suspend fun deleteAllWalletTokens(list: List<WalletTokens>)

    @Delete
    suspend fun deleteWallet(walletToken: WalletTokens)

    @Transaction
    @Insert
    suspend fun insertWalletToken(walletToken: WalletTokens)


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
    suspend fun insertTokenImageTokens(list: List<TokenListImageModel>)

    @Insert
    suspend fun updateTokenImage(tokenListImageModel: TokenListImageModel)


    @Query("SELECT * FROM TokenImageModel")
    fun getAlleTokenImageList(): List<TokenListImageModel>

    @Query("SELECT * FROM walletTokens")
    fun getAllWalletTokens(): List<WalletTokens>

    @Query("SELECT count(*)  FROM walletTokens WHERE walletId = :walletId and t_pk=:tokenid")
    fun getCountTokenIdSpecificWallet(walletId: Int, tokenid: Int): Int


    @Query("SELECT * FROM Tokens where t_address in (:contractAddress)")
    fun getTokenByContractAddress(contractAddress: List<String>): List<Tokens>

    @Query("UPDATE walletTokens SET balance =:newBalance WHERE t_pk =:id and walletId=:walletId")
    suspend fun updateBalanceWalletTokens(id: Int, newBalance: String, walletId: Int)

    @Query("SELECT t.t_pk,t.t_decimal,t.t_address, wt.balance as t_balance,t.t_last_price_change_impact,t.t_logouri,t.t_name,t.t_price,t.t_symbol,t.t_type,t.isEnable,t.isCustomTokens,t.tokenId,t.tokenPerPrice  FROM Tokens t LEFT JOIN walletTokens wt ON t.t_pk = wt.t_pk WHERE wt.walletId = :walletId ORDER BY t.t_symbol ASC")
    fun getTokensWithBalance(walletId: Int): List<Tokens>


    @Query("UPDATE Tokens SET t_balance = 0.0")
    suspend fun updateAllTokenBalanceZero()


    @Transaction
    suspend fun updateAndInsertTokens(tokens: List<Tokens>) {
        val tokensToUpdate = mutableListOf<Tokens>()
        val tokensToInsert = mutableListOf<Tokens>()

        for (token in tokens) {
            if (tokenExists(token.t_symbol, token.t_type, token.tokenId)) {
                tokensToUpdate.add(token)
            } else {
                tokensToInsert.add(token)
            }
        }

        loge("tokensToInsert", "${tokensToInsert}")
        loge(
            "tokensToInsert2",
            "${tokensToUpdate.filter { it.tokenId == "polygon-ecosystem-token" }}"
        )

        if (tokensToUpdate.isNotEmpty()) {
            val lastToken =
                tokensToInsert.filter { it.t_symbol.lowercase() != "bnry" || it.tokenId.lowercase() != "binary-holdings" }
            updateTokens(lastToken)
        }

        if (tokensToInsert.isNotEmpty()) {
            val lastToken =
                tokensToInsert.filter { it.t_symbol.lowercase() != "bnry" || it.tokenId.lowercase() != "binary-holdings" }
            insertAll(lastToken)
        }
    }

    @Query("SELECT EXISTS(SELECT 1 FROM Tokens WHERE t_symbol = :symbol AND t_type =:type AND tokenId=:tokenId)")
    suspend fun tokenExists(/*name: String?,*/ symbol: String,
                            type: String?,
                            tokenId: String?
    ): Boolean


    @Update
    suspend fun updateAllWalletTokens(list: List<WalletTokens>)

    @Query("SELECT EXISTS(SELECT 1 FROM WalletTokens WHERE walletId = :walletId AND t_pk = :tPk)")
    suspend fun walletTokenExists(walletId: Int, tPk: Int): Boolean

    @Transaction
    suspend fun updateAndInsertWalletTokens(walletTokens: List<WalletTokens>) {
        loge("updateAndInsertWalletTokens", "${walletTokens}")
        val tokensToUpdate = mutableListOf<WalletTokens>()
        val tokensToInsert = mutableListOf<WalletTokens>()

        for (walletToken in walletTokens) {
            if (walletTokenExists(walletToken.walletId, walletToken.t_pk)) {
                tokensToUpdate.add(walletToken)
            } else {
                tokensToInsert.add(walletToken)
            }
        }

        loge("tokensToUpdate", "${tokensToUpdate}")
        loge("tokensToInsert", "${tokensToInsert}")

        if (tokensToUpdate.isNotEmpty()) {
            updateAllWalletTokens(tokensToUpdate)
        }

        if (tokensToInsert.isNotEmpty()) {
            insertAllWalletTokens(tokensToInsert)
        }
    }

    @Query("UPDATE Tokens SET tokenId =:tokenId,t_name = :newName, t_symbol = :newSymbol WHERE t_pk = :tokenPK")
    suspend fun updateTokenNameAndSymbol(
        tokenPK: Int,
        tokenId: String?,
        newName: String?,
        newSymbol: String?
    )


    @Transaction
    suspend fun updateWalletTokenId(oldToken: Tokens, newToken: Tokens) {
        updateTokenNameAndSymbol(
            oldToken.t_pk,
            newToken.tokenId,
            newToken.t_name,
            newToken.t_symbol
        )
    }

    @Delete
    suspend fun delete(walletToken: WalletTokens)


    @Query("SELECT * FROM walletTokens WHERE walletId = :walletId AND t_pk = :tPk LIMIT 1")
    suspend fun getWalletToken(walletId: Int, tPk: Int): WalletTokens?

    @Update
    suspend fun updateWalletToken(walletToken: WalletTokens)

    @Transaction
    suspend fun upsertWalletToken(walletToken: WalletTokens) {

        val existingToken = getWalletToken(walletToken.walletId, walletToken.t_pk)
        if (existingToken != null) {
            // Manually create a new object instead of using copy()
            val updatedToken = WalletTokens(
                walletId = existingToken.walletId,
                t_pk = existingToken.t_pk,
                isEnable = existingToken.isEnable, // Updated value
                balance = existingToken.balance, // Updated value
                id = existingToken.id // Keep existing ID
            )
            updateWalletToken(updatedToken)
        } else {
            // Insert new record
            insertWalletToken(walletToken)
        }
    }
}