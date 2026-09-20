package com.example.auramusic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.auramusic.data.provider.OwnerConfig
import com.example.auramusic.ui.theme.AuraBackground
import com.example.auramusic.ui.theme.AuraBorder
import com.example.auramusic.ui.theme.AuraPrimary
import com.example.auramusic.ui.theme.AuraSurfaceElevated
import com.example.auramusic.ui.theme.AuraTextMuted
import com.example.auramusic.ui.theme.AuraTextPrimary
import com.example.auramusic.ui.theme.AuraTextSecondary

@Composable
fun WelcomeDialog(
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
                .padding(horizontal = 28.dp, vertical = 32.dp)
                .testTag("welcome_dialog"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Minimalist Music Icon badge
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(AuraBackground)
                        .border(1.dp, AuraBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Aura Music Logo",
                        tint = AuraPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = OwnerConfig.APP_NAME,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = AuraTextPrimary,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "WELCOME TO ${OwnerConfig.APP_NAME}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AuraTextSecondary,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Creator & Instagram Attribution Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(AuraBackground)
                        .border(1.dp, AuraBorder, RoundedCornerShape(16.dp))
                        .padding(vertical = 16.dp, horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = OwnerConfig.CREATOR_NAME,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = AuraTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = OwnerConfig.CREATOR_INSTAGRAM,
                        fontSize = 13.sp,
                        color = AuraPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("welcome_continue_button"),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AuraTextPrimary,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = "Continue",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
