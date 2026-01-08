package com.example.spp_kursovoy.ui.profile

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.spp_kursovoy.data.auth.AuthStorage

@Composable
fun ProfileScreen(authStorage: AuthStorage, onLoggedOut: () -> Unit) {

    val viewModel = remember { ProfileViewModel(authStorage) }

    val profile by viewModel.profile.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    var logoutInProgress by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Text(
            text = "Профиль",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            loading -> {
                CircularProgressIndicator()
            }

            error != null -> {
                Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error
                )
            }

            profile != null -> {
                ProfileField(label = "Имя", value = profile!!.name)
                ProfileField(label = "Email", value = profile!!.email)
                ProfileField(label = "Роль", value = profile!!.role)

                Spacer(modifier = Modifier.height(24.dp))

                // Кнопка Log out
                Button(
                    onClick = {
                        logoutInProgress = true
                        viewModel.logout {
                            logoutInProgress = false
                            onLoggedOut()
                        }
                    },
                    enabled = !logoutInProgress,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (logoutInProgress) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Log out", color = Color.White)
                }
            }

            else -> {
                Text("Данные профиля недоступны")
            }
        }
    }
}

@Composable
private fun ProfileField(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}
