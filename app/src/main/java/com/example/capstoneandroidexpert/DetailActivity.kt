package com.example.capstoneandroidexpert

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.capstoneandroidexpert.core.domain.model.Movie
import com.example.capstoneandroidexpert.databinding.ActivityDetailBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class DetailActivity : AppCompatActivity() {

    private val detailViewModel: DetailViewModel by viewModel()
    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val detailMovie = intent.getSerializableExtra(MainActivity.EXTRA_DATA) as? Movie
        showDetailMovie(detailMovie)
    }

    private fun showDetailMovie(detailMovie: Movie?) {
        detailMovie?.let {
            supportActionBar?.title = it.title
            binding.tvDetailTitle.text = it.title
            binding.tvDetailReleaseDate.text = it.releaseDate
            binding.tvDetailRating.text = getString(R.string.rating_format, it.voteAverage.toString())
            binding.ratingBar.rating = (it.voteAverage / 2).toFloat()
            binding.tvDetailOverview.text = it.overview

            Glide.with(this)
                .load(it.posterPath)
                .into(binding.ivDetailPoster)

            Glide.with(this)
                .load(it.backdropPath)
                .into(binding.ivBackdrop)

            var statusFavorite = it.isFavorite
            setStatusFavorite(statusFavorite)
            binding.fabFavorite.setOnClickListener {
                statusFavorite = !statusFavorite
                detailViewModel.setFavoriteMovie(detailMovie, statusFavorite)
                setStatusFavorite(statusFavorite)
            }
        }
    }

    private fun setStatusFavorite(statusFavorite: Boolean) {
        if (statusFavorite) {
            binding.fabFavorite.setImageDrawable(ContextCompat.getDrawable(this, com.example.capstoneandroidexpert.core.R.drawable.ic_favorite))
        } else {
            binding.fabFavorite.setImageDrawable(ContextCompat.getDrawable(this, com.example.capstoneandroidexpert.core.R.drawable.ic_favorite_border))
        }
    }
}
