package com.github.junhee8649.cleancalendar.data

import com.github.junhee8649.cleancalendar.data.source.network.WorkLogDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage

class DefaultWorkLogRepository(
    private val supabase: SupabaseClient
) : WorkLogRepository {

    private val bucket get() = supabase.storage["work-log-images"]

    override suspend fun getWorkLogs(): List<WorkLog> =
        supabase.from("work_logs")
            .select(columns = Columns.raw("*, schools(name)")) {
                order("created_at", Order.DESCENDING)
            }
            .decodeList<WorkLogDto>()
            .map { it.toWorkLog() }

    override suspend fun getWorkLog(id: String): WorkLog? =
        supabase.from("work_logs")
            .select(columns = Columns.raw("*, schools(name)")) {
                filter { eq("id", id) }
            }
            .decodeSingleOrNull<WorkLogDto>()
            ?.toWorkLog()

    override suspend fun addWorkLog(workLog: WorkLog) {
        supabase.from("work_logs").insert(workLog.toInsertDto())
    }

    override suspend fun deleteWorkLog(id: String) {
        supabase.from("work_logs").delete {
            filter { eq("id", id) }
        }
    }

    // DB에 full public URL을 저장 — 읽을 때 변환 불필요
    override suspend fun uploadImage(imageBytes: ByteArray, fileName: String): String {
        bucket.upload(fileName, imageBytes)
        return bucket.publicUrl(fileName)
    }
}
