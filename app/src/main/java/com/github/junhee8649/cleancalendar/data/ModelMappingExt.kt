package com.github.junhee8649.cleancalendar.data

import com.github.junhee8649.cleancalendar.data.source.network.MaintenanceTaskDto
import com.github.junhee8649.cleancalendar.data.source.network.SchoolDto
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
