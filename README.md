# 📱 FintrackMobile — Super Ahorro

> Trabajo Práctico Integrador · Materia **Tecnologías Móviles** · 2026  
> Universidad de la Defensa Nacional (UNDEF)

Aplicación Android para registrar, consultar y analizar gastos de supermercado, permitiendo al usuario llevar un mejor control de sus compras y detectar oportunidades de ahorro.

Link demo v1: https://drive.google.com/file/d/1htavD_bSEjQ8O3MailvDOJT8khzOK8vE/view

Link demo v2: https://drive.google.com/file/d/1hB5-7Jzt8AE3i2rVbFzvpz1rzoEbQ2-Q/view?usp=sharing

---

## 📋 Descripción

**Super Ahorro** permite al usuario:

- Registrarse, iniciar sesión y cerrar sesión.
- Gestionar su perfil de usuario.
- Registrar compras con fecha, hora, supermercado y total abonado.
- Agregar, editar y eliminar los productos asociados a cada compra.
- Visualizar el listado de las últimas compras y consultar el historial completo.
- Ver estadísticas y gráficos de gastos (por período, por supermercado, evolución mensual).
- Adjuntar o capturar una imagen del ticket de compra desde galería o cámara.

---

## ✅ Requisitos funcionales cubiertos

| # | Requisito | Estado |
|---|---|---|
| 1 | Pantalla de bienvenida / Splash | ✅ |
| 2 | Flujo de registro, login y logout | ✅ |
| 3 | Pantalla Mi Perfil con edición de datos | ✅ |
| 4 | Persistencia de sesión con DataStore | ✅ |
| 5 | Registro de compras (fecha, hora, supermercado, total) | ✅ |
| 6 | Edición y eliminación de compras | ✅ |
| 7 | Listado de últimas compras | ✅ |
| 8 | Gestión de productos por compra (CRUD) | ✅ |
| 9 | Detalle de compra con productos asociados | ✅ |
| 10 | Historial de compras ordenado por fecha | ✅ |
| 11 | Estadísticas de gastos con gráficos | ✅ |
| 12 | Adjuntar imagen de ticket (galería / cámara) | ✅ |
| 13 | Consumo de API externa con corrutinas | ✅ |
| 14 | Navigation Compose como sistema de navegación principal | ✅ |
| 15 | Uso de Intents para interacción con el sistema | ✅ |

---

## 🏗️ Arquitectura

El proyecto sigue el patrón **MVVM + Clean Architecture** con separación clara entre capas:

```
┌─────────────────────────────────────────┐
│     UI Layer  (Jetpack Compose)         │
│  Composables · NavHost · collectAsState │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│     Presentation Layer  (ViewModel)     │
│  StateFlow · viewModelScope · combine() │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│     Domain Layer  (Repository)          │
│  suspend fun · Dispatchers.IO           │
└───────┬──────────────────────┬──────────┘
        │                      │
  ┌─────▼──────┐         ┌─────▼──────┐
  │  Room DB   │         │  Retrofit  │
  │  (Local)   │         │  (Network) │
  └────────────┘         └────────────┘
        │
  ┌─────▼──────┐
  │  DataStore │
  │  (Prefs)   │
  └────────────┘
```

**Flujo de datos:** el usuario interactúa con un Composable → el ViewModel lanza una corrutina en `viewModelScope` → el Repository ejecuta la operación en `Dispatchers.IO` → el resultado se emite por un `Flow` → el ViewModel lo transforma en un `StateFlow` → el Composable recompone automáticamente.

---

## 📂 Pantallas

