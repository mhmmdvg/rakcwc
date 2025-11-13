package com.rakcwc.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = android.graphics.Color.WHITE,
                darkScrim = android.graphics.Color.BLACK,
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