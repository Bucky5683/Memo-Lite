package com.example.memo_lite.domain.model

data class Memo(
    val id: String,
    val title: String,
    val body: String,
    val updatedAt: Long = System.currentTimeMillis()
)
