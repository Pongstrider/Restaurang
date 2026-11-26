package se.storgatan.schema.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import se.storgatan.schema.ui.screens.available.AvailableShiftsScreen
import se.storgatan.schema.ui.screens.confirmation.ConfirmationScreen
import se.storgatan.schema.ui.screens.detail.ShiftDetailScreen
import se.storgatan.schema.ui.screens.login.LoginScreen
import se.storgatan.schema.ui.screens.myshifts.MyShiftsScreen
import se.storgatan.schema.ui.screens.notifications.NotificationsScreen
import se.storgatan.schema.ui.screens.release.ReleaseShiftScreen

@Composable
fun SchemaNavHost(
    navController: NavHostController,
    startRoute: Any,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = startRoute,
        modifier = modifier,
    ) {
        composable<LoginRoute> {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(MyShiftsRoute) {
                        popUpTo(LoginRoute) { inclusive = true }
                    }
                },
            )
        }

        composable<MyShiftsRoute> {
            MyShiftsScreen(
                onShiftClick = { shiftId -> navController.navigate(ShiftDetailRoute(shiftId)) },
            )
        }

        composable<ShiftDetailRoute> {
            ShiftDetailScreen(
                onBack = { navController.popBackStack() },
                onRelease = { shiftId -> navController.navigate(ReleaseShiftRoute(shiftId)) },
                onUnreleased = { navController.popBackStack() },
            )
        }

        composable<ReleaseShiftRoute> {
            ReleaseShiftScreen(
                onBack = { navController.popBackStack() },
                onReleased = { navController.popBackStack(MyShiftsRoute, inclusive = false) },
            )
        }

        composable<AvailableShiftsRoute> {
            AvailableShiftsScreen(
                onShiftClaimed = { shiftId -> navController.navigate(ConfirmationRoute(shiftId)) },
            )
        }

        composable<ConfirmationRoute> {
            ConfirmationScreen(
                onDone = { navController.popBackStack(AvailableShiftsRoute, inclusive = false) },
            )
        }

        composable<NotificationsRoute> {
            NotificationsScreen()
        }
    }
}
