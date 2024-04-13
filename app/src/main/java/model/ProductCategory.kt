package com.example.softwareengineering.model

import com.example.softwareengineering.model.Skladnik

data class ProductCategory(
    val name: String,
    val products: MutableList<Skladnik>
)
