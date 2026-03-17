package com.github.junhee8649.cleancalendar.data

import com.github.junhee8649.cleancalendar.data.source.network.SchoolDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

class DefaultSchoolRepository(
    private val supabase: SupabaseClient
) : SchoolRepository {

    override suspend fun getSchools(): List<School> =
        supabase.from("schools").select().decodeList<SchoolDto>().map { it.toSchool() }

    override suspend fun getSchool(id: String): School? =
        supabase.from("schools").select {
            filter { eq("id", id) }
        }.decodeSingleOrNull<SchoolDto>()?.toSchool()

    override suspend fun addSchool(school: School) {
        supabase.from("schools").insert(school.toDto())
    }

    override suspend fun updateSchool(school: School) {
        supabase.from("schools").update(school.toDto()) {
            filter { eq("id", school.id) }
        }
    }

    override suspend fun deleteSchool(id: String) {
        supabase.from("schools").delete {
            filter { eq("id", id) }
        }
    }
}
