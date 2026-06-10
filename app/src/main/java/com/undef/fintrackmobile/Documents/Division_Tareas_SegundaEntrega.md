# División de Tareas — Segunda Entrega "SUPER AHORRO"
### Guía para trabajo en equipo sin conflictos en GitHub
> Dos integrantes · Nivel similar · Basado en el contexto de Segunda Entrega

---

## 🧠 Lógica de la división

La clave para no chocarse en GitHub es que **cada integrante tenga su propio "territorio" de archivos**. La división está hecha por capas de la arquitectura:

- **Integrante A → Capa de Datos + Red** (todo lo que no se ve en pantalla)
- **Integrante B → Capa de UI + Presentación** (todo lo que el usuario toca)

Ambos hacen aproximadamente la misma cantidad de trabajo y usan los mismos conceptos del profesor (Room, Corrutinas, StateFlow, Intents). Ninguno tiene una parte "más fácil" que el otro.

---

## 👤 INTEGRANTE A — Capa de Datos, Red y ViewModels

### Responsabilidad general
Construir toda la infraestructura que conecta la app con el mundo: la base de datos real, la API externa, y los ViewModels que procesan esa información.

---

### 📁 Archivos que SON tuyos (solo vos los tocás)

```
data/
├── local/
│   └── dao/PurchaseDao.kt                        ← agregar queries nuevas
├── network/
│   ├── SuperAhorroApiService.kt                  ← NUEVO: interfaz GET + POST
│   ├── NetworkModule.kt                          ← agregar segundo Retrofit
│   └── dto/
│       ├── SupermercadoDto.kt                    ← NUEVO
│       ├── CompraRemotaDto.kt                    ← NUEVO
│       └── CompraRemotaRespuestaDto.kt           ← NUEVO
├── repository/
│   └── SincronizacionRepository.kt              ← NUEVO
AppContainer.kt                                   ← agregar nuevas dependencias

ui/viewmodel/
├── PurchaseViewModel.kt                          ← agregar sincronizarCompra()
├── HomeViewModel.kt                              ← verificar que el Flow funciona
├── RecordsViewModel.kt                           ← verificar filtros y estadísticas
└── FintrackViewModelFactory.kt                   ← registrar nuevo repositorio
```

---

### ✅ Tareas concretas paso a paso

**TAREA A-1: Ampliar el DAO**
- Abrir `data/local/dao/PurchaseDao.kt`
- Agregar `getPurchaseWithProductsById(purchaseId: Long)` con `@Transaction` y `suspend`
- Agregar `observePurchasesByPeriod(startMillis, endMillis)` que retorna `Flow` (sin `suspend`)
- NO cambiar la versión de la base de datos si no agregás columnas

**TAREA A-2: Crear los DTOs de red**
- Crear carpeta `data/network/dto/`
- Crear `SupermercadoDto.kt` (campos: `id: Int`, `title: String`, `body: String`)
- Crear `CompraRemotaDto.kt` (campos: `titulo: String`, `detalle: String`, `usuarioId: Int = 1`)
- Crear `CompraRemotaRespuestaDto.kt` (campos: `id: Int`, `titulo: String`, `detalle: String`)
- Usar `@Json(name = "...")` de Moshi para mapear los nombres del JSON

**TAREA A-3: Crear `SuperAhorroApiService`**
- Crear `data/network/SuperAhorroApiService.kt`
- Definir interfaz con:
  - `@GET("posts") suspend fun obtenerSupermercados(): List<SupermercadoDto>`
  - `@POST("posts") suspend fun sincronizarCompra(@Body compra: CompraRemotaDto): CompraRemotaRespuestaDto`
- Base URL: `https://jsonplaceholder.typicode.com/`

**TAREA A-4: Actualizar `NetworkModule`**
- Abrir `data/network/NetworkModule.kt`
- Agregar una segunda instancia de Retrofit llamada `superAhorroApiService`
- NO tocar el `apiService` existente (er-api.com)

**TAREA A-5: Crear `SincronizacionRepository`**
- Crear `data/repository/SincronizacionRepository.kt`
- Implementar `obtenerSupermercadosRemotos(): Result<List<SupermercadoDto>>`
- Implementar `sincronizarCompra(dto: CompraRemotaDto): Result<CompraRemotaRespuestaDto>`
- Ambos métodos con `withContext(Dispatchers.IO)` y `try/catch` obligatorio

**TAREA A-6: Actualizar `AppContainer`**
- Instanciar `SincronizacionRepository` en `AppContainer.kt`
- Agregar al `FintrackViewModelFactory`

**TAREA A-7: Ampliar `PurchaseViewModel`**
- Crear sealed class `SincronizacionEstado` (Inactivo, Cargando, Exito, Error)
- Agregar `_estadoSincronizacion: MutableStateFlow<SincronizacionEstado>`
- Agregar función `sincronizarCompra(compra: PurchaseEntity)`
- Agregar función `resetearEstadoSincronizacion()`

