package com.github.junhee8649.cleancalendar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.github.junhee8649.cleancalendar.calendar.CalendarScreen
import com.github.junhee8649.cleancalendar.history.HistoryScreen
import com.github.junhee8649.cleancalendar.history.ImageViewerScreen
import com.github.junhee8649.cleancalendar.history.WorkLogFormScreen
import com.github.junhee8649.cleancalendar.schooldetail.SchoolDetailScreen
import com.github.junhee8649.cleancalendar.schools.SchoolsScreen

@Composable
fun CleanCalendarNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val navActions = remember(navController) { NavigationActions(navController) }

    NavHost(
        navController = navController,
        startDestination = CleanCalendarDestinations.CALENDAR_ROUTE,
        modifier = modifier
    ) {
        composable(CleanCalendarDestinations.CALENDAR_ROUTE) {
            CalendarScreen()
        }

        composable(CleanCalendarDestinations.SCHOOLS_ROUTE) {
            SchoolsScreen(
                onSchoolClick = { schoolId ->
                    navActions.navigateToSchoolDetail(schoolId)
                }
            )
        }

        composable(
            route = "${CleanCalendarDestinations.SCHOOL_DETAIL_ROUTE}/{schoolId}",
            arguments = listOf(navArgument("schoolId") { type = NavType.StringType })
        ) { backStackEntry ->
            val schoolId = backStackEntry.arguments?.getString("schoolId") ?: return@composable
            SchoolDetailScreen(
                schoolId = schoolId,
                onBack = { navActions.navigateUp() }
            )
        }

        composable(CleanCalendarDestinations.HISTORY_ROUTE) {
            HistoryScreen(
                onAddClick = { schoolId -> navActions.navigateToWorkLogForm(schoolId) },
                onImageClick = { workLogId, imageIndex ->
                    navActions.navigateToImageViewer(workLogId, imageIndex)
                }
            )
        }

        composable(
            route = "${CleanCalendarDestinations.WORK_LOG_FORM_ROUTE}?schoolId={schoolId}",
            arguments = listOf(navArgument("schoolId") {
                type = NavType.StringType
                defaultValue = ""
            })
        ) { backStackEntry ->
            val schoolId = backStackEntry.arguments?.getString("schoolId")?.takeIf { it.isNotEmpty() }
            WorkLogFormScreen(preSelectedSchoolId = schoolId, onBack = { navActions.navigateUp() })
        }

        composable(
            route = "${CleanCalendarDestinations.IMAGE_VIEWER_ROUTE}/{workLogId}/{startIndex}",
            arguments = listOf(
                navArgument("workLogId") { type = NavType.StringType },
                navArgument("startIndex") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val workLogId = backStackEntry.arguments?.getString("workLogId") ?: return@composable
            val startIndex = backStackEntry.arguments?.getInt("startIndex") ?: 0
            ImageViewerScreen(
                workLogId = workLogId,
                startIndex = startIndex,
                onBack = { navActions.navigateUp() }
            )
        }
    }
}
