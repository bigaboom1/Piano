package com.piano

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.piano.databinding.ActivityMainBinding
import com.piano.databinding.ActivityOktavaInfoBinding


class OktavaInfo : AppCompatActivity() {
    private lateinit var binding: ActivityOktavaInfoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOktavaInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val textOktavaNumber = binding.textView3
        val textOktavaDesc = binding.textView4

        val intent = intent
        val oktNum = intent.getStringExtra("OktNumber")
        val oktDesc = intent.getStringExtra("OktDesc")

        textOktavaNumber.text = oktNum
        textOktavaDesc.text = oktDesc



    }
    fun exit(v: View?) {
        finish()
    }
}