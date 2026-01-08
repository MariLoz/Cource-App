package com.example.spp_kursovoy.ui.groups

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spp_kursovoy.data.auth.AuthStorage
import com.example.spp_kursovoy.data.network.ApiClient
import com.example.spp_kursovoy.data.network.dto.UserDto
import com.example.spp_kursovoy.data.network.dto.groups.CreateGroupRequest
import com.example.spp_kursovoy.data.network.dto.groups.GroupDetailsDto
import com.example.spp_kursovoy.data.network.dto.groups.UpdateGroupRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GroupsViewModel(private val authStorage: AuthStorage) : ViewModel() {
    private val api = ApiClient.create(authStorage)

    private val _groups = MutableStateFlow<List<GroupDetailsDto>>(emptyList())
    val groups: StateFlow<List<GroupDetailsDto>> = _groups

    fun loadGroups() {
        viewModelScope.launch {
            try {
                _groups.value = api.getAllGroups()
            } catch (e: Exception) {
                _groups.value = emptyList()
                Log.e("GroupsViewModel", "Failed to load groups", e)
            }
        }
    }

    suspend fun getGroupDetails(groupId: String) = api.getGroup(groupId)

    suspend fun createGroup(
        title: String,
        teacherId: String,
        studentIds: List<String>,
        adminEmail: String,
        adminPassword: String
    ) = api.createGroup(
        CreateGroupRequest(title, teacherId, studentIds, adminEmail, adminPassword)
    )

    suspend fun updateGroup(
        groupId: String,
        title: String?,
        teacherId: String?,
        studentIds: List<String>?,
        adminEmail: String,
        adminPassword: String
    ) = api.updateGroup(
        groupId,
        UpdateGroupRequest(title, teacherId, studentIds, adminEmail, adminPassword)
    )

    suspend fun deleteGroup(groupId: String, adminEmail: String, adminPassword: String) =
        api.deleteGroup(groupId, mapOf(
            "adminEmail" to adminEmail,
            "adminPassword" to adminPassword
        ))

    suspend fun getUsersByRole(role: String): List<UserDto> =
        api.getUsers(role = role)
}
