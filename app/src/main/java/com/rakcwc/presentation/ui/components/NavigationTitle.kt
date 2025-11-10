package com.rakcwc.presentation.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import kotlin.math.max
import kotlin.math.min

@Composable
fun NavigationTitle(
    modifier: Modifier = Modifier,
    navController: NavController,
    title: String,
    scrollOffset: Int = 0,
    maxOffset: Int = 200,
) {
    val interactionSource = remember { MutableInteractionSource() }

    val collapseProgress = min(1f, max(0f, scrollOffset.toFloat() / maxOffset))

    val titleSize by animateFloatAsState(
        targetValue = if (collapseProgress < 0.5f) 32f else 20f,
        label = "titleSize"
    )

    val largeTitleAlpha by animateFloatAsState(
        targetValue = 1f - (collapseProgress * 2f).coerceIn(0f, 1f),
        label = "largeTitleAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(Color.White)
            .padding(bottom = 24.dp)
            .zIndex(1f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .alpha(largeTitleAlpha)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    fontSize = titleSize.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black,
                    letterSpacing = (-0.5).sp
                )

                CacheImage(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = {
                                navController.navigate("profile")
                            }
                        ),
                    imageUrl = "https://github.com/evilrabbit.png"
                )

//                when (currentMe) {
//                    is Resource.Loading -> {
//                        Box(
//                            modifier = Modifier
//                                .size(48.dp)
//                                .clip(CircleShape)
//                                .shimmerEffect()
//                        )
//                    }
//
//                    is Resource.Success -> {
//                        CacheImage(
//                            modifier = Modifier
//                                .size(48.dp)
//                                .clip(CircleShape)
//                                .clickable(
//                                    interactionSource = interactionSource,
//                                    indication = null,
//                                    onClick = {
//                                        navController.navigate("profile")
//                                    }
//                                ),
//                            imageUrl = currentMe.data?.images?.get(0)?.url ?: ""
//                        )
//                    }
//
//                    is Resource.Error -> {
//                        Box(
//                            modifier = Modifier
//                                .size(48.dp)
//                                .clip(CircleShape)
//                                .background(Color.Gray.copy(alpha = 0.5f))
//                        )
//                    }
//                }
            }
        }
    }
}