package com.github.junhee8649.cleancalendar.history

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.github.junhee8649.cleancalendar.data.School
import com.github.junhee8649.cleancalendar.data.WorkLog
import org.koin.androidx.compose.koinViewModel

@Composable
fun HistoryScreen(
    onAddClick: (schoolId: String?) -> Unit,
    onImageClick: (workLogId: String, imageIndex: Int) -> Unit,
    viewModel: HistoryViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var workLogToDelete by remember { mutableStateOf<WorkLog?>(null) }

    val filteredSchools = remember(uiState.schools, uiState.searchQuery) {
        if (uiState.searchQuery.isBlank()) uiState.schools
        else uiState.schools.filter { it.name.contains(uiState.searchQuery, ignoreCase = true) }
    }
    val workLogCountBySchool = remember(uiState.workLogs) {
        uiState.workLogs.groupingBy { it.schoolId }.eachCount()
    }
    val selectedSchoolWorkLogs = remember(uiState.workLogs, uiState.selectedSchoolId) {
        uiState.workLogs.filter { it.schoolId == uiState.selectedSchoolId }
    }
    val selectedSchool = remember(uiState.schools, uiState.selectedSchoolId) {
        uiState.schools.find { it.id == uiState.selectedSchoolId }
    }

    BackHandler(enabled = uiState.selectedSchoolId != null) {
        viewModel.clearSchoolSelection()
    }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.userMessageShown()
        }
    }

    LifecycleResumeEffect(Unit) {
        viewModel.loadWorkLogs()
        onPauseOrDispose { }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (uiState.selectedSchoolId == null) {
            SchoolListView(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                schools = filteredSchools,
                workLogCountBySchool = workLogCountBySchool,
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = viewModel::onSearchQueryChange,
                onSchoolClick = viewModel::selectSchool,
                onAddClick = { onAddClick(null) }
            )
        } else {
            WorkLogListView(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                schoolName = selectedSchool?.name ?: "",
                workLogs = selectedSchoolWorkLogs,
                onBack = viewModel::clearSchoolSelection,
                onAddClick = { onAddClick(uiState.selectedSchoolId) },
                onImageClick = onImageClick,
                onDeleteClick = { workLogToDelete = it }
            )
        }
    }

    workLogToDelete?.let { workLog ->
        DeleteWorkLogConfirmDialog(
            workLog = workLog,
            onConfirm = {
                viewModel.deleteWorkLog(workLog.id)
                workLogToDelete = null
            },
            onDismiss = { workLogToDelete = null }
        )
    }
}

@Composable
private fun SchoolListView(
    modifier: Modifier = Modifier,
    schools: List<School>,
    workLogCountBySchool: Map<String, Int>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSchoolClick: (String) -> Unit,
    onAddClick: () -> Unit
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 28.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "히스토리",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            IconButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "작업 일지 추가", tint = MaterialTheme.colorScheme.primary)
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            placeholder = { Text("학교 검색", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "검색 초기화", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        if (schools.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    if (searchQuery.isBlank()) "등록된 학교가 없습니다." else "검색 결과가 없습니다.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp
            ) {
                LazyColumn {
                    itemsIndexed(schools) { index, school ->
                        SchoolHistoryItem(
                            school = school,
                            count = workLogCountBySchool[school.id] ?: 0,
                            onClick = { onSchoolClick(school.id) }
                        )
                        if (index < schools.lastIndex) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant,
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SchoolHistoryItem(
    school: School,
    count: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = school.name,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "${count}건",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun WorkLogListView(
    modifier: Modifier = Modifier,
    schoolName: String,
    workLogs: List<WorkLog>,
    onBack: () -> Unit,
    onAddClick: () -> Unit,
    onImageClick: (workLogId: String, imageIndex: Int) -> Unit,
    onDeleteClick: (WorkLog) -> Unit
) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp)
                .height(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = schoolName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 56.dp)
            )
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "뒤로",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            IconButton(
                onClick = onAddClick,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(Icons.Default.Add, contentDescription = "작업 일지 추가", tint = MaterialTheme.colorScheme.primary)
            }
        }

        if (workLogs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "작업 일지가 없습니다.\n+ 버튼으로 첫 일지를 작성해보세요.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(workLogs, key = { it.id }) { workLog ->
                    WorkLogCard(
                        workLog = workLog,
                        onImageClick = { index -> onImageClick(workLog.id, index) },
                        onDeleteClick = { onDeleteClick(workLog) }
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkLogCard(
    workLog: WorkLog,
    onImageClick: (Int) -> Unit,
    onDeleteClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var cutIndex by remember { mutableStateOf(-1) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = workLog.date.toString(),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "삭제",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            if (workLog.taskCategories.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.padding(end = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    workLog.taskCategories.forEach { category ->
                        FilterChip(
                            selected = true,
                            onClick = {},
                            label = { Text(category, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            if (workLog.issuesText.isNotBlank()) {
                val primaryColor = MaterialTheme.colorScheme.primary
                val onSurfaceColor = MaterialTheme.colorScheme.onSurface
                val displayText = remember(cutIndex, expanded, workLog.issuesText) {
                    if (expanded || cutIndex < 0) {
                        buildAnnotatedString { append(workLog.issuesText) }
                    } else {
                        buildAnnotatedString {
                            val truncateAt = maxOf(0, cutIndex - 6)
                            append(workLog.issuesText.take(truncateAt))
                            append("...")
                            withLink(
                                LinkAnnotation.Clickable(
                                    tag = "EXPAND",
                                    styles = TextLinkStyles(
                                        style = SpanStyle(
                                            color = primaryColor,
                                            fontWeight = FontWeight.Medium
                                        )
                                    ),
                                    linkInteractionListener = { expanded = true }
                                )
                            ) {
                                append("더보기")
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = displayText,
                    fontSize = 14.sp,
                    color = onSurfaceColor,
                    maxLines = if (expanded) Int.MAX_VALUE else 3,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier.padding(end = 12.dp),
                    onTextLayout = { result ->
                        if (!expanded && cutIndex < 0 && result.hasVisualOverflow) {
                            cutIndex = result.getLineEnd(2, visibleEnd = true)
                        }
                    }
                )
                if (expanded) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "접기",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable {
                            expanded = false
                            cutIndex = -1
                        }
                    )
                }
            }

            if (workLog.imageUrls.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(end = 12.dp)
                ) {
                    itemsIndexed(workLog.imageUrls) { index, url ->
                        AsyncImage(
                            model = url,
                            contentDescription = "첨부 이미지 ${index + 1}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onImageClick(index) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeleteWorkLogConfirmDialog(
    workLog: WorkLog,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "작업 일지 삭제",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                "${workLog.schoolName} ${workLog.date} 일지를 삭제하시겠습니까?\n이 작업은 되돌릴 수 없습니다.",
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
