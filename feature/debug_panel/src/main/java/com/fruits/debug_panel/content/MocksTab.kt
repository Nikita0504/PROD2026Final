package com.fruits.debug_panel.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fruits.debug_panel.DebugPanelViewModel

@Composable
fun MocksTab(vm: DebugPanelViewModel, mockEnabled: Boolean) {
    val userMockState by vm.userMockState.collectAsStateWithLifecycle()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Главный тоггл
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Моки включены", style = MaterialTheme.typography.titleMedium)
            Switch(checked = mockEnabled, onCheckedChange = vm::toggleMocks)
        }

        HorizontalDivider()

        // Блок для каждой модели — изолирован
        UserMockBlock(
            state = userMockState,
            enabled = mockEnabled,
            onStateChange = vm::updateUserMockState,
            onSave = vm::saveUserMock
        )

        // Сюда добавляешь следующие блоки по аналогии
        // ProductsMockBlock(...)
    }
}