 package com.example.musicgalaxy

 import android.annotation.SuppressLint
 import android.content.pm.PackageManager
 import android.net.Uri
 import android.os.Build
 import android.os.Bundle
 import android.provider.MediaStore
 import android.view.Menu
 import android.widget.Toast
 import androidx.appcompat.app.AppCompatActivity
 import androidx.appcompat.widget.SearchView
 import androidx.core.app.ActivityCompat
 import androidx.databinding.DataBindingUtil
 import androidx.recyclerview.widget.LinearLayoutManager
 import com.example.musicgalaxy.adapters.MusicPlayerAdapter
 import com.example.musicgalaxy.data.MyMusic
 import com.example.musicgalaxy.databinding.ActivityMainBinding
 import java.io.File
 import kotlin.system.exitProcess

 class MainActivity : AppCompatActivity() {
     private lateinit var binding: ActivityMainBinding
     private lateinit var myAdapter: MusicPlayerAdapter
     companion object{
         lateinit var myMainMusicList : ArrayList<MyMusic>
         lateinit var searchMusicList : ArrayList<MyMusic>
         var search = false
     }
     @SuppressLint("ResourceAsColor")
     override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)
         setTheme(R.style.Theme_MusicGalaxy)
         binding = DataBindingUtil.setContentView(this,R.layout.activity_main)
         requestRunTimePermission()
         initializeView()

         binding.favoritesBtn.setOnClickListener {
             Toast.makeText(this, "Not Implemented...", Toast.LENGTH_SHORT).show()
         }

         binding.songPlaylistBtn.setOnClickListener {
             Toast.makeText(this, "Not Implemented...", Toast.LENGTH_SHORT).show()
         }

     }

     // Search icon.
     override fun onCreateOptionsMenu(menu: Menu?): Boolean {
         menuInflater.inflate(R.menu.song_search_menu,menu)
         val searchView = menu?.findItem(R.id.search_view)?.actionView as SearchView
         searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
             override fun onQueryTextSubmit(query: String?): Boolean = true

             override fun onQueryTextChange(newText: String?): Boolean {
                 searchMusicList = ArrayList()
                 if (newText!=null){
                     val inputSearch = newText.lowercase()
                     for (song in myMainMusicList){
                         if (song.title.lowercase().contains(inputSearch)){
                             searchMusicList.add(song)
                         }
                     }
                     search = true
                     myAdapter.updateList(searchMusicList)
                 }
                 return true
             }
         })
         return super.onCreateOptionsMenu(menu)
     }

     // This method query all the storage audio file by using cursor.
     // Returns a list of audio files.
     @SuppressLint("Range", "SuspiciousIndentation")
     private fun getAllMusic(): ArrayList<MyMusic>{
         val tempList = ArrayList<MyMusic>()

         // What type of storage.
         val collection =
             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                 MediaStore.Audio.Media.getContentUri(
                     MediaStore.VOLUME_EXTERNAL
                 )
             } else MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

         val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"  // Type of Media
         val projection = arrayOf(MediaStore.Audio.Media._ID,
             MediaStore.Audio.Media.TITLE,
             MediaStore.Audio.Media.ALBUM,
             MediaStore.Audio.Media.ARTIST,
             MediaStore.Audio.Media.DURATION,
             MediaStore.Audio.Media.DATA,
             MediaStore.Audio.Media.DATE_ADDED,
             MediaStore.Audio.Media.ALBUM_ID) // All the media data content.

         val cursor = this.contentResolver.query(collection,
             projection,selection,null,
             MediaStore.Audio.Media.TITLE + " ASC",null) // cursor object for go through all the media.

         if (cursor != null)
             if (cursor.moveToFirst())
                 do {
                     val musicTitle = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                     val musicId = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
                     val musicAlbum = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                     val musicArtist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                     val musicDuration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                     val musicPath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))

                     val musicPic = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)).toString()
                     val uri = Uri.parse("content://media/external/audio/albumart")
                     val picUri = Uri.withAppendedPath(uri,musicPic).toString()

                     val myMusic = MyMusic(musicId,musicTitle,musicAlbum,musicArtist,musicDuration,musicPath,picUri)
                     val file = File(myMusic.path)
                     if (file.exists())
                         tempList.add(myMusic)
                 }while (cursor.moveToNext())
         cursor?.close()

         return tempList
     }

     // Function for requesting Storage Permission.
     private fun requestRunTimePermission(){
         if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
             != PackageManager.PERMISSION_GRANTED)
             ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),100)
     }

     /* Function for checking if request is granted or not. */
     @SuppressLint("SuspiciousIndentation")
     override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
         super.onRequestPermissionsResult(requestCode, permissions, grantResults)
         if (requestCode == 100)
             if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                 Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                 initializeView()
     }

     @SuppressLint("SetTextI18n")
     private fun initializeView(){
         search = false
         myMainMusicList = getAllMusic()
         binding.songsRecyclerView.setHasFixedSize(true)
         myAdapter = MusicPlayerAdapter(this, myMainMusicList)
         binding.songsRecyclerView.layoutManager = LinearLayoutManager(this)
         binding.songsRecyclerView.adapter = myAdapter
         binding.totalSongsTv.text = "Total Songs: ${myMainMusicList.size}"
     }

     // When activity is destroys release everything. .
     override fun onDestroy() {
         super.onDestroy()
         if (!PlayerActivity.isPlaying && PlayerActivity.myService != null) {
             PlayerActivity.myService!!.audioManager.abandonAudioFocus(PlayerActivity.myService)
             PlayerActivity.myService!!.stopForeground(true)
             PlayerActivity.myService!!.mediaPlayer!!.release()
             PlayerActivity.myService = null
             exitProcess(1)
         }
     }
}