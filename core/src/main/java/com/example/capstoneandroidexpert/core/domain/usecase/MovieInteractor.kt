package com.example.capstoneandroidexpert.core.domain.usecase

import com.example.capstoneandroidexpert.core.domain.model.Movie
import com.example.capstoneandroidexpert.core.domain.repository.IMovieRepository

class MovieInteractor(private val movieRepository: IMovieRepository) : MovieUseCase {
    override fun getAllMovie() = movieRepository.getAllMovie()
    override fun getNowPlayingMovies() = movieRepository.getNowPlayingMovies()
    override fun getPopularMovies() = movieRepository.getPopularMovies()
    override fun getTopRatedMovies() = movieRepository.getTopRatedMovies()
    override fun getUpcomingMovies() = movieRepository.getUpcomingMovies()
    override fun searchMovies(query: String) = movieRepository.searchMovies(query)
    override fun getFavoriteMovie() = movieRepository.getFavoriteMovie()
    override fun setFavoriteMovie(movie: Movie, state: Boolean) = movieRepository.setFavoriteMovie(movie, state)
}
