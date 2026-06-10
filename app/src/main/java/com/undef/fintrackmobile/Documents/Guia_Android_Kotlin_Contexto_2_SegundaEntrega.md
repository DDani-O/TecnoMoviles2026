# Guía de Desarrollo Android con Kotlin — Segunda Entrega
### App "SUPER AHORRO" · Especificación Técnica para Implementación
> Documento de contexto para generación de código — Proyecto Universitario 2026  
> **ATENCIÓN IA:** Este documento es la única fuente de verdad para implementar la Segunda Entrega. Leerlo completo antes de generar cualquier línea de código.

---

## ESTADO ACTUAL DEL REPOSITORIO (lo que YA existe y NO se debe romper)

El proyecto `FintrackMobile` (package `com.undef.fintrackmobile`) ya tiene implementado:

- ✅ **Activity** única: `MainActivity` + `SettingsActivity` (Intent explícito funcionando)
- ✅ **Navigation Compose** completa con `NavHost`, rutas tipadas en `FintrackDestination` y `Routes`
- ✅ **Room** configurado: `AppDatabase` (version 3), `PurchaseEntity`, `ProductEntity`, `PurchaseWithProducts`, `PurchaseDao`
- ✅ **DataStore** funcionando: `UserPreferencesRepository` con todas las claves tipadas
- ✅ **Networking** básico con Retrofit + Moshi: `ExploreApiService` consume `open.er-api.com` para tasas de cambio
- ✅ **Corrutinas** en todos los ViewModels con `viewModelScope.launch`
- ✅ **MVVM** completo: `AppStateViewModel`, `AuthViewModel`, `HomeViewModel`, `PurchaseViewModel`, `RecordsViewModel`, `ProfileViewModel`, `ExploreViewModel`
- ✅ **Seed de datos**: `PurchaseRepository.seedIfEmpty()` carga datos de prueba si la BD está vacía
- ✅ **Intents**: compartir, galería, cámara, abrir URL de supermercados
- ✅ **Pantallas**: Splash, Welcome, Login, Register, Home, NewPurchase, AdjustTicket, Records, Profile, Settings, Explore
- ✅ **Permisos**: INTERNET y CAMERA en `AndroidManifest.xml`
- ✅ **FileProvider** configurado para tomar fotos del ticket

### Arquitectura de paquetes actual
```
com.undef.fintrackmobile/
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt
│   │   ├── dao/PurchaseDao.kt
│   │   └── entity/  (PurchaseEntity, ProductEntity, PurchaseWithProducts)
│   ├── network/
│   │   ├── ExploreApiService.kt   ← solo GET tasas de cambio
│   │   └── NetworkModule.kt
│   ├── preferences/
│   │   ├── UserPreferences.kt
│   │   └── UserPreferencesRepository.kt
│   └── repository/
│       ├── ExploreRepository.kt
│       └── PurchaseRepository.kt
├── ui/
│   ├── components/  (muchos composables reutilizables)
│   ├── navigation/  (FintrackDestinations.kt, Routes)
│   ├── screens/     (todas las pantallas)
│   ├── theme/       (BrandTheme, Color, Theme, Type)
│   ├── util/        (Dialogs.kt, Formatters.kt)
│   └── viewmodel/   (todos los ViewModels + Factory)
├── AppContainer.kt
├── FintrackApp.kt
├── MainActivity.kt
└── SettingsActivity.kt
```

---

## 1. COMPONENTES TECNOLÓGICOS DE LA FASE 2

### 1.1 Stack a agregar / completar

Según el enunciado de la Segunda Entrega y la guía del profesor, los componentes **ya parcialmente presentes** que deben **completarse o ampliarse** son:

| Componente | Estado actual | Qué falta |
|---|---|---|
| **Room** | Configurado con compras y productos | Verificar que la persistencia real funcione (no solo seed). Agregar consultas faltantes en el DAO (getById para edición). |
| **DataStore** | Implementado para sesión y preferencias | Validar que `isLoggedIn` persista correctamente entre sesiones. Asegurar que el logout limpie el estado. |
| **Retrofit** | Solo GET a er-api.com (tasas de cambio) | Agregar al menos **un endpoint POST** a una API real o simulada relacionada con el negocio (compras o supermercados). Ver sección 1.2. |
| **Corrutinas** | Presentes en ViewModels | Asegurar `Dispatchers.IO` explícito en operaciones de red en el Repository (aunque Retrofit + suspend lo maneja, el profesor lo valora explícitamente). |
| **Intents** | Cámara, galería, URL, Settings | Agregar **compartir una compra** via `Intent.ACTION_SEND` con texto formateado. |
| **Menús y Diálogos** | `DeleteConfirmationDialog` existe | Verificar que funcione correctamente. Agregar `DropdownMenu` o menú contextual en al menos una pantalla. |

