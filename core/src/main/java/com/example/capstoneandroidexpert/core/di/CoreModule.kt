package com.example.capstoneandroidexpert.core.di

import com.example.capstoneandroidexpert.core.data.MovieRepository
import com.example.capstoneandroidexpert.core.data.source.local.LocalDataSource
import com.example.capstoneandroidexpert.core.data.source.remote.RemoteDataSource
import com.example.capstoneandroidexpert.core.domain.repository.IMovieRepository
import com.example.capstoneandroidexpert.core.utils.AppExecutors
import com.example.capstoneandroidexpert.core.utils.SettingPreferences
import org.koin.dsl.module

val repositoryModule = module {
    single { RemoteDataSource(get()) }
    single { LocalDataSource(get()) }
    factory { AppExecutors() }
    single<IMovieRepository> {
        MovieRepository(
            get(),
            get(),
            get()
        )
    }
}

val preferencesModule = module {
    single { SettingPreferences(get()) }
}
