package com.fruits.debugPanel.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fruits.debug.DebugLogStorage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LogsTab(logs: List<DebugLogStorage.LogEntry>, onClear: () -> Unit) {
    val listState = rememberLazyListState()

    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) listState.animateScrollToItem(logs.lastIndex)
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Записей: ${logs.size}", style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = onClear) { Text("Очистить") }
        }

        if (logs.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Логов пока нет", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                items(items = logs, key = { it.id }) { entry ->
                    LogEntryRow(entry)
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun LogEntryRow(entry: DebugLogStorage.LogEntry) {
    val time = remember(entry.timestamp) {
        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(entry.timestamp))
    }
    val tagColor = when {
        entry.tag.contains("ERROR", ignoreCase = true) -> MaterialTheme.colorScheme.error
        entry.tag.contains("HTTP", ignoreCase = true) -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.secondary
    }
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(time, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline, modifier = Modifier.width(64.dp))
        Text(entry.tag, style = MaterialTheme.typography.labelSmall,
            color = tagColor, modifier = Modifier.width(80.dp))
        Text(entry.message, style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f))
    }
}
