package com.example.tasknotifier.domain.model

enum class TaskPriority(val displayName: String, val colorRes: String) {
    HIGH("Высокий", "#FF4444"),
    MEDIUM("Средний", "#FFBB33"),
    LOW("Низкий", "#99CC00")
}