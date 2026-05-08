package com.undef.fintrackmobile

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.undef.fintrackmobile.ui.components.FintrackBottomBar
import com.undef.fintrackmobile.ui.navigation.FintrackDestination
import com.undef.fintrackmobile.ui.navigation.Routes
import com.undef.fintrackmobile.ui.screens.AdjustTicketScreen
import com.undef.fintrackmobile.ui.screens.ExploreScreen
import com.undef.fintrackmobile.ui.screens.HomeScreen
import com.undef.fintrackmobile.ui.screens.LoginScreen
import com.undef.fintrackmobile.ui.screens.NewPurchaseScreen
import com.undef.fintrackmobile.ui.screens.ProfileScreen
import com.undef.fintrackmobile.ui.screens.RecordsScreen
import com.undef.fintrackmobile.ui.screens.RegisterScreen
import com.undef.fintrackmobile.ui.screens.SplashScreen
import com.undef.fintrackmobile.ui.screens.WelcomeScreen
import com.undef.fintrackmobile.ui.theme.FintrackMobileTheme
import com.undef.fintrackmobile.ui.viewmodel.AppStateViewModel
import com.undef.fintrackmobile.ui.viewmodel.AuthViewModel
import com.undef.fintrackmobile.ui.viewmodel.ExploreViewModel
import com.undef.fintrackmobile.ui.viewmodel.FintrackViewModelFactory
import com.undef.fintrackmobile.ui.viewmodel.HomeViewModel
import com.undef.fintrackmobile.ui.viewmodel.ProfileViewModel
import com.undef.fintrackmobile.ui.viewmodel.PurchaseViewModel
import com.undef.fintrackmobile.ui.viewmodel.RecordsViewModel

@Composable
fun FintrackApp(container: AppContainer) {
    val viewModelFactory = remember(container) { FintrackViewModelFactory(container) }
    val appStateViewModel: AppStateViewModel = viewModel(factory = viewModelFactory)
    val preferences by appStateViewModel.preferences.collectAsStateWithLifecycle()

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = FintrackDestination.bottomItems.any { it.route == currentRoute }

    FintrackMobileTheme(dynamicColor = false) {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    FintrackBottomBar(navController, currentRoute)
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Routes.SPLASH,
                modifier = Modifier.padding(innerPadding),
            ) {
                composable(Routes.SPLASH) {
                    SplashScreen(
                        isLoggedIn = preferences.isLoggedIn,
                        onFinished = { destination ->
                            navController.navigate(destination) {
                                popUpTo(Routes.SPLASH) { inclusive = true }
                            }
                        }
                    )
                }
                composable(Routes.WELCOME) {
                    WelcomeScreen(
                        onLogin = { navController.navigate(Routes.LOGIN) },
                        onRegister = { navController.navigate(Routes.REGISTER) },
                    )
                }
                composable(Routes.LOGIN) {
                    val viewModel: AuthViewModel = viewModel(factory = viewModelFactory)
                    LoginScreen(
                        onLogin = { name, email ->
                            viewModel.login(name, email)
                            navController.navigate(FintrackDestination.Home.route) {
                                popUpTo(Routes.WELCOME) { inclusive = true }
                            }
                        },
                    ) { navController.navigate(Routes.REGISTER) }
                }
                composable(Routes.REGISTER) {
                    val viewModel: AuthViewModel = viewModel(factory = viewModelFactory)
                    RegisterScreen(
                        onRegister = { name, email, lastName, birthDate ->
                            viewModel.register(name, email, lastName, birthDate)
                            navController.navigate(FintrackDestination.Home.route) {
                                popUpTo(Routes.WELCOME) { inclusive = true }
                            }
                        },
                    ) { navController.navigate(Routes.LOGIN) }
                }
                composable(FintrackDestination.Home.route) {
                    val viewModel: HomeViewModel = viewModel(factory = viewModelFactory)
                    HomeScreen(
                        displayName = preferences.displayName,
                        currencyCode = preferences.currencyCode,
                        profileImageUri = preferences.profileImageUri,
                        viewModel = viewModel,
                        onProfileClick = { navController.navigate(FintrackDestination.Profile.route) },
                    )
                }
                composable(FintrackDestination.Explore.route) {
                    val viewModel: ExploreViewModel = viewModel(factory = viewModelFactory)
                    ExploreScreen(viewModel = viewModel)
                }
                composable(FintrackDestination.NewPurchase.route) {
                    val viewModel: PurchaseViewModel = viewModel(factory = viewModelFactory)
                    NewPurchaseScreen(
                        currencyCode = preferences.currencyCode,
                        viewModel = viewModel,
                        onAdjustTicket = { navController.navigate(Routes.ADJUST_TICKET) }
                    )
                }
                composable(Routes.ADJUST_TICKET) { entry ->
                    val parentEntry = remember(entry) {
                        navController.getBackStackEntry(FintrackDestination.NewPurchase.route)
                    }
                    val viewModel: PurchaseViewModel = viewModel(parentEntry, factory = viewModelFactory)
                    AdjustTicketScreen(
                        currencyCode = preferences.currencyCode,
                        viewModel = viewModel,
                        onDone = { navController.popBackStack() }
                    )
                }
                composable(FintrackDestination.Records.route) {
                    val viewModel: RecordsViewModel = viewModel(factory = viewModelFactory)
                    RecordsScreen(
                        currencyCode = preferences.currencyCode,
                        viewModel = viewModel,
                    )
                }
                composable(FintrackDestination.Profile.route) {
                    val viewModel: ProfileViewModel = viewModel(factory = viewModelFactory)
                    ProfileScreen(
                        viewModel = viewModel,
                        onOpenSettings = { navController.context.startActivity(SettingsActivity.intent(navController.context)) },
                        onLoggedOut = {
                            navController.navigate(Routes.WELCOME) {
                                popUpTo(navController.graph.id) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    }
}
