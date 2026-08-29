package com.cotx.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

object ImageCompressor {

    /**
     * Compresses and resizes an image safely without OOM (~150-200 KB)
     */
    fun compressUriToWebp(
        context: Context,
        imageUri: Uri,
        maxWidth: Int = 1080,
        maxHeight: Int = 1920,
        quality: Int = 80
    ): ByteArray {
        return try {
            val rawBytes = context.contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
                ?: return byteArrayOf()

            if (rawBytes.isEmpty()) return byteArrayOf()

            // 1. Read image dimensions
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, options)

            if (options.outWidth <= 0 || options.outHeight <= 0) {
                return byteArrayOf()
            }

            // 2. Calculate sample size and decode sampled bitmap
            options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight)
            options.inJustDecodeBounds = false

            val decodedBitmap = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, options)
                ?: return byteArrayOf()

            // 3. Handle Exif rotation from rawBytes
            val rotatedBitmap = rotateImageIfRequired(rawBytes, decodedBitmap)

            // 4. Calculate exact scaled dimensions maintaining aspect ratio
            val (scaledWidth, scaledHeight) = getScaledDimensions(
                rotatedBitmap.width,
                rotatedBitmap.height,
                maxWidth,
                maxHeight
            )

            val finalBitmap = if (scaledWidth != rotatedBitmap.width || scaledHeight != rotatedBitmap.height) {
                Bitmap.createScaledBitmap(rotatedBitmap, scaledWidth, scaledHeight, true)
            } else {
                rotatedBitmap
            }

            // 5. Compress to JPEG byte array
            val outputStream = ByteArrayOutputStream()
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            val resultBytes = outputStream.toByteArray()

            // 6. Safely recycle temporary bitmaps
            if (finalBitmap != rotatedBitmap && !rotatedBitmap.isRecycled) {
                rotatedBitmap.recycle()
            }
            if (rotatedBitmap != decodedBitmap && !decodedBitmap.isRecycled) {
                decodedBitmap.recycle()
            }
            if (finalBitmap != decodedBitmap && !finalBitmap.isRecycled) {
                finalBitmap.recycle()
            }

            resultBytes
        } catch (e: Exception) {
            e.printStackTrace()
            byteArrayOf()
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun rotateImageIfRequired(rawBytes: ByteArray, bitmap: Bitmap): Bitmap {
        return try {
            val orientation = ByteArrayInputStream(rawBytes).use { inputStream ->
                val ei = ExifInterface(inputStream)
                ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            }

            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
                else -> bitmap
            }
        } catch (e: Exception) {
            bitmap
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun getScaledDimensions(
        originalWidth: Int,
        originalHeight: Int,
        maxWidth: Int,
        maxHeight: Int
    ): Pair<Int, Int> {
        var width = originalWidth
        var height = originalHeight

        if (width > maxWidth || height > maxHeight) {
            val widthRatio = maxWidth.toFloat() / originalWidth
            val heightRatio = maxHeight.toFloat() / originalHeight
            val ratio = minOf(widthRatio, heightRatio)

            width = (originalWidth * ratio).toInt()
            height = (originalHeight * ratio).toInt()
        }
        return Pair(width, height)
    }
}



