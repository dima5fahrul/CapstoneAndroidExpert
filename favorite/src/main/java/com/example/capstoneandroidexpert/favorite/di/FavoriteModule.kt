package com.example.capstoneandroidexpert.favorite.di

import com.example.capstoneandroidexpert.favorite.FavoriteViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val favoriteModule = module {
    viewModel { FavoriteViewModel(get()) }
}
