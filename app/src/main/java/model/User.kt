package model

data class User(
    val id: String? = "",
    val email: String = "",
    val firstName: String? = "",
    val lastName: String? = "",
    val gender: String? = "",
    val photoUrl: String? = "",
    val calsMax: Double? = 0.0
)