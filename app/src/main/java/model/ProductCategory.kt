package model

data class ProductCategory(
    val id: String? = "",
    val name: String,
    val dishesIds: MutableList<String?> = mutableListOf(),
    val userId: String? = ""
){
    constructor() : this("", "", mutableListOf<String?>(), "")
}
