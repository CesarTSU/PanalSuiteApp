package com.example.panalsuite.ui.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.panalsuite.domain.model.Priority
import com.example.panalsuite.domain.model.Role
import com.example.panalsuite.domain.model.TicketStatus
import com.example.panalsuite.ui.theme.CriticalColor
import com.example.panalsuite.ui.theme.InProgressColor
import com.example.panalsuite.ui.theme.PendingColor
import com.example.panalsuite.ui.theme.ResolvedColor

fun TicketStatus.label() = when (this) {
    TicketStatus.ABIERTO -> "Pendiente"
    TicketStatus.EN_PROGRESO -> "En proceso"
    TicketStatus.EN_REVISION -> "En revisión"
    TicketStatus.RESUELTO -> "Resuelta"
    TicketStatus.CERRADO -> "Cerrada"
}

fun TicketStatus.color(): Color = when (this) {
    TicketStatus.ABIERTO -> PendingColor
    TicketStatus.EN_PROGRESO, TicketStatus.EN_REVISION -> InProgressColor
    TicketStatus.RESUELTO, TicketStatus.CERRADO -> ResolvedColor
}

fun Priority.label() = when (this) {
    Priority.BAJA -> "Baja"
    Priority.MEDIA -> "Media"
    Priority.ALTA -> "Alta"
    Priority.CRITICA -> "Crítica"
}

@Composable
fun Priority.color(): Color = when (this) {
    Priority.BAJA -> MaterialTheme.colorScheme.onSurfaceVariant
    Priority.MEDIA -> InProgressColor
    Priority.ALTA -> PendingColor
    Priority.CRITICA -> CriticalColor
}

fun Role.label() = when (this) {
    Role.SOLICITANTE -> "Solicitante"
    Role.RESOLUTOR -> "Resolutor"
    Role.SUPERVISOR -> "Supervisor"
    Role.GERENCIA -> "Gerencia"
    Role.ADMINISTRADOR -> "Administrador"
}

fun relativeTime(millis: Long, now: Long = System.currentTimeMillis()): String {
    val minutes = ((now - millis) / 60_000).coerceAtLeast(0)
    return when {
        minutes < 1 -> "ahora"
        minutes < 60 -> "hace $minutes min"
        minutes < 60 * 24 -> "hace ${minutes / 60} h"
        else -> "hace ${minutes / (60 * 24)} d"
    }
}
