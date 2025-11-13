package com.rakcwc.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

object ImageCropper {
    suspend fun cropAndSaveImage(
        context: Context,
        bitmap: Bitmap,
        scale: Float,
        offsetX: Float,
        offsetY: Float,
        canvasSize: IntSize
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val canvasWidth = canvasSize.width.toFloat()
            val canvasHeight = canvasSize.height.toFloat()
            val cropSize = min(canvasWidth, canvasHeight).toInt()

            val imageWidth = bitmap.width.toFloat()
            val imageHeight = bitmap.height.toFloat()
            val imageAspect = imageWidth / imageHeight
            val canvasAspect = canvasWidth / canvasHeight

            val scaleFactor = if (imageAspect > canvasAspect) {
                canvasHeight / imageHeight
            } else {
                canvasWidth / imageWidth
            }

            val scaledWidth = imageWidth * scaleFactor * scale
            val scaledHeight = imageHeight * scaleFactor * scale

            val left = (canvasWidth - scaledWidth) / 2f + offsetX
            val top = (canvasHeight - scaledHeight) / 2f + offsetY

            val cropLeft = (canvasWidth - cropSize) / 2f
            val cropTop = (canvasHeight - cropSize) / 2f

            val cropX = ((cropLeft - left) / scaledWidth * imageWidth).coerceIn(0f, imageWidth)
            val cropY = ((cropTop - top) / scaledHeight * imageHeight).coerceIn(0f, imageHeight)
            val cropWidth = (cropSize / scaledWidth * imageWidth).coerceAtMost(imageWidth - cropX)
            val cropHeight = (cropSize / scaledHeight * imageHeight).coerceAtMost(imageHeight - cropY)

            val croppedBitmap = Bitmap.createBitmap(
                bitmap,
                cropX.toInt(),
                cropY.toInt(),
                cropWidth.toInt().coerceAtLeast(1),
                cropHeight.toInt().coerceAtLeast(1)
            )

            val finalSize = min(croppedBitmap.width, croppedBitmap.height)
            val squareBitmap = Bitmap.createBitmap(
                croppedBitmap,
                (croppedBitmap.width - finalSize) / 2,
                (croppedBitmap.height - finalSize) / 2,
                finalSize,
                finalSize
            )

            // Use .jpg extension explicitly for proper MIME type detection
            val file = File(context.cacheDir, "cropped_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { outputStream ->
                // Compress as JPEG with high quality
                squareBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                outputStream.flush()
            }

            // Clean up bitmaps
            if (croppedBitmap != squareBitmap) {
                croppedBitmap.recycle()
            }
            squareBitmap.recycle()

            // Return simple file URI (not FileProvider URI)
            // This ensures proper MIME type detection
            Uri.fromFile(file)
        } catch (error: Exception) {
            error.printStackTrace()
            null
        }
    }
}