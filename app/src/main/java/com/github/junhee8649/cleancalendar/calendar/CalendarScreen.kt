package com.github.junhee8649.cleancalendar.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.junhee8649.cleancalendar.data.MaintenanceTask
import org.koin.androidx.compose.koinViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

private val DAY_LABELS = listOf("일", "월", "화", "수", "목", "금", "토")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onNavigateToSchoolDetail: (String) -> Unit,
    viewModel: CalendarViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.userMessageShown()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("캘린더") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            MonthHeader(
                yearMonth = uiState.currentYearMonth,
                onPrevious = { viewModel.previousMonth() },
                onNext = { viewModel.nextMonth() }
            )
            MonthCalendarGrid(
                yearMonth = uiState.currentYearMonth,
                selectedDate = uiState.selectedDate,
                completedDates = uiState.completedDates,
                hasTaskDates = buildHasTaskDates(uiState.tasksInMonth, uiState.currentYearMonth),
                onDateClick = { viewModel.selectDate(it) }
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            if (uiState.isLoading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                SelectedMonthTaskList(
                    selectedDate = uiState.selectedDate,
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
    if (tasks.isEmpty()) return emptySet()
    // 태스크가 있는 달의 1일을 대표 날짜로 사용 (month/year 기반 데이터)
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
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "이전 달")
        }
        Text(
            "${yearMonth.year}년 ${yearMonth.monthValue}월",
            style = MaterialTheme.typography.titleMedium
        )
        IconButton(onClick = onNext) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "다음 달")
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
    // 일요일=0 기준 시작 오프셋
    val startOffset = firstDay.dayOfWeek.value % 7
    val daysInMonth = yearMonth.lengthOfMonth()
    val today = LocalDate.now()

    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
        // 요일 헤더
        Row(modifier = Modifier.fillMaxWidth()) {
            DAY_LABELS.forEach { label ->
                Text(
                    label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = when (label) {
                        "일" -> MaterialTheme.colorScheme.error
                        "토" -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))

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
                        val isSelected = date == selectedDate
                        val isToday = date == today
                        val isCompleted = completedDates.contains(date)
                        val hasTask = hasTaskDates.contains(date)

                        DayCell(
                            day = dayCounter,
                            isSelected = isSelected,
                            isToday = isToday,
                            isCompleted = isCompleted,
                            hasTask = hasTask,
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
    val bgColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primaryContainer
        else -> Color.Transparent
    }
    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isSunday -> MaterialTheme.colorScheme.error
        isSaturday -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
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
                style = MaterialTheme.typography.bodySmall,
                color = textColor
            )
            if (hasTask || isCompleted) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(
                            if (isCompleted) MaterialTheme.colorScheme.tertiary
                            else MaterialTheme.colorScheme.secondary
                        )
                )
            }
        }
    }
}

@Composable
private fun SelectedMonthTaskList(
    selectedDate: LocalDate,
    tasks: List<MaintenanceTask>,
    schoolNames: Map<String, String>,
    onToggleTask: (MaintenanceTask) -> Unit,
    onSchoolClick: (String) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            "${selectedDate.year}년 ${selectedDate.monthValue}월 일정",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("이 달 일정이 없습니다.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn {
                items(tasks, key = { it.id }) { task ->
                    CalendarTaskItem(
                        task = task,
                        schoolName = schoolNames[task.schoolId] ?: task.schoolId,
                        onToggle = { onToggleTask(task) },
                        onSchoolClick = { onSchoolClick(task.schoolId) }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun CalendarTaskItem(
    task: MaintenanceTask,
    schoolName: String,
    onToggle: () -> Unit,
    onSchoolClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = task.isCompleted, onCheckedChange = { onToggle() })
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSchoolClick() }
            ) {
                Text(
                    schoolName,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    task.taskDescription,
                    style = MaterialTheme.typography.bodySmall,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface
                )
                if (task.completedDate != null) {
                    Text(
                        "완료: ${task.completedDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}
