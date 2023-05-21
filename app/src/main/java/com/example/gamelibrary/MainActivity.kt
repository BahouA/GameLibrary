package com.example.gamelibrary

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val name: EditText = findViewById(R.id.name)
        val continueButton: Button = findViewById(R.id.continueButton)
        continueButton.setOnClickListener{
            val intent = Intent(this, SecondActivity::class.java)
            intent.putExtra("name", name.text.toString())
            startActivity(intent)
        }
    }
}