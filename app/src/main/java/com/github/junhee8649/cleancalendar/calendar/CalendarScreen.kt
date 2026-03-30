package com.github.junhee8649.cleancalendar.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.junhee8649.cleancalendar.data.MaintenanceTask
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.YearMonth

private val TdsBlue = Color(0xFF0064FF)
private val TdsRed = Color(0xFFFF3B30)
private val TdsSaturday = Color(0xFF3182F6)

private val DAY_LABELS = listOf("일", "월", "화", "수", "목", "금", "토")

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    val tasksByDate = remember(uiState.tasksInMonth) {
        uiState.tasksInMonth
            .filter { it.scheduledDate != null }
            .groupBy { it.scheduledDate!! }
    }
    val selectedDate = uiState.selectedDate
    val tasksForSelectedDate = selectedDate?.let { tasksByDate[it] } ?: emptyList()

    // 미배정 task 가나다순 정렬
    val unscheduledTasks = remember(uiState.tasksInMonth, uiState.schoolNames) {
        uiState.tasksInMonth
            .filter { it.scheduledDate == null }
            .sortedBy { uiState.schoolNames[it.schoolId] ?: "" }
    }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.userMessageShown()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
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
                    selectedDate = selectedDate,
                    tasksByDate = tasksByDate,
                    schoolNames = uiState.schoolNames,
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
                    CircularProgressIndicator(color = TdsBlue)
                }
            } else if (selectedDate == null) {
                AllTasksSection(
                    tasksByDate = tasksByDate,
                    schoolNames = uiState.schoolNames
                )
            } else {
                DateTaskSection(
                    selectedDate = selectedDate,
                    tasks = tasksForSelectedDate,
                    schoolNames = uiState.schoolNames,
                    onAddClick = { viewModel.showTaskPicker() },
                    onRemoveTask = { viewModel.removeTaskFromDate(it) }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (uiState.showTaskPicker) {
        TaskPickerBottomSheet(
            tasks = unscheduledTasks,
            schoolNames = uiState.schoolNames,
            onDismiss = { viewModel.hideTaskPicker() },
            onTaskSelect = { taskId ->
                viewModel.addTaskForDate(taskId)
                viewModel.hideTaskPicker()
            }
        )
    }
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
    selectedDate: LocalDate?,
    tasksByDate: Map<LocalDate, List<MaintenanceTask>>,
    schoolNames: Map<String, String>,
    onDateClick: (LocalDate) -> Unit
) {
    val firstDay = yearMonth.atDay(1)
    val startOffset = firstDay.dayOfWeek.value % 7
    val daysInMonth = yearMonth.lengthOfMonth()
    val today = LocalDate.now()

    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            DAY_LABELS.forEach { label ->
                Text(
                    label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = when (label) {
                        "일" -> TdsRed
                        "토" -> TdsSaturday
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
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
                        Spacer(
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 64.dp)
                        )
                    } else {
                        val date = yearMonth.atDay(dayCounter)
                        val tasksOnDate = tasksByDate[date] ?: emptyList()
                        DayCell(
                            day = dayCounter,
                            isSelected = selectedDate != null && date == selectedDate,
                            isToday = date == today,
                            isSunday = col == 0,
                            isSaturday = col == 6,
                            tasksOnDate = tasksOnDate,
                            schoolNames = schoolNames,
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
    isSunday: Boolean,
    isSaturday: Boolean,
    tasksOnDate: List<MaintenanceTask>,
    schoolNames: Map<String, String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = when {
        isSelected -> TdsBlue
        isToday -> TdsBlue.copy(alpha = 0.10f)
        else -> Color.Transparent
    }
    val dateTextColor = when {
        isSelected -> Color.White
        isSunday -> TdsRed
        isSaturday -> TdsSaturday
        else -> MaterialTheme.colorScheme.onBackground
    }

    Column(
        modifier = modifier
            .defaultMinSize(minHeight = 64.dp)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 2.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            day.toString(),
            fontSize = 13.sp,
            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
            color = dateTextColor
        )
        if (tasksOnDate.isNotEmpty()) {
            Spacer(modifier = Modifier.height(2.dp))
            val maxVisible = 2
            val visibleTasks = tasksOnDate.take(maxVisible)
            val overflow = tasksOnDate.size - maxVisible

            visibleTasks.forEach { task ->
                val abbr = (schoolNames[task.schoolId] ?: "").take(3)
                Text(
                    abbr,
                    fontSize = 8.sp,
                    color = if (isSelected) Color.White.copy(alpha = 0.9f) else TdsBlue,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (overflow > 0) {
                Text(
                    "+$overflow",
                    fontSize = 8.sp,
                    color = if (isSelected) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun AllTasksSection(
    tasksByDate: Map<LocalDate, List<MaintenanceTask>>,
    schoolNames: Map<String, String>
) {
    val sortedDates = remember(tasksByDate) { tasksByDate.keys.sorted() }

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
                    "이번 달 전체 일정",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (sortedDates.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "${tasksByDate.values.sumOf { it.size }}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TdsBlue
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

            if (sortedDates.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "이번 달 배정된 일정이 없습니다.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                sortedDates.forEach { date ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            "${date.monthValue}월 ${date.dayOfMonth}일",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TdsBlue
                        )
                    }
                    tasksByDate[date]?.forEach { task ->
                        DateTaskItem(
                            schoolName = schoolNames[task.schoolId] ?: task.schoolId,
                            taskDescription = task.taskDescription,
                            onRemove = {},
                            showRemove = false
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant,
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DateTaskSection(
    selectedDate: LocalDate,
    tasks: List<MaintenanceTask>,
    schoolNames: Map<String, String>,
    onAddClick: () -> Unit,
    onRemoveTask: (String) -> Unit
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${selectedDate.monthValue}월 ${selectedDate.dayOfMonth}일",
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
                            color = TdsBlue
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(TdsBlue)
                        .clickable { onAddClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "일정 추가",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "이 날 배정된 학교가 없습니다.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                tasks.forEach { task ->
                    DateTaskItem(
                        schoolName = schoolNames[task.schoolId] ?: task.schoolId,
                        taskDescription = task.taskDescription,
                        onRemove = { onRemoveTask(task.id) }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                        thickness = 0.5.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun DateTaskItem(
    schoolName: String,
    taskDescription: String,
    onRemove: () -> Unit,
    showRemove: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                schoolName,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (taskDescription.isNotBlank()) {
                Text(
                    taskDescription,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (showRemove) {
            Text(
                "되돌리기",
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onRemove() }
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskPickerBottomSheet(
    tasks: List<MaintenanceTask>,
    schoolNames: Map<String, String>,
    onDismiss: () -> Unit,
    onTaskSelect: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                "일정 선택",
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "배정 가능한 일정이 없습니다.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn {
                    items(tasks, key = { it.id }) { task ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onTaskSelect(task.id) }
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    schoolNames[task.schoolId] ?: task.schoolId,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                if (task.taskDescription.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        task.taskDescription,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            color = MaterialTheme.colorScheme.outlineVariant,
                            thickness = 0.5.dp
                        )
                    }
                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }
            }
        }
    }
}
