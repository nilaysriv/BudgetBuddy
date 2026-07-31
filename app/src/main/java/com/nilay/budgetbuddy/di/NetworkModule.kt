package com.nilay.budgetbuddy.di

import com.nilay.budgetbuddy.data.local.TokenHolder
import com.nilay.budgetbuddy.data.remote.api.AuthService
import com.nilay.budgetbuddy.data.remote.api.BudgetService
import com.nilay.budgetbuddy.data.remote.api.CategoryService
import com.nilay.budgetbuddy.data.remote.api.TransactionService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.cdimascio.dotenv.dotenv
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideDotenv(): io.github.cdimascio.dotenv.Dotenv {
        return dotenv {
            directory = "/assets"
            filename = "env"
        }
    }

    @Provides
    @Singleton
    @BackendUrl
    fun provideBackendUrl(dotenv: io.github.cdimascio.dotenv.Dotenv): String {
        val url = dotenv["BACKEND_URL"] ?: "http://10.0.2.2:8080"
        return if (url.endsWith("/")) url else "$url/"
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(tokenHolder: TokenHolder): OkHttpClient {
        val authInterceptor = okhttp3.Interceptor { chain ->
            val token = tokenHolder.token
            val request = if (token != null) {
                chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
            } else {
                chain.request()
            }
            chain.proceed(request)
        }
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        @BackendUrl backendUrl: String,
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(backendUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideTransactionService(retrofit: Retrofit): TransactionService {
        return retrofit.create(TransactionService::class.java)
    }

    @Provides
    @Singleton
    fun provideCategoryService(retrofit: Retrofit): CategoryService {
        return retrofit.create(CategoryService::class.java)
    }

    @Provides
    @Singleton
    fun provideBudgetService(retrofit: Retrofit): BudgetService {
        return retrofit.create(BudgetService::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthService(retrofit: Retrofit): AuthService {
        return retrofit.create(AuthService::class.java)
    }
}

@javax.inject.Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BackendUrl
