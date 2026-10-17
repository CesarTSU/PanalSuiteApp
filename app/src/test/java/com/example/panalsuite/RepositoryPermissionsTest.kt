package com.example.panalsuite

import com.example.panalsuite.data.fake.FakeTicketDataSource
import com.example.panalsuite.data.fake.FakeUserDataSource
import com.example.panalsuite.data.repository.AuthRepositoryImpl
import com.example.panalsuite.data.repository.TicketRepositoryImpl
import com.example.panalsuite.data.repository.UserAdminRepositoryImpl
import com.example.panalsuite.domain.model.Priority
import com.example.panalsuite.domain.model.Role
import com.example.panalsuite.domain.model.TicketStatus
import com.example.panalsuite.domain.security.PermissionDeniedException
import com.example.panalsuite.fakes.FakeTicketDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RepositoryPermissionsTest {
    private val users = FakeUserDataSource()
    private val auth = AuthRepositoryImpl(users)
    private val repo = TicketRepositoryImpl(FakeTicketDao(), FakeTicketDataSource(), auth)
    private val admin = UserAdminRepositoryImpl(users, auth)

    private suspend fun loginAs(name: String) {
        assertTrue(auth.login(name, "demo123").isSuccess)
        repo.refresh()
    }

    private fun <T> Result<T>.isDenied() = exceptionOrNull() is PermissionDeniedException

    @Test fun wrongPasswordFails() = runTest {
        assertTrue(auth.login("solicitante", "mala").isFailure)
    }

    @Test fun solicitanteSeesOnlyOwnTickets() = runTest {
        loginAs("solicitante")
        val list = repo.observeTickets().first()
        assertTrue(list.isNotEmpty())
        assertTrue(list.all { it.creatorId == "U-01" })
    }

    @Test fun supervisorSeesAllAndWorkload() = runTest {
        loginAs("supervisor")
        assertEquals(7, repo.observeTickets().first().size)
        assertTrue(repo.observeWorkload().first().isNotEmpty())
    }

    @Test fun solicitanteHasNoWorkload() = runTest {
        loginAs("solicitante")
        assertTrue(repo.observeWorkload().first().isEmpty())
    }

    @Test fun gerenciaSeesSummaryButNoTickets() = runTest {
        loginAs("gerencia")
        assertTrue(repo.observeTickets().first().isEmpty())
        val s = repo.observeSummary().first()
        assertTrue(s.pending + s.inProgress + s.resolved > 0)
    }

    @Test fun adminSeesNoTickets() = runTest {
        loginAs("administrador")
        assertTrue(repo.observeTickets().first().isEmpty())
    }

    @Test fun solicitanteCanCreateButGerenciaCannot() = runTest {
        loginAs("solicitante")
        assertTrue(repo.createTicket("Titulo valido", "Descripcion suficientemente larga", Priority.ALTA).isSuccess)
        loginAs("gerencia")
        assertTrue(repo.createTicket("Titulo valido", "Descripcion suficientemente larga", Priority.ALTA).isDenied())
    }

    @Test fun solicitanteCannotTakeTicket() = runTest {
        loginAs("solicitante")
        assertTrue(repo.updateStatus("T-001", TicketStatus.EN_PROGRESO).isDenied())
    }

    @Test fun resolutorTakesUnassignedTicket() = runTest {
        loginAs("resolutor")
        assertTrue(repo.updateStatus("T-001", TicketStatus.EN_PROGRESO).isSuccess)
        val t = repo.observeTicket("T-001").first()!!
        assertEquals(TicketStatus.EN_PROGRESO, t.status)
        assertEquals("U-02", t.assigneeId)
    }

    @Test fun resolutorCannotChangeOthersTicket() = runTest {
        loginAs("resolutor")
        assertTrue(repo.updateStatus("T-005", TicketStatus.RESUELTO).isDenied())
        assertTrue(repo.addComment("T-005", "hola").isDenied())
    }

    @Test fun resolutorCommentsAndAddsEvidenceOnOwnTicket() = runTest {
        loginAs("resolutor")
        assertTrue(repo.addComment("T-002", "Avance").isSuccess)
        assertTrue(repo.addEvidence("T-002", "captura.png").isSuccess)
    }

    @Test fun supervisorCannotCommentNorChangeStatus() = runTest {
        loginAs("supervisor")
        assertTrue(repo.addComment("T-002", "x").isDenied())
        assertTrue(repo.updateStatus("T-002", TicketStatus.EN_REVISION).isDenied())
        assertTrue(repo.addEvidence("T-002", "a.png").isDenied())
    }

    @Test fun noSessionIsDenied() = runTest {
        auth.logout()
        assertTrue(repo.updateStatus("T-001", TicketStatus.EN_PROGRESO).isDenied())
        assertTrue(repo.createTicket("Titulo valido", "Descripcion suficientemente larga", Priority.BAJA).isDenied())
    }

    @Test fun nonAdminCannotManageUsers() = runTest {
        loginAs("supervisor")
        assertTrue(admin.createTestUser("nuevo", Role.RESOLUTOR).isDenied())
        assertTrue(admin.observeUsers().first().isEmpty())
    }

    @Test fun adminManagesUsersButNotSelf() = runTest {
        loginAs("administrador")
        val created = admin.createTestUser("nuevo", Role.RESOLUTOR).getOrThrow()
        assertTrue(admin.updateRole(created.id, Role.SUPERVISOR).isSuccess)
        assertTrue(admin.updateRole("U-05", Role.SOLICITANTE).isFailure)
        assertTrue(admin.setActive("U-05", false).isFailure)
        assertTrue(admin.setActive(created.id, false).isSuccess)
        auth.logout()
        assertTrue(auth.login("nuevo", "demo123").isFailure)
    }
}
