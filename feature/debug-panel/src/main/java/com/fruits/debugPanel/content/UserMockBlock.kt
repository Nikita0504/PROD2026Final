package com.fruits.debugPanel.content

import com.fruits.debugPanel.UserMockEditState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier


@Composable
fun UserMockBlock(
    state: UserMockEditState,
    enabled: Boolean,
    onStateChange: (UserMockEditState) -> Unit,
    onSave: () -> Unit
) {
    var saved by remember { mutableStateOf(false) }

    MockEditBlock(
        title = "User",
        enabled = enabled,
        onSave = {
            onSave()
            saved = true
        },
        saved = saved,
    ) {
        OutlinedTextField(
            value = state.firstName,
            onValueChange = { onStateChange(state.copy(firstName = it)); saved = false },
            label = { Text("firstName") },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = true
        )
        OutlinedTextField(
            value = state.secondName,
            onValueChange = { onStateChange(state.copy(secondName = it)); saved = false },
            label = { Text("secondName") },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = true
        )
        OutlinedTextField(
            value = state.email,
            onValueChange = { onStateChange(state.copy(email = it)); saved = false },
            label = { Text("email") },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = true
        )
    }
}
