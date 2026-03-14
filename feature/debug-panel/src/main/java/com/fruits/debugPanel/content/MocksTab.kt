package com.fruits.debugPanel.content

import com.fruits.debugPanel.DebugPanelViewModel

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
import com.fruits.debugPanel.content.mockblock.TokensMockBlock
import com.fruits.debugPanel.content.mockblock.UserMockBlock

@Composable
fun MocksTab(vm: DebugPanelViewModel, mockEnabled: Boolean) {
    val userState by vm.userState.collectAsStateWithLifecycle()
    val tokensState by vm.tokensState.collectAsStateWithLifecycle()
    val recommendationsState by vm.recommendationsState.collectAsStateWithLifecycle()


    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Моки включены", style = MaterialTheme.typography.titleMedium)
            Switch(checked = mockEnabled, onCheckedChange = vm::toggleMocks)
        }

        HorizontalDivider()

        UserMockBlock(
            state = userState,
            enabled = mockEnabled,
            onStateChange = vm::updateUserState,
            onSave = vm::saveUserMock
        )

        TokensMockBlock(
            state = tokensState,
            enabled = mockEnabled,
            onStateChange = vm::updateTokensState,
            onSave = vm::saveTokensMock
        )


        RecommendationsMockBlock(
            states = recommendationsState,
            enabled = mockEnabled,
            onStateChange = vm::updateRecommendationState,
            onSave = vm::saveRecommendationsMock
        )
    }
}
