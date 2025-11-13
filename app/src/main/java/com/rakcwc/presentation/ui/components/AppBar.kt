package com.rakcwc.presentation.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.Lucide
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    modifier: Modifier = Modifier,
    title: String,
    onBackPressed: (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null,
    scrollOffset: Int = 0,
    maxOffset: Int = 200
) {
    val collapseProgress = min(1f, max(0f, scrollOffset.toFloat() / maxOffset))

    val smallTitleAlpha by animateFloatAsState(
        targetValue = when {
            collapseProgress < 0.4f -> 0f
            collapseProgress < 0.8f -> (collapseProgress - 0.4f) / 0.4f * 0.6f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "smallTitleAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(64.dp)
            .alpha(smallTitleAlpha)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White,
                        Color.White.copy(alpha = 0.6f),
                        Color.White.copy(alpha = 0.4f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
            .zIndex(1f)
    ) {
        // Centered title
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = Color.Black,
                letterSpacing = (-0.3).sp
            )
        }

        // Back button on the left
        onBackPressed?.let {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                IconButton(
                    onClick = it,
                    modifier = Modifier
                        .size(44.dp)
                        .padding(start = 4.dp)
                ) {
                    Icon(
                        imageVector = Lucide.ChevronLeft,
                        contentDescription = "Back",
                        modifier = Modifier
                            .size(24.dp)
                            .alpha(smallTitleAlpha * 0.8f + 0.2f),
                        tint = Color.Black
                    )
                }
            }
        }

        // Trailing actions on the right
        actions?.let {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                it()
            }
        }
    }
}