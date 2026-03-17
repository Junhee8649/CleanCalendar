package com.github.junhee8649.cleancalendar.data.source.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SchoolDto(
    val id: String,
    val name: String,
    val address: String,
    @SerialName("contact_name") val contactName: String,
    @SerialName("contact_phone") val contactPhone: String,
    @SerialName("equipment_info") val equipmentInfo: String,
    val memo: String
)
