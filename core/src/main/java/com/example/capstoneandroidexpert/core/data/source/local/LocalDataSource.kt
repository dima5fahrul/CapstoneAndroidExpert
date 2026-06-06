package com.example.capstoneandroidexpert.core.data.source.local

import com.example.capstoneandroidexpert.core.data.source.local.entity.MovieEntity
import com.example.capstoneandroidexpert.core.data.source.local.room.MovieDao
import kotlinx.coroutines.flow.Flow

class LocalDataSource(private val movieDao: MovieDao) {
    fun getAllMovie(): Flow<List<MovieEntity>> = movieDao.getAllMovie()
    fun getFavoriteMovie(): Flow<List<MovieEntity>> = movieDao.getFavoriteMovie()
    fun insertMovie(movieList: List<MovieEntity>) = movieDao.insertMovie(movieList)
    fun setFavoriteMovie(movie: MovieEntity, newState: Boolean) {
        movie.isFavorite = newState
        movieDao.updateFavoriteMovie(movie)
    }
}
