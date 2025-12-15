
package com.example.w09.calendar

import java.time.LocalDate

/**
 * 일정 데이터 모델 (간단 버전)
 * - colorHex: ARGB HEX (예: 0xFF64B5F6)
 */
data class CalendarEvent(
    val id: Long,
    val date: LocalDate,
    val title: String,
    val colorHex: Long
)
