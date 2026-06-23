package com.undef.fintrackmobile.data.repository

import android.util.Log
import android.util.Base64
import com.squareup.moshi.Moshi
import com.undef.fintrackmobile.data.network.GroqApiService
import com.undef.fintrackmobile.data.network.dto.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class GroqRepository(
    private val apiService: GroqApiService,
    private val moshi: Moshi
) {
    suspend fun parseTicket(imageBytes: ByteArray): Result<TicketParsedResponse> = withContext(Dispatchers.IO) {
        try {
            val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
            val imageUrl = "data:image/jpeg;base64,$base64Image"

            val prompt = """
        Sos un experto en lectura de tickets y facturas de comercios argentinos.
        Analizá la imagen y extraé los datos en formato JSON.
        
        REGLAS PARA IDENTIFICAR EL COMERCIO:
        - Es el nombre en el encabezado, antes de los datos fiscales (CUIT, IVA, etc).
        - Ignorá "S.A.", "S.R.L.", "FACTURA B", "CUIT", "ORIGINAL", etc.
        - Ejemplos: "SUPER MAMI", "Lider", "Easy", "Carrefour", "Coto".
        
        FORMATOS DE PRODUCTOS QUE PODÉS ENCONTRAR EN TICKETS ARGENTINOS:
        
        FORMATO A - Producto en dos líneas (muy común en Easy, Carrefour, Walmart):
            Línea 1: NOMBRE DEL PRODUCTO
            Línea 2: [cantidad]x[precio unitario] / [código de barras]   [total de línea]
            Ejemplo:
                CAJA PLASTICA PARA ZAPATOS X3
                4x9.900,00 / 7791231103174        39.600,00
            → name="CAJA PLASTICA PARA ZAPATOS X3", quantity=4, price=9900.00, discount=0
            
        FORMATO B - Producto en una línea con código al inicio (común en Super Mami, kioscos):
            [código]  [NOMBRE]    [precio]
            Ejemplo:
                9191228   CAMPERA IA917    64990.00
            → name="CAMPERA IA917", quantity=1, price=64990.00
            Si la línea siguiente dice "DTO", "Descuento" o "AHORRO" con un monto → ese es el discount.
            
        FORMATO C - Producto con cantidad al inicio y descuento en línea siguiente (farmacias, perfumerías):
            [cantidad]  [NOMBRE DEL PRODUCTO]
                        [precio total]   [precio total]
            Descuento  :[porcentaje]%
            Ejemplo:
                1   QUELAT MAGNESIO comp x 30
                    34472.94    34472.94
                Descuento  :30%
            → name="QUELAT MAGNESIO comp x 30", quantity=1, price=34472.94, discount=10341.88
            Nota: el discount se calcula como precio * porcentaje, o tomarlo del resumen final.
        
        REGLAS GENERALES PARA TODOS LOS FORMATOS:
        - "price" es siempre el precio UNITARIO del producto (no el total de línea).
          Si ves "4x9.900,00" → price=9900.00, quantity=4.
        - "discount" es el descuento por UNIDAD en pesos. Si no hay descuento → 0.
        - Los números pueden usar punto o coma como separador decimal según el ticket.
          Convertí siempre a número con punto decimal (ej: "9.900,00" → 9900.00).
        - Ignorá completamente líneas de: SUBTOTAL, IVA, Imp. Internos, VISA, MASTERCARD,
          SU PAGO, VUELTO, VUELTA, CAE, CAEA, CUIT, Ingresos Brutos, cajero, comprobante,
          dirección, teléfono, NRO.TIENDA, NRO.CAJA, NRO.TICKET.
        
        REGLAS PARA EL TOTAL:
        - Buscá la línea "TOTAL" o "NETO" (no "SUBTOTAL SIN DESCUENTOS").
        - Si hay descuentos globales, el total real es el NETO ya descontado.
        - Ignorá los montos de VISA/pago, que repiten el total.
        
        REGLAS PARA LA FECHA:
        - Formatos posibles: DD/MM/YYYY, DD/MM/YY, DD-MM-YYYY.
        - Convertí siempre a formato yyyy-MM-dd.
        
        IMPORTANTE:
        - Si un valor no se puede leer con certeza → null.
        - Respondé ÚNICAMENTE con el JSON puro, sin texto extra ni bloques markdown.
        
        JSON de salida (schema estricto):
        {
          "supermarket": "String o null",
          "date": "yyyy-MM-dd o null",
          "total": number o null,
          "products": [
            {
              "name": "String",
              "quantity": number,
              "price": number,
              "discount": number
            }
          ]
        }
    """.trimIndent()

            val request = GroqRequest(
                model = "meta-llama/llama-4-scout-17b-16e-instruct",
                messages = listOf(
                    GroqMessage(
                        role = "user",
                        content = listOf(
                            GroqContent(type = "text", text = prompt),
                            GroqContent(type = "image_url", imageUrl = GroqImageUrl(url = imageUrl))
                        )
                    )
                ),
                temperature = 0.0,
                responseFormat = GroqResponseFormat(type = "json_object")
            )

            Log.d("GroqRepo", "Sending request to Groq...")
            val response = apiService.getChatCompletion(request)
            val jsonContent = response.choices.firstOrNull()?.message?.content ?: return@withContext Result.failure(Exception("Empty response from AI"))
            
            Log.d("GroqRepo", "Received response: $jsonContent")
            val adapter = moshi.adapter(TicketParsedResponse::class.java)
            val parsed = adapter.fromJson(jsonContent) ?: return@withContext Result.failure(Exception("Failed to parse AI JSON"))
            
            Result.success(parsed)
        } catch (e: Exception) {
            if (e is HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("GroqRepo", "HTTP Error ${e.code()}: $errorBody")
                Result.failure(Exception("Groq Error: $errorBody"))
            } else {
                Log.e("GroqRepo", "Unknown error", e)
                Result.failure(e)
            }
        }
    }
}