| Pantalla | Descripción |
|---|---|
| `SplashScreen` | Pantalla de inicio con animación de carga |
| `WelcomeScreen` | Presentación de la app y acceso a login/registro |
| `LoginScreen` | Autenticación de usuario |
| `RegisterScreen` | Creación de cuenta nueva |
| `HomeScreen` | Dashboard con resumen de gastos y últimas compras |
| `NewPurchaseScreen` | Registro de una nueva compra |
| `NewProductScreen` | Alta de producto dentro de una compra |
| `PurchaseDetailScreen` | Detalle completo de una compra con sus productos |
| `HistoryScreen` | Historial de compras ordenado por fecha |
| `StatisticsScreen` | Estadísticas y gráficos de gastos |
| `ProfileScreen` | Visualización y edición del perfil de usuario |
| `SettingsScreen` | Configuración de la cuenta (en `SettingsActivity`) |

---

## 🔍 Detalle de cada concepto aplicado

### 1️⃣ Activity — Single-Activity Architecture
Toda la aplicación vive en un único `MainActivity` (`ComponentActivity`). La navegación entre pantallas se delega a Navigation Compose, evitando la complejidad del modelo tradicional multi-Activity. `SettingsActivity` es la excepción deliberada: una Activity independiente que demuestra el uso de Intents y el backstack del sistema Android.

### 2️⃣ Jetpack Compose — UI Declarativa
La interfaz está construida enteramente con funciones `@Composable`. Se aplica **State Hoisting** para separar la lógica de negocio de la presentación: los Composables reciben el estado como parámetro y emiten eventos hacia arriba.

### 3️⃣ Navigation Compose — Router Centralizado
Las rutas están definidas como `sealed class` en `FintrackDestinations`, garantizando *type-safety* en tiempo de compilación. El `NavHost` observa el `NavController` y recompone el Composable correspondiente ante cada cambio de ruta. El backstack es manejado automáticamente; `popUpTo()` descarta el flujo de login tras autenticarse.

### 4️⃣ Intents — Comunicación entre Componentes
`SettingsActivity` se lanza desde `ProfileScreen` mediante un Intent explícito usando el patrón *factory method* (`SettingsActivity.intent(context)`), encapsulando la creación del Intent y garantizando *type-safety*. Ilustra la diferencia entre navegación interna (Navigation Compose) y navegación entre Activities (sistema Android).

### 5️⃣ DataStore — Persistencia de Preferencias
Reemplaza a `SharedPreferences` con una API basada en corrutinas y `Flow`. Se persisten datos del usuario (nombre, email, estado de sesión) usando claves tipadas (`stringPreferencesKey`, `booleanPreferencesKey`). Los cambios se propagan reactivamente a cualquier observer.

### 6️⃣ Room — Base de Datos Local
Capa de abstracción sobre SQLite con acceso *type-safe* mediante anotaciones (`@Entity`, `@Dao`, `@Query`). El modelo implementa una relación **1:N** entre Compras y Productos. Las consultas retornan `Flow<List<T>>` para observación reactiva, y las operaciones de escritura usan `suspend fun` con soporte para `@Transaction`.

### 7️⃣ Networking — Retrofit + API REST
`SupabaseDataApiService` define la interfaz de comunicación con Supabase mediante anotaciones Retrofit (`@GET`, `@POST`, `@PATCH`). El mapeo de JSON se realiza con Moshi. `SincronizacionRepository` ejecuta las llamadas en `Dispatchers.IO`, gestionando la persistencia remota de compras y la obtención de ofertas y supermercados en tiempo real.

### 8️⃣ Corrutinas — Asincronismo Estructurado
Las operaciones de I/O se escriben de forma **secuencial y legible** gracias a `suspend fun` y `withContext(Dispatchers.IO)`. No hay callbacks ni bloqueo del hilo principal. El scope `viewModelScope` cancela automáticamente todas las corrutinas activas al destruir el ViewModel, previniendo memory leaks sin código adicional. Se utiliza en todos los repositorios para interactuar con Room y Retrofit.

