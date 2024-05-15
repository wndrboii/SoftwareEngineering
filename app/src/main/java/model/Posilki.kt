package com.example.softwareengineering.model

data class Posilki(
    val id: String? = "",
    val name: String = "",
    val category: String = "",
    val quantity: Int = 0,
    val products: List<Skladnik> = emptyList(),
    val photoUrl: String = "",

    val comments: Map<String, Comment>? = null,
    val userId: String? = "",
    val liked: MutableList<String?> = mutableListOf(),
    var checked: Boolean = false
){
    fun isLikedByUser(userId: String): Boolean {
        return liked.contains(userId)
    }
}