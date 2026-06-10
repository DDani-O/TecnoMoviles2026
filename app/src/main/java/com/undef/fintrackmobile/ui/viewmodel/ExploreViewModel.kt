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
    data class Error(val message: String? = null, val messageRes: Int? = null, val messageArgs: List<Any> = emptyList()) : ExploreUiState()
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
    private val sincronizacionRepository: com.undef.fintrackmobile.data.repository.SincronizacionRepository
) : ViewModel() {
    private val _state = MutableStateFlow<ExploreUiState>(ExploreUiState.Loading)
    val state: StateFlow<ExploreUiState> = _state.asStateFlow()

    init {
        loadData()
    }

    /**
     * loadData: Carga datos reales desde la API a través del repositorio de sincronización.
     * Implementa la TAREA B-4 (Networking GET real).
     */
    fun loadData() {
        viewModelScope.launch {
            try {
                _state.value = ExploreUiState.Loading
                
                // Realizamos la llamada real a tu MockAPI (GET)
                val supermarketsResult = sincronizacionRepository.getRemoteSupermarkets()
                val offersResult = sincronizacionRepository.getRemoteOffers()
                
                if (supermarketsResult.isSuccess && offersResult.isSuccess) {
                    val supermarketsDto = supermarketsResult.getOrThrow()
                    val offersDto = offersResult.getOrThrow()

                    /**
                     * Mapeamos los datos reales de tu MockAPI a la UI de Fintrack.
                     * Usamos el nombre para determinar el logo correcto.
                     */
                    val supermarkets = supermarketsDto.map { dto ->
                        val (realLogo, brandDescription) = when {
                            dto.name.contains("Carrefour", ignoreCase = true) -> 
                                R.drawable.logo_carrefour to "Precios bajos todos los días"
                            dto.name.contains("Coto", ignoreCase = true) -> 
                                R.drawable.logo_coto to "Yo te conozco"
                            dto.name.contains("Jumbo", ignoreCase = true) -> 
                                R.drawable.logo_jumbo to "Calidad para tu familia"
                            else -> R.drawable.logo_carrefour to "Tu supermercado amigo"
                        }

                        SupermarketExplore(
                            id = dto.id.toIntOrNull() ?: 0,
                            name = dto.name,
                            imageRes = realLogo,
                            location = dto.address,
                            rating = dto.rating,
                            comments = "Sucursal con excelentes ofertas en Córdoba",
                            hours = dto.schedule,
                            webUrl = "https://www.google.com/maps/search/${dto.address}",
                            shortDescription = brandDescription,
                            promotions = listOf("Oferta Web en Córdoba"),
                            paymentMethods = listOf("Todas las tarjetas", "Mercado Pago"),
                            benefits = listOf("Puntos de Lealtad", "Descuentos Locales")
                        )
                    }

                    // Mapeo de Ofertas reales desde MockAPI
                    val sections = listOf(
                        OfferSectionExplore(
                            title = "Ofertas en Córdoba",
                            items = offersDto.map { dto ->
                                OfferItemExplore(
                                    title = dto.title,
                                    description = dto.description,
                                    store = dto.store
                                )
                            }
                        )
                    )

                    _state.value = ExploreUiState.Success(
                        ExploreData(
                            supermarkets = supermarkets,
                            offerSections = sections,
                            news = emptyList(),
                            suggestions = emptyList()
                        )
                    )
                } else {
                    val error = supermarketsResult.exceptionOrNull() ?: offersResult.exceptionOrNull()
                    _state.value = ExploreUiState.Error(
                        message = error?.message,
                        messageRes = R.string.explore_error_loading,
                        messageArgs = listOf(error?.message ?: "")
                    )
                }
            } catch (e: Exception) {
                _state.value = ExploreUiState.Error(
                    message = e.message,
                    messageRes = R.string.explore_error_loading,
                    messageArgs = listOf(e.message ?: "")
                )
            }
        }
    }
}
