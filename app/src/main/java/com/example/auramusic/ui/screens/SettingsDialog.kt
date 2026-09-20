package com.example.auramusic.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.auramusic.viewmodel.MusicViewModel

@Composable
fun SettingsDialog(
    viewModel: MusicViewModel,
    onDismiss: () -> Unit,
    sleepTimerRemaining: Int? = null,
    onSetSleepTimer: (Int) -> Unit = {},
    onCancelSleepTimer: () -> Unit = {},
    onShowWelcome: () -> Unit = {}
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            SettingsScreen(
                viewModel = viewModel,
                onBack = onDismiss
            )
        }
    }
}
