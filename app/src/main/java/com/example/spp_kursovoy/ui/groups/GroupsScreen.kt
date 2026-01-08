package com.example.spp_kursovoy.ui.groups

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.spp_kursovoy.data.auth.AuthStorage
import com.example.spp_kursovoy.data.network.dto.groups.GroupDetailsDto
import com.example.spp_kursovoy.data.network.dto.UserDto
import kotlinx.coroutines.launch

@Composable
fun GroupsScreen(authStorage: AuthStorage) {
    val viewModel = remember { GroupsViewModel(authStorage) }
    val scope = rememberCoroutineScope()
    val userRole = authStorage.getUser()?.role ?: "STUDENT"

    val groups by viewModel.groups.collectAsState()
    var selectedGroup by remember { mutableStateOf<GroupDetailsDto?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadGroups()
    }

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Группы", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))

            if (userRole == "ADMIN") {
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { showAddDialog = true }) { Text("Добавить группу") }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (groups.isEmpty()) {
            Text("Список групп пуст")
        } else {
            LazyColumn {
                items(groups) { group ->
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                selectedGroup = group
                                if (userRole == "ADMIN" || userRole == "TEACHER") {
                                    showEditDialog = true
                                }
                            },
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(group.title, style = MaterialTheme.typography.bodyLarge)
                                Text("Учитель: ${group.teacher.name}", style = MaterialTheme.typography.bodySmall)
                                if (userRole == "STUDENT") {
                                    Text("Студентов: ${group.students.size}", style = MaterialTheme.typography.bodySmall)
                                }
                            }

                            if (userRole == "ADMIN") {
                                Column {
                                    Button(onClick = {
                                        selectedGroup = group
                                        showEditDialog = true
                                    }) { Text("Редактировать") }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Button(onClick = {
                                        selectedGroup = group
                                        showDeleteDialog = true
                                    }) { Text("Удалить") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showAddDialog) {
        GroupEditDialog(
            viewModel = viewModel,
            onDismiss = { showAddDialog = false },
            onSaveSuccess = {
                showAddDialog = false
                viewModel.loadGroups()
            },
            isNew = true
        )
    }

    if (showEditDialog && selectedGroup != null) {
        GroupEditDialog(
            viewModel = viewModel,
            group = selectedGroup,
            onDismiss = { showEditDialog = false },
            onSaveSuccess = {
                showEditDialog = false
                viewModel.loadGroups()
            },
            isNew = false
        )
    }

    if (showDeleteDialog && selectedGroup != null) {
        GroupDeleteDialog(
            viewModel = viewModel,
            group = selectedGroup!!,
            onDismiss = { showDeleteDialog = false },
            onDeleteSuccess = {
                showDeleteDialog = false
                viewModel.loadGroups()
            }
        )
    }
}
