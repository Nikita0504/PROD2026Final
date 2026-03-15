package com.fruits.debugPanel.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

        Text(
            text = "№$index  ${state.firstName} ${state.secondName}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = state.firstName,
                onValueChange = { onStateChange(state.copy(firstName = it)) },
                label = { Text("firstName") },
                modifier = Modifier.weight(1f),
                enabled = enabled,
                singleLine = true
            )
            OutlinedTextField(
                value = state.secondName,
                onValueChange = { onStateChange(state.copy(secondName = it)) },
                label = { Text("secondName") },
                modifier = Modifier.weight(1f),
                enabled = enabled,
                singleLine = true
            )
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = state.age,
                onValueChange = { onStateChange(state.copy(age = it)) },
                label = { Text("age") },
                modifier = Modifier.width(80.dp),
                enabled = enabled,
                singleLine = true
            )
            OutlinedTextField(
                value = state.city,
                onValueChange = { onStateChange(state.copy(city = it)) },
                label = { Text("city") },
                modifier = Modifier.weight(1f),
                enabled = enabled,
                singleLine = true
            )
        }

        OutlinedTextField(
            value = state.description,
            onValueChange = { onStateChange(state.copy(description = it)) },
            label = { Text("description") },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            maxLines = 3,
            minLines = 2
        )

        OutlinedTextField(
            value = state.explanation,
            onValueChange = { onStateChange(state.copy(explanation = it)) },
            label = { Text("explanation (по строкам)") },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            maxLines = 3,
            minLines = 1
        )
    }
}
