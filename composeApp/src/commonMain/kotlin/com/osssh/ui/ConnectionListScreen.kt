package com.osssh.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.osssh.data.ConnectionProfile
import java.util.UUID

@Composable
fun ConnectionListScreen(
    profiles: List<ConnectionProfile>,
    onAdd: (ConnectionProfile, String?) -> Unit,
    onConnect: (ConnectionProfile) -> Unit,
    notice: String? = null,
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("OSSSH", style = MaterialTheme.typography.headlineMedium)
        notice?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        LazyColumn(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
        ) {
            items(profiles, key = { it.id }) { p ->
                Card(Modifier.fillMaxWidth().clickable { onConnect(p) }) {
                    Column(Modifier.padding(16.dp)) {
                        Text(p.name, style = MaterialTheme.typography.titleMedium)
                        Text("${p.user}@${p.host}:${p.port}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        Button(onClick = { showDialog = true }, modifier = Modifier.fillMaxWidth()) { Text("New connection") }
    }

    if (showDialog) NewConnectionDialog(onAdd) { showDialog = false }
}

@Composable
private fun NewConnectionDialog(onAdd: (ConnectionProfile, String?) -> Unit, dismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var host by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("22") }
    var user by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = dismiss,
        title = { Text("New connection") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Name") }, singleLine = true)
                OutlinedTextField(host, { host = it }, label = { Text("Host") }, singleLine = true)
                OutlinedTextField(port, { port = it.filter(Char::isDigit) }, label = { Text("Port") }, singleLine = true)
                OutlinedTextField(user, { user = it }, label = { Text("User") }, singleLine = true)
                OutlinedTextField(
                    password, { password = it }, label = { Text("Password (optional)") },
                    singleLine = true, visualTransformation = PasswordVisualTransformation(),
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = host.isNotBlank() && user.isNotBlank() && port.toIntOrNull() in 1..65535,
                onClick = {
                    onAdd(
                        ConnectionProfile(
                            id = UUID.randomUUID().toString(),
                            name = name.ifBlank { host },
                            host = host.trim(),
                            port = port.toInt(),
                            user = user.trim(),
                            auth = if (password.isEmpty()) ConnectionProfile.AuthSpec.InteractivePassword
                                   else ConnectionProfile.AuthSpec.SavedSecret,
                        ),
                        password.ifBlank { null },
                    )
                    dismiss()
                },
            ) { Text("Save") }
        },
        dismissButton = { TextButton(dismiss) { Text("Cancel") } },
    )
}
