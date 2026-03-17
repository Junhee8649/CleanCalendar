package com.github.junhee8649.cleancalendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.github.junhee8649.cleancalendar.ui.theme.CleanCalendarTheme

private val TdsBlue = Color(0xFF0064FF)
private val TdsTextSecondary = Color(0xFF8B95A1)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        setContent {
            CleanCalendarTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                val bottomNavRoutes = BottomNavDestination.entries.map { it.route }.toSet()
                val showBottomBar = currentDestination?.route in bottomNavRoutes

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            val navBarColor = if (isSystemInDarkTheme()) Color.Black else Color.White
                            Column {
                                Surface(
                                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                                    color = navBarColor,
                                    shadowElevation = 8.dp,
                                ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    BottomNavDestination.entries.forEach { destination ->
                                        val selected = currentDestination?.hierarchy?.any {
                                            it.route == destination.route
                                        } == true
                                        val itemColor = if (selected) TdsBlue else TdsTextSecondary
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable {
                                                    navController.navigate(destination.route) {
                                                        popUpTo(navController.graph.findStartDestination().id) {
                                                            saveState = true
                                                        }
                                                        launchSingleTop = true
                                                        restoreState = true
                                                    }
                                                }
                                                .padding(top = 4.dp, bottom = 0.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy((-2).dp)
                                        ) {
                                            Icon(
                                                imageVector = destination.icon,
                                                contentDescription = destination.label,
                                                tint = itemColor
                                            )
                                            Text(
                                                text = destination.label,
                                                color = itemColor,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                                } // Surface
                                Spacer(
                                    Modifier
                                        .windowInsetsBottomHeight(WindowInsets.navigationBars)
                                        .fillMaxWidth()
                                        .background(navBarColor)
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    CleanCalendarNavGraph(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
