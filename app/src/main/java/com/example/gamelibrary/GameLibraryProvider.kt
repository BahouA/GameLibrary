package com.example.gamelibrary

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.text.TextUtils

class GameLibraryProvider : ContentProvider() {

    companion object {
        // Column names
        const val ID = "_id"
        const val NAME = "GameName"
        const val RANK = "Rank"

        private const val PROVIDER_NAME = "com.example.GameLibrary.GameLibraryProvider"
        private const val URL = "content://$PROVIDER_NAME/games"
        val CONTENT_URI = Uri.parse(URL)

        private const val GAMES = 1
        private const val GAME_ID = 2

        private var uriMatcher: UriMatcher? = null

        private const val DATABASE_NAME = "GameLibrary"
        private const val GAMES_TABLE_NAME = "games"
        private const val DATABASE_VERSION = 1

        private const val CREATE_DB_TABLE = "CREATE TABLE $GAMES_TABLE_NAME " +
                "($ID INTEGER PRIMARY KEY AUTOINCREMENT, $NAME TEXT NOT NULL, $RANK INTEGER NOT NULL);"

    }

    private var db: SQLiteDatabase? = null

    private class DatabaseHelper(context: Context?) :
        SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(CREATE_DB_TABLE)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE IF EXISTS $GAMES_TABLE_NAME")
            onCreate(db)
        }
    }

    override fun onCreate(): Boolean {
        val context = context
        val dbHelper = DatabaseHelper(context)
        db = dbHelper.writableDatabase
        return db != null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val rowID = db!!.insert(GAMES_TABLE_NAME, "", values)
        if (rowID > 0) {
            val _uri = ContentUris.withAppendedId(CONTENT_URI, rowID)
            context!!.contentResolver.notifyChange(_uri, null)
            return _uri
        }
        throw SQLException("Failed to add a record into $uri")
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        var sortOrder = sortOrder
        val qb = SQLiteQueryBuilder()
        qb.tables = GAMES_TABLE_NAME
        when (uriMatcher!!.match(uri)) {
            GAME_ID -> qb.appendWhere(ID + "=" + uri.pathSegments[1])
            else -> {}
        }
        if (sortOrder == null || sortOrder == "") {
            sortOrder = RANK
        }
        val c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder)
        c.setNotificationUri(context!!.contentResolver, uri)
        return c
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        var count = 0
        when (uriMatcher!!.match(uri)) {
            GAMES -> count = db!!.update(GAMES_TABLE_NAME, values, selection, selectionArgs)
            GAME_ID -> {
                val id = uri.pathSegments[1]
                count = db!!.update(
                    GAMES_TABLE_NAME,
                    values,
                    "$ID = $id" + if (!TextUtils.isEmpty(selection)) " AND ($selection)" else "",
                    selectionArgs
                )
            }
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
        context!!.contentResolver.notifyChange(uri, null)
        return count
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        var count = 0
        when (uriMatcher!!.match(uri)) {
            GAMES -> count = db!!.delete(GAMES_TABLE_NAME, selection, selectionArgs)
            GAME_ID -> {
                val id = uri.pathSegments[1]
                count = db!!.delete(
                    GAMES_TABLE_NAME,
                    "$ID = $id" + if (!TextUtils.isEmpty(selection)) " AND ($selection)" else "",
                    selectionArgs
                )
            }
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
        context!!.contentResolver.notifyChange(uri, null)
        return count
    }

    override fun getType(uri: Uri): String? {
        return when (uriMatcher!!.match(uri)) {
            GAMES -> "vnd.android.cursor.dir/vnd.example.games"
            GAME_ID -> "vnd.android.cursor.item/vnd.example.games"
            else -> throw IllegalArgumentException("Unsupported URI: $uri")
        }
    }

    init {
        uriMatcher = UriMatcher(UriMatcher.NO_MATCH)
        uriMatcher!!.addURI(PROVIDER_NAME, "games", GAMES)
        uriMatcher!!.addURI(PROVIDER_NAME, "games/#", GAME_ID)
    }
}
