package com.example.panalsuite.data.fake

import com.example.panalsuite.domain.model.Comment
import com.example.panalsuite.domain.model.Evidence
import com.example.panalsuite.domain.model.Priority
import com.example.panalsuite.domain.model.Ticket
import com.example.panalsuite.domain.model.TicketHistoryEntry
import com.example.panalsuite.domain.model.TicketStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** Fuente de datos de prueba con información totalmente ficticia. */
class FakeTicketDataSource {
    private val now = System.currentTimeMillis()
    private val hour = 3_600_000L

    private val names = mapOf("U-01" to "Usuario 01", "U-02" to "Usuario 02", "U-06" to "Usuario 06", "U-07" to "Usuario 07")

    private val comments = MutableStateFlow(
        listOf(
            Comment("C-1", "T-001", "Usuario 01", "No puedo acceder al módulo de reportes desde la red corporativa.", now - 5 * hour),
            Comment("C-2", "T-002", "Usuario 02", "Reproduje el error con el filtro de fechas en el servidor de pruebas.", now - 8 * hour),
            Comment("C-3", "T-004", "Usuario 02", "Se aplicó el parche en el servidor SMTP y se verificó el envío.", now - 20 * hour),
        )
    )

    private val evidence = MutableStateFlow(
        listOf(
            Evidence("E-1", "T-002", "captura_filtro_fechas.png", "Usuario 02", now - 7 * hour),
        )
    )

    private val history = MutableStateFlow(
        listOf(
            TicketHistoryEntry("H-1", "T-001", "Usuario 01", "Solicitud creada en estado Abierto", now - 24 * hour),
            TicketHistoryEntry("H-2", "T-002", "Usuario 01", "Solicitud creada en estado Abierto", now - 18 * hour),
            TicketHistoryEntry("H-3", "T-002", "Usuario 02", "Estado cambiado a En Progreso (Asignado a Usuario 02)", now - 10 * hour),
            TicketHistoryEntry("H-4", "T-004", "Usuario 01", "Solicitud creada en estado Abierto", now - 48 * hour),
            TicketHistoryEntry("H-5", "T-004", "Usuario 02", "Estado cambiado a Resuelto", now - 22 * hour),
            TicketHistoryEntry("H-6", "T-005", "Usuario 06", "Solicitud creada en estado Abierto", now - 36 * hour),
            TicketHistoryEntry("H-7", "T-005", "Usuario 07", "Estado cambiado a En Revisión", now - 12 * hour),
        )
    )

    suspend fun fetchTickets(): List<Ticket> {
        delay(700)
        return listOf(
            ticket("T-001", "Acceso al módulo de reportes", "El usuario no puede ingresar al módulo de reportes desde ayer.", "Soporte TI", "Software", TicketStatus.ABIERTO, Priority.ALTA, "Edificio Principal - Piso 3", "U-01", "Mesa de Ayuda TI", null, 24, 1),
            ticket("T-002", "Error en filtro de fechas", "El reporte mensual falla al aplicar un rango de fechas en la pantalla principal.", "Soporte TI", "Software", TicketStatus.EN_PROGRESO, Priority.MEDIA, "Planta Sur - Oficina 12", "U-01", "Mesa de Ayuda TI", "U-02", 18, 4),
            ticket("T-003", "Solicitud de nuevo perfil", "Se requiere un perfil de solo lectura para la auditoría anual.", "Gestión de Accesos", "Seguridad", TicketStatus.ABIERTO, Priority.BAJA, "Sucursal Centro - Piso 1", "U-06", "Seguridad TI", null, 12, 6),
            ticket("T-004", "Caída del servicio de correo", "Las notificaciones por correo no se están enviando a los clientes.", "Infraestructura", "Redes", TicketStatus.RESUELTO, Priority.CRITICA, "Data Center - Servidor 04", "U-01", "Infraestructura TI", "U-02", 48, 22),
            ticket("T-005", "Actualización de datos maestros", "Cambiar la nomenclatura de áreas en el catálogo de productos.", "Mantenimiento", "Base de Datos", TicketStatus.EN_REVISION, Priority.MEDIA, "Edificio B - Oficina 204", "U-06", "Mesa de Ayuda TI", "U-07", 36, 12),
            ticket("T-006", "Consulta sobre facturación demo", "Duda sobre el formato del documento de prueba en el ambiente demo.", "Atención al Cliente", "Facturación", TicketStatus.CERRADO, Priority.BAJA, "Atención Remota", "U-06", "Atención al Cliente", "U-07", 60, 40),
            ticket("T-007", "Lentitud en carga de bandeja", "La bandeja tarda más de 10 segundos en cargar los registros iniciales.", "Soporte TI", "Software", TicketStatus.EN_PROGRESO, Priority.ALTA, "Edificio Principal - Piso 2", "U-01", "Mesa de Ayuda TI", "U-02", 8, 2),
        )
    }

    fun observeComments(ticketId: String): Flow<List<Comment>> =
        comments.map { list -> list.filter { it.ticketId == ticketId }.sortedBy { it.createdAt } }

    fun observeEvidence(ticketId: String): Flow<List<Evidence>> =
        evidence.map { list -> list.filter { it.ticketId == ticketId }.sortedBy { it.createdAt } }

    fun observeHistory(ticketId: String): Flow<List<TicketHistoryEntry>> =
        history.map { list -> list.filter { it.ticketId == ticketId }.sortedBy { it.timestamp } }

    suspend fun addComment(ticketId: String, authorName: String, text: String) {
        delay(300)
        comments.update { it + Comment("C-${System.nanoTime()}", ticketId, authorName, text, System.currentTimeMillis()) }
    }

    suspend fun addEvidence(ticketId: String, fileName: String, authorName: String) {
        delay(300)
        evidence.update { it + Evidence("E-${System.nanoTime()}", ticketId, fileName, authorName, System.currentTimeMillis()) }
    }

    suspend fun addHistoryEntry(ticketId: String, authorName: String, action: String) {
        delay(100)
        history.update { it + TicketHistoryEntry("H-${System.nanoTime()}", ticketId, authorName, action, System.currentTimeMillis()) }
    }

    private fun ticket(
        id: String,
        title: String,
        description: String,
        flowType: String,
        category: String,
        status: TicketStatus,
        priority: Priority,
        referenceLocation: String,
        creatorId: String,
        resolvingTeam: String,
        assigneeId: String?,
        createdHoursAgo: Long,
        updatedHoursAgo: Long,
    ) = Ticket(
        id = id,
        title = title,
        description = description,
        flowType = flowType,
        category = category,
        status = status,
        priority = priority,
        createdAt = now - createdHoursAgo * hour,
        updatedAt = now - updatedHoursAgo * hour,
        referenceLocation = referenceLocation,
        creatorId = creatorId,
        creatorName = names[creatorId] ?: creatorId,
        resolvingTeam = resolvingTeam,
        assigneeId = assigneeId,
        assigneeName = assigneeId?.let { names[it] },
    )
}
