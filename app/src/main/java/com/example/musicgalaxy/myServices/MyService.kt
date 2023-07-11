package com.example.musicgalaxy.myServices

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import com.example.musicgalaxy.MainActivity
import com.example.musicgalaxy.PlayerActivity
import com.example.musicgalaxy.R
import com.example.musicgalaxy.broadcastReceiver.MyNotificationReceiver
import com.example.musicgalaxy.data.getImageArt
import com.example.musicgalaxy.data.longDurationToTime
import com.example.musicgalaxy.notificationChannel.MyApplication
import com.example.musicgalaxy.nowPlayingFragment.NowPlayingSong

class MyService : Service(), AudioManager.OnAudioFocusChangeListener{

    private var myBinder = MyBinder()
    var mediaPlayer : MediaPlayer? = null
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var runnable: Runnable
    lateinit var audioManager: AudioManager

    override fun onBind(intent: Intent): IBinder {
        mediaSession = MediaSessionCompat(baseContext,"MyMusic")
        return myBinder
    }

    // Returns the object of this class.
    inner class MyBinder: Binder(){
        fun currentService():MyService{
            return this@MyService
        }
    }

    // Function for showing notification.
    fun showNotification(playPauseBtn : Int){

        val intent = Intent(baseContext,MainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(baseContext,0,intent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val prevIntent = Intent(baseContext,MyNotificationReceiver::class.java).setAction(MyApplication.PREVIOUS)
        val prevPendingIntent = PendingIntent.getBroadcast(
            baseContext,1,prevIntent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val nextIntent = Intent(baseContext,MyNotificationReceiver::class.java).setAction(MyApplication.NEXT)
        val nextPendingIntent = PendingIntent.getBroadcast(
            baseContext,1,nextIntent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val playPauseIntent = Intent(baseContext,MyNotificationReceiver::class.java).setAction(MyApplication.PLAY)
        val playPausePendingIntent = PendingIntent.getBroadcast(
            baseContext,1,playPauseIntent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val exitIntent = Intent(baseContext,MyNotificationReceiver::class.java).setAction(MyApplication.EXIT)
        val exitPendingIntent = PendingIntent.getBroadcast(
            baseContext,1,exitIntent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // Current song image in Notification.
        val imageArtByteArray = getImageArt(PlayerActivity.playerMusicList[PlayerActivity.position].path)
        val songPic = if (imageArtByteArray == null) {
            BitmapFactory.decodeResource(resources,R.drawable.music)
        } else {
            BitmapFactory.decodeByteArray(imageArtByteArray,0,imageArtByteArray.size)
        }

        // Notification to show.
        val notification = NotificationCompat.Builder(baseContext,MyApplication.CHANNEL_ID)
            .setContentTitle(PlayerActivity.playerMusicList[PlayerActivity.position].title)
            .setContentText(PlayerActivity.playerMusicList[PlayerActivity.position].artist)
            .setSmallIcon(R.drawable.music)
            .setLargeIcon(songPic)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setStyle(androidx.media.app.NotificationCompat.DecoratedMediaCustomViewStyle())
            .addAction(R.drawable.baseline_skip_previous_24 , "previous" , prevPendingIntent)
            .addAction(playPauseBtn, "play" , playPausePendingIntent)
            .addAction(R.drawable.baseline_skip_next_24 , "next" , nextPendingIntent)
            .addAction(R.drawable.exit , "exit" , exitPendingIntent)
            .setContentIntent(contentIntent)
            .build()

        // Foreground means background task with notification.
        startForeground(100,notification)
    }

    // Global initialization of media player.
    fun setMediaPlayer(){
        try {
            if (PlayerActivity.myService!!.mediaPlayer == null)
                PlayerActivity.myService!!.mediaPlayer = MediaPlayer()
            PlayerActivity.myService!!.mediaPlayer!!.reset()
            PlayerActivity.myService!!.mediaPlayer!!.setDataSource(PlayerActivity.playerMusicList[PlayerActivity.position].path)
            PlayerActivity.myService!!.mediaPlayer!!.prepare()

            PlayerActivity.playerBinding.playPauseBtn.setIconResource(R.drawable.baseline_pause_24)
            PlayerActivity.myService!!.showNotification(R.drawable.baseline_pause_24)

            PlayerActivity.playerBinding.startSeekbarTv.text = longDurationToTime(mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.playerBinding.endSeekbarTv.text = longDurationToTime(mediaPlayer!!.duration.toLong())
            PlayerActivity.playerBinding.seekbarTv.progress = 0
            PlayerActivity.playerBinding.seekbarTv.max = mediaPlayer!!.duration
            PlayerActivity.currentSongId = PlayerActivity.playerMusicList[PlayerActivity.position].id
        }catch (e: Exception) {return}
    }

    // Setup the runnable and set the progress automatically.
    fun seekBarSetup(){
        // Executes the code over and over.
        runnable = Runnable {
            PlayerActivity.playerBinding.startSeekbarTv.text = longDurationToTime(
                mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.playerBinding.seekbarTv.progress = mediaPlayer!!.currentPosition
            Handler(Looper.getMainLooper()).postDelayed(runnable,1000)
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable,0)
    }

    override fun onAudioFocusChange(focusChange: Int) {
        if (focusChange <= 0){
            PlayerActivity.isPlaying = true
            PlayerActivity.myService!!.showNotification(R.drawable.baseline_pause_24)
            PlayerActivity.playerBinding.playPauseBtn.setIconResource(R.drawable.baseline_pause_24)
            PlayerActivity.myService!!.mediaPlayer!!.start()
            NowPlayingSong.bindingNowPlayingSong.nowPlayingPlayBtn.setIconResource(R.drawable.baseline_pause_24)
        }
        else{
            PlayerActivity.isPlaying = false
            PlayerActivity.myService!!.showNotification(R.drawable.baseline_play_arrow_24)
            PlayerActivity.playerBinding.playPauseBtn.setIconResource(R.drawable.baseline_play_arrow_24)
            PlayerActivity.myService!!.mediaPlayer!!.pause()
            NowPlayingSong.bindingNowPlayingSong.nowPlayingPlayBtn.setIconResource(R.drawable.baseline_play_arrow_24)
        }
    }
}