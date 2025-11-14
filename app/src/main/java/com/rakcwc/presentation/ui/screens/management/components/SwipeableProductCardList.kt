package com.rakcwc.presentation.ui.screens.management.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.Trash2
import com.rakcwc.presentation.ui.components.CacheImage
import com.rakcwc.presentation.ui.theme.AccentColor
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SwipeableProductsCardList(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    imageUrl: String,
    title: String,
    subTitle: String,
    price: Double? = null,
) {
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    val maxSwipeDistance = with(density) { -162.dp.toPx() }
    val swipeThreshold = maxSwipeDistance * 0.5f

    var isRevealed by remember { mutableStateOf(false) }
    val offsetX = remember { Animatable(0f) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // Background with Edit and Delete buttons
        SwipeActionsBackground(
            onEdit = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                scope.launch {
                    offsetX.animateTo(0f, tween(300, easing = LinearOutSlowInEasing))
                    isRevealed = false
                }
                onEdit()
            },
            onDelete = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                scope.launch {
                    offsetX.animateTo(0f, tween(300, easing = LinearOutSlowInEasing))
                    isRevealed = false
                }
                onDelete()
            }
        )

        // Foreground content
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                if (isRevealed) {
                                    // Already revealed, check if swiping right to close
                                    if (offsetX.value > maxSwipeDistance * 0.3f) {
                                        offsetX.animateTo(
                                            0f,
                                            tween(300, easing = LinearOutSlowInEasing)
                                        )
                                        isRevealed = false
                                    } else {
                                        offsetX.animateTo(
                                            maxSwipeDistance,
                                            tween(300, easing = LinearOutSlowInEasing)
                                        )
                                    }
                                } else {
                                    // Not revealed, check if swiping left to open
                                    if (offsetX.value < swipeThreshold) {
                                        offsetX.animateTo(
                                            maxSwipeDistance,
                                            tween(300, easing = LinearOutSlowInEasing)
                                        )
                                        isRevealed = true
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    } else {
                                        offsetX.animateTo(
                                            0f,
                                            tween(300, easing = LinearOutSlowInEasing)
                                        )
                                    }
                                }
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            scope.launch {
                                val newValue = (offsetX.value + dragAmount).coerceIn(
                                    maxSwipeDistance,
                                    0f
                                )
                                offsetX.snapTo(newValue)
                            }
                        }
                    )
                }
        ) {
            ProductCardContent(
                onClick = {
                    if (!isRevealed) {
                        onClick()
                    } else {
                        // Close if clicking while revealed
                        scope.launch {
                            offsetX.animateTo(0f, tween(300, easing = LinearOutSlowInEasing))
                            isRevealed = false
                        }
                    }
                },
                imageUrl = imageUrl,
                title = title,
                subTitle = subTitle,
                price = price
            )
        }
    }
}

@Composable
private fun SwipeActionsBackground(
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Edit Button (Orange/Yellow)
        Box(
            modifier = Modifier
                .width(80.dp)
                .fillMaxHeight()
                .padding(vertical = 8.dp)
                .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                .background(Color(0xFFFF9800))
                .clickable { onEdit() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Lucide.Pencil,
                    contentDescription = "Edit",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Edit",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.width(2.dp))

        // Delete Button (Red)
        Box(
            modifier = Modifier
                .width(80.dp)
                .padding(vertical = 8.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp))
                .background(Color(0xFFE53935))
                .clickable { onDelete() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Lucide.Trash2,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Delete",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ProductCardContent(
    onClick: () -> Unit,
    imageUrl: String,
    title: String,
    subTitle: String,
    price: Double?
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(
            durationMillis = 100,
            easing = LinearOutSlowInEasing
        ),
        label = "scale_animation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
            ) {
                onClick()
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CacheImage(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp)),
                imageUrl = imageUrl,
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = title,
                    color = Color.Black,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subTitle,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )
                price?.let {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Rp ${String.format("%,.0f", it)}",
                        color = AccentColor,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}