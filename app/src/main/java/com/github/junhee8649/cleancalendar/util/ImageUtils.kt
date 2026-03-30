package com.github.junhee8649.cleancalendar.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException

suspend fun compressImageUri(context: Context, uri: Uri): ByteArray {
    // 1단계: URI → 원본 바이트 + EXIF 방향 (I/O bound, 스트림 한 번만 열기)
    val (rawBytes, exifOrientation) = withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            val bytes = stream.readBytes()
            val orientation = try {
                ExifInterface(ByteArrayInputStream(bytes))
                    .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            } catch (e: IOException) {
                Log.w("ImageUtils", "EXIF 읽기 실패, 방향 보정 생략", e)
                ExifInterface.ORIENTATION_NORMAL
            }
            bytes to orientation
        } ?: error("Cannot open URI: $uri")
    }

    // 2단계: 디코드 + EXIF 방향 보정 + 리사이즈 + 압축 (CPU bound)
    return withContext(Dispatchers.Default) {
        val bitmap = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size)
            ?: error("이미지 디코딩 실패: 손상된 파일")

        // EXIF 방향 보정 (BitmapFactory는 EXIF를 무시함)
        val matrix = Matrix()
        when (exifOrientation) {
            ExifInterface.ORIENTATION_ROTATE_90  -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL   -> matrix.preScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE  -> { matrix.preScale(-1f, 1f); matrix.postRotate(270f) }
            ExifInterface.ORIENTATION_TRANSVERSE -> { matrix.preScale(-1f, 1f); matrix.postRotate(90f) }
        }
        val oriented = if (!matrix.isIdentity)
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true).also { bitmap.recycle() }
        else bitmap

        // 긴 변 기준 max 2048px — 도면 작은 숫자 가독성 확보
        val maxDim = 2048
        val scale = maxDim.toFloat() / maxOf(oriented.width, oriented.height)
        val scaled = if (scale < 1f)
            Bitmap.createScaledBitmap(
                oriented,
                (oriented.width * scale).toInt(),
                (oriented.height * scale).toInt(),
                true
            )
        else oriented

        // WebP: JPEG 동급 품질에서 ~25-30% 용량 절감 + 경계선 선명
        // minSdk 26 대응 — WEBP_LOSSY는 API 30+
        @Suppress("DEPRECATION")
        val format = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            Bitmap.CompressFormat.WEBP_LOSSY
        else
            Bitmap.CompressFormat.WEBP

        ByteArrayOutputStream().also { out ->
            scaled.compress(format, 85, out)
            if (scaled !== oriented) scaled.recycle()
            oriented.recycle()
        }.toByteArray()
    }
}