### 1.2 Integración de los nuevos componentes en la arquitectura MVVM

```
Composable
    ↓ observa StateFlow / llama métodos
ViewModel  (viewModelScope.launch)
    ↓ llama al Repository
Repository  (única fuente de verdad)
    ├── PurchaseDao        → Room (SQLite local)
    ├── UserPrefsRepository→ DataStore (preferencias)
    └── ApiService         → Retrofit (red)
```

**Regla de oro (del profesor):** El ViewModel NUNCA toca Room ni DataStore directamente. Siempre a través del Repository.

---

## 2. MODIFICACIONES AL MODELO DE DATOS Y ARQUITECTURA

### 2.1 Entidades Room existentes (NO modificar sin incrementar `version`)

```kotlin
// Ya existe — NO tocar la estructura, solo agregar queries en el DAO
@Entity(tableName = "purchases")
data class PurchaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val supermarketName: String,
    val dateMillis: Long,
    val totalCents: Long,
    val reason: String = ""
)

@Entity(
    tableName = "products",
    foreignKeys = [ForeignKey(
        entity = PurchaseEntity::class,
        parentColumns = ["id"], childColumns = ["purchaseId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("purchaseId")]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val purchaseId: Long,
    val name: String,
    val code: String = "",
    val description: String = "",
    val quantity: Int,
    val priceCents: Long,
    val discountCents: Long = 0
)
```

> ⚠️ **ALERTA CRÍTICA del profesor:** Si se necesita agregar una columna (ej: `ticketImagePath`), se DEBE incrementar `version` en `AppDatabase` de 3 a 4, y agregar una migración. Usar `fallbackToDestructiveMigration(dropAllTables = true)` como está actualmente solo es aceptable en desarrollo.

### 2.2 Nuevas queries necesarias en `PurchaseDao`

Agregar estos métodos que faltan para funcionalidades completas:

```kotlin
@Dao
interface PurchaseDao {
    // --- YA EXISTEN (no duplicar) ---
    // observePurchases(): Flow<List<PurchaseEntity>>
    // observePurchasesWithProducts(): Flow<List<PurchaseWithProducts>>
    // insertPurchase, insertProducts, updatePurchase, deletePurchase, etc.

    // --- AGREGAR: obtener una compra por ID con sus productos ---
    @Transaction
    @Query("SELECT * FROM purchases WHERE id = :purchaseId")
    suspend fun getPurchaseWithProductsById(purchaseId: Long): PurchaseWithProducts?

    // --- AGREGAR: para estadísticas del período ---
    @Query("SELECT * FROM purchases WHERE dateMillis BETWEEN :startMillis AND :endMillis ORDER BY dateMillis DESC")
    fun observePurchasesByPeriod(startMillis: Long, endMillis: Long): Flow<List<PurchaseEntity>>

    // --- AGREGAR: para agregar imagen del ticket a una compra existente ---
    // Solo si se agrega ticketImagePath a PurchaseEntity (requiere migración)
    @Query("UPDATE purchases SET ticketImagePath = :imagePath WHERE id = :purchaseId")
    suspend fun updateTicketImage(purchaseId: Long, imagePath: String)
}
```

### 2.3 DTO para la API externa (Networking - POST)

Crear un nuevo DTO en `data/network/` para el endpoint POST. Usar la API pública `https://jsonplaceholder.typicode.com` como API simulada (o mockapi.io si el equipo tiene cuenta):

```kotlin
// data/network/dto/CompraRemotaDto.kt
data class CompraRemotaDto(
    @Json(name = "title") val titulo: String,        // nombre del supermercado
    @Json(name = "body") val detalle: String,         // resumen de la compra
    @Json(name = "userId") val usuarioId: Int = 1
)

// Respuesta del POST (JSONPlaceholder devuelve el objeto con id asignado)
data class CompraRemotaRespuestaDto(
    @Json(name = "id") val id: Int,
    @Json(name = "title") val titulo: String,
    @Json(name = "body") val detalle: String,
    @Json(name = "userId") val usuarioId: Int
)
```

### 2.4 Nueva interfaz de API Service con GET y POST

Modificar o extender `ExploreApiService` — mejor crear un **nuevo service** para no confundir responsabilidades:

```kotlin
// data/network/SuperAhorroApiService.kt
interface SuperAhorroApiService {

    // GET existente (tasas de cambio) — Este ya está en ExploreApiService
    // Se mantiene separado

    // NUEVO — GET: obtener lista de supermercados (simula una API de negocio)
    @GET("posts")  // JSONPlaceholder devuelve 100 posts, los usamos como "supermercados"
    suspend fun obtenerSupermercados(): List<SupermercadoDto>

    // NUEVO — POST: sincronizar/registrar una compra en el servidor
    @POST("posts")
    suspend fun sincronizarCompra(@Body compra: CompraRemotaDto): CompraRemotaRespuestaDto
}
```

