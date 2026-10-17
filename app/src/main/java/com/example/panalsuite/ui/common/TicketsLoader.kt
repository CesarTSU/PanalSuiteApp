package com.example.panalsuite.ui.common

import com.example.panalsuite.domain.model.Ticket
import com.example.panalsuite.domain.repository.TicketRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Combina la base local con el estado de la carga para exponer Loading, Error o Success. */
class TicketsLoader(
    private val repository: TicketRepository,
    private val scope: CoroutineScope,
) {
    private val phase = MutableStateFlow<UiState<Unit>>(UiState.Loading)

    val state: StateFlow<UiState<List<Ticket>>> =
        combine(repository.observeTickets(), phase) { tickets, p ->
            when {
                tickets.isNotEmpty() -> UiState.Success(tickets)
                p is UiState.Error -> p
                p is UiState.Success -> UiState.Success(emptyList())
                else -> UiState.Loading
            }
        }.stateIn(scope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    init {
        reload()
    }

    fun reload() {
        scope.launch {
            phase.value = UiState.Loading
            phase.value = repository.refresh().fold(
                onSuccess = { UiState.Success(Unit) },
                onFailure = { UiState.Error(it.message ?: "No se pudieron cargar las solicitudes.") },
            )
        }
    }
}
