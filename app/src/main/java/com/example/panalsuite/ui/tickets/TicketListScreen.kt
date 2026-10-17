package com.example.panalsuite.ui.tickets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.panalsuite.domain.model.Priority
import com.example.panalsuite.domain.usecase.StatusFilter
import com.example.panalsuite.ui.common.label
import com.example.panalsuite.ui.components.EmptyView
import com.example.panalsuite.ui.components.TicketCard
import com.example.panalsuite.ui.components.UiStateContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketListScreen(
    viewModel: TicketListViewModel,
    onBack: () -> Unit,
    onOpenTicket: (String) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(state.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                placeholder = { Text("Buscar por título, ID, flujo, ubicación...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                trailingIcon = {
                    if (state.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                        }
                    }
                },
                singleLine = true,
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(StatusFilter.entries) { option ->
                    FilterChip(
                        selected = option == state.statusFilter,
                        onClick = { viewModel.onStatusFilterSelected(option) },
                        label = { Text(option.label) },
                    )
                }
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    FilterChip(
                        selected = state.priorityFilter == null,
                        onClick = { viewModel.onPriorityFilterSelected(null) },
                        label = { Text("Todas las prioridades") },
                    )
                }
                items(Priority.entries) { priority ->
                    FilterChip(
                        selected = priority == state.priorityFilter,
                        onClick = { viewModel.onPriorityFilterSelected(priority) },
                        label = { Text("Prioridad ${priority.label()}") },
                    )
                }
            }

            UiStateContent(state.content, onRetry = viewModel::retry, modifier = Modifier.fillMaxSize()) { tickets ->
                if (tickets.isEmpty()) {
                    EmptyView("Sin solicitudes", "No hay solicitudes que coincidan con los filtros aplicados.")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(tickets, key = { it.id }) { ticket ->
                            TicketCard(ticket, onClick = { onOpenTicket(ticket.id) })
                        }
                    }
                }
            }
        }
    }
}
