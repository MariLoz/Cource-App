package com.example.spp_kursovoy.data.network.dto.lessons

data class AttendanceUpdateRequest(
    val attendance: List<AttendanceUpdateItem>
)

data class AttendanceUpdateItem(
    val studentId: String,
    val present: Boolean
)
