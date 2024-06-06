package model

data class DailyNutrition(
    val id: String? = "",
    val time: String = "",
    val userId: String = "",
    val posilekId: String = "",
    val day : Int = 0
)