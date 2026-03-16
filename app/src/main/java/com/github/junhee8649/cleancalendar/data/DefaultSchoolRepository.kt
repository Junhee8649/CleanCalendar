package com.github.junhee8649.cleancalendar.data

class DefaultSchoolRepository : SchoolRepository {

    override suspend fun getSchools(): List<School> = emptyList()

    override suspend fun getSchool(id: String): School? = null

    override suspend fun addSchool(school: School) = Unit

    override suspend fun updateSchool(school: School) = Unit

    override suspend fun deleteSchool(id: String) = Unit
}
