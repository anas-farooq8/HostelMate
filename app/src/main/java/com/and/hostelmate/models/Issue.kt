package com.and.hostelmate.models

data class Issue(
    val issueId: Int? = null,
    val title: String,
    val description: String? = null,
    val imageAddress: String? = null,
    val userId: String
)

