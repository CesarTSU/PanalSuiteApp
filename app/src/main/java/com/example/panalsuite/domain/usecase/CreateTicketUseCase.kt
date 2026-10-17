package com.example.panalsuite.domain.usecase

import com.example.panalsuite.domain.model.Priority
import com.example.panalsuite.domain.model.Ticket
import com.example.panalsuite.domain.repository.TicketRepository

class CreateTicketUseCase(private val repository: TicketRepository) {
    suspend operator fun invoke(
        title: String,
        description: String,
        priority: Priority,
        flowType: String = "Soporte TI",
        category: String = "General",
        referenceLocation: String = "Sede Central",
        resolvingTeam: String = "Mesa de Ayuda TI",
    ): Result<Ticket> {
        val cleanTitle = title.trim()
        val cleanDescription = description.trim()
        val cleanFlowType = flowType.trim()
        val cleanCategory = category.trim()
        val cleanLocation = referenceLocation.trim()
        val cleanTeam = resolvingTeam.trim()

        if (cleanTitle.length !in 5..80) {
            return Result.failure(IllegalArgumentException("El título debe tener entre 5 y 80 caracteres."))
        }
        if (cleanDescription.length !in 10..500) {
            return Result.failure(IllegalArgumentException("La descripción debe tener entre 10 y 500 caracteres."))
        }
        if (cleanFlowType.isEmpty()) {
            return Result.failure(IllegalArgumentException("Debe ingresar el tipo de flujo."))
        }
        if (cleanCategory.isEmpty()) {
            return Result.failure(IllegalArgumentException("Debe ingresar la categoría."))
        }
        if (cleanLocation.isEmpty()) {
            return Result.failure(IllegalArgumentException("Debe ingresar la ubicación referencial."))
        }
        if (cleanTeam.isEmpty()) {
            return Result.failure(IllegalArgumentException("Debe ingresar el equipo resolutor."))
        }

        return repository.createTicket(
            title = cleanTitle,
            description = cleanDescription,
            priority = priority,
            flowType = cleanFlowType,
            category = cleanCategory,
            referenceLocation = cleanLocation,
            resolvingTeam = cleanTeam,
        )
    }
}
