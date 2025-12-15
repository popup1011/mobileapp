
package com.example.w09

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.w09.calendar.CalendarEvent
import com.example.w09.calendar.MonthView
import com.example.w09.calendar.WeekView
import com.example.w09.calendar.YearView
import com.example.w09.ui.theme.ComposeLabTheme
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeLabTheme {
                CalendarApp()
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CalendarApp() {
    val startYear = 2020
    val totalYears = 20

    val pagerState = rememberPagerState(
        initialPage = (LocalDate.now().year - startYear) * 12 + LocalDate.now().monthValue - 1,
        pageCount = { totalYears * 12 }
    )
    val scope = rememberCoroutineScope()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    var currentView by remember { mutableStateOf(CalendarViewMode.MONTH) }

    var showSearchDialog by remember { mutableStateOf(false) }
    var inputYear by remember { mutableStateOf(LocalDate.now().year.toString()) }
    var inputMonth by remember { mutableStateOf(LocalDate.now().monthValue.toString()) }

    // ✅ 선택 날짜 상태 추가
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    val eventsByDate = remember { mutableStateMapOf<LocalDate, MutableList<CalendarEvent>>() }
    var nextEventId by remember { mutableStateOf(1L) }
    fun addEvent(date: LocalDate, title: String, colorHex: Long) {
        val newEvent = CalendarEvent(id = nextEventId++, date = date, title = title, colorHex = colorHex)
        eventsByDate.getOrPut(date) { mutableListOf() }.add(newEvent)
        selectedDate = date // ✅ 이벤트 추가 시 선택 날짜 갱신
    }

    val currentYear = startYear + pagerState.currentPage / 12
    val currentMonth = (pagerState.currentPage % 12) + 1

    val titleText = when (currentView) {
        CalendarViewMode.YEAR -> "${currentYear}년"
        CalendarViewMode.MONTH -> "${currentYear}년 ${currentMonth}월"
        CalendarViewMode.WEEK -> "${selectedDate.year}년 ${selectedDate.monthValue}월 (주간)"
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("보기 선택", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp))
                NavigationDrawerItem(
                    label = { Text("연(Year)") },
                    selected = currentView == CalendarViewMode.YEAR,
                    onClick = {
                        currentView = CalendarViewMode.YEAR
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    label = { Text("월(Month)") },
                    selected = currentView == CalendarViewMode.MONTH,
                    onClick = {
                        currentView = CalendarViewMode.MONTH
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    label = { Text("주(Week)") },
                    selected = currentView == CalendarViewMode.WEEK,
                    onClick = {
                        currentView = CalendarViewMode.WEEK
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        BaseAppScaffold(
            title = titleText,
            navigationIcon = {
                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                    Icon(Icons.Filled.Menu, contentDescription = "메뉴")
                }
            },
            actions = {
                IconButton(onClick = { showSearchDialog = true }) {
                    Icon(Icons.Filled.Search, contentDescription = "검색")
                }
            },
            content = { padding ->
                when (currentView) {
                    CalendarViewMode.YEAR -> {
                        YearView(
                            year = currentYear,
                            eventsByDate = eventsByDate.mapValues { it.value.toList() },
                            onMonthClick = { month ->
                                val targetPage = (currentYear - startYear) * 12 + (month - 1)
                                scope.launch {
                                    pagerState.scrollToPage(targetPage)
                                    currentView = CalendarViewMode.MONTH
                                }
                            },
                            modifier = Modifier.padding(padding)
                        )
                    }
                    CalendarViewMode.MONTH -> {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.padding(padding)
                        ) { page ->
                            val year = startYear + page / 12
                            val month = (page % 12) + 1
                            val yearMonth = YearMonth.of(year, month)

                            MonthView(
                                yearMonth = yearMonth,
                                firstDayOfWeek = DayOfWeek.MONDAY,
                                eventsByDate = eventsByDate.mapValues { it.value.toList() },
                                onAddEvent = { date, title, colorHex ->
                                    addEvent(date, title, colorHex)
                                }
                            )
                        }
                    }
                    CalendarViewMode.WEEK -> {
                        WeekView(
                            baseDate = selectedDate, // ✅ 선택 날짜 기준으로 주간 뷰 표시
                            firstDayOfWeek = DayOfWeek.MONDAY,
                            eventsByDate = eventsByDate.mapValues { it.value.toList() },
                            onDayClick = { clickedDate ->
                                selectedDate = clickedDate // ✅ 주간 뷰에서 날짜 클릭 시 갱신
                            }
                        )
                    }
                }
            }
        )
    }

    if (showSearchDialog) {
        AlertDialog(
            onDismissRequest = { showSearchDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    val year = inputYear.toIntOrNull() ?: LocalDate.now().year
                    val month = inputMonth.toIntOrNull() ?: LocalDate.now().monthValue
                    if (month in 1..12 && year in startYear..(startYear + totalYears - 1)) {
                        val targetPage = (year - startYear) * 12 + (month - 1)
                        scope.launch { pagerState.scrollToPage(targetPage) }
                    }
                    showSearchDialog = false
                }) { Text("이동") }
            },
            dismissButton = {
                TextButton(onClick = { showSearchDialog = false }) { Text("취소") }
            },
            title = { Text("원하는 연/월로 이동") },
            text = {
                Column {
                    OutlinedTextField(value = inputYear, onValueChange = { inputYear = it }, label = { Text("연도") })
                    OutlinedTextField(value = inputMonth, onValueChange = { inputMonth = it }, label = { Text("월 (1~12)") })
                }
            }
        )
    }
} // ← ✅ 여기! CalendarApp() 닫는 중괄호가 반드시 필요합니다.


// ✅ enum은 반드시 "파일 최상위"에 위치해야 합니다.
enum class CalendarViewMode { YEAR, MONTH, WEEK }
