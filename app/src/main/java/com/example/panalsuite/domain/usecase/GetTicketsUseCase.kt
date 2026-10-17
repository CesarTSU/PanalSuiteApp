package com.example.panalsuite.domain.usecase

import com.example.panalsuite.domain.model.Priority
import com.example.panalsuite.domain.model.Ticket
import com.example.panalsuite.domain.model.TicketStatus
import com.example.panalsuite.domain.repository.TicketRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class StatusFilter(val label: String) {
    ALL("Todas"),
    PENDING("Pendientes"),
    IN_PROGRESS("En proceso"),
    RESOLVED("Resueltas");

    fun matches(ticket: Ticket) = when (this) {
        ALL -> true
        PENDING -> ticket.status == TicketStatus.ABIERTO
        IN_PROGRESS -> ticket.status == TicketStatus.EN_PROGRESO || ticket.status == TicketStatus.EN_REVISION
        RESOLVED -> ticket.status == TicketStatus.RESUELTO || ticket.status == TicketStatus.CERRADO
    }
}

class GetTicketsUseCase(private val repository: TicketRepository) {
    operator fun invoke(
        statusFilter: StatusFilter = StatusFilter.ALL,
        priorityFilter: Priority? = null,
        query: String = "",
    ): Flow<List<Ticket>> {
        return repository.observeTickets().map { list ->
            filter(list, statusFilter, priorityFilter, query)
        }
    }

    fun filter(
        list: List<Ticket>,
        statusFilter: StatusFilter = StatusFilter.ALL,
        priorityFilter: Priority? = null,
        query: String = "",
    ): List<Ticket> {
        val cleanQuery = query.trim().lowercase()
        return list.filter { ticket ->
            val matchesStatus = statusFilter.matches(ticket)
            val matchesPriority = (priorityFilter == null) || (ticket.priority == priorityFilter)
            val matchesQuery = cleanQuery.isEmpty() ||
                ticket.id.lowercase().contains(cleanQuery) ||
                ticket.title.lowercase().contains(cleanQuery) ||
                ticket.description.lowercase().contains(cleanQuery) ||
                ticket.category.lowercase().contains(cleanQuery) ||
                ticket.flowType.lowercase().contains(cleanQuery) ||
                ticket.referenceLocation.lowercase().contains(cleanQuery) ||
                ticket.creatorName.lowercase().contains(cleanQuery) ||
                ticket.resolvingTeam.lowercase().contains(cleanQuery)
            matchesStatus && matchesPriority && matchesQuery
        }
    }
}
