package com.example.musicgalaxy.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.musicgalaxy.MainActivity
import com.example.musicgalaxy.PlayerActivity
import com.example.musicgalaxy.R
import com.example.musicgalaxy.data.MyMusic
import com.example.musicgalaxy.data.longDurationToTime
import com.example.musicgalaxy.databinding.MusicItemBinding

class MusicPlayerAdapter(private val context: Context, private var songList: ArrayList<MyMusic>):
    RecyclerView.Adapter<MusicPlayerAdapter.MyMusicViewHolder>() {

    inner class MyMusicViewHolder(binding: MusicItemBinding) : ViewHolder(binding.root){
        val songTitle = binding.songItemNameTv
        val songAlbum = binding.albumItemNameTv
        val songImage = binding.songItemImageView
        val songDuration = binding.durationItemNameTv
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyMusicViewHolder {
        return MyMusicViewHolder(MusicItemBinding.inflate(LayoutInflater.from(context),parent,false))
    }

    override fun getItemCount(): Int {
        return songList.size
    }

    override fun onBindViewHolder(holder: MyMusicViewHolder, position: Int) {
        holder.songTitle.text = songList[position].title
        holder.songAlbum.text = songList[position].artist
        holder.songDuration.text = longDurationToTime(songList[position].duration)
        Glide.with(context)
            .load(songList[position].musicPic)
            .placeholder(R.drawable.music)
            .into(holder.songImage)

        holder.root.setOnClickListener {
            when{
                MainActivity.search -> sendIntent("searchAdapter", position)
                songList[position].id == PlayerActivity.currentSongId -> sendIntent("NowPlaying",position)
                else -> sendIntent("MusicPlayerAdapter" , position)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(searchList: ArrayList<MyMusic>){
        songList = ArrayList()
        songList.addAll(searchList)
        notifyDataSetChanged()
    }

    private fun sendIntent(ref: String , pos: Int){
        context.startActivity(Intent(context,PlayerActivity::class.java).apply {
            this.putExtra("title",songList[pos].title)
            this.putExtra("position",pos)
            this.putExtra("class",ref)
        })
    }
}