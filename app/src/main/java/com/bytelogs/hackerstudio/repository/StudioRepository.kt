package com.bytelogs.hackerstudio.repository

import androidx.lifecycle.MutableLiveData
import com.bytelogs.hackerstudio.model.NetworkService
import com.bytelogs.hackerstudio.model.SafeApiRequest
import com.bytelogs.hackerstudio.model.SongModel

class StudioRepository : SafeApiRequest() {

    private val songListLiveData = MutableLiveData<List<SongModel>>()
    val networkService = NetworkService.create()

    suspend fun  getSongsList():MutableLiveData<List<SongModel>>{

            val response = apiRequest {networkService.fetchHackerStudioSongs()}
            songListLiveData.postValue(response)
        return songListLiveData
    }
}