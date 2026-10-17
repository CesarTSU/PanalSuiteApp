package com.example.panalsuite.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.panalsuite.domain.model.Priority
import com.example.panalsuite.domain.model.SyncState
import com.example.panalsuite.domain.model.Ticket
import com.example.panalsuite.domain.model.TicketStatus

@Entity(tableName = "tickets")
data class TicketEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val flowType: String,
    val category: String,
    val status: String,
    val priority: String,
    val createdAt: Long,
    val updatedAt: Long,
    val referenceLocation: String,
    val creatorId: String,
    val creatorName: String,
    val resolvingTeam: String,
    val assigneeId: String?,
    val assigneeName: String?,
    val syncState: String,
)

fun TicketEntity.toDomain() = Ticket(
    id = id,
    title = title,
    description = description,
    flowType = flowType,
    category = category,
    status = TicketStatus.valueOf(status),
    priority = Priority.valueOf(priority),
    createdAt = createdAt,
    updatedAt = updatedAt,
    referenceLocation = referenceLocation,
    creatorId = creatorId,
    creatorName = creatorName,
    resolvingTeam = resolvingTeam,
    assigneeId = assigneeId,
    assigneeName = assigneeName,
    syncState = SyncState.valueOf(syncState),
)

fun Ticket.toEntity() = TicketEntity(
    id = id,
    title = title,
    description = description,
    flowType = flowType,
    category = category,
    status = status.name,
    priority = priority.name,
    createdAt = createdAt,
    updatedAt = updatedAt,
    referenceLocation = referenceLocation,
    creatorId = creatorId,
    creatorName = creatorName,
    resolvingTeam = resolvingTeam,
    assigneeId = assigneeId,
    assigneeName = assigneeName,
    syncState = syncState.name,
)
