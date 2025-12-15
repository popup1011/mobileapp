
package com.example.w09.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.w09.calender.DayCellModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

/**
 * 월 달력 화면:
 * - 요일 헤더(월~일)
 * - 7xN 날짜 그리드
 * - 날짜 클릭 → 일정 추가 다이얼로그
 * - 일정이 있는 날짜: 하단 컬러 바 표시
 */
@Composable
fun MonthView(
    yearMonth: YearMonth,
    firstDayOfWeek: DayOfWeek,
    eventsByDate: Map<LocalDate, List<CalendarEvent>>,
    onAddEvent: (date: LocalDate, title: String, colorHex: Long) -> Unit
) {
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val cells = remember(yearMonth, firstDayOfWeek) {
        buildMonthCells(yearMonth, firstDayOfWeek)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 요일 헤더 (월~일)
        val daysOfWeek = listOf(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY,
            DayOfWeek.SUNDAY
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp)
        ) {
            // 헤더 7칸
            items(daysOfWeek.size) { idx ->
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .aspectRatio(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (daysOfWeek[idx]) {
                            DayOfWeek.MONDAY -> "월"
                            DayOfWeek.TUESDAY -> "화"
                            DayOfWeek.WEDNESDAY -> "수"
                            DayOfWeek.THURSDAY -> "목"
                            DayOfWeek.FRIDAY -> "금"
                            DayOfWeek.SATURDAY -> "토"
                            DayOfWeek.SUNDAY -> "일"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 날짜 셀들
            items(cells) { cell ->
                val events = eventsByDate[cell.date].orEmpty()
                DayCell(
                    cell = cell,
                    isSelected = selectedDate == cell.date,
                    eventColorHexes = events.map { it.colorHex },
                    onClick = {
                        selectedDate = cell.date
                        showAddDialog = true
                    }
                )
            }
        }
    }

    // 일정 추가 다이얼로그
    if (showAddDialog && selectedDate != null) {
        AddEventDialog(
            date = selectedDate!!,
            onDismiss = { showAddDialog = false },
            onAdd = { title, colorHex ->
                onAddEvent(selectedDate!!, title, colorHex)
                showAddDialog = false
            }
        )
    }
}

/**
 * YearMonth를 7xN 그리드로 채우기 위해 앞/뒤 공백 포함 셀 생성
 */
fun buildMonthCells(
    yearMonth: YearMonth,
    firstDayOfWeek: DayOfWeek
): List<DayCellModel> {
    val firstOfMonth = yearMonth.atDay(1)
    val lastOfMonth = yearMonth.atEndOfMonth()

    val leading = ((firstOfMonth.dayOfWeek.value - firstDayOfWeek.value) + 7) % 7
    val cells = mutableListOf<DayCellModel>()

    // 앞쪽: 이전 달로 채우기
    var cursor = firstOfMonth.minusDays(leading.toLong())
    repeat(leading) {
        cells.add(DayCellModel(cursor, false))
        cursor = cursor.plusDays(1)
    }

    // 이번 달
    var inMonth = firstOfMonth
    while (!inMonth.isAfter(lastOfMonth)) {
        cells.add(DayCellModel(inMonth, true))
        inMonth = inMonth.plusDays(1)
    }

    // 뒤쪽: 7로 나눠떨어지도록 채우기
    val remainder = (7 - (cells.size % 7)) % 7
    var tail = lastOfMonth.plusDays(1)
    repeat(remainder) {
        cells.add(DayCellModel(tail, false))
        tail = tail.plusDays(1)
    }

    return cells
}

/**
 * 단일 날짜 셀:
 * - 선택/오늘 강조
 * - 하단 컬러 바(일정 표시) 렌더링
 */
@Composable
fun DayCell(
    cell: DayCellModel,
    isSelected: Boolean,
    eventColorHexes: List<Long>,
    onClick: () -> Unit
) {
    val today = LocalDate.now()
    val isToday = cell.date == today

    Box(
        modifier = Modifier
            .padding(4.dp)
            .aspectRatio(1f)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.10f)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // 날짜 숫자
        Text(
            text = cell.date.dayOfMonth.toString(),
            color = when {
                isSelected -> MaterialTheme.colorScheme.primary
                isToday -> MaterialTheme.colorScheme.secondary
                cell.isInCurrentMonth -> MaterialTheme.colorScheme.onSurface
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            style = MaterialTheme.typography.bodyMedium
        )

        // 하단 컬러 바 표시 (최대 3개)
        val bars = eventColorHexes.take(3).map { Color(it) }
        if (bars.isNotEmpty()) {
            // 바들이 겹치지 않도록 여백을 두고 얇은 바를 그립니다.
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp)
            ) {
                // 한 줄에 여러 바를 나란히 표시
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    bars.forEach { barColor ->
                        Box(
                            modifier = Modifier
                                .height(4.dp)
                                .width(18.dp)
                                .background(barColor)
                        )
                    }
                }
            }
        }
    }
}