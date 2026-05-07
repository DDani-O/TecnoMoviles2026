package com.fintrack.mobile.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.fintrack.mobile.ui.navigation.FintrackDestination
import com.fintrack.mobile.ui.theme.FintrackTheme

@Composable
fun FintrackBottomBar(
    navController: NavHostController,
    currentRoute: String?,
) {
    val colors = FintrackTheme.colors

    NavigationBar(containerColor = colors.navBarContainer) {
        FintrackDestination.bottomItems.forEach { destination ->
            val selected = currentRoute == destination.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = stringResource(destination.labelRes)
                    )
                },
                label = {
                    Text(text = stringResource(destination.labelRes))
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colors.navBarSelected,
                    selectedTextColor = colors.navBarSelected,
                    indicatorColor = colors.navBarIndicator,
                    unselectedIconColor = colors.navBarUnselected,
                    unselectedTextColor = colors.navBarUnselected
                )
            )
        }
    }
}
