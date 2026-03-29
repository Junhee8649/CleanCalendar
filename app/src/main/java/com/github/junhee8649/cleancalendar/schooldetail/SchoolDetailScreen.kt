package com.github.junhee8649.cleancalendar.schooldetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.junhee8649.cleancalendar.data.MaintenanceTask
import com.github.junhee8649.cleancalendar.data.School
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.YearMonth

private val MONTHS = listOf("1월","2월","3월","4월","5월","6월","7월","8월","9월","10월","11월","12월")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolDetailScreen(
    schoolId: String,
    onBack: () -> Unit,
    viewModel: SchoolDetailViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showEditDialog by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var taskToDelete by remember { mutableStateOf<MaintenanceTask?>(null) }

    LaunchedEffect(schoolId) { viewModel.loadData(schoolId) }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.userMessageShown()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        uiState.school?.name ?: "",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    if (uiState.school != null) {
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "수정",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when {
                uiState.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                uiState.school == null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text(
                        "학교 정보를 불러올 수 없습니다.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                else -> SchoolDetailContent(
                    school = uiState.school!!,
                    tasks = uiState.tasks,
                    onToggleTask = { viewModel.toggleTaskCompleted(it) },
                    onDeleteTask = { taskId -> taskToDelete = uiState.tasks.find { it.id == taskId } },
                    onAddTask = { showAddTaskDialog = true }
                )
            }
        }
    }

    if (showEditDialog && uiState.school != null) {
        EditSchoolDialog(
            school = uiState.school!!,
            onDismiss = { showEditDialog = false },
            onConfirm = { updated ->
                viewModel.updateSchool(updated)
                showEditDialog = false
            }
        )
    }

    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { year, month, description ->
                viewModel.addTask(schoolId, year, month, description)
                showAddTaskDialog = false
            }
        )
    }

    taskToDelete?.let { task ->
        DeleteTaskConfirmDialog(
            task = task,
            onConfirm = {
                viewModel.deleteTask(task.id)
                taskToDelete = null
            },
            onDismiss = { taskToDelete = null }
        )
    }
}

@Composable
private fun SchoolDetailContent(
    school: School,
    tasks: List<MaintenanceTask>,
    onToggleTask: (MaintenanceTask) -> Unit,
    onDeleteTask: (String) -> Unit,
    onAddTask: () -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            SchoolInfoCard(school)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 28.dp, end = 16.dp, top = 12.dp, bottom = 0.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "유지보수 일정",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (tasks.isNotEmpty()) {
                        Text(
                            "  ${tasks.size}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                IconButton(onClick = onAddTask) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "일정 추가",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        if (tasks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "등록된 일정이 없습니다.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            val grouped = tasks.sortedWith(compareBy({ it.year }, { it.month }))
                .groupBy { it.year }
            grouped.forEach { (year, yearTasks) ->
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 12.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column {
                            Text(
                                "${year}년",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                            )
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant,
                                thickness = 0.5.dp
                            )
                            yearTasks.forEach { task ->
                                TaskItem(task = task, onToggle = onToggleTask, onDelete = onDeleteTask)
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun SchoolInfoCard(school: School) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (school.address.isNotBlank()) InfoRow("주소", school.address)
            if (school.contactName.isNotBlank()) InfoRow("담당자", school.contactName)
            if (school.contactPhone.isNotBlank()) InfoRow("연락처", school.contactPhone)
            if (school.equipmentInfo.isNotBlank()) InfoRow("장비", school.equipmentInfo)
            if (school.memo.isNotBlank()) InfoRow("메모", school.memo)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row {
        Text(
            "$label  ",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun TaskItem(
    task: MaintenanceTask,
    onToggle: (MaintenanceTask) -> Unit,
    onDelete: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
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
                    .clickable { onToggle(task) },
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
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    "${task.month}월",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    task.taskDescription,
                    fontSize = 14.sp,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant
                            else MaterialTheme.colorScheme.onBackground
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
            IconButton(onClick = { onDelete(task.id) }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "삭제",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(start = 50.dp),
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 0.5.dp
        )
    }
}

@Composable
private fun DeleteTaskConfirmDialog(
    task: MaintenanceTask,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "작업 삭제",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                "${task.year}년 ${task.month}월 작업을 삭제하시겠습니까?\n이 작업은 되돌릴 수 없습니다.",
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("삭제", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditSchoolDialog(
    school: School,
    onDismiss: () -> Unit,
    onConfirm: (School) -> Unit
) {
    var name by remember { mutableStateOf(school.name) }
    var address by remember { mutableStateOf(school.address) }
    var contactName by remember { mutableStateOf(school.contactName) }
    var contactPhone by remember { mutableStateOf(school.contactPhone) }
    var equipmentInfo by remember { mutableStateOf(school.equipmentInfo) }
    var memo by remember { mutableStateOf(school.memo) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("학교 정보 수정") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("학교명 *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("주소") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = contactName,
                    onValueChange = { contactName = it },
                    label = { Text("담당자") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = contactPhone,
                    onValueChange = { contactPhone = it },
                    label = { Text("연락처") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = equipmentInfo,
                    onValueChange = { equipmentInfo = it },
                    label = { Text("장비 정보") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = memo,
                    onValueChange = { memo = it },
                    label = { Text("메모") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(school.copy(
                            name = name.trim(),
                            address = address.trim(),
                            contactName = contactName.trim(),
                            contactPhone = contactPhone.trim(),
                            equipmentInfo = equipmentInfo.trim(),
                            memo = memo.trim()
                        ))
                    }
                }
            ) { Text("저장") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (year: Int, month: Int, description: String) -> Unit
) {
    val currentYearMonth = remember { YearMonth.now() }
    var selectedYear by remember { mutableIntStateOf(currentYearMonth.year) }
    var selectedMonth by remember { mutableIntStateOf(currentYearMonth.monthValue) }
    var description by remember { mutableStateOf("") }
    var monthExpanded by remember { mutableStateOf(false) }

    val years = remember { (LocalDate.now().year - 1..LocalDate.now().year + 1).toList() }
    var yearExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("일정 추가") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = yearExpanded,
                        onExpandedChange = { yearExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = "${selectedYear}년",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("연도") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(yearExpanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = yearExpanded,
                            onDismissRequest = { yearExpanded = false }
                        ) {
                            years.forEach { year ->
                                DropdownMenuItem(
                                    text = { Text("${year}년") },
                                    onClick = { selectedYear = year; yearExpanded = false }
                                )
                            }
                        }
                    }
                    ExposedDropdownMenuBox(
                        expanded = monthExpanded,
                        onExpandedChange = { monthExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = MONTHS[selectedMonth - 1],
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("월") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(monthExpanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = monthExpanded,
                            onDismissRequest = { monthExpanded = false }
                        ) {
                            MONTHS.forEachIndexed { index, label ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = { selectedMonth = index + 1; monthExpanded = false }
                                )
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("작업 내용 *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (description.isNotBlank()) {
                        onConfirm(selectedYear, selectedMonth, description.trim())
                    }
                }
            ) { Text("추가") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        }
    )
}
