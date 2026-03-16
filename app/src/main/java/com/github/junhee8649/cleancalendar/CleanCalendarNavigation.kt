package com.github.junhee8649.cleancalendar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.School
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController

object CleanCalendarDestinations {
    const val CALENDAR_ROUTE = "calendar"
    const val SCHOOLS_ROUTE = "schools"
    const val SCHOOL_DETAIL_ROUTE = "school_detail"
    fun schoolDetailRoute(schoolId: String) = "$SCHOOL_DETAIL_ROUTE/$schoolId"
}

enum class BottomNavDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    CALENDAR(
        route = CleanCalendarDestinations.CALENDAR_ROUTE,
        label = "캘린더",
        icon = Icons.Default.CalendarMonth
    ),
    SCHOOLS(
        route = CleanCalendarDestinations.SCHOOLS_ROUTE,
        label = "학교 목록",
        icon = Icons.Default.School
    )
}

class NavigationActions(private val navController: NavHostController) {
    fun navigateToSchoolDetail(schoolId: String) {
        navController.navigate(CleanCalendarDestinations.schoolDetailRoute(schoolId))
    }

    fun navigateUp() {
        navController.navigateUp()
    }
}
