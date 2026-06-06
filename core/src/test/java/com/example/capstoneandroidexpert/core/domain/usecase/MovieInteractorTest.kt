package com.example.capstoneandroidexpert.core.domain.usecase

import app.cash.turbine.test
import com.example.capstoneandroidexpert.core.data.Resource
import com.example.capstoneandroidexpert.core.domain.model.Movie
import com.example.capstoneandroidexpert.core.domain.repository.IMovieRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class MovieInteractorTest {

    private val repository: IMovieRepository = mockk()
    private lateinit var interactor: MovieInteractor

    private val fakeMovie = Movie(1, "Title", "Overview", "poster", "backdrop", "2024-01-01", 7.5, false)
    private val fakeMovieList = listOf(fakeMovie)

    @Before
    fun setUp() {
        interactor = MovieInteractor(repository)
    }

    @Test
    fun `getAllMovie delegates to repository`() = runTest {
        every { repository.getAllMovie() } returns flowOf(Resource.Success(fakeMovieList))

        interactor.getAllMovie().test {
            val item = awaitItem()
            assert(item is Resource.Success)
            assertEquals(fakeMovieList, item.data)
            cancelAndIgnoreRemainingEvents()
        }

        verify { repository.getAllMovie() }
    }

    @Test
    fun `getNowPlayingMovies delegates to repository`() = runTest {
        every { repository.getNowPlayingMovies() } returns flowOf(Resource.Success(fakeMovieList))

        interactor.getNowPlayingMovies().test {
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        verify { repository.getNowPlayingMovies() }
    }

    @Test
    fun `getPopularMovies delegates to repository`() = runTest {
        every { repository.getPopularMovies() } returns flowOf(Resource.Success(fakeMovieList))

        interactor.getPopularMovies().test {
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        verify { repository.getPopularMovies() }
    }

    @Test
    fun `getFavoriteMovie delegates to repository`() = runTest {
        every { repository.getFavoriteMovie() } returns flowOf(fakeMovieList)

        interactor.getFavoriteMovie().test {
            val item = awaitItem()
            assertEquals(fakeMovieList, item)
            cancelAndIgnoreRemainingEvents()
        }

        verify { repository.getFavoriteMovie() }
    }

    @Test
    fun `setFavoriteMovie delegates to repository`() {
        every { repository.setFavoriteMovie(fakeMovie, true) } returns Unit

        interactor.setFavoriteMovie(fakeMovie, true)

        verify { repository.setFavoriteMovie(fakeMovie, true) }
    }

    @Test
    fun `searchMovies delegates to repository with query`() = runTest {
        every { repository.searchMovies("batman") } returns flowOf(Resource.Success(fakeMovieList))

        interactor.searchMovies("batman").test {
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        verify { repository.searchMovies("batman") }
    }
}
