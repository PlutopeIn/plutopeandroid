package com.app.plutope.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.app.plutope.model.CurrencyModel
import kotlinx.coroutines.flow.Flow

@Dao
interface CurrencyDao {

    @Query("SELECT * from currency")
    fun getAllCurrency(): List<CurrencyModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(currency: CurrencyModel)

    // @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCurrency(list: List<CurrencyModel>)

    @Query("SELECT * from currency where isSelected=1")
    fun getSelectedCurrency(): List<CurrencyModel>


    @Query("SELECT * from currency order by code asc")
    fun getAllCurrencyList(): Flow<List<CurrencyModel>>
}