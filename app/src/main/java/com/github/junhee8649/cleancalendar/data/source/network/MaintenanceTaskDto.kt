package com.github.junhee8649.cleancalendar.data.source.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MaintenanceTaskDto(
    val id: String,
    @SerialName("school_id") val schoolId: String,
    val month: Int,
    val year: Int,
    @SerialName("task_description") val taskDescription: String,
    @SerialName("is_completed") val isCompleted: Boolean,
    @SerialName("completed_date") val completedDate: String?,
    val notes: String,
    @SerialName("scheduled_date") val scheduledDate: String?
)
