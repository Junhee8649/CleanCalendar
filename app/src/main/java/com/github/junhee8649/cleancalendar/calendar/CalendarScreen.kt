package com.github.junhee8649.cleancalendar.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.junhee8649.cleancalendar.data.MaintenanceTask
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.YearMonth

private val DAY_LABELS = listOf("일", "월", "화", "수", "목", "금", "토")

@Composable
fun CalendarScreen(
    onNavigateToSchoolDetail: (String) -> Unit,
    viewModel: CalendarViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.userMessageShown()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(innerPadding)
        ) {
            MonthHeader(
                yearMonth = uiState.currentYearMonth,
                onPrevious = { viewModel.previousMonth() },
                onNext = { viewModel.nextMonth() }
            )
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                MonthCalendarGrid(
                    yearMonth = uiState.currentYearMonth,
                    selectedDate = uiState.selectedDate,
                    completedDates = uiState.completedDates,
                    hasTaskDates = buildHasTaskDates(uiState.tasksInMonth, uiState.currentYearMonth),
                    onDateClick = { viewModel.selectDate(it) }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                TaskSection(
                    yearMonth = uiState.currentYearMonth,
                    tasks = uiState.tasksInMonth,
                    schoolNames = uiState.schoolNames,
                    onToggleTask = { viewModel.toggleTaskCompleted(it) },
                    onSchoolClick = onNavigateToSchoolDetail
                )
            }
        }
    }
}

private fun buildHasTaskDates(tasks: List<MaintenanceTask>, yearMonth: YearMonth): Set<LocalDate> {
    return if (tasks.isNotEmpty()) setOf(yearMonth.atDay(1)) else emptySet()
}

@Composable
private fun MonthHeader(
    yearMonth: YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrevious) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "이전 달",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "${yearMonth.year}년",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "${yearMonth.monthValue}월",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        IconButton(onClick = onNext) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "다음 달",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun MonthCalendarGrid(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    completedDates: Set<LocalDate>,
    hasTaskDates: Set<LocalDate>,
    onDateClick: (LocalDate) -> Unit
) {
    val firstDay = yearMonth.atDay(1)
    val startOffset = firstDay.dayOfWeek.value % 7
    val daysInMonth = yearMonth.lengthOfMonth()
    val today = LocalDate.now()

    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            DAY_LABELS.forEach { label ->
                Text(
                    label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = when (label) {
                        "일" -> MaterialTheme.colorScheme.error
                        "토" -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        val totalCells = startOffset + daysInMonth
        val rows = (totalCells + 6) / 7
        var dayCounter = 1

        repeat(rows) { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { col ->
                    val cellIndex = row * 7 + col
                    if (cellIndex < startOffset || dayCounter > daysInMonth) {
                        Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val date = yearMonth.atDay(dayCounter)
                        DayCell(
                            day = dayCounter,
                            isSelected = date == selectedDate,
                            isToday = date == today,
                            isCompleted = completedDates.contains(date),
                            hasTask = hasTaskDates.contains(date),
                            isSunday = col == 0,
                            isSaturday = col == 6,
                            onClick = { onDateClick(date) },
                            modifier = Modifier.weight(1f)
                        )
                        dayCounter++
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    isSelected: Boolean,
    isToday: Boolean,
    isCompleted: Boolean,
    hasTask: Boolean,
    isSunday: Boolean,
    isSaturday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val bgColor = when {
        isSelected -> primary
        isToday -> primary.copy(alpha = 0.12f)
        else -> Color.Transparent
    }
    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isSunday -> MaterialTheme.colorScheme.error
        isSaturday -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.onBackground
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(bgColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                day.toString(),
                fontSize = 14.sp,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                color = textColor
            )
            if (hasTask || isCompleted) {
                Spacer(modifier = Modifier.height(1.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(
                            if (isCompleted) MaterialTheme.colorScheme.tertiary
                            else MaterialTheme.colorScheme.primary
                        )
                )
            }
        }
    }
}

@Composable
private fun TaskSection(
    yearMonth: YearMonth,
    tasks: List<MaintenanceTask>,
    schoolNames: Map<String, String>,
    onToggleTask: (MaintenanceTask) -> Unit,
    onSchoolClick: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${yearMonth.monthValue}월 일정",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (tasks.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "${tasks.size}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "이 달 일정이 없습니다.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                tasks.forEach { task ->
                    TaskItem(
                        task = task,
                        schoolName = schoolNames[task.schoolId] ?: task.schoolId,
                        onToggle = { onToggleTask(task) },
                        onSchoolClick = { onSchoolClick(task.schoolId) }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 56.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                        thickness = 0.5.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskItem(
    task: MaintenanceTask,
    schoolName: String,
    onToggle: () -> Unit,
    onSchoolClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSchoolClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(
                    if (task.isCompleted) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outlineVariant
                )
                .clickable { onToggle() },
            contentAlignment = Alignment.Center
        ) {
            if (task.isCompleted) {
                Text(
                    "✓",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                schoolName,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onBackground,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
            )
            Text(
                task.taskDescription,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
            )
            if (task.completedDate != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "완료 ${task.completedDate}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}
