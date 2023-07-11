package com.example.musicgalaxy

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.example.musicgalaxy.data.MyMusic
import com.example.musicgalaxy.data.longDurationToTime
import com.example.musicgalaxy.data.setSongPosition
import com.example.musicgalaxy.databinding.ActivityPlayerBinding
import com.example.musicgalaxy.myServices.MyService

class PlayerActivity : AppCompatActivity(),ServiceConnection, MediaPlayer.OnCompletionListener {

    companion object{
        lateinit var playerMusicList: ArrayList<MyMusic>
        var position:Int = 0
        var isPlaying = false
        var myService : MyService? = null
        var isShuffle = false
        @SuppressLint("StaticFieldLeak")
        lateinit var playerBinding: ActivityPlayerBinding
        var isRepeat = false
        var currentSongId: String = ""
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_MusicGalaxy)
        playerBinding = DataBindingUtil.setContentView(this,R.layout.activity_player)
        initializeView()

        playerBinding.playPauseBtn.setOnClickListener { if (isPlaying) pauseSong() else playSong() }
        playerBinding.nextSongBtn.setOnClickListener { prevNextSong(true) }
        playerBinding.previousSongBtn.setOnClickListener { prevNextSong(false) }

        // Seekbar change listener.
        playerBinding.seekbarTv.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) myService!!.mediaPlayer!!.seekTo(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit

        })

        // Repeat button.
        playerBinding.repeatSongBtn.setOnClickListener {
            if (!isRepeat){
                isRepeat = true
                playerBinding.repeatSongBtn.setColorFilter(ContextCompat.getColor(this,R.color.repeatBtn))
                Toast.makeText(this, "Song Repeat is ON", Toast.LENGTH_SHORT).show()
            }else {
                isRepeat = false
                playerBinding.repeatSongBtn.setColorFilter(ContextCompat.getColor(this, R.color.black))
                Toast.makeText(this, "Song Repeat is OFF", Toast.LENGTH_SHORT).show()
            }
        }

        // Back Button.
        playerBinding.backButton.setOnClickListener { finish() }

        // Share button.
        playerBinding.shareSongBtn.setOnClickListener {
            val intent = Intent()
            intent.also {
                it.action = Intent.ACTION_SEND
                it.type = "audio/*"
                it.putExtra(Intent.EXTRA_STREAM, Uri.parse(playerMusicList[position].path))
            }
            startActivity(Intent.createChooser(intent,"Sharing music file..."))
        }

        // Shuffle song btn.
        playerBinding.shuffleSongBtn.setOnClickListener {
            if (!isShuffle){
                isShuffle = true
                playerBinding.shuffleSongBtn.setColorFilter(getColor(R.color.repeatBtn))
                Toast.makeText(this, "Song Shuffle is ON", Toast.LENGTH_SHORT).show()
                shuffleSongs()
            }else {
                isShuffle = false
                playerBinding.shuffleSongBtn.setColorFilter(getColor(R.color.black))
                Toast.makeText(this, "Song Shuffle is OFF", Toast.LENGTH_SHORT).show()
                playerMusicList = ArrayList()
                playerMusicList.addAll(MainActivity.myMainMusicList)
                setSongImage()
                setMediaPlayer()
            }
        }
    }

    // Shuffle Song.
    private fun shuffleSongs() {
        // Starting service.
        val intent = Intent(this,MyService::class.java)
        bindService(intent,this, BIND_AUTO_CREATE)
        startService(intent)
        playerMusicList = ArrayList()
        playerMusicList.addAll(MainActivity.myMainMusicList)
        playerMusicList.shuffle()
        setSongImage()
        setMediaPlayer()
    }

    // It receive intent, setting list, and load image.
    private fun initializeView(){
        position = intent.getIntExtra("position",0)
        playerBinding.songTitle.isSelected = true
        when(intent.getStringExtra("class")){
            "NowPlaying" -> {
                setSongImage()
                playerBinding.startSeekbarTv.text = longDurationToTime(myService!!.mediaPlayer!!.currentPosition.toLong())
                playerBinding.endSeekbarTv.text = longDurationToTime(myService!!.mediaPlayer!!.duration.toLong())
                playerBinding.seekbarTv.progress = myService!!.mediaPlayer!!.currentPosition
                playerBinding.seekbarTv.max = myService!!.mediaPlayer!!.duration
                if (isPlaying) playerBinding.playPauseBtn.setIconResource(R.drawable.baseline_pause_24)
                else playerBinding.playPauseBtn.setIconResource(R.drawable.baseline_play_arrow_24)
            }
            "MusicPlayerAdapter" -> {
                // Starting service.
                val intent = Intent(this,MyService::class.java)
                bindService(intent,this, BIND_AUTO_CREATE)
                startService(intent)
                playerMusicList = ArrayList()
                playerMusicList.addAll(MainActivity.myMainMusicList)
                setSongImage()
            }

            "searchAdapter" -> {
                // Starting service.
                val intent = Intent(this,MyService::class.java)
                bindService(intent,this, BIND_AUTO_CREATE)
                startService(intent)
                playerMusicList = ArrayList()
                playerMusicList.addAll(MainActivity.searchMusicList)
                setSongImage()
            }
        }
    }

    // Setting media player and set notification play pause icon.
    private fun setMediaPlayer(){
        try {
            if (myService!!.mediaPlayer == null) myService!!.mediaPlayer = MediaPlayer()
            isPlaying = true
            myService!!.mediaPlayer!!.reset()
            myService!!.mediaPlayer!!.setDataSource(playerMusicList[position].path)
            playerBinding.playPauseBtn.setIconResource(R.drawable.baseline_pause_24)
            myService!!.mediaPlayer!!.prepare()
            myService!!.mediaPlayer!!.start()

            myService!!.showNotification(R.drawable.baseline_pause_24)

            playerBinding.startSeekbarTv.text = longDurationToTime(myService!!.mediaPlayer!!.currentPosition.toLong())
            playerBinding.endSeekbarTv.text = longDurationToTime(myService!!.mediaPlayer!!.duration.toLong())
            playerBinding.seekbarTv.progress = 0
            playerBinding.seekbarTv.max = myService!!.mediaPlayer!!.duration

            myService!!.mediaPlayer!!.setOnCompletionListener(this)

            currentSongId = playerMusicList[position].id
        }catch (e: Exception) {return}
    }

    private fun setSongImage(){
        Glide.with(this)
            .load(playerMusicList[position].musicPic)
            .placeholder(R.drawable.music)
            .into(playerBinding.musicImage)
        playerBinding.songTitle.text = playerMusicList[position].title

        if (isRepeat) playerBinding.repeatSongBtn.setColorFilter(ContextCompat.getColor(this,R.color.repeatBtn))
        if (isShuffle) playerBinding.shuffleSongBtn.setColorFilter(ContextCompat.getColor(this,R.color.repeatBtn))
    }

    private fun playSong(){
        isPlaying = true
        myService!!.showNotification(R.drawable.baseline_pause_24)
        playerBinding.playPauseBtn.setIconResource(R.drawable.baseline_pause_24)
        myService!!.mediaPlayer!!.start()
    }

    private fun pauseSong(){
        isPlaying = false
        myService!!.showNotification(R.drawable.baseline_play_arrow_24)
        playerBinding.playPauseBtn.setIconResource(R.drawable.baseline_play_arrow_24)
        myService!!.mediaPlayer!!.pause()
    }

    // True for next and false for previous.
    private fun prevNextSong(increment: Boolean) = if (increment){
        setSongPosition(true)
        setMediaPlayer()
        setSongImage()
    }else{
        setSongPosition(false)
        setMediaPlayer()
        setSongImage()
    }

    // When service is connected and set mediaPlayer.
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MyService.MyBinder
        myService = binder.currentService()
        setMediaPlayer()
        myService!!.seekBarSetup()
        myService!!.audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        myService!!.audioManager.requestAudioFocus(myService,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN)
    }

    // When service is disconnected.
    override fun onServiceDisconnected(name: ComponentName?) {
        myService = null
    }

    // When song is completed.
    override fun onCompletion(mp: MediaPlayer?) {
        setSongPosition(true)
        setMediaPlayer()
        try {setSongImage()}catch (e:Exception){return}
    }
}