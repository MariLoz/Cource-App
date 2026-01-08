package com.example.spp_kursovoy.data.network.dto.lessons

data class CreateLessonRequest(
    val groupId: String,
    val dateTime: String,
    val durationMinutes: Int
)
