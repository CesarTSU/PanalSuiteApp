package com.example.panalsuite.ui.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.panalsuite.domain.model.Priority
import com.example.panalsuite.domain.repository.AuthRepository
import com.example.panalsuite.domain.repository.TicketRepository
import com.example.panalsuite.domain.security.AccessPolicy
import com.example.panalsuite.domain.security.Permission
import com.example.panalsuite.domain.usecase.CreateTicketUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateTicketUiState(
    val allowed: Boolean,
    val title: String = "",
    val description: String = "",
    val flowType: String = "Soporte TI",
    val category: String = "Software",
    val priority: Priority = Priority.MEDIA,
    val referenceLocation: String = "Edificio Principal - Piso 3",
    val resolvingTeam: String = "Mesa de Ayuda TI",
    val saving: Boolean = false,
    val error: String? = null,
    val created: Boolean = false,
)

class CreateTicketViewModel(
    repository: TicketRepository,
    auth: AuthRepository,
    private val createTicketUseCase: CreateTicketUseCase = CreateTicketUseCase(repository),
) : ViewModel() {
    private val _state = MutableStateFlow(
        CreateTicketUiState(allowed = AccessPolicy.has(auth.currentUser.value, Permission.CREATE_TICKET)),
    )
    val state: StateFlow<CreateTicketUiState> = _state.asStateFlow()

    fun onTitleChange(value: String) = _state.update { it.copy(title = value.take(80), error = null) }
    fun onDescriptionChange(value: String) = _state.update { it.copy(description = value.take(500), error = null) }
    fun onFlowTypeChange(value: String) = _state.update { it.copy(flowType = value.take(50), error = null) }
    fun onCategoryChange(value: String) = _state.update { it.copy(category = value.take(50), error = null) }
    fun onPriorityChange(value: Priority) = _state.update { it.copy(priority = value) }
    fun onLocationChange(value: String) = _state.update { it.copy(referenceLocation = value.take(80), error = null) }
    fun onResolvingTeamChange(value: String) = _state.update { it.copy(resolvingTeam = value.take(50), error = null) }
    fun onNavigated() = _state.update { it.copy(created = false) }

    fun submit() {
        val s = _state.value
        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            createTicketUseCase(
                title = s.title,
                description = s.description,
                priority = s.priority,
                flowType = s.flowType,
                category = s.category,
                referenceLocation = s.referenceLocation,
                resolvingTeam = s.resolvingTeam,
            )
                .onSuccess { _state.update { it.copy(saving = false, created = true) } }
                .onFailure { e -> _state.update { it.copy(saving = false, error = e.message) } }
        }
    }
}