### 2.5 Nuevo `NetworkModule` ampliado

Agregar el nuevo service al `NetworkModule` existente como segundo `apiService`:

```kotlin
// En NetworkModule.kt — AGREGAR (no reemplazar el existente)
val superAhorroApiService: SuperAhorroApiService = Retrofit.Builder()
    .baseUrl("https://jsonplaceholder.typicode.com/")
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .client(okHttpClient)
    .build()
    .create(SuperAhorroApiService::class.java)
```

### 2.6 Nuevo `SincronizacionRepository`

Crear un repositorio dedicado a la sincronización remota:

```kotlin
// data/repository/SincronizacionRepository.kt
class SincronizacionRepository(private val apiService: SuperAhorroApiService) {

    // GET — lista de supermercados desde API
    suspend fun obtenerSupermercadosRemotos(): Result<List<SupermercadoDto>> =
        withContext(Dispatchers.IO) {
            try {
                val lista = apiService.obtenerSupermercados()
                Result.success(lista)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    // POST — sincronizar una compra con el servidor
    suspend fun sincronizarCompra(compra: CompraRemotaDto): Result<CompraRemotaRespuestaDto> =
        withContext(Dispatchers.IO) {
            try {
                val respuesta = apiService.sincronizarCompra(compra)
                Result.success(respuesta)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
```

### 2.7 Actualización de `AppContainer`

Agregar el nuevo repositorio al contenedor de dependencias:

```kotlin
// En AppContainer.kt — AGREGAR estas líneas
val sincronizacionRepository = SincronizacionRepository(NetworkModule.superAhorroApiService)
```

---

## 3. PLAN DE IMPLEMENTACIÓN PASO A PASO (ROADMAP)

Seguir este orden estrictamente. Cada paso debe compilar antes de pasar al siguiente.

### PASO 1 — Ampliar la capa de Datos (Room DAO)
**Archivo:** `data/local/dao/PurchaseDao.kt`
- Agregar `getPurchaseWithProductsById(purchaseId: Long)` con `@Transaction` y `suspend`.
- Verificar que `observePurchasesWithProducts()` retorna `Flow` (sin `suspend`, que es correcto).
- **No cambiar** la versión de la base de datos si no se agregan columnas nuevas.

### PASO 2 — Crear los DTOs de red
**Archivos nuevos en** `data/network/dto/`:
- `SupermercadoDto.kt` — data class para el GET de supermercados.
- `CompraRemotaDto.kt` y `CompraRemotaRespuestaDto.kt` — para el POST.
- Usar `@Json(name = "...")` de Moshi (ya está como dependencia).

### PASO 3 — Crear `SuperAhorroApiService`
**Archivo nuevo:** `data/network/SuperAhorroApiService.kt`
- Definir interfaz con `@GET("posts")` y `@POST("posts")`.
- Todas las funciones deben ser `suspend fun`.
- Base URL apunta a `https://jsonplaceholder.typicode.com/`.

### PASO 4 — Actualizar `NetworkModule`
**Archivo:** `data/network/NetworkModule.kt`
- Agregar `superAhorroApiService` como segunda instancia de Retrofit.
- Mantener el `apiService` existente (er-api.com) intacto.

### PASO 5 — Crear `SincronizacionRepository`
**Archivo nuevo:** `data/repository/SincronizacionRepository.kt`
- Implementar `obtenerSupermercadosRemotos()` con `withContext(Dispatchers.IO)` y manejo de errores con `try/catch`.
- Implementar `sincronizarCompra()` con el mismo patrón.
- Retornar `Result<T>` para que el ViewModel pueda manejar éxito/error.

### PASO 6 — Actualizar `AppContainer`
**Archivo:** `AppContainer.kt`
- Instanciar `SincronizacionRepository`.
- Agregar al factory de ViewModels en `FintrackViewModelFactory`.

### PASO 7 — Ampliar `PurchaseViewModel` con sincronización
**Archivo:** `ui/viewmodel/PurchaseViewModel.kt`
- Agregar función `sincronizarCompra(compra: PurchaseEntity)` que:
  1. Mapea la entidad a `CompraRemotaDto`.
  2. Llama `sincronizacionRepository.sincronizarCompra()`.
  3. Actualiza un `StateFlow<SincronizacionEstado>` con el resultado.
- Exponer `estadoSincronizacion: StateFlow<SincronizacionEstado>` a la UI.

