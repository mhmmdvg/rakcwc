package com.rakcwc.presentation.ui.screens.home.components

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.rakcwc.domain.models.CatalogsResponse
import com.rakcwc.presentation.ui.components.CacheImage

@Composable
fun CatalogCard(
    modifier: Modifier = Modifier,
    data: CatalogsResponse,
    onClick: () -> Unit = {},
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(
            durationMillis = 100,
            easing = LinearOutSlowInEasing
        ),
        label = "scale_animation"
    )

    Box(
        modifier = modifier
            .background(
                color = Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
            ) {
                onClick()
            }
    ) {
        CacheImage(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp)),
            imageUrl = data.imageUrl ?: "",
            description = data.name,
            contentScale = ContentScale.Crop
        )
    }
}