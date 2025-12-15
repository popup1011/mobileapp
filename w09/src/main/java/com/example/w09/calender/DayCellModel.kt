package com.example.w09.calender

import java.time.LocalDate

/**
 * 달력 셀 모델 (분리 파일 버전).
 * MonthView.kt에 포함해도 무방합니다.
 */
data class DayCellModel(
    val date: LocalDate,
    val isInCurrentMonth: Boolean
)
