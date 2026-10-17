package com.example.panalsuite.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TicketDao {
    @Query("SELECT * FROM tickets ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<TicketEntity>>

    @Query("SELECT * FROM tickets WHERE id = :id")
    fun observeById(id: String): Flow<TicketEntity?>

    @Query("SELECT * FROM tickets WHERE id = :id")
    suspend fun getById(id: String): TicketEntity?

    @Query("SELECT COUNT(*) FROM tickets")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<TicketEntity>)

    @Query(
        "UPDATE tickets SET status = :status, assigneeId = :assigneeId, " +
            "assigneeName = :assigneeName, updatedAt = :updatedAt WHERE id = :id"
    )
    suspend fun updateStatus(id: String, status: String, assigneeId: String?, assigneeName: String?, updatedAt: Long)
}
