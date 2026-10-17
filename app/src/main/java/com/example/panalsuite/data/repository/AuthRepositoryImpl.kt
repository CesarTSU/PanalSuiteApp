package com.example.panalsuite.data.repository

import com.example.panalsuite.data.fake.FakeUserDataSource
import com.example.panalsuite.domain.model.User
import com.example.panalsuite.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Sesión local simulada: vive solo en memoria y no representa autenticación de servidor. */
class AuthRepositoryImpl(private val users: FakeUserDataSource) : AuthRepository {
    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: StateFlow<User?> = _currentUser

    override suspend fun login(username: String, password: String): Result<User> {
        val user = users.authenticate(username, password)
        return if (user != null && user.active) {
            _currentUser.value = user
            Result.success(user)
        } else {
            Result.failure(IllegalArgumentException("Credenciales inválidas o usuario inactivo"))
        }
    }

    override fun logout() {
        _currentUser.value = null
    }
}