### PASO 8 — Agregar Intent de compartir compra
**Archivo:** `ui/screens/RecordsScreen.kt` (o en `HistoryRecordCard.kt`)
- Agregar botón "Compartir" en `HistoryRecordCard`.
- Al presionar, lanzar `Intent(Intent.ACTION_SEND)` con texto formateado:
  ```
  🛒 Compra en [Supermercado]
  📅 [Fecha]
  💰 Total: [Monto]
  Registrado con SUPER AHORRO
  ```
- El Intent se dispara con `context.startActivity(Intent.createChooser(intent, "Compartir compra"))`.

### PASO 9 — Agregar feedback de sincronización en `NewPurchaseScreen`
**Archivo:** `ui/screens/NewPurchaseScreen.kt`
- Después de guardar una compra (botón "Guardar"), mostrar opción de sincronizar:
  - Un `Snackbar` con acción "Sincronizar" que dispara el POST.
  - O un botón secundario que llame a `viewModel.sincronizarCompra()`.
- Observar `estadoSincronizacion` con `collectAsStateWithLifecycle()` y mostrar resultado.

### PASO 10 — Agregar menú contextual en `RecordsScreen`
**Archivo:** `ui/components/records/HistoryRecordCard.kt`
- Reemplazar o complementar los íconos de editar/eliminar con un `DropdownMenu`.
- Opciones del menú: "Editar", "Eliminar", "Compartir", "Sincronizar".
- Usar `DropdownMenu` y `DropdownMenuItem` de Material3.

### PASO 11 — Verificar persistencia de sesión
**Archivo:** `ui/screens/SplashScreen.kt` y `ui/viewmodel/AppStateViewModel.kt`
- Confirmar que el `isLoggedIn` de DataStore persiste entre aperturas de la app.
- Verificar que el logout (`ProfileViewModel.logout()`) llama `setLoggedIn(false)` y navega a Welcome.
- Agregar logs de depuración temporales si es necesario.

### PASO 12 — Carga real de datos en `HomeScreen`
**Archivo:** `ui/viewmodel/HomeViewModel.kt`
- Verificar que `seedIfEmpty()` solo corre una vez (ya usa `Mutex`).
- Confirmar que el `Flow` de Room reactualiza la UI cuando se agrega una compra nueva.
- El `combine()` existente ya conecta Room → ViewModel → UI correctamente.

### PASO 13 — Internacionalización (strings.xml)
**Archivo:** `res/values/strings.xml` y **NUEVO** `res/values-en/strings.xml`
- Asegurarse de que **todos** los textos hardcodeados en el código estén en `strings.xml`.
- Crear `values-en/strings.xml` con traducción al inglés de al menos las strings principales.
- Esto demuestra el cumplimiento del requisito de internacionalización.

---

## 4. ACTUALIZACIÓN DE PANTALLAS Y NAVEGACIÓN

### 4.1 Pantallas existentes que necesitan modificaciones

| Pantalla | Archivo | Qué agregar |
|---|---|---|
| `NewPurchaseScreen` | `ui/screens/NewPurchaseScreen.kt` | Feedback POST (Snackbar con acción "Sincronizar"). Mostrar estado de sincronización. |
| `RecordsScreen` | `ui/screens/RecordsScreen.kt` | DropdownMenu contextual en cada compra (Editar, Eliminar, Compartir). |
| `HistoryRecordCard` | `ui/components/records/HistoryRecordCard.kt` | Botón compartir con Intent implícito. |
| `ExploreScreen` | `ui/screens/ExploreScreen.kt` | Consumir GET real de supermercados desde API (actualmente datos hardcodeados en ViewModel). |
| `HomeScreen` | `ui/screens/HomeScreen.kt` | Mostrar indicador si hay datos sin sincronizar (opcional, suma puntos). |

### 4.2 NO se requieren pantallas nuevas para la Segunda Entrega

El enunciado pide funcionalidad, no pantallas adicionales. Todas las pantallas ya existen de la Primera Entrega. El foco es hacer funcionar la lógica real detrás de ellas.

### 4.3 Navegación — Sin cambios en el NavHost

El `NavHost` en `FintrackApp.kt` ya tiene todas las rutas necesarias. No agregar rutas nuevas a menos que sea imprescindible. Los argumentos entre rutas (si fuera necesario pasar un `purchaseId`) se pasan como:

```kotlin
// En NavHost — pasar argumento:
composable("detalle/{purchaseId}") { backStackEntry ->
    val purchaseId = backStackEntry.arguments?.getString("purchaseId")?.toLongOrNull()
    // ...
}

// Navegar con argumento:
navController.navigate("detalle/$purchaseId")
```

### 4.4 Flujo de datos que debe funcionar end-to-end

