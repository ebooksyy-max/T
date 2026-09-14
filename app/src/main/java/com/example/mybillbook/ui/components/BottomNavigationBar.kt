package com.example.mybillbook.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import com.example.mybillbook.ui.navigation.Routes
import com.example.mybillbook.ui.theme.BillBookColors
import com.example.mybillbook.ui.theme.PrimaryBlue

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem(Routes.HOME, "Home", Icons.Default.Dashboard)
    object Bills : BottomNavItem(Routes.BILLS, "Bills", Icons.AutoMirrored.Filled.ListAlt)
    object Khata : BottomNavItem(Routes.KHATA, "Khata", Icons.Default.AccountBalanceWallet)
    object Inventory : BottomNavItem(Routes.INVENTORY, "Inventory", Icons.Default.Inventory2)
    object Reports : BottomNavItem(Routes.REPORTS, "Reports", Icons.Default.BarChart)
}

@Composable
fun AppBottomNavigationBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Bills,
        BottomNavItem.Khata,
        BottomNavItem.Inventory,
        BottomNavItem.Reports
    )

    NavigationBar(
        modifier = modifier.testTag("app_bottom_navigation_bar"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = NavigationBarDefaults.Elevation
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                modifier = Modifier.testTag("bottom_nav_${item.title.lowercase()}"),
                selected = isSelected,
                onClick = {
                    if (currentRoute != item.route) {
                        onNavigate(item.route)
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BillBookColors.Primary,
                    selectedTextColor = BillBookColors.Primary,
                    indicatorColor = BillBookColors.PrimaryLight,
                    unselectedIconColor = BillBookColors.TextSecondary,
                    unselectedTextColor = BillBookColors.TextSecondary
                )
            )
        }
    }
}
