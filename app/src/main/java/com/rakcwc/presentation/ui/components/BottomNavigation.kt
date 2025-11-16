package com.rakcwc.presentation.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.rakcwc.presentation.ui.theme.AccentColor
import com.rakcwc.utils.NavigationConfig
import kotlinx.coroutines.launch

@Composable
fun BottomNavigation(
    navController: NavController
) {
    val selectedNavigationIndex = rememberSaveable { mutableIntStateOf(0) }
    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

    // Observe current route to sync selected index
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Update selected index based on current route
    LaunchedEffect(currentRoute) {
        val index = NavigationConfig.items.indexOfFirst { it.screen == currentRoute }
        if (index != -1) {
            selectedNavigationIndex.intValue = index
        }
    }

    NavigationBar(
        modifier = Modifier
            .drawBehind {
                val strokeWidth = 1.dp.toPx()
                drawLine(
                    color = borderColor,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = strokeWidth
                )
            }
            .height(64.dp),
        containerColor = Color.White,
        contentColor = Color.Black
    ) {
        NavigationConfig.items.forEachIndexed { index, it ->
            val interactionSource = remember { MutableInteractionSource() }
            val scale = remember { Animatable(1f) }
            val scope = rememberCoroutineScope()

            NavigationBarItem(
                modifier = Modifier.size(20.dp),
                interactionSource = interactionSource,
                selected = selectedNavigationIndex.intValue == index,
                onClick = {
                    // Only navigate if not already on this screen
                    if (selectedNavigationIndex.intValue != index) {
                        selectedNavigationIndex.intValue = index
                        navController.navigate(it.screen) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination when
                            // reselecting the same item
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }

                    scope.launch {
                        scale.animateTo(
                            targetValue = 0.88f,
                            animationSpec = tween(
                                durationMillis = 100,
                                easing = LinearOutSlowInEasing
                            )
                        )
                        scale.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(durationMillis = 100, easing = LinearOutSlowInEasing)
                        )
                    }
                },
                icon = {
                    Icon(
                        modifier = Modifier.scale(scale.value),
                        imageVector = it.icon,
                        contentDescription = it.title,
                        tint = if (selectedNavigationIndex.intValue == index) AccentColor else Color.Gray.copy(alpha = 0.8f)
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AccentColor,
                    unselectedIconColor = Color.Gray.copy(alpha = 0.8f),
                    indicatorColor = Color.Transparent,
                    selectedTextColor = Color.Transparent,
                    unselectedTextColor = Color.Transparent
                )
            )
        }
    }
}