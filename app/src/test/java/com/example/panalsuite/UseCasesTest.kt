package com.example.panalsuite

import com.example.panalsuite.data.fake.FakeTicketDataSource
import com.example.panalsuite.data.fake.FakeUserDataSource
import com.example.panalsuite.data.repository.AuthRepositoryImpl
import com.example.panalsuite.data.repository.TicketRepositoryImpl
import com.example.panalsuite.domain.model.Priority
import com.example.panalsuite.domain.model.TicketStatus
import com.example.panalsuite.domain.usecase.AddCommentUseCase
import com.example.panalsuite.domain.usecase.AddEvidenceUseCase
import com.example.panalsuite.domain.usecase.CreateTicketUseCase
import com.example.panalsuite.domain.usecase.GetTicketDetailUseCase
import com.example.panalsuite.domain.usecase.GetTicketsUseCase
import com.example.panalsuite.domain.usecase.StatusFilter
import com.example.panalsuite.domain.usecase.UpdateTicketStatusUseCase
import com.example.panalsuite.fakes.FakeTicketDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UseCasesTest {
    private val userSource = FakeUserDataSource()
    private val auth = AuthRepositoryImpl(userSource)
    private val repo = TicketRepositoryImpl(FakeTicketDao(), FakeTicketDataSource(), auth)

    private val getTickets = GetTicketsUseCase(repo)
    private val getTicketDetail = GetTicketDetailUseCase(repo, auth)
    private val createTicket = CreateTicketUseCase(repo)
    private val updateTicketStatus = UpdateTicketStatusUseCase(repo)
    private val addComment = AddCommentUseCase(repo)
    private val addEvidence = AddEvidenceUseCase(repo)

    @Before
    fun setUp() = runTest {
        auth.login("solicitante", "demo123")
        repo.refresh()
    }

    @Test
    fun createTicketUseCaseValidatesInputsAndCreatesTicketWithHistory() = runTest {
        val result = createTicket(
            title = "Nueva impresora defectuosa",
            description = "La impresora de la sala de reuniones no enciende.",
            priority = Priority.ALTA,
            flowType = "Soporte Hardware",
            category = "Periféricos",
            referenceLocation = "Piso 2 - Sala A",
            resolvingTeam = "Mesa de Ayuda TI",
        )

        assertTrue(result.isSuccess)
        val created = result.getOrThrow()
        assertEquals("Nueva impresora defectuosa", created.title)
        assertEquals("Soporte Hardware", created.flowType)
        assertEquals("Periféricos", created.category)
        assertEquals("Piso 2 - Sala A", created.referenceLocation)

        val detail = getTicketDetail(created.id).first()
        assertNotNull(detail)
        assertTrue(detail!!.history.any { it.action.contains("Solicitud creada") })
    }

    @Test
    fun createTicketFailsShortTitleOrDescription() = runTest {
        val shortTitle = createTicket("hola", "Descripcion suficientemente larga", Priority.BAJA)
        assertTrue(shortTitle.isFailure)

        val shortDesc = createTicket("Titulo valido de mas de 5 letras", "corta", Priority.BAJA)
        assertTrue(shortDesc.isFailure)
    }

    @Test
    fun statusChangeUpdatesStatusAndRecordsHistoryTraceability() = runTest {
        auth.login("resolutor", "demo123")
        val result = updateTicketStatus("T-001", TicketStatus.EN_PROGRESO)
        assertTrue(result.isSuccess)

        val detail = getTicketDetail("T-001").first()
        assertNotNull(detail)
        assertEquals(TicketStatus.EN_PROGRESO, detail!!.ticket.status)
        assertTrue(detail.history.any { it.action.contains("Estado cambiado de ABIERTO a EN_PROGRESO") })
    }

    @Test
    fun addCommentAndEvidenceRecordsHistoryTraceability() = runTest {
        auth.login("resolutor", "demo123")
        updateTicketStatus("T-001", TicketStatus.EN_PROGRESO)

        assertTrue(addComment("T-001", "Verificando logs del sistema.").isSuccess)
        assertTrue(addEvidence("T-001", "log_error.txt").isSuccess)

        val detail = getTicketDetail("T-001").first()
        assertNotNull(detail)
        assertTrue(detail!!.comments.any { it.text == "Verificando logs del sistema." })
        assertTrue(detail.evidence.any { it.fileName == "log_error.txt" })
        assertTrue(detail.history.any { it.action.contains("Comentario publicado") })
        assertTrue(detail.history.any { it.action.contains("Evidencia adjuntada") })
    }

    @Test
    fun searchAndFilterTicketsByStatusPriorityAndQuery() = runTest {
        auth.login("supervisor", "demo123")

        val allPending = getTickets(statusFilter = StatusFilter.PENDING).first()
        assertTrue(allPending.all { it.status == TicketStatus.ABIERTO })

        val highPriority = getTickets(priorityFilter = Priority.ALTA).first()
        assertTrue(highPriority.all { it.priority == Priority.ALTA })

        val queryMatches = getTickets(query = "reportes").first()
        assertTrue(queryMatches.isNotEmpty())
        assertTrue(queryMatches.all { it.title.lowercase().contains("reportes") || it.description.lowercase().contains("reportes") })
    }
}
