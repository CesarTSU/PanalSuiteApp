package com.example.panalsuite.data.fake

import com.example.panalsuite.domain.model.Role
import com.example.panalsuite.domain.model.SimulatedParameters
import com.example.panalsuite.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * Almacén en memoria de usuarios ficticios. La contraseña de demostración es común y se guarda sin
 * protección porque es una simulación académica; nunca debe usarse como modelo de credenciales reales.
 */
class FakeUserDataSource {
    private data class Record(val user: User, val password: String)

    private val records = MutableStateFlow(
        listOf(
            Record(User("U-01", "solicitante", "Usuario 01", Role.SOLICITANTE), DEMO_PASSWORD),
            Record(User("U-02", "resolutor", "Usuario 02", Role.RESOLUTOR), DEMO_PASSWORD),
            Record(User("U-03", "supervisor", "Usuario 03", Role.SUPERVISOR), DEMO_PASSWORD),
            Record(User("U-04", "gerencia", "Usuario 04", Role.GERENCIA), DEMO_PASSWORD),
            Record(User("U-05", "administrador", "Usuario 05", Role.ADMINISTRADOR), DEMO_PASSWORD),
            Record(User("U-06", "solicitante2", "Usuario 06", Role.SOLICITANTE), DEMO_PASSWORD),
            Record(User("U-07", "resolutor2", "Usuario 07", Role.RESOLUTOR), DEMO_PASSWORD),
        )
    )

    val users: Flow<List<User>> = records.map { list -> list.map { it.user } }
    val parameters = MutableStateFlow(SimulatedParameters())

    fun authenticate(username: String, password: String): User? =
        records.value.firstOrNull {
            it.user.username == username.trim().lowercase() && it.password == password
        }?.user

    fun find(id: String): User? = records.value.firstOrNull { it.user.id == id }?.user

    fun usernameExists(username: String): Boolean = records.value.any { it.user.username == username }

    fun add(username: String, role: Role): User {
        val number = records.value.size + 1
        val user = User("U-%02d".format(number), username, "Usuario %02d".format(number), role)
        records.update { it + Record(user, DEMO_PASSWORD) }
        return user
    }

    fun update(id: String, transform: (User) -> User) {
        records.update { list -> list.map { if (it.user.id == id) it.copy(user = transform(it.user)) else it } }
    }

    companion object {
        const val DEMO_PASSWORD = "demo123"
    }
}
