package com.example.panalsuite.data.repository

import com.example.panalsuite.data.fake.FakeUserDataSource
import com.example.panalsuite.domain.model.Role
import com.example.panalsuite.domain.model.SimulatedParameters
import com.example.panalsuite.domain.model.User
import com.example.panalsuite.domain.repository.AuthRepository
import com.example.panalsuite.domain.repository.UserAdminRepository
import com.example.panalsuite.domain.security.AccessPolicy
import com.example.panalsuite.domain.security.Permission
import com.example.panalsuite.domain.security.PermissionDeniedException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class UserAdminRepositoryImpl(
    private val users: FakeUserDataSource,
    private val auth: AuthRepository,
) : UserAdminRepository {

    override fun observeUsers(): Flow<List<User>> =
        combine(users.users, auth.currentUser) { list, me ->
            if (AccessPolicy.has(me, Permission.MANAGE_USERS)) list else emptyList()
        }

    override fun observeParameters(): Flow<SimulatedParameters> =
        combine(users.parameters, auth.currentUser) { params, me ->
            if (AccessPolicy.has(me, Permission.MANAGE_PARAMETERS)) params else SimulatedParameters()
        }

    override suspend fun createTestUser(username: String, role: Role): Result<User> {
        requirePermission(Permission.MANAGE_USERS)?.let { return Result.failure(it) }
        val clean = username.trim().lowercase()
        if (!USERNAME_REGEX.matches(clean)) {
            return Result.failure(IllegalArgumentException("Usuario inválido: use 3 a 20 letras minúsculas o números."))
        }
        if (users.usernameExists(clean)) return Result.failure(IllegalArgumentException("El usuario ya existe."))
        return Result.success(users.add(clean, role))
    }

    override suspend fun updateRole(userId: String, role: Role): Result<Unit> {
        requirePermission(Permission.MANAGE_USERS)?.let { return Result.failure(it) }
        if (userId == auth.currentUser.value?.id) {
            return Result.failure(PermissionDeniedException("No puedes cambiar tu propio perfil."))
        }
        if (users.find(userId) == null) return Result.failure(IllegalArgumentException("Usuario no encontrado."))
        users.update(userId) { it.copy(role = role) }
        return Result.success(Unit)
    }

    override suspend fun setActive(userId: String, active: Boolean): Result<Unit> {
        requirePermission(Permission.MANAGE_USERS)?.let { return Result.failure(it) }
        if (userId == auth.currentUser.value?.id) {
            return Result.failure(PermissionDeniedException("No puedes desactivar tu propia cuenta."))
        }
        if (users.find(userId) == null) return Result.failure(IllegalArgumentException("Usuario no encontrado."))
        users.update(userId) { it.copy(active = active) }
        return Result.success(Unit)
    }

    override suspend fun updateParameters(parameters: SimulatedParameters): Result<Unit> {
        requirePermission(Permission.MANAGE_PARAMETERS)?.let { return Result.failure(it) }
        if (parameters.maxActivePerResolver !in 1..20) {
            return Result.failure(IllegalArgumentException("El máximo debe estar entre 1 y 20."))
        }
        users.parameters.value = parameters
        return Result.success(Unit)
    }

    private fun requirePermission(permission: Permission): PermissionDeniedException? =
        if (AccessPolicy.has(auth.currentUser.value, permission)) null
        else PermissionDeniedException("Tu rol no tiene permiso para esta operación.")

    private companion object {
        val USERNAME_REGEX = Regex("^[a-z0-9]{3,20}$")
    }
}
