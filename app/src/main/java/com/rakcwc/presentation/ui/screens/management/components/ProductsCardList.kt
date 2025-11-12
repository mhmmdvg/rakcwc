package com.rakcwc.presentation.ui.screens.management.components

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rakcwc.presentation.ui.components.CacheImage
import com.rakcwc.presentation.ui.theme.AccentColor

@Composable
fun ProductsCardList(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
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
        modifier = modifier
            .background(Color.Transparent)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
            ) {
                onClick()
            }
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
                .fillMaxSize()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CacheImage(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp)),
                imageUrl = "https://github.com/evilrabbit.png"
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {

                Text(
                    text = "EvilRabbit",
                    color = Color.Black,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "ER",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Rp.20000",
                    color = AccentColor,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewProductsCardList() {
    ProductsCardList()
}