```
Usuario llena NewPurchaseScreen
    → PurchaseViewModel.savePurchase() [suspend, Dispatchers.IO vía Room]
    → PurchaseRepository.addPurchase()
    → PurchaseDao.insertPurchase() + insertProducts()
    → Room persiste en SQLite
    → Flow en PurchaseDao emite nuevo valor
    → HomeViewModel.uiState se actualiza automáticamente
    → HomeScreen muestra la nueva compra en el carrusel de tickets
    → RecordsScreen muestra la compra en el historial
```

```
Usuario presiona "Sincronizar compra"
    → PurchaseViewModel.sincronizarCompra()
    → SincronizacionRepository.sincronizarCompra() [withContext(Dispatchers.IO)]
    → SuperAhorroApiService.sincronizarCompra() [POST a jsonplaceholder]
    → Resultado llega al ViewModel
    → UI muestra Snackbar "Compra sincronizada ✓" o "Error al sincronizar"
```

---

## 5. REGLAS ESTRICTAS DE CODIFICACIÓN (LINEAMIENTOS DEL PROFESOR)

Estas reglas son **obligatorias**. Violarlas puede bajar la nota aunque el código funcione.

### 5.1 Reglas sobre Corrutinas y Dispatchers

```kotlin
// ✅ CORRECTO — Dispatcher explícito en el Repository para operaciones de red
suspend fun sincronizarCompra(dto: CompraRemotaDto): Result<...> =
    withContext(Dispatchers.IO) {
        try {
            val respuesta = apiService.sincronizarCompra(dto)
            Result.success(respuesta)
        } catch (e: IOException) {
            Result.failure(e)  // Error de red
        } catch (e: Exception) {
            Result.failure(e)  // Error genérico
        }
    }

// ✅ CORRECTO — ViewModel lanza corrutina con viewModelScope
fun sincronizarCompraDesdeUI(compra: PurchaseEntity) {
    viewModelScope.launch {
        _estadoSincronizacion.value = SincronizacionEstado.Cargando
        val resultado = sincronizacionRepository.sincronizarCompra(compra.toDto())
        _estadoSincronizacion.value = resultado.fold(
            onSuccess = { SincronizacionEstado.Exito(it.id) },
            onFailure = { SincronizacionEstado.Error(it.message ?: "Error") }
        )
    }
}

// ❌ PROHIBIDO — Operación de red en el Main Thread (causa crash ANR)
fun sincronizarCompra() {
    val resultado = apiService.sincronizarCompra(dto)  // ← SIN suspend/corrutina = crash
}

// ❌ PROHIBIDO — GlobalScope (memory leak)
GlobalScope.launch { repository.obtenerDatos() }

// ❌ PROHIBIDO — ViewModel accede a Room directamente
class MiViewModel(private val dao: PurchaseDao) : ViewModel() {  // ← dao directo al VM
    fun guardar() { viewModelScope.launch { dao.insertPurchase(...) } }  // ← bypasea el Repository
}
```

### 5.2 Reglas sobre Room

```kotlin
// ✅ CORRECTO — Query reactiva SIN suspend (Flow ya es asíncrono)
@Query("SELECT * FROM purchases ORDER BY dateMillis DESC")
fun observePurchases(): Flow<List<PurchaseEntity>>  // ← sin suspend

// ✅ CORRECTO — Escritura CON suspend
@Insert
suspend fun insertPurchase(purchase: PurchaseEntity): Long  // ← con suspend

// ❌ PROHIBIDO — Query con Flow Y suspend (contradicción)
@Query("SELECT * FROM purchases")
suspend fun observePurchases(): Flow<List<PurchaseEntity>>  // ← ERROR: no se hace así

// ✅ CORRECTO — version = 1 al arrancar (ya está en version = 3, NO bajar)
// Si se agrega una columna nueva, incrementar a 4 y agregar migración:
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE purchases ADD COLUMN ticketImagePath TEXT")
    }
}
// Y en AppDatabase builder: .addMigrations(MIGRATION_3_4)

// ❌ PROHIBIDO en producción real — pero aceptado en este TP universitario:
.fallbackToDestructiveMigration(dropAllTables = true)  // ← ya está, no cambia
```

### 5.3 Reglas sobre DataStore

```kotlin
// ✅ CORRECTO — Declarar DataStore como top-level extension del Context
private val Context.dataStore by preferencesDataStore(name = "fintrack_prefs")
// ← Esta línea ya existe y está bien. NO crear otra instancia.

// ✅ CORRECTO — Claves tipadas (ya implementado)
val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")

// ✅ CORRECTO — Escritura suspend
suspend fun setLoggedIn(value: Boolean) {
    context.dataStore.edit { prefs -> prefs[Keys.isLoggedIn] = value }
}

// ✅ CORRECTO — Lectura como Flow
val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { it[Keys.isLoggedIn] ?: false }

// ❌ PROHIBIDO — Leer DataStore de forma bloqueante
val value = runBlocking { context.dataStore.data.first() }  // ← bloquea el Main Thread
```

