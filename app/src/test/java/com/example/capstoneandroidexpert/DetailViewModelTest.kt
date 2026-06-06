package com.example.capstoneandroidexpert

import com.example.capstoneandroidexpert.core.domain.model.Movie
import com.example.capstoneandroidexpert.core.domain.usecase.MovieUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class DetailViewModelTest {

    private val movieUseCase: MovieUseCase = mockk()
    private lateinit var viewModel: DetailViewModel

    private val fakeMovie = Movie(
        movieId = 1,
        title = "Test Movie",
        overview = "Overview",
        posterPath = "https://image.tmdb.org/t/p/w500/poster.jpg",
        backdropPath = "https://image.tmdb.org/t/p/original/backdrop.jpg",
        releaseDate = "2024-01-01",
        voteAverage = 8.0,
        isFavorite = false
    )

    @Before
    fun setUp() {
        viewModel = DetailViewModel(movieUseCase)
    }

    @Test
    fun `setFavoriteMovie with true delegates to useCase`() {
        every { movieUseCase.setFavoriteMovie(fakeMovie, true) } returns Unit

        viewModel.setFavoriteMovie(fakeMovie, true)

        verify { movieUseCase.setFavoriteMovie(fakeMovie, true) }
    }

    @Test
    fun `setFavoriteMovie with false delegates to useCase`() {
        every { movieUseCase.setFavoriteMovie(fakeMovie, false) } returns Unit

        viewModel.setFavoriteMovie(fakeMovie, false)

        verify { movieUseCase.setFavoriteMovie(fakeMovie, false) }
    }
}
