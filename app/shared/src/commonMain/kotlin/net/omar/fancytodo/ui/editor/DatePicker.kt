package net.omar.fancytodo.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.omar.fancytodo.time.MONTH_NAMES
import net.omar.fancytodo.time.WEEKDAY_ABBREVIATIONS
import net.omar.fancytodo.time.daysInMonth
import net.omar.fancytodo.time.isoDate
import net.omar.fancytodo.time.parseDate
import net.omar.fancytodo.time.weekdayIndex
import net.omar.fancytodo.ui.components.ChevronIcon
import net.omar.fancytodo.ui.theme.AccentColor
import net.omar.fancytodo.ui.theme.TextMuted
import net.omar.fancytodo.ui.theme.TextPrimary

@Composable
internal fun DatePicker(selected: String?, today: String, modifier: Modifier = Modifier, onSelect: (String) -> Unit) {
    val start = parseDate(selected ?: today)
    var year by remember { mutableStateOf(start.year) }
    var month by remember { mutableStateOf(start.month) }
    val offset = weekdayIndex(isoDate(year, month, 1))
    val length = daysInMonth(year, month)
    val cell: Dp = 34.dp

    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(28.dp).clip(CircleShape).clickable {
                    if (month == 1) {
                        month = 12
                        year -= 1
                    } else {
                        month -= 1
                    }
                },
                contentAlignment = Alignment.Center
            ) { ChevronIcon(left = true) }
            Text(
                "${MONTH_NAMES[month - 1]} $year",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Box(
                Modifier.size(28.dp).clip(CircleShape).clickable {
                    if (month == 12) {
                        month = 1
                        year += 1
                    } else {
                        month += 1
                    }
                },
                contentAlignment = Alignment.Center
            ) { ChevronIcon(left = false) }
        }
        Spacer(Modifier.height(6.dp))
        Row {
            WEEKDAY_ABBREVIATIONS.forEach {
                Text(it.take(2), color = TextMuted, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.width(cell))
            }
        }
        for (week in 0 until (offset + length + 6) / 7) {
            Row {
                for (column in 0..6) {
                    val day = week * 7 + column - offset + 1
                    val valid = day in 1..length
                    val iso = isoDate(year, month, day)
                    val active = valid && iso == selected
                    Box(
                        Modifier.size(cell).aspectRatio(1f).padding(2.dp).clip(CircleShape)
                            .background(if (active) AccentColor else Color.Transparent)
                            .then(if (valid) Modifier.clickable { onSelect(iso) } else Modifier),
                        contentAlignment = Alignment.Center
                    ) {
                        if (valid) {
                            Text(
                                "$day",
                                color = if (active) Color.White else if (iso == today) AccentColor else TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = if (iso == today) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}
