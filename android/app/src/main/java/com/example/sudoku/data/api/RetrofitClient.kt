package com.example.sudoku.data.api

import com.example.sudoku.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton providing the configured [SudokuApiService] instance.
 *
 * The base URL is set via BuildConfig:
 * - Debug: emulator localhost (http://10.0.2.2:8080/)
 * - Release: Cloud Run production URL
 *
 * Every request includes an `X-Firebase-AppCheck` header with a fresh
 * App Check token so the backend can verify request authenticity.
 */
object RetrofitClient {

    private const val BASE_URL = BuildConfig.BACKEND_URL

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**
     * OkHttp interceptor that attaches a Firebase App Check token
     * to every outgoing request as the `X-Firebase-AppCheck` header.
     */
    private val appCheckInterceptor = Interceptor { chain ->
        val tokenResult = runBlocking {
            try {
                Firebase.appCheck.getAppCheckToken(false).await()
            } catch (e: Exception) {
                null
            }
        }

        val requestBuilder = chain.request().newBuilder()
        tokenResult?.token?.let { token ->
            requestBuilder.addHeader("X-Firebase-AppCheck", token)
        }
        chain.proceed(requestBuilder.build())
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(appCheckInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: SudokuApiService = retrofit.create(SudokuApiService::class.java)
}
