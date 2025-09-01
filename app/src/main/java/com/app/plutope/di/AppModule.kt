package com.app.plutope.di

import android.content.Context
import com.app.plutope.BuildConfig
import com.app.plutope.data.database.AppDataBase
import com.app.plutope.data.database.ContactDao
import com.app.plutope.data.database.CurrencyDao
import com.app.plutope.data.database.TokensDao
import com.app.plutope.data.database.WalletDao
import com.app.plutope.data.database.WalletTokensDao
import com.app.plutope.data.repository.ContactRepo
import com.app.plutope.data.repository.CurrencyRepo
import com.app.plutope.data.repository.DashboardRepo
import com.app.plutope.data.repository.TokensRepo
import com.app.plutope.data.repository.WalletRepo
import com.app.plutope.network.ApiHelper
import com.app.plutope.network.ApiHelperImpl
import com.app.plutope.network.ApiService
import com.app.plutope.network.NetworkConnectionInterceptor
import com.app.plutope.ui.base.App
import com.app.plutope.utils.constant.cryptoCurrencyUrl
import com.app.plutope.utils.extras.PreferenceHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    fun provideBaseUrl() = cryptoCurrencyUrl

    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient, baseUrl: String): Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .build()

    @Singleton
    @Provides
    fun provideOkHttpClient() = if (BuildConfig.DEBUG) {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.HEADERS
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        OkHttpClient.Builder()
            .addInterceptor(providesOkhttpInterceptor())
            .addInterceptor(loggingInterceptor)
            .addNetworkInterceptor(
                HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS).setLevel(
                    HttpLoggingInterceptor.Level.BODY
                )
            )
            .addInterceptor(NetworkConnectionInterceptor(App.getContext()))
            .connectTimeout(10, TimeUnit.MINUTES)
            .readTimeout(5, TimeUnit.MINUTES)
            .writeTimeout(5, TimeUnit.MINUTES)
            .build()
    } else {
        OkHttpClient
            .Builder()
            .connectTimeout(10, TimeUnit.MINUTES)
            .addInterceptor(providesOkhttpInterceptor())
            .addInterceptor(NetworkConnectionInterceptor(App.getContext()))
            .connectTimeout(10, TimeUnit.MINUTES)
            .readTimeout(5, TimeUnit.MINUTES)
            .writeTimeout(5, TimeUnit.MINUTES)
            .build()
    }


    @Provides
    fun providesOkhttpInterceptor(): Interceptor {
        return Interceptor { chain: Interceptor.Chain ->
            val original: Request = chain.request()
            val requestBuilder: Request.Builder = original.newBuilder()
                .addHeader("Accept", "application/json")
            val request: Request = requestBuilder.build()
            chain.proceed(request)
        }
    }

    @Provides
    @Singleton
    fun provideApiHelper(apiHelper: ApiHelperImpl): ApiHelper = apiHelper

    @Provides
    fun provideApiService(retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)

    @Singleton
    @Provides
    fun providePrefHelper(@ApplicationContext context: Context): PreferenceHelper =
        PreferenceHelper(context)

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext appContext: Context) =
        AppDataBase.getDatabase(appContext)

    @Singleton
    @Provides
    fun provideWalletRepo(
        walletDao: WalletDao
    ) =
        WalletRepo(walletDao)

    @Singleton
    @Provides
    fun provideDashboardRepo(
        apiHelper: ApiHelper
    ) =
        DashboardRepo(apiHelper)

    @Singleton
    @Provides
    fun provideWalletDao(database: AppDataBase): WalletDao {
        return database.walletDao()
    }


    @Singleton
    @Provides
    fun provideTokenRepo(
        tokenDao: TokensDao,
        apiHelper: ApiHelper
    ) =
        TokensRepo(tokenDao, apiHelper)

    @Singleton
    @Provides
    fun provideTokenDao(database: AppDataBase): TokensDao {
        return database.tokensDao()
    }

    @Singleton
    @Provides
    fun provideWalletTokensDao(database: AppDataBase): WalletTokensDao {
        return database.walletTokensDao()
    }

    @Singleton
    @Provides
    fun provideCurrencyDao(database: AppDataBase): CurrencyDao {
        return database.currencyDao()
    }

    @Singleton
    @Provides
    fun provideContactDao(database: AppDataBase): ContactDao {
        return database.contactDao()
    }


    @Singleton
    @Provides
    fun provideCurrencyRepo(
        apiHelper: ApiHelper,
        currencyDao: CurrencyDao
    ) =
        CurrencyRepo(apiHelper, currencyDao)

    @Singleton
    @Provides
    fun provideContactRepo(
        contactDao: ContactDao
    ) = ContactRepo(contactDao)
}