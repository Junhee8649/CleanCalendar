package com.github.junhee8649.cleancalendar.data

import java.time.LocalDate

data class MaintenanceTask(
    val id: String,
    val schoolId: String,
    val month: Int,
    val year: Int,
    val taskDescription: String,
    val isCompleted: Boolean,
    val completedDate: LocalDate?,
    val notes: String,
    val scheduledDate: LocalDate?
)
