package model

data class Skladnik(
    val id: String? = "",
    val name: String = "",
    val calories: Double = 0.0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0,
    val userId: String? = "",
    var checked: Boolean = false
)