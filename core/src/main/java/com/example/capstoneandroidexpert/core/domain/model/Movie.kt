package com.example.capstoneandroidexpert.core.domain.model

import java.io.Serializable

data class Movie(
    val movieId: Int,
    val title: String,
    val overview: String,
    val posterPath: String,
    val backdropPath: String,
    val releaseDate: String,
    val voteAverage: Double,
    val isFavorite: Boolean
) : Serializable
