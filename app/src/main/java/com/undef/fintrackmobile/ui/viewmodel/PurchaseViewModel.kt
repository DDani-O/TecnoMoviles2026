package com.undef.fintrackmobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undef.fintrackmobile.data.local.entity.PurchaseEntity
import com.undef.fintrackmobile.data.network.dto.RemotePurchaseDto
import com.undef.fintrackmobile.data.repository.NewProduct
import com.undef.fintrackmobile.data.repository.PurchaseRepository
import com.undef.fintrackmobile.data.repository.SincronizacionRepository
import com.undef.fintrackmobile.data.preferences.UserPreferencesRepository
import com.undef.fintrackmobile.ui.util.parseCents
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class SincronizacionEstado {
    object Inactivo : SincronizacionEstado()
    object Cargando : SincronizacionEstado()
    object Exito : SincronizacionEstado()
    data class Error(val mensaje: String) : SincronizacionEstado()
}

data class EditableProductDraft(
    val id: Long = 0,
    val name: String = "",
    val code: String = "",
    val description: String = "",
    val quantity: String = "",
    val price: String = "",
    val discount: String = "",
    val purchaseId: Long = 0,
)

class PurchaseViewModel(
    private val repository: PurchaseRepository,
    private val sincronizacionRepository: SincronizacionRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    // Estado para los datos generales de la compra
    private val _supermarket = MutableStateFlow("")
    val supermarket: StateFlow<String> = _supermarket.asStateFlow()

    private val _reason = MutableStateFlow("")
    val reason: StateFlow<String> = _reason.asStateFlow()

    private val _dateMillis = MutableStateFlow(System.currentTimeMillis())
    val dateMillis: StateFlow<Long> = _dateMillis.asStateFlow()

    private val _ticketUri = MutableStateFlow<String?>(null)
    val ticketUri: StateFlow<String?> = _ticketUri.asStateFlow()

    // Lista de productos en borrador
    private val _products = MutableStateFlow<List<EditableProductDraft>>(emptyList())
    val products: StateFlow<List<EditableProductDraft>> = _products.asStateFlow()

    private val _estadoSincronizacion = MutableStateFlow<SincronizacionEstado>(SincronizacionEstado.Inactivo)
    val estadoSincronizacion: StateFlow<SincronizacionEstado> = _estadoSincronizacion.asStateFlow()

    private var nextProductId = 1L

    fun setSupermarket(value: String) {
        _supermarket.value = value
    }

    fun setReason(value: String) {
        _reason.value = value
    }

    fun setDateMillis(value: Long) {
        _dateMillis.value = value
    }

    fun setTicketUri(value: String?) {
        _ticketUri.value = value
    }

    fun addEmptyProduct() {
        // Agregamos el producto al inicio de la lista para mejorar la usabilidad (evita scroll innecesario)
        _products.value = listOf(buildEmptyProduct()) + _products.value
    }

    fun updateProduct(index: Int, updated: EditableProductDraft) {
        val current = _products.value
        if (index !in current.indices) return
        val mutable = current.toMutableList()
        mutable[index] = updated
        _products.value = mutable
    }

    fun removeProduct(index: Int) {
        val current = _products.value
        if (index !in current.indices) return
        val mutable = current.toMutableList()
        mutable.removeAt(index)
        _products.value = mutable
    }

    fun clearDraft() {
        // Limpia todos los campos del borrador
        _supermarket.value = ""
        _reason.value = ""
        _dateMillis.value = System.currentTimeMillis()
        _ticketUri.value = null
        _products.value = emptyList()
        nextProductId = 1L // Reiniciamos el contador de IDs para la próxima compra
    }

    fun savePurchase(totalCents: Long) {
        // Persiste la compra en el repositorio local
        val supermarketName = _supermarket.value.trim()
        val reason = _reason.value.trim()
        val dateMillis = _dateMillis.value
        val products = _products.value.map { it.toNewProduct() }
        if (supermarketName.isBlank()) return
        viewModelScope.launch {
            repository.addPurchase(supermarketName, totalCents, dateMillis, reason, products)
            clearDraft()
        }
    }

    /**
     * syncPurchase: Sincroniza la compra con el servidor (MockAPI) usando el esquema profesional.
     */
    fun syncPurchase(purchase: PurchaseEntity) {
        viewModelScope.launch {
            _estadoSincronizacion.value = SincronizacionEstado.Cargando
            
            // Obtenemos el email real del usuario actual desde DataStore
            val currentUserEmail = userPreferencesRepository.preferencesFlow.first().email

            // Formateo de fecha para la API (YYYY-MM-DD)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formattedDate = sdf.format(Date(purchase.dateMillis))

            // Mapeo al DTO profesional con nombres en inglés, incluyendo el email del usuario
            val dto = RemotePurchaseDto(
                storeName = purchase.supermarketName,
                totalAmount = purchase.totalCents / 100.0,
                purchaseDate = formattedDate,
                reason = purchase.reason,
                userEmail = currentUserEmail // Usamos el email obtenido directamente de las preferencias
            )

            val result = sincronizacionRepository.syncPurchase(dto)
            result.onSuccess {
                _estadoSincronizacion.value = SincronizacionEstado.Exito
            }.onFailure {
                _estadoSincronizacion.value = SincronizacionEstado.Error(it.message ?: "Error desconocido")
            }
        }
    }

    fun resetSyncStatus() {
        _estadoSincronizacion.value = SincronizacionEstado.Inactivo
    }

    private fun buildEmptyProduct(): EditableProductDraft {
        return EditableProductDraft(
            id = nextProductId++,
            name = "",
            code = "",
            description = "",
            quantity = "1",
            price = "0.00",
            discount = "0.00"
        )
    }

    private fun EditableProductDraft.toNewProduct(): NewProduct {
        val safeQuantity = quantity.toIntOrNull()?.coerceAtLeast(0) ?: 0
        return NewProduct(
            name = name,
            code = code,
            description = description,
            quantity = safeQuantity,
            priceCents = parseCents(price).coerceAtLeast(0),
            discountCents = parseCents(discount).coerceAtLeast(0)
        )
    }
}
