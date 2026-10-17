package com.example.panalsuite.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.panalsuite.domain.model.Comment
import com.example.panalsuite.domain.model.TicketHistoryEntry
import com.example.panalsuite.ui.common.UiState
import com.example.panalsuite.ui.common.relativeTime
import com.example.panalsuite.ui.components.EmptyView
import com.example.panalsuite.ui.components.PriorityBadge
import com.example.panalsuite.ui.components.StatusChip
import com.example.panalsuite.ui.components.UiStateContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketDetailScreen(viewModel: TicketDetailViewModel, onBack: () -> Unit) {
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
                title = { Text("Detalle de solicitud") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if ((state.content as? UiState.Success)?.data?.canComment == true) {
                CommentInput(
                    draft = state.draft,
                    enabled = !state.busy,
                    onDraftChange = viewModel::onDraftChange,
                    onSend = viewModel::sendComment,
                )
            }
        },
    ) { padding ->
        UiStateContent(state.content, onRetry = null, modifier = Modifier.padding(padding).fillMaxSize()) { data ->
            val ticket = data.ticket
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(ticket.id, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                                StatusChip(ticket.status)
                            }
                            Text(ticket.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                PriorityBadge(ticket.priority)
                                Text("${ticket.flowType} • ${ticket.category}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            HorizontalDivider()
                            DetailRow("Solicitante", ticket.creatorName)
                            DetailRow("Equipo resolutor", ticket.resolvingTeam)
                            DetailRow("Responsable", ticket.assigneeName ?: "Sin asignar")
                            DetailRow("Ubicación referencial", ticket.referenceLocation)
                            DetailRow("Creación", relativeTime(ticket.createdAt))
                            DetailRow("Última actualización", relativeTime(ticket.updatedAt))
                            HorizontalDivider()
                            Text("Descripción:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                            Text(ticket.description, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }

                item {
                    Text("Acciones disponibles", style = MaterialTheme.typography.titleMedium)
                }
                if (data.actions.isEmpty()) {
                    item {
                        Text(
                            "No hay acciones disponibles para tu rol en este estado.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    items(data.actions, key = { it.label }) { action ->
                        val isPrimary = action == data.actions.first()
                        val modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                        if (isPrimary) {
                            Button(onClick = { viewModel.performAction(action) }, enabled = !state.busy, modifier = modifier) {
                                Text(action.label)
                            }
                        } else {
                            OutlinedButton(onClick = { viewModel.performAction(action) }, enabled = !state.busy, modifier = modifier) {
                                Text(action.label)
                            }
                        }
                    }
                }

                item {
                    Text("Trazabilidad e Historial de Cambios (${data.history.size})", style = MaterialTheme.typography.titleMedium)
                }
                if (data.history.isEmpty()) {
                    item {
                        Text(
                            "Sin cambios registrados.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    items(data.history, key = { it.id }) { entry -> HistoryItem(entry) }
                }

                item {
                    Text("Evidencias (${data.evidence.size})", style = MaterialTheme.typography.titleMedium)
                }
                if (data.evidence.isEmpty()) {
                    item {
                        Text(
                            "Sin evidencias adjuntas.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    items(data.evidence, key = { it.id }) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(item.fileName, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    "${item.authorName} · ${relativeTime(item.createdAt)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
                if (data.canAddEvidence) {
                    item {
                        OutlinedButton(
                            onClick = viewModel::addEvidence,
                            enabled = !state.busy,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                        ) { Text("Adjuntar evidencia simulada") }
                    }
                }

                item {
                    Text("Comentarios (${data.comments.size})", style = MaterialTheme.typography.titleMedium)
                }
                if (data.comments.isEmpty()) {
                    item {
                        EmptyView("Sin comentarios", "Aún no hay comentarios en esta solicitud.", Modifier.heightIn(min = 160.dp))
                    }
                } else {
                    items(data.comments, key = { it.id }) { comment -> CommentItem(comment) }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun HistoryItem(entry: TicketHistoryEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row {
                Text(entry.authorName, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                Text(
                    relativeTime(entry.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(entry.action, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun CommentItem(comment: Comment) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row {
                Text(comment.authorName, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                Text(
                    relativeTime(comment.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(comment.text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun CommentInput(draft: String, enabled: Boolean, onDraftChange: (String) -> Unit, onSend: () -> Unit) {
    Surface(tonalElevation = 3.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = draft,
                onValueChange = onDraftChange,
                label = { Text("Escribe un comentario") },
                modifier = Modifier.weight(1f),
                maxLines = 3,
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onSend, enabled = enabled && draft.isNotBlank()) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar comentario")
            }
        }
    }
}
