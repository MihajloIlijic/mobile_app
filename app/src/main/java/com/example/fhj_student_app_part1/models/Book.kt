package com.example.fhj_student_app_part1.models

import java.util.UUID

data class Book(
    val id: String = UUID.randomUUID().toString(),
    val author: String = "",
    val title: String = "",
    val status: BookStatus = BookStatus.UNREAD,
    val ownerId: String = "",
    val thoughts: String = "",
    val coverImageUrl: String = "",
    val createdAt: Long = 0,
    val updatedAt: Long = 0
)

enum class BookStatus {
    READ,
    UNREAD
} 