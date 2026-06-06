package com.example.capstoneandroidexpert.di

import com.example.capstoneandroidexpert.DetailViewModel
import com.example.capstoneandroidexpert.core.domain.usecase.MovieInteractor
import com.example.capstoneandroidexpert.core.domain.usecase.MovieUseCase
import com.example.capstoneandroidexpert.presentation.home.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val useCaseModule = module {
    factory<MovieUseCase> { MovieInteractor(get()) }
}

val viewModelModule = module {
    viewModel { MainViewModel(get(), get()) }
    viewModel { DetailViewModel(get()) }
}
