package com.rakcwc.presentation.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.X
import com.rakcwc.presentation.ui.theme.AccentColor
import com.rakcwc.utils.ImageCropper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.min

@Composable
fun ImageCropDialog(
    imageUri: Uri,
    onDismiss: () -> Unit,
    onCropComplete: (Uri) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(imageUri) {
        withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(imageUri)
                bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
            } catch (error: Exception) {
                error.printStackTrace()
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header with better contrast
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Black.copy(alpha = 0.8f),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Close button with background
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = Color.White.copy(alpha = 0.1f),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Lucide.X,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Text(
                            text = "Crop Image",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        // Done button with accent color background
                        Button(
                            onClick = {
                                scope.launch {
                                    isProcessing = true
                                    bitmap?.let { bmp ->
                                        val croppedUri = ImageCropper.cropAndSaveImage(
                                            context = context,
                                            bitmap = bmp,
                                            scale = scale,
                                            offsetX = offsetX,
                                            offsetY = offsetY,
                                            canvasSize = canvasSize
                                        )
                                        Log.d("ImageCropper", "croppedUri: $croppedUri")
                                        croppedUri?.let { onCropComplete(it) }
                                    }
                                    isProcessing = false
                                }
                            },
                            enabled = !isProcessing,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentColor,
                                disabledContainerColor = AccentColor.copy(alpha = 0.6f)
                            ),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
                            modifier = Modifier.height(40.dp)
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Done",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }

                // Canvas area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .onSizeChanged { canvasSize = it },
                    contentAlignment = Alignment.Center
                ) {
                    bitmap?.let { bmp ->
                        val imageBitmap = remember(bmp) { bmp.asImageBitmap() }

                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    // Handle pinch-to-zoom
                                    detectTransformGestures { _, pan, zoom, _ ->
                                        scale = (scale * zoom).coerceIn(1f, 5f)

                                        // Update offset with pan
                                        offsetX += pan.x
                                        offsetY += pan.y

                                        // Constrain offset based on current scale
                                        val canvasW = canvasSize.width.toFloat()
                                        val canvasH = canvasSize.height.toFloat()
                                        val imageW = imageBitmap.width.toFloat()
                                        val imageH = imageBitmap.height.toFloat()
                                        val imageAspect = imageW / imageH
                                        val canvasAspect = canvasW / canvasH

                                        val scaleFactor = if (imageAspect > canvasAspect) {
                                            canvasH / imageH
                                        } else {
                                            canvasW / imageW
                                        }

                                        val scaledW = imageW * scaleFactor * scale
                                        val scaledH = imageH * scaleFactor * scale

                                        val maxOffsetX = (scaledW - canvasW).coerceAtLeast(0f) / 2f
                                        val maxOffsetY = (scaledH - canvasH).coerceAtLeast(0f) / 2f

                                        offsetX = offsetX.coerceIn(-maxOffsetX, maxOffsetX)
                                        offsetY = offsetY.coerceIn(-maxOffsetY, maxOffsetY)
                                    }
                                }
                                .pointerInput(Unit) {
                                    // Handle drag (single finger)
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()

                                        offsetX += dragAmount.x
                                        offsetY += dragAmount.y

                                        // Constrain offset
                                        val canvasW = canvasSize.width.toFloat()
                                        val canvasH = canvasSize.height.toFloat()
                                        val imageW = imageBitmap.width.toFloat()
                                        val imageH = imageBitmap.height.toFloat()
                                        val imageAspect = imageW / imageH
                                        val canvasAspect = canvasW / canvasH

                                        val scaleFactor = if (imageAspect > canvasAspect) {
                                            canvasH / imageH
                                        } else {
                                            canvasW / imageW
                                        }

                                        val scaledW = imageW * scaleFactor * scale
                                        val scaledH = imageH * scaleFactor * scale

                                        val maxOffsetX = (scaledW - canvasW).coerceAtLeast(0f) / 2f
                                        val maxOffsetY = (scaledH - canvasH).coerceAtLeast(0f) / 2f

                                        offsetX = offsetX.coerceIn(-maxOffsetX, maxOffsetX)
                                        offsetY = offsetY.coerceIn(-maxOffsetY, maxOffsetY)
                                    }
                                }
                        ) {
                            val canvasWidth = size.width
                            val canvasHeight = size.height
                            val cropSize = min(canvasWidth, canvasHeight)

                            val imageWidth = imageBitmap.width.toFloat()
                            val imageHeight = imageBitmap.height.toFloat()
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

                            // Draw image
                            drawImage(
                                image = imageBitmap,
                                srcOffset = IntOffset.Zero,
                                srcSize = IntSize(
                                    imageBitmap.width,
                                    imageBitmap.height
                                ),
                                dstOffset = IntOffset(
                                    left.toInt(),
                                    top.toInt()
                                ),
                                dstSize = IntSize(
                                    scaledWidth.toInt(),
                                    scaledHeight.toInt()
                                )
                            )

                            val cropLeft = (canvasWidth - cropSize) / 2f
                            val cropTop = (canvasHeight - cropSize) / 2f

                            // Draw overlay (darkened area outside crop)
                            // Top
                            drawRect(
                                color = Color.Black.copy(alpha = 0.7f),
                                topLeft = Offset(0f, 0f),
                                size = Size(canvasWidth, cropTop)
                            )
                            // Bottom
                            drawRect(
                                color = Color.Black.copy(alpha = 0.7f),
                                topLeft = Offset(0f, cropTop + cropSize),
                                size = Size(canvasWidth, canvasHeight - (cropTop + cropSize))
                            )
                            // Left
                            drawRect(
                                color = Color.Black.copy(alpha = 0.7f),
                                topLeft = Offset(0f, cropTop),
                                size = Size(cropLeft, cropSize)
                            )
                            // Right
                            drawRect(
                                color = Color.Black.copy(alpha = 0.7f),
                                topLeft = Offset(cropLeft + cropSize, cropTop),
                                size = Size(canvasWidth - (cropLeft + cropSize), cropSize)
                            )

                            // Crop border (white frame)
                            drawRect(
                                color = Color.White,
                                topLeft = Offset(cropLeft, cropTop),
                                size = Size(cropSize, cropSize),
                                style = Stroke(width = 3.dp.toPx())
                            )

                            // Grid lines (rule of thirds)
                            val gridLineColor = Color.White.copy(alpha = 0.6f)
                            val gridLineWidth = 1.5.dp.toPx()

                            // Vertical lines
                            for (i in 1..2) {
                                val x = cropLeft + (cropSize * i / 3f)
                                drawLine(
                                    color = gridLineColor,
                                    start = Offset(x, cropTop),
                                    end = Offset(x, cropTop + cropSize),
                                    strokeWidth = gridLineWidth
                                )
                            }

                            // Horizontal lines
                            for (i in 1..2) {
                                val y = cropTop + (cropSize * i / 3f)
                                drawLine(
                                    color = gridLineColor,
                                    start = Offset(cropLeft, y),
                                    end = Offset(cropLeft + cropSize, y),
                                    strokeWidth = gridLineWidth
                                )
                            }
                        }
                    }
                }

                // Instructions at bottom with better contrast
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Black.copy(alpha = 0.8f),
                    shadowElevation = 4.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp, horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Pinch to zoom • Drag to reposition",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Square crop area (1:1 ratio)",
                                color = Color.White.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}