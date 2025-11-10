package com.rakcwc.utils

import com.composables.icons.lucide.House
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.Settings
import com.rakcwc.domain.models.NavigationItem
import com.rakcwc.presentation.Screen

object NavigationConfig {
    val items = listOf(
        NavigationItem(
            title = "Home",
            icon = Lucide.House,
            screen = Screen.Home.route
        ),
        NavigationItem(
            title = "Search",
            icon = Lucide.Search,
            screen = Screen.Search.route
        ),
        NavigationItem(
            title = "Setting",
            icon = Lucide.Settings,
            screen = Screen.Setting.route
        )
    )
}