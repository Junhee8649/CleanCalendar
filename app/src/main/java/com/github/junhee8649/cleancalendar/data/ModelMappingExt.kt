package com.github.junhee8649.cleancalendar.data

import com.github.junhee8649.cleancalendar.data.source.network.MaintenanceTaskDto
import com.github.junhee8649.cleancalendar.data.source.network.SchoolDto
import com.github.junhee8649.cleancalendar.data.source.network.WorkLogDto
import java.time.LocalDate

fun SchoolDto.toSchool(): School = School(
    id = id,
    name = name,
    address = address,
    contactName = contactName,
    contactPhone = contactPhone,
    equipmentInfo = equipmentInfo,
    memo = memo
)

fun School.toDto(): SchoolDto = SchoolDto(
    id = id,
    name = name,
    address = address,
    contactName = contactName,
    contactPhone = contactPhone,
    equipmentInfo = equipmentInfo,
    memo = memo
)

fun MaintenanceTaskDto.toTask(): MaintenanceTask = MaintenanceTask(
    id = id,
    schoolId = schoolId,
    month = month,
    year = year,
    taskDescription = taskDescription,
    isCompleted = isCompleted,
    completedDate = completedDate?.let { LocalDate.parse(it) },
    notes = notes,
    scheduledDate = scheduledDate?.let { LocalDate.parse(it) }
)

fun MaintenanceTask.toDto(): MaintenanceTaskDto = MaintenanceTaskDto(
    id = id,
    schoolId = schoolId,
    month = month,
    year = year,
    taskDescription = taskDescription,
    isCompleted = isCompleted,
    completedDate = completedDate?.toString(),
    notes = notes,
    scheduledDate = scheduledDate?.toString()
)

// DB의 image_paths 컬럼에 full public URL을 저장하므로 변환 불필요
fun WorkLogDto.toWorkLog(): WorkLog = WorkLog(
    id = id,
    schoolId = schoolId,
    schoolName = schools?.name ?: "",
    date = LocalDate.parse(date),
    taskCategories = taskCategories,
    issuesText = issuesText,
    imageUrls = imagePaths
)

fun WorkLog.toInsertDto(): WorkLogDto = WorkLogDto(
    id = id,
    schoolId = schoolId,
    date = date.toString(),
    taskCategories = taskCategories,
    issuesText = issuesText,
    imagePaths = imageUrls
)