### 5.4 Reglas sobre Networking / Retrofit

```kotlin
// ✅ CORRECTO — Interfaz API con suspend (Retrofit maneja el thread internamente)
interface SuperAhorroApiService {
    @GET("posts")
    suspend fun obtenerSupermercados(): List<SupermercadoDto>

    @POST("posts")
    suspend fun sincronizarCompra(@Body compra: CompraRemotaDto): CompraRemotaRespuestaDto
}

// ✅ CORRECTO — Repository maneja el error de red
suspend fun obtenerSupermercados(): Result<List<SupermercadoDto>> =
    withContext(Dispatchers.IO) {
        try {
            Result.success(apiService.obtenerSupermercados())
        } catch (e: retrofit2.HttpException) {
            Result.failure(Exception("Error HTTP: ${e.code()}"))
        } catch (e: java.io.IOException) {
            Result.failure(Exception("Sin conexión a internet"))
        }
    }

// ❌ PROHIBIDO — Llamar a Retrofit desde el Main Thread (crash)
// ❌ PROHIBIDO — Ignorar errores de red sin manejarlos
```

### 5.5 Reglas sobre MVVM

```kotlin
// ✅ CORRECTO — Composable solo observa StateFlow
@Composable
fun MiPantalla(viewModel: MiViewModel = viewModel()) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    // solo observa, no llama al Repository
}

// ✅ CORRECTO — ViewModel expone estado inmutable
private val _uiState = MutableStateFlow<MiUiState>(MiUiState.Loading)
val uiState: StateFlow<MiUiState> = _uiState.asStateFlow()

// ❌ PROHIBIDO — Exponer MutableStateFlow a la UI
val uiState = MutableStateFlow<MiUiState>(MiUiState.Loading)  // ← la UI puede modificarlo
```

### 5.6 Reglas sobre Intents

```kotlin
// ✅ CORRECTO — Intent explícito para Activities internas
val intent = Intent(context, SettingsActivity::class.java)
context.startActivity(intent)

// ✅ CORRECTO — Intent implícito para compartir
val intent = Intent(Intent.ACTION_SEND).apply {
    type = "text/plain"
    putExtra(Intent.EXTRA_TEXT, textoFormateado)
}
context.startActivity(Intent.createChooser(intent, "Compartir compra"))

// ✅ CORRECTO — Intent para abrir URL
val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
context.startActivity(intent)
```

### 5.7 Reglas sobre strings e internacionalización

```kotlin
// ✅ CORRECTO — Texto desde strings.xml
Text(text = stringResource(R.string.purchase_save_success))

// ❌ PROHIBIDO — Texto hardcodeado en Composable (descuenta puntos)
Text(text = "Compra guardada exitosamente")

// El archivo res/values-en/strings.xml debe existir con al menos las strings de navegación
```

### 5.8 Manejo de estados de UI (sealed class obligatorio)

Todos los nuevos estados de UI deben seguir el patrón de sealed class ya establecido:

```kotlin
// Patrón obligatorio para cualquier nueva funcionalidad con red
sealed class SincronizacionEstado {
    object Inactivo : SincronizacionEstado()
    object Cargando : SincronizacionEstado()
    data class Exito(val idRemoto: Int) : SincronizacionEstado()
    data class Error(val mensaje: String) : SincronizacionEstado()
}
```

---

## 6. CONVENCIONES DE NOMBRES DEL PROYECTO

Para mantener consistencia con el código existente:

| Tipo | Convención | Ejemplo |
|---|---|---|
| **Entidades Room** | PascalCase + Entity | `PurchaseEntity`, `ProductEntity` |
| **DAOs** | PascalCase + Dao | `PurchaseDao` |
| **Repositorios** | PascalCase + Repository | `PurchaseRepository`, `SincronizacionRepository` |
| **ViewModels** | PascalCase + ViewModel | `PurchaseViewModel`, `HomeViewModel` |
| **DTOs de red** | PascalCase + Dto | `CompraRemotaDto`, `SupermercadoDto` |
| **Estados UI** | PascalCase + UiState | `HomeUiState`, `RecordsUiState` |
| **Rutas Nav** | SCREAMING_SNAKE o camelCase | `Routes.SPLASH`, `FintrackDestination.Home.route` |
| **Strings (dominio)** | español | `compra`, `producto`, `supermercado`, `sincronizacion` |
| **Strings (técnico)** | inglés | `uiState`, `viewModel`, `repository`, `flow` |
| **Keys DataStore** | `stringPreferencesKey("snake_case")` | `stringPreferencesKey("display_name")` |
| **Tablas Room** | `tableName = "snake_case_plural"` | `"purchases"`, `"products"` |

