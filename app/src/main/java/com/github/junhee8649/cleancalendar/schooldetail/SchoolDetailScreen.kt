package com.github.junhee8649.cleancalendar.schooldetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

    LaunchedEffect(schoolId) { viewModel.loadData(schoolId) }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.userMessageShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.school?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    if (uiState.school != null) {
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "수정")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (uiState.school != null) {
                FloatingActionButton(onClick = { showAddTaskDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "일정 추가")
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when {
                uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                uiState.school == null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text("학교 정보를 불러올 수 없습니다.")
                }
                else -> SchoolDetailContent(
                    school = uiState.school!!,
                    tasks = uiState.tasks,
                    onToggleTask = { viewModel.toggleTaskCompleted(it) },
                    onDeleteTask = { viewModel.deleteTask(it) }
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
}

@Composable
private fun SchoolDetailContent(
    school: School,
    tasks: List<MaintenanceTask>,
    onToggleTask: (MaintenanceTask) -> Unit,
    onDeleteTask: (String) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            SchoolInfoCard(school)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text(
                "유지보수 일정",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }
        if (tasks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("등록된 일정이 없습니다.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            val grouped = tasks.sortedWith(compareBy({ it.year }, { it.month }))
                .groupBy { it.year }
            grouped.forEach { (year, yearTasks) ->
                item {
                    Text(
                        "${year}년",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
                items(yearTasks, key = { it.id }) { task ->
                    TaskItem(task = task, onToggle = onToggleTask, onDelete = onDeleteTask)
                }
            }
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun SchoolInfoCard(school: School) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
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
            "$label: ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun TaskItem(
    task: MaintenanceTask,
    onToggle: (MaintenanceTask) -> Unit,
    onDelete: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle(task) }
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${task.month}월",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(task.taskDescription, style = MaterialTheme.typography.bodyMedium)
                if (task.completedDate != null) {
                    Text(
                        "완료: ${task.completedDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
    }
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
