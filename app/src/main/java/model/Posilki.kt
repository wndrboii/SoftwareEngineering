package com.example.softwareengineering.model

data class Posilki(
    val id: String? = "",
    val name: String = "",
    val category: String = "",
    val quantity: Int = 0,
    val products: List<Skladnik> = emptyList(),
    val photoUrl: String = ""
)