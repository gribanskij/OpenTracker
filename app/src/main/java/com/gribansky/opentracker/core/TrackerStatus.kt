package com.gribansky.opentracker.core

import androidx.compose.ui.graphics.Color

enum class TrackerStatus(val label: String,val color: Color) {
    ACTIVE("Трекер запущен",Color.Green),
    WAITING("Трекер ожидает запуск",Color.Yellow),
    INACTIVE("Трекер остановлен",Color.Red)
}