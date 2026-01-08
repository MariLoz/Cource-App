package com.example.spp_kursovoy.ui.schedule

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spp_kursovoy.data.auth.AuthStorage
import com.example.spp_kursovoy.data.network.ApiClient
import com.example.spp_kursovoy.data.network.dto.groups.GroupDetailsDto
import com.example.spp_kursovoy.data.network.dto.lessons.AttendanceItemDto
import com.example.spp_kursovoy.data.network.dto.lessons.AttendanceUpdateItem
import com.example.spp_kursovoy.data.network.dto.lessons.AttendanceUpdateRequest
import com.example.spp_kursovoy.data.network.dto.lessons.CreateLessonRequest
import com.example.spp_kursovoy.data.network.dto.lessons.LessonDetailsDto
import com.example.spp_kursovoy.data.network.dto.schedule.ScheduleItemDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime


private const val TAG = "ScheduleViewModel"

class ScheduleViewModel(private val authStorage: AuthStorage) : ViewModel() {

    private val api = ApiClient.create(authStorage)

    private val _schedule = MutableStateFlow<List<ScheduleItemDto>>(emptyList())
    val schedule: StateFlow<List<ScheduleItemDto>> = _schedule

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun loadSchedule(from: LocalDate, to: LocalDate) {
        viewModelScope.launch {
            try {
                val items = api.getSchedule(from.format(formatter), to.format(formatter))
                _schedule.value = items
            } catch (e: Exception) {
                Log.e(TAG, "Error loading schedule", e)
                _schedule.value = emptyList()
            }
        }
    }

    suspend fun loadLesson(lessonId: String): LessonDetailsDto {
        return api.getLesson(lessonId)
    }

    suspend fun loadGroupStudents(groupId: String): List<AttendanceItemDto> {
        val group: GroupDetailsDto = api.getGroup(groupId)
        return group.students.map { student ->
            AttendanceItemDto(
                studentId = student.id,
                name = student.name,
                present = false
            )
        }
    }

    suspend fun updateLesson(
        lessonId: String,
        status: String,
        attendance: Map<String, Boolean>
    ) {
        api.updateLessonStatus(lessonId, mapOf("status" to status))
        val attendanceList = attendance.map { (studentId, present) ->
            AttendanceUpdateItem(studentId, present)
        }
        api.updateAttendance(lessonId, AttendanceUpdateRequest(attendanceList))
    }

    suspend fun deleteLesson(lessonId: String) {
        api.deleteLesson(lessonId)
    }

    suspend fun loadAllGroups(): List<GroupDetailsDto> {
        return api.getAllGroups() // GET /groups
    }

    suspend fun createLesson(request: CreateLessonRequest): LessonDetailsDto {
        return api.createLesson(request) // POST /lessons
    }
}
