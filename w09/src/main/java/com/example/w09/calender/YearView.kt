
package com.example.w09.calendar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate

/**
 * 연간 뷰:
 * - 3×4 그리드로 1~12월 표시
 * - 해당 월에 이벤트가 하나라도 있으면 하단 바로 표시
 * - 월 타일 클릭 시 onMonthClick(month) 호출
 *
 * @param modifier BaseAppScaffold의 content padding 적용을 위해 사용하세요.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun YearView(
    year: Int,
    eventsByDate: Map<LocalDate, List<CalendarEvent>> = emptyMap(),
    onMonthClick: (month: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val months = remember { (1..12).toList() }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(months) { month ->
            val hasEvents = remember(eventsByDate, year, month) {
                eventsByDate.keys.any { it.year == year && it.monthValue == month }
            }

            MonthTile(
                month = month,
                hasEvents = hasEvents,
                onClick = { onMonthClick(month) }
            )
        }
    }
}

@Composable
private fun MonthTile(
    month: Int,
    hasEvents: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(8.dp)
            .aspectRatio(1f) // 정사각형
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // 중앙에 "N월" 텍스트
        Text(
            text = "${month}월",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        // 하단 바 (해당 월에 이벤트가 있을 때 표시)
        if (hasEvents) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp)
                    .height(4.dp)
                    .width(36.dp)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}