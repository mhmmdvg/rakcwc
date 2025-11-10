package com.rakcwc.presentation.ui.screens.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.rakcwc.data.remote.local.TokenManager
import com.rakcwc.presentation.Screen
import com.rakcwc.presentation.ui.theme.AccentColor
import com.rakcwc.presentation.ui.theme.PrimaryColor

@Composable
fun SplashScreen(
    tokenManager: TokenManager,
    navController: NavController
) {
    LaunchedEffect(Unit) {
        val token = tokenManager.getToken()
        val destination = if (!token.isNullOrEmpty()) "home" else "authentication"

        navController.navigate(destination) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
            .background(
                color = AccentColor
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Mayora",
            fontSize = 24.sp,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = PrimaryColor,
            textAlign = TextAlign.Start,
        )
    }
}