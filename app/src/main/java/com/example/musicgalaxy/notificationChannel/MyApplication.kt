package com.example.musicgalaxy.notificationChannel

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager

// This class is always calls whenever app is installed.
class MyApplication : Application() {

    companion object{
        const val CHANNEL_ID = "channel_id"
        const val PLAY = "play"
        const val PAUSE = "pause"
        const val PREVIOUS = "previous"
        const val NEXT = "next"
        const val EXIT = "exit"
        const val PRIORITY = NotificationManager.IMPORTANCE_LOW
    }

    // Notification Channel.
    override fun onCreate() {
        super.onCreate()
        val notificationChannel = NotificationChannel(CHANNEL_ID , "Music is playing" , PRIORITY)
        notificationChannel.description = "Now Song is playing"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)
//        Toast.makeText(this,"Channel is created",Toast.LENGTH_SHORT).show()
    }
}