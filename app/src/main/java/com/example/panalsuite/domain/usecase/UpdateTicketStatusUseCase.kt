package com.example.panalsuite.domain.usecase

import com.example.panalsuite.domain.model.TicketStatus
import com.example.panalsuite.domain.repository.TicketRepository

class UpdateTicketStatusUseCase(private val repository: TicketRepository) {
    suspend operator fun invoke(ticketId: String, targetStatus: TicketStatus): Result<Unit> {
        if (ticketId.isBlank()) {
            return Result.failure(IllegalArgumentException("ID de solicitud inválido."))
        }
        return repository.updateStatus(ticketId, targetStatus)
    }
}
