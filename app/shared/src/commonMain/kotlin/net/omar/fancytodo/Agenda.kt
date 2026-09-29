package net.omar.fancytodo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val NowLineColor = Color(0xFFE0554A)
private val DateBadgeColor = Color(0xFFC7DBF5)

@Composable
internal fun DayCalendar(modifier: Modifier = Modifier, startHour: Int = 7, endHour: Int = 20) {
    val info = remember { currentNow() }
    val hourRowHeight = 60.dp
    val minutesFromStart = ((info.hour - startHour) * 60 + info.minute).coerceIn(0, (endHour - startHour) * 60)
    val nowOffsetDp = hourRowHeight * (minutesFromStart / 60f)

    Column(modifier.background(BgColor)) {
        Column(Modifier.padding(16.dp)) {
            Text(info.offsetLabel, color = TextMid, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(info.dayAbbrev, color = Color(0xFF9BB8E8), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Box(Modifier.size(44.dp).background(DateBadgeColor, RoundedCornerShape(22.dp)), contentAlignment = Alignment.Center) {
                    Text("${info.dayNumber}", color = Color(0xFF15202B), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Box(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            Column {
                for (hour in startHour..endHour) {
                    Box(Modifier.fillMaxWidth().height(hourRowHeight)) {
                        HorizontalDivider(color = BorderColor, modifier = Modifier.align(Alignment.TopStart).fillMaxWidth())
                        Text(
                            hour.toString().padStart(2, '0') + ":00",
                            color = TextMid,
                            fontSize = 12.sp,
                            modifier = Modifier.align(Alignment.TopStart).padding(top = 4.dp)
                        )
                    }
                }
            }
            Row(Modifier.fillMaxWidth().offset(y = nowOffsetDp).align(Alignment.TopStart), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(10.dp).background(NowLineColor, RoundedCornerShape(5.dp)))
                Box(Modifier.weight(1f).height(2.dp).background(NowLineColor))
            }
        }
    }
}
