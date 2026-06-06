package com.example.capstoneandroidexpert.core.utils

import com.example.capstoneandroidexpert.core.data.source.local.entity.MovieEntity
import com.example.capstoneandroidexpert.core.data.source.remote.response.MovieResponse
import com.example.capstoneandroidexpert.core.domain.model.Movie
import org.junit.Assert.assertEquals
import org.junit.Test

class DataMapperTest {

    private val fakeResponse = MovieResponse(
        id = 1,
        title = "Test Movie",
        overview = "Overview",
        posterPath = "/poster.jpg",
        backdropPath = "/backdrop.jpg",
        releaseDate = "2024-01-01",
        voteAverage = 8.5
    )

    private val fakeEntity = MovieEntity(
        movieId = 1,
        title = "Test Movie",
        overview = "Overview",
        posterPath = "https://image.tmdb.org/t/p/w500/poster.jpg",
        backdropPath = "https://image.tmdb.org/t/p/original/backdrop.jpg",
        releaseDate = "2024-01-01",
        voteAverage = 8.5,
        isFavorite = false
    )

    private val fakeDomain = Movie(
        movieId = 1,
        title = "Test Movie",
        overview = "Overview",
        posterPath = "https://image.tmdb.org/t/p/w500/poster.jpg",
        backdropPath = "https://image.tmdb.org/t/p/original/backdrop.jpg",
        releaseDate = "2024-01-01",
        voteAverage = 8.5,
        isFavorite = false
    )

    @Test
    fun `mapResponsesToEntities maps all fields correctly`() {
        val result = DataMapper.mapResponsesToEntities(listOf(fakeResponse))

        assertEquals(1, result.size)
        assertEquals(fakeResponse.id, result[0].movieId)
        assertEquals(fakeResponse.title, result[0].title)
        assertEquals("https://image.tmdb.org/t/p/w500/poster.jpg", result[0].posterPath)
        assertEquals("https://image.tmdb.org/t/p/original/backdrop.jpg", result[0].backdropPath)
        assertEquals(false, result[0].isFavorite)
    }

    @Test
    fun `mapResponsesToEntities handles null posterPath`() {
        val responseWithNullPoster = fakeResponse.copy(posterPath = null)
        val result = DataMapper.mapResponsesToEntities(listOf(responseWithNullPoster))

        assertEquals("https://image.tmdb.org/t/p/w500null", result[0].posterPath)
    }

    @Test
    fun `mapResponsesToEntities handles null releaseDate`() {
        val responseNullDate = fakeResponse.copy(releaseDate = null)
        val result = DataMapper.mapResponsesToEntities(listOf(responseNullDate))

        assertEquals("N/A", result[0].releaseDate)
    }

    @Test
    fun `mapResponsesToEntities returns empty list for empty input`() {
        val result = DataMapper.mapResponsesToEntities(emptyList())
        assertEquals(0, result.size)
    }

    @Test
    fun `mapResponsesToDomain maps all fields correctly`() {
        val result = DataMapper.mapResponsesToDomain(listOf(fakeResponse))

        assertEquals(1, result.size)
        assertEquals(fakeResponse.id, result[0].movieId)
        assertEquals(fakeResponse.title, result[0].title)
        assertEquals("https://image.tmdb.org/t/p/w500/poster.jpg", result[0].posterPath)
        assertEquals(false, result[0].isFavorite)
        assertEquals("2024-01-01", result[0].releaseDate)
        assertEquals(8.5, result[0].voteAverage, 0.001)
    }

    @Test
    fun `mapEntitiesToDomain maps all fields correctly`() {
        val result = DataMapper.mapEntitiesToDomain(listOf(fakeEntity))

        assertEquals(1, result.size)
        assertEquals(fakeEntity.movieId, result[0].movieId)
        assertEquals(fakeEntity.title, result[0].title)
        assertEquals(fakeEntity.isFavorite, result[0].isFavorite)
        assertEquals(fakeEntity.posterPath, result[0].posterPath)
        assertEquals(fakeEntity.backdropPath, result[0].backdropPath)
        assertEquals(fakeEntity.releaseDate, result[0].releaseDate)
        assertEquals(fakeEntity.voteAverage, result[0].voteAverage, 0.001)
    }

    @Test
    fun `mapDomainToEntity maps all fields correctly`() {
        val result = DataMapper.mapDomainToEntity(fakeDomain)

        assertEquals(fakeDomain.movieId, result.movieId)
        assertEquals(fakeDomain.title, result.title)
        assertEquals(fakeDomain.isFavorite, result.isFavorite)
        assertEquals(fakeDomain.posterPath, result.posterPath)
        assertEquals(fakeDomain.backdropPath, result.backdropPath)
        assertEquals(fakeDomain.releaseDate, result.releaseDate)
        assertEquals(fakeDomain.voteAverage, result.voteAverage, 0.001)
    }
}
