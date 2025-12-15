
package com.example.w09.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.LocalDate

/**
 * 날짜에 일정을 추가하는 다이얼로그.
 * - 제목 입력
 * - 색상 선택 (HEX)
 */

@Composable
fun AddEventDialog(
    date: LocalDate,
    onDismiss: () -> Unit,
    onAdd: (title: String, colorHex: Long) -> Unit
) {
    var title by remember { mutableStateOf("") }

    // 1) 먼저 Composable 컨텍스트에서 읽는다
    val initialColor = MaterialTheme.colorScheme.primary

    // 2) 그 값을 remember 초기값으로 넣는다
    var selectedColor by remember(initialColor) { mutableStateOf(initialColor) }

    val colorOptions = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        Color(0xFFE57373),
        Color(0xFF64B5F6),
        Color(0xFF81C784)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank(),
                onClick = {
                    val hex = colorToHex(selectedColor)
                    onAdd(title.trim(), hex)
                }
            ) { Text("추가") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        },
        title = { Text("${date.year}년 ${date.monthValue}월 ${date.dayOfMonth}일 일정 추가") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("제목") }
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("색상 선택", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    colorOptions.forEach { c ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(c, shape = CircleShape)
                                .clickable { selectedColor = c }
                        )
                    }
                }
            }
        }
    )
}
/**
 * Compose Color를 ARGB Long(0xFFRRGGBB)로 변환
 */
private fun colorToHex(color: Color): Long {
    val a = (color.alpha * 255).toInt() and 0xFF
    val r = (color.red * 255).toInt() and 0xFF
    val g = (color.green * 255).toInt() and 0xFF
    val b = (color.blue * 255).toInt() and 0xFF
    return ((a.toLong() shl 24) or (r.toLong() shl 16) or (g.toLong() shl 8) or b.toLong())
}