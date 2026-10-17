package com.example.panalsuite.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.panalsuite.ui.common.label
import com.example.panalsuite.ui.components.TicketCard
import com.example.panalsuite.ui.components.UiStateContent
import com.example.panalsuite.ui.theme.InProgressColor
import com.example.panalsuite.ui.theme.PendingColor
import com.example.panalsuite.ui.theme.ResolvedColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenList: (filter: String) -> Unit,
    onOpenTicket: (String) -> Unit,
    onCreate: () -> Unit,
    onWorkload: () -> Unit,
    onAdmin: () -> Unit,
    onLogout: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("PanalSuite") },
                actions = { TextButton(onClick = onLogout, modifier = Modifier.heightIn(min = 48.dp)) { Text("Salir") } },
            )
        },
    ) { padding ->
        UiStateContent(state, onRetry = viewModel::retry, modifier = Modifier.padding(padding).fillMaxSize()) { data ->
            val caps = data.capabilities
            val listClick: ((String) -> Unit)? = if (caps.canViewTickets) onOpenList else null
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Column {
                        Text("Hola, ${data.userName}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        data.role?.let {
                            Text(
                                "Rol: ${it.label()}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                if (caps.canAdminister || data.role != null) {
                    item { Text(caps.summaryTitle, style = MaterialTheme.typography.titleMedium) }
                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SummaryCard(data.summary.pending, "Pendientes", PendingColor, Modifier.weight(1f), listClick?.let { { it("PENDING") } })
                            SummaryCard(data.summary.inProgress, "En proceso", InProgressColor, Modifier.weight(1f), listClick?.let { { it("IN_PROGRESS") } })
                            SummaryCard(data.summary.resolved, "Resueltas", ResolvedColor, Modifier.weight(1f), listClick?.let { { it("RESOLVED") } })
                        }
                    }
                }
                if (caps.canCreate) {
                    item {
                        Button(onClick = onCreate, shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                            Text("Nueva solicitud")
                        }
                    }
                }
                if (caps.canViewTickets) {
                    item {
                        OutlinedButton(onClick = { onOpenList("ALL") }, shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                            Text(caps.ticketsLabel)
                        }
                    }
                }
                if (caps.canViewWorkload) {
                    item {
                        OutlinedButton(onClick = onWorkload, shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                            Text("Cargas de trabajo")
                        }
                    }
                }
                if (caps.canAdminister) {
                    item {
                        OutlinedButton(onClick = onAdmin, shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                            Text("Administración (simulada)")
                        }
                    }
                }
                if (data.recent.isNotEmpty()) {
                    item { Text("Actividad reciente", style = MaterialTheme.typography.titleMedium) }
                    items(data.recent, key = { it.id }) { ticket ->
                        TicketCard(ticket, onClick = { onOpenTicket(ticket.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(count: Int, label: String, accent: Color, modifier: Modifier, onClick: (() -> Unit)?) {
    val colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    val border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    val semanticModifier = modifier.semantics(mergeDescendants = true) { contentDescription = "$count $label" }
    val content: @Composable ColumnScope.() -> Unit = {
        Column(
            Modifier.fillMaxWidth().padding(12.dp).heightIn(min = 72.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(count.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = accent)
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
    if (onClick != null) {
        Card(onClick = onClick, modifier = semanticModifier, shape = MaterialTheme.shapes.large, colors = colors, border = border, content = content)
    } else {
        Card(modifier = semanticModifier, shape = MaterialTheme.shapes.large, colors = colors, border = border, content = content)
    }
}

