package model

data class User(
    val id: String? = "",
    val email: String = "",
    val firstName: String? = "",
    val lastName: String? = "",
    val gender: String? = "",
    val notificationsEnabled: Boolean? = true,
    val photoUrl: String? = "",
)