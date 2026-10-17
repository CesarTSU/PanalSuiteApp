package com.example.panalsuite.fakes

import com.example.panalsuite.data.local.TicketDao
import com.example.panalsuite.data.local.TicketEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeTicketDao : TicketDao {
    private val items = MutableStateFlow<List<TicketEntity>>(emptyList())

    override fun observeAll(): Flow<List<TicketEntity>> = items
    override fun observeById(id: String): Flow<TicketEntity?> = items.map { l -> l.firstOrNull { it.id == id } }
    override suspend fun getById(id: String): TicketEntity? = items.value.firstOrNull { it.id == id }
    override suspend fun count(): Int = items.value.size
    override suspend fun upsertAll(items: List<TicketEntity>) {
        val ids = items.map { it.id }.toSet()
        this.items.value = this.items.value.filter { it.id !in ids } + items
    }
    override suspend fun updateStatus(id: String, status: String, assigneeId: String?, assigneeName: String?, updatedAt: Long) {
        items.value = items.value.map {
            if (it.id == id) it.copy(status = status, assigneeId = assigneeId, assigneeName = assigneeName, updatedAt = updatedAt) else it
        }
    }
}
