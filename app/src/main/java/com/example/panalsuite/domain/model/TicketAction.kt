package com.example.panalsuite.domain.model

import com.example.panalsuite.domain.security.AccessPolicy

data class TicketAction(val label: String, val target: TicketStatus)

object TicketActions {
    private val transitions = listOf(
        Triple(TicketStatus.ABIERTO, TicketStatus.EN_PROGRESO, "Tomar solicitud"),
        Triple(TicketStatus.EN_PROGRESO, TicketStatus.EN_REVISION, "Enviar a revisión"),
        Triple(TicketStatus.EN_REVISION, TicketStatus.RESUELTO, "Marcar como resuelta"),
        Triple(TicketStatus.EN_REVISION, TicketStatus.EN_PROGRESO, "Devolver a en proceso"),
        Triple(TicketStatus.RESUELTO, TicketStatus.CERRADO, "Cerrar solicitud"),
        Triple(TicketStatus.RESUELTO, TicketStatus.ABIERTO, "Reabrir solicitud"),
    )

    fun available(user: User?, ticket: Ticket): List<TicketAction> =
        transitions
            .filter { (from, target, _) -> from == ticket.status && AccessPolicy.canTransition(user, ticket, target) }
            .map { (_, target, label) -> TicketAction(label, target) }
}
