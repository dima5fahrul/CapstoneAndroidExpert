package com.example.capstoneandroidexpert

import androidx.lifecycle.ViewModel
import com.example.capstoneandroidexpert.core.domain.model.Movie
import com.example.capstoneandroidexpert.core.domain.usecase.MovieUseCase

class DetailViewModel(private val movieUseCase: MovieUseCase) : ViewModel() {
    fun setFavoriteMovie(movie: Movie, newStatus: Boolean) =
        movieUseCase.setFavoriteMovie(movie, newStatus)
}
