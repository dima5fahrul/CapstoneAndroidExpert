package com.example.capstoneandroidexpert.presentation.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capstoneandroidexpert.DetailActivity
import com.example.capstoneandroidexpert.MainActivity
import com.example.capstoneandroidexpert.R
import com.example.capstoneandroidexpert.core.data.Resource
import com.example.capstoneandroidexpert.core.ui.MovieAdapter
import com.example.capstoneandroidexpert.databinding.FragmentHomeBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : Fragment() {

    private val homeViewModel: MainViewModel by viewModel()
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val movieAdapter = MovieAdapter()
        movieAdapter.onItemClick = { selectedData ->
            val intent = Intent(requireContext(), DetailActivity::class.java)
            intent.putExtra(MainActivity.EXTRA_DATA, selectedData)
            startActivity(intent)
        }

        homeViewModel.movies.observe(viewLifecycleOwner) { movies ->
            if (movies != null) {
                when (movies) {
                    is Resource.Loading -> {
                        binding.layoutLoading.visibility = View.VISIBLE
                        binding.recyclerViewMovies.visibility = View.GONE
                        binding.layoutError.visibility = View.GONE
                        binding.layoutEmpty.visibility = View.GONE
                    }
                    is Resource.Success -> {
                        binding.layoutLoading.visibility = View.GONE
                        if (movies.data.isNullOrEmpty()) {
                            binding.layoutEmpty.visibility = View.VISIBLE
                            binding.recyclerViewMovies.visibility = View.GONE
                        } else {
                            movieAdapter.setData(movies.data)
                            binding.recyclerViewMovies.visibility = View.VISIBLE
                            binding.layoutEmpty.visibility = View.GONE
                        }
                    }
                    is Resource.Error -> {
                        binding.layoutLoading.visibility = View.GONE
                        binding.layoutError.visibility = View.VISIBLE
                        binding.tvErrorMessage.text = movies.message ?: getString(R.string.error_message_generic)
                    }
                }
            }
        }

        binding.chipGroupCategories.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chip = view.findViewById<com.google.android.material.chip.Chip>(checkedIds[0])
                homeViewModel.setCategory(chip.text.toString())
            }
        }

        binding.etSearch.doOnTextChanged { text, _, _, _ ->
            if (text.isNullOrEmpty()) {
                homeViewModel.clearSearch()
                binding.chipAll.isChecked = true
                binding.ivClearSearch.visibility = View.GONE
            } else {
                binding.ivClearSearch.visibility = View.VISIBLE
            }
        }

        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.etSearch.text.toString()
                if (query.isNotEmpty()) {
                    homeViewModel.setSearchQuery(query)
                }
                true
            } else {
                false
            }
        }

        binding.ivClearSearch.setOnClickListener {
            binding.etSearch.text?.clear()
        }

        with(binding.recyclerViewMovies) {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = movieAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
