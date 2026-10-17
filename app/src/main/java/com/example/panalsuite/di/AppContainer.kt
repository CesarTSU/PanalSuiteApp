package com.example.panalsuite.di

import android.content.Context
import androidx.room.Room
import com.example.panalsuite.data.fake.FakeTicketDataSource
import com.example.panalsuite.data.fake.FakeUserDataSource
import com.example.panalsuite.data.local.PanalDatabase
import com.example.panalsuite.data.repository.AuthRepositoryImpl
import com.example.panalsuite.data.repository.TicketRepositoryImpl
import com.example.panalsuite.data.repository.UserAdminRepositoryImpl
import com.example.panalsuite.domain.repository.AuthRepository
import com.example.panalsuite.domain.repository.TicketRepository
import com.example.panalsuite.domain.repository.UserAdminRepository

/** Contenedor de dependencias manual (sin framework de DI). */
class AppContainer(context: Context) {
    private val database = Room.databaseBuilder(
        context.applicationContext, PanalDatabase::class.java, "panalsuite.db"
    ).fallbackToDestructiveMigration(dropAllTables = true).build()

    private val userSource = FakeUserDataSource()

    val authRepository: AuthRepository = AuthRepositoryImpl(userSource)
    val ticketRepository: TicketRepository =
        TicketRepositoryImpl(database.ticketDao(), FakeTicketDataSource(), authRepository)
    val userAdminRepository: UserAdminRepository = UserAdminRepositoryImpl(userSource, authRepository)
}
