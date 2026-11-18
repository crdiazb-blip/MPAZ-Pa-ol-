package com.example.mpazpaolr.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mpazpaolr.data.repository.AuthRepository
import com.example.mpazpaolr.model.Rol
import com.example.mpazpaolr.ui.screen.AdminEquipoForm
import com.example.mpazpaolr.ui.screen.AdminHome
import com.example.mpazpaolr.ui.screen.AdminInventario
import com.example.mpazpaolr.ui.screen.AdminSolicitudes
import com.example.mpazpaolr.ui.screen.Carrito
import com.example.mpazpaolr.ui.screen.Login
import com.example.mpazpaolr.ui.screen.MisSolicitudes
import com.example.mpazpaolr.ui.screen.Principal
import com.example.mpazpaolr.ui.screen.Solicitud
import com.example.mpazpaolr.ui.viewmodel.GestionSolicitudesViewModel
import com.example.mpazpaolr.ui.viewmodel.InventarioViewModel
import com.example.mpazpaolr.ui.viewmodel.LoginViewModel
import com.example.mpazpaolr.ui.viewmodel.PanolViewModel

object Routes {
    const val CARGANDO = "cargando"
    const val LOGIN = "login"

    // Perfil Solicitante
    const val PRINCIPAL = "principal"
    const val CARRITO = "carrito"
    const val SOLICITUD = "solicitud"
    const val MIS_SOLICITUDES = "mis_solicitudes"

    // Perfil Administrador
    const val ADMIN_HOME = "admin_home"
    const val ADMIN_INVENTARIO = "admin_inventario"
    const val ADMIN_EQUIPO_FORM = "admin_equipo_form"
    const val ADMIN_SOLICITUDES = "admin_solicitudes"

    /** Pantalla de inicio de cada perfil. */
    fun inicioDe(rol: Rol): String = if (rol == Rol.ADMIN) ADMIN_HOME else PRINCIPAL
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val panolViewModel: PanolViewModel = viewModel()
    val loginViewModel: LoginViewModel = viewModel()
    val inventarioViewModel: InventarioViewModel = viewModel()
    val gestionViewModel: GestionSolicitudesViewModel = viewModel()

    val cerrarSesion = {
        panolViewModel.limpiarSesion()
        loginViewModel.cerrarSesion {
            navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.CARGANDO
    ) {
        // Al abrir la app se restaura la sesión guardada en SQLite (si no ha expirado).
        composable(Routes.CARGANDO) {
            LaunchedEffect(Unit) {
                val perfil = loginViewModel.restaurarSesion()
                val destino = perfil?.let { Routes.inicioDe(it.rol) } ?: Routes.LOGIN
                navController.navigate(destino) { popUpTo(Routes.CARGANDO) { inclusive = true } }
            }
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        composable(Routes.LOGIN) {
            Login(
                viewModel = loginViewModel,
                onLoginSuccess = { perfil ->
                    navController.navigate(Routes.inicioDe(perfil.rol)) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // ---------------------------------------------------------------- Solicitante
        composable(Routes.PRINCIPAL) {
            SoloPara(Rol.SOLICITANTE, navController) {
                Principal(
                    viewModel = panolViewModel,
                    onIrCarrito = { navController.navigate(Routes.CARRITO) },
                    onIrSolicitudes = { navController.navigate(Routes.MIS_SOLICITUDES) },
                    onCerrarSesion = cerrarSesion
                )
            }
        }

        composable(Routes.CARRITO) {
            SoloPara(Rol.SOLICITANTE, navController) {
                Carrito(
                    viewModel = panolViewModel,
                    onVolver = { navController.popBackStack() },
                    onContinuar = { navController.navigate(Routes.SOLICITUD) }
                )
            }
        }

        composable(Routes.SOLICITUD) {
            SoloPara(Rol.SOLICITANTE, navController) {
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
        }

        composable(Routes.MIS_SOLICITUDES) {
            SoloPara(Rol.SOLICITANTE, navController) {
                MisSolicitudes(
                    viewModel = panolViewModel,
                    onVolver = { navController.popBackStack() }
                )
            }
        }

        // ---------------------------------------------------------------- Administrador
        composable(Routes.ADMIN_HOME) {
            SoloPara(Rol.ADMIN, navController) {
                AdminHome(
                    onIrInventario = { navController.navigate(Routes.ADMIN_INVENTARIO) },
                    onIrSolicitudes = { navController.navigate(Routes.ADMIN_SOLICITUDES) },
                    onCerrarSesion = cerrarSesion
                )
            }
        }

        composable(Routes.ADMIN_INVENTARIO) {
            SoloPara(Rol.ADMIN, navController) {
                AdminInventario(
                    viewModel = inventarioViewModel,
                    onVolver = { navController.popBackStack() },
                    onNuevo = {
                        inventarioViewModel.nuevoEquipo()
                        navController.navigate(Routes.ADMIN_EQUIPO_FORM)
                    },
                    onEditar = { equipo ->
                        inventarioViewModel.editarEquipo(equipo)
                        navController.navigate(Routes.ADMIN_EQUIPO_FORM)
                    }
                )
            }
        }

        composable(Routes.ADMIN_SOLICITUDES) {
            SoloPara(Rol.ADMIN, navController) {
                AdminSolicitudes(
                    viewModel = gestionViewModel,
                    onVolver = { navController.popBackStack() }
                )
            }
        }

        composable(Routes.ADMIN_EQUIPO_FORM) {
            SoloPara(Rol.ADMIN, navController) {
                AdminEquipoForm(
                    viewModel = inventarioViewModel,
                    onVolver = { navController.popBackStack() },
                    onGuardado = { navController.popBackStack() }
                )
            }
        }
    }
}

/**
 * Protege cada pantalla según el perfil: si la sesión activa no tiene el rol requerido
 * (o no hay sesión), se vuelve al login en lugar de mostrar la pantalla.
 */
@Composable
private fun SoloPara(rol: Rol, navController: NavHostController, contenido: @Composable () -> Unit) {
    val permitido = AuthRepository.perfilActivo?.rol == rol
    if (permitido) {
        contenido()
    } else {
        LaunchedEffect(Unit) {
            navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
        }
    }
}
