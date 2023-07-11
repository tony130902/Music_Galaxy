package com.example.musicgalaxy.nowPlayingFragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.musicgalaxy.PlayerActivity
import com.example.musicgalaxy.R
import com.example.musicgalaxy.data.setSongPosition
import com.example.musicgalaxy.databinding.FragmentNowPlayingSongBinding
import com.example.musicgalaxy.myServices.MyService


class NowPlayingSong : Fragment() {

    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var bindingNowPlayingSong: FragmentNowPlayingSongBinding
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_now_playing_song, container, false)
        bindingNowPlayingSong = FragmentNowPlayingSongBinding.bind(view)
        bindingNowPlayingSong.root.visibility = View.INVISIBLE

        bindingNowPlayingSong.nowPlayingPlayBtn.setOnClickListener {
            if (PlayerActivity.isPlaying) pauseSong() else playSong()
        }

        bindingNowPlayingSong.nowPlayingNextBtn.setOnClickListener {
            setSongPosition(true)
            PlayerActivity.myService!!.setMediaPlayer()
            if (context != null) {
                Glide.with(this)
                    .load(PlayerActivity.playerMusicList[PlayerActivity.position].musicPic)
                    .placeholder(R.drawable.music)
                    .into(NowPlayingSong.bindingNowPlayingSong.nowPlayingImage)
                NowPlayingSong.bindingNowPlayingSong.nowPlayingSongName.text = PlayerActivity.playerMusicList[PlayerActivity.position].title
            }
            playSong()
        }
        bindingNowPlayingSong.root.setOnClickListener {
            val intent = Intent(requireContext(),PlayerActivity::class.java).apply {
                this.putExtra("position",PlayerActivity.position)
                this.putExtra("class","NowPlaying")
            }
            ContextCompat.startActivity(requireContext(),intent,null)
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        if (PlayerActivity.myService != null){
            bindingNowPlayingSong.root.visibility = View.VISIBLE
            bindingNowPlayingSong.nowPlayingSongName.isSelected = true
            Glide.with(this)
                .load(PlayerActivity.playerMusicList[PlayerActivity.position].musicPic)
                .placeholder(R.drawable.music)
                .into(bindingNowPlayingSong.nowPlayingImage)
            bindingNowPlayingSong.nowPlayingSongName.text = PlayerActivity.playerMusicList[PlayerActivity.position].title

            if (PlayerActivity.isPlaying) bindingNowPlayingSong.nowPlayingPlayBtn.setIconResource(R.drawable.baseline_pause_24)
            else bindingNowPlayingSong.nowPlayingPlayBtn.setIconResource(R.drawable.baseline_play_arrow_24)
        }
    }

    private fun playSong(){
        PlayerActivity!!.myService!!.showNotification(R.drawable.baseline_pause_24)
        PlayerActivity.playerBinding.playPauseBtn.setIconResource(R.drawable.baseline_pause_24)
        bindingNowPlayingSong.nowPlayingPlayBtn.setIconResource(R.drawable.baseline_pause_24)
        PlayerActivity.myService!!.mediaPlayer!!.start()
        PlayerActivity.isPlaying = true
    }
    private fun pauseSong(){
        PlayerActivity!!.myService!!.showNotification(R.drawable.baseline_play_arrow_24)
        PlayerActivity.playerBinding.playPauseBtn.setIconResource(R.drawable.baseline_play_arrow_24)
        bindingNowPlayingSong.nowPlayingPlayBtn.setIconResource(R.drawable.baseline_play_arrow_24)
        PlayerActivity.myService!!.mediaPlayer!!.pause()
        PlayerActivity.isPlaying = false
    }
}