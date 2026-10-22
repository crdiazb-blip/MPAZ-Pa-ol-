package com.example.mpazpaolr.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mpazpaolr.data.repository.AuthRepository
import com.example.mpazpaolr.ui.screen.Carrito
import com.example.mpazpaolr.ui.screen.Login
import com.example.mpazpaolr.ui.screen.MisSolicitudes
import com.example.mpazpaolr.ui.screen.Principal
import com.example.mpazpaolr.ui.screen.Solicitud
import com.example.mpazpaolr.ui.viewmodel.PanolViewModel

object Routes {
    const val LOGIN = "login"
    const val PRINCIPAL = "principal"
    const val CARRITO = "carrito"
    const val SOLICITUD = "solicitud"
    const val MIS_SOLICITUDES = "mis_solicitudes"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    // ViewModel compartido (catálogo + carrito) a nivel de Activity
    val panolViewModel: PanolViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            Login(
                onLoginSuccess = {
                    navController.navigate(Routes.PRINCIPAL) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.PRINCIPAL) {
            Principal(
                viewModel = panolViewModel,
                onIrCarrito = { navController.navigate(Routes.CARRITO) },
                onIrSolicitudes = { navController.navigate(Routes.MIS_SOLICITUDES) },
                onCerrarSesion = {
                    AuthRepository.cerrarSesion()
                    panolViewModel.vaciarCarrito()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.PRINCIPAL) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.CARRITO) {
            Carrito(
                viewModel = panolViewModel,
                onVolver = { navController.popBackStack() },
                onContinuar = { navController.navigate(Routes.SOLICITUD) }
            )
        }

        composable(Routes.SOLICITUD) {
            Solicitud(
                panolViewModel = panolViewModel,
                onVolver = { navController.popBackStack() },
                onSolicitudEnviada = {
                    navController.navigate(Routes.MIS_SOLICITUDES) {
                        popUpTo(Routes.PRINCIPAL)
                    }
                }
            )
        }

        composable(Routes.MIS_SOLICITUDES) {
            MisSolicitudes(
                viewModel = panolViewModel,
                onVolver = { navController.popBackStack() }
            )
        }
    }
}
