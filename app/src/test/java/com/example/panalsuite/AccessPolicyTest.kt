package com.example.panalsuite

import com.example.panalsuite.domain.model.Role
import com.example.panalsuite.domain.model.Ticket
import com.example.panalsuite.domain.model.TicketStatus
import com.example.panalsuite.domain.model.Priority
import com.example.panalsuite.domain.model.User
import com.example.panalsuite.domain.security.AccessPolicy
import com.example.panalsuite.domain.security.Permission
import com.example.panalsuite.domain.security.RolePermissions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AccessPolicyTest {
    private fun user(id: String, role: Role) = User(id, id, id, role, true)
    private fun ticket(status: TicketStatus, creator: String = "req", assignee: String? = null) = Ticket(
        id = "T-1",
        title = "Titulo",
        description = "Descripcion",
        priority = Priority.MEDIA,
        status = status,
        creatorId = creator,
        assigneeId = assignee,
        assigneeName = assignee,
    )

    private val req = user("req", Role.SOLICITANTE)
    private val res = user("res", Role.RESOLUTOR)
    private val sup = user("sup", Role.SUPERVISOR)
    private val ger = user("ger", Role.GERENCIA)
    private val adm = user("adm", Role.ADMINISTRADOR)

    @Test fun solicitanteCreatesButCannotManage() {
        assertTrue(RolePermissions.of(Role.SOLICITANTE).contains(Permission.CREATE_TICKET))
        assertFalse(RolePermissions.of(Role.SOLICITANTE).contains(Permission.MANAGE_USERS))
    }

    @Test fun onlyAdminManagesUsersAndParameters() {
        Role.entries.filter { it != Role.ADMINISTRADOR }.forEach {
            assertFalse(RolePermissions.of(it).contains(Permission.MANAGE_USERS))
            assertFalse(RolePermissions.of(it).contains(Permission.MANAGE_PARAMETERS))
        }
        assertTrue(AccessPolicy.has(adm, Permission.MANAGE_USERS))
    }

    @Test fun noSessionHasNoPermissions() {
        Permission.entries.forEach { assertFalse(AccessPolicy.has(null, it)) }
    }

    @Test fun solicitanteSeesOnlyOwnTickets() {
        assertTrue(AccessPolicy.canViewTicket(req, ticket(TicketStatus.ABIERTO)))
        assertFalse(AccessPolicy.canViewTicket(req, ticket(TicketStatus.ABIERTO, creator = "other")))
    }

    @Test fun supervisorSeesAllButGerenciaAndAdminSeeNone() {
        val t = ticket(TicketStatus.ABIERTO, creator = "other")
        assertTrue(AccessPolicy.canViewTicket(sup, t))
        assertFalse(AccessPolicy.canViewTicket(ger, t))
        assertFalse(AccessPolicy.canViewTicket(adm, t))
    }

    @Test fun resolutorTransitionsOnlyOwnTickets() {
        assertTrue(AccessPolicy.canTransition(res, ticket(TicketStatus.ABIERTO), TicketStatus.EN_PROGRESO))
        assertTrue(AccessPolicy.canTransition(res, ticket(TicketStatus.EN_PROGRESO, assignee = "res"), TicketStatus.EN_REVISION))
        assertFalse(AccessPolicy.canTransition(res, ticket(TicketStatus.EN_PROGRESO, assignee = "x"), TicketStatus.EN_REVISION))
        assertFalse(AccessPolicy.canTransition(res, ticket(TicketStatus.ABIERTO), TicketStatus.CERRADO))
    }

    @Test fun solicitanteCannotStartWork() {
        assertFalse(AccessPolicy.canTransition(req, ticket(TicketStatus.ABIERTO), TicketStatus.EN_PROGRESO))
    }

    @Test fun readOnlyRolesCannotCommentOrAddEvidence() {
        val t = ticket(TicketStatus.EN_PROGRESO, assignee = "res")
        listOf(sup, ger, adm).forEach {
            assertFalse(AccessPolicy.canComment(it, t))
            assertFalse(AccessPolicy.canAddEvidence(it, t))
        }
    }

    @Test fun evidenceOnlyForAssignedResolutor() {
        assertTrue(AccessPolicy.canAddEvidence(res, ticket(TicketStatus.EN_PROGRESO, assignee = "res")))
        assertFalse(AccessPolicy.canAddEvidence(res, ticket(TicketStatus.EN_PROGRESO, assignee = "x")))
        assertFalse(AccessPolicy.canAddEvidence(req, ticket(TicketStatus.EN_PROGRESO)))
    }

    @Test fun indicatorsForSupervisorGerenciaAdmin() {
        assertEquals(
            setOf(Role.SUPERVISOR, Role.GERENCIA, Role.ADMINISTRADOR),
            Role.entries.filter { RolePermissions.of(it).contains(Permission.VIEW_INDICATORS) }.toSet(),
        )
    }
}
