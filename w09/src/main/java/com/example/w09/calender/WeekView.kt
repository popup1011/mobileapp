
package com.example.w09.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale

/**
 * ✅ 주간 뷰 (확실히 보이도록 단순화한 버전)
 * - baseDate가 속한 주의 7일을 크게 보여줌
 * - 헤더(요일)는 firstDayOfWeek 기준으로 정렬
 * - 각 날짜 박스 하단에 이벤트 바(최대 3개) 표시
 */
@Composable
fun WeekView(
    baseDate: LocalDate,
    firstDayOfWeek: DayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek,
    eventsByDate: Map<LocalDate, List<CalendarEvent>> = emptyMap(),
    onDayClick: ((LocalDate) -> Unit)? = null
) {
    // 주 시작/7일 계산
    val weekDates = remember(baseDate, firstDayOfWeek) {
        computeWeekDates(baseDate, firstDayOfWeek)
    }

    // 헤더 요일 순서도 firstDayOfWeek 기준으로 맞춤
    val headerOrder = remember(firstDayOfWeek) {
        (0 until 7).map { DayOfWeek.of(((firstDayOfWeek.value - 1 + it) % 7) + 1) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // 요일 헤더 (큰 글씨로 확실히 표시)
        Row(
            modifier = Modifier.padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            headerOrder.forEach { dow ->
                Text(
                    text = koreanDowLabel(dow),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 날짜 7칸 (큰 박스로 확실히 보이도록)
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            weekDates.forEach { date ->
                val events = eventsByDate[date].orEmpty()
                WeekDayBox(
                    modifier = Modifier.weight(1f), // ⬅️ weight는 호출부(RowScope)에서 적용
                    date = date,
                    eventColors = events.take(3).map { Color(it.colorHex) },
                    onClick = onDayClick
                )
            }
        }
    }
}

@Composable
private fun WeekDayBox(
    modifier: Modifier = Modifier,
    date: LocalDate,
    eventColors: List<Color>,
    onClick: ((LocalDate) -> Unit)?
) {
    val clickable = if (onClick != null) Modifier.clickable { onClick(date) } else Modifier

    Box(
        modifier = modifier
            .semantics { contentDescription = "${date.monthValue}월 ${date.dayOfMonth}일" }
            .then(clickable)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.14f))
            .padding(8.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        // 날짜 텍스트
        Text(
            text = "${date.monthValue}/${date.dayOfMonth}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // 이벤트 바들 (하단에 굵직하게)
        if (eventColors.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                eventColors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(width = 24.dp, height = 6.dp)
                            .background(color)
                    )
                }
            }
        }
    }
}

private fun computeWeekDates(
    baseDate: LocalDate,
    firstDayOfWeek: DayOfWeek
): List<LocalDate> {
    val offset = ((baseDate.dayOfWeek.value - firstDayOfWeek.value) + 7) % 7
    val start = baseDate.minusDays(offset.toLong())
    return (0..6).map { start.plusDays(it.toLong()) }
}

private fun koreanDowLabel(dow: DayOfWeek): String = when (dow) {
    DayOfWeek.MONDAY -> "월"
    DayOfWeek.TUESDAY -> "화"
    DayOfWeek.WEDNESDAY -> "수"
    DayOfWeek.THURSDAY -> "목"
    DayOfWeek.FRIDAY -> "금"
    DayOfWeek.SATURDAY -> "토"
    DayOfWeek.SUNDAY -> "일" }
