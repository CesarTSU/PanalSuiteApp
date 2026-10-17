package com.example.panalsuite.domain.usecase

import com.example.panalsuite.domain.model.Comment
import com.example.panalsuite.domain.model.Evidence
import com.example.panalsuite.domain.model.Ticket
import com.example.panalsuite.domain.model.TicketAction
import com.example.panalsuite.domain.model.TicketActions
import com.example.panalsuite.domain.model.TicketHistoryEntry
import com.example.panalsuite.domain.repository.AuthRepository
import com.example.panalsuite.domain.repository.TicketRepository
import com.example.panalsuite.domain.security.AccessPolicy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class TicketDetailData(
    val ticket: Ticket,
    val comments: List<Comment>,
    val evidence: List<Evidence>,
    val history: List<TicketHistoryEntry>,
    val actions: List<TicketAction>,
    val canComment: Boolean,
    val canAddEvidence: Boolean,
)

class GetTicketDetailUseCase(
    private val ticketRepository: TicketRepository,
    private val authRepository: AuthRepository,
) {
    operator fun invoke(ticketId: String): Flow<TicketDetailData?> {
        return combine(
            ticketRepository.observeTicket(ticketId),
            ticketRepository.observeComments(ticketId),
            ticketRepository.observeEvidence(ticketId),
            ticketRepository.observeHistory(ticketId),
            authRepository.currentUser,
        ) { ticket, comments, evidence, history, user ->
            if (ticket == null) {
                null
            } else {
                TicketDetailData(
                    ticket = ticket,
                    comments = comments,
                    evidence = evidence,
                    history = history,
                    actions = TicketActions.available(user, ticket),
                    canComment = AccessPolicy.canComment(user, ticket),
                    canAddEvidence = AccessPolicy.canAddEvidence(user, ticket),
                )
            }
        }
    }
}
