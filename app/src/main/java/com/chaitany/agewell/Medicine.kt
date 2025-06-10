package com.chaitany.agewell

data class Medicine(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val quantity: Int = 0,
    val schedule: List<String> = listOf(),
    val mealTime: String = "",
    val userPhone: String = ""
)
