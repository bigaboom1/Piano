package com.piano

import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.piano.databinding.ActivityOpenActvBinding
import java.io.File
import androidx.core.net.toUri

class OpenActv : AppCompatActivity() {

    private lateinit var binding: ActivityOpenActvBinding
    private lateinit var buttonPlay: ImageButton
    private lateinit var text: TextView
    private lateinit var rv: RecyclerView
    private var player:MediaPlayer? = null

    private val AUDIO_CODE  = 1001
    private fun getRequiredAudioPermission(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOpenActvBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        buttonPlay = binding.imageButton5

        text = binding.titleTextView
        rv = binding.rvList


        checkAudioPermission()
        handleIncomingIntent()
        setupRecyclerView()




    }
    private fun handleIncomingIntent() {
        try {
            val intent: Intent? = intent
            val action: String? = intent?.action
            val type: String? = intent?.type

            if (Intent.ACTION_VIEW == action && type != null) {
                if (type.startsWith("audio/")) {
                    val uri: Uri? = intent.data
                    if (uri != null) {
                        val fileName = uri.lastPathSegment ?: "unknown_file"
                        text.text = fileName
                        playAudio(uri)
                    }
                }
            }
        } catch (e: Exception) {
            File(filesDir, "error_out.txt").writeText(e.toString())
        }
    }
    private fun setupRecyclerView() {

        val list = getList()


        val adapter = CustomAdapter(list)

        rv.adapter = adapter
        rv.layoutManager = LinearLayoutManager(this)
        adapter.onItemClick = {
            text.text = it
            playAudio("/storage/emulated/0/Music/${it}".toUri())

        }
    }
    private fun playAudio(uri: Uri) {
        if(player?.isPlaying == true){
            player?.release()
        }
        player = MediaPlayer.create(this,uri)
        player?.start()
        buttonPlay.setImageResource(android.R.drawable.ic_media_pause)
        player?.setOnCompletionListener{
            buttonPlay.setImageResource(android.R.drawable.ic_media_play)
        }

    }
    fun playButton(view: View?){
        if (view != null) {
            when(view.id){
                R.id.imageButton4->{
                    //HomeButton
                    val intent = Intent(this,MainActivity::class.java)
                    startActivity(intent)
                }
                R.id.imageButton5->{
                   if(player?.isPlaying == true){
                       player?.pause()
                       buttonPlay.setImageResource(android.R.drawable.ic_media_play)
                   }

                }

            }
        }
    }
    private fun getList(): ArrayList<String> {
        val directory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).path)
        val audioExtensions = listOf("mp3", "wav", "ogg", "flac", "m4a", "aac")
        val nameList = arrayListOf<String>()
        val fileList = directory.listFiles()?.filter { file ->
            file.isFile && audioExtensions.any { ext ->
                file.name.endsWith(".$ext", ignoreCase = true)
            }
        }

        if (fileList != null) {
            fileList.forEach{
                file->nameList.add(file.name)
            }
        }
        return nameList
    }
    private fun checkAudioPermission() {
        val permission = getRequiredAudioPermission()

        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted
            }
            else -> {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(permission),
                    AUDIO_CODE
                )
            }
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}