package com.example.capstoneandroidexpert.core.data

import app.cash.turbine.test
import com.example.capstoneandroidexpert.core.data.source.local.LocalDataSource
import com.example.capstoneandroidexpert.core.data.source.local.entity.MovieEntity
import com.example.capstoneandroidexpert.core.data.source.remote.RemoteDataSource
import com.example.capstoneandroidexpert.core.domain.model.Movie
import com.example.capstoneandroidexpert.core.utils.AppExecutors
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.concurrent.Executor

class MovieRepositoryTest {

    private val localDataSource: LocalDataSource = mockk()
    private val remoteDataSource: RemoteDataSource = mockk()
    private val appExecutors: AppExecutors = mockk()
    private lateinit var repository: MovieRepository

    private val fakeEntity = MovieEntity(
        movieId = 1,
        title = "Test Movie",
        overview = "Overview",
        posterPath = "https://image.tmdb.org/t/p/w500/poster.jpg",
        backdropPath = "https://image.tmdb.org/t/p/original/backdrop.jpg",
        releaseDate = "2024-01-01",
        voteAverage = 8.5,
        isFavorite = true
    )

    private val expectedDomain = Movie(
        movieId = 1,
        title = "Test Movie",
        overview = "Overview",
        posterPath = "https://image.tmdb.org/t/p/w500/poster.jpg",
        backdropPath = "https://image.tmdb.org/t/p/original/backdrop.jpg",
        releaseDate = "2024-01-01",
        voteAverage = 8.5,
        isFavorite = true
    )

    @Before
    fun setUp() {
        repository = MovieRepository(remoteDataSource, localDataSource, appExecutors)
    }

    @Test
    fun `getFavoriteMovie returns mapped domain list`() = runTest {
        every { localDataSource.getFavoriteMovie() } returns flowOf(listOf(fakeEntity))

        repository.getFavoriteMovie().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(expectedDomain.movieId, result[0].movieId)
            assertEquals(expectedDomain.title, result[0].title)
            assertEquals(true, result[0].isFavorite)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getFavoriteMovie returns empty list when no favorites`() = runTest {
        every { localDataSource.getFavoriteMovie() } returns flowOf(emptyList())

        repository.getFavoriteMovie().test {
            val result = awaitItem()
            assertEquals(0, result.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setFavoriteMovie executes on disk IO executor`() {
        val directExecutor = Executor { it.run() }
        every { appExecutors.diskIO() } returns directExecutor
        every { localDataSource.setFavoriteMovie(any(), any()) } returns Unit

        repository.setFavoriteMovie(expectedDomain, true)

        verify { appExecutors.diskIO() }
        verify { localDataSource.setFavoriteMovie(any(), true) }
    }
}
