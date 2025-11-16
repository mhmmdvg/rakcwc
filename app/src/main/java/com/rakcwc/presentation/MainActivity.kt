package com.rakcwc.presentation

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.rakcwc.data.remote.local.TokenManager
import com.rakcwc.presentation.ui.theme.RAKCWCTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                scrim = android.graphics.Color.WHITE, // Light background
                darkScrim = android.graphics.Color.WHITE
            ),
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = android.graphics.Color.WHITE,
                darkScrim = android.graphics.Color.BLACK,
            )
        )

        setContent {
            RAKCWCTheme {
                App(
                    tokenManager = tokenManager
                )
            }
        }
    }
}