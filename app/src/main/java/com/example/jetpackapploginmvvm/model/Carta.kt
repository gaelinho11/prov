package com.example.jetpackapploginmvvm.model

data class Carta(val nom: String, val valor: Int)

object Baraja {
    val llistaCartes = listOf(
        Carta("As", 11), Carta("2", 2), Carta("3", 3), Carta("4", 4),
        Carta("5", 5), Carta("6", 6), Carta("7", 7), Carta("8", 8),
        Carta("9", 9), Carta("10", 10), Carta("Sota", 10),
        Carta("Cavall", 10), Carta("Rei", 10)
    )
}