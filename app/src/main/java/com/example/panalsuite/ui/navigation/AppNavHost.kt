package com.example.panalsuite.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.panalsuite.di.AppContainer
import com.example.panalsuite.ui.admin.AdminScreen
import com.example.panalsuite.ui.admin.AdminViewModel
import com.example.panalsuite.ui.create.CreateTicketScreen
import com.example.panalsuite.ui.create.CreateTicketViewModel
import com.example.panalsuite.ui.detail.TicketDetailScreen
import com.example.panalsuite.ui.detail.TicketDetailViewModel
import com.example.panalsuite.ui.home.HomeScreen
import com.example.panalsuite.ui.home.HomeViewModel
import com.example.panalsuite.ui.login.LoginScreen
import com.example.panalsuite.ui.login.LoginViewModel
import com.example.panalsuite.ui.tickets.TicketListScreen
import com.example.panalsuite.ui.tickets.TicketListViewModel
import com.example.panalsuite.ui.workload.WorkloadScreen
import com.example.panalsuite.ui.workload.WorkloadViewModel

object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val TICKETS = "tickets?filter={filter}"
    const val DETAIL = "ticket/{ticketId}"
    const val CREATE = "create"
    const val WORKLOAD = "workload"
    const val ADMIN = "admin"

    fun tickets(filter: String) = "tickets?filter=$filter"
    fun detail(id: String) = "ticket/$id"
}

@Composable
fun AppNavHost(container: AppContainer) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            val vm: LoginViewModel = viewModel(factory = viewModelFactory {
                initializer { LoginViewModel(container.authRepository) }
            })
            LoginScreen(vm) {
                navController.navigate(Routes.HOME) { popUpTo(Routes.LOGIN) { inclusive = true } }
            }
        }
        composable(Routes.HOME) {
            val vm: HomeViewModel = viewModel(factory = viewModelFactory {
                initializer { HomeViewModel(container.ticketRepository, container.authRepository) }
            })
            HomeScreen(
                viewModel = vm,
                onOpenList = { filter -> navController.navigate(Routes.tickets(filter)) },
                onOpenTicket = { id -> navController.navigate(Routes.detail(id)) },
                onCreate = { navController.navigate(Routes.CREATE) },
                onWorkload = { navController.navigate(Routes.WORKLOAD) },
                onAdmin = { navController.navigate(Routes.ADMIN) },
                onLogout = {
                    vm.logout()
                    navController.navigate(Routes.LOGIN) { popUpTo(Routes.HOME) { inclusive = true } }
                },
            )
        }
        composable(
            route = Routes.TICKETS,
            arguments = listOf(navArgument("filter") { type = NavType.StringType; defaultValue = "ALL" }),
        ) {
            val vm: TicketListViewModel = viewModel(factory = viewModelFactory {
                initializer {
                    TicketListViewModel(createSavedStateHandle(), container.ticketRepository, container.authRepository)
                }
            })
            TicketListScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onOpenTicket = { id -> navController.navigate(Routes.detail(id)) },
            )
        }
        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("ticketId") { type = NavType.StringType }),
        ) {
            val vm: TicketDetailViewModel = viewModel(factory = viewModelFactory {
                initializer {
                    TicketDetailViewModel(createSavedStateHandle(), container.ticketRepository, container.authRepository)
                }
            })
            TicketDetailScreen(vm, onBack = { navController.popBackStack() })
        }
        composable(Routes.CREATE) {
            val vm: CreateTicketViewModel = viewModel(factory = viewModelFactory {
                initializer { CreateTicketViewModel(container.ticketRepository, container.authRepository) }
            })
            CreateTicketScreen(vm, onBack = { navController.popBackStack() }, onCreated = { navController.popBackStack() })
        }
        composable(Routes.WORKLOAD) {
            val vm: WorkloadViewModel = viewModel(factory = viewModelFactory {
                initializer { WorkloadViewModel(container.ticketRepository, container.authRepository) }
            })
            WorkloadScreen(vm, onBack = { navController.popBackStack() })
        }
        composable(Routes.ADMIN) {
            val vm: AdminViewModel = viewModel(factory = viewModelFactory {
                initializer { AdminViewModel(container.userAdminRepository, container.authRepository) }
            })
            AdminScreen(vm, onBack = { navController.popBackStack() })
        }
    }
}
