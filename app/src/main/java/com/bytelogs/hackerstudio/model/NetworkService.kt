package com.bytelogs.hackerstudio.model

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface NetworkService {

    @GET("/studio")
    suspend fun fetchHackerStudioSongs(): Response<MutableList<SongModel>>

    /**
     * Companion object to create the GithubApiService
     */
    companion object Factory {
        fun create(): NetworkService {
            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("http://starlord.hackerearth.com")
                .build()

            return retrofit.create(NetworkService::class.java)
        }
    }
}