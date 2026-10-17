package com.example.panalsuite.ui.workload

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.panalsuite.domain.model.WorkloadItem
import com.example.panalsuite.domain.repository.AuthRepository
import com.example.panalsuite.domain.repository.TicketRepository
import com.example.panalsuite.domain.security.AccessPolicy
import com.example.panalsuite.domain.security.Permission
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class WorkloadUiState(val allowed: Boolean, val items: List<WorkloadItem>)

class WorkloadViewModel(repository: TicketRepository, auth: AuthRepository) : ViewModel() {
    val state: StateFlow<WorkloadUiState> =
        combine(repository.observeWorkload(), auth.currentUser) { items, user ->
            WorkloadUiState(AccessPolicy.has(user, Permission.VIEW_WORKLOAD), items)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            WorkloadUiState(AccessPolicy.has(auth.currentUser.value, Permission.VIEW_WORKLOAD), emptyList()),
        )
}
