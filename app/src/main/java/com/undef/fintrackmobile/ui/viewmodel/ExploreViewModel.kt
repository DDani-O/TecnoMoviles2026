package com.undef.fintrackmobile.ui.viewmodel

import android.app.Application
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.undef.fintrackmobile.R
import com.undef.fintrackmobile.data.repository.SincronizacionRepository
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
    @param:StringRes val shortDescriptionRes: Int = R.string.supermarket_default_description,
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
 * Utilizamos AndroidViewModel para acceder de forma segura a los recursos (strings)
 * sin provocar leaks de memoria, ya que utiliza el Application Context.
 */
class ExploreViewModel(
    private val sincronizacionRepository: SincronizacionRepository,
    application: Application
) : AndroidViewModel(application) {
    private val _state = MutableStateFlow<ExploreUiState>(ExploreUiState.Loading)
    val state: StateFlow<ExploreUiState> = _state.asStateFlow()

    private val context = getApplication<Application>()

    init {
        loadData()
    }

    /**
     * loadData: Carga datos reales desde la API a través del repositorio de sincronización.
     */
    fun loadData() {
        viewModelScope.launch {
            try {
                _state.value = ExploreUiState.Loading
                
                val supermarketsResult = sincronizacionRepository.getRemoteSupermarkets()
                val offersResult = sincronizacionRepository.getRemoteOffers()
                
                if (supermarketsResult.isSuccess && offersResult.isSuccess) {
                    val supermarketsDto = supermarketsResult.getOrThrow()
                    val offersDto = offersResult.getOrThrow()

                    val supermarkets = supermarketsDto.map { dto ->
                        val (realLogo, brandDescriptionRes) = when {
                            dto.name.contains("Carrefour", ignoreCase = true) -> 
                                R.drawable.logo_carrefour to R.string.supermarket_carrefour_description
                            dto.name.contains("Coto", ignoreCase = true) -> 
                                R.drawable.logo_coto to R.string.supermarket_coto_description
                            dto.name.contains("Jumbo", ignoreCase = true) -> 
                                R.drawable.logo_jumbo to R.string.supermarket_jumbo_description
                            else -> R.drawable.logo_carrefour to R.string.supermarket_default_description
                        }

                        SupermarketExplore(
                            id = dto.id,
                            name = dto.name,
                            imageRes = realLogo,
                            location = dto.address ?: context.getString(R.string.explore_fallback_location, 123),
                            rating = dto.rating ?: 0f,
                            comments = context.getString(R.string.supermarket_mock_comments),
                            hours = dto.schedule ?: context.getString(R.string.explore_fallback_hours),
                            webUrl = context.getString(R.string.maps_url_search, dto.address ?: ""),
                            shortDescriptionRes = brandDescriptionRes,
                            promotions = listOf(context.getString(R.string.supermarket_mock_promo)),
                            paymentMethods = listOf(
                                context.getString(R.string.payment_method_all_cards),
                                context.getString(R.string.payment_method_mercado_pago)
                            ),
                            benefits = listOf(
                                context.getString(R.string.benefit_mi_carrefour_points),
                                context.getString(R.string.explore_fallback_benefits)
                            )
                        )
                    }

                    val sections = listOf(
                        OfferSectionExplore(
                            title = context.getString(R.string.explore_offers_section_title),
                            items = offersDto.map { dto ->
                                OfferItemExplore(
                                    title = dto.title,
                                    description = dto.description ?: "",
                                    store = dto.store ?: ""
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
