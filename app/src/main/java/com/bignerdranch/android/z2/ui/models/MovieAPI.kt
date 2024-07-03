package com.bignerdranch.android.z2.ui.models

import retrofit2.Call
import retrofit2.http.GET

interface MovieAPI {

    @GET("movies")
    fun getData(): Call<List<Movie>>
}