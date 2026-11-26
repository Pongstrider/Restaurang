package se.storgatan.schema

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import se.storgatan.schema.data.session.SessionState
import se.storgatan.schema.data.session.SessionStore
import se.storgatan.schema.domain.ShiftRepository
import se.storgatan.schema.ui.navigation.AvailableShiftsRoute
import se.storgatan.schema.ui.navigation.BottomTab
import se.storgatan.schema.ui.navigation.LoginRoute
import se.storgatan.schema.ui.navigation.MyShiftsRoute
import se.storgatan.schema.ui.navigation.NotificationsRoute
import se.storgatan.schema.ui.navigation.SchemaBottomBar
import se.storgatan.schema.ui.navigation.SchemaNavHost
import se.storgatan.schema.ui.theme.SchemaTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var sessionStore: SessionStore
    @Inject lateinit var shiftRepository: ShiftRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SchemaTheme {
                SchemaApp(sessionStore, shiftRepository)
            }
        }
    }
}

@Composable
private fun SchemaApp(session: SessionStore, shiftRepository: ShiftRepository) {
    val sessionState by session.state.collectAsState()
    val navController = rememberNavController()

    val isLoggedIn = sessionState is SessionState.LoggedIn
    val isLoading = sessionState is SessionState.Loading

    if (isLoading) return

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val currentTab = when {
        currentRoute?.contains("MyShiftsRoute") == true -> BottomTab.MY_SHIFTS
        currentRoute?.contains("AvailableShiftsRoute") == true -> BottomTab.AVAILABLE
        currentRoute?.contains("NotificationsRoute") == true -> BottomTab.NOTIFICATIONS
        else -> null
    }

    val showBottomBar = isLoggedIn && currentTab != null
    val unreadCount by shiftRepository.unreadCount.collectAsState()

    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn) {
            navController.navigate(LoginRoute) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                SchemaBottomBar(
                    currentTab = currentTab,
                    unreadCount = unreadCount,
                    onTabSelected = { tab ->
                        val route = when (tab) {
                            BottomTab.MY_SHIFTS -> MyShiftsRoute as Any
                            BottomTab.AVAILABLE -> AvailableShiftsRoute as Any
                            BottomTab.NOTIFICATIONS -> NotificationsRoute as Any
                        }
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        SchemaNavHost(
            navController = navController,
            startRoute = if (isLoggedIn) MyShiftsRoute else LoginRoute,
            onLogout = { },
            modifier = Modifier.padding(innerPadding),
        )
    }
}
