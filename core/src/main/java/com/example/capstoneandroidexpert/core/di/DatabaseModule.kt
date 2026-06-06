package com.example.capstoneandroidexpert.core.di

import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.example.capstoneandroidexpert.core.data.source.local.room.MovieDatabase
import com.example.capstoneandroidexpert.core.utils.SecureKeyManager
import net.sqlcipher.database.SupportFactory
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    factory { get<MovieDatabase>().movieDao() }
    single {
        val passphrase = SecureKeyManager.getOrCreateDatabaseKey()
        val factory: SupportSQLiteOpenHelper.Factory = SupportFactory(passphrase)
        androidx.room.Room.databaseBuilder(
            androidContext(),
            MovieDatabase::class.java,
            "Movie.db"
        )
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration()
            .build()
    }
}
