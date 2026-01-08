package com.example.spp_kursovoy.data.network.dto.schedule

data class ScheduleItemDto(
    val id: String,
    val group: GroupShortDto,
    val dateTime: String,
    val durationMinutes: Int,
    val status: String
)

data class GroupShortDto(
    val id: String,
    val title: String
)