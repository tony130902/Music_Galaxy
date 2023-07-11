package com.example.musicgalaxy.data

import android.content.Context
import android.media.MediaMetadataRetriever
import android.widget.Toast
import androidx.core.content.ContextCompat.getColor
import com.example.musicgalaxy.MainActivity
import com.example.musicgalaxy.PlayerActivity
import com.example.musicgalaxy.R
import java.util.concurrent.TimeUnit

data class MyMusic(
    val id:String,
    val title:String,
    val album:String,
    val artist:String,
    val duration:Long = 0,
    val path:String,
    val musicPic: String
)

fun longDurationToTime(milliseconds: Long):String{
    val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) - minutes*TimeUnit.SECONDS.convert(1,TimeUnit.MINUTES)
    return String.format("%2d:%02d",minutes,seconds)
}

fun getImageArt(path: String): ByteArray?{
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(path)
    return retriever.embeddedPicture
}

fun setSongPosition(increment: Boolean){
    // If repeat is not on then next song plays.
    if (!PlayerActivity.isRepeat) {
            if (increment) {
                if (PlayerActivity.position == PlayerActivity.playerMusicList.size - 1) PlayerActivity.position =
                    0 else ++PlayerActivity.position

            } else {
                if (PlayerActivity.position == 0) PlayerActivity.position =
                    PlayerActivity.playerMusicList.size - 1 else --PlayerActivity.position
            }
    }
}

