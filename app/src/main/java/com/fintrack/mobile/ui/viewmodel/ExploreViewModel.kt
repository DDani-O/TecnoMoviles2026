package com.fintrack.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.mobile.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Estados para la pantalla de Explorar.
 */
sealed class ExploreUiState {
    object Cargando : ExploreUiState()
    data class Exito(val datos: DatosExplore) : ExploreUiState()
    data class Error(val mensaje: String) : ExploreUiState()
}

/**
 * Datos que consume la pantalla de Explorar.
 */
data class DatosExplore(
    val supermercados: List<SupermercadoExplore>,
    val seccionesOfertas: List<SeccionOfertasExplore>,
    val noticias: List<NoticiaExplore>,
    val sugerencias: List<SugerenciaExplore>
)

data class SupermercadoExplore(
    val id: Int,
    val nombre: String,
    val imagenRes: Int,
    val ubicacion: String,
    val puntuacion: Float,
    val comentarios: String,
    val horario: String,
    val webUrl: String
)

data class SeccionOfertasExplore(
    val titulo: String,
    val items: List<OfertaItemExplore>
)

data class OfertaItemExplore(
    val nombreProducto: String,
    val precio: String,
    val tienda: String
)

data class NoticiaExplore(
    val id: Int,
    val titulo: String,
    val descripcion: String
)

data class SugerenciaExplore(
    val id: Int,
    val titulo: String,
    val descripcion: String
)

class ExploreViewModel(
    private val exploreRepository: com.fintrack.mobile.data.repository.ExploreRepository
) : ViewModel() {
    private val _state = MutableStateFlow<ExploreUiState>(ExploreUiState.Cargando)
    val state: StateFlow<ExploreUiState> = _state.asStateFlow()

    init {
        cargarDatos()
    }

    /**
     * Carga datos iniciales para la pantalla.
     * En una app real vendrían de un repositorio.
     */
    fun cargarDatos() {
        viewModelScope.launch {
            try {
                // Simulamos una demora de red para mostrar el estado de carga
                _state.value = ExploreUiState.Cargando
                
                val supermercados = listOf(
                    SupermercadoExplore(
                        id = 1,
                        nombre = "Carrefour",
                        imagenRes = R.drawable.logo_carrefour,
                        ubicacion = "Av. Santa Fe 1234, CABA",
                        puntuacion = 4.5f,
                        comentarios = "¡Excelente atención y frescura!",
                        horario = "08:00 - 22:00",
                        webUrl = "https://www.carrefour.com.ar"
                    ),
                    SupermercadoExplore(
                        id = 2,
                        nombre = "Coto",
                        imagenRes = R.drawable.logo_coto,
                        ubicacion = "Pueyrredón 2501, CABA",
                        puntuacion = 4.2f,
                        comentarios = "Las mejores ofertas en carnicería.",
                        horario = "08:30 - 21:30",
                        webUrl = "https://www.coto.com.ar"
                    ),
                    SupermercadoExplore(
                        id = 3,
                        nombre = "Jumbo",
                        imagenRes = R.drawable.logo_jumbo,
                        ubicacion = "Bullrich 345, CABA",
                        puntuacion = 4.8f,
                        comentarios = "Calidad premium asegurada.",
                        horario = "09:00 - 21:00",
                        webUrl = "https://www.jumbo.com.ar"
                    )
                )

                val secciones = listOf(
                    SeccionOfertasExplore(
                        titulo = "🔥 Ofertas explosivas",
                        items = listOf(
                            OfertaItemExplore("Asado de Novillo", "$8900 kg", "Coto"),
                            OfertaItemExplore("Yerba Mate 1kg", "$3200", "Carrefour"),
                            OfertaItemExplore("Aceite Girasol", "$1450", "Jumbo")
                        )
                    ),
                    SeccionOfertasExplore(
                        titulo = "🕒 Últimas ofertas agregadas",
                        items = listOf(
                            OfertaItemExplore("Leche Entera", "$1100", "Carrefour"),
                            OfertaItemExplore("Pan Lactal", "$2400", "Jumbo"),
                            OfertaItemExplore("Queso Cremoso", "$4500 kg", "Coto")
                        )
                    ),
                    SeccionOfertasExplore(
                        titulo = "🥛 Productos más buscados",
                        items = listOf(
                            OfertaItemExplore("Yogur Firme", "$950", "Carrefour"),
                            OfertaItemExplore("Manteca 200g", "$1800", "Coto"),
                            OfertaItemExplore("Crema de Leche", "$1300", "Jumbo")
                        )
                    )
                )

                val noticias = listOf(
                    NoticiaExplore(1, "¡Aviso importante!", "Jumbo cierra este viernes debido al feriado. ¡Corré que se acaba todo!")
                )

                val sugerencias = listOf(
                    SugerenciaExplore(1, "Basado en tu consumo", "Sueles comprar mucho café. ¡Hay un descuento del 20% en Starbucks de Carrefour!")
                )

                _state.value = ExploreUiState.Exito(
                    DatosExplore(supermercados, secciones, noticias, sugerencias)
                )
            } catch (e: Exception) {
                _state.value = ExploreUiState.Error("¡Ups! Algo salió mal: ${e.message}")
            }
        }
    }
}
