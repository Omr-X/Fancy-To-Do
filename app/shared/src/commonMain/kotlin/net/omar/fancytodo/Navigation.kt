package net.omar.fancytodo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal enum class Page(val label: String) {
    Home("Home"),
    Calendar("Calendar"),
    Dashboard("Dashboard"),
    Tasks("Tasks")
}

internal data class EditorRequest(val task: Task?, val defaultDue: String? = null, val defaultTopicId: String? = null)

@Composable
internal fun Sidebar(page: Page, onSelect: (Page) -> Unit) {
    Column(Modifier.width(232.dp).fillMaxHeight().background(SidebarColor).padding(horizontal = 16.dp, vertical = 24.dp)) {
        Row(Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(30.dp).clip(RoundedCornerShape(9.dp)).background(AccentColor), contentAlignment = Alignment.Center) {
                CheckIcon(Color.White, 16.dp)
            }
            Spacer(Modifier.width(10.dp))
            Text("TaskMan", color = TextHi, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(32.dp))
        Page.entries.forEach { entry ->
            val selected = entry == page
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                    .background(if (selected) PanelHiColor else Color.Transparent)
                    .clickable { onSelect(entry) }
                    .padding(horizontal = 12.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(6.dp).clip(CircleShape).background(if (selected) AccentColor else Color.Transparent))
                Spacer(Modifier.width(12.dp))
                Text(entry.label, color = if (selected) TextHi else TextMid, fontSize = 15.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium)
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
internal fun BottomNav(page: Page, onSelect: (Page) -> Unit) {
    Row(Modifier.fillMaxWidth().background(SidebarColor).padding(vertical = 6.dp)) {
        Page.entries.forEach { entry ->
            val selected = entry == page
            Column(
                Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).clickable { onSelect(entry) }.padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(Modifier.size(5.dp).clip(CircleShape).background(if (selected) AccentColor else Color.Transparent))
                Spacer(Modifier.height(5.dp))
                Text(entry.label, color = if (selected) TextHi else TextMid, fontSize = 13.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium)
            }
        }
    }
}
