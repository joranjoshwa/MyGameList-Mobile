package com.example.mygamelist

import java.io.Serializable

data class Game(
    val id: Int, // usaremos só como identificador local por enquanto
    val nome: String,
    val plataforma: String,
    val genero: String,
    val status: String, // Ex: "Zerado", "Zerando", "Não jogado"
    val nota: Float,
    val descricao: String?,
    val imagemUrl: String?,
    val review: String?
) : Serializable