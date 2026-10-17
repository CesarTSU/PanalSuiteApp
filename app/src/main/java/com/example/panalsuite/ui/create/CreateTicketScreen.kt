package com.example.panalsuite.ui.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.panalsuite.domain.model.Priority
import com.example.panalsuite.ui.common.label
import com.example.panalsuite.ui.components.AccessDeniedView

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateTicketScreen(viewModel: CreateTicketViewModel, onBack: () -> Unit, onCreated: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.created) {
        if (state.created) {
            viewModel.onNavigated()
            onCreated()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Nueva solicitud") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
    ) { padding ->
        if (!state.allowed) {
            AccessDeniedView(Modifier.padding(padding))
        } else {
            Column(
                modifier = Modifier.padding(padding).imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = state.title,
                    onValueChange = viewModel::onTitleChange,
                    label = { Text("Título de la solicitud") },
                    singleLine = true,
                    supportingText = { Text("${state.title.length}/80") },
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = state.description,
                    onValueChange = viewModel::onDescriptionChange,
                    label = { Text("Descripción del requerimiento") },
                    minLines = 3,
                    supportingText = { Text("${state.description.length}/500") },
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = state.flowType,
                    onValueChange = viewModel::onFlowTypeChange,
                    label = { Text("Tipo de flujo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = state.category,
                    onValueChange = viewModel::onCategoryChange,
                    label = { Text("Categoría") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = state.referenceLocation,
                    onValueChange = viewModel::onLocationChange,
                    label = { Text("Ubicación referencial ficticia") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = state.resolvingTeam,
                    onValueChange = viewModel::onResolvingTeamChange,
                    label = { Text("Equipo resolutor") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Text("Prioridad", style = MaterialTheme.typography.titleSmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Priority.entries.forEach { option ->
                        FilterChip(
                            selected = option == state.priority,
                            onClick = { viewModel.onPriorityChange(option) },
                            label = { Text(option.label()) },
                        )
                    }
                }

                state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                Button(
                    onClick = viewModel::submit,
                    enabled = !state.saving,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                ) { Text(if (state.saving) "Guardando..." else "Crear solicitud") }
            }
        }
    }
}
