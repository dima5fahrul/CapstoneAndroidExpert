package com.example.capstoneandroidexpert.core.data.source.remote

import android.util.Log
import com.example.capstoneandroidexpert.core.data.source.remote.network.ApiResponse
import com.example.capstoneandroidexpert.core.data.source.remote.network.ApiService
import com.example.capstoneandroidexpert.core.data.source.remote.response.MovieResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.concurrent.CancellationException

class RemoteDataSource(private val apiService: ApiService) {
    fun getAllMovie(): Flow<ApiResponse<List<MovieResponse>>> {
        return flow {
            try {
                val response = apiService.getMovies()
                val dataArray = response.results
                if (dataArray.isNotEmpty()) {
                    emit(ApiResponse.Success(response.results))
                } else {
                    emit(ApiResponse.Empty)
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                emit(ApiResponse.Error(e.toString()))
                Log.e("RemoteDataSource", e.toString())
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getNowPlayingMovies(): Flow<ApiResponse<List<MovieResponse>>> {
        return flow {
            try {
                val response = apiService.getNowPlayingMovies()
                if (response.results.isNotEmpty()) emit(ApiResponse.Success(response.results))
                else emit(ApiResponse.Empty)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                emit(ApiResponse.Error(e.toString()))
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getPopularMovies(): Flow<ApiResponse<List<MovieResponse>>> {
        return flow {
            try {
                val response = apiService.getPopularMovies()
                if (response.results.isNotEmpty()) emit(ApiResponse.Success(response.results))
                else emit(ApiResponse.Empty)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                emit(ApiResponse.Error(e.toString()))
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getTopRatedMovies(): Flow<ApiResponse<List<MovieResponse>>> {
        return flow {
            try {
                val response = apiService.getTopRatedMovies()
                if (response.results.isNotEmpty()) emit(ApiResponse.Success(response.results))
                else emit(ApiResponse.Empty)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                emit(ApiResponse.Error(e.toString()))
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getUpcomingMovies(): Flow<ApiResponse<List<MovieResponse>>> {
        return flow {
            try {
                val response = apiService.getUpcomingMovies()
                if (response.results.isNotEmpty()) emit(ApiResponse.Success(response.results))
                else emit(ApiResponse.Empty)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                emit(ApiResponse.Error(e.toString()))
            }
        }.flowOn(Dispatchers.IO)
    }

    fun searchMovies(query: String): Flow<ApiResponse<List<MovieResponse>>> {
        return flow {
            try {
                val response = apiService.searchMovies(query)
                if (response.results.isNotEmpty()) emit(ApiResponse.Success(response.results))
                else emit(ApiResponse.Empty)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                emit(ApiResponse.Error(e.toString()))
            }
        }.flowOn(Dispatchers.IO)
    }
}
