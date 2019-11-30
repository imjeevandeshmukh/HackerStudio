package com.bytelogs.hackerstudio.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.bytelogs.hackerstudio.R
import com.bytelogs.hackerstudio.model.SongModel
import com.bytelogs.hackerstudio.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.razorpay.Checkout
import java.util.*








class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {


    private lateinit var  mainViewModel:MainViewModel
    private lateinit var songAdapter:SongAdapter
    private lateinit var mutableSongList: MutableList<SongModel>
    private lateinit var textToSpeech: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Checkout.preload(getApplicationContext())
        setContentView(R.layout.activity_main)
        initViews()
        mainViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        GlobalScope.launch (Dispatchers.Main) {onGetSongList()}


    }

    fun initViews(){
        mutableSongList = mutableListOf()
        rvSongList.layoutManager = LinearLayoutManager(this)
        songAdapter = SongAdapter(mutableSongList)
        rvSongList.adapter= songAdapter
        textToSpeech = TextToSpeech(this, this)

    }
    suspend fun onGetSongList(){
        mainViewModel.onGetSongList().observe(this, Observer {
            songAdapter.updatePostList(it as MutableList<SongModel>)
        })
    }
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {

            val result = textToSpeech.setLanguage(Locale.US)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported")
            } else {
                speakOut()
            }

        } else {
            Log.e("TTS", "Initilization Failed!")
        }
    }
    public override fun onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        super.onDestroy()
    }
    private fun speakOut() {
        textToSpeech.speak(getString(R.string.introString), TextToSpeech.QUEUE_FLUSH, null)
    }


}
