package com.bytelogs.hackerstudio.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.bytelogs.hackerstudio.model.SongModel
import com.bytelogs.hackerstudio.repository.StudioRepository

class MainViewModel : ViewModel() {
    private val studioRepository:StudioRepository = StudioRepository()

    suspend fun onGetSongList():LiveData<List<SongModel>>{
       return studioRepository.getSongsList()
    }
}