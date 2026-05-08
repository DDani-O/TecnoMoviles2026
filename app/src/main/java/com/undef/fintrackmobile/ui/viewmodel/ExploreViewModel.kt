package com.undef.fintrackmobile.ui.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undef.fintrackmobile.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ExploreUiState: Estados para la pantalla de Explorar.
 */
sealed class ExploreUiState {
    object Loading : ExploreUiState()
    data class Success(val data: ExploreData) : ExploreUiState()
    data class Error(val message: String) : ExploreUiState()
}

/**
 * ExploreData: Datos que consume la pantalla de Explorar.
 */
@Immutable
data class ExploreData(
    val supermarkets: List<SupermarketExplore>,
    val offerSections: List<OfferSectionExplore>,
    val news: List<NewsExplore>,
    val suggestions: List<SuggestionExplore>
)

/**
 * SupermarketExplore: Información detallada de un supermercado para la sección de exploración.
 */
@Immutable
data class SupermarketExplore(
    val id: Int,
    val name: String,
    val imageRes: Int,
    val location: String,
    val rating: Float,
    val comments: String,
    val hours: String,
    val webUrl: String,
    val shortDescription: String = "Tu supermercado de confianza",
    val promotions: List<String> = emptyList(),
    val paymentMethods: List<String> = emptyList(),
    val benefits: List<String> = emptyList()
)

/**
 * OfferSectionExplore: Sección que agrupa un conjunto de ítems de oferta.
 */
@Immutable
data class OfferSectionExplore(
    val title: String,
    val items: List<OfferItemExplore>
)

/**
 * OfferItemExplore: Ítem individual de una oferta con título, descripción y tienda.
 */
@Immutable
data class OfferItemExplore(
    val title: String,
    val description: String,
    val store: String
)

/**
 * NewsExplore: Noticia o aviso relevante para la pantalla de exploración.
 */
@Immutable
data class NewsExplore(
    val id: Int,
    val title: String,
    val description: String
)

/**
 * SuggestionExplore: Sugerencia personalizada basada en el consumo del usuario.
 */
@Immutable
data class SuggestionExplore(
    val id: Int,
    val title: String,
    val description: String
)

/**
 * ExploreViewModel: Gestiona el estado y la lógica de la pantalla de exploración.
 */
class ExploreViewModel(
    private val exploreRepository: com.undef.fintrackmobile.data.repository.ExploreRepository
) : ViewModel() {
    private val _state = MutableStateFlow<ExploreUiState>(ExploreUiState.Loading)
    val state: StateFlow<ExploreUiState> = _state.asStateFlow()

    init {
        loadData()
    }

    /**
     * loadData: Carga datos iniciales para la pantalla.
     * En una app real vendrían de un repositorio.
     */
    fun loadData() {
        viewModelScope.launch {
            try {
                // Simulamos una demora de red para mostrar el estado de carga
                _state.value = ExploreUiState.Loading
                
                val supermarkets = listOf(
                    SupermarketExplore(
                        id = 1,
                        name = "Carrefour",
                        imageRes = R.drawable.logo_carrefour,
                        location = "Av. Santa Fe 1234, CABA",
                        rating = 4.5f,
                        comments = "¡Excelente atención y frescura!",
                        hours = "08:00 - 22:00",
                        webUrl = "https://www.carrefour.com.ar",
                        shortDescription = "Precios bajos todos los días",
                        promotions = listOf("2x1 en lácteos", "70% 2da unidad vinos"),
                        paymentMethods = listOf("Tarjeta Mi Carrefour", "Todas las tarjetas"),
                        benefits = listOf("Puntos Mi Carrefour", "Envío gratis > $30.000")
                    ),
                    SupermarketExplore(
                        id = 2,
                        name = "Coto",
                        imageRes = R.drawable.logo_coto,
                        location = "Pueyrredón 2501, CABA",
                        rating = 4.2f,
                        comments = "Las mejores ofertas en carnicería.",
                        hours = "08:30 - 21:30",
                        webUrl = "https://www.coto.com.ar",
                        shortDescription = "Yo te conozco",
                        promotions = listOf("Miércoles 15% Comunidad Coto", "OFERTAS de carne"),
                        paymentMethods = listOf("Comunidad Coto", "Mercado Pago"),
                        benefits = listOf("Descuentos en carnes", "Cuotas sin interés")
                    ),
                    SupermarketExplore(
                        id = 3,
                        name = "Jumbo",
                        imageRes = R.drawable.logo_jumbo,
                        location = "Bullrich 345, CABA",
                        rating = 4.8f,
                        comments = "Calidad premium asegurada.",
                        hours = "09:00 - 21:00",
                        webUrl = "https://www.jumbo.com.ar",
                        shortDescription = "Calidad para tu familia",
                        promotions = listOf("Descuento con Jumbo Mas", "Especial Gourmet"),
                        paymentMethods = listOf("Jumbo Mas", "Tarjetas Bancarias"),
                        benefits = listOf("Puntos Jumbo Mas", "Atención preferencial")
                    )
                )

                val sections = listOf(
                    OfferSectionExplore(
                        title = "Ofertas explosivas",
                        items = listOf(
                            OfferItemExplore("20% de ahorro", "Usando Mercado Pago", "Coto"),
                            OfferItemExplore("15% de descuento", "En carnicería los martes", "Carrefour"),
                            OfferItemExplore("3x2 en galletitas", "Llevando marcas elegidas", "Jumbo")
                        )
                    ),
                    OfferSectionExplore(
                        title = "Productos más buscados",
                        items = listOf(
                            OfferItemExplore("Yogur Firme", "$950", "Carrefour"),
                            OfferItemExplore("Manteca 200g", "$1800", "Coto"),
                            OfferItemExplore("Crema de Leche", "$1300", "Jumbo")
                        )
                    )
                )

                val news = listOf(
                    NewsExplore(1, "¡Aviso importante!", "Jumbo cierra este viernes debido al feriado. ¡Corré que se acaba todo!")
                )

                val suggestions = listOf(
                    SuggestionExplore(1, "Basado en tu consumo", "Sueles comprar mucho café. ¡Hay un descuento del 20% en Starbucks de Carrefour!")
                )

                _state.value = ExploreUiState.Success(
                    ExploreData(supermarkets, sections, news, suggestions)
                )
            } catch (e: Exception) {
                _state.value = ExploreUiState.Error("¡Ups! Algo salió mal: ${e.message}")
            }
        }
    }
}
