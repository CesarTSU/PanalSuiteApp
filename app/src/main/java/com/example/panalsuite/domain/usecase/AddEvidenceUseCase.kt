package com.example.panalsuite.domain.usecase

import com.example.panalsuite.domain.repository.TicketRepository

class AddEvidenceUseCase(private val repository: TicketRepository) {
    suspend operator fun invoke(ticketId: String, fileName: String): Result<Unit> {
        val clean = fileName.trim()
        if (clean.isEmpty() || clean.length > 100) {
            return Result.failure(IllegalArgumentException("Nombre de evidencia inválido."))
        }
        return repository.addEvidence(ticketId, clean)
    }
}