---

## 7. CHECKLIST DE SEGUNDA ENTREGA (según enunciado oficial)

Marcar cada ítem antes de entregar:

### Requerimientos funcionales obligatorios
- [ ] **Persistencia local de sesión:** `isLoggedIn` en DataStore persiste entre aperturas
- [ ] **Base de datos local:** compras y productos se guardan en Room, listado funciona con datos reales
- [ ] **Operaciones con corrutinas:** todas las escrituras/lecturas de BD y red usan corrutinas correctamente
- [ ] **Networking GET:** al menos una llamada GET real a API externa (ya existe: er-api.com)
- [ ] **Networking POST:** al menos una llamada POST real a API externa (A IMPLEMENTAR)
- [ ] **Menús y diálogos:** `DeleteConfirmationDialog` funciona, agregar DropdownMenu en historial
- [ ] **Carga real de datos:** `HomeScreen` y `RecordsScreen` muestran datos reales de Room
- [ ] **Al menos un Intent:** compartir compra vía `Intent.ACTION_SEND` (A IMPLEMENTAR)
- [ ] **Guardar usuario logueado:** tras login/register, `isLoggedIn = true` persiste en DataStore
- [ ] **Registrar compras en BD:** `NewPurchaseScreen` guarda en Room correctamente
- [ ] **Listar compras guardadas:** `RecordsScreen` muestra historial real desde Room
- [ ] **Consultar datos desde API:** ExploreScreen consume API externa (GET real)
- [ ] **Compartir una compra por Intent:** botón compartir en HistoryRecordCard

### Requerimientos no funcionales
- [ ] **Architecture MVVM:** ningún Composable accede a Room/DataStore directamente
- [ ] **Corrutinas:** todas las ops asíncronas en suspend functions o viewModelScope
- [ ] **Dispatchers.IO:** usado explícitamente en Repository para operaciones de red y BD
- [ ] **Internacionalización:** `strings.xml` sin textos hardcodeados + `values-en/strings.xml`
- [ ] **Manejo de errores:** try/catch en todas las operaciones de red
- [ ] **GitHub actualizado:** commits incrementales con mensajes descriptivos

---

## 8. EJEMPLOS DE IMPLEMENTACIÓN CLAVE

### 8.1 Ejemplo: POST de sincronización de compra

```kotlin
// En PurchaseViewModel.kt — AGREGAR
private val _estadoSincronizacion = MutableStateFlow<SincronizacionEstado>(SincronizacionEstado.Inactivo)
val estadoSincronizacion: StateFlow<SincronizacionEstado> = _estadoSincronizacion.asStateFlow()

fun sincronizarCompra(compra: PurchaseEntity) {
    viewModelScope.launch {
        _estadoSincronizacion.value = SincronizacionEstado.Cargando
        val dto = CompraRemotaDto(
            titulo = compra.supermarketName,
            detalle = "Total: ${compra.totalCents / 100} | Fecha: ${compra.dateMillis}"
        )
        val resultado = sincronizacionRepository.sincronizarCompra(dto)
        _estadoSincronizacion.value = resultado.fold(
            onSuccess = { SincronizacionEstado.Exito(it.id) },
            onFailure = { SincronizacionEstado.Error(it.message ?: "Error desconocido") }
        )
    }
}

fun resetearEstadoSincronizacion() {
    _estadoSincronizacion.value = SincronizacionEstado.Inactivo
}
```

### 8.2 Ejemplo: Intent de compartir en HistoryRecordCard

```kotlin
// En HistoryRecordCard.kt — AGREGAR dentro del composable
val context = LocalContext.current

// Función para compartir
fun compartirCompra(purchase: PurchaseWithProducts) {
    val texto = buildString {
        appendLine("🛒 Compra en ${purchase.purchase.supermarketName}")
        appendLine("📅 ${formatDate(purchase.purchase.dateMillis)}")
        appendLine("💰 Total: ${formatCurrency(purchase.purchase.totalCents, "ARS")}")
        if (purchase.products.isNotEmpty()) {
            appendLine("📦 ${purchase.products.size} producto(s)")
        }
        appendLine("Registrado con SUPER AHORRO 🏷️")
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, texto)
        putExtra(Intent.EXTRA_SUBJECT, "Mi compra en ${purchase.purchase.supermarketName}")
    }
    context.startActivity(Intent.createChooser(intent, "Compartir compra"))
}

// En la UI, dentro del Row de acciones (junto a editar y eliminar):
IconButton(onClick = { compartirCompra(purchase) }) {
    Icon(
        imageVector = Icons.Filled.Share,
        contentDescription = stringResource(R.string.action_share),
        tint = colors.celesteBase
    )
}
```

