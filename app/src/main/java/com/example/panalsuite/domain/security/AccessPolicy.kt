package com.example.panalsuite.domain.security

import com.example.panalsuite.domain.model.Role
import com.example.panalsuite.domain.model.Ticket
import com.example.panalsuite.domain.model.TicketStatus
import com.example.panalsuite.domain.model.User

enum class Permission {
    CREATE_TICKET,
    VIEW_OWN_TICKETS,
    VIEW_ASSIGNED_TICKETS,
    VIEW_ALL_TICKETS,
    CHANGE_STATUS,
    COMMENT,
    ADD_EVIDENCE,
    VIEW_WORKLOAD,
    VIEW_INDICATORS,
    MANAGE_USERS,
    MANAGE_PARAMETERS,
}

object RolePermissions {
    private val matrix: Map<Role, Set<Permission>> = mapOf(
        Role.SOLICITANTE to setOf(
            Permission.CREATE_TICKET,
            Permission.VIEW_OWN_TICKETS,
            Permission.CHANGE_STATUS,
            Permission.COMMENT,
        ),
        Role.RESOLUTOR to setOf(
            Permission.VIEW_ASSIGNED_TICKETS,
            Permission.CHANGE_STATUS,
            Permission.COMMENT,
            Permission.ADD_EVIDENCE,
        ),
        Role.SUPERVISOR to setOf(
            Permission.VIEW_ALL_TICKETS,
            Permission.VIEW_WORKLOAD,
            Permission.VIEW_INDICATORS,
        ),
        Role.GERENCIA to setOf(
            Permission.VIEW_INDICATORS,
        ),
        Role.ADMINISTRADOR to setOf(
            Permission.MANAGE_USERS,
            Permission.MANAGE_PARAMETERS,
            Permission.VIEW_INDICATORS,
        ),
    )

    fun of(role: Role): Set<Permission> = matrix.getValue(role)
}

class PermissionDeniedException(message: String) : Exception(message)

/**
 * Reglas de autorización puras. Los repositorios las aplican antes de leer o modificar datos.
 * Es una simulación local para fines académicos: no sustituye la validación de un servidor real.
 */
object AccessPolicy {

    fun has(user: User?, permission: Permission): Boolean =
        user != null && user.active && permission in RolePermissions.of(user.role)

    fun canViewTicket(user: User?, ticket: Ticket): Boolean = when {
        user == null || !user.active -> false
        has(user, Permission.VIEW_ALL_TICKETS) -> true
        has(user, Permission.VIEW_OWN_TICKETS) && ticket.creatorId == user.id -> true
        has(user, Permission.VIEW_ASSIGNED_TICKETS) &&
            (ticket.assigneeId == user.id || ticket.assigneeId == null) -> true
        else -> false
    }

    fun canTransition(user: User?, ticket: Ticket, target: TicketStatus): Boolean {
        if (user == null || !has(user, Permission.CHANGE_STATUS) || !canViewTicket(user, ticket)) return false
        val from = ticket.status
        return when (user.role) {
            Role.RESOLUTOR -> {
                val mine = ticket.assigneeId == user.id
                when {
                    from == TicketStatus.ABIERTO && target == TicketStatus.EN_PROGRESO ->
                        ticket.assigneeId == null || mine
                    from == TicketStatus.EN_PROGRESO && target == TicketStatus.EN_REVISION -> mine
                    from == TicketStatus.EN_REVISION &&
                        (target == TicketStatus.EN_PROGRESO || target == TicketStatus.RESUELTO) -> mine
                    else -> false
                }
            }
            Role.SOLICITANTE ->
                ticket.creatorId == user.id &&
                    from == TicketStatus.RESUELTO &&
                    (target == TicketStatus.CERRADO || target == TicketStatus.ABIERTO)
            else -> false
        }
    }

    fun canComment(user: User?, ticket: Ticket): Boolean =
        user != null && has(user, Permission.COMMENT) && canViewTicket(user, ticket) &&
            ticket.status != TicketStatus.CERRADO && isInvolved(user, ticket)

    fun canAddEvidence(user: User?, ticket: Ticket): Boolean =
        user != null && has(user, Permission.ADD_EVIDENCE) && canViewTicket(user, ticket) &&
            ticket.status != TicketStatus.CERRADO && isInvolved(user, ticket)

    private fun isInvolved(user: User, ticket: Ticket) = when (user.role) {
        Role.SOLICITANTE -> ticket.creatorId == user.id
        Role.RESOLUTOR -> ticket.assigneeId == user.id
        else -> false
    }
}
