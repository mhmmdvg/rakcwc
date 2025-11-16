package com.rakcwc.presentation.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.rakcwc.presentation.Screen
import com.rakcwc.presentation.ui.screens.settings.components.GuestProfileSection
import com.rakcwc.presentation.ui.screens.settings.components.ProfileSection
import com.rakcwc.presentation.ui.screens.settings.components.SettingsMenuItem
import com.rakcwc.presentation.ui.screens.settings.components.SettingsSectionHeader

@Composable
fun SettingScreen(
    settingVm: SettingsViewModel = hiltViewModel(),
    navController: NavController,
    isAuthenticated: Boolean = false
) {
    val settingState by settingVm.settings.collectAsState()

    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            settingVm.getProfile()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(top = 86.dp)
    ) {
        // Profile Section - Different UI for authenticated vs guest
        if (isAuthenticated) {
            ProfileSection(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                data = settingState
            )

            // Settings Menu Items - Only show for authenticated users
            Column(modifier = Modifier.padding(top = 16.dp)) {
                SettingsSectionHeader(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    title = "Management"
                )
                SettingsMenuItem(
                    title = "Catalog Management",
                    onClick = { navController.navigate("setting/catalog-management") }
                )

                SettingsMenuItem(
                    title = "Product Management",
                    onClick = { navController.navigate("setting/product-management") }
                )
            }

            // Logout Section
            SettingsSectionHeader(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                title = "Account"
            )

            Column {
                SettingsMenuItem(
                    title = "Log out",
                    onClick = {
                        settingVm.signOut()
                        navController.navigate(Screen.BottomNav.route) {
                            popUpTo(Screen.BottomNav.route) { inclusive = true }
                        }
                    },
                    showDivider = false
                )
            }
        } else {
            // Guest User Section
            GuestProfileSection(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                onLoginClick = {
                    navController.navigate(Screen.Authentication.route)
                }
            )
        }

        // Add bottom padding for scroll
        Column(modifier = Modifier.padding(bottom = 32.dp)) { }
    }
}