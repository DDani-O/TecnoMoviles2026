package com.undef.fintrackmobile.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.undef.fintrackmobile.data.repository.GroqRepository
import com.undef.fintrackmobile.data.repository.NewProduct
import com.undef.fintrackmobile.data.repository.PurchaseRepository
import com.undef.fintrackmobile.data.repository.SincronizacionRepository
import com.undef.fintrackmobile.ui.util.parseCents
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

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
    private val groqRepository: GroqRepository,
    private val sincronizacionRepository: SincronizacionRepository,
    private val application: Application
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

    private val _ticketRemoteUrl = MutableStateFlow<String?>(null)

    // Lista de productos en borrador
    private val _products = MutableStateFlow<List<EditableProductDraft>>(emptyList())
    val products: StateFlow<List<EditableProductDraft>> = _products.asStateFlow()

    private val _estadoSincronizacion = MutableStateFlow<SincronizacionEstado>(SincronizacionEstado.Inactivo)
    val estadoSincronizacion: StateFlow<SincronizacionEstado> = _estadoSincronizacion.asStateFlow()

    private val _parsingTicket = MutableStateFlow(false)
    val parsingTicket: StateFlow<Boolean> = _parsingTicket.asStateFlow()

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


    fun processTicketImage(uri: Uri) {
        _ticketUri.value = uri.toString()
        viewModelScope.launch {
            _parsingTicket.value = true
            Log.d("PurchaseVM", "Starting ticket processing for URI: $uri")
            try {
                val bytes = application.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null) {
                    Log.d("PurchaseVM", "Image bytes read successfully: ${bytes.size} bytes")
                    
                    // 1. Cargar datos vía IA
                    val result = groqRepository.parseTicket(bytes)
                    result.onSuccess { parsed ->
                        Log.d("PurchaseVM", "IA parsing success: ${parsed.supermarket}")
                        _supermarket.value = parsed.supermarket
                        parsed.date?.let { dateStr ->
                            try {
                                val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                format.parse(dateStr)?.let { _dateMillis.value = it.time }
                            } catch (e: Exception) {
                                Log.e("PurchaseVM", "Error parsing date from IA: $dateStr", e)
                            }
                        }
                        _products.value = parsed.products.map { p ->
                            EditableProductDraft(
                                id = nextProductId++,
                                name = p.name,
                                quantity = p.quantity.toString(),
                                price = String.format(Locale.US, "%.2f", p.price),
                                discount = String.format(Locale.US, "%.2f", p.discount)
                            )
                        }
                    }.onFailure { e ->
                        Log.e("PurchaseVM", "IA parsing failed: ${e.message}", e)
                        _estadoSincronizacion.value = SincronizacionEstado.Error("Error IA: ${e.message}")
                    }

                    // 2. Subir imagen a Supabase
                    val fileName = "ticket_${System.currentTimeMillis()}.jpg"
                    Log.d("PurchaseVM", "Uploading image to Supabase: $fileName")
                    sincronizacionRepository.uploadTicketImage(fileName, bytes).onSuccess { url ->
                        Log.d("PurchaseVM", "Image upload success: $url")
                        _ticketRemoteUrl.value = url
                    }.onFailure { e ->
                        Log.e("PurchaseVM", "Image upload failed: ${e.message}", e)
                        // No bloqueamos el proceso si falla la subida, pero avisamos en log
                    }
                } else {
                    Log.e("PurchaseVM", "Failed to read bytes from URI")
                    _estadoSincronizacion.value = SincronizacionEstado.Error("No se pudo leer la imagen")
                }
            } catch (e: Exception) {
                Log.e("PurchaseVM", "Critical error processing ticket", e)
                _estadoSincronizacion.value = SincronizacionEstado.Error("Error crítico: ${e.message}")
            } finally {
                _parsingTicket.value = false
            }
        }
    }

    fun addEmptyProduct() {
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
        _supermarket.value = ""
        _reason.value = ""
        _dateMillis.value = System.currentTimeMillis()
        _ticketUri.value = null
        _ticketRemoteUrl.value = null
        _products.value = emptyList()
        nextProductId = 1L
    }

    fun savePurchase(totalCents: Long) {
        val supermarketName = _supermarket.value.trim()
        val reason = _reason.value.trim()
        val dateMillis = _dateMillis.value
        val products = _products.value.map { it.toNewProduct() }
        val ticketImageUrl = _ticketRemoteUrl.value
        if (supermarketName.isBlank()) return
        
        viewModelScope.launch {
            _estadoSincronizacion.value = SincronizacionEstado.Cargando
            try {
                // 1. Guardar localmente
                repository.addPurchase(supermarketName, totalCents, dateMillis, reason, products, ticketImageUrl)
                clearDraft()
                
                // 2. Intentar sincronizar inmediatamente
                val result = repository.syncWithRemote()
                result.onSuccess {
                    _estadoSincronizacion.value = SincronizacionEstado.Exito
                }.onFailure { e ->
                    val msg = if (e is retrofit2.HttpException && e.code() == 401) {
                        "Sesión expirada. Por favor, cierra sesión y vuelve a entrar."
                    } else {
                        e.message ?: "Sync failed"
                    }
                    _estadoSincronizacion.value = SincronizacionEstado.Error(msg)
                }
            } catch (e: Exception) {
                _estadoSincronizacion.value = SincronizacionEstado.Error(e.message ?: "Save failed")
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
