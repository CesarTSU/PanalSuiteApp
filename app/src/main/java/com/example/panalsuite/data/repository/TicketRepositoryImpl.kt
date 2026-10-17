package com.example.panalsuite.data.repository

import com.example.panalsuite.data.fake.FakeTicketDataSource
import com.example.panalsuite.data.local.TicketDao
import com.example.panalsuite.data.local.toDomain
import com.example.panalsuite.data.local.toEntity
import com.example.panalsuite.domain.model.Comment
import com.example.panalsuite.domain.model.Evidence
import com.example.panalsuite.domain.model.Priority
import com.example.panalsuite.domain.model.SyncState
import com.example.panalsuite.domain.model.Ticket
import com.example.panalsuite.domain.model.TicketHistoryEntry
import com.example.panalsuite.domain.model.TicketStatus
import com.example.panalsuite.domain.model.TicketSummary
import com.example.panalsuite.domain.model.WorkloadItem
import com.example.panalsuite.domain.model.toSummary
import com.example.panalsuite.domain.model.toWorkload
import com.example.panalsuite.domain.repository.AuthRepository
import com.example.panalsuite.domain.repository.TicketRepository
import com.example.panalsuite.domain.security.AccessPolicy
import com.example.panalsuite.domain.security.Permission
import com.example.panalsuite.domain.security.PermissionDeniedException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class TicketRepositoryImpl(
    private val dao: TicketDao,
    private val source: FakeTicketDataSource,
    private val auth: AuthRepository,
) : TicketRepository {

    override fun observeTickets(): Flow<List<Ticket>> =
        combine(dao.observeAll(), auth.currentUser) { list, user ->
            list.map { it.toDomain() }.filter { AccessPolicy.canViewTicket(user, it) }
        }

    override fun observeTicket(id: String): Flow<Ticket?> =
        combine(dao.observeById(id), auth.currentUser) { entity, user ->
            entity?.toDomain()?.takeIf { AccessPolicy.canViewTicket(user, it) }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeComments(ticketId: String): Flow<List<Comment>> =
        observeTicket(ticketId).flatMapLatest { ticket ->
            if (ticket == null) flowOf(emptyList()) else source.observeComments(ticketId)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeEvidence(ticketId: String): Flow<List<Evidence>> =
        observeTicket(ticketId).flatMapLatest { ticket ->
            if (ticket == null) flowOf(emptyList()) else source.observeEvidence(ticketId)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeHistory(ticketId: String): Flow<List<TicketHistoryEntry>> =
        observeTicket(ticketId).flatMapLatest { ticket ->
            if (ticket == null) flowOf(emptyList()) else source.observeHistory(ticketId)
        }

    override fun observeSummary(): Flow<TicketSummary> =
        combine(dao.observeAll(), auth.currentUser) { list, user ->
            val tickets = list.map { it.toDomain() }
            when {
                user == null -> TicketSummary()
                AccessPolicy.has(user, Permission.VIEW_INDICATORS) -> tickets.toSummary()
                else -> tickets.filter { AccessPolicy.canViewTicket(user, it) }.toSummary()
            }
        }

    override fun observeWorkload(): Flow<List<WorkloadItem>> =
        combine(dao.observeAll(), auth.currentUser) { list, user ->
            if (AccessPolicy.has(user, Permission.VIEW_WORKLOAD)) list.map { it.toDomain() }.toWorkload()
            else emptyList()
        }

    override suspend fun refresh(): Result<Unit> = runCatching {
        if (dao.count() == 0) {
            dao.upsertAll(source.fetchTickets().map { it.toEntity() })
        }
    }

    override suspend fun createTicket(
        title: String,
        description: String,
        priority: Priority,
        flowType: String,
        category: String,
        referenceLocation: String,
        resolvingTeam: String,
    ): Result<Ticket> {
        val user = auth.currentUser.value
        if (!AccessPolicy.has(user, Permission.CREATE_TICKET) || user == null) {
            return denied("Tu rol no puede crear solicitudes.")
        }
        val cleanTitle = title.trim()
        val cleanDescription = description.trim()
        val cleanFlowType = flowType.trim().ifEmpty { "Soporte TI" }
        val cleanCategory = category.trim().ifEmpty { "General" }
        val cleanLocation = referenceLocation.trim().ifEmpty { "Sede Central" }
        val cleanTeam = resolvingTeam.trim().ifEmpty { "Mesa de Ayuda TI" }

        if (cleanTitle.length !in 5..80) return invalid("El título debe tener entre 5 y 80 caracteres.")
        if (cleanDescription.length !in 10..500) return invalid("La descripción debe tener entre 10 y 500 caracteres.")

        val now = System.currentTimeMillis()
        val id = "T-%03d".format(dao.count() + 1)
        val ticket = Ticket(
            id = id,
            title = cleanTitle,
            description = cleanDescription,
            flowType = cleanFlowType,
            category = cleanCategory,
            status = TicketStatus.ABIERTO,
            priority = priority,
            createdAt = now,
            updatedAt = now,
            referenceLocation = cleanLocation,
            creatorId = user.id,
            creatorName = user.displayName,
            resolvingTeam = cleanTeam,
            assigneeId = null,
            assigneeName = null,
            syncState = SyncState.PENDING,
        )
        dao.upsertAll(listOf(ticket.toEntity()))
        source.addHistoryEntry(id, user.displayName, "Solicitud creada en estado Abierto")
        return Result.success(ticket)
    }

    override suspend fun updateStatus(ticketId: String, target: TicketStatus): Result<Unit> {
        val user = auth.currentUser.value ?: return denied("No hay una sesión activa.")
        val ticket = dao.getById(ticketId)?.toDomain()
        if (ticket == null || !AccessPolicy.canViewTicket(user, ticket)) {
            return denied("No tienes acceso a esta solicitud.")
        }
        if (!AccessPolicy.canTransition(user, ticket, target)) {
            return denied("Tu rol no puede aplicar ese cambio de estado.")
        }
        val (assigneeId, assigneeName) = when {
            ticket.status == TicketStatus.ABIERTO && target == TicketStatus.EN_PROGRESO -> user.id to user.displayName
            target == TicketStatus.ABIERTO -> null to null
            else -> ticket.assigneeId to ticket.assigneeName
        }
        dao.updateStatus(ticketId, target.name, assigneeId, assigneeName, System.currentTimeMillis())
        source.addHistoryEntry(ticketId, user.displayName, "Estado cambiado de ${ticket.status.name} a ${target.name}")
        return Result.success(Unit)
    }

    override suspend fun addComment(ticketId: String, text: String): Result<Unit> {
        val user = auth.currentUser.value ?: return denied("No hay una sesión activa.")
        val ticket = dao.getById(ticketId)?.toDomain()
        if (ticket == null || !AccessPolicy.canComment(user, ticket)) {
            return denied("No puedes comentar esta solicitud.")
        }
        val clean = text.trim()
        if (clean.isEmpty() || clean.length > 500) return invalid("El comentario debe tener entre 1 y 500 caracteres.")
        source.addComment(ticketId, user.displayName, clean)
        source.addHistoryEntry(ticketId, user.displayName, "Comentario publicado: $clean")
        return Result.success(Unit)
    }

    override suspend fun addEvidence(ticketId: String, fileName: String): Result<Unit> {
        val user = auth.currentUser.value ?: return denied("No hay una sesión activa.")
        val ticket = dao.getById(ticketId)?.toDomain()
        if (ticket == null || !AccessPolicy.canAddEvidence(user, ticket)) {
            return denied("No puedes adjuntar evidencias a esta solicitud.")
        }
        val clean = fileName.trim()
        if (clean.isEmpty() || clean.length > 100) return invalid("Nombre de evidencia inválido.")
        source.addEvidence(ticketId, clean, user.displayName)
        source.addHistoryEntry(ticketId, user.displayName, "Evidencia adjuntada ($clean)")
        return Result.success(Unit)
    }

    private fun <T> denied(message: String): Result<T> = Result.failure(PermissionDeniedException(message))

    private fun <T> invalid(message: String): Result<T> = Result.failure(IllegalArgumentException(message))
}
