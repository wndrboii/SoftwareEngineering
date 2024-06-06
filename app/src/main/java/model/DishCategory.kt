package model

data class DishCategory(
    val id: String? = "",
    val name: String,
){
    constructor() : this("", "")
}
