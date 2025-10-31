package com.example.tasknotifier.domain.model

enum class NotificationFrequency(val minutes: Int) {
    EVERY_30_MIN(30),
    EVERY_1_HOUR(60),
    EVERY_2_HOURS(120),
    EVERY_3_HOURS(180),
    EVERY_6_HOURS(360),
    EVERY_9_HOURS(540)
}