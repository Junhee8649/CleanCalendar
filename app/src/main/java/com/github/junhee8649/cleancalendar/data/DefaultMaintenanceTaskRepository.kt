package com.github.junhee8649.cleancalendar.data

import com.github.junhee8649.cleancalendar.data.source.network.MaintenanceTaskDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

class DefaultMaintenanceTaskRepository(
    private val supabase: SupabaseClient
) : MaintenanceTaskRepository {

    override suspend fun getTasksBySchool(schoolId: String): List<MaintenanceTask> =
        supabase.from("maintenance_tasks").select {
            filter { eq("school_id", schoolId) }
        }.decodeList<MaintenanceTaskDto>().map { it.toTask() }

    override suspend fun getTasksByMonth(year: Int, month: Int): List<MaintenanceTask> =
        supabase.from("maintenance_tasks").select {
            filter {
                eq("year", year)
                eq("month", month)
            }
        }.decodeList<MaintenanceTaskDto>().map { it.toTask() }

    override suspend fun getTask(id: String): MaintenanceTask? =
        supabase.from("maintenance_tasks").select {
            filter { eq("id", id) }
        }.decodeSingleOrNull<MaintenanceTaskDto>()?.toTask()

    override suspend fun addTask(task: MaintenanceTask) {
        supabase.from("maintenance_tasks").insert(task.toDto())
    }

    override suspend fun updateTask(task: MaintenanceTask) {
        supabase.from("maintenance_tasks").update(task.toDto()) {
            filter { eq("id", task.id) }
        }
    }

    override suspend fun deleteTask(id: String) {
        supabase.from("maintenance_tasks").delete {
            filter { eq("id", id) }
        }
    }
}
