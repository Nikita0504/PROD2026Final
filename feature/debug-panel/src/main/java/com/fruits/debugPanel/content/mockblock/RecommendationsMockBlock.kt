package com.fruits.debugPanel.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fruits.debugPanel.RecommendationMockEditState

@Composable
fun RecommendationsMockBlock(
    states: List<RecommendationMockEditState>,
    enabled: Boolean,
    onStateChange: (RecommendationMockEditState) -> Unit,

    onSave: () -> Unit
) {
    var saved by remember { mutableStateOf(false) }

    MockEditBlock(
        title = "Recommendations (${states.size})",
        enabled = enabled,
        saved = saved,
        onSave = { onSave(); saved = true }
    ) {
        states.forEachIndexed { index, state ->
            RecommendationItemBlock(
                index = index + 1,
                state = state,
                enabled = enabled,
                onStateChange = {
                    onStateChange(it)
                    saved = false
                }
            )
            if (index < states.lastIndex) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 0.5.dp
                )
            }
        }
    }
}

@Composable
private fun RecommendationItemBlock(
    index: Int,
    state: RecommendationMockEditState,
    enabled: Boolean,
    onStateChange: (RecommendationMockEditState) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

        // Заголовок элемента
        Text(
            text = "№$index  ${state.authorName}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )

        // Описание — многострочное
        OutlinedTextField(
            value = state.description,
            onValueChange = { onStateChange(state.copy(description = it)) },
            label = { Text("description") },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            maxLines = 3,
            minLines = 2
        )

        // Лайки + переключатель в одной строке, компактно
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = state.likesCount,
                onValueChange = { onStateChange(state.copy(likesCount = it)) },
                label = { Text("likes") },
                modifier = Modifier.width(100.dp), // фиксированная компактная ширина
                enabled = enabled,
                singleLine = true
            )

            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "isLiked",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enabled)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.outline
                )
                Switch(
                    checked = state.isLiked,
                    onCheckedChange = { onStateChange(state.copy(isLiked = it)) },
                    enabled = enabled
                )
            }
        }
    }
}
