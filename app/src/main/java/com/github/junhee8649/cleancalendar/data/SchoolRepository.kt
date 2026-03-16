package com.github.junhee8649.cleancalendar.data

interface SchoolRepository {
    suspend fun getSchools(): List<School>
    suspend fun getSchool(id: String): School?
    suspend fun addSchool(school: School)
    suspend fun updateSchool(school: School)
    suspend fun deleteSchool(id: String)
}
