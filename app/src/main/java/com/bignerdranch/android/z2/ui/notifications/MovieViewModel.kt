package com.bignerdranch.android.z2.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bignerdranch.android.z2.ui.models.Movie

class MovieViewModel : ViewModel() {

    private val _movies = MutableLiveData<List<Movie>>()
    val movies: LiveData<List<Movie>>
        get() = _movies

    fun setMovies(value: List<Movie>){
        _movies.value = value
    }

    private val _query = MutableLiveData<String>()
    val query: LiveData<String>
        get() = _query

    fun setQuery(value: String){
        _query.value = value
        filterMovies(_query.value ?: "")
    }

    private val _filteredMovies = MutableLiveData<List<Movie>>()
    val filteredMovies: LiveData<List<Movie>>
        get() = _filteredMovies

    fun setFilteredMovies(value: List<Movie>){
        _filteredMovies.value = value
    }

    private fun filterMovies(query: String) {
        _filteredMovies.value = _movies.value?.filter { movie ->
            query.isNullOrEmpty() || movie.movie.contains(query)
        }
    }
}