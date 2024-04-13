package com.example.softwareengineering.model

data class Skladnik(
    val id: String? = "",
    val name: String = "",
    val calories: Int = 0,
    val protein: Int = 0,
    val carbs: Int = 0,
    val fat: Int = 0
)