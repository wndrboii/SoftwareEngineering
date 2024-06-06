package model

data class Measurement(
    val id: String? = "",
    val height: Int = 0,
    val weight: Int = 0,
    val shoulder: Int = 0,
    val arm: Int = 0,
    val chest: Int = 0,
    val waist: Int = 0,
    val hips: Int = 0,
    val thigh: Int = 0,
    val calves: Int = 0,
    val date: Long = System.currentTimeMillis(),
    val userId: String = "",
)