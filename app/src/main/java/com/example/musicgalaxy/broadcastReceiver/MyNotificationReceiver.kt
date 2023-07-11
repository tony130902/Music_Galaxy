package com.example.musicgalaxy.broadcastReceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.musicgalaxy.PlayerActivity
import com.example.musicgalaxy.R
import com.example.musicgalaxy.data.setSongPosition
import com.example.musicgalaxy.notificationChannel.MyApplication
import com.example.musicgalaxy.nowPlayingFragment.NowPlayingSong
import kotlin.system.exitProcess

class MyNotificationReceiver : BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {

        when(intent?.action){
            MyApplication.PREVIOUS -> nextPrevMusic(false,context)
            MyApplication.PLAY -> if (PlayerActivity.isPlaying) pauseMusic() else playMusic()
            MyApplication.NEXT -> nextPrevMusic(true,context)
            MyApplication.EXIT -> {
                PlayerActivity.myService!!.stopForeground(true)
                PlayerActivity.myService = null
                PlayerActivity.myService!!.mediaPlayer!!.release()
                Toast.makeText(context , "App exited...",Toast.LENGTH_SHORT).show()
                exitProcess(1)}
        }
    }

    // Action when play pause button is clicked in notification.
    private fun playMusic(){
        PlayerActivity.isPlaying = true
        PlayerActivity.myService!!.mediaPlayer!!.start()
        PlayerActivity.myService!!.showNotification(R.drawable.baseline_pause_24)
        PlayerActivity.playerBinding.playPauseBtn.setIconResource(R.drawable.baseline_pause_24)
        NowPlayingSong.bindingNowPlayingSong.nowPlayingPlayBtn.setIconResource(R.drawable.baseline_pause_24)
    }

    private fun pauseMusic(){
        PlayerActivity.isPlaying = false
        PlayerActivity.myService!!.mediaPlayer!!.pause()
        PlayerActivity.myService!!.showNotification(R.drawable.baseline_play_arrow_24)
        PlayerActivity.playerBinding.playPauseBtn.setIconResource(R.drawable.baseline_play_arrow_24)
        NowPlayingSong.bindingNowPlayingSong.nowPlayingPlayBtn.setIconResource(R.drawable.baseline_play_arrow_24)

    }
    // Action when next previous button is clicked in notification.
    private fun nextPrevMusic(increment: Boolean , context: Context?){
        setSongPosition(increment)
        PlayerActivity.myService!!.setMediaPlayer()
        if (context != null) {
            Glide.with(context)
                .load(PlayerActivity.playerMusicList[PlayerActivity.position].musicPic)
                .placeholder(R.drawable.music)
                .into(PlayerActivity.playerBinding.musicImage)
            PlayerActivity.playerBinding.songTitle.text = PlayerActivity.playerMusicList[PlayerActivity.position].title

            Glide.with(context)
                .load(PlayerActivity.playerMusicList[PlayerActivity.position].musicPic)
                .placeholder(R.drawable.music)
                .into(NowPlayingSong.bindingNowPlayingSong.nowPlayingImage)
            NowPlayingSong.bindingNowPlayingSong.nowPlayingSongName.text = PlayerActivity.playerMusicList[PlayerActivity.position].title
        }
        playMusic()
    }
}