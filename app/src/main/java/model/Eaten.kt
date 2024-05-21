package com.example.softwareengineering.model

data class Eaten(
    val id: String? = "",
    val date: Long = System.currentTimeMillis(),
    val userId: String = "",
    val posilekId: String = ""
)