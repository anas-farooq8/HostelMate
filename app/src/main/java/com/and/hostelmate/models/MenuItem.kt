package com.and.hostelmate.models

data class MenuItem(
    val menuId: Int,
    val name: String,
    val description: String,
    val rating: Float,
    val imageAddress: String,
    val day: String,
    val mealType: String  // Values are 'breakfast' or 'dinner'
)
