package com.piano

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.Manifest
import android.os.Environment
import android.provider.Settings
import android.view.View
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.arthenica.ffmpegkit.FFmpegKit
import com.piano.databinding.ActivityMainBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private var player:MediaPlayer? = null
    val CHANNEL_ID = "ch1"

    private lateinit var buttonStart: ImageButton
    private lateinit var buttonStop: ImageButton
    private lateinit var buttonInfo: ImageButton
    private lateinit var spinner: Spinner

    private var text=""
    private val seq= arrayListOf<String>()
    private var play=false

    override fun onCreate(savedInstanceState: Bundle?) {
        try{
            super.onCreate(savedInstanceState)
            binding = ActivityMainBinding.inflate(layoutInflater)
            enableEdgeToEdge()
            setContentView(binding.root)
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }

            buttonStart = binding.ButtonPlay
            buttonStop = binding.ButtonStop
            buttonInfo = binding.imageButton
            spinner = binding.spinner

            createNotificationChannel()





            text = ""
            play = false

            try{
                val listSounds = listOf("do","re","mi","fa","sol","lya","si")
                for (i in listSounds){
                    if(!File(filesDir,"sound_$i.wav").exists()){
                        copyResourceToFile(this,
                            this.resIdByName("sound_$i", "raw"),
                            "sound_$i.wav")
                    }
                    if (!File(filesDir,"sound_$i"+"_2okt.wav").exists()){
                        copyResourceToFile(this,
                            this.resIdByName("sound_$i"+"_2okt", "raw"),
                            "sound_$i"+"_2okt.wav")
                    }
                }

            }
            catch (e:Exception){
                File(filesDir,"errors.txt").appendText("\n" + e.toString()+"Copy")
            }

            buttonStart.setOnClickListener {

                play = !play
                if (play)
                    buttonStart.setImageResource(android.R.drawable.ic_media_pause)
                else
                    buttonStart.setImageResource(android.R.drawable.ic_media_play)
            }
            buttonStop.setOnClickListener {
                showDialog()
            }
            buttonInfo.setOnClickListener {
                val intent = Intent(this, OktavaInfo::class.java)
                intent.putExtra("OktNumber",spinner.selectedItem.toString())

                if(spinner.selectedItemPosition == 0)
                    intent.putExtra("OktDesc", getString(R.string._2093_0_4186_0_4_7))
                else
                    intent.putExtra("OktDesc", getString(R.string._1046_5_2093_0_3_6))
                startActivity(intent)
            }


        }
        catch (e: Exception){
            File(filesDir,"errors.txt").appendText("\n" + e.toString()+"There")
        }
        finally {
            File(filesDir,"errors.txt").appendText("\n------------------------------")
        }



    }
    private fun sound(sound:String, position:Int){
        when(position){
            0->{
                player = MediaPlayer.create(this,"android.resource://$packageName/raw/sound_$sound".toUri())
                if(play)
                    seq.add(File(filesDir,"sound_$sound.wav").path)
            }
            1->{
                player = MediaPlayer.create(this, ("android.resource://$packageName/raw/sound_$sound"+"_2okt").toUri())
                if(play)
                    seq.add(File(filesDir,"sound_$sound"+"_2okt.wav").path)
            }
        }
        player?.start()
    }
    private fun merge(){
        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        var filename = "output.wav"
        if(File(directory,filename).exists()){
            var k = 1
            while(File(directory,"output$k.wav").exists())
                k++
            filename = "output$k.wav"
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivity(intent)
            }
        }
        val command = arrayOf(
            "-y",
            "-f",
            "concat",
            "-safe 0",
            "-i", File(filesDir, "sequence.txt").path,
            "-c copy",
            File(directory,filename)
        )
        val commandString = command.joinToString(" ")
        try {

            if(ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                return
            }
            else {
                showNotification()
            }

            val logs = FFmpegKit.execute(commandString).allLogsAsString
            File(filesDir,"FFmpegstat.txt").writeText(logs)

        }
        catch (e: Exception){
            File(filesDir,"Errors.txt").writeText(e.toString())
            Toast.makeText(applicationContext,"Failure", Toast.LENGTH_SHORT).show()
        }

    }
    private fun copyResourceToFile(context: Context, resourceId: Int, fileName: String) {
        val inputStream: InputStream? = try {
            context.resources.openRawResource(resourceId)
        } catch (e: Exception) {
            File(filesDir,"errors.txt").appendText("\n" + e.toString()+"Problem with copy")
            return
        }

        val outputFile = File(context.filesDir, fileName)

        try {
            FileOutputStream(outputFile).use { outputStream ->
                inputStream?.use { input ->
                    val buffer = ByteArray(4 * 1024)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                }
            }
        } catch (e: IOException) {
            File(filesDir,"errors.txt").appendText(e.toString()+"Copy")
        } finally {
            inputStream?.close()
        }
    }
    fun playButton(view: View?){
        if (view != null) {
            when(view.id){
                R.id.button_do->sound("do",spinner.selectedItemPosition)
                R.id.button_re->sound("re",spinner.selectedItemPosition)
                R.id.button_mi->sound("mi",spinner.selectedItemPosition)
                R.id.button_fa->sound("fa",spinner.selectedItemPosition)
                R.id.button_sol->sound("sol",spinner.selectedItemPosition)
                R.id.button_lya->sound("lya",spinner.selectedItemPosition)
                R.id.button_si->sound("si",spinner.selectedItemPosition)
                R.id.imageButton3->{
                    val intent = Intent(this, OpenActv::class.java)
                    startActivity(intent)
                }
            }
        }

    }
    private fun showDialog() {
        val builder = AlertDialog.Builder(this)

        builder.setTitle("Подтверждение создания")

            .setMessage("Вы уверены, что хотите создать последовательность нот?")

            .setPositiveButton("Да") { dialog, which ->

                buttonStart.setImageResource(android.R.drawable.ic_media_play)
                for (i in seq) {
                    text += "file '$i'\n"
                }

                File(filesDir, "sequence.txt").writeText(text)


                merge()

                text = ""
                seq.clear()
                play = false

            }

            .setNegativeButton("Нет") { dialog, which ->

                dialog.dismiss()

            }

        val alertDialog = builder.create()

        alertDialog.show()
    }
    private fun Context.resIdByName(resIdName: String?, resType: String): Int {
        resIdName?.let {
            return resources.getIdentifier(it, resType, packageName)
        }
        throw Resources.NotFoundException()
    }


    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){
            result:Boolean->
            if(result){
                showNotification()
            }
            else{
                Toast.makeText(
                    this@MainActivity,
                    "Permission not Granted",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    @SuppressLint("MissingPermission")
    private fun showNotification(){
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.piano_icon_2)
            .setContentTitle("Piano")
            .setContentText("There is a new soundtrack in your audio-library.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        with(NotificationManagerCompat.from(this)){
            notify(1,builder.build())
        }
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}