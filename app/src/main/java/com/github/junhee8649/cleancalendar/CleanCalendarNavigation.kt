package com.github.junhee8649.cleancalendar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.School
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController

object CleanCalendarDestinations {
    const val CALENDAR_ROUTE = "calendar"
    const val SCHOOLS_ROUTE = "schools"
    const val SCHOOL_DETAIL_ROUTE = "school_detail"
    const val HISTORY_ROUTE = "history"
    const val WORK_LOG_FORM_ROUTE = "work_log_form"
    const val IMAGE_VIEWER_ROUTE = "image_viewer"

    fun schoolDetailRoute(schoolId: String) = "$SCHOOL_DETAIL_ROUTE/$schoolId"
    fun imageViewerRoute(workLogId: String, startIndex: Int) =
        "$IMAGE_VIEWER_ROUTE/$workLogId/$startIndex"
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
    ),
    HISTORY(
        route = CleanCalendarDestinations.HISTORY_ROUTE,
        label = "히스토리",
        icon = Icons.Default.History
    )
}

class NavigationActions(private val navController: NavHostController) {
    fun navigateToSchoolDetail(schoolId: String) {
        navController.navigate(CleanCalendarDestinations.schoolDetailRoute(schoolId))
    }

    fun navigateToWorkLogForm(schoolId: String? = null) {
        val route = if (!schoolId.isNullOrEmpty())
            "${CleanCalendarDestinations.WORK_LOG_FORM_ROUTE}?schoolId=$schoolId"
        else
            CleanCalendarDestinations.WORK_LOG_FORM_ROUTE
        navController.navigate(route)
    }

    fun navigateToImageViewer(workLogId: String, startIndex: Int) {
        navController.navigate(CleanCalendarDestinations.imageViewerRoute(workLogId, startIndex))
    }

    fun navigateUp() {
        navController.navigateUp()
    }
}
