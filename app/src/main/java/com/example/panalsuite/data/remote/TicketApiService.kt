package com.example.panalsuite.data.remote

import com.example.panalsuite.domain.model.Priority
import com.example.panalsuite.domain.model.SyncState
import com.example.panalsuite.domain.model.Ticket
import com.example.panalsuite.domain.model.TicketStatus
import kotlinx.serialization.Serializable
import retrofit2.http.GET

@Serializable
data class TicketDto(
    val id: String,
    val title: String,
    val description: String,
    val status: String,
    val priority: String = "MEDIA",
    val creatorId: String = "",
    val assigneeId: String? = null,
    val assigneeName: String? = null,
    val updatedAt: Long,
)

fun TicketDto.toDomain() = Ticket(
    id = id,
    title = title,
    description = description,
    status = TicketStatus.valueOf(status),
    priority = Priority.valueOf(priority),
    creatorId = creatorId,
    assigneeId = assigneeId,
    assigneeName = assigneeName,
    updatedAt = updatedAt,
    syncState = SyncState.SYNCED,
)

interface TicketApiService {
    @GET("tickets")
    suspend fun getTickets(): List<TicketDto>
}
