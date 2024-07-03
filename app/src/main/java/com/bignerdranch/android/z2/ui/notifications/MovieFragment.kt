package com.bignerdranch.android.z2.ui.notifications

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bignerdranch.android.z2.ui.models.MovieAPI
import com.bignerdranch.android.z2.ui.adapters.MovieAdapter
import com.bignerdranch.android.z2.ui.models.Movie
import com.example.android.z2.R
import com.example.android.z2.databinding.FragmentMovieBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MovieFragment : Fragment(), MovieAdapter.onItemClickMovie {

    private lateinit var binding: FragmentMovieBinding
    private lateinit var context: Context
    private lateinit var adapter: MovieAdapter
    private var allMovies: List<Movie>? = null
    private lateinit var moviesOriginal: List<Movie>
    private var filteredMovies: List<Movie>? = null
    private lateinit var movieViewModel: MovieViewModel


    override fun onResume() {
        super.onResume()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMovieBinding.inflate(inflater, container, false)

        context = this.requireContext()

        binding.rvMovies.layoutManager = LinearLayoutManager(context)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://dummyapi.online/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val moviesAPI: MovieAPI = retrofit.create(MovieAPI::class.java)

        movieViewModel = ViewModelProvider(requireActivity()).get(MovieViewModel::class.java)

        movieViewModel.movies.observe(viewLifecycleOwner, Observer { movies ->
            adapter = MovieAdapter(movies, context)
            adapter.setOnMovieClickListener(this@MovieFragment)
            binding.rvMovies.adapter = adapter
            moviesOriginal = movies
            allMovies = movies
            filterMovies(movieViewModel.query.value ?: "")
        })

        movieViewModel.query.observe(viewLifecycleOwner, Observer { query ->
            binding.searchView.setQuery(query, false)
            filterMovies(query)
        })

        movieViewModel.filteredMovies.observe(viewLifecycleOwner, Observer { filteredMovies ->
            adapter.updateMovie(filteredMovies)
        })

        moviesAPI.getData().enqueue(object: Callback<List<Movie>> {
            override fun onResponse(call: Call<List<Movie>>, response: Response<List<Movie>>) {
                if (response.isSuccessful) {
                    Log.d("r",response.body().toString())
                    movieViewModel.setMovies(response.body() ?: emptyList())
                }
                else
                {
                    Log.e("Error","Connection is failed (1)")
                }
            }
            override fun onFailure(call: Call<List<Movie>>, t: Throwable) {
                Log.e("Error","Connection is failed (2)")
            }
        }
        )

        SearchView(binding.searchView)

        return binding.root
    }

    override fun onMovieClick(movieUrl: String) {
        val bundle = Bundle().apply {
            putString("url", movieUrl)
        }
        findNavController().navigate(R.id.action_navigation_notifications_to_navigation_movie_url, bundle)
    }

    private fun filterMovies(query: String) {
        filteredMovies = allMovies?.filter { movie ->
            query.isNullOrEmpty() || movie.movie.contains(query)
        }
        movieViewModel.setFilteredMovies(filteredMovies ?: emptyList())
    }

    fun SearchView(searchView: SearchView)
    {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { query ->
                    movieViewModel.setQuery(query)
                }
                return true
            }
        })
    }
}