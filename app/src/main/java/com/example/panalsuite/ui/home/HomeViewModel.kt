package com.example.panalsuite.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.panalsuite.domain.model.Role
import com.example.panalsuite.domain.model.Ticket
import com.example.panalsuite.domain.model.TicketSummary
import com.example.panalsuite.domain.model.User
import com.example.panalsuite.domain.repository.AuthRepository
import com.example.panalsuite.domain.repository.TicketRepository
import com.example.panalsuite.domain.security.AccessPolicy
import com.example.panalsuite.domain.security.Permission
import com.example.panalsuite.ui.common.TicketsLoader
import com.example.panalsuite.ui.common.UiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** Opciones visibles según el rol. La autorización real se vuelve a validar en los repositorios. */
data class HomeCapabilities(
    val canCreate: Boolean,
    val canViewTickets: Boolean,
    val ticketsLabel: String,
    val canViewWorkload: Boolean,
    val canAdminister: Boolean,
    val summaryTitle: String,
)

data class HomeData(
    val userName: String,
    val role: Role?,
    val summary: TicketSummary,
    val recent: List<Ticket>,
    val capabilities: HomeCapabilities,
)

fun capabilitiesFor(user: User?) = HomeCapabilities(
    canCreate = AccessPolicy.has(user, Permission.CREATE_TICKET),
    canViewTickets = AccessPolicy.has(user, Permission.VIEW_OWN_TICKETS) ||
        AccessPolicy.has(user, Permission.VIEW_ASSIGNED_TICKETS) ||
        AccessPolicy.has(user, Permission.VIEW_ALL_TICKETS),
    ticketsLabel = when (user?.role) {
        Role.SOLICITANTE -> "Mis solicitudes"
        Role.RESOLUTOR -> "Mi bandeja"
        else -> "Todas las solicitudes"
    },
    canViewWorkload = AccessPolicy.has(user, Permission.VIEW_WORKLOAD),
    canAdminister = AccessPolicy.has(user, Permission.MANAGE_USERS) ||
        AccessPolicy.has(user, Permission.MANAGE_PARAMETERS),
    summaryTitle = if (AccessPolicy.has(user, Permission.VIEW_INDICATORS)) "Indicadores generales" else "Resumen de mis solicitudes",
)

class HomeViewModel(
    repository: TicketRepository,
    private val auth: AuthRepository,
) : ViewModel() {
    private val loader = TicketsLoader(repository, viewModelScope)

    val state: StateFlow<UiState<HomeData>> =
        combine(loader.state, repository.observeSummary(), auth.currentUser) { tickets, summary, user ->
            when (tickets) {
                UiState.Loading -> UiState.Loading
                is UiState.Error -> tickets
                is UiState.Success -> UiState.Success(
                    HomeData(
                        userName = user?.displayName ?: "Invitado",
                        role = user?.role,
                        summary = summary,
                        recent = tickets.data.take(3),
                        capabilities = capabilitiesFor(user),
                    )
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    fun retry() = loader.reload()

    fun logout() = auth.logout()
}
