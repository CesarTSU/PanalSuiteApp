package com.example.panalsuite.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.panalsuite.domain.model.Comment
import com.example.panalsuite.domain.model.Evidence
import com.example.panalsuite.domain.model.Ticket
import com.example.panalsuite.domain.model.TicketAction
import com.example.panalsuite.domain.model.TicketHistoryEntry
import com.example.panalsuite.domain.repository.AuthRepository
import com.example.panalsuite.domain.repository.TicketRepository
import com.example.panalsuite.domain.usecase.AddCommentUseCase
import com.example.panalsuite.domain.usecase.AddEvidenceUseCase
import com.example.panalsuite.domain.usecase.GetTicketDetailUseCase
import com.example.panalsuite.domain.usecase.UpdateTicketStatusUseCase
import com.example.panalsuite.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailData(
    val ticket: Ticket,
    val comments: List<Comment>,
    val evidence: List<Evidence>,
    val history: List<TicketHistoryEntry>,
    val actions: List<TicketAction>,
    val canComment: Boolean,
    val canAddEvidence: Boolean,
)

data class DetailUiState(
    val content: UiState<DetailData> = UiState.Loading,
    val draft: String = "",
    val busy: Boolean = false,
    val message: String? = null,
)

class TicketDetailViewModel(
    savedStateHandle: SavedStateHandle,
    repository: TicketRepository,
    auth: AuthRepository,
    getTicketDetailUseCase: GetTicketDetailUseCase = GetTicketDetailUseCase(repository, auth),
    private val updateTicketStatusUseCase: UpdateTicketStatusUseCase = UpdateTicketStatusUseCase(repository),
    private val addCommentUseCase: AddCommentUseCase = AddCommentUseCase(repository),
    private val addEvidenceUseCase: AddEvidenceUseCase = AddEvidenceUseCase(repository),
) : ViewModel() {
    private val ticketId: String = checkNotNull(savedStateHandle["ticketId"])

    private data class Local(val draft: String = "", val busy: Boolean = false, val message: String? = null)

    private val local = MutableStateFlow(Local())

    val state: StateFlow<DetailUiState> = combine(
        getTicketDetailUseCase(ticketId),
        local,
    ) { detailData, l ->
        val content = if (detailData == null) {
            UiState.Error("No se encontró la solicitud o no tienes permiso para verla.")
        } else {
            UiState.Success(
                DetailData(
                ticket = detailData.ticket,
                comments = detailData.comments,
                evidence = detailData.evidence,
                history = detailData.history,
                actions = detailData.actions,
                canComment = detailData.canComment,
                canAddEvidence = detailData.canAddEvidence,
            ),
            )
        }
        DetailUiState(content, l.draft, l.busy, l.message)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetailUiState())

    fun onDraftChange(value: String) = local.update { it.copy(draft = value) }

    fun onMessageShown() = local.update { it.copy(message = null) }

    fun performAction(action: TicketAction) {
        viewModelScope.launch {
            local.update { it.copy(busy = true) }
            val result = updateTicketStatusUseCase(ticketId, action.target)
            local.update {
                it.copy(
                    busy = false,
                    message = result.fold({ "Acción aplicada: ${action.label}" }, { e -> e.message ?: "No se pudo aplicar la acción." }),
                )
            }
        }
    }

    fun sendComment() {
        val text = local.value.draft
        if (text.isBlank()) return
        viewModelScope.launch {
            local.update { it.copy(busy = true) }
            val result = addCommentUseCase(ticketId, text)
            local.update {
                if (result.isSuccess) it.copy(busy = false, draft = "", message = "Comentario publicado")
                else it.copy(busy = false, message = result.exceptionOrNull()?.message ?: "No se pudo publicar.")
            }
        }
    }

    fun addEvidence() {
        viewModelScope.launch {
            local.update { it.copy(busy = true) }
            val number = (state.value.content as? UiState.Success)?.data?.evidence?.size?.plus(1) ?: 1
            val result = addEvidenceUseCase(ticketId, "evidencia_simulada_$number.png")
            local.update {
                it.copy(
                    busy = false,
                    message = result.fold({ "Evidencia simulada adjuntada" }, { e -> e.message ?: "No se pudo adjuntar." }),
                )
            }
        }
    }
}
