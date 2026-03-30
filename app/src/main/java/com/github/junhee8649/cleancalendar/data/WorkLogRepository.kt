package com.github.junhee8649.cleancalendar.data

interface WorkLogRepository {
    suspend fun getWorkLogs(): List<WorkLog>
    suspend fun getWorkLog(id: String): WorkLog?
    suspend fun addWorkLog(workLog: WorkLog)
    suspend fun deleteWorkLog(id: String)
    suspend fun uploadImage(imageBytes: ByteArray, fileName: String): String
}
