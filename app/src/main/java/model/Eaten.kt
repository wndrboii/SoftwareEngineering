package model

data class Eaten(
    val id: String? = "",
    val date: Long = System.currentTimeMillis(),
    val userId: String = "",
    val calories: Double = 0.0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0,
    val dishName: String = "",
    val photoUrl: String = "",
    val category: String = ""
)