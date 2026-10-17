package com.example.panalsuite.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.panalsuite.domain.model.Role
import com.example.panalsuite.domain.model.User
import com.example.panalsuite.ui.common.label
import com.example.panalsuite.ui.components.AccessDeniedView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(viewModel: AdminViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onMessageShown()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Administración (simulada)") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        if (!state.allowed) {
            AccessDeniedView(Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Text(
                        "Datos ficticios en memoria. Los cambios se pierden al cerrar la aplicación.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                item { Text("Parámetros simulados", style = MaterialTheme.typography.titleMedium) }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Máx. solicitudes activas por resolutor: ${state.parameters.maxActivePerResolver}",
                                    modifier = Modifier.weight(1f),
                                )
                                OutlinedButton(
                                    onClick = { viewModel.changeMaxActive(-1) },
                                    modifier = Modifier.semantics { contentDescription = "Disminuir máximo" },
                                ) { Text("-") }
                                OutlinedButton(
                                    onClick = { viewModel.changeMaxActive(1) },
                                    modifier = Modifier.semantics { contentDescription = "Aumentar máximo" },
                                ) { Text("+") }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Notificaciones simuladas", modifier = Modifier.weight(1f))
                                Switch(
                                    checked = state.parameters.notificationsEnabled,
                                    onCheckedChange = viewModel::toggleNotifications,
                                )
                            }
                        }
                    }
                }
                item { Text("Usuarios de prueba", style = MaterialTheme.typography.titleMedium) }
                items(state.users, key = { it.id }) { user ->
                    UserCard(
                        user = user,
                        isSelf = user.id == state.currentUserId,
                        onRole = { viewModel.changeRole(user.id, it) },
                        onActive = { viewModel.setActive(user.id, it) },
                    )
                }
                item {
                    Button(
                        onClick = viewModel::createTestUser,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                    ) { Text("Agregar usuario de prueba") }
                }
            }
        }
    }
}

@Composable
private fun UserCard(user: User, isSelf: Boolean, onRole: (Role) -> Unit, onActive: (Boolean) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "${user.displayName} (${user.username})" + if (isSelf) " · tú" else "",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    OutlinedButton(onClick = { expanded = true }, enabled = !isSelf) {
                        Text("Perfil: ${user.role.label()}")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        Role.entries.forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role.label()) },
                                onClick = {
                                    expanded = false
                                    onRole(role)
                                },
                            )
                        }
                    }
                }
                Text(if (user.active) "Activo" else "Inactivo", modifier = Modifier.padding(end = 8.dp))
                Switch(checked = user.active, onCheckedChange = onActive, enabled = !isSelf)
            }
        }
    }
}
