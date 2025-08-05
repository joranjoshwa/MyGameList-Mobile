package com.example.mygamelist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mygamelist.R
import com.example.mygamelist.Game
import com.bumptech.glide.Glide

class GameAdapter(
    private var games: List<Game>,
    private val onItemClick: (Game) -> Unit
) : RecyclerView.Adapter<GameAdapter.GameViewHolder>() {

    inner class GameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.textViewGameTitle)
        private val details: TextView = itemView.findViewById(R.id.textViewGameDetails)
        private val status: TextView = itemView.findViewById(R.id.textViewGameStatus)
        private val image: ImageView = itemView.findViewById(R.id.imageViewGameCover)

        fun bind(game: Game) {
            title.text = game.nome
            details.text = game.plataforma
            status.text = game.status

            // Carrega imagem
            Glide.with(itemView)
                .load(game.imagemUrl)
                .placeholder(R.drawable.ic_image_placeholder)
                .into(image)

            itemView.setOnClickListener {
                onItemClick(game)
            }

            // Define o background do status com base no valor
            val statusLower = game.status.lowercase()

            val backgroundRes = when (statusLower) {
                "zerado" -> R.drawable.bg_status_chip
                "zerando" -> R.drawable.bg_status_chip_zerando
                "não jogado" -> R.drawable.bg_status_chip_nao_jogado
                else -> R.drawable.bg_status_chip_nao_jogado
            }

            status.setBackgroundResource(backgroundRes)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_game, parent, false)
        return GameViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        holder.bind(games[position])
    }

    override fun getItemCount(): Int = games.size

    fun updateList(newGames: List<Game>) {
        games = newGames
        notifyDataSetChanged()
    }
}