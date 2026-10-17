package com.example.panalsuite.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [TicketEntity::class], version = 4, exportSchema = false)
abstract class PanalDatabase : RoomDatabase() {
    abstract fun ticketDao(): TicketDao
}
