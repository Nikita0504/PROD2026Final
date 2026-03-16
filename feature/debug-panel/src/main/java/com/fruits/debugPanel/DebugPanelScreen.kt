package com.fruits.debugPanel

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fruits.debugPanel.content.LogsTab
import com.fruits.debugPanel.content.MocksTab
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugPanelScreen(
    onClose: (() -> Unit)? = null,
    vm: DebugPanelViewModel = koinViewModel()
) {
    val mockEnabled by vm.mockEnabled.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Debug Panel") },
                navigationIcon = {
                    if (onClose != null) {
                        IconButton(onClick = onClose) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Закрыть"
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text("Логи", Modifier.padding(vertical = 12.dp))
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text("Моки", Modifier.padding(vertical = 12.dp))
                }
            }
            when (selectedTab) {
                0 -> {
                    // Подписка на логи только когда активна вкладка — без этого рекомпозиция
                    // происходила бы на каждый лог даже при открытой вкладке "Моки"
                    val logs by vm.logs.collectAsStateWithLifecycle()
                    LogsTab(logs = logs, onClear = vm::clearLogs)
                }
                1 -> MocksTab(vm = vm, mockEnabled = mockEnabled)
            }
        }
    }
}