### 8.3 Ejemplo: DropdownMenu contextual

```kotlin
// En HistoryRecordCard.kt — reemplazar los IconButtons sueltos
var mostrarMenu by remember { mutableStateOf(false) }

Box {
    IconButton(onClick = { mostrarMenu = true }) {
        Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
    }
    DropdownMenu(
        expanded = mostrarMenu,
        onDismissRequest = { mostrarMenu = false }
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.records_action_edit)) },
            onClick = { mostrarMenu = false; onEdit() },
            leadingIcon = { Icon(Icons.Filled.Edit, null) }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.action_share)) },
            onClick = { mostrarMenu = false; compartirCompra(purchase) },
            leadingIcon = { Icon(Icons.Filled.Share, null) }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.records_action_delete), color = colors.pastelRed) },
            onClick = { mostrarMenu = false; onDelete() },
            leadingIcon = { Icon(Icons.Filled.Delete, null, tint = colors.pastelRed) }
        )
    }
}
```

### 8.4 Ejemplo: observar estado de sincronización en NewPurchaseScreen

```kotlin
// En NewPurchaseScreen.kt — AGREGAR
val estadoSincronizacion by viewModel.estadoSincronizacion.collectAsStateWithLifecycle()

// Reaccionar al estado (dentro de un LaunchedEffect)
LaunchedEffect(estadoSincronizacion) {
    when (val estado = estadoSincronizacion) {
        is SincronizacionEstado.Exito -> {
            snackbarHostState.showSnackbar("✓ Compra sincronizada (ID: ${estado.idRemoto})")
            viewModel.resetearEstadoSincronizacion()
        }
        is SincronizacionEstado.Error -> {
            snackbarHostState.showSnackbar("⚠ ${estado.mensaje}")
            viewModel.resetearEstadoSincronizacion()
        }
        else -> Unit
    }
}

// En el botón de guardar — MODIFICAR para también sincronizar
Button(
    onClick = {
        if (totals.totalCents > 0L && supermarket.isNotBlank()) {
            viewModel.savePurchase(totals.totalCents)
            // La sincronización se puede disparar aquí o como acción separada en el Snackbar
            scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = saveSuccessMsg,
                    actionLabel = "Sincronizar",
                    duration = SnackbarDuration.Long
                )
                if (result == SnackbarResult.ActionPerformed) {
                    // viewModel.sincronizarUltimaCompra()  // opcional
                }
            }
        }
    }
) { /* ... */ }
```

---

## 9. DEPENDENCIAS GRADLE ACTUALES Y REQUERIDAS

El proyecto ya tiene en `build.gradle.kts`:
- Retrofit 2 + Moshi (networking)
- Room 2 (base de datos)
- DataStore Preferences (preferencias)
- Coil (carga de imágenes)
- Navigation Compose
- Lifecycle + ViewModel Compose
- Coroutines

**No se necesitan nuevas dependencias** para la Segunda Entrega si se usa JSONPlaceholder como API simulada. Todo el stack necesario ya está configurado.

Si se decide usar una API diferente o agregar autenticación, revisar si se necesita el interceptor de OkHttp (ya está configurado el `HttpLoggingInterceptor` en `NetworkModule`).

---

## 10. NOTAS FINALES PARA LA IA PROGRAMADORA

1. **No romper lo que funciona:** antes de modificar cualquier archivo existente, leer su contenido completo.
2. **El `AppContainer` es el DI manual:** toda dependencia nueva se instancia ahí y se pasa al Factory.
3. **`FintrackViewModelFactory` debe actualizarse** con cualquier nuevo ViewModel que necesite el `SincronizacionRepository`.
4. **Strings:** todos los textos nuevos van a `res/values/strings.xml`. Buscar el patrón existente antes de agregar.
5. **Colores:** usar siempre `FintrackTheme.colors.XXXX`. Nunca hardcodear colores.
6. **`@Composable` sin lógica de negocio:** cualquier cálculo complejo va al ViewModel o al Repository.
7. **Testing:** si se implementan tests, usar `runTest {}` de `kotlinx-coroutines-test` para testear funciones suspend.
8. **El profesor evalúa el código, no solo la UI:** los comentarios en el código (ya presentes con los emoji 1️⃣-8️⃣) son un plus valorado.

> **Nota para la IA:** Este documento describe el estado actual y lo que falta implementar. El proyecto compila y funciona. El objetivo es agregar la capa funcional real (networking POST, intents de compartir, menús contextuales) sin romper la arquitectura existente. Respetar siempre el patrón MVVM, las convenciones de nombres y las reglas del profesor detalladas en la Sección 5.
