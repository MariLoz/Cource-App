package com.example.spp_kursovoy.data.network.dto.groups

data class CreateGroupRequest(
    val title: String,
    val teacherId: String,
    val studentIds: List<String>,
    val adminEmail: String,
    val adminPassword: String
)
