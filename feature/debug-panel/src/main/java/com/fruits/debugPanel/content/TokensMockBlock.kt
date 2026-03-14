package com.fruits.debugPanel.content

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.fruits.debugPanel.TokensMockEditState

@Composable
fun TokensMockBlock(
    state: TokensMockEditState,
    enabled: Boolean,
    onStateChange: (TokensMockEditState) -> Unit,
    onSave: () -> Unit
) {
    var saved by remember { mutableStateOf(false) }

    MockEditBlock(title = "Tokens", enabled = enabled, saved = saved, onSave = {
        onSave(); saved = true
    }) {
        OutlinedTextField(
            value = state.accessToken,
            onValueChange = { onStateChange(state.copy(accessToken = it)); saved = false },
            label = { Text("accessToken") },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = true
        )
        OutlinedTextField(
            value = state.refreshToken,
            onValueChange = { onStateChange(state.copy(refreshToken = it)); saved = false },
            label = { Text("refreshToken") },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = true
        )
    }
}
