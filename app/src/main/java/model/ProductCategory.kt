package com.example.softwareengineering.model

data class ProductCategory(
    val id: String? = "",
    val name: String,
    val dishes: MutableList<String?>,
    val userId: String? = ""
){
    constructor() : this("", "", mutableListOf<String?>(), "")
}
