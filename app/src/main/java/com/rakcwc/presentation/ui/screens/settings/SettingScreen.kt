package com.rakcwc.presentation.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.rakcwc.presentation.ui.screens.settings.components.ProfileSection
import com.rakcwc.presentation.ui.screens.settings.components.SettingsMenuItem
import com.rakcwc.presentation.ui.screens.settings.components.SettingsSectionHeader

@Composable
fun SettingScreen(
    settingVm: SettingsViewModel = hiltViewModel(),
    navController: NavController,
) {
    val settingState by settingVm.settings.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(top = 86.dp)
            .padding(horizontal = 20.dp)
    ) {
        // Profile Section
        ProfileSection(
            data = settingState
        )

        // Settings Menu Items
        Column(modifier = Modifier.padding(top = 16.dp)) {
            SettingsSectionHeader(title = "Settings")
            SettingsMenuItem(
                title = "Catalog Management",
                onClick = { navController.navigate("setting/catalog-management") }
            )

            SettingsMenuItem(
                title = "Product Management",
                onClick = { navController.navigate("setting/product-management") }
            )
        }

        // Login Section
        SettingsSectionHeader(title = "Login")

        Column {
            SettingsMenuItem(
                title = "Log out",
                onClick = { /* Navigate to Security */ },
                showDivider = false
            )
        }

        // Add bottom padding for scroll
        Column(modifier = Modifier.padding(bottom = 32.dp)) { }
    }
}