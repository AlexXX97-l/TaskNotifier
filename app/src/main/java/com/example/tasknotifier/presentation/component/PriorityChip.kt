package com.example.tasknotifier.presentation.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.tasknotifier.domain.model.TaskPriority

@Composable
fun PriorityChip(priority: String) {
    val (text, color) = when (priority) {
        TaskPriority.HIGH.name -> "Высокий" to Color(0xFFFF4444)
        TaskPriority.MEDIUM.name -> "Средний" to Color(0xFFFFBB33)
        else -> "Низкий" to Color(0xFF99CC00)
    }

    Surface(
        color = color.copy(alpha = 0.2f),
        shape = MaterialTheme.shapes.small,
        border = ButtonDefaults.outlinedButtonBorder.copy(
            width = 1.dp
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = androidx.compose.ui.Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}