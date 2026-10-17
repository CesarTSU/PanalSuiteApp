package com.example.panalsuite.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.panalsuite.domain.model.Role
import com.example.panalsuite.domain.model.SimulatedParameters
import com.example.panalsuite.domain.model.User
import com.example.panalsuite.domain.repository.AuthRepository
import com.example.panalsuite.domain.repository.UserAdminRepository
import com.example.panalsuite.domain.security.AccessPolicy
import com.example.panalsuite.domain.security.Permission
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminUiState(
    val allowed: Boolean,
    val currentUserId: String?,
    val users: List<User>,
    val parameters: SimulatedParameters,
    val message: String? = null,
)

class AdminViewModel(
    private val repository: UserAdminRepository,
    auth: AuthRepository,
) : ViewModel() {
    private val message = MutableStateFlow<String?>(null)

    val state: StateFlow<AdminUiState> = combine(
        repository.observeUsers(),
        repository.observeParameters(),
        auth.currentUser,
        message,
    ) { users, params, me, msg ->
        AdminUiState(
            allowed = AccessPolicy.has(me, Permission.MANAGE_USERS),
            currentUserId = me?.id,
            users = users,
            parameters = params,
            message = msg,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        AdminUiState(AccessPolicy.has(auth.currentUser.value, Permission.MANAGE_USERS), null, emptyList(), SimulatedParameters()),
    )

    private var nextTestUser = 1

    fun onMessageShown() = message.update { null }

    fun createTestUser() = run("Usuario de prueba creado") {
        repository.createTestUser("prueba${nextTestUser++}", Role.SOLICITANTE).map { }
    }

    fun changeRole(userId: String, role: Role) = run("Perfil actualizado") { repository.updateRole(userId, role) }

    fun setActive(userId: String, active: Boolean) =
        run(if (active) "Usuario activado" else "Usuario desactivado") { repository.setActive(userId, active) }

    fun changeMaxActive(delta: Int) {
        val current = state.value.parameters
        run("Parámetro actualizado") {
            repository.updateParameters(current.copy(maxActivePerResolver = current.maxActivePerResolver + delta))
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        val current = state.value.parameters
        run("Parámetro actualizado") { repository.updateParameters(current.copy(notificationsEnabled = enabled)) }
    }

    private fun run(success: String, block: suspend () -> Result<Unit>) {
        viewModelScope.launch {
            message.value = block().fold({ success }, { it.message ?: "Operación no permitida." })
        }
    }
}
