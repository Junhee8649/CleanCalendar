package com.github.junhee8649.cleancalendar.data

class DefaultMaintenanceTaskRepository : MaintenanceTaskRepository {

    override suspend fun getTasksBySchool(schoolId: String): List<MaintenanceTask> = emptyList()

    override suspend fun getTasksByMonth(year: Int, month: Int): List<MaintenanceTask> = emptyList()

    override suspend fun getTask(id: String): MaintenanceTask? = null

    override suspend fun addTask(task: MaintenanceTask) = Unit

    override suspend fun updateTask(task: MaintenanceTask) = Unit

    override suspend fun deleteTask(id: String) = Unit
}
