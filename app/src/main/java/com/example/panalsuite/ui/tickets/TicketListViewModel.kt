package com.example.panalsuite.ui.tickets

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.panalsuite.domain.model.Priority
import com.example.panalsuite.domain.model.Role
import com.example.panalsuite.domain.model.Ticket
import com.example.panalsuite.domain.repository.AuthRepository
import com.example.panalsuite.domain.repository.TicketRepository
import com.example.panalsuite.domain.usecase.GetTicketsUseCase
import com.example.panalsuite.domain.usecase.StatusFilter
import com.example.panalsuite.ui.common.TicketsLoader
import com.example.panalsuite.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class TicketListUiState(
    val title: String,
    val query: String = "",
    val statusFilter: StatusFilter = StatusFilter.ALL,
    val priorityFilter: Priority? = null,
    val content: UiState<List<Ticket>>,
)

class TicketListViewModel(
    savedStateHandle: SavedStateHandle,
    repository: TicketRepository,
    auth: AuthRepository,
    private val getTicketsUseCase: GetTicketsUseCase = GetTicketsUseCase(repository),
) : ViewModel() {
    private val loader = TicketsLoader(repository, viewModelScope)

    private val initialFilter = savedStateHandle.get<String>("filter")
        ?.let { name -> StatusFilter.entries.firstOrNull { it.name == name } }
        ?: StatusFilter.ALL

    private val searchQuery = MutableStateFlow("")
    private val statusFilter = MutableStateFlow(initialFilter)
    private val priorityFilter = MutableStateFlow<Priority?>(null)

    val state: StateFlow<TicketListUiState> =
        combine(
            loader.state,
            searchQuery,
            statusFilter,
            priorityFilter,
            auth.currentUser,
        ) { rawState, query, sFilter, pFilter, user ->
            val title = when (user?.role) {
                Role.SOLICITANTE -> "Mis solicitudes"
                Role.RESOLUTOR -> "Mi bandeja"
                else -> "Bandeja de solicitudes"
            }

            val content = when (rawState) {
                is UiState.Success -> {
                    val filtered = getTicketsUseCase.filter(rawState.data, sFilter, pFilter, query)
                    UiState.Success(filtered)
                }
                is UiState.Error -> rawState
                UiState.Loading -> UiState.Loading
            }

            TicketListUiState(
                title = title,
                query = query,
                statusFilter = sFilter,
                priorityFilter = pFilter,
                content = content,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            TicketListUiState("Bandeja de solicitudes", "", initialFilter, null, UiState.Loading),
        )

    fun onQueryChange(value: String) {
        searchQuery.value = value
    }

    fun onStatusFilterSelected(value: StatusFilter) {
        statusFilter.value = value
    }

    fun onPriorityFilterSelected(value: Priority?) {
        priorityFilter.value = value
    }

    fun retry() = loader.reload()
}
