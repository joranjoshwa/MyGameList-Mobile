package com.example.mygamelist

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class GameDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "games.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_NAME = "games"
        const val COLUMN_ID = "id"
        const val COLUMN_NOME = "nome"
        const val COLUMN_PLATAFORMA = "plataforma"
        const val COLUMN_GENERO = "genero"
        const val COLUMN_STATUS = "status"
        const val COLUMN_NOTA = "nota"
        const val COLUMN_DESCRICAO = "descricao"
        const val COLUMN_IMAGEM_URL = "imagemUrl"
        const val COLUMN_REVIEW = "review"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NOME TEXT NOT NULL,
                $COLUMN_PLATAFORMA TEXT NOT NULL,
                $COLUMN_GENERO TEXT NOT NULL,
                $COLUMN_STATUS TEXT NOT NULL,
                $COLUMN_NOTA REAL NOT NULL,
                $COLUMN_DESCRICAO TEXT,
                $COLUMN_IMAGEM_URL TEXT,
                $COLUMN_REVIEW TEXT
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertGame(game: Game): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NOME, game.nome)
            put(COLUMN_PLATAFORMA, game.plataforma)
            put(COLUMN_GENERO, game.genero)
            put(COLUMN_STATUS, game.status)
            put(COLUMN_NOTA, game.nota)
            put(COLUMN_DESCRICAO, game.descricao)
            put(COLUMN_IMAGEM_URL, game.imagemUrl)
            put(COLUMN_REVIEW, game.review)
        }
        return db.insert(TABLE_NAME, null, values)
    }

    fun updateGame(game: Game): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NOME, game.nome)
            put(COLUMN_PLATAFORMA, game.plataforma)
            put(COLUMN_GENERO, game.genero)
            put(COLUMN_STATUS, game.status)
            put(COLUMN_NOTA, game.nota)
            put(COLUMN_DESCRICAO, game.descricao)
            put(COLUMN_IMAGEM_URL, game.imagemUrl)
            put(COLUMN_REVIEW, game.review)
        }
        return db.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(game.id.toString()))
    }

    fun deleteGame(id: Int): Int {
        val db = writableDatabase
        return db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    fun getAllGames(): List<Game> {
        val games = mutableListOf<Game>()
        val db = readableDatabase
        val cursor = db.query(TABLE_NAME, null, null, null, null, null, "$COLUMN_ID DESC")

        with(cursor) {
            while (moveToNext()) {
                val game = Game(
                    id = getInt(getColumnIndexOrThrow(COLUMN_ID)),
                    nome = getString(getColumnIndexOrThrow(COLUMN_NOME)),
                    plataforma = getString(getColumnIndexOrThrow(COLUMN_PLATAFORMA)),
                    genero = getString(getColumnIndexOrThrow(COLUMN_GENERO)),
                    status = getString(getColumnIndexOrThrow(COLUMN_STATUS)),
                    nota = getFloat(getColumnIndexOrThrow(COLUMN_NOTA)),
                    descricao = getString(getColumnIndexOrThrow(COLUMN_DESCRICAO)),
                    imagemUrl = getString(getColumnIndexOrThrow(COLUMN_IMAGEM_URL)),
                    review = getString(getColumnIndexOrThrow(COLUMN_REVIEW))
                )
                games.add(game)
            }
            close()
        }

        return games
    }

    fun getGamesByStatus(status: String): List<Game> {
        val games = mutableListOf<Game>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            null,
            "$COLUMN_STATUS = ?",
            arrayOf(status),
            null,
            null,
            "$COLUMN_ID DESC"
        )

        with(cursor) {
            while (moveToNext()) {
                val game = Game(
                    id = getInt(getColumnIndexOrThrow(COLUMN_ID)),
                    nome = getString(getColumnIndexOrThrow(COLUMN_NOME)),
                    plataforma = getString(getColumnIndexOrThrow(COLUMN_PLATAFORMA)),
                    genero = getString(getColumnIndexOrThrow(COLUMN_GENERO)),
                    status = getString(getColumnIndexOrThrow(COLUMN_STATUS)),
                    nota = getFloat(getColumnIndexOrThrow(COLUMN_NOTA)),
                    descricao = getString(getColumnIndexOrThrow(COLUMN_DESCRICAO)),
                    imagemUrl = getString(getColumnIndexOrThrow(COLUMN_IMAGEM_URL)),
                    review = getString(getColumnIndexOrThrow(COLUMN_REVIEW))
                )
                games.add(game)
            }
            close()
        }

        return games
    }
}