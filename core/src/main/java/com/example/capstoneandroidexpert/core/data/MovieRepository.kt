package com.example.capstoneandroidexpert.core.data

import com.example.capstoneandroidexpert.core.data.source.local.LocalDataSource
import com.example.capstoneandroidexpert.core.data.source.remote.RemoteDataSource
import com.example.capstoneandroidexpert.core.data.source.remote.network.ApiResponse
import com.example.capstoneandroidexpert.core.data.source.remote.response.MovieResponse
import com.example.capstoneandroidexpert.core.domain.model.Movie
import com.example.capstoneandroidexpert.core.domain.repository.IMovieRepository
import com.example.capstoneandroidexpert.core.utils.AppExecutors
import com.example.capstoneandroidexpert.core.utils.DataMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class MovieRepository(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource,
    private val appExecutors: AppExecutors
) : IMovieRepository {

    override fun getAllMovie(): Flow<Resource<List<Movie>>> =
        object : NetworkBoundResource<List<Movie>, List<MovieResponse>>() {
            override fun loadFromDB(): Flow<List<Movie>> {
                return localDataSource.getAllMovie().map {
                    DataMapper.mapEntitiesToDomain(it)
                }
            }

            override fun shouldFetch(data: List<Movie>?): Boolean =
                data.isNullOrEmpty()

            override suspend fun createCall(): Flow<ApiResponse<List<MovieResponse>>> =
                remoteDataSource.getAllMovie()

            override suspend fun saveCallResult(data: List<MovieResponse>) {
                val movieList = DataMapper.mapResponsesToEntities(data)
                localDataSource.insertMovie(movieList)
            }
        }.asFlow()

    override fun getNowPlayingMovies(): Flow<Resource<List<Movie>>> = flow {
        emit(Resource.Loading())
        when (val apiResponse = remoteDataSource.getNowPlayingMovies().first()) {
            is ApiResponse.Success -> emit(Resource.Success(DataMapper.mapResponsesToDomain(apiResponse.data)))
            is ApiResponse.Empty -> emit(Resource.Success(emptyList()))
            is ApiResponse.Error -> emit(Resource.Error(apiResponse.errorMessage))
        }
    }

    override fun getPopularMovies(): Flow<Resource<List<Movie>>> = flow {
        emit(Resource.Loading())
        when (val apiResponse = remoteDataSource.getPopularMovies().first()) {
            is ApiResponse.Success -> emit(Resource.Success(DataMapper.mapResponsesToDomain(apiResponse.data)))
            is ApiResponse.Empty -> emit(Resource.Success(emptyList()))
            is ApiResponse.Error -> emit(Resource.Error(apiResponse.errorMessage))
        }
    }

    override fun getTopRatedMovies(): Flow<Resource<List<Movie>>> = flow {
        emit(Resource.Loading())
        when (val apiResponse = remoteDataSource.getTopRatedMovies().first()) {
            is ApiResponse.Success -> emit(Resource.Success(DataMapper.mapResponsesToDomain(apiResponse.data)))
            is ApiResponse.Empty -> emit(Resource.Success(emptyList()))
            is ApiResponse.Error -> emit(Resource.Error(apiResponse.errorMessage))
        }
    }

    override fun getUpcomingMovies(): Flow<Resource<List<Movie>>> = flow {
        emit(Resource.Loading())
        when (val apiResponse = remoteDataSource.getUpcomingMovies().first()) {
            is ApiResponse.Success -> emit(Resource.Success(DataMapper.mapResponsesToDomain(apiResponse.data)))
            is ApiResponse.Empty -> emit(Resource.Success(emptyList()))
            is ApiResponse.Error -> emit(Resource.Error(apiResponse.errorMessage))
        }
    }

    override fun searchMovies(query: String): Flow<Resource<List<Movie>>> = flow {
        emit(Resource.Loading())
        when (val apiResponse = remoteDataSource.searchMovies(query).first()) {
            is ApiResponse.Success -> emit(Resource.Success(DataMapper.mapResponsesToDomain(apiResponse.data)))
            is ApiResponse.Empty -> emit(Resource.Success(emptyList()))
            is ApiResponse.Error -> emit(Resource.Error(apiResponse.errorMessage))
        }
    }

    override fun getFavoriteMovie(): Flow<List<Movie>> {
        return localDataSource.getFavoriteMovie().map {
            DataMapper.mapEntitiesToDomain(it)
        }
    }

    override fun setFavoriteMovie(movie: Movie, state: Boolean) {
        val movieEntity = DataMapper.mapDomainToEntity(movie)
        appExecutors.diskIO().execute { localDataSource.setFavoriteMovie(movieEntity, state) }
    }
}
