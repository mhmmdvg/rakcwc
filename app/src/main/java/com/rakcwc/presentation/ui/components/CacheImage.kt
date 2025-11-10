package com.rakcwc.presentation.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rakcwc.utils.shimmerEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.security.MessageDigest

private class LRUCache<K, V>(private val maxSize: Int) : LinkedHashMap<K, V>(0, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean = size > maxSize
}

private val inMemoryCache = LRUCache<String, Bitmap>(50)

@Composable
fun CacheImage(
    modifier: Modifier = Modifier,
    imageUrl: String,
    description: String? = null,
    contentScale: ContentScale = ContentScale.Fit,
    maxWidth: Dp? = null,
    maxHeight: Dp? = null,
    onLoadingComplete: (() -> Unit)? = null,
    onFailure: ((error: Throwable) -> Unit)? = null,
) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val density = LocalDensity.current

    LaunchedEffect(imageUrl) {
        if (imageUrl.isBlank()) {
            isLoading = false
            hasError = true
            onFailure?.invoke(Throwable("Image url is empty"))
            return@LaunchedEffect
        }

        isLoading = true
        hasError = false

        try {
            val cachedBitmap = inMemoryCache[imageUrl]

            if (cachedBitmap != null && !cachedBitmap.isRecycled) {
                bitmap = cachedBitmap
                isLoading = false
                onLoadingComplete?.invoke()
                return@LaunchedEffect
            }

            val targetWidth = maxWidth?.let { with(density) { it.toPx().toInt() } }
            val targetHeight = maxHeight?.let { with(density) { it.toPx().toInt() } }

            val loadedBitmap = withContext(Dispatchers.IO) {
                loadImageWithCache(context, imageUrl, targetWidth, targetHeight)
            }

            if (loadedBitmap != null) {
                inMemoryCache[imageUrl] = loadedBitmap
                bitmap = loadedBitmap
                onLoadingComplete?.invoke()
            } else {
                hasError = true
                onFailure?.invoke(Throwable("Failed to load image"))
            }
        } catch (error: Exception) {
            error.printStackTrace()
            hasError = true
            onFailure?.invoke(error)
        } finally {
            isLoading = false
        }
    }

    when {
        isLoading -> Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .shimmerEffect()
            )
        }
        bitmap != null -> Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = description,
            contentScale = contentScale,
            modifier = modifier,
        )
        hasError -> Box(
            modifier = modifier.background(Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = BitmapFactory.decodeResource(context.resources, android.R.drawable.stat_notify_error).asImageBitmap(),
                contentDescription = description,
                contentScale = contentScale,
                modifier = modifier,
            )
        }
    }
}

private suspend fun loadImageWithCache(
    context: Context,
    url: String,
    targetWidth: Int? = null,
    targetHeight: Int? = null,
): Bitmap? {
    return withContext(Dispatchers.IO) {
        try {
            val cacheFile = getCacheFile(context, url)

            if (cacheFile.exists()) {
                val diskBitmap = decodeBitmapFromFile(cacheFile.absolutePath, targetWidth, targetHeight)

                if (diskBitmap != null) return@withContext diskBitmap
            }

            val networkBitmap = loadImageFromUrl(url, targetWidth, targetHeight)
            if (networkBitmap != null) {
                saveToDiskCache(cacheFile, networkBitmap)
            }

            networkBitmap
        } catch (error: Exception) {
            error.printStackTrace()
            null
        }
    }
}

private suspend fun loadImageFromUrl(
    url: String,
    targetWidth: Int? = null,
    targetHeight: Int? = null,
): Bitmap? {
    return withContext(Dispatchers.IO) {
        try {
            if (targetWidth != null && targetHeight != null) {
                val connection = URL(url).openConnection().apply {
                    connectTimeout = 10000
                    readTimeout = 10000
                    setRequestProperty("User-Agent", "Android App")
                }

                val imageData = connection.getInputStream().use { it.readBytes() }
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeByteArray(imageData, 0, imageData.size, options)

                options.inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight)
                options.inJustDecodeBounds = false

                BitmapFactory.decodeByteArray(imageData, 0, imageData.size, options)
            } else {
                val connection = URL(url).openConnection().apply {
                    connectTimeout = 10000
                    readTimeout = 10000
                    setRequestProperty("User-Agent", "Android App")
                }

                connection.getInputStream().use { BitmapFactory.decodeStream(it) }
            }
        } catch (error: Exception) {
            error.printStackTrace()
            null
        }
    }
}

private fun decodeBitmapFromFile(
    filePath: String,
    targetWidth: Int? = null,
    targetHeight: Int? = null,
): Bitmap? {
    return try {
        if (targetWidth != null && targetHeight != null) {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }

            BitmapFactory.decodeFile(filePath, options)

            options.inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight)
            options.inJustDecodeBounds = false

            BitmapFactory.decodeFile(filePath, options)
        } else {
            BitmapFactory.decodeFile(filePath)
        }
    } catch (error: Exception) {
        error.printStackTrace()
        null
    }
}

private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    val (height: Int, width: Int) = options.run { outHeight to outWidth }
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

private fun getCacheFile(context: Context, url: String): File {
    val cacheDir = File(context.cacheDir, "image_cache")
    if (!cacheDir.exists()) {
        cacheDir.mkdirs()
    }

    return File(cacheDir, getCacheFileName(url))
}

private fun getCacheFileName(url: String): String {
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(url.toByteArray())

    return digest.fold("") { str, it -> str + "%02x".format(it) }
}

private fun saveToDiskCache(cacheFile: File, bitmap: Bitmap) {
    try {
        FileOutputStream(cacheFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            out.flush()
        }
    } catch (error: Exception) {
        error.printStackTrace()
    }
}