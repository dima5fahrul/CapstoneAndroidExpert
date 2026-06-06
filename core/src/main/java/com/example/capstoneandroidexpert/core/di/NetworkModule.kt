package com.example.capstoneandroidexpert.core.di

import com.example.capstoneandroidexpert.core.BuildConfig
import com.example.capstoneandroidexpert.core.data.source.remote.network.ApiService
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val networkModule = module {
    single {
        val certificatePinner = CertificatePinner.Builder()
            .add("api.themoviedb.org", "sha256/QfyoR20v8hyYX7L+ikLzM/euPGSDl67gFFcor/sROMs=")
            .add("api.themoviedb.org", "sha256/G9LNNAql897egYsabashkzUCTEJkWBzgoEtk8X/678c=")
            .build()

        OkHttpClient.Builder()
            .certificatePinner(certificatePinner)
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer ${BuildConfig.TMDB_API_TOKEN}")
                    .addHeader("accept", "application/json")
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build()
    }
    single {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(get())
            .build()
        retrofit.create(ApiService::class.java)
    }
}
