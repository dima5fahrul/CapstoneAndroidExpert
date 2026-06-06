package com.example.capstoneandroidexpert.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.capstoneandroidexpert.core.domain.usecase.MovieUseCase
import com.example.capstoneandroidexpert.core.utils.SettingPreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest

class MainViewModel(private val movieUseCase: MovieUseCase, private val pref: SettingPreferences) : ViewModel() {

    fun getThemeSettings() = pref.getThemeSetting().asLiveData()

    private val currentCategory = MutableStateFlow("All")
    private val searchQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val movies = currentCategory.flatMapLatest { category ->
        when (category) {
            "Now Playing" -> movieUseCase.getNowPlayingMovies()
            "Popular" -> movieUseCase.getPopularMovies()
            "Top Rated" -> movieUseCase.getTopRatedMovies()
            "Upcoming" -> movieUseCase.getUpcomingMovies()
            "Search" -> searchQuery.flatMapLatest { query ->
                movieUseCase.searchMovies(query)
            }
            else -> movieUseCase.getAllMovie()
        }
    }.asLiveData()

    fun setCategory(category: String) {
        currentCategory.value = category
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
        currentCategory.value = "Search"
    }

    fun clearSearch() {
        searchQuery.value = ""
        currentCategory.value = "All"
    }
}