**TAREA A-8: Verificar flujo Room → UI**
- Confirmar que agregar una compra en `NewPurchaseScreen` actualiza `HomeScreen` automáticamente
- Si no funciona, revisar que `observePurchasesWithProducts()` retorna `Flow` (sin `suspend`)
- Testear manualmente: agregar compra → ir a Home → ver que aparece sin reiniciar la app

---

### 📐 Reglas que el profesor evalúa en tu parte

- `withContext(Dispatchers.IO)` en **todas** las operaciones de red y base de datos del Repository
- El ViewModel usa `viewModelScope.launch`, **nunca** `GlobalScope`
- `Flow` en el DAO **sin** `suspend` (son incompatibles)
- `suspend fun` en escrituras del DAO **con** `suspend`
- Manejo de errores con `try/catch` en cada llamada a la API
- `Result.success()` / `Result.failure()` para comunicar éxito/error al ViewModel

---

## 👤 INTEGRANTE B — Capa de UI, Pantallas e Intents

### Responsabilidad general
Conectar la infraestructura que armó el Integrante A con lo que el usuario ve y toca. Agregar los Intents, los menús, los diálogos y el feedback visual de todas las operaciones.

---

### 📁 Archivos que SON tuyos (solo vos los tocás)

```
ui/
├── screens/
│   ├── NewPurchaseScreen.kt         ← agregar feedback de sincronización
│   ├── RecordsScreen.kt             ← conectar con datos reales de Room
│   └── ExploreScreen.kt             ← conectar GET real desde ViewModel
├── components/
│   └── records/
│       └── HistoryRecordCard.kt     ← agregar botón compartir + DropdownMenu

res/
├── values/strings.xml               ← agregar strings nuevos
└── values-en/strings.xml            ← NUEVO: traducción al inglés
```

---

### ✅ Tareas concretas paso a paso

**TAREA B-1: Intent de compartir compra en `HistoryRecordCard`**
- Abrir `ui/components/records/HistoryRecordCard.kt`
- Agregar función local `compartirCompra(purchase: PurchaseWithProducts)` que:
  - Arma un texto con supermercado, fecha y total
  - Lanza `Intent(Intent.ACTION_SEND)` con `type = "text/plain"`
  - Usa `Intent.createChooser()` para que el usuario elija la app
- Agregar un `IconButton` con `Icons.Filled.Share` que llame a esa función

**TAREA B-2: Reemplazar íconos sueltos por DropdownMenu en `HistoryRecordCard`**
- En el mismo archivo, reemplazar los dos `IconButton` (editar y eliminar) por un solo botón de tres puntos (`Icons.Default.MoreVert`)
- Agregar un `DropdownMenu` con tres opciones: Editar, Compartir, Eliminar
- El estado `var mostrarMenu by remember { mutableStateOf(false) }` va dentro del Composable

**TAREA B-3: Feedback de sincronización en `NewPurchaseScreen`**
- Abrir `ui/screens/NewPurchaseScreen.kt`
- Agregar `val estadoSincronizacion by viewModel.estadoSincronizacion.collectAsStateWithLifecycle()`
- Agregar `LaunchedEffect(estadoSincronizacion)` que muestre Snackbar según el estado:
  - `Exito` → "✓ Compra sincronizada correctamente"
  - `Error` → "⚠ No se pudo sincronizar: [mensaje]"
  - Al final de cada caso llamar `viewModel.resetearEstadoSincronizacion()`
- Modificar el botón "Guardar" para que el Snackbar tenga una acción "Sincronizar"

**TAREA B-4: Conectar `ExploreScreen` con datos reales del GET**
- El `ExploreViewModel` actualmente tiene los datos de supermercados hardcodeados
- Coordinar con el Integrante A para saber cuándo termina `SincronizacionRepository`
- Una vez disponible, modificar `ExploreViewModel.loadData()` para que llame al repositorio en lugar de devolver la lista fija
- La pantalla ya maneja los estados Loading / Success / Error, solo hay que conectar el dato real

**TAREA B-5: Verificar `RecordsScreen` con datos reales**
- Confirmar que el historial de compras muestra datos de Room (no del seed únicamente)
- Confirmar que agregar una compra desde `NewPurchaseScreen` aparece en el listado sin reiniciar
- Si hay algún problema visual con la lista vacía, ajustar el mensaje de `EmptyHistoryCard`

**TAREA B-6: Internacionalización — `strings.xml`**
- Revisar todos los archivos de pantalla y componentes
- Asegurarse de que no haya ningún texto hardcodeado (buscar comillas con texto en español directo en el código)
- Agregar en `res/values/strings.xml` cualquier string que falte
- Crear `res/values-en/strings.xml` con la traducción al inglés de al menos:
  - Todos los textos de navegación (Home, Explorar, Nueva Compra, Historial, Perfil)
  - Los botones principales (Guardar, Eliminar, Editar, Compartir, Cancelar)
  - Los mensajes de error y éxito

