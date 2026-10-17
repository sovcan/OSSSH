package com.osssh.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val MonoStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = Color(0xFFA5F3FC))

@Composable
fun TerminalScreen(
    lines: List<String>,
    banner: String,
    onSend: (String) -> Unit,
    onDisconnect: () -> Unit,
) {
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    LaunchedEffect(lines.size) { if (lines.isNotEmpty()) listState.animateScrollToItem(lines.lastIndex) }

    Column(Modifier.fillMaxSize().background(Color(0xFF07101F))) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
            Text(banner, color = Color(0xFF64748B), modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall)
            IconButton(onDisconnect) { Text("\u23FB", color = Color(0xFFF87171)) }
        }
        LazyColumn(state = listState, modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp)) {
            items(lines) { Text(it, style = MonoStyle) }
        }
        TextField(
            value = input,
            onValueChange = { input = it },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MonoStyle,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF0B1B33),
                unfocusedContainerColor = Color(0xFF0B1B33),
                focusedTextColor = Color(0xFFA5F3FC),
            ),
            placeholder = { Text("type a command…", style = MonoStyle.copy(color = Color(0xFF475569))) },
            trailingIcon = {
                IconButton(onClick = { if (input.isNotBlank()) { onSend(input); input = "" } }) { Text("\u21B5", color = Color(0xFF7DD3FC)) }
            },
        )
    }
}
