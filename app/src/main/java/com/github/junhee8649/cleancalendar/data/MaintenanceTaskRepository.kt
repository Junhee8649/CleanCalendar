package com.github.junhee8649.cleancalendar.data

interface MaintenanceTaskRepository {
    suspend fun getTasksBySchool(schoolId: String): List<MaintenanceTask>
    suspend fun getTasksByMonth(year: Int, month: Int): List<MaintenanceTask>
    suspend fun getTask(id: String): MaintenanceTask?
    suspend fun addTask(task: MaintenanceTask)
    suspend fun updateTask(task: MaintenanceTask)
    suspend fun deleteTask(id: String)
}
