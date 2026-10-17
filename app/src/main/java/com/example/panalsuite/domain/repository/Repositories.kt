package com.example.panalsuite.domain.repository

import com.example.panalsuite.domain.model.Comment
import com.example.panalsuite.domain.model.Evidence
import com.example.panalsuite.domain.model.Priority
import com.example.panalsuite.domain.model.Role
import com.example.panalsuite.domain.model.SimulatedParameters
import com.example.panalsuite.domain.model.Ticket
import com.example.panalsuite.domain.model.TicketHistoryEntry
import com.example.panalsuite.domain.model.TicketStatus
import com.example.panalsuite.domain.model.TicketSummary
import com.example.panalsuite.domain.model.User
import com.example.panalsuite.domain.model.WorkloadItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/** Autenticación local simulada con usuarios ficticios. No existe servidor ni seguridad real. */
interface AuthRepository {
    val currentUser: StateFlow<User?>
    suspend fun login(username: String, password: String): Result<User>
    fun logout()
}

/**
 * Todas las operaciones validan la sesión y el rol actuales. Las lecturas no autorizadas devuelven
 * datos vacíos y las escrituras no autorizadas fallan con PermissionDeniedException.
 */
interface TicketRepository {
    fun observeTickets(): Flow<List<Ticket>>
    fun observeTicket(id: String): Flow<Ticket?>
    fun observeComments(ticketId: String): Flow<List<Comment>>
    fun observeEvidence(ticketId: String): Flow<List<Evidence>>
    fun observeHistory(ticketId: String): Flow<List<TicketHistoryEntry>>
    fun observeSummary(): Flow<TicketSummary>
    fun observeWorkload(): Flow<List<WorkloadItem>>
    suspend fun refresh(): Result<Unit>
    suspend fun createTicket(
        title: String,
        description: String,
        priority: Priority,
        flowType: String = "Soporte TI",
        category: String = "General",
        referenceLocation: String = "Sede Central",
        resolvingTeam: String = "Mesa de Ayuda TI",
    ): Result<Ticket>
    suspend fun updateStatus(ticketId: String, target: TicketStatus): Result<Unit>
    suspend fun addComment(ticketId: String, text: String): Result<Unit>
    suspend fun addEvidence(ticketId: String, fileName: String): Result<Unit>
}

interface UserAdminRepository {
    fun observeUsers(): Flow<List<User>>
    fun observeParameters(): Flow<SimulatedParameters>
    suspend fun createTestUser(username: String, role: Role): Result<User>
    suspend fun updateRole(userId: String, role: Role): Result<Unit>
    suspend fun setActive(userId: String, active: Boolean): Result<Unit>
    suspend fun updateParameters(parameters: SimulatedParameters): Result<Unit>
}
