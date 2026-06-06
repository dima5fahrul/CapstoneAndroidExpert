package com.example.capstoneandroidexpert.core.utils

import com.example.capstoneandroidexpert.core.data.source.local.entity.MovieEntity
import com.example.capstoneandroidexpert.core.data.source.remote.response.MovieResponse
import com.example.capstoneandroidexpert.core.domain.model.Movie

object DataMapper {
    fun mapResponsesToEntities(input: List<MovieResponse>): List<MovieEntity> {
        val movieList = ArrayList<MovieEntity>()
        input.forEach {
            val movie = MovieEntity(
                movieId = it.id,
                title = it.title,
                overview = it.overview,
                posterPath = "https://image.tmdb.org/t/p/w500${it.posterPath}",
                backdropPath = "https://image.tmdb.org/t/p/original${it.backdropPath}",
                releaseDate = it.releaseDate ?: "N/A",
                voteAverage = it.voteAverage,
                isFavorite = false
            )
            movieList.add(movie)
        }
        return movieList
    }

    fun mapResponsesToDomain(input: List<MovieResponse>): List<Movie> =
        input.map {
            Movie(
                movieId = it.id,
                title = it.title,
                overview = it.overview,
                posterPath = "https://image.tmdb.org/t/p/w500${it.posterPath}",
                backdropPath = "https://image.tmdb.org/t/p/original${it.backdropPath}",
                releaseDate = it.releaseDate ?: "N/A",
                voteAverage = it.voteAverage,
                isFavorite = false
            )
        }

    fun mapEntitiesToDomain(input: List<MovieEntity>): List<Movie> =
        input.map {
            Movie(
                movieId = it.movieId,
                title = it.title,
                overview = it.overview,
                posterPath = it.posterPath,
                backdropPath = it.backdropPath,
                releaseDate = it.releaseDate,
                voteAverage = it.voteAverage,
                isFavorite = it.isFavorite
            )
        }

    fun mapDomainToEntity(input: Movie) = MovieEntity(
        movieId = input.movieId,
        title = input.title,
        overview = input.overview,
        posterPath = input.posterPath,
        backdropPath = input.backdropPath,
        releaseDate = input.releaseDate,
        voteAverage = input.voteAverage,
        isFavorite = input.isFavorite
    )
}
