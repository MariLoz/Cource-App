package com.example.spp_kursovoy.data.network.dto.groups

data class GroupDetailsDto(
    val id: String,
    val title: String,
    val teacher: TeacherDto,
    val students: List<StudentDto>
)

data class TeacherDto(
    val id: String,
    val name: String
)

data class StudentDto(
    val id: String,
    val name: String,
    val email: String
)