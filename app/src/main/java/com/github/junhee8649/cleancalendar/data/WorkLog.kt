package com.github.junhee8649.cleancalendar.data

import java.time.LocalDate

data class WorkLog(
    val id: String,
    val schoolId: String,
    val schoolName: String,
    val date: LocalDate,
    val taskCategories: List<String>,
    val issuesText: String,
    val imageUrls: List<String>   // Supabase Storage public URL (변환 완료)
)
