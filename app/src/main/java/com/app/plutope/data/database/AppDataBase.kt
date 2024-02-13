package com.app.plutope.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.app.plutope.model.ContactModel
import com.app.plutope.model.CurrencyModel
import com.app.plutope.model.TokenListImageModel
import com.app.plutope.model.Tokens
import com.app.plutope.model.WalletTokens
import com.app.plutope.model.Wallets


@Database(
    entities = [Tokens::class, Wallets::class, WalletTokens::class, CurrencyModel::class, TokenListImageModel::class, ContactModel::class],
    version = 6,
    exportSchema = false
)
abstract class AppDataBase : RoomDatabase() {
    abstract fun walletDao(): WalletDao
    abstract fun tokensDao(): TokensDao
    abstract fun walletTokensDao(): WalletTokensDao
    abstract fun currencyDao(): CurrencyDao
    abstract fun contactDao(): ContactDao



    companion object {
        @Volatile
        private var instance: AppDataBase? = null

        //  var factory: SupportFactory = SupportFactory(BuildConfig.ENCRYPTION_KEY.toByteArray())
        fun getDatabase(context: Context, dbName: String? = "PlutoPe-db"): AppDataBase =
            instance ?: synchronized(this) {
                instance ?: buildDatabase(context, dbName).also {
                    instance = it
                }
            }

        private fun buildDatabase(appContext: Context, dbName: String?) =
            Room.databaseBuilder(appContext, AppDataBase::class.java, dbName)
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                // .openHelperFactory(factory)
                .build()


        fun clearAllTable(context: Context) {
            instance?.clearAllTables()
            // instance?.close()

            // AppDataBase.getDatabase(context)

            val newDbName = "PlutoPe-db"
            instance = buildDatabase(context, newDbName)


        }

    }
}
