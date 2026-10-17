package com.example.panalsuite.domain.usecase

import com.example.panalsuite.domain.repository.TicketRepository

class AddCommentUseCase(private val repository: TicketRepository) {
    suspend operator fun invoke(ticketId: String, text: String): Result<Unit> {
        val clean = text.trim()
        if (clean.isEmpty() || clean.length > 500) {
            return Result.failure(IllegalArgumentException("El comentario debe tener entre 1 y 500 caracteres."))
        }
        return repository.addComment(ticketId, clean)
    }
}
