package com.example.mygamelist

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch

class GameDetailsActivity : AppCompatActivity() {

    private lateinit var imageViewGameArt: ImageView
    private lateinit var textViewGameTitleDetails: TextView
    private lateinit var textViewPlataformValue: TextView
    private lateinit var textViewGenreValue: TextView
    private lateinit var textViewStatusValue: TextView
    private lateinit var textViewRatingValue: TextView
    private lateinit var ratingBarDetails: RatingBar
    private lateinit var textViewDescription: TextView
    private lateinit var textViewReview: TextView

    private lateinit var buttonEdit: Button
    private lateinit var buttonDelete: Button

    private lateinit var jogo: Game
    private lateinit var dbHelper: GameDatabaseHelper

    private val editGameLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val jogoEditado = result.data?.getSerializableExtra("jogoEditado") as? Game
            if (jogoEditado != null) {
                val returnIntent = Intent()
                returnIntent.putExtra("jogoEditado", jogoEditado)
                setResult(Activity.RESULT_OK, returnIntent)
                finish()
            }
        }
    }

    private fun updateRatingProgressBars(ratings: List<RatingBreakdown>) {
        val titleToViewIds = mapOf(
            "exceptional" to Pair(R.id.progressBar5, R.id.textViewPercent5),
            "recommended" to Pair(R.id.progressBar4, R.id.textViewPercent4),
            "meh" to Pair(R.id.progressBar3, R.id.textViewPercent3),
            "skip" to Pair(R.id.progressBar1, R.id.textViewPercent1)
        )

        for (rating in ratings) {
            val ids = titleToViewIds[rating.title.lowercase()]
            if (ids != null) {
                val (progressBarId, percentTextId) = ids
                val progressBar = findViewById<ProgressBar>(progressBarId)
                val percentText = findViewById<TextView>(percentTextId)

                progressBar.progress = rating.percent.toInt()
                percentText.text = "${rating.percent.toInt()}%"
            } else {
                Log.d("RATING_NAO_MAPEADO", "Título '${rating.title}' sem barra associada.")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_details)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Instancia o DBHelper
        dbHelper = GameDatabaseHelper(this)

        imageViewGameArt = findViewById(R.id.imageViewGameArt)
        textViewGameTitleDetails = findViewById(R.id.textViewGameTitleDetails)
        textViewPlataformValue = findViewById(R.id.textViewPlataformValue)
        textViewGenreValue = findViewById(R.id.textViewGenreValue)
        textViewStatusValue = findViewById(R.id.textViewStatusValue)
        textViewRatingValue = findViewById(R.id.textViewRatingValue)
        ratingBarDetails = findViewById(R.id.ratingBarDetails)
        textViewDescription = findViewById(R.id.textViewDescription)
        textViewReview = findViewById(R.id.textViewReviewContent)

        buttonEdit = findViewById(R.id.buttonEdit)
        buttonDelete = findViewById(R.id.buttonDelete)

        val recebido = intent.getSerializableExtra("jogoSelecionado") as? Game
        if (recebido != null) {
            jogo = recebido

            // Carrega a imagem (caso tenha sido salva localmente na base)
            if (!jogo.imagemUrl.isNullOrBlank()) {
                Glide.with(this)
                    .load(jogo.imagemUrl)
                    .placeholder(R.drawable.ic_image_placeholder) // opcional
                    .into(imageViewGameArt)
            } else {
                imageViewGameArt.setImageResource(R.drawable.ic_image_placeholder)
            }

            textViewGameTitleDetails.text = jogo.nome
            textViewPlataformValue.text = jogo.plataforma
            textViewGenreValue.text = jogo.genero
            textViewStatusValue.text = jogo.status

            // Definir background colorido conforme status
            val statusLower = jogo.status.lowercase()

            val backgroundRes = when (statusLower) {
                "zerado" -> R.drawable.bg_status_chip        // Verde
                "zerando" -> R.drawable.bg_status_chip_zerando // Amarelo
                "não jogado" -> R.drawable.bg_status_chip_nao_jogado // Cinza/Azul
                else -> R.drawable.bg_status_chip_nao_jogado
            }

            textViewStatusValue.setBackgroundResource(backgroundRes)

            textViewRatingValue.text = jogo.nota.toString()
            ratingBarDetails.rating = jogo.nota
            textViewDescription.text = jogo.descricao
            textViewReview.text = if (jogo.review.isNullOrBlank()) {
                "Nenhuma review adicionada."
            } else {
                jogo.review
            }
        }

        val apiKey = "9f5a92663651420ba6a1aa767acf63a6"

        lifecycleScope.launch {
            try {
                val searchResponse = RetrofitClient.instance.searchGames(apiKey, jogo.nome)
                val firstResult = searchResponse.results.firstOrNull()

                if (firstResult != null) {
                    val gameId = firstResult.id

                    val response = RetrofitClient.instance.getGameDetails(gameId, apiKey)

                    // Atualiza avaliação geral
                    textViewRatingValue.text = response.rating.toString()
                    ratingBarDetails.rating = response.rating

                    // Atualiza número de avaliações
                    val reviewsCountText = "${response.ratings_count} avaliações"
                    findViewById<TextView>(R.id.textViewReviewsCount).text = reviewsCountText

                    // Atualiza progressos
                    updateRatingProgressBars(response.ratings)

                    // Atualiza descrição oficial
                    Log.d("API_DESCRICAO", "Descrição recebida: ${response.description_raw}")
                    textViewDescription.text = response.description_raw ?: "Descrição oficial não disponível."

                    val plataformas = response.platforms?.joinToString(", ") { it.platform.name }
                    textViewPlataformValue.text = plataformas ?: "Plataformas não encontradas"
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        buttonEdit.setOnClickListener {
            val intent = Intent(this, AddGameActivity::class.java)
            intent.putExtra("jogoParaEditar", jogo)
            editGameLauncher.launch(intent)
        }

        buttonDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Excluir jogo")
                .setMessage("Tem certeza que deseja excluir este jogo?")
                .setPositiveButton("Sim") { _, _ ->
                    // Exclui do banco
                    dbHelper.deleteGame(jogo.id)

                    // Retorna para MainActivity
                    val intent = Intent()
                    intent.putExtra("jogoParaExcluir", jogo)
                    setResult(Activity.RESULT_FIRST_USER, intent)
                    finish()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
