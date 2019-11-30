package com.bytelogs.hackerstudio.view

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.bytelogs.hackerstudio.AppContants
import com.bytelogs.hackerstudio.R
import com.bytelogs.hackerstudio.model.SongModel
import kotlinx.android.synthetic.main.activity_player.*
import androidx.palette.graphics.Palette
import androidx.palette.graphics.Palette.PaletteAsyncListener
import android.os.Build
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.Toast
import com.bytelogs.hackerstudio.UrlExpander
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject
import java.lang.Exception
import java.util.*


class PlayerActivity : AppCompatActivity(), Player.EventListener, PaymentResultListener, TextToSpeech.OnInitListener {



    private lateinit var songModel: SongModel
    private lateinit var simpleExoplayer: SimpleExoPlayer
    private var playbackPosition = 0L
    private var isPlaying: Boolean = false
    private lateinit var textToSpeech: TextToSpeech
    private val bandwidthMeter by lazy {
        DefaultBandwidthMeter()
    }
    private val adaptiveTrackSelectionFactory by lazy {
        AdaptiveTrackSelection.Factory(bandwidthMeter)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        textToSpeech = TextToSpeech(this, this)
        setContentView(R.layout.activity_player)
        songModel = intent.getSerializableExtra(AppContants.SONGMODEL) as SongModel
        initView(songModel)


    }

    fun initView(songModel: SongModel) {
        loadImage(songModel)
        tvArtistName.text = songModel.artists
        tvSongName.text = songModel.song
        setBuyClickListener()


    }

    fun setBuyClickListener() {
        buyCard.setOnClickListener(View.OnClickListener {
            speakOut("You clicked on payments")
            startPayment()
        })
    }
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {

            val result = textToSpeech.setLanguage(Locale.US)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported")
            } else {
                speakOut("You have selected "+songModel.song+" and artists are "+songModel.artists+" stream starts in few seconds you can play pause and buy this song")
            }

        } else {
            Log.e("TTS", "Initilization Failed!")
        }
    }

    fun setPlayClickListener() {
        playCard.setOnClickListener(View.OnClickListener {
            if (isPlaying) {
                speakOut("Song is paused")
                playIcon.setImageResource(R.drawable.ic_pause)
                simpleExoplayer.playWhenReady = false

            } else {
                speakOut("Song is resumed")
                playIcon.setImageResource(R.drawable.ic_play)
                simpleExoplayer.playWhenReady = true
            }
        })

    }
    public override fun onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        super.onDestroy()
    }
    private fun speakOut(string: String) {
        textToSpeech.speak(string, TextToSpeech.QUEUE_FLUSH, null)
    }

    override fun onStart() {
        super.onStart()
        initializeExoplayer(songModel?.url)
        setPlayClickListener()
    }

    override fun onStop() {
        releaseExoplayer()
        super.onStop()
    }

    fun loadImage(songModel: SongModel) {
        Glide.with(this)
            .asBitmap()
            .load(songModel.cover_image)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    Palette.from(resource).generate(PaletteAsyncListener {

                        val swatch = it?.vibrantSwatch
                        swatch?.rgb?.let { it1 -> ivCoverBackground.setBackgroundColor(it1) }
                        ivCover.setImageBitmap(resource)
                        swatch?.rgb?.let { it1 -> setStatusBarColor(it1) }

                        swatch?.rgb?.let { it1 -> playCard.setCardBackgroundColor(it1) }
                        swatch?.rgb?.let { it1 -> buyCard.setCardBackgroundColor(it1) }


                    })

                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })

    }

    fun setStatusBarColor(color: Int) {
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(color)
        }
    }

    private fun initializeExoplayer(url: String) {
        simpleExoplayer = ExoPlayerFactory.newSimpleInstance(
            DefaultRenderersFactory(this),
            DefaultTrackSelector(adaptiveTrackSelectionFactory),
            DefaultLoadControl()
        )
        val expandedUrl = UrlExpander.expandUrl(url)
        prepareExoplayer(expandedUrl)
        simpleExoplayer.seekTo(playbackPosition)
        simpleExoplayer.playWhenReady = true
        simpleExoplayer.addListener(this)

    }

    private fun buildMediaSource(uri: Uri): MediaSource {

        val userAgent = "exoplayer-codelab"

        if (uri.getLastPathSegment()!!.contains("mp3") || uri.getLastPathSegment()!!.contains("mp4")) {
            return ExtractorMediaSource.Factory(DefaultHttpDataSourceFactory(userAgent))
                .createMediaSource(uri)
        } else if (uri.getLastPathSegment()!!.contains("m3u8")) {
            return HlsMediaSource.Factory(DefaultHttpDataSourceFactory(userAgent))
                .createMediaSource(uri)
        } else {
            val dashChunkSourceFactory = DefaultDashChunkSource.Factory(
                DefaultHttpDataSourceFactory("ua", bandwidthMeter)
            )
            val manifestDataSourceFactory = DefaultHttpDataSourceFactory(userAgent)
            return DashMediaSource.Factory(dashChunkSourceFactory, manifestDataSourceFactory)
                .createMediaSource(uri)
        }
    }

    private fun prepareExoplayer(songUri: String) {
        val uri = Uri.parse(songUri)
        val mediaSource = buildMediaSource(uri)
        simpleExoplayer.prepare(mediaSource)

    }

    private fun releaseExoplayer() {
        playbackPosition = simpleExoplayer.currentPosition
        simpleExoplayer.release()
    }

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
    }

    override fun onSeekProcessed() {
    }

    override fun onTracksChanged(
        trackGroups: TrackGroupArray?,
        trackSelections: TrackSelectionArray?
    ) {
    }

    override fun onPlayerError(error: ExoPlaybackException?) {
    }

    override fun onLoadingChanged(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE
        } else {
            progressBar.visibility = View.GONE
        }
    }

    override fun onPositionDiscontinuity(reason: Int) {
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
    }

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
    }

    override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        if (playWhenReady && playbackState == Player.STATE_READY) {
            isPlaying = true
            playIcon.setImageResource(R.drawable.ic_pause)
        } else if (playWhenReady) {

        } else {
            isPlaying = false
            playIcon.setImageResource(R.drawable.ic_play)
        }
    }

    fun startPayment() {
        val checkout = Checkout()
        checkout.setImage(R.drawable.ic_launcher_foreground);

        try {
            val options = JSONObject()
            options.put("name", "Hacker Studio")
            options.put("currency", "INR")
            options.put("amount", "100")

            checkout.open(this, options)
        } catch (e: Exception) {
        }
    }
    override fun onPaymentError(p0: Int, p1: String?) {
        Toast.makeText(this,p1,Toast.LENGTH_SHORT).show()
    }

    override fun onPaymentSuccess(p0: String?) {
        Toast.makeText(this,p0,Toast.LENGTH_SHORT).show()

    }
}



