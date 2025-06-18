package com.example.mygamelist

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class GameDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_details)

        val title = intent.getStringExtra("title")
        val platform = intent.getStringExtra("platform")

        findViewById<TextView>(R.id.textViewGameTitle).text = title
        findViewById<TextView>(R.id.textViewGameDetails).text = platform
    }
}