### 9️⃣ ViewModel + StateFlow — Estado Reactivo
Cada pantalla tiene su propio ViewModel que expone un único `StateFlow<UiState>`. El operador `combine()` orquesta múltiples flows (compras, productos, preferencias) en un **único estado coherente**, evitando sincronización manual. La estrategia `WhileSubscribed(5_000)` detiene el flujo cuando no hay observers activos, optimizando recursos. Los Composables observan el estado con `collectAsStateWithLifecycle()`, que pausa la recolección cuando la UI está en segundo plano.

### 🔟 Sealed Classes — Modelos de Estado Tipados
Los estados de UI se modelan con `sealed class` (`Loading`, `Success`, `Error`), garantizando exhaustiveness en los bloques `when`. El compilador obliga a manejar todos los casos, haciendo imposibles los estados inválidos. Cada rama lleva datos tipados asociados (`data class` vs `object`).

---

## 🛠️ Stack tecnológico

| Categoría | Tecnología |
|---|---|
| **Lenguaje** | Kotlin |
| **UI** | Jetpack Compose |
| **Navegación** | Navigation Compose |
| **Arquitectura** | MVVM + Clean Architecture · Repository Pattern |
| **Base de datos** | Room (SQLite) |
| **Preferencias** | DataStore |
| **Networking** | Retrofit + Moshi |
| **Asincronismo** | Corrutinas de Kotlin · StateFlow · Flow |
| **Ciclo de vida** | ViewModel · `viewModelScope` · `collectAsStateWithLifecycle` |
| **Internacionalización** | `strings.xml` |

---

## 📦 Conceptos mínimos exigidos por la materia

| Concepto requerido | Implementación | Archivo |
|---|---|---|
| **Activity** | `MainActivity` (Single-Activity) + `SettingsActivity` | `MainActivity.kt` |
| **Compose** | Todas las pantallas | `ui/screens/` |
| **Navigation Compose** | `NavHost` con rutas `sealed class` | `FintrackApp.kt` |
| **Intents** | Lanzamiento de `SettingsActivity` desde `ProfileScreen` | `FintrackApp.kt` |
| **DataStore** | Sesión y datos del usuario | `UserPreferencesRepository.kt` |
| **Room** | Compras y productos con relación 1:N | `AppDatabase.kt` · `PurchaseDao.kt` |
| **Networking** | Consulta de datos externos vía API REST | `SupabaseDataApiService.kt` |
| **Corrutinas** | Todas las operaciones asincrónicas de I/O | `PurchaseRepository.kt` |

---

## 🚀 Buenas prácticas implementadas

- **State Hoisting** en todos los Composables
- **Single Source of Truth** mediante `StateFlow` por pantalla
- **Repository Pattern** para abstracción del origen de datos
- **Inyección de dependencias manual** mediante `AppContainer`
- **Manejo de errores tipado** con `sealed class`
- **Type-safety** de extremo a extremo (rutas, base de datos, red, estado)
- **Cancellación automática** de corrutinas al destruir el ViewModel
- **Internacionalización** con `strings.xml`
- **Paquete** siguiendo la estructura `com.undef.fintrackmobile`

---

## 🗂️ Estructura del proyecto

```
app/src/main/java/com/undef/fintrackmobile/
├── MainActivity.kt
├── SettingsActivity.kt
├── FintrackApp.kt
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt
│   │   └── dao/
│   │       └── PurchaseDao.kt
│   ├── network/
│   │   ├── SupabaseDataApiService.kt
│   │   └── SupabaseAuthApiService.kt
│   ├── preferences/
│   │   └── UserPreferencesRepository.kt
│   └── repository/
│       ├── PurchaseRepository.kt
│       └── SincronizacionRepository.kt
└── ui/
    ├── navigation/
    │   └── FintrackDestinations.kt
    ├── screens/
    │   ├── SplashScreen.kt
    │   ├── WelcomeScreen.kt
    │   ├── LoginScreen.kt
    │   ├── RegisterScreen.kt
    │   ├── HomeScreen.kt
    │   ├── ProfileScreen.kt
    │   └── SettingsScreen.kt
    ├── components/
    └── viewmodel/
        └── HomeViewModel.kt
```
