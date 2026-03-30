package com.github.junhee8649.cleancalendar.data.source.network

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class WorkLogDto(
    val id: String,
    @SerialName("school_id") val schoolId: String,
    val date: String,
    @SerialName("task_categories") val taskCategories: List<String> = emptyList(),
    @SerialName("issues_text") val issuesText: String = "",
    @SerialName("image_paths") val imagePaths: List<String> = emptyList(),
    // insert 시 서버가 자동 생성 — null이면 JSON에 포함되지 않음
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @SerialName("created_at") val createdAt: String? = null,
    // Postgrest embedded resource (select 전용, insert 시 제외)
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val schools: EmbeddedSchoolDto? = null
)

@Serializable
data class EmbeddedSchoolDto(
    @SerialName("name") val name: String
)
