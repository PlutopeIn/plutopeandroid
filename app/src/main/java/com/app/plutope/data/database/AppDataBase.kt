package com.app.plutope.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.app.plutope.model.ContactModel
import com.app.plutope.model.CurrencyModel
import com.app.plutope.model.TokenListImageModel
import com.app.plutope.model.Tokens
import com.app.plutope.model.WalletTokens
import com.app.plutope.model.Wallets


@Database(
    entities = [Tokens::class, Wallets::class, WalletTokens::class, CurrencyModel::class, TokenListImageModel::class, ContactModel::class],
    version = 7,
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
                //.fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .addMigrations(MIGRATION_6_7)
                // .openHelperFactory(factory)
                .build()


        fun clearAllTable(context: Context) {
            instance?.clearAllTables()
            // instance?.close()

            // AppDataBase.getDatabase(context)

            val newDbName = "PlutoPe-db"
            instance = buildDatabase(context, newDbName)


        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {

                database.execSQL(
                    """
            CREATE TABLE Tokens_new (
                t_pk INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                t_decimal INTEGER NOT NULL DEFAULT 0,
                t_address TEXT NOT NULL DEFAULT '',
                t_balance TEXT NOT NULL DEFAULT '',
                t_last_price_change_impact TEXT NOT NULL DEFAULT '',
                t_logouri TEXT NOT NULL DEFAULT '',
                t_name TEXT NOT NULL DEFAULT '',
                t_price TEXT NOT NULL DEFAULT '',
                t_symbol TEXT NOT NULL DEFAULT '',
                t_type TEXT NOT NULL DEFAULT '',
                isEnable INTEGER NOT NULL DEFAULT 0,
                isCustomTokens INTEGER NOT NULL DEFAULT 0,
                tokenId TEXT NOT NULL DEFAULT '',
                tokenPerPrice TEXT NOT NULL DEFAULT ''
            )
        """
                )

                // Copy the data from the old table to the new one
                database.execSQL(
                    """
            INSERT INTO Tokens_new (t_pk, t_decimal, t_address, t_balance, t_last_price_change_impact,
                t_logouri, t_name, t_price, t_symbol, t_type, isEnable, isCustomTokens, tokenId, tokenPerPrice)
            SELECT t_pk,
                   COALESCE(t_decimal, 0),
                   COALESCE(t_address, ''),
                   COALESCE(t_balance, ''),
                   COALESCE(t_last_price_change_impact, ''),
                   COALESCE(t_logouri, ''),
                   COALESCE(t_name, ''),
                   COALESCE(t_price, ''),
                   COALESCE(t_symbol, ''),
                   COALESCE(t_type, ''),
                   COALESCE(isEnable, 0),
                   COALESCE(isCustomTokens, 0),
                   COALESCE(tokenId, ''),
                   COALESCE(tokenPerPrice, '')
            FROM Tokens
        """
                )

                // Remove the old table
                database.execSQL("DROP TABLE Tokens")

                // Rename the new table to the old table name
                database.execSQL("ALTER TABLE Tokens_new RENAME TO Tokens")


            }
        }

    }
}
