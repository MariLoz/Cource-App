package com.example.spp_kursovoy.data.network.dto.lessons

data class LessonDetailsDto(
    val id: String,
    val group: LessonGroupDto,
    val dateTime: String,
    val durationMinutes: Int,
    val status: String,
    val attendance: List<AttendanceItemDto>
)

data class LessonGroupDto(
    val id: String,
    val title: String,
    val teacher: TeacherDto
)

data class TeacherDto(
    val id: String,
    val name: String
)

data class AttendanceItemDto(
    val studentId: String,
    val name: String,
    val present: Boolean
)
