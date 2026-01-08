package com.example.spp_kursovoy.data.network

import com.example.spp_kursovoy.data.network.dto.LoginRequest
import com.example.spp_kursovoy.data.network.dto.LoginResponse
import com.example.spp_kursovoy.data.network.dto.RefreshRequest
import com.example.spp_kursovoy.data.network.dto.UserDto
import com.example.spp_kursovoy.data.network.dto.UserProfileDto
import com.example.spp_kursovoy.data.network.dto.groups.CreateGroupRequest
import com.example.spp_kursovoy.data.network.dto.groups.GroupDetailsDto
import com.example.spp_kursovoy.data.network.dto.groups.UpdateGroupRequest
import com.example.spp_kursovoy.data.network.dto.lessons.AttendanceUpdateRequest
import com.example.spp_kursovoy.data.network.dto.lessons.CreateLessonRequest
import com.example.spp_kursovoy.data.network.dto.lessons.LessonDetailsDto
import com.example.spp_kursovoy.data.network.dto.schedule.ScheduleItemDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query


interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): LoginResponse

    @GET("schedule")
    suspend fun getSchedule(
        @Query("from") from: String,
        @Query("to") to: String
    ): List<ScheduleItemDto>

    @GET("lessons/{lessonId}")
    suspend fun getLesson(
        @Path("lessonId") lessonId: String
    ): LessonDetailsDto

    @PATCH("lessons/{lessonId}/attendance")
    suspend fun updateAttendance(
        @Path("lessonId") lessonId: String,
        @Body body: AttendanceUpdateRequest
    ): LessonDetailsDto

    @PATCH("lessons/{lessonId}/status")
    suspend fun updateLessonStatus(
        @Path("lessonId") lessonId: String,
        @Body body: Map<String, String>
    )

    @DELETE("lessons/{lessonId}")
    suspend fun deleteLesson(
        @Path("lessonId") lessonId: String
    )

    @POST("lessons")
    suspend fun createLesson(
        @Body body: CreateLessonRequest
    ): LessonDetailsDto

    @GET("groups/{groupId}")
    suspend fun getGroup(
        @Path("groupId") groupId: String
    ): GroupDetailsDto

    @GET("groups")
    suspend fun getAllGroups(): List<GroupDetailsDto>

    @POST("groups")
    suspend fun createGroup(@Body request: CreateGroupRequest): GroupDetailsDto

    @PATCH("groups/{groupId}")
    suspend fun updateGroup(
        @Path("groupId") groupId: String,
        @Body request: UpdateGroupRequest
    ): GroupDetailsDto

    @HTTP(method = "DELETE", path = "groups/{groupId}", hasBody = true)
    suspend fun deleteGroup(
        @Path("groupId") groupId: String,
        @Body body: Map<String, String>
    )

    @GET("users")
    suspend fun getUsers(@Query("role") role: String? = null): List<UserDto>

    @GET("users/me")
    suspend fun getMyProfile(): UserProfileDto

    @POST("/auth/logout")
    suspend fun logout(@Body body: Map<String, String>)
}