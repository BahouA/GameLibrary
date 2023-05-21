package com.example.gamelibrary

import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity

class SecondActivity : AppCompatActivity() {
    private lateinit var profile: TextView
    private lateinit var gameNameEditText: EditText
    private lateinit var rankEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        profile = findViewById(R.id.profile)
        gameNameEditText = findViewById(R.id.editText2)
        rankEditText = findViewById(R.id.editText3)

        val toggle = findViewById<View>(R.id.toggle1) as ToggleButton
        toggle.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                startService(Intent(this, NewService::class.java))
            } else {
                stopService(Intent(this, NewService::class.java))
            }

        }

        val bundle: Bundle? = intent.extras
        bundle?.let {
            val name = bundle.getString("name")
            profile.text = "$name's Game Library"
        }
    }

    fun onClickAddGame(view: View?) {
        val gameName = gameNameEditText.text.toString()
        val rank = rankEditText.text.toString()

        val values = ContentValues().apply {
            put(GameLibraryProvider.NAME, gameName)
            put(GameLibraryProvider.RANK, rank)
        }

        val uri = contentResolver.insert(GameLibraryProvider.CONTENT_URI, values)
        if (uri != null) {
            Toast.makeText(this, "Game added: $gameName (Rank: $rank)", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to add game", Toast.LENGTH_SHORT).show()
        }

        gameNameEditText.text.clear()
        rankEditText.text.clear()
    }

    fun onClickRetrieveGames(view: View?) {
        val URL = "content://com.example.GameLibrary.GameLibraryProvider/games"
        val gamesUri = Uri.parse(URL)
        val cursor: Cursor? = contentResolver.query(gamesUri, null, null, null, null)
        if (cursor != null && cursor.moveToFirst()) {
            do {
                val gameName = cursor.getString(cursor.getColumnIndexOrThrow("GameName"))
                val rank = cursor.getString(cursor.getColumnIndexOrThrow("Rank"))
                val gameInfo = "Game: $gameName, Rank: $rank"
                Toast.makeText(this, gameInfo, Toast.LENGTH_SHORT).show()
            } while (cursor.moveToNext())
            cursor.close()
        } else {
            Toast.makeText(this, "No games found", Toast.LENGTH_SHORT).show()
        }
    }

    fun onClickUpdateRank(view: View?) {
        val gameName = gameNameEditText.text.toString()
        val newRank = rankEditText.text.toString()

        val values = ContentValues()
        values.put("Rank", newRank)

        val selection = "GameName = ?"
        val selectionArgs = arrayOf(gameName)

        val gamesUri = Uri.parse("content://com.example.GameLibrary.GameLibraryProvider/games")
        val updatedRows = contentResolver.update(gamesUri, values, selection, selectionArgs)

        if (updatedRows > 0) {
            Toast.makeText(this, "Rank updated successfully", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to update rank", Toast.LENGTH_SHORT).show()
        }
    }
}
