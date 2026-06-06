package com.example.capstoneandroidexpert

import android.app.Application
import com.example.capstoneandroidexpert.core.di.repositoryModule
import com.example.capstoneandroidexpert.core.di.preferencesModule
import com.example.capstoneandroidexpert.di.useCaseModule
import com.example.capstoneandroidexpert.di.viewModelModule
import com.example.capstoneandroidexpert.core.di.networkModule
import com.example.capstoneandroidexpert.core.di.databaseModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.NONE)
            androidContext(this@MyApplication)
            modules(
                listOf(
                    databaseModule,
                    networkModule,
                    repositoryModule,
                    preferencesModule,
                    useCaseModule,
                    viewModelModule
                )
            )
        }
    }
}
