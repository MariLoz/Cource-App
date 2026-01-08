package com.example.spp_kursovoy.ui.schedule

import android.util.Log
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.spp_kursovoy.data.auth.AuthStorage
import com.example.spp_kursovoy.data.network.dto.groups.GroupDetailsDto
import com.example.spp_kursovoy.data.network.dto.lessons.AttendanceItemDto
import com.example.spp_kursovoy.data.network.dto.lessons.CreateLessonRequest
import com.example.spp_kursovoy.data.network.dto.lessons.LessonDetailsDto
import com.example.spp_kursovoy.data.network.dto.schedule.ScheduleItemDto
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

@Composable
fun ScheduleScreen(authStorage: AuthStorage) {
    val viewModel = remember { ScheduleViewModel(authStorage) }

    val today = LocalDate.now()
    val fromDate = today.minusDays(3)
    val toDate = today.plusDays(3)

    LaunchedEffect(Unit) { viewModel.loadSchedule(fromDate, toDate) }

    val schedule by viewModel.schedule.collectAsState()
    var expandedLessonId by remember { mutableStateOf<String?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val isAdmin = authStorage.getUser()?.role == "ADMIN"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Расписание: $fromDate — $toDate",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )

            if (isAdmin) {
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { showAddDialog = true }) {
                    Text("Добавить занятие")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (schedule.isEmpty()) {
            Text("Нет занятий в выбранном диапазоне")
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(schedule) { item ->
                    ScheduleItemCardExpanded(
                        item = item,
                        authStorage = authStorage,
                        isExpanded = expandedLessonId == item.id,
                        onExpandToggle = {
                            expandedLessonId = if (expandedLessonId == item.id) null else item.id
                        },
                        reloadSchedule = { viewModel.loadSchedule(fromDate, toDate) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    if (showAddDialog) {
        AddLessonDialog(
            viewModel = viewModel,
            onDismiss = { showAddDialog = false },
            reloadSchedule = { viewModel.loadSchedule(fromDate, toDate) }
        )
    }
}

@Composable
fun AddLessonDialog(
    viewModel: ScheduleViewModel,
    onDismiss: () -> Unit,
    reloadSchedule: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var groups by remember { mutableStateOf<List<GroupDetailsDto>>(emptyList()) }
    var selectedGroup by remember { mutableStateOf<GroupDetailsDto?>(null) }

    var dateText by remember { mutableStateOf("") } // yyyy-MM-dd
    var timeText by remember { mutableStateOf("") } // HH:mm
    var duration by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        try {
            groups = viewModel.loadAllGroups()
        } catch (e: Exception) {
            Log.e("AddLessonDialog", "Failed to load groups", e)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.statusBarsPadding().padding(16.dp)) {
                Text("Добавить занятие", style = MaterialTheme.typography.titleMedium)

                Spacer(modifier = Modifier.height(12.dp))

                // Выбор группы
                var expandedGroup by remember { mutableStateOf(false) }
                Box {
                    Button(onClick = { expandedGroup = true }) {
                        Text(selectedGroup?.title ?: "Выберите группу")
                    }
                    DropdownMenu(
                        expanded = expandedGroup,
                        onDismissRequest = { expandedGroup = false }
                    ) {
                        groups.forEach { group ->
                            DropdownMenuItem(onClick = {
                                selectedGroup = group
                                expandedGroup = false
                            }, text = { Text(group.title) })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Дата
                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("Дата (yyyy-MM-dd)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val now = Calendar.getInstance()
                            DatePickerDialog(context, { _, year, month, dayOfMonth ->
                                dateText = "%04d-%02d-%02d".format(year, month + 1, dayOfMonth)
                            }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show()
                        }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Время
                OutlinedTextField(
                    value = timeText,
                    onValueChange = { timeText = it },
                    label = { Text("Время (HH:mm)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val now = Calendar.getInstance()
                            TimePickerDialog(context, { _, hour, minute ->
                                timeText = "%02d:%02d".format(hour, minute)
                            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show()
                        }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Длительность
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Длительность (мин)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("Отмена") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        scope.launch {
                            try {
                                if (selectedGroup != null && dateText.isNotBlank() && timeText.isNotBlank() && duration.isNotBlank()) {
                                    val dateTime = LocalDate.parse(dateText, DateTimeFormatter.ISO_DATE)
                                        .atTime(timeText.split(":")[0].toInt(), timeText.split(":")[1].toInt())
                                    val dateTimeIso = dateTime.toString() + "Z"
                                    val request = CreateLessonRequest(
                                        groupId = selectedGroup!!.id,
                                        dateTime = dateTimeIso,
                                        durationMinutes = duration.toInt()
                                    )
                                    viewModel.createLesson(request)
                                    reloadSchedule()
                                    onDismiss()
                                }
                            } catch (e: Exception) {
                                Log.e("AddLessonDialog", "Failed to create lesson", e)
                            }
                        }
                    }) {
                        Text("Добавить")
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduleItemCardExpanded(
    item: ScheduleItemDto,
    authStorage: AuthStorage,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    reloadSchedule: () -> Unit
) {
    val isAdmin = authStorage.getUser()?.role == "ADMIN"
    val isTeacher = authStorage.getUser()?.role == "TEACHER"
    val canEdit = isAdmin || isTeacher

    var lessonDetails by remember { mutableStateOf<LessonDetailsDto?>(null) }
    var groupStudents by remember { mutableStateOf<List<AttendanceItemDto>>(emptyList()) }
    var selectedStatus by remember { mutableStateOf(item.status) }
    var currentStatus by remember { mutableStateOf(item.status) }
    var attendanceState by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }
    var changesMade by remember { mutableStateOf(false) }

    val viewModel = remember { ScheduleViewModel(authStorage) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(isExpanded) {
        if (isExpanded && canEdit) {
            try {
                lessonDetails = viewModel.loadLesson(item.id)
                groupStudents = viewModel.loadGroupStudents(item.group.id)
                attendanceState = groupStudents.associate { student ->
                    val present = lessonDetails?.attendance?.find { it.studentId == student.studentId }?.present ?: false
                    student.studentId to present
                }
                selectedStatus = lessonDetails?.status ?: item.status
                currentStatus = lessonDetails?.status ?: item.status
                changesMade = false
            } catch (e: Exception) {
                Log.e("ScheduleItemCard", "Error loading lesson details", e)
            }
        }
    }

    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onExpandToggle() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Группа: ${item.group.title}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    "Статус: $currentStatus",
                    color = when (currentStatus) {
                        "PLANNED" -> Color.Blue
                        "COMPLETED" -> Color.Green
                        "CANCELLED" -> Color.Red
                        else -> Color.Black
                    }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Разделяем дату и время на два поля
            val dateTimeFormatted = try {
                LocalDateTime.parse(item.dateTime.removeSuffix("Z"))
            } catch (e: Exception) {
                null
            }
            val datePart = dateTimeFormatted?.toLocalDate()?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) ?: "-"
            val timePart = dateTimeFormatted?.toLocalTime()?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "-"

            Text("Дата: $datePart", style = MaterialTheme.typography.bodySmall)
            Text("Время: $timePart", style = MaterialTheme.typography.bodySmall)
            Text("Длительность: ${item.durationMinutes} мин", style = MaterialTheme.typography.bodySmall)
            Text("ID занятия: ${item.id}", style = MaterialTheme.typography.bodySmall)

            if (isExpanded && canEdit && lessonDetails != null) {
                Spacer(modifier = Modifier.height(8.dp))

                Text("Статус:")
                var statusDropdownExpanded by remember { mutableStateOf(false) }
                Box {
                    Button(onClick = { statusDropdownExpanded = true }) {
                        Text(selectedStatus)
                    }
                    DropdownMenu(
                        expanded = statusDropdownExpanded,
                        onDismissRequest = { statusDropdownExpanded = false }
                    ) {
                        listOf("PLANNED", "COMPLETED", "CANCELLED").forEach { status ->
                            DropdownMenuItem(onClick = {
                                selectedStatus = status
                                statusDropdownExpanded = false
                                changesMade = true
                            }, text = { Text(status) })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Посещаемость:")

                groupStudents.forEach { student ->
                    val present = attendanceState[student.studentId] ?: false
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = present,
                            onCheckedChange = { checked ->
                                attendanceState = attendanceState.toMutableMap().apply {
                                    put(student.studentId, checked)
                                }
                                changesMade = true
                            }
                        )
                        Text(student.name)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row {
                    if (changesMade) {
                        Button(onClick = {
                            scope.launch {
                                viewModel.updateLesson(
                                    lessonDetails!!.id,
                                    selectedStatus,
                                    attendanceState
                                )
                                currentStatus = selectedStatus
                                changesMade = false
                                reloadSchedule()
                            }
                        }) {
                            Text("Подтвердить изменения")
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    if (isAdmin) {
                        Button(onClick = {
                            scope.launch {
                                viewModel.deleteLesson(item.id)
                                reloadSchedule()
                                onExpandToggle()
                            }
                        }) {
                            Text("Удалить занятие")
                        }
                    }
                }
            }
        }
    }
}
