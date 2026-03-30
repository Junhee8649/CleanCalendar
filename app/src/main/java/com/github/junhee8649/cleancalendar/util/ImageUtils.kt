package com.github.junhee8649.cleancalendar.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

suspend fun compressImageUri(context: Context, uri: Uri): ByteArray {
    // 1단계: URI → 원본 바이트 (I/O bound)
    val rawBytes = withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("Cannot open URI: $uri")
    }
    // 2단계: 디코드 + 리사이즈 + 압축 (CPU bound)
    return withContext(Dispatchers.Default) {
        val bitmap = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size)
            ?: error("이미지 디코딩 실패: 손상된 파일")

        // 긴 변 기준 max 2048px — 도면 작은 숫자 가독성 확보
        val maxDim = 2048
        val scale = maxDim.toFloat() / maxOf(bitmap.width, bitmap.height)
        val scaled = if (scale < 1f)
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * scale).toInt(),
                (bitmap.height * scale).toInt(),
                true
            )
        else bitmap

        // WebP: JPEG 동급 품질에서 ~25-30% 용량 절감 + 경계선 선명
        // minSdk 26 대응 — WEBP_LOSSY는 API 30+
        @Suppress("DEPRECATION")
        val format = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            Bitmap.CompressFormat.WEBP_LOSSY
        else
            Bitmap.CompressFormat.WEBP

        ByteArrayOutputStream().also { out ->
            scaled.compress(format, 85, out)
            if (scaled !== bitmap) scaled.recycle()
            bitmap.recycle()
        }.toByteArray()
    }
}
