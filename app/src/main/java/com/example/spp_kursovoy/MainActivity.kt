package com.example.spp_kursovoy

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.spp_kursovoy.data.auth.AuthStorage
import com.example.spp_kursovoy.ui.groups.GroupsScreen
import com.example.spp_kursovoy.ui.login.LoginActivity
import com.example.spp_kursovoy.ui.profile.ProfileScreen
import com.example.spp_kursovoy.ui.schedule.ScheduleScreen
import com.example.spp_kursovoy.ui.theme.SPP_kursovoyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val authStorage = AuthStorage(this)
        val user = authStorage.getUser()

        setContent {
            SPP_kursovoyTheme {
                SPP_kursovoyApp(authStorage)
            }
        }
    }

    private fun logoutAndGoToLogin(authStorage: AuthStorage) {
        authStorage.clear()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}

@Composable
fun SPP_kursovoyApp(authStorage: AuthStorage) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.SCHEDULE) }
    val context = LocalContext.current

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach { destination ->
                item(
                    icon = {
                        Icon(
                            destination.icon,
                            contentDescription = destination.label
                        )
                    },
                    label = { Text(destination.label) },
                    selected = destination == currentDestination,
                    onClick = { currentDestination = destination }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            when (currentDestination) {
                AppDestinations.SCHEDULE -> ScheduleScreen(authStorage)
                AppDestinations.GROUPS -> GroupsScreen(authStorage)
                AppDestinations.PROFILE -> ProfileScreen(
                    authStorage = authStorage,
                    onLoggedOut = {
                        // Правильный способ получить Activity из Context
                        (context as? Activity)?.let { activity ->
                            authStorage.clear()
                            activity.startActivity(Intent(activity, LoginActivity::class.java))
                            activity.finish()
                        }
                    }
                )
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    SCHEDULE("Schedule", Icons.Default.DateRange),
    GROUPS("Groups", Icons.Default.Menu),
    PROFILE("Profile", Icons.Default.AccountBox),
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SPP_kursovoyTheme {
        Greeting("Android")
    }
}
