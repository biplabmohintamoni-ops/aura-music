package com.example.auramusic

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.auramusic.ui.navigation.AuraApp
import com.example.auramusic.ui.theme.AuraMusicTheme
import com.example.auramusic.ui.viewmodel.MusicViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        enableEdgeToEdge()
        setContent {
            val viewModel: MusicViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsState()
            val dynamicAccent by viewModel.dynamicAccentColor.collectAsState()
            val blurIntensity by viewModel.blurIntensity.collectAsState()

            AuraMusicTheme(
                themeMode = themeMode,
                dynamicAccentColor = dynamicAccent,
                blurIntensity = blurIntensity
            ) {
                AuraApp(viewModel = viewModel)
            }
        }
    }
}
