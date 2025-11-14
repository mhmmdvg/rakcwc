package com.rakcwc.presentation

sealed class Screen(val route: String) {
    object BottomNav : Screen("bottom_nav")
    object Authentication : Screen("authentication")
    object Home : Screen("home")
    object Search : Screen("search")
    object Setting : Screen("setting")
    object CreateCatalog : Screen("create_catalog")
    object EditCatalog : Screen("edit_catalog/{id}") {
        fun createRoute(id: String) = "edit_catalog/$id"
    }

    object CatalogDetail : Screen("catalog/{id}") {
        fun createRoute(id: String) = "catalog/$id"
    }

    object SettingManagement : Screen("setting/{management}") {
        fun createRoute(management: String) = "setting/$management"
    }
}