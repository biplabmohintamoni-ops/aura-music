package com.example.auramusic.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auramusic.ui.theme.AuraAccentPill
import com.example.auramusic.ui.theme.LocalAuraTheme
import com.example.auramusic.ui.theme.LocalDynamicAccent

enum class AuraNavDestination {
    HOME,
    SEARCH,
    LIBRARY
}

@Composable
fun AuraBottomNav(
    currentDestination: AuraNavDestination,
    onNavigate: (AuraNavDestination) -> Unit,
    onOpenQuickMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalAuraTheme.current
    val accent = LocalDynamicAccent.current

    val barBg = if (theme.isGlass) Color(0x601E1E2C) else theme.capsuleNav
    val barBorder = if (theme.isGlass) Color(0x40FFFFFF) else theme.border

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .testTag("aura_bottom_nav"),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Main floating capsule bar
        Box(
            modifier = Modifier
                .height(56.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(barBg)
                .border(1.dp, barBorder, RoundedCornerShape(28.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                NavPillItem(
                    label = "Home",
                    icon = if (currentDestination == AuraNavDestination.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                    isSelected = currentDestination == AuraNavDestination.HOME,
                    onClick = { onNavigate(AuraNavDestination.HOME) },
                    testTag = "nav_home"
                )

                NavPillItem(
                    label = "Search",
                    icon = if (currentDestination == AuraNavDestination.SEARCH) Icons.Filled.Search else Icons.Outlined.Search,
                    isSelected = currentDestination == AuraNavDestination.SEARCH,
                    onClick = { onNavigate(AuraNavDestination.SEARCH) },
                    testTag = "nav_search"
                )

                NavPillItem(
                    label = "Library",
                    icon = if (currentDestination == AuraNavDestination.LIBRARY) Icons.Default.MusicNote else Icons.Outlined.LibraryMusic,
                    isSelected = currentDestination == AuraNavDestination.LIBRARY,
                    onClick = { onNavigate(AuraNavDestination.LIBRARY) },
                    testTag = "nav_library"
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Circular 3-dots Quick Action Button (Right side of capsule)
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(barBg)
                .border(1.dp, barBorder, CircleShape)
                .clickable { onOpenQuickMenu() }
                .testTag("nav_quick_menu"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MoreHoriz,
                contentDescription = "Quick Menu",
                tint = theme.textPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun NavPillItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val theme = LocalAuraTheme.current
    val accent = LocalDynamicAccent.current

    val targetBg = if (isSelected) {
        if (theme.isGlass) accent.copy(alpha = 0.35f)
        else if (theme.background.red > 0.8f) accent.copy(alpha = 0.15f)
        else AuraAccentPill
    } else Color.Transparent

    val targetContent = if (isSelected) {
        if (theme.isGlass) Color.White
        else if (theme.background.red > 0.8f) accent
        else Color.Black
    } else theme.textSecondary

    val bgAnim by animateColorAsState(targetValue = targetBg, label = "nav_bg")
    val contentColorAnim by animateColorAsState(targetValue = targetContent, label = "nav_content")

    Box(
        modifier = Modifier
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(bgAnim)
            .clickable { onClick() }
            .padding(horizontal = if (isSelected) 16.dp else 14.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColorAnim,
                modifier = Modifier.size(20.dp)
            )

            if (isSelected) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColorAnim
                )
            }
        }
    }
}
