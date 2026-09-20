package com.example.auramusic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.auramusic.ui.theme.AuraBackground
import com.example.auramusic.ui.theme.AuraBorder
import com.example.auramusic.ui.theme.AuraPrimary
import com.example.auramusic.ui.theme.AuraSurfaceElevated
import com.example.auramusic.ui.theme.AuraTextPrimary
import com.example.auramusic.ui.theme.AuraTextSecondary

@Composable
fun QuickMenuSheet(
    onOpenRecognize: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(24.dp))
                .background(AuraSurfaceElevated)
                .border(1.dp, AuraBorder, RoundedCornerShape(24.dp))
                .padding(20.dp)
                .testTag("quick_menu_sheet")
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Quick Actions",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AuraTextPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                )

                QuickMenuItem(
                    title = "Recognize Music (Echo Find)",
                    subtitle = "Identify what's playing around you",
                    icon = Icons.Default.Mic,
                    onClick = {
                        onDismiss()
                        onOpenRecognize()
                    }
                )

                QuickMenuItem(
                    title = "Graphic Equalizer",
                    subtitle = "DSP 5-band sound enhancement",
                    icon = Icons.Default.Equalizer,
                    onClick = {
                        onDismiss()
                        onOpenEqualizer()
                    }
                )

                QuickMenuItem(
                    title = "Sleep Timer",
                    subtitle = "Auto fade out music",
                    icon = Icons.Default.NightsStay,
                    onClick = {
                        onDismiss()
                        onOpenSleepTimer()
                    }
                )

                QuickMenuItem(
                    title = "Google Account",
                    subtitle = "Manage login and cloud sync",
                    icon = Icons.Default.Person,
                    onClick = {
                        onDismiss()
                        onOpenProfile()
                    }
                )

                QuickMenuItem(
                    title = "Settings & About",
                    subtitle = "AURA MUSIC • MADE BY K98",
                    icon = Icons.Default.Settings,
                    onClick = {
                        onDismiss()
                        onOpenSettings()
                    }
                )
            }
        }
    }
}

@Composable
private fun QuickMenuItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AuraBackground)
            .border(1.dp, AuraBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AuraPrimary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = AuraTextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = AuraTextSecondary
            )
        }
    }
}
