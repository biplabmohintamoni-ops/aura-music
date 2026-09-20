package com.example.auramusic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.auramusic.ui.theme.AuraBackground
import com.example.auramusic.ui.theme.AuraSurface

@Composable
fun AuraBackground(
    modifier: Modifier = Modifier,
    primaryColor: Color = Color.Transparent,
    secondaryColor: Color = Color.Transparent,
    isEnabled: Boolean = false
) {
    // Clean, subtle premium dark charcoal backdrop
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        AuraSurface.copy(alpha = 0.85f),
                        AuraBackground,
                        AuraBackground
                    )
                )
            )
    )
}
