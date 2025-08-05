package com.example.mygamelist

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.widget.TextView
import android.view.View

class FilterActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAddFiltered: FloatingActionButton
    private lateinit var gameAdapter: GameAdapter
    private lateinit var dbHelper: GameDatabaseHelper
    private var gameList: MutableList<Game> = mutableListOf()
    private lateinit var textEmpty: TextView

    private val editGameLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                val jogoEditado = data?.getSerializableExtra("jogoEditado") as? Game
                jogoEditado?.let {
                    dbHelper.updateGame(it)
                    val index = gameList.indexOfFirst { g -> g.id == it.id }
                    if (index != -1) {
                        gameList[index] = it
                        gameAdapter.notifyItemChanged(index)
                    }
                }
            }
            Activity.RESULT_FIRST_USER -> {
                val jogoExcluido = data?.getSerializableExtra("jogoParaExcluir") as? Game
                jogoExcluido?.let {
                    dbHelper.deleteGame(it.id)
                    val index = gameList.indexOfFirst { g -> g.id == it.id }
                    if (index != -1) {
                        gameList.removeAt(index)
                        gameAdapter.notifyItemRemoved(index)
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filters)

        tabLayout = findViewById(R.id.tabLayout)
        recyclerView = findViewById(R.id.recyclerViewFiltered)
        fabAddFiltered = findViewById(R.id.fabAddFiltered)
        dbHelper = GameDatabaseHelper(this)
        textEmpty = findViewById(R.id.textEmpty)

        // Inicializa RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        gameAdapter = GameAdapter(gameList) { selectedGame ->
            val intent = Intent(this, GameDetailsActivity::class.java)
            intent.putExtra("jogoSelecionado", selectedGame)
            editGameLauncher.launch(intent)
        }
        recyclerView.adapter = gameAdapter
        recyclerView.adapter = gameAdapter

        // Carrega inicialmente a primeira aba (Zerado)
        loadGamesByStatus("Zerado")

        // Listener para trocar aba e filtrar os jogos
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val status = tab?.text.toString()
                loadGamesByStatus(status)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Botão para adicionar novo jogo (opcional: pode abrir AddGameActivity)
        fabAddFiltered.setOnClickListener {
            val intent = Intent(this, AddGameActivity::class.java)
            startActivity(intent)
        }

        // Voltar no ícone de voltar da toolbar
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar).setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadGamesByStatus(status: String) {
        gameList.clear()
        gameList.addAll(dbHelper.getGamesByStatus(status))
        gameAdapter.notifyDataSetChanged()

        if (gameList.isEmpty()) {
            textEmpty.visibility = View.VISIBLE
        } else {
            textEmpty.visibility = View.GONE
        }
    }
}
