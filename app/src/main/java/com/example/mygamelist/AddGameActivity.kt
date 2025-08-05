package com.example.mygamelist

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class AddGameActivity : AppCompatActivity() {

    private lateinit var inputNome: AutoCompleteTextView
    private lateinit var inputPlataforma: TextInputEditText
    private lateinit var inputGenero: TextInputEditText
    private lateinit var inputReview: TextInputEditText
    private lateinit var inputStatus: AutoCompleteTextView
    private lateinit var ratingBar: RatingBar
    private lateinit var buttonSave: Button

    private var jogoEditando: Game? = null
    private lateinit var dbHelper: GameDatabaseHelper

    private var sugestoes: List<GameResult> = emptyList()

    private var imagemSelecionada: String? = null
    private lateinit var imageViewGame: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_game)

        // Instância do banco
        dbHelper = GameDatabaseHelper(this)

        // Referências
        inputNome = findViewById(R.id.inputNome)
        inputPlataforma = findViewById(R.id.inputPlataforma)
        inputGenero = findViewById(R.id.inputGenero)
        inputReview = findViewById(R.id.inputDescricao)
        inputStatus = findViewById(R.id.inputStatus)
        ratingBar = findViewById(R.id.ratingBar)
        buttonSave = findViewById(R.id.buttonSave)
        imageViewGame = findViewById(R.id.imageViewGamePlaceholder)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        // Preenche dropdown de status
        val statusOptions = listOf("Zerado", "Zerando", "Não jogado")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, statusOptions)
        inputStatus.setAdapter(adapter)

        inputStatus.setOnClickListener {
            inputStatus.showDropDown()
        }

        // Verifica se estamos em modo de edição
        jogoEditando = intent.getSerializableExtra("jogoParaEditar") as? Game
        jogoEditando?.let { jogo ->
            inputNome.setText(jogo.nome)
            inputPlataforma.setText(jogo.plataforma)
            inputGenero.setText(jogo.genero)
            inputReview.setText(jogo.review)
            inputStatus.setText(jogo.status, false)
            ratingBar.rating = jogo.nota
            buttonSave.text = "Salvar alterações"

            imagemSelecionada = jogo.imagemUrl
            if (!imagemSelecionada.isNullOrBlank()) {
                Glide.with(this)
                    .load(imagemSelecionada)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .into(imageViewGame)
            } else {
                imageViewGame.setImageResource(R.drawable.ic_image_placeholder)
            }
        }

        // Clique no botão de salvar
        buttonSave.setOnClickListener {
            val nome = inputNome.text.toString().trim()
            val plataforma = inputPlataforma.text.toString().trim()
            val genero = inputGenero.text.toString().trim()
            val status = inputStatus.text.toString().trim()
            val review = inputReview.text.toString().trim()
            val nota = ratingBar.rating

            if (nome.isEmpty() || plataforma.isEmpty() || genero.isEmpty() || status.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos obrigatórios.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val jogoFinal = Game(
                id = jogoEditando?.id ?: 0,
                nome = nome,
                plataforma = plataforma,
                genero = genero,
                status = status,
                nota = nota,
                descricao = jogoEditando?.descricao,
                imagemUrl = imagemSelecionada,
                review = review
            )

            val resultIntent = Intent()

            if (jogoEditando != null) {
                dbHelper.updateGame(jogoFinal)
                resultIntent.putExtra("jogoEditado", jogoFinal)
            } else {
                val novoId = dbHelper.insertGame(jogoFinal).toInt()
                val jogoComId = jogoFinal.copy(id = novoId)
                resultIntent.putExtra("novoJogo", jogoComId)
            }

            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        inputNome.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val nomeDigitado = s.toString().trim()
                if (nomeDigitado.length >= 3) {
                    buscarSugestoesDaRawg(nomeDigitado)
                }
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        inputNome.setOnItemClickListener { _, _, position, _ ->
            val jogoSelecionado = sugestoes.getOrNull(position)
            jogoSelecionado?.let { jogo ->
                buscarDetalhesDoJogo(jogo.id)
            }
        }

    }

    private fun buscarSugestoesDaRawg(query: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.searchGames(
                    apiKey = "9f5a92663651420ba6a1aa767acf63a6",
                    query = query
                )
                sugestoes = response.results ?: emptyList()
                val nomes = sugestoes.map { it.name }
                val adapter = ArrayAdapter(this@AddGameActivity, android.R.layout.simple_dropdown_item_1line, nomes)
                inputNome.setAdapter(adapter)
                inputNome.showDropDown()
            } catch (e: Exception) {
                Toast.makeText(this@AddGameActivity, "Erro ao buscar jogos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun buscarDetalhesDoJogo(id: Int) {
        lifecycleScope.launch {
            try {
                val game = RetrofitClient.instance.getGameDetails(
                    gameId = id,
                    apiKey = "9f5a92663651420ba6a1aa767acf63a6"
                )
                inputGenero.setText(game.genres.joinToString(", ") { it.name })
                // inputReview.setText(game.description_raw ?: "")
                imagemSelecionada = game.background_image
                inputPlataforma.setText(game.platforms?.joinToString(", ") { it.platform.name })

                Glide.with(this@AddGameActivity)
                    .load(imagemSelecionada)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .into(imageViewGame)

            } catch (e: Exception) {
                Toast.makeText(this@AddGameActivity, "Erro ao carregar detalhes: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


}
