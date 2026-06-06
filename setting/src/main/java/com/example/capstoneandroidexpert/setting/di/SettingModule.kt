package com.example.capstoneandroidexpert.setting.di

import com.example.capstoneandroidexpert.setting.SettingViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val settingModule = module {
    viewModel { SettingViewModel(get()) }
}
