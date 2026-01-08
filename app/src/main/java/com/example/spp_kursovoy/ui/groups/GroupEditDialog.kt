package com.example.spp_kursovoy.ui.groups

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.spp_kursovoy.data.network.dto.groups.GroupDetailsDto
import com.example.spp_kursovoy.data.network.dto.UserDto
import com.example.spp_kursovoy.data.network.dto.groups.TeacherDto
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupEditDialog(
    viewModel: GroupsViewModel,
    group: GroupDetailsDto? = null,
    onDismiss: () -> Unit,
    onSaveSuccess: () -> Unit,
    isNew: Boolean
) {
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf(group?.title ?: "") }
    var selectedTeacher by remember { mutableStateOf(group?.teacher) }
    var allTeachers by remember { mutableStateOf<List<UserDto>>(emptyList()) }
    val selectedStudents = remember { mutableStateListOf<String>().apply {
        group?.students?.mapNotNull { it.id }?.let { addAll(it) }
    } }
    var allStudents by remember { mutableStateOf<List<UserDto>>(emptyList()) }
    var adminEmail by remember { mutableStateOf("") }
    var adminPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    // Загрузка учителей и студентов
    LaunchedEffect(Unit) {
        try {
            allTeachers = viewModel.getUsersByRole("TEACHER")
            allStudents = viewModel.getUsersByRole("STUDENT")
            Log.d("GroupEditDialog", "Teachers loaded: ${allTeachers.map { it.name }}")
            Log.d("GroupEditDialog", "Students loaded: ${allStudents.map { it.name }}")
        } catch (e: Exception) {
            Log.e("GroupEditDialog", "Failed to load users", e)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isNew) "Добавить группу" else "Редактировать группу",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Название группы
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название группы") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Выбор учителя
                var teacherExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = teacherExpanded,
                    onExpandedChange = { teacherExpanded = !teacherExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedTeacher?.name ?: "Выберите учителя",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Учитель") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor() // важно для правильного открытия
                    )
                    ExposedDropdownMenu(
                        expanded = teacherExpanded,
                        onDismissRequest = { teacherExpanded = false }
                    ) {
                        allTeachers.forEach { teacher ->
                            DropdownMenuItem(
                                text = { Text(teacher.name) },
                                onClick = {
                                    selectedTeacher = TeacherDto(
                                        id = teacher.id ?: "",
                                        name = teacher.name
                                    )
                                    teacherExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Выбор студентов
                var studentExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = studentExpanded,
                    onExpandedChange = { studentExpanded = !studentExpanded }
                ) {
                    OutlinedTextField(
                        value = if (selectedStudents.isEmpty()) "Выберите студентов" else "${selectedStudents.size} выбрано",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Студенты") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = studentExpanded,
                        onDismissRequest = { studentExpanded = false }
                    ) {
                        allStudents.forEach { student ->
                            if (student.id !in selectedStudents) {
                                DropdownMenuItem(
                                    text = { Text(student.name) },
                                    onClick = {
                                        selectedStudents.add(student.id ?: "")
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Email администратора
                OutlinedTextField(
                    value = adminEmail,
                    onValueChange = { adminEmail = it },
                    label = { Text("Email администратора") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Пароль администратора
                OutlinedTextField(
                    value = adminPassword,
                    onValueChange = { adminPassword = it },
                    label = { Text("Пароль администратора") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage, color = MaterialTheme.colorScheme.error)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Кнопки отмены и сохранения
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) { Text("Отмена") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        scope.launch {
                            try {
                                if (isNew) {
                                    viewModel.createGroup(
                                        title,
                                        selectedTeacher?.id ?: "",
                                        selectedStudents,
                                        adminEmail,
                                        adminPassword
                                    )
                                } else if (group != null) {
                                    viewModel.updateGroup(
                                        group.id,
                                        title,
                                        selectedTeacher?.id,
                                        selectedStudents,
                                        adminEmail,
                                        adminPassword
                                    )
                                }
                                onSaveSuccess()
                            } catch (e: Exception) {
                                errorMessage = "Ошибка: ${e.message}"
                                Log.e("GroupEditDialog", "Save error", e)
                            }
                        }
                    }) {
                        Text(if (isNew) "Создать" else "Сохранить")
                    }
                }
            }
        }
    }
}