---

### 📐 Reglas que el profesor evalúa en tu parte

- Los Composables **no acceden al Repository** directamente, solo llaman métodos del ViewModel
- `collectAsStateWithLifecycle()` en lugar de `collectAsState()` (es el correcto para Compose)
- El Intent de compartir usa `Intent.createChooser()` (permite elegir la app, no abre una fija)
- Todos los textos visibles al usuario tienen su entrada en `strings.xml`
- Los estados de UI se manejan con `when(estado)` exhaustivo (Loading, Success, Error)

---

## 🤝 Archivos COMPARTIDOS (coordinar antes de tocar)

Estos archivos los pueden necesitar los dos. Ponerse de acuerdo antes de editarlos y hacer commits chicos y frecuentes:

| Archivo | ¿Quién lo toca primero? | ¿Por qué puede necesitarlo el otro? |
|---|---|---|
| `res/values/strings.xml` | Integrante B | Integrante A puede necesitar agregar strings de error |
| `ui/viewmodel/ExploreViewModel.kt` | Integrante A (agrega llamada al repo) | Integrante B (conecta con la pantalla) |
| `FintrackApp.kt` | Ninguno idealmente | Solo si se agrega una ruta nueva |

**Regla para los archivos compartidos:** el que lo toca avisa por chat al otro antes de hacer el commit. Si los dos lo modificaron, hacer merge manualmente comparando los cambios.

---

## 🌿 Estrategia de ramas en GitHub

Para no pisarse, trabajar así:

```
main (rama principal — solo código que funciona)
├── feature/datos-y-red          ← Integrante A trabaja acá
└── feature/ui-e-intents         ← Integrante B trabaja acá
```

**Flujo de trabajo:**

```
1. Cada uno crea su rama desde main
   git checkout -b feature/datos-y-red

2. Trabajar y hacer commits chicos con mensajes descriptivos
   git commit -m "feat: agregar SuperAhorroApiService con GET y POST"
   git commit -m "feat: crear SincronizacionRepository con manejo de errores"

3. Cuando una tarea está terminada y compilando, hacer push
   git push origin feature/datos-y-red

4. Abrir un Pull Request hacia main
5. El otro integrante revisa y aprueba (o comenta)
6. Merge a main
7. El otro hace git pull en su rama para actualizar
   git merge main
```

---

## 📅 Orden recomendado de trabajo

Para que el Integrante B no quede bloqueado esperando al A:

```
DÍA 1-2
  A: Tareas A-1 a A-4 (DAO + DTOs + ApiService + NetworkModule)
  B: Tareas B-2 y B-1 (DropdownMenu + Intent compartir) — no dependen de A

DÍA 3-4
  A: Tareas A-5 a A-7 (Repository + AppContainer + ViewModel)
  B: Tareas B-5 y B-6 (verificar RecordsScreen + internacionalización)

DÍA 5
  A: Tarea A-8 (verificar flujo Room → UI)
  B: Tareas B-3 y B-4 (feedback sincronización + conectar ExploreScreen con GET real)
  → Acá se integran las dos partes, coordinar

DÍA 6
  Ambos: pruebas manuales completas, fix de bugs, README, APK
```

---

## 📊 Comparación de lo que entrega cada uno al profesor

| Concepto del profesor | Integrante A | Integrante B |
|---|---|---|
| Room (BD local) | ✅ DAO + queries | ✅ Visualización en pantalla |
| DataStore | ✅ Verifica persistencia de sesión | — |
| Networking GET | ✅ Configura el service y repository | ✅ Conecta con ExploreScreen |
| Networking POST | ✅ Implementa sincronizarCompra() | ✅ Botón que lo dispara + feedback |
| Corrutinas | ✅ withContext + viewModelScope | ✅ LaunchedEffect + collectAsStateWithLifecycle |
| Intents | — | ✅ Compartir compra |
| Menús y diálogos | — | ✅ DropdownMenu contextual |
| StateFlow | ✅ estadoSincronizacion | ✅ Observa y reacciona al estado |
| Internacionalización | — | ✅ strings.xml + values-en |
| MVVM (arquitectura) | ✅ Repository + ViewModel | ✅ Composable + observación |

Los dos tocan corrutinas, los dos tocan StateFlow, los dos aplican MVVM. El profesor puede preguntarle a cualquiera sobre cualquier concepto y los dos van a poder responder desde su propia experiencia de haberlo implementado.

---

> **Consejo final:** hagan commits chicos y con mensajes claros. Un commit por tarea es ideal. Si algo no compila, no lo suban a `main`. Usen las ramas para eso.
