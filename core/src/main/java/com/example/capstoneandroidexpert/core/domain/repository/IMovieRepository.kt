package com.example.capstoneandroidexpert.core.domain.repository

import com.example.capstoneandroidexpert.core.data.Resource
import com.example.capstoneandroidexpert.core.domain.model.Movie
import kotlinx.coroutines.flow.Flow

interface IMovieRepository {
    fun getAllMovie(): Flow<Resource<List<Movie>>>
    fun getNowPlayingMovies(): Flow<Resource<List<Movie>>>
    fun getPopularMovies(): Flow<Resource<List<Movie>>>
    fun getTopRatedMovies(): Flow<Resource<List<Movie>>>
    fun getUpcomingMovies(): Flow<Resource<List<Movie>>>
    fun searchMovies(query: String): Flow<Resource<List<Movie>>>
    fun getFavoriteMovie(): Flow<List<Movie>>
    fun setFavoriteMovie(movie: Movie, state: Boolean)
}
