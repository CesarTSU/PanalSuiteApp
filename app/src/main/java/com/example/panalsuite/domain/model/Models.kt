package com.example.panalsuite.domain.model

enum class Role { SOLICITANTE, RESOLUTOR, SUPERVISOR, GERENCIA, ADMINISTRADOR }

enum class TicketStatus { ABIERTO, EN_PROGRESO, EN_REVISION, RESUELTO, CERRADO }

enum class Priority { BAJA, MEDIA, ALTA, CRITICA }

enum class SyncState { SYNCED, PENDING, FAILED }

data class User(
    val id: String,
    val username: String,
    val displayName: String,
    val role: Role,
    val active: Boolean = true,
)

data class Ticket(
    val id: String,
    val title: String,
    val description: String,
    val flowType: String = "Soporte TI",
    val category: String = "General",
    val priority: Priority,
    val status: TicketStatus,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val referenceLocation: String = "Sede Central",
    val creatorId: String,
    val creatorName: String = "Solicitante",
    val resolvingTeam: String = "Mesa de Ayuda TI",
    val assigneeId: String? = null,
    val assigneeName: String? = null,
    val syncState: SyncState = SyncState.SYNCED,
)

data class TicketHistoryEntry(
    val id: String,
    val ticketId: String,
    val authorName: String,
    val action: String,
    val timestamp: Long,
)

data class Comment(
    val id: String,
    val ticketId: String,
    val authorName: String,
    val text: String,
    val createdAt: Long,
)

data class Evidence(
    val id: String,
    val ticketId: String,
    val fileName: String,
    val authorName: String,
    val createdAt: Long,
)

data class TicketSummary(
    val pending: Int = 0,
    val inProgress: Int = 0,
    val resolved: Int = 0,
)

data class WorkloadItem(
    val assigneeId: String?,
    val assigneeName: String,
    val pending: Int,
    val inProgress: Int,
    val inReview: Int,
    val resolved: Int,
) {
    val active: Int get() = pending + inProgress + inReview
    val total: Int get() = active + resolved
}

/** Parámetros de demostración: no afectan ninguna regla real del sistema. */
data class SimulatedParameters(
    val maxActivePerResolver: Int = 5,
    val notificationsEnabled: Boolean = true,
)

fun List<Ticket>.toSummary() = TicketSummary(
    pending = count { it.status == TicketStatus.ABIERTO },
    inProgress = count { it.status == TicketStatus.EN_PROGRESO || it.status == TicketStatus.EN_REVISION },
    resolved = count { it.status == TicketStatus.RESUELTO || it.status == TicketStatus.CERRADO },
)

fun List<Ticket>.toWorkload(): List<WorkloadItem> =
    groupBy { it.assigneeId }
        .map { (id, list) ->
            WorkloadItem(
                assigneeId = id,
                assigneeName = list.firstNotNullOfOrNull { it.assigneeName } ?: "Sin asignar",
                pending = list.count { it.status == TicketStatus.ABIERTO },
                inProgress = list.count { it.status == TicketStatus.EN_PROGRESO },
                inReview = list.count { it.status == TicketStatus.EN_REVISION },
                resolved = list.count { it.status == TicketStatus.RESUELTO || it.status == TicketStatus.CERRADO },
            )
        }
        .sortedByDescending { it.active }
